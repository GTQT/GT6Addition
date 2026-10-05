# Mixin 目标与直接实现核对

核对范围：`src/main/java`中的全部`@Mixin`、early/late JSON和两个加载器。约束：本模组可控的实现直接修改所属类；外部调用本模组方法不等于注入本模组类。判断目标以`@Mixin`及其import为准，不能只看类名的Crucible/Anthracite前缀。

## 本轮迁回本模组的实现

| 原Mixin | 原注入目标 | 直接实现与移除结果 |
| --- | --- | --- |
| CrucibleEntityCollisionMixin | `net.minecraft.block.Block` | 在`MetaTileEntityCrucible.update`每服务端tick检查块内生物并执行温度接触/致死回收；检查范围缩进0.001，包含空心内部，不扩大到相邻块。移除Mixin源码及early配置项。 |
| ElectricContainerInfoProviderMixin | `gregtech.integration.theoneprobe.provider.ElectricContainerInfoProvider` | `TopCommonProvider`直接显示多能量代理的可用量；本模组机器通过自己创建的EnergyContainerHandler覆写`isOneProbeHidden`，使用CEu既有接口隐藏其EU条。保留原工作机器接收电流行为，移除Mixin源码及late配置项。 |

以上两项原本也没有以本模组类为注入目标，但为了实现本模组功能拦截外部入口，现已迁回所属源码。接触检查从原版实体回调改到机器tick，仍每tick处理；具体实体移动时序和TOP显示待客户端运行核验。

## 保留的12个外部接入点

| Mixin | 实际目标 | 保留原因 |
| --- | --- | --- |
| BlockMachineComparatorMixin | `net.minecraft.block.Block` | CEu机器承载方块继承原版比较器入口，没有查询本模组比较器接口的回调；该桥只为机器方块提供入口，数值由自己的传送门实现计算。 |
| PlayerBlockRemovalMixin | `net.minecraft.server.management.PlayerInteractionManager` | CEu的onRemoval没有玩家参数，创造模式高温拆除豁免需要实际拆除玩家。只在Forge已通过取消、工具和权限检查后的removedByPlayer调用期间传入实例上下文，try/finally恢复；危险逻辑直接写在自家坩埚中，其他机器照常拆除。 |
| MetaItemLighterBehaviourMixin | `gregtech.common.items.behaviors.LighterBehaviour` | CEu打火机/火柴在方块交互前消费物品并执行点火，须在其onItemUseFirst桥接本模组可点燃机器与门。 |
| GTFlintAndSteelToolBehaviorMixin | `gregtech.common.items.tool.FlintAndSteelToolBehavior` | CEu打火石工具具有同样的提前物品交互入口；机器/门的激活逻辑仍在本模组。 |
| HeatNetTransferBudgetMixin | `gregtech.common.pipelike.heat.net.HeatNetHandler` | 真实世界复现128HU源经分支向两个坩埚累计送出并扣掉256HU；外部路由每条支路重复使用完整输入量。自家接收端无法知道源总预算，此桥仅限制每条调用不超过同次传输剩余额度，保留原网络/损耗/统计，预算计算直接在自家HeatTransferBudget中。 |
| CrucibleToolRecipeMixin | `gregtech.api.recipes.ModHandler` | 读取CEu及其他模组通过CEu注册的工具合成配方，包括符号展开、清NBT行为和返还工具；注册入口和实际配方来自外部。 |
| CrucibleToolMachineRecipeMixin | `gregtech.api.recipes.RecipeMap` | 记录外部机器配方成功注册后的实际工具材料，涵盖CEu/Core等配方。自家坩埚只读取回收记录。 |
| CrucibleElectricCraftingMixin | `gregtech.common.crafting.ShapedOreEnergyTransferRecipe` | CEu电动工具配方执行时才能获得实际选择的电池等输入；记录该次合成的材料，避免按EU数量猜材料。 |
| CrucibleElectricHeadReplacementMixin | `gregtech.common.crafting.ToolHeadReplaceRecipe` | CEu换头配方负责产生新工具，需要传递原动力部件材料和真实新工具头材料。 |
| CrucibleElectricToolBreakMixin | `gregtech.common.ToolEventHandlers` | CEu玩家工具损坏事件产生动力部件，需从原工具向返回物传递材料记录。 |
| CrucibleElectricCraftingBreakMixin | `gregtech.api.items.toolitem.ItemGTTool`、`ItemGTAxe`、`ItemGTHoe`、`ItemGTSword` | CEu工具在合成台作为工具使用至损坏的返回物流程，需保留实际动力部件材料记录。 |
| AnthraciteOreByProductMixin | `gregtech.integration.jei.basic.OreByProduct` | CEu矿物副产JEI总览是内部固定示意图，不由本模组真实机器配方生成；修正该外部示意图的无烟煤副产显示。 |

当前这12项没有`com.drppp.gt6addition`注入目标。它们导入本模组接口/helper，用于外部事件传入数据；材料解析、危险、机器激活和回收逻辑均直接位于本模组类。加载器自身的包名和Mixin配置的package字段只是插件位置，也不表示注入本模组。

## 核验边界

静态核对源码目标、import、JSON引用及被删除类引用；使用实际依赖JAR确认TOP的`allowDisplaying`读取`IEnergyContainer.isOneProbeHidden`。用户恢复验证后，最新完整build及265项测试已通过（含4项Mixin归属检查及3项玩家拆除边界字节码检查）；启用RFG注解处理、early/late资源展开和EarlyMixin核心插件入口，verifyMixinPackaging检查开发包及生产包的入口、配置、class和比较器SRG映射，并禁止发布测试专用smoketest类。独立Forge开发服务端已完成运行验收：11个通用Mixin覆盖的14个目标类均在实际变换字节码中含有对应钩子的调用。43项世界场景也已通过，包括原33项及10项真实缓存、流体边界、同材料相变合并、雨水及玩家蒸汽防护场景。客户端AnthraciteOreByProductMixin、生产重混淆环境及未列出的实际玩法仍未验收。后续新增Mixin须在此表记录外部目标和必要入口，不用Mixin改本模组类。

`MixinOwnershipTest` 已接入常规test/check，直接读取编译字节码的 `@Mixin` 注解，而非按类名推断目标：检查配置中的所有Mixin只指向外部类、主源码中的Mixin全部列入配置，禁止class literal或字符串targets指向本模组包。两个违规样本测试确认约束不会被字符串目标或内部斜杠类名绕过。这是自动化约束核验，不代表游戏内Mixin应用已成功。

玩家拆除桥接的两个方法都是Forge新增、没有原版SRG映射的方法，使用完整描述符且两处remap=false、require=1。已读取Forge补丁后SRG源码确认名称仍为removeBlock(BlockPos,boolean)及Block.removedByPlayer；三项自动化测试读取实际Minecraft依赖字节码及Mixin注解，确认该入口唯一、原版单参数拆除委托到相同入口、注解描述符与强制匹配设置一致。随后独立Forge启动和变换字节码检查确认了真实Redirect调用；再通过真实机器放置和tryHarvestBlock验证冷热/创造/取消/未处理物品掉落行为，发现并直接修复自家高温替换岩浆时外层空气写入返回false的问题。并未新增或扩大Mixin入口。

运行验收命令：`gradlew.bat verifyCrucibleForgeStartup -Pgt6ParitySmoke --console=plain`（JAVA_HOME使用文档中的Java 25，游戏进程使用项目Java 8）。仅显式指定此属性才改变runServer；测试世界/配置/日志位于build/crucible-parity-smoke，绑定127.0.0.1随机端口，无查询/RCON，正常初始化后自动stop。不读取现有run/world或saves，只复制既有eula=true决定；若原决定不存在则拒绝启动，不代用户接受EULA。该验收任务检查Done/正常停止、当前日志每个目标的应用记录，以及导出字节码中的真实钩子调用，不只检查合并后的方法名存在。常规build不会启动服务端。

加 `-Pgt6ParityWorld` 启用独立测试源集，使用每次唯一名称世界执行43项世界检查，验收须含CRUCIBLE_WORLD_PASS 43且无失败标记。创造持CEu扳手会被宿主Chisel兼容事件取消（实际工具被外部ItemGTToolChiselMixin添加IChiselItem，ChiselController取消创造持该接口物品的BreakEvent）；测试确认方块/NBT不变及换空手可拆除，不绕过其他模组的取消决定。这不是本项目注入自己的类。矿石倍率、缺失目标注册、氟石锭形态及相变合并修复直接在自家材料、物品和坩埚解析类实现，未新增Mixin；具体已验证范围和剩余形态问题详见坩埚实施文档。

热管预算桥使用实际依赖的transferHeat(JI)J及唯一IHeatable.transferHeat(JI)J调用，remap=false、require=1；预算开始/目标限量/返回清理三个钩子均检查实际变换调用。真实单管双目标确认不再绕过128HU源流率，原有CEu损耗提供量和温度显示流程未重写；递归和异常保护代码仍需复杂环路/异常目标运行验收。
