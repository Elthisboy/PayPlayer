package com.elthisboy.paypleyer.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * GUI para ajustar el monto.
 * El jugador objetivo se selecciona haciendo click derecho sobre él en el mundo.
 * giving=false → cobrar | giving=true → dar
 */
public class AmountScreen extends Screen {

    private final boolean giving;
    private TextFieldWidget amountField;

    public static int currentAmount = 100;
    public static boolean currentGiving = false;

    private static final int PANEL_W = 230;
    private static final int PANEL_H = 148;

    // ── Paleta de colores (modo cobrar: púrpura/índigo | modo dar: esmeralda/cian) ──
    // Fondo del panel
    private static final int COLOR_BG          = 0xF2111120;
    // Borde exterior
    private static final int COLOR_BORDER_DARK  = 0xFF0D0D1A;
    // Borde interior brillante
    private static final int COLOR_BORDER_LIGHT = 0xFF3D3D6B;

    // Header cobrar: degradado índigo oscuro → morado
    private static final int COLOR_HDR_CHARGE_A = 0xFF1A0A3D;
    private static final int COLOR_HDR_CHARGE_B = 0xFF3D1260;
    // Header dar: degradado azul petróleo → verde esmeralda
    private static final int COLOR_HDR_GIVE_A   = 0xFF083028;
    private static final int COLOR_HDR_GIVE_B   = 0xFF0D5040;

    // Acento cobrar (morado brillante) / dar (verde menta)
    private static final int COLOR_ACCENT_CHARGE = 0xFFB06EFF;
    private static final int COLOR_ACCENT_GIVE   = 0xFF40E8A0;

    // Icono / emoji color
    private static final int COLOR_ICON_CHARGE  = 0xFFCC88FF;
    private static final int COLOR_ICON_GIVE    = 0xFF66FFB8;

    // Texto principal
    private static final int COLOR_TEXT         = 0xFFEEEEFF;
    // Texto secundario / hint
    private static final int COLOR_SUB          = 0xFF8888AA;
    // Texto del campo de entrada
    private static final int COLOR_INPUT_BG     = 0xFF1C1C32;
    private static final int COLOR_INPUT_BORDER = 0xFF404070;

    public AmountScreen(boolean giving) {
        super(Text.translatable(giving ? "screen.payplayer.give_money" : "screen.payplayer.charge_money"));
        this.giving = giving;
        currentGiving = giving;
    }

    @Override
    protected void init() {
        int x = (width - PANEL_W) / 2;
        int y = (height - PANEL_H) / 2;

        // Campo de texto para monto
        amountField = new TextFieldWidget(textRenderer,
                x + 12, y + 42, PANEL_W - 24, 18,
                Text.translatable("screen.payplayer.amount"));
        amountField.setMaxLength(9);
        amountField.setText(String.valueOf(currentAmount));
        amountField.setChangedListener(text -> {
            try {
                int val = Integer.parseInt(text);
                if (val > 0) currentAmount = val;
            } catch (NumberFormatException ignored) {}
        });
        addDrawableChild(amountField);

        // Botones de montos rápidos
        int[] presets = {10, 50, 100, 500, 1000};
        int totalW   = PANEL_W - 24;
        int gap      = 3;
        int bw       = (totalW - gap * (presets.length - 1)) / presets.length;
        for (int i = 0; i < presets.length; i++) {
            final int preset = presets[i];
            int bx = x + 12 + i * (bw + gap);
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("$" + preset),
                    btn -> {
                        currentAmount = preset;
                        amountField.setText(String.valueOf(preset));
                    })
                    .dimensions(bx, y + 68, bw, 16)
                    .build());
        }

        // Botón confirmar / cerrar
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("screen.payplayer.close"),
                btn -> close())
                .dimensions(x + 12, y + PANEL_H - 30, PANEL_W - 24, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = (width - PANEL_W) / 2;
        int y = (height - PANEL_H) / 2;

        // ── Fondo difuminado ──────────────────────────────────────────────
        context.fillGradient(0, 0, width, height, 0x70000010, 0x90000020);

        // ── Sombra del panel ──────────────────────────────────────────────
        context.fill(x + 4, y + 4, x + PANEL_W + 4, y + PANEL_H + 4, 0x50000000);

        // ── Panel principal ───────────────────────────────────────────────
        context.fill(x, y, x + PANEL_W, y + PANEL_H, COLOR_BG);

        // Borde oscuro exterior
        context.drawBorder(x - 1, y - 1, PANEL_W + 2, PANEL_H + 2, COLOR_BORDER_DARK);
        // Borde interior iluminado
        context.drawBorder(x, y, PANEL_W, PANEL_H, COLOR_BORDER_LIGHT);

        // ── Header con degradado ──────────────────────────────────────────
        int hdrA = giving ? COLOR_HDR_GIVE_A   : COLOR_HDR_CHARGE_A;
        int hdrB = giving ? COLOR_HDR_GIVE_B   : COLOR_HDR_CHARGE_B;
        context.fillGradient(x, y, x + PANEL_W, y + 24, hdrA, hdrB);

        // Línea separadora del header (acento de color)
        int accent = giving ? COLOR_ACCENT_GIVE : COLOR_ACCENT_CHARGE;
        context.fill(x, y + 23, x + PANEL_W, y + 25, accent);

        // ── Título en header ──────────────────────────────────────────────
        int iconColor = giving ? COLOR_ICON_GIVE : COLOR_ICON_CHARGE;
        context.drawText(textRenderer, this.title, x + 10, y + 8, iconColor, true);

        // ── Etiqueta del campo ────────────────────────────────────────────
        context.drawText(textRenderer,
                Text.translatable("screen.payplayer.amount_label"),
                x + 12, y + 32, COLOR_SUB, false);

        // Fondo del input
        context.fill(x + 11, y + 41, x + PANEL_W - 11, y + 61, COLOR_INPUT_BG);
        context.drawBorder(x + 11, y + 41, PANEL_W - 22, 20, COLOR_INPUT_BORDER);

        // ── Hint ──────────────────────────────────────────────────────────
        context.drawText(textRenderer,
                Text.translatable("screen.payplayer.hint"),
                x + 12, y + 88, COLOR_SUB, false);

        // ── Línea decorativa inferior (antes del botón) ───────────────────
        context.fill(x + 12, y + PANEL_H - 36, x + PANEL_W - 12, y + PANEL_H - 35, COLOR_BORDER_LIGHT);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}
