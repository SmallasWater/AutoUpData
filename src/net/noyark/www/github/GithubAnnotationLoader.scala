package net.noyark.www.github


import java.io.File

import cn.nukkit.plugin.PluginBase
import updata.AutoData

/**
  * 注释版实现
  * <code>
  *   { @GithubUpdate(
  *   user="hello",
  *   project="123"
  *  )}
  * public class APluginBase extends PluginBase{
  *
  *   public void onLoad(){
  *     GithubAnnotationLoader.update(this);
  *   }
  * }
  * </code>
  */
object GithubAnnotationLoader {

  def update(plugin: PluginBase): Unit={
    val updateAnnotation = plugin.getClass.getAnnotation(classOf[GithubUpdate])
    val file = plugin.getClass.getDeclaredMethod("getFile")
    file.setAccessible(true)
    val f = file.invoke(plugin)
    AutoData.defaultUpData(plugin,f.asInstanceOf[File],updateAnnotation.user(),updateAnnotation.project())
  }
}
