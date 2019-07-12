package flavor.pie.boop;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.bstats.sponge.MetricsLite2;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.TabCompleteEvent;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.channel.MessageChannel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Plugin(id = "boop", name = "Boop", version = "1.6.2", authors = "pie_flavor",
        description = "Notifies you when you're mentioned in chat.")
public class Boop {
    public static Boop instance;
    @Inject
    Game game;
    @Inject
    Logger logger;
    @Inject @DefaultConfig(sharedRoot = true)
    Path path;
    @Inject @DefaultConfig(sharedRoot = true)
    ConfigurationLoader<CommentedConfigurationNode> loader;
    @Inject
    MetricsLite2 metrics;
    Config config;

    @Listener
    public void preInit(GamePreInitializationEvent e) throws IOException, ObjectMappingException {
        instance = this;
        if (!Files.exists(path)) {
            game.getAssetManager().getAsset(this, "default.conf").get().copyToFile(path);
        }
        config = loader.load().getValue(Config.type);
    }

    @Listener
    public void onChat(MessageChannelEvent.Chat e) {
        MessageChannel channel = e.getChannel().orElseGet(e::getOriginalChannel);
        if (!(channel instanceof BoopableChannel)) {
            for (Class<?> bChannel : config.blacklistedChannels()) {
                if (bChannel.isAssignableFrom(channel.getClass())) {
                    return;
                }
            }
            e.setChannel(new BoopableChannel(channel));
        }
    }

    private final static Pattern SPACE = Pattern.compile(" ");

    @Listener
    public void onTab(TabCompleteEvent.Chat e) {
        String currentWord;
        String[] words = SPACE.split(e.getRawMessage());
        if (words.length == 0) {
            currentWord = e.getRawMessage();
        } else {
            currentWord = words[words.length - 1];
        }
        if (currentWord.startsWith("@")) {
            e.getTabCompletions().addAll(
                    config.groups.stream()
                            .map(s -> "@"+s)
                            .filter(s -> s.toLowerCase().startsWith(currentWord.toLowerCase()))
                            .collect(Collectors.toList()));
            e.getTabCompletions().addAll(
                    game.getServer().getOnlinePlayers().stream()
                            .filter(p -> !p.get(Keys.VANISH).orElse(false))
                            .map(Player::getName)
                            .map(s -> "@"+s)
                            .filter(s -> s.toLowerCase().startsWith(currentWord.toLowerCase()))
                            .collect(Collectors.toList()));
        }
    }

    @Listener
    public void onReload(GameReloadEvent e) throws IOException, ObjectMappingException {
        try {
            config = loader.load().getValue(Config.type);
        } catch (IOException ex) {
            logger.error("Could not reload config!");
            throw ex;
        } catch (ObjectMappingException ex) {
            logger.error("Invalid config!");
            throw ex;
        }
    }
}
