package net.nok0.dsrtrs;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

/**
 * The list of mob types that R kills, stored in config/dsrtrs.json.
 * Entries are entity IDs like "minecraft:cow". The special entry "*" means every mob.
 */
public final class MobList {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("dsrtrs.json");

	private static class Data {
		List<String> mobs = new ArrayList<>(List.of(
				"minecraft:player", "minecraft:pillager", "minecraft:illager", "minecraft:pig",
				"minecraft:cow", "minecraft:enderman", "minecraft:zombie", "minecraft:skeleton",
				"minecraft:spider", "minecraft:evoker", "minecraft:iron_golem", "minecraft:creeper"));
	}

	private MobList() {
	}

	private static Data load() {
		if (Files.exists(FILE)) {
			try (Reader reader = Files.newBufferedReader(FILE)) {
				Data data = GSON.fromJson(reader, Data.class);
				if (data != null && data.mobs != null) {
					return data;
				}
			} catch (Exception e) {
				Dsrtrs.LOGGER.error("Could not read {}, using defaults", FILE, e);
			}
		}
		Data defaults = new Data();
		save(defaults);
		return defaults;
	}

	private static void save(Data data) {
		try (Writer writer = Files.newBufferedWriter(FILE)) {
			GSON.toJson(data, writer);
		} catch (IOException e) {
			Dsrtrs.LOGGER.error("Could not write {}", FILE, e);
		}
	}

	public static List<String> get() {
		return List.copyOf(load().mobs);
	}

	public static boolean contains(String id) {
		List<String> mobs = load().mobs;
		return mobs.contains("*") || mobs.contains(id);
	}

	/** @return true if it was added, false if already present */
	public static boolean add(String id) {
		Data data = load();
		if (data.mobs.contains(id)) {
			return false;
		}
		data.mobs.add(id);
		save(data);
		return true;
	}

	/** @return true if it was removed, false if it wasn't in the list */
	public static boolean remove(String id) {
		Data data = load();
		boolean removed = data.mobs.remove(id);
		if (removed) {
			save(data);
		}
		return removed;
	}
}
