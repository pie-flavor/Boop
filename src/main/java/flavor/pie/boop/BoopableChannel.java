package flavor.pie.boop;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.ChatTypeMessageReceiver;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatType;

import javax.annotation.Nullable;
import java.util.Collection;
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
                if (textContains(original, config.prefix + player.getName()) && config.sound.play) {
                    player.playSound(config.sound.sound, player.getLocation().getPosition(), 10.0);
                }
            }
        }
    }

    private boolean textContains(Text text, String match) {
        return StreamSupport.stream(text.withChildren().spliterator(), false).anyMatch(t -> t.toPlain().contains(match));
    }

    @Override
    public Optional<Text> transformMessage(@Nullable Object sender, MessageReceiver recipient, Text original, ChatType type) {
        if (!(recipient instanceof Player)) return Optional.of(original);
        String match = config.prefix + ((Player) recipient).getName();
        if (!textContains(original, match)) return Optional.of(original);
        if (config.name.recolor) original = addColor(original, match);
        if (config.message.recolor) original = original.toBuilder().color(config.message.color).build();
        return Optional.of(original);
    }

    private Text addColor(Text text, String name) {
        if (!text.getChildren().isEmpty()) {
            text = text.toBuilder().removeAll().append(text.getChildren().stream().map(child -> addColor(child, name)).collect(Collectors.toList())).build();
        }
        String plain = text.toPlainSingle();
        if (plain.contains(name)) {
            Text nameText = Text.of(config.name.color, name);
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
