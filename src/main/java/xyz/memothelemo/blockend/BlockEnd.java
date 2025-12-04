package xyz.memothelemo.blockend;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mojang.datafixers.util.Unit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
//import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
//import net.kyori.adventure.text.Component;
//import net.kyori.adventure.text.format.NamedTextColor;
//import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class BlockEnd implements ModInitializer {
    /**
     * This static variable determines to whether to
     * block players' access to the End dimension.
     */
    public static final boolean ENABLED = true;
    public static final Logger LOGGER = LoggerFactory.getLogger("BlockEnd");

    // I know using Caffeine is overkill as a way to send players
    // alerts not too much, but it is much easier to implement like this
    // than to implement custom timer to expire (it also saves in the
    // heap memory too).
    private static final Cache<@NotNull String, Unit> ALERTED_PLAYERS = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(30))
            .maximumSize(50)
            .build();

//    private static final Component ALERT_MESSAGE = Component.text()
//            .append(Component.text("[Dystopia] ").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
//            .append(Component.text("End dimension is locked.").color(NamedTextColor.RED))
//            .build();

    public static void sendAlertMessage(ServerPlayer player) {
        // don't alert until their alert timeout expires.
        String uuid = player.getStringUUID();
        if (ALERTED_PLAYERS.getIfPresent(uuid) != null) return;

        ALERTED_PLAYERS.put(uuid, Unit.INSTANCE);
//        MinecraftServerAudiences.of(player.level().getServer())
//                .audience(player)
//                .sendMessage(ALERT_MESSAGE);
    }

	@Override
	public void onInitialize() {
        LOGGER.info("Loaded BlockEnd mod");
        if (ENABLED) LOGGER.info("Blocked access to the End dimension");

        ServerPlayerEvents.LEAVE.register((player) -> {
            ALERTED_PLAYERS.invalidate(player.getStringUUID());
        });

        ServerPlayerEvents.JOIN.register((player) -> {
            // We don't need to close the level because it is assumed to be loaded already.
            ServerLevel theirLevel = player.level();
            if (theirLevel.dimension() == ServerLevel.END && ENABLED) {
                spawnBackToOverworld(player);
                sendAlertMessage(player);
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((old, player, alive) -> {
            // We don't need to close the level because it is assumed to be loaded already.
            ServerLevel theirLevel = player.level();
            if (theirLevel.dimension() == ServerLevel.END && alive && ENABLED) {
                spawnBackToOverworld(player);
                sendAlertMessage(player);
            }
        });
	}

    private static void spawnBackToOverworld(@NotNull ServerPlayer player) {
        TeleportTransition transition = player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);
        player.teleport(transition);
    }
}
