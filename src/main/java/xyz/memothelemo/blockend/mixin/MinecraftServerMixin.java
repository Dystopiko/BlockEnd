package xyz.memothelemo.blockend.mixin;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.memothelemo.blockend.BlockEndMod;

@Mixin(DedicatedServer.class)
public class MinecraftServerMixin {
    @Inject(method = "initServer", at = @At(value = "TAIL"))
    public void removeEndDimension(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() == false) return;

        MinecraftServerAccessor server = (MinecraftServerAccessor) this;
        Level endLevel = server.blockEnd$getLevels().remove(Level.END);
        if (endLevel == null) {
            BlockEndMod.LOGGER.warn("Can not block End dimension!");
            return;
        }

        try {
            endLevel.close();
            BlockEndMod.LOGGER.info("Blocked End dimension");
        } catch (Exception e) {
            BlockEndMod.LOGGER.warn("Failed to close End dimension level", e);
        }
    }
}
