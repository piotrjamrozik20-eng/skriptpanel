package pl.skriptpanel;

import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Kolory panelu (RGB bez alfa). Zapisywane w config/skriptpanel.properties. */
public final class Theme {
    private Theme() {}

    public static final int DEF_BASE = 0x101218, DEF_TEXT = 0xE8EAF0;

    public static final int[] GUI_PRESETS = {
        0x101218, 0x0A0A0A, 0x0E1A2B, 0x1A1030, 0x0F1F18, 0x241216, 0x23262D, 0xE8EAF0
    };
    public static final int[] TEXT_PRESETS = {
        0xE8EAF0, 0xFFC857, 0x81C784, 0x4FC3F7, 0xF48FB1, 0xFFB74D, 0xE57373, 0x111111
    };

    public static int base = DEF_BASE;
    public static int text = DEF_TEXT;

    static {
        load();
    }

    public static int argb(int rgb) { return 0xFF000000 | rgb; }

    public static int mix(int a, int b, double t) {
        int r = (int) (((a >> 16) & 0xFF) * (1 - t) + ((b >> 16) & 0xFF) * t);
        int g = (int) (((a >> 8) & 0xFF) * (1 - t) + ((b >> 8) & 0xFF) * t);
        int bl = (int) ((a & 0xFF) * (1 - t) + (b & 0xFF) * t);
        return (r << 16) | (g << 8) | bl;
    }

    private static boolean light(int c) {
        double l = 0.299 * ((c >> 16) & 0xFF) + 0.587 * ((c >> 8) & 0xFF) + 0.114 * (c & 0xFF);
        return l > 140;
    }

    private static int step(double amt) {
        return argb(mix(base, light(base) ? 0x000000 : 0xFFFFFF, amt));
    }

    public static int bg()     { return argb(base); }
    public static int panel()  { return step(0.04); }
    public static int panel2() { return step(0.09); }
    public static int hover()  { return step(0.15); }
    public static int line()   { return step(0.20); }
    public static int text()   { return argb(text); }
    public static int muted()  { return argb(mix(text, base, 0.45)); }
    public static int faint()  { return argb(mix(text, base, 0.62)); }

    public static String hex(int rgb) { return String.format("%06X", rgb & 0xFFFFFF); }

    /** Zwraca null, jesli tekst nie jest poprawnym kolorem hex (6 znakow, opcjonalnie z #). */
    public static Integer parse(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.startsWith("#")) s = s.substring(1);
        if (s.length() != 6) return null;
        try {
            return Integer.parseInt(s, 16);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void reset() {
        base = DEF_BASE;
        text = DEF_TEXT;
        save();
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("skriptpanel.properties");
    }

    public static void load() {
        try {
            Path f = file();
            if (!Files.exists(f)) return;
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(f)) { p.load(in); }
            Integer b = parse(p.getProperty("gui"));
            Integer t = parse(p.getProperty("text"));
            if (b != null) base = b;
            if (t != null) text = t;
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            Properties p = new Properties();
            p.setProperty("gui", hex(base));
            p.setProperty("text", hex(text));
            try (OutputStream out = Files.newOutputStream(file())) { p.store(out, "Skript Panel"); }
        } catch (Exception ignored) {
        }
    }
}
