package com.github.befrimon.hungerbane.config

import net.neoforged.neoforge.common.ModConfigSpec

object HungerbaneConfig {
    private val BUILDER = ModConfigSpec.Builder()

    val MOD_ENABLED: ModConfigSpec.BooleanValue = BUILDER
        .comment("Enable Hungerbane mod?")
        .translation("config.hungerbane.mod_enabled")
        .define("mod_enabled", true)

    val HEAL_MULTIPLIER: ModConfigSpec.DoubleValue = BUILDER
        .comment("Healing multiplier from food. 0.5 means food heals 50% of its nutrition value.")
        .translation("config.hungerbane.heal_multiplier")
        .defineInRange("healMultiplier", 0.5, 0.0, 2.0)

    val NOURISHED_MULTIPLIER: ModConfigSpec.DoubleValue = BUILDER
        .comment("Multiplier for healing from food when the Nourished effect is active.",
            "For example, 1.5 means healing is increased by 50% per level of the effect.",
            "Set to 1.0 to disable the bonus healing from Nourished.")
        .translation("config.hungerbane.nourished_multiplier")
        .defineInRange("nourishedMultiplier", 1.5, 1.0, 2.0)

    val HUNGRY_MULTIPLIER: ModConfigSpec.DoubleValue = BUILDER
        .comment("Multiplier for healing from food when the Hunger effect is active.",
            "For example, 0.5 means healing is reduced by 50% per level of the effect.",
            "Set to 1.0 to disable the penalty from Hunger.")
        .translation("config.hungerbane.hungry_multiplier")
        .defineInRange("hungryMultiplier", 0.5, 0.0, 1.0)

    val WAKE_HEAL_AMOUNT: ModConfigSpec.DoubleValue = BUILDER
        .comment("Amount of health restored when waking up from a bed (in HP, where 2 HP = 1 heart).",
            "Set to 0.0 to disable healing from sleep.")
        .translation("config.hungerbane.wake_heal_amount")
        .defineInRange("wakeHealAmount", 4.0, 0.0, 20.0)


    val SPEC: ModConfigSpec = BUILDER.build()

    val ALL_VALUES: List<ConfigField<*>> = listOf(
        ConfigField(MOD_ENABLED),
        ConfigField(HEAL_MULTIPLIER),
        ConfigField(NOURISHED_MULTIPLIER),
        ConfigField(HUNGRY_MULTIPLIER),
        ConfigField(WAKE_HEAL_AMOUNT)
    )
}