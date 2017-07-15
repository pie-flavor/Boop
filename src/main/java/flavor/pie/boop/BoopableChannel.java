package flavor.pie.boop;

import com.google.common.collect.Lists;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;

public class BoopableChannel implements MessageChannel {
    private Collection<MessageReceiver> receivers;
    private Config getConfig() {
        return Boop.instance.config;
    }

    public BoopableChannel(Collection<MessageReceiver> receivers) {
        this.receivers = receivers;
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
                if ((getConfig().sound.play || getConfig().title.use) &&
                        ((!p || (s.hasPermission("boop.use." + player.getName()) || !getConfig().restricted.contains(player.getName())))
                                && (textContainsAny(original, getPlayerMatches(player))
                            || textContainsAny(original, applyPrefixes(getGroupNames(player).stream()
                                .filter(t -> !p || (s.hasPermission("boop.use." + t) || !getConfig().restricted.contains(t)))
                                .collect(Collectors.toList())))))) {
                    if (getConfig().sound.play) {
                        player.playSound(getConfig().sound.sound, player.getLocation().getPosition(), 10.0);
                    }
                    if (getConfig().title.use) {
                        player.sendTitle(Title.builder().subtitle(getConfig().title.text).fadeIn(20).fadeOut(20).stay(40).build());
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
        return getConfig().groups.stream().filter(s -> isInGroup(p, s)).collect(Collectors.toList());
    }

    private List<String> getPlayerMatches(Player match) {
        List<String> ret = new LinkedList<>();
        ret.add("@" + match.getName());
        if (getConfig().aliases.containsKey(match.getUniqueId())) {
            for (String s : getConfig().aliases.get(match.getUniqueId())) {
                if (!Sponge.getServer().getPlayer(s).isPresent()) {
                    ret.add(s);
                }
            }
        }
        return ret;
    }

    private List<String> applyPrefixes(List<String> in) {
        return Lists.transform(in, getConfig().prefix::concat);
    }

    private boolean isInGroup(Player p, String group) {
        return p.hasPermission("boop.group." + (group.contains(".") ? group.replace('.', '_') : group));
    }

    @Override
    public Optional<Text> transformMessage(@Nullable Object sender, MessageReceiver recipient, Text original, ChatType type) {
        if (!(recipient instanceof Player)) return Optional.of(original);
        Player p = (Player) recipient;
        List<String> groups = applyPrefixes(getGroupNames(p));
        boolean matchesAny = textContainsAny(original, getPlayerMatches(p)) || textContainsAny(original, groups);
        if (!matchesAny && !getConfig().name.colorAll) return Optional.of(original);
        if (getConfig().name.recolor) {
            for (String match : getPlayerMatches(p)) {
                original = addColor(original, match, getConfig().name.color);
            }
            for (String s : groups) {
                original = addColor(original, s, getConfig().name.color);
            }
        }
        if (getConfig().name.colorAll) {
            for (Player pl : Sponge.getServer().getOnlinePlayers()) {
                for (String pmatch : getPlayerMatches(pl)) {
                    if (!pl.equals(p) && textContains(original, pmatch)) {
                        original = addColor(original, pmatch, getConfig().name.altColor);
                    }
                }
            }
            for (String group : getConfig().groups) {
                String gmatch = getConfig().prefix + group;
                if (!isInGroup(p, group) && textContains(original, gmatch)) {
                    original = addColor(original, gmatch, getConfig().name.altColor);
                }
            }
        }
        if (matchesAny && getConfig().message.recolor) original = original.toBuilder().color(getConfig().message.color).build();
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
            if (plain.endsWith(name)) {
                builder.append(nameText);
            }
            builder.style(text.getStyle()).color(text.getColor()).append(text.getChildren());
            return builder.build();
        }
        return text;
    }
}
