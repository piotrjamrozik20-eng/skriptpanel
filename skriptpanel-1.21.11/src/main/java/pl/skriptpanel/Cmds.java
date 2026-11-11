package pl.skriptpanel;

import java.util.List;

/** Wszystkie komendy ze skryptow Skript, pogrupowane w zakladki. */
public final class Cmds {
    private Cmds() {}

    /** cmd bez ukosnika. Jesli konczy sie spacja, komenda wymaga dopisania argumentow (otwiera czat). */
    public record Cmd(String title, String cmd, String desc, boolean op) {
        public boolean needsInput() { return cmd.endsWith(" "); }
    }

    public record Tab(String name, int color, List<Cmd> cmds) {}

    private static Cmd c(String title, String cmd, String desc, boolean op) { return new Cmd(title, cmd, desc, op); }
    private static Cmd p(String title, String cmd, String desc) { return new Cmd(title, cmd, desc, false); }

    public static final List<Tab> TABS = List.of(
        new Tab("Spawn", 0xFF4FC3F7, List.of(
            p("Spawn", "spawn", "Teleport na spawn"),
            c("Ustaw spawn", "setspawn", "Ustawia spawn w miejscu, w ktorym stoisz", true)
        )),
        new Tab("Skrzynki", 0xFFFFB74D, List.of(
            c("Ustaw skrzynke", "setskrzynka", "Patrz na skrzynke z nagrodami i uzyj", true),
            c("Daj klucz", "klucz ", "/klucz <gracz> [ilosc]", true),
            c("Keyall teraz", "keyall give", "Klucz dla kazdego gracza online (1 szt.)", true),
            c("Keyall wiele", "keyall give ", "/keyall give <ilosc>", true),
            c("Keyall auto", "keyall auto ", "/keyall auto <minuty> - klucz co X minut", true),
            c("Keyall wylacz", "keyall off", "Wylacza automatyczny keyall", true),
            c("Keyall status", "keyall status", "Czas do nastepnego keyalla", true)
        )),
        new Tab("Eventy", 0xFFE57373, List.of(
            c("Start: swiateczny", "event start swiateczny", "Event swiateczny", true),
            c("Start: mikolajkowy", "event start mikolajkowy", "Event mikolajkowy", true),
            c("Start: wakacyjny", "event start wakacyjny", "Event wakacyjny", true),
            c("Start: dzien dziecka", "event start dzien dziecka", "Event Dzien Dziecka", true),
            c("Start: halloween", "event start halloween", "Event Halloween", true),
            c("Start: wielkanocny", "event start wielkanocny", "Event wielkanocny", true),
            c("Start: szkolny", "event start szkolny", "Event szkolny (quiz)", true),
            c("Stop eventu", "event stop", "Konczy aktywny event", true),
            c("Status eventu", "event status", "Pokazuje aktywny event", true),
            p("Prezent", "prezent", "Prezent mikolajkowy (raz dziennie)"),
            p("Jajka", "jajka", "Wymiana 10 jajek na diament (Wielkanoc)")
        )),
        new Tab("Przedmioty", 0xFFBA68C8, List.of(
            c("Swiateczny", "eventitem swiateczny", "Laska Cukrowa", true),
            c("Mikolajkowy", "eventitem mikolajkowy", "Czapka Mikolaja", true),
            c("Wakacyjny", "eventitem wakacyjny", "Wakacyjne Klapki", true),
            c("Dzien dziecka", "eventitem dzien dziecka", "Petarda Radosci", true),
            c("Halloween", "eventitem halloween", "Maska Dyni", true),
            c("Wielkanocny", "eventitem wielkanocny", "Jajko Niespodzianka", true),
            c("Szkolny", "eventitem szkolny", "Zeszyt Prymusa", true),
            c("Daj graczowi", "eventitem ", "/eventitem <nazwa> [gracz]", true)
        )),
        new Tab("RTP", 0xFF81C784, List.of(
            p("RTP", "rtp", "Losowy teleport"),
            c("Ustaw przycisk", "setrtp", "Patrz na przycisk i uzyj", true),
            c("Usun przycisk", "delrtp", "Patrz na przycisk i uzyj", true)
        )),
        new Tab("Sklep", 0xFFFFD54F, List.of(
            p("Sklep", "sklep", "Menu sklepu za czas gry"),
            p("Moj czas", "czas", "Ile minut czasu masz"),
            p("Czas gracza", "czas ", "/czas <gracz>"),
            c("Daj czas", "dajczas ", "/dajczas <gracz> <minuty>", true),
            c("Usun czas", "usunczas ", "/usunczas <gracz> <minuty>", true)
        )),
        new Tab("NPC", 0xFF90A4AE, List.of(
            c("Nowy NPC", "npc create ", "/npc create <komenda> <nazwa>", true),
            c("NPC: sklep", "npc create sklep &6&lSKLEP", "Villager otwierajacy /sklep", true),
            c("NPC: rtp", "npc create rtp &a&lRTP", "Villager wykonujacy /rtp", true),
            c("NPC: spawn", "npc create spawn &e&lSPAWN", "Villager wykonujacy /spawn", true),
            c("Usun NPC", "npc remove", "Patrz na NPC", true),
            c("Lista NPC", "npc list", "Wszystkie NPC", true),
            c("Przenies NPC", "npc tp", "Najblizszy NPC do ciebie", true)
        )),
        new Tab("AFK", 0xFF4DB6AC, List.of(
            p("Strefa AFK", "afk", "Teleport do strefy AFK"),
            c("Rog 1", "setafk pos1", "Pierwszy rog strefy", true),
            c("Rog 2", "setafk pos2", "Przeciwlegly rog strefy", true),
            c("Spawn AFK", "setafk spawn", "Miejsce teleportacji do AFK", true)
        ))
    );
}
