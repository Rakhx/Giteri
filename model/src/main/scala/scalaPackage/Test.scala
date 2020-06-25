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

  /** calcul de factoriel
   *
   * @param n
   * @return
   */
  def factorial(n: Int): Int = {
    var f : Int = 1
    for(i <- 1 to n) {
      f = f * i
    }
    f
  }

  /** semi factoriel, produit de min a max de 1 en 1
   *
   * @param max inclu dans les calculs
   * @param min inclu dans les calculs
   * @return
   */
  def semiFact(min:Int, max:Int) : Int = {
    var f : Int = 1
    for(i<- min to max){
      f = f*i
    }

    f
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

  def combSansRepSimplified(k:Int, parmiN:Int) : Int =
  {
    return semiFact(parmiN-k+1,parmiN) / factorial(k);
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
