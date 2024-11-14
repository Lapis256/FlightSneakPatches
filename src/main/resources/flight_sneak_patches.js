var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var targetMethod = ASMAPI.mapMethod("m_6047_"); // isCrouching
var replacementMethod = ASMAPI.mapMethod("m_6144_"); // isShiftKeyDown


function initializeCoreMod() {
    return {
        "flight_sneak_patches": {
            "target": {
                "type": "CLASS",
                "names": function (_) {
                    return [
                        "me.desht.modularrouters.item.module.ModuleItem",
                        "me.desht.modularrouters.item.module.TargetedModule",
                        "thetadev.constructionwand.client.RenderBlockPreview",
                        "thetadev.constructionwand.client.ClientEvents",
                        "thetadev.constructionwand.items.wand.ItemWand",
                        "vazkii.morphtool.ClientHandler",
                        "vazkii.morphtool.MorphingHandler",
                        "net.gigabit101.shrink.items.ItemShrinkingDevice",
                        "ironfurnaces.blocks.furnaces.BlockIronFurnaceBase",
                        "ironfurnaces.items.ItemFurnaceCopy"
                    ];
                }
            },
            "transformer": function (classNode) {
                var methods = classNode.methods;
                for (var i = 0; i < methods.size(); i++) {
                    var method = methods.get(i);
                    var instructions = method.instructions;
                    for (var j = 0; j < instructions.size(); j++) {
                        var node = instructions.get(j);
                        if (node.getOpcode() === Opcodes.INVOKEVIRTUAL && node.name === targetMethod) {
                            node.name = replacementMethod;
                            ASMAPI.log("INFO", "Replaced isCrouching to isShiftKeyDown in " + classNode.name + "#" + method.name);
                        }
                    }
                }

                return classNode;
            }
        }
    }
}
