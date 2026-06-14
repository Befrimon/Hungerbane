package com.github.befrimon.hungerbane.events

import com.github.befrimon.hungerbane.Hungerbane
import com.github.befrimon.hungerbane.config.HungerbaneConfig
import com.github.befrimon.hungerbane.events.ClientEvents.hungryMultiplier
import com.github.befrimon.hungerbane.events.ClientEvents.nourishedMultiplier
import com.github.befrimon.hungerbane.game.ModEffects
import net.minecraft.ChatFormatting
import kotlin.math.max
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.Items
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent
import net.neoforged.neoforge.client.gui.VanillaGuiLayers
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
import net.neoforged.neoforge.event.entity.living.MobEffectEvent
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent
import net.neoforged.neoforge.event.tick.PlayerTickEvent

@EventBusSubscriber(modid = Hungerbane.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
object ModEvents {
    @SubscribeEvent
    fun onRenderGui(event: RenderGuiLayerEvent.Pre) {
        if (!HungerbaneConfig.MOD_ENABLED.get()) return
        if (event.name == VanillaGuiLayers.FOOD_LEVEL) {
            event.setCanceled(true)
        }
    }

    @SubscribeEvent
    fun onLivingIncomingDamage(event: LivingIncomingDamageEvent) {
        if (!HungerbaneConfig.MOD_ENABLED.get()) return
        if (event.source.`is`(DamageTypes.STARVE)) {
            event.setCanceled(true)
        }
    }

    @SubscribeEvent
    fun onPlayerTick(event: PlayerTickEvent.Post) {
        if (!HungerbaneConfig.MOD_ENABLED.get()) return
        val player = event.entity
        if (player.foodData.foodLevel != 18 || player.foodData.saturationLevel != 5.0f) {
            player.foodData.foodLevel = 18
            player.foodData.setSaturation(5.0f)
        }
    }

    @SubscribeEvent
    fun onPlayerFinishUsingItem(event: LivingEntityUseItemEvent.Finish) {
        if (!HungerbaneConfig.MOD_ENABLED.get()) return
        val entity = event.entity
        if (entity is ServerPlayer) {
            val stack = event.item
            val food = stack.get(DataComponents.FOOD)

            if (food != null) {
                var healAmount = food.nutrition() * HungerbaneConfig.HEAL_MULTIPLIER.get().toFloat() * hungryMultiplier * nourishedMultiplier
                healAmount = max(0f, healAmount)
                entity.heal(healAmount)
            }
        }
    }

    @SubscribeEvent
    fun onPlayerWakeUp(event: PlayerWakeUpEvent) {
        if (!HungerbaneConfig.MOD_ENABLED.get()) return
        val player = event.entity

        val dayTime = player.level().dayTime % 24000
        val isMorning = dayTime !in 1001..<23000

        if (player is ServerPlayer && isMorning) {
            val healAmount = HungerbaneConfig.WAKE_HEAL_AMOUNT.get().toFloat()
            if (healAmount > 0f && player.health < player.maxHealth) {
                player.heal(healAmount)
            }
        }
    }

    @SubscribeEvent
    fun onMobEffectAdded(event: MobEffectEvent.Added) {
        if (!HungerbaneConfig.MOD_ENABLED.get()) return
        val entity = event.entity
        val instance = event.effectInstance ?: return

        if (instance.`is`(MobEffects.SATURATION)) {
            entity.removeEffect(MobEffects.SATURATION)

            val amplifier = instance.amplifier
            val duration = if (instance.duration <= 20) 600 else instance.duration

            entity.addEffect(MobEffectInstance(ModEffects.NOURISHED, duration, amplifier))
        }
    }

    @SubscribeEvent
    fun onItemTooltip(event: ItemTooltipEvent) {
        if (!HungerbaneConfig.MOD_ENABLED.get()) return
        if (event.itemStack.`is`(Items.SUSPICIOUS_STEW)) {
            val tooltip = event.toolTip

            val iterator = tooltip.iterator()
            while (iterator.hasNext()) {
                val contents = iterator.next().contents
                val saturation = MobEffects.SATURATION.value().descriptionId
                if (contents is TranslatableContents && contents.key == saturation) {
                    iterator.remove()
                    tooltip.add(
                        Component.translatable("effect.hungerbane.nourished")
                            .withStyle(ChatFormatting.BLUE)
                    )
                    break
                }
            }
        }
    }
}