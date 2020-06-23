package giteri
import java.io.File

import giteri.network.networkStuff.NetworkFileLoader
import giteri.run.configurator.Configurator
import giteri.run.jarVersion.JarVersion
import giteri.tool.other.WriteNRead

import scala.util.Random

object Test {
  def main(args: Array[String]): Unit = {

  }

  /** Sérialize, après lecture, les propriétés d'un réseau, issu d'un fichier .txt sous forme
   * list des edges séparé par un espace (tabulation? )
   *
   */
  def serializNetworkTarget(): Unit = {

    val trueReader = new WriteNRead
    val lineReader = new NetworkFileLoader(null, trueReader)

    val reader = trueReader.readAndCreateNetwork("network.txt", lineReader," ","#")
    reader.getNetworkProperties(false, true);
  }
}

