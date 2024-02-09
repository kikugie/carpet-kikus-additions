package me.kikugie.carpet.mixin;

import me.kikugie.carpet.access.ServerPlayerEntityAccess;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ServerPlayerEntityAccess {
    @Unique
    private boolean dumpItemsFlag = false;


    @Override
    public boolean shouldDumpItems() {
        return dumpItemsFlag;
    }

    @Override
    public void setItemDump(boolean value) {
        dumpItemsFlag = value;
    }
}
