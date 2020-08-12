package com.knoban.atlas.data.local;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * The class provides json parsing functionality to and from a .json file. This is a utility class.
 *
 * @author Alden Bansemer (kNoAPP)
 */
public class JsonDataStore {

    private JavaPlugin plugin;

    /**
     * Creates a JsonDataStore instance using an instance of the main class.
     * @param plugin The instance of the plugin creating this data store.
     */
    public JsonDataStore(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Parses Json from a passed file into a passed class. Each private data member of the passed class must be able to
     * be parsed. Primitives can naturally be parsed. Most Java Library classes are also able to be parsed.
     * Each custom class must have a public constructor that takes no arguments.
     * In addition, the class must include a public getter for each property.
     *
     * Additionally, generic data members can be parsed, but whole generic classes cannot be parsed.
     * @param file The file to parse from
     * @param clazz The class to parse to
     * @param <T> The class
     * @return An instance of the passed class parsed from the .json file. May return null if the json
     * file is missing, unreadable, or empty.
     */
    @Nullable
    public <T> T getJson(@NotNull File file, @NotNull Class<T> clazz) {
        String cached = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            for(String line; (line = br.readLine()) != null;)
                sb.append(line);
            br.close();
            cached = sb.toString();
        } catch(IOException e) {
            plugin.getServer().getConsoleSender().sendMessage("§e[" + plugin.getName() +
                    "] Failed to read file (" + file.getName() + "): " + e.getMessage());
        }

        return gson.fromJson(cached, clazz);
    }

    /**
     * Parses a passed Java Object into a .json file.  Each private data member of the passed class must be able to
     * be parsed. Primitives can naturally be parsed. Most Java Library classes are also able to be parsed.
     * Each custom class must have a public constructor that takes no arguments.
     * In addition, the class must include a public getter for each property.
     *
     * Additionally, generic data members can be parsed, but whole generic classes cannot be parsed.
     * @param file The file to save to (this file will be overwritten)
     * @param obj The Object to parse and save
     */
    public void saveJson(@NotNull File file, @Nullable Object obj) {
        String cached = gson.toJson(obj);
        try {
            org.apache.commons.io.FileUtils.writeStringToFile(file, cached, StandardCharsets.UTF_8);
        } catch(IOException e) {
            plugin.getLogger().warning("Failed to write to file (" + file.getName() + "): " + e.getMessage());
        }
    }
}
