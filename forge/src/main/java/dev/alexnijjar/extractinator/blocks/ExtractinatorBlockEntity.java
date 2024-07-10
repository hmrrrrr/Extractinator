package dev.alexnijjar.extractinator.blocks;

import java.util.List;

import javax.annotation.Nullable;

import dev.alexnijjar.extractinator.capabilities.ItemStackHandlerWrapper;
import dev.alexnijjar.extractinator.registry.forge.ModBlockEntities;
import dev.alexnijjar.extractinator.util.ModUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;


public class ExtractinatorBlockEntity extends BlockEntity {
    private ItemStackHandler inputInventory = new ItemStackHandler(1) {
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate){
            if(ModUtils.isValidInput(level, stack)){
                return super.insertItem(slot, stack, simulate);
            }
            return stack;
        }
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    };
    private ItemStackHandler outputInventory = new ItemStackHandler(33) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    };
    private ItemStackHandlerWrapper inventory = new ItemStackHandlerWrapper(inputInventory, outputInventory);

    private final LazyOptional<IItemHandler> inventoryOptional = LazyOptional.of(() -> this.inventory);

    public Level getBlockLevel() {
        return level;
    }

    public ExtractinatorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.EXTRACTINATOR.get(), blockPos, blockState);
    }

    public void addItemToInput(ItemStack stack) {
        if (ModUtils.isValidInput(level, stack)) {

            ItemStack copy = stack.copy();
            copy.setCount(1);
            stack.shrink(1);
            inputInventory.insertItem(0, copy, false);
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryOptional.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction direction) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return this.inventoryOptional.cast();
        }
        return super.getCapability(capability, direction); // See note after snippet
    }

    public void tick() {
        if (!this.level.isClientSide()) {
            if (level.getGameTime() % 8 == 0) {
                extractinate();
            }
        }
    }

    protected void extractinate() {
        dispenseItems();
        placeBlockAbove();
        extractBlockAbove();
        setChanged();
    }

    private ItemStack addItemToOutput(ItemStack stack) {
        for (int i = 0; i < outputInventory.getSlots(); ++i) {
            stack = outputInventory.insertItem(i, stack, false);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    protected void placeBlockAbove() {
        BlockState above = level.getBlockState(this.getBlockPos().above());
        ItemStack input = inputInventory.getStackInSlot(0);
        Block toPlace = Block.byItem(input.getItem());
        if (!(toPlace == Blocks.AIR)) {
            if (above.isAir() || Blocks.WATER.equals(above.getBlock())) {
                level.setBlock(this.getBlockPos().above(), toPlace.defaultBlockState(), Block.UPDATE_NONE);
            } else {
                level.playSound(null, this.getBlockPos(), SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
                List<ItemStack> outputs = ModUtils.extractItem(level, input);
                if (!outputs.isEmpty()) {
                    outputs.forEach(this::addItemToOutput);
                }
            }
            inputInventory.getStackInSlot(0).shrink(1);
        }
    }

    protected void extractBlockAbove() {
        BlockState above = level.getBlockState(this.getBlockPos().above());
        if (above.isAir())
            return;
        ItemStack stack = above.getBlock().asItem().getDefaultInstance();
        if (ModUtils.isValidInput(level, stack)) {
            this.level.destroyBlock(this.getBlockPos().above(), false);
            List<ItemStack> outputs = ModUtils.extractItem(this.level, stack);
            if (!outputs.isEmpty()) {
                outputs.forEach(this::addItemToOutput);
            }
        }
    }

    protected void dispenseItems() {
        for (int i = 0; i < this.outputInventory.getSlots(); i++) {
            ItemStack stack = outputInventory.getStackInSlot(i);
            if (stack.isEmpty())
                continue;
            if (!this.level.getBlockState(getBlockPos().above()).isAir())
                continue;
            BlockPos pos = this.getBlockPos();
            ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5f, pos.getY() + 2.0f, pos.getZ() + 0.5f,
                    stack.copy());
            itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().scale(1.5f));
            stack.setCount(0);
            level.addFreshEntity(itemEntity);
            break;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("Items", this.inventory.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        this.inventory.deserializeNBT(tag.getCompound("Items"));
    }
}