package xyz.memothelemo.blockend;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class BlockEndMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("BlockEnd");

    // I know using Caffeine is overkill as a way to send players alerts not too much,
    // but it is much easier to implement like this than to implement custom timer to
    // expire (it also saves in the heap memory too).
    private static final Cache<@NotNull String, Unit> PLAYERS_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(10))
            .maximumSize(50)
            .build();

    // We can't use `.build()` because calling it will crash the entire server.
    //
    // TODO: Call `.build()` once Adventure's 26.1.0 support has stabilized.
    private static final ComponentBuilder<TextComponent, TextComponent.Builder> ALERT_MESSAGE = Component.text()
        .append(Component.text("[Dystopia] ")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD))
        .append(Component.text("End dimension is locked.")
                .color(NamedTextColor.RED));

    public static void sendAlertMessage(ServerPlayer player) {
        // Don't alert until their alert timeout expires.
        String uuid = player.getStringUUID();
        if (PLAYERS_CACHE.getIfPresent(uuid) != null) return;

        PLAYERS_CACHE.put(uuid, Unit.INSTANCE);
        player.sendMessage(ALERT_MESSAGE);
    }

    @Override
    public void onInitialize() {
        ServerPlayerEvents.LEAVE.register((player) -> {
            PLAYERS_CACHE.invalidate(player.getStringUUID());
        });
    }
}
