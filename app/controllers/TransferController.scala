package controllers

import javax.inject.Inject

import dal._
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Api("TransferController")
class TransferController @Inject()(mar: MiddleAccountRepository
                                   , val messagesApi: MessagesApi
                                   , ear: ExcodeAccountRepository
                                   , arr: AccountRecordRepository
                                   , mrr: MAccountRecordRepository
                                   , eir: ExcodeInfoRepository
                                   , tfr: TransferRepository
                                  )(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  @ApiOperation(notes = "transferInto", httpMethod = "GET", value = "转入中间账户")
  def transferInto(@ApiParam(required = true, value = "商户代码") excode: String,
                   @ApiParam(required = true, value = "账户id") accountId: String,
                   @ApiParam(required = true, value = "转账金额") amount: Int) = Action.async {
    eir.queryTypeByExcode(excode).flatMap {
      case Some(x) =>
        tfr.transferInto(excode, accountId, amount, x).map {
          case Success(json: Int) => Ok("success")
          case Failure(_) => InternalServerError("Something1")
        }
      case None => Future {
        InternalServerError("Something2")
      }
    }
  }

  @ApiOperation(notes = "transferOut", httpMethod = "GET", value = "转出中间账户")
  def transferOut(@ApiParam(required = true, value = "商户代码") excode: String,
                  @ApiParam(required = true, value = "账户id") accountId: String,
                  @ApiParam(required = true, value = "转账金额") amount: Int) = Action.async {
    eir.queryTypeByExcode(excode).flatMap {
      case Some(x) =>
        tfr.transferOut(excode, accountId, amount, x).map {
          case Success(json: Int) => Ok("success")
          case Failure(_) => InternalServerError("Something1")
        }
      case None => Future {
        InternalServerError("Something2")
      }
    }
  }
}
