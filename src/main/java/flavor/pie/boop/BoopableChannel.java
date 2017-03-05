package flavor.pie.boop;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.ChatTypeMessageReceiver;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.title.Title;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;

public class BoopableChannel implements MessageChannel {
    private Collection<MessageReceiver> receivers;
    private Config config;
    public BoopableChannel(Collection<MessageReceiver> receivers, Config config) {
        this.receivers = receivers;
        this.config = config;
    }

    @Override
    public Collection<MessageReceiver> getMembers() {
        return receivers;
    }

    @Override
    public void send(@Nullable Object sender, Text original, ChatType type) {
        checkNotNull(original, "original text");
        checkNotNull(type, "type");
        for (MessageReceiver member : this.getMembers()) {
            if (member instanceof ChatTypeMessageReceiver) {
                this.transformMessage(sender, member, original, type).ifPresent(text -> ((ChatTypeMessageReceiver) member).sendMessage(type, text));
            } else {
                this.transformMessage(sender, member, original, type).ifPresent(member::sendMessage);
            }
            if (member instanceof Player) {
                Player player = (Player) member;
                boolean p = sender instanceof Player;
                Player s = p ? (Player) sender : null;
                if ((config.sound.play || config.title.use) &&
                        ((!p || (s.hasPermission("boop.use." + player.getName()) || !config.restricted.contains(player.getName())))
                                && (textContains(original, config.prefix + player.getName())
                            ||  textContainsAny(original, getGroupNames(player).stream()
                                .filter(t -> !p || (s.hasPermission("boop.use"+t) || !config.restricted.contains(t)))
                                .collect(Collectors.toList()))))) {
                    if (config.sound.play) {
                        player.playSound(config.sound.sound, player.getLocation().getPosition(), 10.0);
                    }
                    if (config.title.use) {
                        player.sendTitle(Title.builder().subtitle(config.title.text).fadeIn(20).fadeOut(20).stay(40).build());
                    }
                }
            }
        }
    }

    private boolean textContains(Text text, String match) {
        return StreamSupport.stream(text.withChildren().spliterator(), false).anyMatch(t -> t.toPlain().toLowerCase().contains(match.toLowerCase()));
    }

    private boolean textContainsAny(Text text, Collection<String> matches) {
        return matches.stream().anyMatch(s -> textContains(text, s));
    }

    private List<String> getGroupNames(Player p) {
        return config.groups.stream().filter(s -> isInGroup(p, s)).map(s -> config.prefix+s).collect(Collectors.toList());
    }

    private boolean isInGroup(Player p, String group) {
        return p.hasPermission("boop.group." + (group.contains(".") ? group.replace('.', '_') : group));
    }

    @Override
    public Optional<Text> transformMessage(@Nullable Object sender, MessageReceiver recipient, Text original, ChatType type) {
        if (!(recipient instanceof Player)) return Optional.of(original);
        Player p = (Player) recipient;
        List<String> groups = getGroupNames(p);
        String match = config.prefix + p.getName();
        if (!textContains(original, match) && !config.name.colorAll && !textContainsAny(original, groups)) return Optional.of(original);
        if (config.name.recolor) {
            original = addColor(original, match, config.name.color);
            for (String s: groups) {
                original = addColor(original, s, config.name.color);
            }
        }
        if (config.name.colorAll) {
            for (Player pl : Sponge.getServer().getOnlinePlayers()) {
                String pmatch = config.prefix + p.getName();
                if (!pl.equals(p) && textContains(original, pmatch)) {
                    original = addColor(original, pmatch, config.name.altColor);
                }
            }
            for (String group : config.groups) {
                String gmatch = config.prefix + group;
                if (!isInGroup(p, group) && textContains(original, gmatch)) {
                    original = addColor(original, gmatch, config.name.altColor);
                }
            }
        }
        if (config.message.recolor) original = original.toBuilder().color(config.message.color).build();
        return Optional.of(original);
    }

    private Text addColor(Text text, String name, TextColor color) {
        if (!text.getChildren().isEmpty()) {
            text = text.toBuilder().removeAll().append(text.getChildren().stream().map(child -> addColor(child, name, color)).collect(Collectors.toList())).build();
        }
        String plain = text.toPlainSingle();
        if (plain.toLowerCase().contains(name.toLowerCase())) {
            Text nameText = Text.of(color, name);
            if (plain.equals(name)) return nameText;
            Text.Builder builder = Text.builder();
            String[] sections = plain.split(Pattern.quote(name));
            for (int i = 0; i < sections.length - 1; i++) {
                builder.append(Text.of(sections[i]));
                builder.append(nameText);
            }
            builder.append(Text.of(sections[sections.length - 1]));
            builder.style(text.getStyle()).color(text.getColor()).append(text.getChildren());
            return builder.build();
        }
        return text;
    }
}
