package traza.util

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}

object DateFormatter {


  val readingFormatter = new DateTimeFormatterBuilder()
    .appendPattern("[yyyy-MM-dd'T'HH:mm:ss.SSS'Z'][yyyy-MM-dd'T'HH:mm:ss'Z'][yyyy-MM-dd HH:mm:ss.SSS]")
    .toFormatter();


  val writingFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
}
