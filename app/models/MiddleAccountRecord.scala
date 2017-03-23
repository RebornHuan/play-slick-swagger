package models

import java.sql.Timestamp
import java.text.SimpleDateFormat

import play.api.libs.json._

case class MiddleAccountRecord(id: Int ,excode: String, mAccount: String, account: String, balance: Int, lastModifiedTime: Option[java.sql.Timestamp] = None)

object MiddleAccountRecord {

  implicit object timeFormat extends Format[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")

    def reads(json: JsValue) = {
      val str = json.as[String]
      JsSuccess(new Timestamp(format.parse(str).getTime))
    }

    def writes(ts: Timestamp) = JsString(format.format(ts))
  }

  implicit val accountFormat = Json.format[MiddleAccountRecord]
}