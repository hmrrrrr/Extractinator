package dev.alexnijjar.extractinator.compat.rei;

import dev.alexnijjar.extractinator.registry.forge.ModBlocks;
import dev.alexnijjar.extractinator.registry.forge.ModItems;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.List;

public class ExtractinatorCategory implements DisplayCategory<ExtractinatorDisplay> {
    @Override
    public CategoryIdentifier<? extends ExtractinatorDisplay> getCategoryIdentifier() {
        return ExtractinatorReiPlugin.CATEGORY;
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent(ModBlocks.EXTRACTINATOR.get().getDescriptionId());
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(ModItems.EXTRACTINATOR.get());
    }

    @Override
    public int getDisplayWidth(ExtractinatorDisplay display) {
        return 144 + 10;
    }

    @Override
    public int getDisplayHeight() {
        return 144 + 15;
    }

    @Override
    public List<Widget> setupDisplay(ExtractinatorDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createSlot(new Point(bounds.getMinX() + 69, bounds.getMinY() + 5)).entries(display.getInputEntries().get(0)).markInput());

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 7; j++) {
                int index = 8 * j + i;
                if (display.getOutputEntries().size() > index) {
                    widgets.add(Widgets.createSlot(new Point(bounds.getMinX() + 5 + (i * 18 + 1), bounds.getMinY() + (j * 18 + 26))).entries(display.getOutputEntries().get(index)).markOutput());
                } else {
                    widgets.add(Widgets.createSlot(new Point(bounds.getMinX() + 5 + (i * 18 + 1), bounds.getMinY() + (j * 18 + 26))));
                }
            }
        }
        return widgets;
    }
}
