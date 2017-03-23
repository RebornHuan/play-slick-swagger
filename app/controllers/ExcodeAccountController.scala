package controllers

import javax.inject.Inject

import dal.{ExcodeAccountRepository, ExcodeInfoRepository}
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Api("ExcodeAccountController")
class ExcodeAccountController @Inject()(ear: ExcodeAccountRepository,
                                        eir: ExcodeInfoRepository,
                                        val messagesApi: MessagesApi)
                                       (implicit ec: ExecutionContext) extends Controller with I18nSupport {

  @ApiOperation(notes = "getAccounts", httpMethod = "GET", value = "获取商家账户列表")
  def getAccounts(@ApiParam(required = true, value = "商户代码") excode: String) = Action.async {
    ear.listAccountsByExcodes(excode).map { excode =>
      Ok(Json.toJson(excode))
    }
  }

  /**
    * 绑定接口,如果账户不存在,就创建,如果存在,直接返回
    *
    * @param excode    商家代码
    * @param accountId 账户ID,[手机号码]
    * @return
    */
  @ApiOperation(notes = "bindAccount", httpMethod = "GET", value = "绑定接口")
  def bindAccount(@ApiParam(required = true, value = "商户代码") excode: String,
                  @ApiParam(required = true, value = "账户id") accountId: String) = Action.async {

    eir.queryByExcode(excode).flatMap {
      case Some(x) => {
        ear.selectAccount(excode, accountId).flatMap {
          /** 已经存在,不做处理 **/
          case Some(x) => Future {
            Ok("bind Success")
          }

          /** 创建账户 **/
          case None => ear.create(excode, accountId).map {
            case Success(_) => Ok("bind Success")
            case Failure(exception) => InternalServerError("bind Failure")
          }
        }
      }
      case None => Future {
        InternalServerError("excode info not Exists")
      }
    }

  }

  @ApiOperation(notes = "getBalance", httpMethod = "GET", value = "获取账户余额")
  def getBalance(@ApiParam(required = true, value = "商户代码") excode: String,
                 @ApiParam(required = true, value = "账户id") account: String) = Action.async {
    ear.selectBalance(excode, account).map { balance =>
      Ok(Json.toJson(balance))
    }
  }
}
