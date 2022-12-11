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
import java.util.List;

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

        // List slots that can be filled
        boolean assumeFull = true;
        List<Integer> availableInventorySlots = new java.util.ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.getStack(i);
            if (itemStack.getCount() >= itemStack.getMaxCount()) continue;
            assumeFull = false;
            availableInventorySlots.add(i);
        }
        if (assumeFull) return;

        // List slots that have items
        ArrayList<Integer> populatedPlayerSlots = new java.util.ArrayList<>();
        for (int i = 0; i < player.getInventory().main.size(); i++) {
            ItemStack itemStack = player.getInventory().getStack(i);
            if (itemStack.isEmpty()) continue;
            populatedPlayerSlots.add(i);
        }
        if (populatedPlayerSlots.isEmpty()) return;

        // Move items
        int playerInventoryIndex = 0;
        int inventoryIndex = 0;
        while (playerInventoryIndex < populatedPlayerSlots.size() && inventoryIndex < availableInventorySlots.size()) {
            ItemStack playerItemStack = player.getInventory().getStack(playerInventoryIndex);
            ItemStack inventoryItemStack = inventory.getStack(inventoryIndex);
            if (inventoryItemStack.isEmpty()) {
                inventory.setStack(inventoryIndex, playerItemStack);
                player.getInventory().setStack(playerInventoryIndex, ItemStack.EMPTY);
                playerInventoryIndex++;
                continue;
            }
            if (ItemStack.canCombine(playerItemStack, inventoryItemStack)) {
                int delta = Math.min(playerItemStack.getCount(), inventoryItemStack.getMaxCount() - inventoryItemStack.getCount());
                inventoryItemStack.increment(delta);
                playerItemStack.decrement(delta);
            }
            if (playerItemStack.isEmpty()) {
                playerInventoryIndex++;
            }
            if (inventoryItemStack.getCount() >= inventoryItemStack.getMaxCount()) {
                inventoryIndex++;
            }
        }
        inventory.markDirty();
        player.closeHandledScreen();
    }
}
