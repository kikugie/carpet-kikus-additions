package me.kikugie.carpet.mixin;

import me.kikugie.carpet.access.ServerPlayerEntityAccess;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ServerPlayerEntityAccess {
    private boolean dumpItemsFlag = false;

    @Override
    public boolean getDumpItemsFlag() {
        return dumpItemsFlag;
    }

    @Override
    public void setDumpItemsFlag(boolean dumpItemsFlag) {
        this.dumpItemsFlag = dumpItemsFlag;
    }
}
