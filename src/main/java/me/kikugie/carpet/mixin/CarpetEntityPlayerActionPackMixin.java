package me.kikugie.carpet.mixin;

import carpet.helpers.EntityPlayerActionPack;
import me.kikugie.carpet.ModSettings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(targets = "carpet/helpers/EntityPlayerActionPack$ActionType$1")
public class CarpetEntityPlayerActionPackMixin {
    private BlockHitResult target;
    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "execute",
            at = @At(value = "INVOKE_ASSIGN",
                    target = "Lcarpet/helpers/EntityPlayerActionPack;getTarget(Lnet/minecraft/server/network/ServerPlayerEntity;)Lnet/minecraft/util/hit/HitResult;"),
            index = 4)
    private HitResult getTarget(HitResult original) {
        target = (BlockHitResult) original;
        return original;
    }

    @Inject(method = "execute",
            at = @At(value = "RETURN",
                    target = "Lcarpet/helpers/EntityPlayerActionPack$ActionType$1;execute(Lnet/minecraft/server/network/ServerPlayerEntity;Lcarpet/helpers/EntityPlayerActionPack$Action;)Z",
                    ordinal = 2))
    private void dumpItems(ServerPlayerEntity player, EntityPlayerActionPack.Action action, CallbackInfoReturnable<Boolean> cir) {
        if (!ModSettings.dumpItemsFlag) return;

        BlockEntity targetedBlockEntity = player.getWorld().getBlockEntity(target.getBlockPos());
        if (!(targetedBlockEntity instanceof Inventory inventory) || !((Inventory) targetedBlockEntity).canPlayerUse(player)) return;
        mergeInventories(player.getInventory(), inventory);
        player.closeHandledScreen();
    }

    private static void mergeInventories(Inventory source, Inventory target) {
        ArrayList<Integer> sourceSlots = getPopulatedSlots(source);
        ArrayList<Integer> targetSlots = getAvailableSlots(target);
        if (sourceSlots.isEmpty() || targetSlots.isEmpty()) return;

        int sourceIndex = 0;
        int targetIndex = 0;

        while (sourceIndex < source.size()) {
            ItemStack sourceItemStack = source.getStack(sourceIndex);
            ItemStack targetItemStack = target.getStack(targetIndex);
            if (targetItemStack.getCount() >= targetItemStack.getMaxCount()) {
                targetIndex++;
                continue;
            }
            if (targetItemStack.isEmpty()) {
                target.setStack(targetIndex, sourceItemStack);
                source.setStack(sourceIndex, ItemStack.EMPTY);
                sourceIndex++;
                continue;
            }
            if (ItemStack.canCombine(sourceItemStack, targetItemStack)) {
                int modifier = Math.min(sourceItemStack.getCount(), targetItemStack.getMaxCount() - targetItemStack.getCount());
                targetItemStack.increment(modifier);
                sourceItemStack.decrement(modifier);
                if (sourceItemStack.isEmpty()) {
                    sourceIndex++;
                    continue;
                }
                continue;
            }

            targetIndex = (targetIndex + 1) % targetSlots.size();
            if (targetIndex == 0) {
                sourceIndex++;
            }

        }
        target.markDirty();
    }

    private static ArrayList<Integer> getAvailableSlots(Inventory inventory) {
        ArrayList<Integer> availableSlots = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.getStack(i);
            if (itemStack.getCount() >= itemStack.getMaxCount()) continue;
            availableSlots.add(i);
        }
        return availableSlots;
    }

    private static ArrayList<Integer> getPopulatedSlots(Inventory inventory) {
        ArrayList<Integer> populatedSlots = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.getStack(i);
            if (itemStack.isEmpty()) continue;
            populatedSlots.add(i);
        }
        return populatedSlots;
    }
}
