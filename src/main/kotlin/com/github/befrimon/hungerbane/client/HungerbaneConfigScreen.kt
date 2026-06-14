package com.github.befrimon.hungerbane.client

import com.github.befrimon.hungerbane.Hungerbane
import com.github.befrimon.hungerbane.config.ConfigField
import com.github.befrimon.hungerbane.config.HungerbaneConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ContainerObjectSelectionList
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.neoforged.fml.ModList
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.config.ModConfig
import java.util.Collections

class HungerbaneConfigScreen(private val parent: Screen) : Screen(Component.translatable("config.hungerbane.title")) {
    class Row {
        lateinit var label: StringWidget
        lateinit var value: AbstractWidget
        lateinit var reset: Button
    }

    inner class ConfigList(minecraft: Minecraft, width: Int, height: Int, top: Int, itemHeight: Int) : ContainerObjectSelectionList<CustomEntry>(minecraft, width, height, top, itemHeight) {
        override fun getRowWidth(): Int = panelWidth - panelGapsOut * 2
        override fun getScrollbarPosition(): Int = panelWidth - panelGapsOut / 2

        override fun addEntry(entry: CustomEntry): Int {
            return super.addEntry(entry)
        }

        fun fuckProtection(entry: CustomEntry): Int {
            return addEntry(entry)
        }
    }

    inner class CustomEntry(field: ConfigField<*>) : ContainerObjectSelectionList.Entry<CustomEntry>() {
        var optionRow = Row()

        init {
            optionRow.label = createLabel(field)
            optionRow.value = createEditable(field)
            optionRow.reset = createReset(field, optionRow)
        }

        override fun render(
            guiGraphics: GuiGraphics, index: Int,
            top: Int, left: Int, width: Int, height: Int,
            mouseX: Int, mouseY: Int, isHovered: Boolean, partialTick: Float
        ) {
            optionRow.label.y = top
            optionRow.value.y = top
            optionRow.reset.y = top

            optionRow.label.render(guiGraphics, mouseX, mouseY, partialTick)
            optionRow.value.render(guiGraphics, mouseX, mouseY, partialTick)
            optionRow.reset.render(guiGraphics, mouseX, mouseY, partialTick)
        }

        override fun children(): List<GuiEventListener?> = listOf(optionRow.label, optionRow.value, optionRow.reset)

        override fun narratables(): List<NarratableEntry?> = listOf(object : NarratableEntry {
            override fun narrationPriority(): NarratableEntry.NarrationPriority =
                NarratableEntry.NarrationPriority.HOVERED
            override fun updateNarration(output: NarrationElementOutput) {
                output.add(NarratedElementType.TITLE, Component.literal("Options Rows"))
            }
        })
    }

    private lateinit var fieldList: ConfigList
    private lateinit var fieldEntries: List<CustomEntry>
    private lateinit var resetButton: Button
    private lateinit var doneButton: Button
    private var fields = HungerbaneConfig.ALL_VALUES

    // Work area
    private var panelY: Int = 0
    private var panelWidth: Int = 0
    private var panelHeight: Int = 0
    private var panelGapsIn: Int = 0
    private var panelGapsOut: Int = 0
    private var widgetHeight: Int = 0
    private var contentWidth: Int = 0

    override fun init() {
        super.init()

        /** Init geometry **/
        panelY = 40
        panelWidth = width
        panelHeight = height - 100
        panelGapsIn = 5
        panelGapsOut = 20
        widgetHeight = 20
        contentWidth = panelWidth - panelGapsOut * 2 - panelGapsIn * 2

        /** Create Title **/
        addRenderableWidget(
            StringWidget(0, 0, width, 40, title, font)
        )

        fieldList = ConfigList(minecraft!!, width, panelHeight, panelY, widgetHeight + panelGapsIn)
        fields.forEachIndexed { index, field ->
            fieldList.fuckProtection(CustomEntry(field))
        }
        fieldEntries = fieldList.children()
        addRenderableWidget(fieldList)

        /** Create Bottom Buttons **/
        createBottomButtons()
        addRenderableWidget(resetButton)
        addRenderableWidget(doneButton)

    }

    private fun createLabel(field: ConfigField<*>): StringWidget {
        val labelText = Component.translatable(field.translationKey)
        val labelWidth = contentWidth * 4 / 5
        val labelX = panelGapsOut

        return StringWidget(labelX, 0, labelWidth, widgetHeight, labelText, font)
            .alignLeft()
    }
    private fun createEditable(field: ConfigField<*>): AbstractWidget {
        val widgetWidth = contentWidth * 1 / 5 - 20
        val widgetX = panelGapsOut + contentWidth * 4 / 5 + panelGapsIn

        return when (field.defaultValue) {
            is Boolean -> createBooleanEditable(field, widgetX, widgetWidth)
            is Number -> createNumberEditable(field, widgetX, widgetWidth)
            is String -> createStringEditable(field, widgetX, widgetWidth)
            else -> throw IllegalArgumentException("Unknown field value!")
        }
    }
    private fun createBooleanEditable(field: ConfigField<*>, x: Int, width: Int): Button {
        val currentValue = field.getValue() as? Boolean ?: throw IllegalArgumentException("Field value not Boolean!")
        return Button.Builder(Component.literal(if (currentValue) "true" else "false")) { btn ->
            val newValue = btn.message.string.toString() == "false"
            btn.message = Component.literal(if (newValue) "true" else "false")
        }
            .bounds(x, 0, width, widgetHeight)
            .build()
    }
    private fun createNumberEditable(field: ConfigField<*>, x: Int, width: Int): EditBox {
        val currentValue = field.getValue() as? Number ?: throw IllegalArgumentException("Field value not Number!")
        val box = EditBox(font, x, 0, width, widgetHeight, Component.literal("Number value"))
        box.value = currentValue.toString()
        box.setFilter { text -> text.isEmpty() || text.toDoubleOrNull() != null }
        return box
    }
    private fun createStringEditable(field: ConfigField<*>, x: Int, width: Int): EditBox {
        val currentValue = field.getValue() as? String ?: throw IllegalArgumentException("Field value not String!")
        val box = EditBox(font, x, 0, width, widgetHeight, Component.literal("String value"))
        box.value = currentValue
        return box
    }
    private fun createReset(field: ConfigField<*>, row: Row): Button {
        val widgetWidth = 20
        val widgetX = panelGapsOut + contentWidth - 20 + panelGapsIn * 2

        return Button.Builder(Component.literal("R")) { _ ->
            val defaultValue = field.defaultValue
            when (val entry = row.value) {
                is Button -> entry.message = Component.literal(defaultValue.toString())
                is EditBox -> entry.value = defaultValue.toString()
            }
        }
            .bounds(widgetX, 0, widgetWidth, widgetHeight)
            .build()
    }

    private fun createBottomButtons() {
        val buttonWidth = (panelWidth - panelGapsOut * 2 - panelGapsIn) / 2
        val buttonY = height - 40

        resetButton = Button.Builder(Component.translatable("config.hungerbane.reset")) { _ ->
            // val entries = selectionList.children()
            for (index in fields.indices) {
                val defaultValue = fields[index].defaultValue.toString()
                when (val entry = fieldEntries[index].optionRow.value) {
                    is Button -> entry.message = Component.literal(defaultValue)
                    is EditBox -> entry.value = defaultValue
                }
            }
        }
            .bounds(panelGapsOut, buttonY, buttonWidth, widgetHeight)
            .build()

        doneButton = Button.Builder(Component.translatable("config.hungerbane.done")) { _ ->
            onClose()
        }
            .bounds(panelGapsOut + buttonWidth + panelGapsIn, buttonY, buttonWidth, widgetHeight)
            .build()
    }

    override fun tick() {
        super.tick()

        var enableReset = false
        for (index in fields.indices) {
            val defaultValue = fields[index].defaultValue.toString()
            val savedValue = fields[index].getValue().toString()
            val boxValue = when (val entry = fieldEntries[index].optionRow.value) {
                is Button -> entry.message.string.toString()
                is EditBox -> entry.value.toString()
                else -> ""
            }

            val isChanged = savedValue != boxValue
            val isDefault = defaultValue != boxValue
            if (isDefault) enableReset = true
            fieldEntries[index].optionRow.reset.active = isDefault


            if (isChanged) {
                @Suppress("UNCHECKED_CAST")
                when (fields[index].defaultValue) {
                    is Boolean -> (fields[index].setValue as (Boolean) -> Unit)(boxValue == "true")
                    is Double -> (fields[index].setValue as (Double) -> Unit)(boxValue.toDouble())
                    is String -> (fields[index].setValue as (String) -> Unit)(boxValue)
                }
                HungerbaneConfig.SPEC.save()
            }
        }
        resetButton.active = enableReset
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun onClose() {
        minecraft?.setScreen(parent)
    }
}