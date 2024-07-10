package dev.alexnijjar.extractinator;

import dev.alexnijjar.extractinator.registry.forge.ModBlockEntities;
import dev.alexnijjar.extractinator.registry.forge.ModBlocks;
import dev.alexnijjar.extractinator.registry.forge.ModItems;
import dev.alexnijjar.extractinator.registry.forge.ModOres;
import dev.alexnijjar.extractinator.registry.forge.ModRecipeSerializers;
import dev.alexnijjar.extractinator.registry.forge.ModRecipeTypes;

public class Extractinator {
    public static final String MOD_ID = "extractinator";

    public static void init() {
        ModBlocks.init();
        ModBlockEntities.init();
        ModItems.init();
        ModRecipeTypes.init();
        ModRecipeSerializers.init();
        ModOres.init();
    }
}