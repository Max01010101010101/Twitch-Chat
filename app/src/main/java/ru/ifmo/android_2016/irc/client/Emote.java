package ru.ifmo.android_2016.irc.client;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ifmo.android_2016.irc.api.TwitchApi;

/**
 * Created by ghost on 11/12/2016.
 */

public class Emote implements Comparable<Emote> {
//    private static final String TAG = Emote.class.getSimpleName();

    private static Pattern pattern = Pattern.compile("([\\w\\\\()-]+):(?:\\d+-\\d+)(?:,\\d+-\\d+)*");
    private static Pattern range = Pattern.compile("(\\d+)-(\\d+)");

    private final String emoteUrl;
    private final String emoteId;
    private final int begin;
    private final int end;

    private final int width;
    private final int height;

    private Emote(String emoteUrl,
                  String emoteId,
                  int begin,
                  int end,
                  int width,
                  int height) {
        this.emoteUrl = emoteUrl;
        this.emoteId = emoteId;
        this.begin = begin;
        this.end = end;
        this.width = width;
        this.height = height;
    }

    public static Emote newEmote(String emoteUrl,
                                 String emoteId,
                                 int begin,
                                 int end,
                                 int width,
                                 int height) {
        return new Emote(emoteUrl, emoteId, begin, end, width, height);
    }

    private static Emote getTwitchEmote(String emoteId, int begin, int end) {
        return new Emote(
                TwitchApi.getEmoteUrl(emoteId),
                emoteId,
                begin,
                end,
                25,
                25);
    }

    @Override
    public int compareTo(@NonNull Emote o) {
        return this.begin - o.begin;
    }

    public String getEmoteUrl() {
        return emoteUrl;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "[" + getBegin() + "-" + getEnd() + "]:" + getEmoteUrl();
    }

    @Nullable
    static List<Emote> parse(String emotes) {
        List<Emote> result = new ArrayList<>(4);

        if (emotes != null) {
            try {
                String[] emote = emotes.split("/");

                for (String e : emote) {
                    Matcher matcher = Emote.pattern.matcher(e);
                    if (matcher.matches()) {
                        String[] p = e.split(":");
                        String eId = p[0];
                        for (String range : p[1].split(",")) {
                            Matcher matcher1 = Emote.range.matcher(range);
                            if (matcher1.matches()) {
                                result.add(Emote.getTwitchEmote(
                                        eId,
                                        Integer.parseInt(matcher1.group(1)),
                                        Integer.parseInt(matcher1.group(2)) + 1));
                            }
                        }
                    } else {
                        throw new ParserException("Emote can't be parsed: " + e);
                    }
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException x) {
                throw new ParserException(x);
            }
        }

        if (result.size() > 0) {
            return result;
        } else {
            return null;
        }
    }

    public String getId() {
        return emoteId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}