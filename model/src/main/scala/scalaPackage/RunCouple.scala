package scalaPackage

object RunCouple {
  def main(args : Array[String]): Unit =
  {
    println("jlk")
    //var e : Array[Double] = Array(1.0,23.4)
    //run(4,1045, )
  }

  /** fonction qui prend en param le nombre de CA activé (nbActivator), un nombre entre 0 et 100 qu'il va
   * falloir reprojeter pour en faire une selection dans les espaces des possibilités de selection combinatoire
   *  et un array de double d'une taille maxi dont les (nbActivator) premiere valeurs seront données aux couples
   *  selectionnées
   * @param nbActivator
   * @param selector
   * @param probas
   * @return
   */
  def run(nbCoupleMax:Int, nbActivator: Int, selector: Double, probas:Array[Double]): Double = {
    // selection renvoi le nombre selection possible pour nbActivator element parmi nbCoupleMax éléments max
    var selection : Int = combSansRepSimplified(nbActivator,nbCoupleMax)

    giteri.run.jarVersion.JarVersionCast.gow()


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

  def combSansRepSimplified(k:Int, parmiN:Int) : Int =
  {
    return semiFact(parmiN-k+1,parmiN) / factorial(k);
  }


}