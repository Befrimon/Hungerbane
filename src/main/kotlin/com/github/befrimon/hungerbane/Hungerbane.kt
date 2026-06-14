package com.github.befrimon.hungerbane

import com.github.befrimon.hungerbane.client.HungerbaneConfigScreen
import com.github.befrimon.hungerbane.config.HungerbaneConfig
import com.github.befrimon.hungerbane.game.*
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import org.slf4j.Logger
import com.mojang.logging.LogUtils
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.ModContainer
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.client.gui.IConfigScreenFactory

@Mod(Hungerbane.MOD_ID)
class Hungerbane(modEventBus: IEventBus, modContainer: ModContainer) {
    companion object {
        const val MOD_ID = "hungerbane"
        val LOGGER: Logger = LogUtils.getLogger()
    }

    init {
        LOGGER.info("Initializing HungerBane...")

        ModEffects.EFFECTS.register(modEventBus)
        modContainer.registerConfig(ModConfig.Type.COMMON, HungerbaneConfig.SPEC)

        LOGGER.info("HungerBane initialized!")
    }
}

@Mod(value = Hungerbane.MOD_ID, dist = [Dist.CLIENT])
class HungerbaneClient(modContainer: ModContainer) {
    init {
        modContainer.registerExtensionPoint(
            IConfigScreenFactory::class.java,
            java.util.function.Supplier {
                IConfigScreenFactory { _, parent ->
                    HungerbaneConfigScreen(parent)
                }
            }
        )
    }
}
