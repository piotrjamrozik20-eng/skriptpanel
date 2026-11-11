package pl.skriptpanel;

import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Skrypty .sk dolaczone do moda (assets/skriptpanel/skripty/). */
public final class Skripty {
    private Skripty() {}

    public record Info(String file, String title, String desc) {}

    public static final List<Info> LIST = List.of(
        new Info("komendy.sk", "Komendy i spawn", "/spawn, /setspawn, /ranga, ochrona spawna i brak PvP"),
        new Info("klucz.sk", "Klucz", "Wyglad klucza i komenda /klucz"),
        new Info("skrzynki.sk", "Skrzynki", "Skrzynka z nagrodami otwierana kluczem"),
        new Info("keyall.sk", "Keyall", "Klucz dla wszystkich graczy, takze automatycznie"),
        new Info("eventy.sk", "Eventy", "7 eventow, /event, /prezent, /jajka, quiz"),
        new Info("event.sk", "Przedmioty eventowe", "Przedmioty z efektami i /eventitem"),
        new Info("rtp.sk", "RTP", "/rtp i przycisk, blokada w walce"),
        new Info("antylogut.sk", "Antylogut", "30 s walki, smierc po wylogowaniu"),
        new Info("sklep-za-czas.sk", "Sklep za czas", "Sklep, w ktorym placisz czasem gry"),
        new Info("npc.sk", "NPC", "NPC wykonujace komendy po kliknieciu"),
        new Info("afk.sk", "Strefa AFK", "Strefa AFK z nagroda czasu do sklepu")
    );

    private static final Map<String, String> CACHE = new HashMap<>();

    public static String read(String file) {
        String c = CACHE.get(file);
        if (c != null) return c;
        String text;
        try (InputStream in = Skripty.class.getResourceAsStream("/assets/skriptpanel/skripty/" + file)) {
            text = in == null ? "# Brak pliku " + file : new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            text = "# Blad odczytu pliku " + file;
        }
        CACHE.put(file, text);
        return text;
    }

    public static List<String> lines(String text) {
        List<String> out = new ArrayList<>();
        for (String l : text.replace("\r", "").split("\n", -1)) {
            out.add(l.replace("\t", "    "));
        }
        return out;
    }

    public static Path dir() {
        return FabricLoader.getInstance().getGameDir().resolve("skriptpanel-skripty");
    }

    /** Zwraca folder, do ktorego zapisano, albo null przy bledzie. */
    public static Path saveAll() {
        try {
            Path d = dir();
            Files.createDirectories(d);
            for (Info i : LIST) {
                Files.writeString(d.resolve(i.file()), read(i.file()), StandardCharsets.UTF_8);
            }
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    public static Path save(String file) {
        try {
            Path d = dir();
            Files.createDirectories(d);
            Files.writeString(d.resolve(file), read(file), StandardCharsets.UTF_8);
            return d;
        } catch (Exception e) {
            return null;
        }
    }
}
