package xyz.memothelemo.blockend.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.memothelemo.blockend.BlockEnd;

@Mixin(EndCrystal.class)
public class EndCrystalMixin {
    @Inject(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z"))
    public void killPlayerIfHurt(ServerLevel level, DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        // Only do this in the end dimension!!!
        if (level.dimension() != ServerLevel.END || !BlockEnd.ENABLED) return;

        // Make sure we don't accidentally kill the player if they're in creative or spectator.
        if (damageSource.getEntity() instanceof ServerPlayer player) {
            GameType currentMode = player.gameMode();
            if (currentMode != GameType.CREATIVE && currentMode != GameType.SPECTATOR) {
                player.kill(level);
            }
        }
    }
}
