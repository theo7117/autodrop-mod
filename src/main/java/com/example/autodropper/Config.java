package com.example.autodropper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration persistante du mod, stockée dans config/autodropper.json.
 */
public class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("autodropper.json");

    /** L'auto-drop est-il actuellement actif ? (basculé par la touche d'activation) */
    public boolean enabled = false;

    /** Intervalle entre deux drops, en secondes. */
    public double intervalSeconds = 60.0;

    /** Si vrai, on drop toute la pile ; sinon un seul objet à la fois. */
    public boolean dropWholeStack = false;

    /** Code de la touche d'activation (GLFW), -1 = non définie. Sert uniquement à la 1ère sauvegarde. */
    public int activationKeyCode = -1;

    public static Config load() {
        if (Files.exists(FILE)) {
            try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
                Config config = GSON.fromJson(reader, Config.class);
                if (config != null) {
                    return config;
                }
            } catch (IOException | RuntimeException e) {
                System.err.println("[AutoDropper] Impossible de lire la configuration, valeurs par défaut utilisées : " + e.getMessage());
            }
        }
        return new Config();
    }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("[AutoDropper] Impossible de sauvegarder la configuration : " + e.getMessage());
        }
    }
}
