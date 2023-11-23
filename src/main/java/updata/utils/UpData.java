package updata.utils;

import cn.nukkit.Server;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import updata.AutoData;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * 自动更新代码
 *
 * @author 若水
 * 此文件可以调用开发者的 GitHub仓库实现插件的自动更新
 */
public abstract class UpData {

    protected String pluginVersion;

    protected PluginBase plugin;

    protected static final String PLUGIN_NAME = "AutoUpData";

    protected File file;

    protected UpData(PluginBase plugin) {
        pluginVersion = plugin.getDescription().getVersion();
        this.plugin = plugin;
    }

    protected UpData(PluginBase plugin, File file) {
        pluginVersion = plugin.getDescription().getVersion();
        this.plugin = plugin;
        this.file = file;
    }

    protected UpData(File file, String pluginVersion) {
        this.file = file;
        this.pluginVersion = pluginVersion;
    }

    public abstract String getUpdateType();

    protected String getPluginVersion() {
        return pluginVersion;
    }

    /**
     * @deprecated 被 GitHubUpData#getConfigSection(String, String) 方法代替
     */
    @Deprecated
    public static UpData getUpData(PluginBase plugin, String user, String project) {
        return GitHubUpData.getUpData(plugin, user, project);
    }

    /**
     * @deprecated 被 GitHubUpData#getUpData(File, String, String) 方法代替
     */
    @Deprecated
    public static UpData getUpData(File file, String user, String project) {
        return GitHubUpData.getUpData(file, user, project);
    }

    /**
     * @deprecated 被 GitHubUpData#getUpData(PluginBase, File, String, String) 方法代替
     */
    @Deprecated
    public static UpData getUpData(PluginBase plugin, File file, String user, String project) {
        return GitHubUpData.getUpData(plugin, file, user, project);
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

    public boolean canUpdate() {
        return compareVersion(getNewVersion(), pluginVersion) == 1;
    }

    /**
     * @return 获取新版版本号
     */
    public abstract String getNewVersion();

    /**
     * @return 获取新版更新信息
     */
    public abstract String getNewVersionMessage();

    /**
     * 更新插件
     *
     * @return 更新是否成功
     */
    protected abstract boolean upData();

    /**
     * 比较版本
     */
    private int compareVersion(String newVersion, String nowVersion) {
        if (newVersion.equalsIgnoreCase(nowVersion)) {
            return 0;
        }
        try {
            LinkedList<String> version1Array = new LinkedList<>(Arrays.asList(newVersion.split("[._-]")));
            LinkedList<String> version2Array = new LinkedList<>(Arrays.asList(nowVersion.split("[._-]")));

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
            AutoData.getInstance().getLogger().error("[" + PLUGIN_NAME + "] 版本比较异常", e);
        }
        return -1;
    }

    protected static void download(String urlPath, String targetDirectory, String fileName) throws Exception {
        URL url = new URL(urlPath);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setConnectTimeout(3000);
        // 设置 User-Agent 避免被拦截
        http.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)");
        InputStream inputStream = http.getInputStream();

        byte[] buff = new byte[1024 * 10];
        OutputStream out = Files.newOutputStream(new File(targetDirectory, fileName).toPath());
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

    protected static String getDataFormUrl(String url) {
        StringBuilder data = new StringBuilder();
        try {
            URL urlObject = new URL(url);
            URLConnection uc = urlObject.openConnection();
            uc.setConnectTimeout(30000);
            uc.setReadTimeout(30000);
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                data.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            return null;
        }
        return data.toString();
    }

}

