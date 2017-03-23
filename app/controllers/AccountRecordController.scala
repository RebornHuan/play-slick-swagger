package controllers

import javax.inject.Inject

import dal.AccountRecordRepository
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

@Api("AccountRecordController")
class AccountRecordController @Inject()(arr: AccountRecordRepository, val messagesApi: MessagesApi)
                                       (implicit ec: ExecutionContext) extends Controller with I18nSupport {
  @ApiOperation(notes = "getRecordsByAccount", httpMethod = "GET", value = "获取某个账号的交易记录")
  def getRecordsByAccount(@ApiParam(required = true, value = "商户代码")excode: String,
                          @ApiParam(required = true, value = "账号信息")account: String) = Action.async {
    arr.listByAccount(excode, account).map { excode =>
      Ok(Json.toJson(excode))
    }
  }

  @ApiOperation(notes = "getRecordsByExcode", httpMethod = "GET", value = "获取某个商家的所有交易记录")
  def getRecordsByExcode(@ApiParam(required = true, value = "商户代码")excode: String) = Action.async {
    arr.listByExcode(excode).map { result =>
      Ok(Json.toJson(result))
    }
  }

}
