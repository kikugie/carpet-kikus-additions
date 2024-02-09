package me.kikugie.carpet.mixin;

import carpet.commands.PlayerCommand;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.kikugie.carpet.access.ServerPlayerEntityAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

@Mixin(PlayerCommand.class)
public abstract class CarpetPlayerCommandMixin {
    @Inject(method = "stop", at = @At("RETURN"), remap = false)
    private static void disableDumpingItems(CommandContext<ServerCommandSource> context, CallbackInfoReturnable<Integer> cir) {
        var player = (ServerPlayerEntityAccess) getPlayer(context);
        player.setItemDump(false);
    }

    @SuppressWarnings("unchecked")
    @ModifyExpressionValue(method = "register", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;then(Lcom/mojang/brigadier/builder/ArgumentBuilder;)Lcom/mojang/brigadier/builder/ArgumentBuilder;", ordinal = 1), remap = false)
    private static ArgumentBuilder<ServerCommandSource, ?> insertDumpItemsLiteral(ArgumentBuilder<ServerCommandSource, ?> original) {
        return original.then(((LiteralArgumentBuilder<ServerCommandSource>) (Object) literal("dumpItems"))
                .executes(context -> {
                    var player = getPlayer(context);
                    ((ServerPlayerEntityAccess) player).setItemDump(true);

                    context.getSource().sendFeedback(Text.of("Enabled item dump for " + player.getDisplayName().getString()), false);
                    return 1;
                }));

    }

    @Unique
    private static ServerPlayerEntity getPlayer(CommandContext<ServerCommandSource> context) {
        String playerName = StringArgumentType.getString(context, "player");
        MinecraftServer server = context.getSource().getServer();
        return server.getPlayerManager().getPlayer(playerName);
    }
}
