package mindustry.client.utils;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.UnitType;
import mindustry.ui.Fonts;
import mindustry.world.Block;

public class BlockEmotes {

    private static final Seq<BlockEmote> emotes = new Seq<>();
    public static void initialize() {
        for (Block block : Vars.content.blocks()) {
            emotes.add(new BlockEmote(Fonts.getUnicodeStr(block.name), block.name));
        }
        for (Item item : Vars.content.items()) {
            emotes.add(new BlockEmote(Fonts.getUnicodeStr(item.name), item.name));
        }
        for (Liquid liquid : Vars.content.liquids()) {
            emotes.add(new BlockEmote(Fonts.getUnicodeStr(liquid.name), liquid.name));
        }
        for (UnitType unit : Vars.content.units()) {
            emotes.add(new BlockEmote(Fonts.getUnicodeStr(unit.name), unit.name));
        }
    }

    public static Autocompleteable getCompletion(String input) {
        return bestMatch(input);
    }

    private static BlockEmote bestMatch(String input) {
        return emotes.max(e -> e.matches(input));
    }

    public static Seq<Autocompleteable> closest(String input) {
        return emotes.sort(item -> item.matches(input)).map(item -> item);
    }

    private static class BlockEmote implements Autocompleteable {
        private final String unicode, name;

        public BlockEmote(String unicode, String name) {
            this.unicode = unicode;
            this.name = name;
        }

        @Override
        public float matches(String input) {
            if (!input.contains(":")) return 0f;

            int count = 0;
            for (char c : input.toCharArray()) {
                if (c == ':') {
                    count++;
                }
            }
            if (count % 2 == 0) return 0f;

            Seq<String> items = new Seq<>(input.split(":"));
            if (items.size == 0) return 0f;
            String text = items.peek();
            float dst = BiasedLevenshtein.biasedLevenshtein(text, name);
            dst *= -1;
            dst += name.length();
            dst /= name.length();
            return dst;
        }

        @Override
        public String getCompletion(String input) {
            Seq<String> items = new Seq<>(input.split(":"));
            items.pop();
            String start = items.reduce("", String::concat);
            return start + unicode;
        }

        @Override
        public String getHover(String input) {
            if (!input.contains(":")) return input;
            Seq<String> items = new Seq<>(input.split(":"));
            if (items.size == 0) return input;
            String text = items.peek();
            return input.replace(":" + text, ":" + name + ":");
        }
    }
}
