package updata.utils;

import cn.nukkit.Server;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.ConfigSection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GitHubUpData extends UpData {

    private static final String GIT_URL = "https://api.github.com/repos/{user}/{project}/releases/latest";

    protected ConfigSection config;

    /**
     * @deprecated
     */
    @Deprecated
    public static GitHubUpData getUpData(PluginBase plugin, String user, String project) {
        ConfigSection section = getConfigSection(user, project);
        if (section == null) {
            return null;
        }
        return new GitHubUpData(plugin, section);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static GitHubUpData getUpData(File file, String user, String project) {
        ConfigSection section = getConfigSection(user, project);
        if (section == null) {
            return null;
        }
        Plugin plugin = Server.getInstance().getPluginManager().loadPlugin(file);
        if (plugin != null) {
            return new GitHubUpData(file, plugin.getDescription().getVersion(), section);
        }
        return null;
    }

    public static GitHubUpData getUpData(PluginBase plugin, File file, String user, String project) {
        ConfigSection section = getConfigSection(user, project);
        if (section == null) {
            return null;
        }
        return new GitHubUpData(plugin, file, section);
    }

    protected GitHubUpData(PluginBase plugin, ConfigSection section) {
        super(plugin);
        this.config = section;
    }

    protected GitHubUpData(PluginBase plugin, File file, ConfigSection section) {
        super(plugin, file);
        this.config = section;
    }

    protected GitHubUpData(File file, String pluginVersion, ConfigSection section) {
        super(file, pluginVersion);
        this.config = section;
    }

    @Override
    public String getUpdateType() {
        return "GitHub";
    }

    protected static String loadJson(String user, String project) {
        return getDataFormUrl(GIT_URL.replace("{user}", user).replace("{project}", project));
    }

    public static ConfigSection getConfigSection(String user, String project) {
        String jsonString = loadJson(user, project);
        if (jsonString == null) {
            return null;
        }
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return new ConfigSection(gson.fromJson(jsonString, (new TypeToken<LinkedHashMap<String, Object>>() {
        }).getType()));
    }

    @Override
    public String getNewVersion() {
        String version = config.getString("tag_name");
        if (version.split("v").length > 1) {
            return version.split("v")[1];
        } else {
            return version;
        }
    }

    @Override
    public String getNewVersionMessage() {
        String strings = config.getString("body");
        String[] ups = strings.trim().split("\\n");
        StringBuilder builder = new StringBuilder("\n");
        for (String s : ups) {
            builder.append(s).append("\n");
        }
        return builder.toString();
    }

    @Override
    protected boolean upData() {
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

}
