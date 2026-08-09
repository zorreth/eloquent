package com.zorreth.eloquent.client;

import com.mojang.text2speech.Narrator;

public class VoiceService {
    private static final Narrator rawNarrator = Narrator.getNarrator();

    public static void say(String msg, float volume) {
        rawNarrator.say(msg, true, volume);
    }
}
