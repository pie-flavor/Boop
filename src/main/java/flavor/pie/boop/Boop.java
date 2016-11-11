package flavor.pie.boop;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.channel.MessageChannel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(id = "boop", name = "Boop", version = "1.0.1", authors = "pie_flavor", description = "Notifies you when you're mentioned in chat.")
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
                    loadDefault();
                }
            }
        }
        try {
            config = loader.load().getValue(Config.type);
        } catch (IOException ex) {
            logger.error("Could not load the config file!");
            try {
                throw ex;
            } finally {
                loadDefault();
            }
        } catch (ObjectMappingException ex) {
            logger.error("Invalid config file!");
            try {
                throw ex;
            } finally {
                loadDefault();
            }
        }
    }
    private void loadDefault() throws IOException, ObjectMappingException {
        try {
            config = HoconConfigurationLoader.builder().setURL(game.getAssetManager().getAsset(this, "default.conf").get().getUrl()).build().load(loader.getDefaultOptions()).getValue(Config.type);
        } catch (IOException | ObjectMappingException ex) {
            logger.error("Could not load the embedded default config! Disabling plugin.");
            game.getEventManager().unregisterPluginListeners(this);
            throw ex;
        }
    }

    @Listener
    public void onChat(MessageChannelEvent.Chat e) {
        MessageChannel channel = e.getChannel().orElseGet(e::getOriginalChannel);
        if (!(channel instanceof BoopableChannel)) {
            e.setChannel(new BoopableChannel(channel.getMembers(), config));
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
