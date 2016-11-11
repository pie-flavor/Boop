package flavor.pie.boop;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.text.format.TextColor;

@ConfigSerializable
public class Config {
    public static TypeToken<Config> type = TypeToken.of(Config.class);
    @Setting public String prefix;
    @Setting public Name name;
    @Setting public Sound sound;
    @Setting public Message message;
    @ConfigSerializable
    public static class Name {
        @Setting public boolean recolor;
        @Setting public TextColor color;
    }
    @ConfigSerializable
    public static class Message {
        @Setting public boolean recolor;
        @Setting public TextColor color;
    }
    @ConfigSerializable
    public static class Sound {
        @Setting public boolean play;
        @Setting public SoundType sound;
    }
}
