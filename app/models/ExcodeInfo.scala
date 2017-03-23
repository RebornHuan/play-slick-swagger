package models

import java.sql.Timestamp
import java.text.SimpleDateFormat

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json._

case class ExcodeInfo(@ApiModelProperty(value = "商家代码", required = true) excode: String,
                      @ApiModelProperty(value = "商家描述", required = true) description: String,
                      @ApiModelProperty(value = "商家有无中间账号,0:无中间账号 1:有中间账号", required = true) excodeType: Int,
                      @ApiModelProperty(value = "时间", required = false) lastModifiedTime: Option[java.sql.Timestamp] = None)

case class ExcodeInfoBody(@ApiModelProperty(value = "商家代码", required = true) excode: String,
                      @ApiModelProperty(value = "商家描述", required = true) description: String,
                      @ApiModelProperty(value = "商家有无中间账号,0:无中间账号 1:有中间账号", required = true) excodeType: Int)

object ExcodeInfo {

  implicit object timeFormat extends Format[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")

    def reads(json: JsValue) = {
      val str = json.as[String]
      JsSuccess(new Timestamp(format.parse(str).getTime))
    }

    def writes(ts: Timestamp) = JsString(format.format(ts))
  }

  implicit val excodeFormat = Json.format[ExcodeInfo]
  implicit val excode1Format = Json.format[ExcodeInfoBody]

}

object ExcodeInfoBody {

  implicit object timeFormat extends Format[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")

    def reads(json: JsValue) = {
      val str = json.as[String]
      JsSuccess(new Timestamp(format.parse(str).getTime))
    }

    def writes(ts: Timestamp) = JsString(format.format(ts))
  }

  implicit val excode1Format = Json.format[ExcodeInfoBody]

}
