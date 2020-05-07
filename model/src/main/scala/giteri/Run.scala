package giteri
import java.io.File

import giteri.network.networkStuff.NetworkFileLoader
import giteri.run.jarVersion.JarVersion
import giteri.tool.other.WriteNRead

import scala.util.Random

object Run{

  def main(args: Array[String]): Unit = {
    val network = new File(args(0))
    val one: Double = args(1).toDouble
    val two: Double = args(2).toDouble
    val three: Double = args(3).toDouble
    val four: Double = args(4).toDouble
    val five: Double = args(5).toDouble
    val six: Double = args(6).toDouble
    val seven: Double = args(7).toDouble
    val eight: Double = args(8).toDouble
    val nine: Double = args(9).toDouble
    val ten: Double = args(10).toDouble
    val eleven: Double = args(11).toDouble
    val twelve: Double = args(12).toDouble
    val thirteen: Double = args(13).toDouble
    val oneA: Integer = args(14).toInt
    val twoA: Integer = args(15).toInt
    val threeA: Integer = args(16).toInt
    val fourA: Integer = args(17).toInt
    val fiveA: Integer = args(18).toInt
    val sixA: Integer = args(19).toInt
    val sevenA: Integer = args(20).toInt
    val eightA: Integer = args(21).toInt
    val nineA: Integer = args(22).toInt
    val tenA: Integer = args(23).toInt
    val elevenA: Integer = args(24).toInt
    val twelveA: Integer = args(25).toInt
    val thirteenA: Integer = args(26).toInt
    val seed: Long = Random.nextLong()

//    println( run(network, one, oneA, two, twoA,
//      three, threeA, four, fourA,
//      five, fiveA, six, sixA,
//      seven, sevenA, eight, eightA,
//      nine, nineA, ten, tenA,
//      eleven, elevenA, twelve, twelveA,
//      thirteen, thirteenA,
//      seed))
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

  def run(network: File, one: Double, oneActi: Integer, two: Double, twoActi: Integer,
          three: Double, threeActi: Integer, four: Double, fourActi: Integer,
          five : Double, fiveActi: Integer, six: Double, sixActi:Integer,
          sevn: Double, sevnActi: Integer, hei: Double, heiActi: Integer,
          nine: Double, nineActi: Integer, ten: Double, tenActi:Integer,
          eleven: Double, elevenActi: Integer, twelve: Double, twelveActi: Integer,
          thirteen: Double, thirteenActi: Integer, fourteen: Double, fourteenActi: Integer,
          fiveteen: Double, fiveteenActi: Integer, sixteen: Double, sixteenActi: Integer,
          seventeen: Double, seventeenActi: Integer,heighteen: Double, heighteenActi: Integer,
          nineteen: Double, nineteenActi: Integer,twenty: Double, twentyActi: Integer,
          seed: Long):Double = { //region Param
    val rand = new java.util.Random(seed)
    JarVersion.run(network,
      oneActi.==(1),twoActi.==(1), threeActi.==(1), fourActi.==(1), fiveActi.==(1), sixActi.==(1), sevnActi.==(1),
      heiActi.==(1), nineActi.==(1),tenActi.==(1), elevenActi.==(1), twelveActi.==(1), thirteenActi.==(1),
      fourteenActi.==(1), fiveteenActi.==(1), sixteenActi.==(1),seventeenActi.==(1),heighteenActi.==(1),
      nineteenActi.==(1), twentyActi.==(1),
      one, two, three, four, five, six, sevn, hei, nine, ten, eleven, twelve, thirteen,
      fourteen, fiveteen, sixteen, seventeen, heighteen, nineteen, twenty)
  }

  def formula(one:Double, two:Double, three:Double) : Double = {
//    print(System.getProperty("user.dir"))
//    print(new java.io.File(".").getCanonicalPath)
    val four = (one * two) / ( two + three )
    four
  }
}
