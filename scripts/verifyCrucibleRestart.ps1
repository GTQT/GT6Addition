# Two independent server JVMs, using a fresh build-only world. Never remove a
# world or accept EULA. Each Gradle phase validates its own terminal evidence.
$ErrorActionPreference = 'Stop'
$restartProjectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$restartWorldName = 'parity-smoke-restart-' + [Guid]::NewGuid().ToString()
$restartEvidenceRoot = Join-Path $restartProjectRoot ('build/reports/crucible-parity/' + $restartWorldName)
$null = New-Item -ItemType Directory -Path $restartEvidenceRoot
Push-Location -LiteralPath $restartProjectRoot
try {
    foreach ($restartStage in @('write', 'verify')) {
        & (Join-Path $restartProjectRoot 'gradlew.bat') verifyCrucibleForgeStartup '-Pgt6ParitySmoke' '-Pgt6ParityWorld' "-Pgt6ParityRestartPhase=$restartStage" "-Pgt6ParityRestartWorld=$restartWorldName" '--console=plain'
        if ($LASTEXITCODE -ne 0) { throw "Crucible restart stage failed: $restartStage, world=$restartWorldName" }
        Copy-Item -LiteralPath (Join-Path $restartProjectRoot 'build/crucible-parity-smoke/logs/latest.log') -Destination (Join-Path $restartEvidenceRoot "$restartStage-latest.log")
        Copy-Item -LiteralPath (Join-Path $restartProjectRoot 'build/crucible-parity-smoke/logs/debug.log') -Destination (Join-Path $restartEvidenceRoot "$restartStage-debug.log")
    }
    Write-Output "CRUCIBLE_RESTART_TWO_PROCESSES_PASS world=$restartWorldName evidence=$restartEvidenceRoot"
} finally {
    Pop-Location
}
