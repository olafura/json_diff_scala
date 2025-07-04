import com.olafura.jsondiff.JsonDiff
import play.api.libs.json.Json
import scala.io.Source

object ProfileMedium {
  def main(args: Array[String]): Unit = {
    val cdcSource = Source.fromFile("profile-data/cdc.json")
    val edgSource = Source.fromFile("profile-data/edg.json")

    try {
      val cdcJSON = Json.parse(cdcSource.mkString)
      val edgJSON = Json.parse(edgSource.mkString)
      for (i <- 1 to 50) {
        val diff =JsonDiff(cdcJSON, edgJSON).diff()
      }
    } finally {
      cdcSource.close()
      edgSource.close()
    }
  }
}
