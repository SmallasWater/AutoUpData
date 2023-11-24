package updata.utils;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import updata.AutoData;

import java.io.File;
import java.util.*;

public class MavenUpData extends UpData {

    public static MavenUpData getUpData(PluginBase plugin, File file, String groupId, String artifactId) {
        return getUpData(plugin, file, groupId, artifactId, null);
    }

    public static MavenUpData getUpData(PluginBase plugin, File file, String groupId, String artifactId, String versionSuffix) {
        MavenUpData data = new MavenUpData(plugin, file, groupId, artifactId, versionSuffix);
        if ("0.0.0".equals(data.getNewVersion())) {
            return null;
        }
        return data;
    }

    private final String groupId;
    private final String artifactId;
    private final String versionSuffix;
    private final String mavenUrl;

    private String lastVersion = "0.0.0";;
    private final ArrayList<String> versions = new ArrayList<>();

    protected MavenUpData(PluginBase plugin, File file, String groupId, String artifactId, String versionSuffix) {
        super(plugin, file);
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.versionSuffix = versionSuffix;

        this.mavenUrl = "https://repo.lanink.cn/repository/maven-public/" + groupId.replace(".", "/") + "/" + artifactId + "/";

        String metadata = getDataFormUrl(mavenUrl + "maven-metadata.xml");

        if (metadata != null) {
            //截取标签 <latest>xxx</latest> 中的内容
            String[] split = metadata.split("<latest>");
            if (split.length > 1) {
                String[] split1 = split[1].split("</latest>");
                lastVersion = split1[0];
            }

            //截取标签 <version>xxx</version> 中的内容
            split = metadata.split("</version>");
            for (String s : split) {
                String[] split1 = s.split("<version>");
                if (split1.length > 1) {
                    versions.add(split1[1]);
                }
            }

            //指定版本后缀时，重新检查符合要求的最新版本
            if (versionSuffix != null && !lastVersion.contains(versionSuffix)) {
                lastVersion = "0.0.0";
                for (String version : versions) {
                    if (version.contains(versionSuffix) && compareVersion(version, lastVersion) > 0) {
                        lastVersion = version;
                        break;
                    }
                }
            }
        }

        AutoData.getInstance().getLogger().info("test");
    }

    @Override
    public String getUpdateType() {
        return "Maven";
    }

    @Override
    public String getNewVersion() {
        return this.lastVersion;
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
