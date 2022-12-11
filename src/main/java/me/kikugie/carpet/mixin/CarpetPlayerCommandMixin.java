package me.kikugie.carpet.mixin;

import carpet.commands.PlayerCommand;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.kikugie.carpet.ModSettings;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

@Mixin(PlayerCommand.class)
public class CarpetPlayerCommandMixin {
    @Inject(method = "stop", at = @At("TAIL"), remap = false)
    private static void disableDumpingItems(CommandContext<ServerCommandSource> context, CallbackInfoReturnable<Integer> cir) {
        ModSettings.dumpItemsFlag = false;
    }

    @Redirect(method = "register", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;then(Lcom/mojang/brigadier/builder/ArgumentBuilder;)Lcom/mojang/brigadier/builder/ArgumentBuilder;", ordinal = 1), remap = false)
    private static ArgumentBuilder insertDumpItemsLiteral(RequiredArgumentBuilder instance, ArgumentBuilder argumentBuilder) {
        return instance.then(argumentBuilder).then(literal("dumpItems").executes(context -> {
            ModSettings.dumpItemsFlag = true;
            return 0;
        }));
    }
}
