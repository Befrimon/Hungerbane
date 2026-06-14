package com.github.befrimon.hungerbane.config

import net.neoforged.neoforge.common.ModConfigSpec

data class ConfigField<T>(val value: ModConfigSpec.ConfigValue<T>) {
    val translationKey: String = value.spec.translationKey.toString()
    val defaultValue: T = value.default
    val getValue: () -> T = { value.get() }
    val setValue: (T) -> Unit = { if (it != null) value.set(it) }
}