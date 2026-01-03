package com.dooji.chatemojis.client;

import com.dooji.chatemojis.ChatEmojis;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EmojiAutocomplete {
    private static final String EMOJI_DIR = "assets/chatemojis/textures/emojis/";
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");
    private static final Set<String> EMOJIS = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

    public static void refresh() {
        EMOJIS.clear();
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) {
            return;
        }

        ResourcePackRepository repo = minecraft.getResourcePackRepository();
        if (repo == null) {
            return;
        }

        addFromEntries(repo.getRepositoryEntries());
        IResourcePack serverPack = repo.func_148530_e();
        if (serverPack != null) {
            addFromResourcePack(serverPack);
        }

        ChatEmojis.LOGGER.info("Loaded {} emojis", EMOJIS.size());
    }

    public static List<String> getMatches(String prefix) {
        ArrayList<String> matches = new ArrayList<String>();
        if (prefix == null) {
            return matches;
        }

        if (EMOJIS.isEmpty()) {
            refresh();
        }

        String lower = prefix.toLowerCase(Locale.ENGLISH);
        for (String name : EMOJIS) {
            if (name.startsWith(lower)) {
                matches.add(name);
            }
        }

        return matches;
    }

    private static void addFromEntries(List entries) {
        for (Object entry : entries) {
            if (entry instanceof ResourcePackRepository.Entry) {
                File file = getEntryFile((ResourcePackRepository.Entry) entry);
                scanPackFile(file);
            }
        }
    }

    private static File getEntryFile(ResourcePackRepository.Entry entry) {
        try {
            return ReflectionHelper.getPrivateValue(ResourcePackRepository.Entry.class, entry, "resourcePackFile", "field_110523_b");
        } catch (Exception e) {
            ChatEmojis.LOGGER.warn("Failed to access resource pack entry file", e);
            return null;
        }
    }

    private static void addFromResourcePack(IResourcePack pack) {
        if (pack instanceof AbstractResourcePack) {
            File file = getPackFile((AbstractResourcePack) pack);
            scanPackFile(file);
        }
    }

    private static File getPackFile(AbstractResourcePack pack) {
        try {
            return ReflectionHelper.getPrivateValue(AbstractResourcePack.class, pack, "resourcePackFile", "field_110597_b");
        } catch (Exception e) {
            ChatEmojis.LOGGER.warn("Failed to access resource pack file", e);
            return null;
        }
    }

    private static void scanPackFile(File file) {
        if (file == null) {
            return;
        }

        if (file.isDirectory()) {
            scanDirectory(file);
        } else if (file.isFile()) {
            scanZip(file);
        }
    }

    private static void scanDirectory(File root) {
        File dir = new File(root, EMOJI_DIR);
        if (!dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".png")) {
                String name = file.getName().substring(0, file.getName().length() - 4);
                addName(name);
            }
        }
    }

    private static void scanZip(File zipFile) {
        ZipFile zip = null;
        try {
            zip = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }

                String name = entry.getName();
                if (!name.startsWith(EMOJI_DIR) || !name.endsWith(".png")) {
                    continue;
                }

                String base = name.substring(EMOJI_DIR.length(), name.length() - 4);
                if (base.indexOf('/') != -1) {
                    continue;
                }

                addName(base);
            }
        } catch (IOException e) {
            ChatEmojis.LOGGER.warn("Failed to scan emojis from {}", zipFile, e);
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {
                    ChatEmojis.LOGGER.warn("Failed to close emoji zip {}", zipFile, e);
                }
            }
        }
    }

    private static void addName(String name) {
        if (name == null) {
            return;
        }

        String trimmed = name.toLowerCase(Locale.ENGLISH);
        if (NAME_PATTERN.matcher(trimmed).matches()) {
            EMOJIS.add(trimmed);
        }
    }
}
