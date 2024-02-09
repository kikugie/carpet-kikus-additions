package me.kikugie.carpet.mixin;

import carpet.helpers.EntityPlayerActionPack;
import com.llamalad7.mixinextras.sugar.Local;
import me.kikugie.carpet.access.ServerPlayerEntityAccess;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kikugie.carpet.InventoryTransfer.mergeInventories;

@Mixin(targets = "carpet/helpers/EntityPlayerActionPack$ActionType$1")
public class CarpetEntityPlayerActionPackMixin {

    @Inject(method = "execute",
            at = @At(value = "RETURN",
                    target = "Lcarpet/helpers/EntityPlayerActionPack$ActionType$1;execute(Lnet/minecraft/server/network/ServerPlayerEntity;Lcarpet/helpers/EntityPlayerActionPack$Action;)Z",
                    ordinal = 2))
    private void dumpItems(ServerPlayerEntity player, EntityPlayerActionPack.Action action, CallbackInfoReturnable<Boolean> cir, @Local BlockHitResult target) {
        if (target == null) return;
        ServerPlayerEntityAccess playerAccess = (ServerPlayerEntityAccess) player;
        if (!playerAccess.shouldDumpItems()) return;

        BlockEntity targetedBlockEntity = player.world.getBlockEntity(target.getBlockPos());
        if (!(targetedBlockEntity instanceof Inventory inventory) || !((Inventory) targetedBlockEntity).canPlayerUse(player))
            return;
        mergeInventories(player.getInventory(), inventory);
        player.closeHandledScreen();
    }
}
