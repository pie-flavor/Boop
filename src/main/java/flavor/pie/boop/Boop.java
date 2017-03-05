package flavor.pie.boop;

import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.config.DefaultConfig;
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
import java.util.stream.Collectors;

@Plugin(id = "boop", name = "Boop", version = "1.4.1", authors = "pie_flavor", description = "Notifies you when you're mentioned in chat.")
public class Boop {
    @Inject
    Game game;
    @Inject
    Logger logger;
    @Inject @DefaultConfig(sharedRoot = true)
    Path path;
    @Inject @DefaultConfig(sharedRoot = true)
    ConfigurationLoader<CommentedConfigurationNode> loader;
    Config config;
    @Listener
    public void preInit(GamePreInitializationEvent e) throws IOException, ObjectMappingException {
        Asset conf = game.getAssetManager().getAsset(this, "default.conf").get();
        if (!Files.exists(path)) {
            try {
                conf.copyToFile(path);
            } catch (IOException ex) {
                logger.error("Could not copy the config file!");
                try {
                    throw ex;
                } finally {
                    mapDefault();
                }
            }
        }
        ConfigurationNode root;
        try {
            root = loader.load();
        } catch (IOException ex) {
            logger.error("Could not load the config file!");
            try {
                throw ex;
            } finally {
                mapDefault();
            }
        }
        if (root.getNode("version").getInt() < 4) {
            try {
                root.mergeValuesFrom(loadDefault());
                root.getNode("version").setValue(4);
            } catch (IOException ex) {
                logger.error("Could not update config!");
                try {
                    throw ex;
                } finally {
                    mapDefault();
                }
            }
            try {
                loader.save(root);
            } catch (IOException ex) {
                logger.error("Could not save config!");
                try {
                    throw ex;
                } finally {
                    try {
                        config = root.getValue(Config.type);
                    } catch (ObjectMappingException ex2) {
                        logger.error("Invalid config file!");
                        try {
                            throw ex;
                        } finally {
                            mapDefault();
                        }
                    }
                }
            }
        }
        try {
            config = root.getValue(Config.type);
        } catch (ObjectMappingException ex) {
            logger.error("Invalid config file!");
            try {
                throw ex;
            } finally {
                mapDefault();
            }
        }
    }
    private void mapDefault() throws IOException, ObjectMappingException {
        try {
            config = loadDefault().getValue(Config.type);
        } catch (IOException | ObjectMappingException ex) {
            logger.error("Could not load the embedded default config! Disabling plugin.");
            game.getEventManager().unregisterPluginListeners(this);
            throw ex;
        }
    }
    private ConfigurationNode loadDefault() throws IOException {
        return HoconConfigurationLoader.builder().setURL(game.getAssetManager().getAsset(this, "default.conf").get().getUrl()).build().load(loader.getDefaultOptions());
    }

    @Listener
    public void onChat(MessageChannelEvent.Chat e) {
        MessageChannel channel = e.getChannel().orElseGet(e::getOriginalChannel);
        if (!(channel instanceof BoopableChannel)) {
            e.setChannel(new BoopableChannel(channel.getMembers(), config));
        }
    }

    @Listener
    public void onTab(TabCompleteEvent.Chat e) {
        String currentWord;
        if (!e.getRawMessage().contains(" ")) {
            currentWord = e.getRawMessage();
        } else {
            String[] words = e.getRawMessage().split(" ");
            currentWord = words[words.length - 1];
        }
        if (currentWord.startsWith("@")) {
            e.getTabCompletions().addAll(config.groups.stream().map(s -> "@"+s).filter(s -> s.toLowerCase().startsWith(currentWord.toLowerCase())).collect(Collectors.toList()));
            e.getTabCompletions().addAll(game.getServer().getOnlinePlayers().stream().map(Player::getName).map(s -> "@"+s).filter(s -> s.toLowerCase().startsWith(currentWord.toLowerCase())).collect(Collectors.toList()));
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
