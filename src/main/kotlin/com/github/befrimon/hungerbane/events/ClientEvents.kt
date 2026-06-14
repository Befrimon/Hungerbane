package com.github.befrimon.hungerbane.events

import com.github.befrimon.hungerbane.Hungerbane
import com.github.befrimon.hungerbane.config.HungerbaneConfig
import com.github.befrimon.hungerbane.game.ModEffects
import com.mojang.blaze3d.systems.RenderSystem
import kotlin.math.*
import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffects
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent
import net.neoforged.neoforge.client.gui.VanillaGuiLayers
import net.neoforged.neoforge.event.tick.PlayerTickEvent

@EventBusSubscriber(modid = Hungerbane.MOD_ID, value = [Dist.CLIENT], bus = EventBusSubscriber.Bus.GAME)
object ClientEvents {
    private val FULL_HEART = ResourceLocation.withDefaultNamespace("hud/heart/full")
    private val HALF_HEART = ResourceLocation.withDefaultNamespace("hud/heart/half")

    var hungryMultiplier = 1.0f
    var nourishedMultiplier = 1.0f

    @SubscribeEvent
    fun onPlayerTick(event: PlayerTickEvent.Post) {
        if (!HungerbaneConfig.MOD_ENABLED.get()) return

        val player = event.entity ?: return

        val hungerEffect = player.getEffect(MobEffects.HUNGER)
        val nourishedEffect = player.getEffect(ModEffects.NOURISHED)
        if (hungerEffect != null) {
            val level = hungerEffect.amplifier + 1
            hungryMultiplier = HungerbaneConfig.HUNGRY_MULTIPLIER.get().pow(level).toFloat()
        } else {
            hungryMultiplier = 1.0f
        }
        if (nourishedEffect != null) {
            val level = nourishedEffect.amplifier + 1
            nourishedMultiplier = HungerbaneConfig.NOURISHED_MULTIPLIER.get().pow(level).toFloat()
        } else {
            nourishedMultiplier = 1.0f
        }
    }

    @SubscribeEvent
    fun onRenderHealth(event: RenderGuiLayerEvent.Post) {
        if (!HungerbaneConfig.MOD_ENABLED.get()) return
        if (event.name != VanillaGuiLayers.PLAYER_HEALTH) return

        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        if (player.isCreative || player.isSpectator) return

        val stack = player.mainHandItem
        val food = stack.get(DataComponents.FOOD) ?: return

        val healAmount = (food.nutrition() * HungerbaneConfig.HEAL_MULTIPLIER.get().toFloat() * hungryMultiplier * nourishedMultiplier).roundToInt()
        if (healAmount <= 0) return

        val health = player.health.roundToInt()
        val maxHealth = player.maxHealth.roundToInt()
        if (health >= maxHealth) return

        val guiGraphics = event.guiGraphics
        val screenWidth = guiGraphics.guiWidth()
        val screenHeight = guiGraphics.guiHeight()
        val left = screenWidth / 2 - 91
        val top = screenHeight - 39

        val tick = player.tickCount
        val alpha = (sin(tick * 0.2) * 0.3 + 0.5).toFloat()

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha)

        val fullHearts = (health + healAmount) / 2
        val halfHearts = (health + healAmount) % 2

        for (i in 0 until fullHearts) {
            if (i >= 10) break
            val x = left + i * 8
            guiGraphics.blitSprite(FULL_HEART, x, top, 9, 9)
        }
        if (halfHearts == 1 && fullHearts < 10) {
            val x = left + fullHearts * 8
            guiGraphics.blitSprite(HALF_HEART, x, top, 9, 9)
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.disableBlend()
    }
}