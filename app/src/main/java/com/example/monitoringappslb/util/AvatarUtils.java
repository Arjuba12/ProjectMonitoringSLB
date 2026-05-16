package com.example.monitoringappslb.util;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

public final class AvatarUtils {
    private static final int[] COLORS = {
            Color.rgb(44, 62, 80),
            Color.rgb(41, 128, 185),
            Color.rgb(22, 160, 133),
            Color.rgb(142, 68, 173),
            Color.rgb(192, 57, 43),
            Color.rgb(211, 84, 0),
            Color.rgb(39, 174, 96),
            Color.rgb(127, 140, 141)
    };

    private AvatarUtils() {}

    public static void applyInitialAvatar(TextView view, String name, String key) {
        if (view == null) return;
        view.setText(buildInitials(name));

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(resolveColor(key != null && !key.trim().isEmpty() ? key : name));
        view.setBackground(drawable);
    }

    public static String buildInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "--";

        String[] words = name.trim().split("\\s+");
        if (words.length == 1) {
            String word = words[0];
            return word.substring(0, Math.min(2, word.length())).toUpperCase();
        }

        return (words[0].substring(0, 1) + words[words.length - 1].substring(0, 1)).toUpperCase();
    }

    private static int resolveColor(String key) {
        if (key == null || key.trim().isEmpty()) return COLORS[0];
        int index = Math.abs(key.trim().toLowerCase().hashCode()) % COLORS.length;
        return COLORS[index];
    }
}
