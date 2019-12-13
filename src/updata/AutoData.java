package updata;


import cn.nukkit.plugin.PluginBase;
import updata.utils.UpData;

import java.io.File;

/**
 * @author 若水
 */
public class AutoData extends PluginBase {

    private UpData data;

    private static boolean canLogin = true;
    @Override
    public void onLoad() {
        this.getLogger().info("开始连接 GitHub 库...");
        data = get(this,this.getFile(),"SmallasWater","AutoUpData");
        if(data == null){
            this.getLogger().info("GitHub 连接失败...");
            canLogin = false;
        }else{
            this.getLogger().info("GitHub 连接成功...");
        }

    }

    @Override
    public void onEnable() {

        if(data == null){
            this.getLogger().info("更新检查失败");
        }else{
            if(data.canUpdate()){
                this.getLogger().info("检查到新版本 v"+data.getNewVersion());
                this.getLogger().info("更新介绍 "+data.getNewVersionMessage());
                if(data.toUpData(false)){
                    this.getLogger().info("更新成功 下次启动生效");
                }else{
                    this.getLogger().info("更新失败");
                }
            }else{
                this.getLogger().info("已经是最新版本");
            }
        }

    }
    /**
     * @param plugin 插件
     * @param user GitHub用户名
     * @param project 工程名
     *
     * @deprecated 可能会出现文件重复
     * @return {@link UpData}
     * */
    public static UpData get(PluginBase plugin,String user,String project) {
        if(canLogin){
            return UpData.getUpData(plugin,user,project);
        }
        return null;

    }

    /**
     * @param file 更新插件的路径
     * @param user GitHub用户名
     * @param project 工程名
     *
     * @deprecated 可能会出现插件在 plugins未加载
     * @return {@link UpData}
     * */
    public static UpData get(File file, String user, String project) {
        if(canLogin){
            if(file.isFile()){
                return UpData.getUpData(file,user,project);
            }
        }
        return null;
    }


    /**
     * @param plugin 插件
     * @param file 更新插件的路径
     * @param user GitHub用户名
     * @param project 工程名
     *
     * @return {@link UpData}
     * */
    public static UpData get(PluginBase plugin, File file, String user, String project) {
        if(canLogin){
            if(file.isFile()){
                return UpData.getUpData(plugin,file,user,project);
            }
        }
        return null;
    }


    /**
     * @param plugin 插件
     * @param file 更新插件的路径
     * @param user GitHub用户名
     * @param project 工程名
     *
     * 直接对插件进行更新 如果需要更详细的设置 请用  {@link UpData} 方法
     *
     * @return true 为 更新成功 建议在 onEnable 直接跳出 否则会有报错 false 是更新失败
     * */
    public static boolean defaultUpData(PluginBase plugin, File file, String user, String project){
        UpData data = AutoData.get(plugin,file,user,project);
        if(data != null){
            if(data.canUpdate()){
                plugin.getLogger().info("检测到新版本 v"+data.getNewVersion());
                String message = data.getNewVersionMessage();
                for(String info : message.split("\\n")){
                    plugin.getLogger().info("更新内容: "+info);
                }
                if(!data.toUpData()){
                    plugin.getLogger().info("更新失败");
                }else{
                    return true;
                }
            }
        }else{
            plugin.getLogger().info("更新检查失败");
        }
        return false;

    }
}
