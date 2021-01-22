package funcdiff

object Statistics {

  private var map = Map[String, String]()

  def getMap: Map[String, String] = map;

  def setStat(string: String, int: String): Unit = map = (map + (string -> int))

  def returnPrintableString: String = {
    val sb = new StringBuilder;
    for (elem <- map) {
      sb.append("\t").append(elem._1).append(" = ").append(elem._2).append("\n")
    }
    sb.toString();
  }

}
