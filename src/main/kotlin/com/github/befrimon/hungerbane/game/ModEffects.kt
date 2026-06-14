package com.github.befrimon.hungerbane.game

import com.github.befrimon.hungerbane.Hungerbane
import net.minecraft.core.registries.Registries
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

class NourishedEffect(category: MobEffectCategory, color: Int) : MobEffect(category, color) {}

object ModEffects {
    val EFFECTS: DeferredRegister<MobEffect> = DeferredRegister.create(Registries.MOB_EFFECT, Hungerbane.MOD_ID)

    val NOURISHED: DeferredHolder<MobEffect, MobEffect> = EFFECTS.register("nourished") { _ ->
        NourishedEffect(MobEffectCategory.BENEFICIAL, 0xF82421)
    }
}