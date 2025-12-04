package xyz.memothelemo.blockend.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.memothelemo.blockend.BlockEnd;

@Mixin(EnderDragon.class)
public class EnderDragonMixin {
    @Inject(method = "reallyHurt", at = @At("HEAD"), cancellable = true)
    public void cancelDamage(ServerLevel level, DamageSource damageSource, float f, CallbackInfo ci) {
        // If it's in the end, cancel the damage.
        if (level.dimension() == ServerLevel.END && BlockEnd.ENABLED) {
            // Instantly kill the player then.
            if (damageSource.getEntity() instanceof ServerPlayer player) {
                player.kill(level);
            }
            ci.cancel();
        }
    }

    @Inject(method = "kill", at = @At("HEAD"), cancellable = true)
    public void makeDragonImmortal(ServerLevel level, CallbackInfo ci) {
        // If it's in the end, make the dragon immortal.
        if (level.dimension() == ServerLevel.END && BlockEnd.ENABLED) {
            ci.cancel();
        }
    }
}
