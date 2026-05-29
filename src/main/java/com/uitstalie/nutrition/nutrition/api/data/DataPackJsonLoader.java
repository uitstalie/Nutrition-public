package com.uitstalie.nutrition.nutrition.api.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.uitstalie.nutrition.nutrition.util.log.Log;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;

/**
 * Classpath data-pack JSON 辅助加载器。
 *
 * <p>用于集成服务端等 reload 未触发时的兜底加载。统一处理 resource 缺失、UTF-8 reader
 * 和目录枚举，避免每个 DataListener 都复制一遍样板代码。</p>
 */
public final class DataPackJsonLoader {

    private DataPackJsonLoader() {}

    public static JsonElement loadJson(Class<?> owner, String logTag, String basePath, String fileName) {
        String fullPath = "/" + basePath + "/" + fileName;
        InputStream stream = owner.getResourceAsStream(fullPath);
        if (stream == null) {
            Log.w(logTag, "Resource not found: " + fullPath);
            return null;
        }

        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        } catch (Exception e) {
            Log.e(logTag, "Failed reading " + fullPath + " — " + e.getMessage());
            return null;
        }
    }

    public static List<String> listJsonFiles(String basePath) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(basePath);
        if (url == null) return List.of();

        try {
            if ("file".equals(url.getProtocol())) {
                File dir = new File(url.toURI());
                File[] files = dir.listFiles((ignored, name) -> name.endsWith(".json"));
                if (files == null) return List.of();

                List<String> names = new ArrayList<>();
                for (File file : files) {
                    names.add(file.getName());
                }
                Collections.sort(names);
                return names;
            }

            if ("jar".equals(url.getProtocol())) {
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                List<String> names = new ArrayList<>();
                try (var jar = connection.getJarFile()) {
                    var entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (!entry.isDirectory()
                                && name.startsWith(basePath + "/")
                                && name.endsWith(".json")) {
                            names.add(name.substring(basePath.length() + 1));
                        }
                    }
                }
                Collections.sort(names);
                return names;
            }
        } catch (Exception e) {
            Log.e("DataPackJsonLoader", "Failed listing " + basePath + " — " + e.getMessage());
        }

        return List.of();
    }
}
