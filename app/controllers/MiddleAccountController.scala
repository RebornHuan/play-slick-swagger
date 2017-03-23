package controllers

import javax.inject.Inject

import dal.MiddleAccountRepository
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

@Api("MiddleAccountController")
class MiddleAccountController @Inject()(mar: MiddleAccountRepository, val messagesApi: MessagesApi)
                                       (implicit ec: ExecutionContext) extends Controller with I18nSupport {


  @ApiOperation(notes = "getAccounts", httpMethod = "GET", value = "获取中间账号列表")
  def getAccounts = Action.async {
    mar.list().map { excode =>
      Ok(Json.toJson(excode))
    }
  }

  @ApiOperation(notes = "getBalance", httpMethod = "GET", value = "获取某个商家中间账号信息")
  def getBalance(@ApiParam(required = true, value = "商户代码")excode: String) = Action.async {
    mar.selectBalance(excode).map { balances =>
      Ok(Json.toJson(balances))
    }
  }

  @ApiOperation(notes = "addBalance", httpMethod = "PUT", value = "为中间账号添加余额")
  def addBalance(@ApiParam(required = true, value = "商户代码") excode: String,
                 @ApiParam(required = true, value = "增减数量,正/负") amount: Int) = Action.async {
    mar.addAmount(excode, amount).map { balances =>
      Ok(Json.toJson(balances))
    }
  }

  def addMAccount(excode: String, amount: Int = 1000) = Action.async {
    mar.addMAccount(excode, amount).map { result =>
      result match {
        case Success(json: Int) => Ok(Json.toJson(json))
        case Failure(_) => InternalServerError("Something terrible happened..")
      }
    }
  }
}
