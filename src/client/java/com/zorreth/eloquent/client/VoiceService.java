package com.zorreth.eloquent.client;

import com.mojang.text2speech.Narrator;

public class VoiceService {
    private static final Narrator rawNarrator = Narrator.getNarrator();

    public static void say(String msg) {
        rawNarrator.say(msg, true, 1.0f);
    }
}
