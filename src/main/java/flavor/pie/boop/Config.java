package flavor.pie.boop;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@ConfigSerializable
public class Config {
    public final static TypeToken<Config> type = TypeToken.of(Config.class);
    @Setting public String prefix;
    @Setting public Name name = new Name();
    @Setting public Sound sound = new Sound();
    @Setting public Message message = new Message();
    @Setting public List<String> groups = ImmutableList.of();
    @Setting public List<String> restricted = ImmutableList.of();
    @Setting public Title title = new Title();
    @Setting public Map<UUID, List<String>> aliases = ImmutableMap.of();
    @ConfigSerializable
    public static class Name {
        @Setting public boolean recolor = true;
        @Setting public TextColor color = TextColors.YELLOW;
        @Setting("color-all") public boolean colorAll = true;
        @Setting("alt-color") public TextColor altColor = TextColors.GRAY;
    }
    @ConfigSerializable
    public static class Message {
        @Setting public boolean recolor = false;
        @Setting public TextColor color = TextColors.GREEN;
    }
    @ConfigSerializable
    public static class Sound {
        @Setting public boolean play = true;
        @Setting public SoundType sound = SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP;
    }
    @ConfigSerializable
    public static class Title {
        @Setting public boolean use = true;
        @Setting public Text text = Text.EMPTY;
    }
}
