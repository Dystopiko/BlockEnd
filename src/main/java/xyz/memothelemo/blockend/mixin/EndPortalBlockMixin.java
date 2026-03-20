package xyz.memothelemo.blockend.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.memothelemo.blockend.BlockEndMod;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {
    @Inject(method = "entityInside", at = @At("HEAD"))
    private void onEntityTouched(
            BlockState blockState,
            Level level,
            BlockPos blockPos,
            Entity entity,
            InsideBlockEffectApplier insideBlockEffectApplier,
            boolean intersects,
            CallbackInfo ci
    ) {
        if (entity instanceof ServerPlayer player) {
            BlockEndMod.sendAlertMessage(player);
            return;
        }

        // Our good friend discovered a fatal bug in this mod where any passengers
        // can go to the End dimension with a horse or any vehicle.
        //
        // Find the associated player and tell them that it is still blocked. >:3
        for (Entity passenger: entity.getPassengers()) {
            if (passenger instanceof ServerPlayer player) {
                BlockEndMod.sendAlertMessage(player);
            }
        }
    }
}
