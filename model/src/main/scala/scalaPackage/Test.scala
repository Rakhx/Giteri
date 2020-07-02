package scalaPackage

import giteri.network.networkStuff.NetworkFileLoader
import giteri.tool.other.WriteNRead

object Test {
  def main(args: Array[String]): Unit =
  {
  }

  def testRandomFn( nbActivator: Int, selector: Double, probas:Array[Double]): Double = {

    var resultat: Double = 1 ;
    print("nbActi " + nbActivator  + " selector: "+ selector + " probas: " )
    for (elem <- probas) {
      println(elem)
      if(resultat > elem)
        resultat = elem
    }


    resultat
  }

  def compute(one:Int ) : Int = {
    one * 3
  }

  def perdu(n:Int, m:Int ):Int = {
    n*m
  }



  /** Sérialize, après lecture, les propriétés d'un réseau, issu d'un fichier .txt sous forme
   * list des edges séparé par un espace (tabulation? )
   *
   */
  def serializNetworkTarget(): Unit = {
    val trueReader = new WriteNRead
    val lineReader = new NetworkFileLoader(null, trueReader)
    val reader = trueReader.readAndCreateNetwork("default.txt", lineReader," ","#")
    reader.getNetworkProperties(false, true);
  }
}
