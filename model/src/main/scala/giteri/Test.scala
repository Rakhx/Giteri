package giteri

import giteri.network.networkStuff.NetworkFileLoader
import giteri.tool.other.WriteNRead

class Test {
  def main(args: Array[String]): Unit =
  {
    testRandomFn(args(0).toInt,args(1).toInt,args(2).toInt)
  }

  def testRandomFn( one: Int, two: Int, three: Int): Unit = {
    print("One: " + one  + " Two: "+ two + " Three: " + three)
  }

  def compute(one:Int ) : Int = {
    one * 3
  }

  def factorial(n: Int): Int = {
    var f : Int = 1
    for(i <- 1 to 10) {
      f = f * i
    }
    f
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
