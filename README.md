# AutoUpData
根据Github仓库 更新插件
## 开发者须知
1. 首先您拥有GitHub仓库
2. 保证版本格式为 v1.0.0 或 1.0.0
## 如何使用
执行以下代码
```
import updata.AutoData;
/**
 * @parm PluginBase plugin
 * @parm String user GitHub用户名
 * @parm String project 工程名称
 * @return 是否更新完毕 
 */
AutoData.getMain().toUpData(plugin,user,project);
```
