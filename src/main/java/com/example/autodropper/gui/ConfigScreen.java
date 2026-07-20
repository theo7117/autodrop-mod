package com.example.autodropper.gui;

import com.example.autodropper.AutoDropperClient;
import com.example.autodropper.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Menu ouvert par la touche "openMenuKey".
 * Permet de choisir la touche d'activation de l'auto-drop et le minuteur (en secondes),
 * puis de sauvegarder ou d'annuler.
 */
public class ConfigScreen extends Screen {

    private final Screen parent;
    private final Config config;

    private int workingKeyCode;
    private double workingInterval;
    private boolean workingEnabled;
    private boolean listeningForKey = false;

    private ButtonWidget keyButton;
    private ButtonWidget enabledButton;
    private TextFieldWidget intervalField;

    public ConfigScreen(Screen parent, Config config) {
        super(Text.translatable("autodropper.gui.title"));
        this.parent = parent;
        this.config = config;
        this.workingKeyCode = config.activationKeyCode;
        this.workingInterval = config.intervalSeconds;
        this.workingEnabled = config.enabled;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        this.keyButton = this.addDrawableChild(ButtonWidget.builder(
                keyButtonText(),
                btn -> {
                    listeningForKey = true;
                    btn.setMessage(Text.translatable("autodropper.gui.press_a_key"));
                }
        ).dimensions(centerX - 100, 50, 200, 20).build());

        this.intervalField = new TextFieldWidget(this.textRenderer, centerX - 100, 90, 200, 20,
                Text.translatable("autodropper.gui.interval"));
        this.intervalField.setMaxLength(10);
        this.intervalField.setTextPredicate(s -> s.isEmpty() || s.matches("[0-9]*[.,]?[0-9]*"));
        this.intervalField.setText(formatNumber(workingInterval));
        this.addDrawableChild(this.intervalField);

        this.enabledButton = this.addDrawableChild(ButtonWidget.builder(
                enabledButtonText(),
                btn -> {
                    workingEnabled = !workingEnabled;
                    btn.setMessage(enabledButtonText());
                }
        ).dimensions(centerX - 100, 120, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("autodropper.gui.save"),
                btn -> save()
        ).dimensions(centerX - 100, 160, 95, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("autodropper.gui.cancel"),
                btn -> close()
        ).dimensions(centerX + 5, 160, 95, 20).build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningForKey) {
            listeningForKey = false;
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                workingKeyCode = keyCode;
            }
            keyButton.setMessage(keyButtonText());
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void save() {
        try {
            double parsed = Double.parseDouble(this.intervalField.getText().replace(',', '.'));
            if (parsed > 0) {
                workingInterval = parsed;
            }
        } catch (NumberFormatException ignored) {
            // On garde la dernière valeur valide connue.
        }

        config.enabled = workingEnabled;
        config.intervalSeconds = Math.max(0.05, workingInterval);
        config.activationKeyCode = workingKeyCode;
        config.save();

        if (workingKeyCode != -1) {
            AutoDropperClient.toggleKey.setBoundKey(InputUtil.Type.KEYSYM.createFromCode(workingKeyCode));
            KeyBinding.updateKeysByCode();
            MinecraftClient.getInstance().options.write();
        }

        close();
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    private Text keyButtonText() {
        Text keyName = workingKeyCode == -1
                ? Text.literal("—")
                : InputUtil.Type.KEYSYM.createFromCode(workingKeyCode).getLocalizedText();
        return Text.translatable("autodropper.gui.activation_key", keyName);
    }

    private Text enabledButtonText() {
        return workingEnabled
                ? Text.translatable("autodropper.gui.enabled_yes")
                : Text.translatable("autodropper.gui.enabled_no");
    }

    private static String formatNumber(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
