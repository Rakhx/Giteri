package giteri
import giteri.network.networkStuff.NetworkFileLoader
import giteri.tool.other.WriteNRead

object Test {
  def main(args: Array[String]): Unit =
    {
      testRandomFn(args(0).toInt,args(1).toInt,args(2).toInt)
    }

  def testRandomFn( one: Int, two: Int, three: Int): Unit = {
    print("One: " + one  + " Two: "+ two + " Three: " + three)
  }

  def compute(one:Int ) : Int = {
    return one * 3
  }

  /** Renvoi une combinaison sans répétition de k éléments parmis n.
   *
   * @param k
   * @param parmiN
   * @return
   */
  def combinaisonSansRep(k:Int, parmiN:Int) : Int =
  {
    return factorial(parmiN) / (factorial(k)*factorial(parmiN - k))
  }

  // Iterative way to calculate // factorial
  def factorial(n: Int): Int = {
    var f = 1
    for(i <- 1 to n)
    {
      f = f * i;
    }

    return f
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

