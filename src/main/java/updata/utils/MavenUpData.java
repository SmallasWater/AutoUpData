package updata.utils;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;

import java.io.File;
import java.util.*;

public class MavenUpData extends UpData {

    public static MavenUpData getUpData(PluginBase plugin, File file, String groupId, String artifactId) {
        MavenUpData data = new MavenUpData(plugin, file, groupId, artifactId);
        if ("0.0.0".equals(data.getNewVersion())) {
            return null;
        }
        return data;
    }

    private String groupId;
    private String artifactId;
    private String mavenUrl;

    private Map<String, Objects> versionMap;

    protected MavenUpData(PluginBase plugin, File file, String groupId, String artifactId) {
        super(plugin, file);
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.mavenUrl = "https://repo.lanink.cn/repository/maven-public/" + groupId.replace(".", "/") + "/" + artifactId + "/";
    }

    @Override
    public String getUpdateType() {
        return "Maven";
    }

    @Override
    public String getNewVersion() {
        //截取标签 <latest>xxx</latest> 中的内容
        String lastVersion = "0.0.0";
        String metadata = getDataFormUrl(mavenUrl + "maven-metadata.xml");
        if (metadata == null) {
            String[] split = metadata.split("<latest>");
            if (split.length > 1) {
                String[] split1 = split[1].split("</latest>");
                lastVersion = split1[0];
            }
        }
        return lastVersion;
    }

    @Override
    public String getNewVersionMessage() {
        return getNewVersion(); //maven仓库中不包含更新内容介绍
    }

    @Override
    protected boolean upData() {
        String newVersion = this.getNewVersion();
        String downloadUrl = mavenUrl + newVersion + "/" + this.artifactId + "-" + newVersion + ".jar";
        try {
            if (file != null) {
                download(downloadUrl, Server.getInstance().getPluginPath(), file.getName());
            } else if (plugin != null) {
                download(downloadUrl, Server.getInstance().getPluginPath(), plugin.getName() + "_v" + plugin.getDescription().getVersion() + ".jar");
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
