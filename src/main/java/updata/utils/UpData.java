package updata.utils;

import cn.nukkit.Server;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.ConfigSection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import updata.AutoData;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 自动更新代码
 *
 * @author 若水
 * 此文件可以调用开发者的 GitHub仓库实现插件的自动更新
 */
public class UpData {


    private static final String GIT_URL = "https://api.github.com/repos/{user}/{project}/releases/latest";

    private String pluginVersion;

    private ConfigSection config;

    private PluginBase plugin;

    private static final String PLUGIN_NAME = "AutoUpData";

    private File file;


    private UpData(PluginBase plugin, ConfigSection section) {
        pluginVersion = plugin.getDescription().getVersion();
        this.config = section;
        this.plugin = plugin;
    }

    private UpData(PluginBase plugin, File file, ConfigSection section) {
        pluginVersion = plugin.getDescription().getVersion();
        this.config = section;
        this.plugin = plugin;
        this.file = file;
    }

    private UpData(File file, String pluginVersion, ConfigSection section) {
        this.file = file;
        this.config = section;
        this.pluginVersion = pluginVersion;
    }

    public String getNewVersion() {
        String version = config.getString("tag_name");
        if (version.split("v").length > 1) {
            return version.split("v")[1];
        } else {
            return version;
        }
    }


    private String getPluginVersion() {
        return pluginVersion;
    }

    private static String loadJson(String user, String project) {
        StringBuilder json = new StringBuilder();
        try {
            URL urlObject = new URL(GIT_URL.replace("{user}", user).replace("{project}", project));
            URLConnection uc = urlObject.openConnection();
            uc.setConnectTimeout(30000);
            uc.setReadTimeout(30000);
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            return null;
        }
        return json.toString();
    }

    /**
     * @deprecated
     */
    public static UpData getUpData(PluginBase plugin, String user, String project) {
        ConfigSection section = getConfigSection(user, project);
        if (section == null) {
            return null;
        }
        return new UpData(plugin, section);
    }

    /**
     * @deprecated
     */
    public static UpData getUpData(File file, String user, String project) {
        ConfigSection section = getConfigSection(user, project);
        if (section == null) {
            return null;
        }
        Plugin plugin = Server.getInstance().getPluginManager().loadPlugin(file);
        if (plugin != null) {
            return new UpData(file, plugin.getDescription().getVersion(), section);
        }
        return null;

    }

    public static UpData getUpData(PluginBase plugin, File file, String user, String project) {
        ConfigSection section = getConfigSection(user, project);
        if (section == null) {
            return null;
        }
        return new UpData(plugin, file, section);
    }

    /**
     * 更新插件
     *
     * @param canReload 是否将插件重新加载
     * @return 更新是否成功
     */
    public boolean toUpData(boolean canReload) {
        try {
            if (canUpdate()) {
                if (upData()) {
                    if (!canReload) {
                        return true;
                    }
                    if (plugin != null) {
                        if (file != null) {
                            Server.getInstance().getPluginManager().disablePlugin(plugin);
                            Server.getInstance().getPluginManager().enablePlugin(
                                    Server.getInstance().getPluginManager().loadPlugin(file));
                        } else {
                            Server.getInstance().getPluginManager().disablePlugin(plugin);
                            Server.getInstance().getPluginManager().enablePlugin(
                                    Server.getInstance().getPluginManager().loadPlugin(
                                            Server.getInstance().getPluginPath() + "/" + plugin.getName() + "_v"
                                                    + plugin.getDescription().getVersion() + ".jar"));
                        }

                    } else {
                        if (file != null) {
                            Plugin plugin = Server.getInstance().getPluginManager().loadPlugin(file);
                            if (plugin != null) {
                                Server.getInstance().getPluginManager().disablePlugin(plugin);
                                Thread.sleep(100);
                                Server.getInstance().getPluginManager().enablePlugin(Server.getInstance().getPluginManager().loadPlugin(file));
                            } else {
                                Server.getInstance().getLogger().info("[" + PLUGIN_NAME + "] 失败原因: Plugin 为 null");
                                return false;
                            }
                        } else {
                            Server.getInstance().getLogger().info("[" + PLUGIN_NAME + "] 失败原因: File 为 null");
                            return false;
                        }
                    }

                } else {
                    Server.getInstance().getLogger().info("[" + PLUGIN_NAME + "] 失败原因: 未知异常");
                    return false;
                }
            }
        } catch (Exception e) {
            Server.getInstance().getLogger().info("[" + PLUGIN_NAME + "] 失败原因: 插件异常");
            return false;
        }
        return true;
    }

    /**
     * 更新插件
     *
     * @return 更新是否成功
     */
    public boolean toUpData() {
        return toUpData(true);
    }

    private static ConfigSection getConfigSection(String user, String project) {
        String jsonString = loadJson(user, project);
        if (jsonString == null) {
            return null;
        }
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return new ConfigSection(gson.fromJson(jsonString, (new TypeToken<LinkedHashMap<String, Object>>() {
        }).getType()));
    }

    public boolean canUpdate() {
        return compareVersion(getNewVersion(), pluginVersion) == 1;
    }

    public String getNewVersionMessage() {
        String strings = config.getString("body");
        String[] ups = strings.trim().split("\\n");
        StringBuilder builder = new StringBuilder("\n");
        for (String s : ups) {
            builder.append(s).append("\n");
        }
        return builder.toString();
    }

    private boolean upData() {
        List<Map> strings = config.getMapList("assets");
        Map map = strings.get(0);
        try {
            if (file != null) {
                download((String) map.get("browser_download_url"), Server.getInstance().getPluginPath(), file.getName());
            } else if (plugin != null) {
                download((String) map.get("browser_download_url"), Server.getInstance().getPluginPath(), plugin.getName() + "_v" + plugin.getDescription().getVersion() + ".jar");
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    /**
     * 比较版本
     */
    private int compareVersion(String v1, String v2) {
        if (v1.equalsIgnoreCase(v2)) {
            return 0;
        }
        try {
            LinkedList<String> version1Array = new LinkedList<>(Arrays.asList(v1.split("[._-]")));
            LinkedList<String> version2Array = new LinkedList<>(Arrays.asList(v2.split("[._-]")));

            LinkedList<String> version1Suffix = new LinkedList<>();
            LinkedList<String> version2Suffix = new LinkedList<>();

            int v1index = version1Array.size() - 1;
            while (v1index != 0) {
                try {
                    Long.parseLong(version1Array.getLast());
                }catch (Exception e) {
                    version1Suffix.addLast(version1Array.pollLast());
                }
                v1index--;
            }
            int v2index = version2Array.size() - 1;
            while (v2index != 0) {
                try {
                    Long.parseLong(version2Array.getLast());
                }catch (Exception e) {
                    version2Suffix.addLast(version2Array.pollLast());
                }
                v2index--;
            }

            int index = 0;
            int minLen = Math.min(version1Array.size(), version2Array.size());
            long diff = 0;

            while (index < minLen
                    && (diff = Long.parseLong(version1Array.get(index))
                    - Long.parseLong(version2Array.get(index))) == 0) {
                index++;
            }
            if (diff == 0) {
                if (!version1Suffix.equals(version2Suffix)) {
                    return -1;
                }

                for (int i = index; i < version1Array.size(); i++) {
                    if (Long.parseLong(version1Array.get(i)) > 0) {
                        return 1;
                    }
                }

                for (int i = index; i < version2Array.size(); i++) {
                    if (Long.parseLong(version2Array.get(i)) > 0) {
                        return -1;
                    }
                }
                return 0;
            } else {
                return diff > 0 ? 1 : -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static void download(String urlPath, String targetDirectory, String fileName) throws Exception {
        URL url = new URL(urlPath);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setConnectTimeout(3000);
        // 设置 User-Agent 避免被拦截
        http.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)");
        InputStream inputStream = http.getInputStream();

        byte[] buff = new byte[1024 * 10];
        OutputStream out = new FileOutputStream(new File(targetDirectory, fileName));
        int len;
        while ((len = inputStream.read(buff)) != -1) {
            out.write(buff, 0, len);
            out.flush();
        }
        // 关闭资源
        out.close();
        inputStream.close();
        http.disconnect();
    }


}

