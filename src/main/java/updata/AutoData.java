package updata;


import cn.nukkit.plugin.PluginBase;
import updata.utils.GitHubUpData;
import updata.utils.MavenUpData;
import updata.utils.UpData;

import java.io.File;

/**
 * @author 若水
 */
public class AutoData extends PluginBase {

    private static AutoData instance;

    private GitHubUpData githubData;
    private MavenUpData mavenData;

    private static boolean canLinkGitHub = true;
    private static boolean canLinkMaven = true;

    public static AutoData getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;

        this.getLogger().info("开始连接 GitHub 库...");
        githubData = GitHubUpData.getUpData(this, this.getFile(), "SmallasWater", "AutoUpData");
        if (githubData == null) {
            this.getLogger().info("GitHub 连接失败...");
            canLinkGitHub = false;
        } else {
            this.getLogger().info("GitHub 连接成功...");
        }

        mavenData = MavenUpData.getUpData(this, this.getFile(), "com.smallaswater.autoupdata", "AutoUpData");
        if (mavenData == null) {
            this.getLogger().info("Maven 连接失败...");
            canLinkMaven = false;
        } else {
            this.getLogger().info("Maven 连接成功...");
        }
    }

    @Override
    public void onEnable() {
        boolean isSuccess = update(this.githubData);

        if (!isSuccess && canLinkMaven) {
            this.getLogger().info("尝试从 Maven 仓库更新...");
            update(this.mavenData);
        }
    }

    private boolean update(UpData data) {
        if (data == null) {
            this.getLogger().info("更新检查失败");
        } else {
            if (data.canUpdate()) {
                this.getLogger().info("检查到新版本 v" + data.getNewVersion());
                this.getLogger().info("更新介绍 " + data.getNewVersionMessage());
                if (data.toUpData(false)) {
                    this.getLogger().info("更新成功 下次启动生效");
                    return true;
                } else {
                    this.getLogger().info("从 " + data.getUpdateType() + " 更新失败");
                }
            } else {
                this.getLogger().info("已经是最新版本");
                return true;
            }
        }
        return false;
    }

    /**
     * @param plugin  插件
     * @param user    GitHub用户名
     * @param project 工程名
     * @return {@link UpData}
     * @deprecated 可能会出现文件重复
     */
    @Deprecated
    public static UpData get(PluginBase plugin, String user, String project) {
        if (canLinkGitHub) {
            return UpData.getUpData(plugin, user, project);
        }
        return null;

    }

    /**
     * @param file    更新插件的路径
     * @param user    GitHub用户名
     * @param project 工程名
     * @return {@link UpData}
     * @deprecated 可能会出现插件在 plugins未加载
     */
    @Deprecated
    public static UpData get(File file, String user, String project) {
        if (canLinkGitHub) {
            if (file.isFile()) {
                return GitHubUpData.getUpData(file, user, project);
            }
        }
        return null;
    }

    @Deprecated
    public static UpData get(PluginBase plugin, File file, String user, String project) {
        return getByGitHub(plugin, file, user, project);
    }

    /**
     * @param plugin  插件
     * @param file    更新插件的路径
     * @param user    GitHub用户名
     * @param project 工程名
     * @return {@link UpData}
     */
    public static GitHubUpData getByGitHub(PluginBase plugin, File file, String user, String project) {
        if (canLinkGitHub) {
            if (file.isFile()) {
                return GitHubUpData.getUpData(plugin, file, user, project);
            }
        }
        return null;
    }

    public static MavenUpData getByMaven(PluginBase plugin, File file, String groupId, String artifactId, String versionSuffix) {
        if (file.isFile()) {
            return MavenUpData.getUpData(plugin, file, groupId, artifactId, versionSuffix);
        }
        return null;
    }

    @Deprecated
    public static boolean defaultUpData(PluginBase plugin, File file, String user, String project) {
        return defaultUpDataByGitHub(plugin, file, user, project);
    }

    /**
     * @param plugin  插件
     * @param file    更新插件的路径
     * @param user    GitHub用户名
     * @param project 工程名
     *                <p>
     *                直接对插件进行更新 如果需要更详细的设置 请用  {@link UpData} 方法
     * @return true 为 更新成功 建议在 onEnable 直接跳出 否则会有报错 false 是更新失败
     */
    public static boolean defaultUpDataByGitHub(PluginBase plugin, File file, String user, String project) {
        UpData data = AutoData.getByGitHub(plugin, file, user, project);
        return defaultUpData0(plugin, data);
    }

    /**
     * @param plugin  插件
     * @param file    更新插件的路径
     * @param groupId 插件maven groupId
     * @param artifactId 插件maven artifactId
     * @return true 为 更新成功 建议在 onEnable 直接跳出 否则会有报错 false 是更新失败
     */
    public static boolean defaultUpDataByMaven(PluginBase plugin, File file, String groupId, String artifactId, String versionSuffix) {
        UpData data = AutoData.getByMaven(plugin, file, groupId, artifactId, versionSuffix);
        return defaultUpData0(plugin, data);
    }

    private static boolean defaultUpData0(PluginBase plugin, UpData data) {
        if (data != null) {
            if (data.canUpdate()) {
                plugin.getLogger().info("检测到新版本 v" + data.getNewVersion());
                String message = data.getNewVersionMessage();
                for (String info : message.split("\\n")) {
                    plugin.getLogger().info("更新内容: " + info);
                }
                if (!data.toUpData()) {
                    plugin.getLogger().info("更新失败");
                } else {
                    return true;
                }
            } else {
                plugin.getLogger().info("当前为最新版本 v" + plugin.getDescription().getVersion());
            }
        } else {
            plugin.getLogger().info("更新检查失败");
        }
        return false;
    }
}
