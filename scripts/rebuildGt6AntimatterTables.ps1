param(
    [string]$Gt6SourceRoot
)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($Gt6SourceRoot)) {
    # Build the user's default path from Unicode code points so this script
    # also works in legacy Windows PowerShell when the file has no UTF-8 BOM.
    $downloadDirectoryName = -join @([char]0x8FC5, [char]0x96F7, [char]0x4E0B, [char]0x8F7D)
    $Gt6SourceRoot = Join-Path 'E:\' (Join-Path $downloadDirectoryName 'gregtech6-master')
}
$amPath = Join-Path $Gt6SourceRoot 'src\main\java\gregapi\data\AM.java'
$expectedHash = '8F012B6BA27396BB799882B91EA4C46BB011F4FC1B6C150233F8078A4403BEEE'
$actualHash = (Get-FileHash -LiteralPath $amPath -Algorithm SHA256).Hash
if ($actualHash -ne $expectedHash) {
    throw "AM.java source hash mismatch. Expected $expectedHash, got $actualHash"
}

$lines = [System.IO.File]::ReadAllLines($amPath, [System.Text.Encoding]::UTF8)
$materialRows = [System.Collections.Generic.List[string]]::new()
$sourceRows = [System.Collections.Generic.List[string]]::new()
$symbols = @{}
$declarationPattern = [regex]'=\s*(?<factory>create|element|metal|metalloid|nonmetal|diatomic|polyatomic|noblegas|alkali|alkaline|lanthanide|actinide|transmetal|posttrans)\s*\(\s*(?<id>\d+)\s*,\s*"(?<name>[^"]+)"'
$symbolPattern = [regex]'^\s*(?<symbol>[A-Za-z_]\w*)\s*,\s*[A-Za-z_]\w*\s*=\s*\k<symbol>\s*=\s*[A-Za-z_]\w*\s*\(\s*\d+\s*,\s*"(?<name>[^"]+)"'
$aliasCallPattern = [regex]'\.addIdenticalNames\((?<args>[^)]*)\)'
$stringPattern = [regex]'"(?<value>[^"]+)"'
$thermalPattern = [regex]',\s*[^,]+\s*,\s*[^,]+\s*,\s*(?<melt>\d+)\s*,\s*(?<boil>\d+)\s*,\s*(?<density>\d+(?:\.\d+)?)'

for ($index = 0; $index -lt $lines.Length; $index++) {
    $line = $lines[$index]
    $symbolMatch = $symbolPattern.Match($line)
    if ($symbolMatch.Success) {
        $symbols[$symbolMatch.Groups['symbol'].Value] = $symbolMatch.Groups['name'].Value
    }

    $match = $declarationPattern.Match($line)
    if (-not $match.Success) { continue }

    $id = [int]$match.Groups['id'].Value
    $name = $match.Groups['name'].Value
    $factory = $match.Groups['factory'].Value
    if ($factory -eq 'create') {
        $melt = 0
        $boil = 0
        $density = '1.0'
    } else {
        $thermal = $thermalPattern.Match($line, $match.Index + $match.Length)
        if (-not $thermal.Success) {
            throw "Could not read AM scalar fields at AM.java:$($index + 1): $line"
        }
        $melt = [int]$thermal.Groups['melt'].Value
        $boil = [int]$thermal.Groups['boil'].Value
        $density = $thermal.Groups['density'].Value
    }
    $meltingFlag = if ($line -match '\.put\([^;]*\bMELTING\b') { 1 } else { 0 }
    $row = "$id|$name|$melt|$boil|$density|$meltingFlag"
    $materialRows.Add($row)
    $sourceRows.Add($row)
}

if ($materialRows.Count -ne 418) {
    throw "Expected 418 AM.java material declarations, found $($materialRows.Count)"
}

$aliasRows = [System.Collections.Generic.List[string]]::new()
$normalizedAliases = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::Ordinal)
for ($index = 0; $index -lt $lines.Length; $index++) {
    $line = $lines[$index]
    $symbolMatch = $symbolPattern.Match($line)
    if (-not $symbolMatch.Success) { continue }
    $targetName = $symbolMatch.Groups['name'].Value
    foreach ($call in $aliasCallPattern.Matches($line)) {
        foreach ($alias in $stringPattern.Matches($call.Groups['args'].Value)) {
            $aliasName = $alias.Groups['value'].Value
            $aliasRows.Add("$aliasName|$targetName")
            $normalized = [regex]::Replace($aliasName.ToLowerInvariant(), '[^a-z0-9]', '')
            [void]$normalizedAliases.Add($normalized)
        }
    }
}

if ($aliasRows.Count -ne 22 -or $normalizedAliases.Count -ne 21) {
    throw "Expected 22 AM aliases / 21 normalized aliases, found $($aliasRows.Count) / $($normalizedAliases.Count)"
}

$resourcesMain = Join-Path (Get-Location) 'src\main\resources'
$resourcesTest = Join-Path (Get-Location) 'src\test\resources'
[System.IO.Directory]::CreateDirectory($resourcesMain) | Out-Null
[System.IO.Directory]::CreateDirectory($resourcesTest) | Out-Null
$encoding = [System.Text.UTF8Encoding]::new($false)
[System.IO.File]::WriteAllLines((Join-Path $resourcesMain 'gt6-antimatter-materials.txt'), @("# AM_SHA256=$actualHash") + $materialRows, $encoding)
[System.IO.File]::WriteAllLines((Join-Path $resourcesMain 'gt6-antimatter-aliases.txt'), @("# AM_SHA256=$actualHash") + (($aliasRows | ForEach-Object { $parts = $_.Split('|'); ([regex]::Replace($parts[0].ToLowerInvariant(), '[^a-z0-9]', '') + '|' + [regex]::Replace($parts[1].ToLowerInvariant(), '[^a-z0-9]', '')) } | Sort-Object -Unique)), $encoding)
[System.IO.File]::WriteAllLines((Join-Path $resourcesTest 'gt6-antimatter-source-values.txt'), @("# AM_SHA256=$actualHash") + $sourceRows, $encoding)
[System.IO.File]::WriteAllLines((Join-Path $resourcesTest 'gt6-antimatter-source-aliases.txt'), @("# AM_SHA256=$actualHash") + $aliasRows, $encoding)

Write-Output "Rebuilt 418 AM identity rows and 22 alias source rows (21 normalized aliases) from $amPath"
