package controllers

import javax.inject.Inject

import dal.MAccountRecordRepository
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

@Api("MAccountRecordController")
class MAccountRecordController @Inject()(marr: MAccountRecordRepository, val messagesApi: MessagesApi)
                                        (implicit ec: ExecutionContext) extends Controller with I18nSupport {

  @ApiOperation(notes = "getRecordsByExcode", httpMethod = "GET", value = "获取某个商家的中间账号交易记录")
  def getRecordsByExcode(@ApiParam(required = true, value = "商户代码")excode: String) = Action.async {
    marr.listByExcode(excode).map { result =>
      Ok(Json.toJson(result))
    }
  }

  def add(excode: String, mAccountId: String, accountId: String, balanceRecord: Int) = Action.async {
    marr.create(excode, mAccountId, accountId, balanceRecord).map { result =>
      Ok(Json.toJson(result))
    }
  }

}
