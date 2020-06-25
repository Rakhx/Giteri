package scalaPackage
import scala.collection.JavaConverters._
import giteri.run.jarVersion.JarVersion

object RunCouple {
  def main(args : Array[String]): Unit =
  {
    run(9494.57,3,121,.1,.2,.3,.4)
  }

  /**fonction qui prend en param le nombre de CA activé (nbActivator), un nombre entre 0 et 100 qu'il va
   * falloir reprojeter pour en faire une selection dans les espaces des possibilités de selection combinatoire
   *  et un array de double d'une taille maxi dont les (nbActivator) premiere valeurs seront données aux couples
   *  selectionnées
   *
   * @param activationCode entre 0 et 10000
   * @param nbMeme entre 1 et 4
   * @param maxCombinaison 11*11
   * @param proba1
   * @param proba2
   * @param proba3
   * @param proba4
   * @return
   */
  def run(activationCode: Double, nbMeme: Int, maxCombinaison:Int,
          proba1: Double,proba2: Double,proba3: Double, proba4: Double): Double = {
    // Nombre total de combinaison d'activation de nbCouple parmi les totalDispo
    var nbPermutation : Int = combSansRepSimplified(nbMeme, maxCombinaison)
    var thoudans : Double = 10000;
    var tmp : Double = (activationCode * nbPermutation) / thoudans
    // conversion de selector double de openmole en int de selection plus tard en java

    println("i'm call selector;conversion "+ activationCode+";"+tmp+" nbMeme:maxCombi "+nbMeme+":"+maxCombinaison
    +" pour "+ nbPermutation +" combinaisons")

    giteri.run.jarVersion.JarVersionCast.run(tmp,nbMeme,nbPermutation, proba1,proba2,proba3, proba4)
  }

  def combSansRepSimplified(k:Int, parmiN:Int) : Int =
  {
    return semiFact(parmiN-k+1,parmiN) / factorial(k);
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

}