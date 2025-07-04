import com.olafura.jsondiff.JsonDiff
import play.api.libs.json.Json
import scala.io.Source

object ProfileBig {
  def main(args: Array[String]): Unit = {
    val big1 = Source.fromFile("profile-data/ModernAtomic.json")
    val big2 = Source.fromFile("profile-data/LegacyAtomic.json")

    try {
      val bigJSON1 = Json.parse(big1.mkString)
      val bigJSON2 = Json.parse(big2.mkString)
      JsonDiff(bigJSON1, bigJSON2).diff()
    } finally {
      big1.close()
      big2.close()
    }
  }
}
