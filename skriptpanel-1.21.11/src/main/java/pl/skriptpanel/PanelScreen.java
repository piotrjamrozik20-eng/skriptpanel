package pl.skriptpanel;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PanelScreen extends Screen {
    private static final int SIDE = 118, HEAD = 36, FOOT = 20, ROW = 38, LINE_H = 10;
    private static final int GOLD = 0xFFFFC857, SCRIPT_COLOR = 0xFF9CCC65;
    private static final int SW = 22, SWGAP = 28;

    private static int lastTab = 0;

    private final int SCRIPT_TAB = Cmds.TABS.size();

    private int px, py, pw, ph;
    private int tab = lastTab;
    private boolean settings = false;
    private double scroll;
    private EditBox search, guiHex, textHex;
    private final List<Cmds.Cmd> visible = new ArrayList<>();
    private final List<Integer> visibleColor = new ArrayList<>();
    private Skripty.Info viewing;
    private List<String> viewLines = new ArrayList<>();
    private String toast = "";
    private long toastUntil;

    public PanelScreen() {
        super(Component.literal("Skript Panel"));
    }

    @Override
    protected void init() {
        pw = Math.min(width - 24, 580);
        ph = Math.min(height - 24, 330);
        px = (width - pw) / 2;
        py = (height - ph) / 2;

        search = new EditBox(font, px + pw - 168, py + 9, 158, 18, Component.literal("Szukaj"));
        search.setHint(Component.literal("Szukaj komendy..."));
        search.setResponder(s -> {
            scroll = 0;
            if (!s.trim().isEmpty()) settings = false;
            refresh();
            updateBoxes();
        });
        addRenderableWidget(search);

        int lx = listX(), ly = listY();
        guiHex = hexBox(lx + 12, ly + 36, Theme.base, true);
        textHex = hexBox(lx + 12, ly + 98, Theme.text, false);

        refresh();
        updateBoxes();
    }

    private EditBox hexBox(int x, int y, int rgb, boolean gui) {
        EditBox b = new EditBox(font, x, y, 64, 16, Component.literal("hex"));
        b.setMaxLength(7);
        b.setValue(Theme.hex(rgb));
        b.setResponder(s -> {
            Integer v = Theme.parse(s);
            if (v == null) return;
            if (gui) Theme.base = v; else Theme.text = v;
            Theme.save();
        });
        addRenderableWidget(b);
        return b;
    }

    private void updateBoxes() {
        guiHex.visible = settings;
        textHex.visible = settings;
    }

    private void syncHex() {
        guiHex.setValue(Theme.hex(Theme.base));
        textHex.setValue(Theme.hex(Theme.text));
    }

    private boolean searchEmpty() {
        return search == null || search.getValue().trim().isEmpty();
    }

    private boolean inScripts() {
        return tab == SCRIPT_TAB && !settings && searchEmpty();
    }

    private void refresh() {
        visible.clear();
        visibleColor.clear();
        String q = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            if (tab >= Cmds.TABS.size()) return;
            Cmds.Tab t = Cmds.TABS.get(tab);
            for (Cmds.Cmd c : t.cmds()) { visible.add(c); visibleColor.add(t.color()); }
        } else {
            for (Cmds.Tab t : Cmds.TABS) {
                for (Cmds.Cmd c : t.cmds()) {
                    String hay = (c.title() + " " + c.cmd() + " " + c.desc() + " " + t.name()).toLowerCase(Locale.ROOT);
                    if (hay.contains(q)) { visible.add(c); visibleColor.add(t.color()); }
                }
            }
        }
    }

    private int tabCount() { return Cmds.TABS.size() + 1; }
    private String tabName(int i) { return i == SCRIPT_TAB ? "Skrypty" : Cmds.TABS.get(i).name(); }
    private int tabColor(int i) { return i == SCRIPT_TAB ? SCRIPT_COLOR : Cmds.TABS.get(i).color(); }
    private int tabItems(int i) { return i == SCRIPT_TAB ? Skripty.LIST.size() : Cmds.TABS.get(i).cmds().size(); }

    private int listX() { return px + SIDE + 10; }
    private int listY() { return py + HEAD + 4; }
    private int listW() { return pw - SIDE - 20; }
    private int listH() { return ph - HEAD - FOOT - 8; }
    private int tabH() { return Math.min(26, (ph - HEAD - FOOT - 8) / tabCount()); }

    private double maxScroll() {
        if (settings) return 0;
        if (inScripts()) {
            if (viewing != null) return Math.max(0, viewLines.size() * LINE_H + 8 - (listH() - 24));
            return Math.max(0, (Skripty.LIST.size() + 1) * ROW - listH());
        }
        return Math.max(0, visible.size() * ROW - listH());
    }

    private int btnX() { return px + pw - 168 - 66; }

    private boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void outline(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    private void text(GuiGraphics g, String s, int x, int y, int color, boolean shadow) {
        g.drawString(font, s, x, y, color, shadow);
    }

    private void button(GuiGraphics g, String label, int x, int y, int w, int mx, int my) {
        boolean hov = in(mx, my, x, y, w, 18);
        g.fill(x, y, x + w, y + 18, hov ? Theme.hover() : Theme.panel2());
        outline(g, x, y, w, 18, Theme.line());
        text(g, label, x + (w - font.width(label)) / 2, y + 5, Theme.text(), false);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float delta) {
        // celowo puste - tlo rysujemy sami w render()
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        int TEXT = Theme.text(), MUTED = Theme.muted(), FAINT = Theme.faint();

        g.fill(0, 0, width, height, 0xB0000000);
        g.fill(px - 1, py - 1, px + pw + 1, py + ph + 1, Theme.line());
        g.fill(px, py, px + pw, py + ph, Theme.bg());

        g.fill(px, py, px + pw, py + HEAD, Theme.panel());
        g.fill(px, py + HEAD - 1, px + pw, py + HEAD, Theme.line());
        g.fill(px + 10, py + 10, px + 14, py + 26, GOLD);
        text(g, "SKRIPT PANEL", px + 20, py + 11, TEXT, true);
        text(g, "serwer Aternos", px + 20, py + 22, MUTED, false);

        boolean bh = in(mx, my, btnX(), py + 9, 60, 18);
        g.fill(btnX(), py + 9, btnX() + 60, py + 27, settings ? Theme.panel2() : (bh ? Theme.hover() : Theme.panel2()));
        outline(g, btnX(), py + 9, 60, 18, settings ? GOLD : Theme.line());
        String bl = "Kolory";
        text(g, bl, btnX() + (60 - font.width(bl)) / 2, py + 14, settings ? GOLD : TEXT, false);

        g.fill(px, py + HEAD, px + SIDE, py + ph - FOOT, Theme.panel());
        g.fill(px + SIDE - 1, py + HEAD, px + SIDE, py + ph - FOOT, Theme.line());
        boolean searching = !searchEmpty();
        int th = tabH();
        for (int i = 0; i < tabCount(); i++) {
            int ty = py + HEAD + 4 + i * th;
            boolean sel = i == tab && !searching && !settings;
            boolean hov = mx >= px && mx < px + SIDE - 1 && my >= ty && my < ty + th;
            int col = tabColor(i);
            if (sel) g.fill(px + 4, ty, px + SIDE - 5, ty + th - 2, Theme.panel2());
            else if (hov) g.fill(px + 4, ty, px + SIDE - 5, ty + th - 2, Theme.hover());
            g.fill(px + 8, ty + 4, px + 11, ty + th - 6, sel ? col : (col & 0x00FFFFFF) | 0x66000000);
            text(g, tabName(i), px + 17, ty + (th - 10) / 2, sel ? TEXT : MUTED, false);
            String n = String.valueOf(tabItems(i));
            text(g, n, px + SIDE - 10 - font.width(n), ty + (th - 10) / 2, FAINT, false);
        }

        if (settings) drawSettings(g, mx, my);
        else if (inScripts()) drawScripts(g, mx, my);
        else drawList(g, mx, my);

        g.fill(px, py + ph - FOOT, px + pw, py + ph, Theme.panel());
        g.fill(px, py + ph - FOOT, px + pw, py + ph - FOOT + 1, Theme.line());
        boolean showToast = System.currentTimeMillis() < toastUntil;
        String foot = showToast ? toast
                : settings ? "Kolory zapisuja sie automatycznie. Wpisz wlasny kod HEX lub wybierz z palety."
                : inScripts() ? "LPM: podglad   PPM: kopiuj skrypt   Pliki wgraj do plugins/Skript/scripts/"
                : "LPM: uruchom / wpisz   PPM: kopiuj   Shift: nie zamykaj   K / ESC: zamknij";
        text(g, foot, px + 10, py + ph - FOOT + 6, showToast ? 0xFF81C784 : MUTED, false);

        super.render(g, mx, my, delta);
    }

    private void drawList(GuiGraphics g, int mx, int my) {
        int TEXT = Theme.text(), MUTED = Theme.muted(), FAINT = Theme.faint();
        int lx = listX(), ly = listY(), lw = listW(), lh = listH();
        scroll = Math.max(0, Math.min(scroll, maxScroll()));
        g.enableScissor(lx, ly, lx + lw, ly + lh);
        if (visible.isEmpty()) {
            text(g, "Brak wynikow.", lx + 6, ly + 8, MUTED, false);
        }
        for (int i = 0; i < visible.size(); i++) {
            int ry = ly + i * ROW - (int) scroll;
            if (ry + ROW < ly || ry > ly + lh) continue;
            Cmds.Cmd c = visible.get(i);
            int color = visibleColor.get(i);
            boolean hov = mx >= lx && mx < lx + lw && my >= Math.max(ry, ly) && my < Math.min(ry + ROW - 3, ly + lh);
            g.fill(lx, ry, lx + lw, ry + ROW - 3, hov ? Theme.hover() : Theme.panel2());
            g.fill(lx, ry, lx + 3, ry + ROW - 3, color);
            text(g, c.title(), lx + 11, ry + 6, TEXT, true);
            text(g, c.desc(), lx + 11, ry + 20, MUTED, false);

            String cmdText = "/" + c.cmd().trim() + (c.needsInput() ? " ..." : "");
            int cw = font.width(cmdText);
            int cx = lx + lw - cw - 12 - (c.op() ? 26 : 0);
            text(g, cmdText, cx, ry + 6, hov ? GOLD : FAINT, false);
            if (c.op()) {
                int bx = lx + lw - 30;
                g.fill(bx, ry + 5, bx + 22, ry + 16, 0x55FFC857);
                text(g, "OP", bx + 6, ry + 7, GOLD, false);
            }
            if (hov) {
                String hint = c.needsInput() ? "kliknij: wpisz w czacie" : "kliknij: uruchom";
                text(g, hint, lx + lw - font.width(hint) - 12, ry + 20, FAINT, false);
            }
        }
        g.disableScissor();
        scrollbar(g, lx + lw + 3, ly, lh, visible.size() * ROW);
    }

    private void scrollbar(GuiGraphics g, int x, int y, int h, int contentH) {
        if (maxScroll() <= 0) return;
        int barH = Math.max(20, (int) (h * (h / (double) contentH)));
        int barY = y + (int) ((h - barH) * (scroll / maxScroll()));
        g.fill(x, y, x + 2, y + h, Theme.panel());
        g.fill(x, barY, x + 2, barY + barH, Theme.faint());
    }

    // ----------------------------------------------------------------- SKRYPTY

    private int codeColor(String line) {
        String t = line.stripLeading();
        if (t.startsWith("#")) return 0xFF6A9955;
        if (!line.isEmpty() && !Character.isWhitespace(line.charAt(0))) return GOLD;
        return Theme.text();
    }

    private void drawScripts(GuiGraphics g, int mx, int my) {
        int lx = listX(), ly = listY(), lw = listW(), lh = listH();
        if (viewing != null) {
            drawViewer(g, mx, my);
            return;
        }
        int TEXT = Theme.text(), MUTED = Theme.muted(), FAINT = Theme.faint();
        scroll = Math.max(0, Math.min(scroll, maxScroll()));
        g.enableScissor(lx, ly, lx + lw, ly + lh);
        int rows = Skripty.LIST.size() + 1;
        for (int i = 0; i < rows; i++) {
            int ry = ly + i * ROW - (int) scroll;
            if (ry + ROW < ly || ry > ly + lh) continue;
            boolean hov = mx >= lx && mx < lx + lw && my >= Math.max(ry, ly) && my < Math.min(ry + ROW - 3, ly + lh);
            g.fill(lx, ry, lx + lw, ry + ROW - 3, hov ? Theme.hover() : Theme.panel2());
            if (i == 0) {
                g.fill(lx, ry, lx + 3, ry + ROW - 3, GOLD);
                text(g, "Zapisz wszystkie skrypty", lx + 11, ry + 6, TEXT, true);
                text(g, "Zapisuje pliki .sk do folderu skriptpanel-skripty w folderze gry", lx + 11, ry + 20, MUTED, false);
                String r = "ZAPISZ";
                text(g, r, lx + lw - font.width(r) - 12, ry + 6, hov ? GOLD : FAINT, false);
            } else {
                Skripty.Info s = Skripty.LIST.get(i - 1);
                g.fill(lx, ry, lx + 3, ry + ROW - 3, SCRIPT_COLOR);
                text(g, s.title(), lx + 11, ry + 6, TEXT, true);
                text(g, s.desc(), lx + 11, ry + 20, MUTED, false);
                String f = s.file();
                text(g, f, lx + lw - font.width(f) - 12, ry + 6, hov ? GOLD : FAINT, false);
                if (hov) {
                    String hint = "LPM: podglad  PPM: kopiuj";
                    text(g, hint, lx + lw - font.width(hint) - 12, ry + 20, FAINT, false);
                }
            }
        }
        g.disableScissor();
        scrollbar(g, lx + lw + 3, ly, lh, rows * ROW);
    }

    private void drawViewer(GuiGraphics g, int mx, int my) {
        int lx = listX(), ly = listY(), lw = listW(), lh = listH();
        button(g, "< Wstecz", lx, ly, 70, mx, my);
        button(g, "Kopiuj calosc", lx + 76, ly, 120, mx, my);
        button(g, "Zapisz plik", lx + 202, ly, 120, mx, my);
        String f = viewing.file();
        text(g, f, lx + lw - font.width(f) - 2, ly + 5, Theme.muted(), false);

        int cy = ly + 24, ch = lh - 24;
        g.fill(lx, cy, lx + lw, cy + ch, Theme.panel2());
        scroll = Math.max(0, Math.min(scroll, maxScroll()));
        g.enableScissor(lx, cy, lx + lw, cy + ch);
        for (int i = 0; i < viewLines.size(); i++) {
            int y = cy + 4 + i * LINE_H - (int) scroll;
            if (y + LINE_H < cy || y > cy + ch) continue;
            String num = String.valueOf(i + 1);
            text(g, num, lx + 24 - font.width(num), y, Theme.faint(), false);
            text(g, viewLines.get(i), lx + 30, y, codeColor(viewLines.get(i)), false);
        }
        g.disableScissor();
        scrollbar(g, lx + lw + 3, cy, ch, viewLines.size() * LINE_H + 8);
    }

    private void openScript(Skripty.Info s) {
        viewing = s;
        viewLines = Skripty.lines(Skripty.read(s.file()));
        scroll = 0;
    }

    private void flashSaved(Path dir, String what) {
        if (dir == null) flash("Nie udalo sie zapisac plikow");
        else flash("Zapisano " + what + " w: " + dir.getFileName() + " (folder gry)");
    }

    // ------------------------------------------------------------------ KOLORY

    private void drawSwatches(GuiGraphics g, int[] presets, int current, int x, int y, int mx, int my) {
        for (int i = 0; i < presets.length; i++) {
            int sx = x + i * SWGAP;
            boolean hov = mx >= sx && mx < sx + SW && my >= y && my < y + SW;
            g.fill(sx, y, sx + SW, y + SW, Theme.argb(presets[i]));
            boolean sel = presets[i] == current;
            outline(g, sx - 1, y - 1, SW + 2, SW + 2, sel ? Theme.text() : (hov ? Theme.muted() : Theme.line()));
        }
    }

    private void drawSettings(GuiGraphics g, int mx, int my) {
        int lx = listX(), ly = listY();
        text(g, "Kolor GUI", lx, ly, Theme.text(), true);
        drawSwatches(g, Theme.GUI_PRESETS, Theme.base, lx, ly + 12, mx, my);
        text(g, "#", lx + 4, ly + 40, Theme.muted(), false);

        text(g, "Kolor napisow", lx, ly + 62, Theme.text(), true);
        drawSwatches(g, Theme.TEXT_PRESETS, Theme.text, lx, ly + 74, mx, my);
        text(g, "#", lx + 4, ly + 102, Theme.muted(), false);

        button(g, "Przywroc domyslne", lx, ly + 124, 130, mx, my);

        int pvx = lx + 150, pvy = ly + 124;
        g.fill(pvx, pvy, pvx + 150, pvy + 18, Theme.panel2());
        g.fill(pvx, pvy, pvx + 3, pvy + 18, GOLD);
        text(g, "Podglad napisu", pvx + 10, pvy + 5, Theme.text(), true);
    }

    private int swatchAt(double mx, double my, int x, int y, int count) {
        if (my < y || my >= y + SW) return -1;
        for (int i = 0; i < count; i++) {
            int sx = x + i * SWGAP;
            if (mx >= sx && mx < sx + SW) return i;
        }
        return -1;
    }

    // ------------------------------------------------------------------- INPUT

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;

        double mx = event.x(), my = event.y();
        int button = event.button();

        if (button == 0 && in(mx, my, btnX(), py + 9, 60, 18)) {
            settings = !settings;
            if (settings) search.setValue("");
            scroll = 0;
            refresh();
            updateBoxes();
            return true;
        }

        int th = tabH();
        if (mx >= px && mx < px + SIDE - 1) {
            for (int i = 0; i < tabCount(); i++) {
                int ty = py + HEAD + 4 + i * th;
                if (my >= ty && my < ty + th) {
                    tab = i;
                    lastTab = i;
                    scroll = 0;
                    settings = false;
                    viewing = null;
                    search.setValue("");
                    refresh();
                    updateBoxes();
                    return true;
                }
            }
        }

        int lx = listX(), ly = listY(), lw = listW(), lh = listH();

        if (settings) {
            if (button != 0) return false;
            int a = swatchAt(mx, my, lx, ly + 12, Theme.GUI_PRESETS.length);
            if (a >= 0) { Theme.base = Theme.GUI_PRESETS[a]; Theme.save(); syncHex(); return true; }
            int b = swatchAt(mx, my, lx, ly + 74, Theme.TEXT_PRESETS.length);
            if (b >= 0) { Theme.text = Theme.TEXT_PRESETS[b]; Theme.save(); syncHex(); return true; }
            if (in(mx, my, lx, ly + 124, 130, 18)) {
                Theme.reset();
                syncHex();
                flash("Przywrocono domyslne kolory");
                return true;
            }
            return false;
        }

        if (inScripts()) {
            if (viewing != null) {
                if (button != 0) return false;
                if (in(mx, my, lx, ly, 70, 18)) { viewing = null; scroll = 0; return true; }
                if (in(mx, my, lx + 76, ly, 120, 18)) {
                    minecraft.keyboardHandler.setClipboard(Skripty.read(viewing.file()));
                    flash("Skopiowano caly skrypt: " + viewing.file());
                    return true;
                }
                if (in(mx, my, lx + 202, ly, 120, 18)) {
                    flashSaved(Skripty.save(viewing.file()), viewing.file());
                    return true;
                }
                return false;
            }
            if (in(mx, my, lx, ly, lw, lh)) {
                int idx = (int) ((my - ly + scroll) / ROW);
                if (idx == 0 && button == 0) {
                    flashSaved(Skripty.saveAll(), "wszystkie skrypty");
                    return true;
                }
                if (idx >= 1 && idx <= Skripty.LIST.size()) {
                    Skripty.Info s = Skripty.LIST.get(idx - 1);
                    if (button == 1) {
                        minecraft.keyboardHandler.setClipboard(Skripty.read(s.file()));
                        flash("Skopiowano skrypt: " + s.file());
                    } else if (button == 0) {
                        openScript(s);
                    }
                    return true;
                }
            }
            return false;
        }

        if (mx >= lx && mx < lx + lw && my >= ly && my < ly + lh) {
            int idx = (int) ((my - ly + scroll) / ROW);
            if (idx >= 0 && idx < visible.size()) {
                Cmds.Cmd c = visible.get(idx);
                if (button == 1) {
                    minecraft.keyboardHandler.setClipboard("/" + c.cmd().trim());
                    flash("Skopiowano: /" + c.cmd().trim());
                } else if (button == 0) {
                    run(c, event.hasShiftDown());
                }
                return true;
            }
        }
        return false;
    }

    private void run(Cmds.Cmd c, boolean keepOpen) {
        if (minecraft == null || minecraft.player == null) return;
        if (c.needsInput()) {
            minecraft.setScreen(new ChatScreen("/" + c.cmd(), false));
            return;
        }
        minecraft.player.connection.sendCommand(c.cmd());
        flash("Wyslano: /" + c.cmd());
        if (!keepOpen) onClose();
    }

    private void flash(String s) {
        toast = s;
        toastUntil = System.currentTimeMillis() + 2500;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        if (settings) return true;
        scroll -= vertical * (inScripts() && viewing != null ? 30 : 24);
        scroll = Math.max(0, Math.min(scroll, maxScroll()));
        return true;
    }

    private boolean typing() {
        return search.isFocused() || (settings && (guiHex.isFocused() || textHex.isFocused()));
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (typing()) {
            if (event.key() == InputConstants.KEY_ESCAPE) {
                search.setFocused(false);
                guiHex.setFocused(false);
                textHex.setFocused(false);
                return true;
            }
            return super.keyPressed(event);
        }
        if (SkriptPanelClient.openKey.matches(event)) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
