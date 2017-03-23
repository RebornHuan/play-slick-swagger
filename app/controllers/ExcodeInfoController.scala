package controllers

import javax.inject.Inject

import dal.ExcodeInfoRepository
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import models.{ExcodeInfo, ExcodeInfoBody}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, BodyParsers, Controller}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Api("ExcodeInfoController")
class ExcodeInfoController @Inject()(eir: ExcodeInfoRepository, val messagesApi: MessagesApi)
                                    (implicit ec: ExecutionContext) extends Controller with I18nSupport {


  /**
    * 查询商家列表
    *
    * @return
    */
  @ApiOperation(notes = "getExcodes", httpMethod = "GET", value = "获取已有商家列表")
  def getExcodes = Action.async {
    eir.list().map { excode =>
      Ok(Json.toJson(excode))
    }
  }

  /**
    * 是查询某个商家信息
    *
    * @param excode 商家编号
    * @return
    */
  @ApiOperation(notes = "getExcode", httpMethod = "GET", value = "获取某个商家信息")
  def getExcode(excode: String) = Action.async {
    eir.queryByExcode(excode).map {
      case Some(data) => Ok(Json.toJson(data));
    }
  }

  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      value = "Make Reservation for a resource",
      required = true,
      dataType = "models.ExcodeInfoBody", // complete path
      paramType = "body"
    )
  ))
  def createExcode = Action.async(BodyParsers.parse.json[ExcodeInfoBody]) { request =>
    val excodeInfo = request.body

    eir.queryByExcode(excodeInfo.excode).flatMap {
      /** 商家已经存在 不做更新, 不做处理 **/
      case Some(x) => Future {
        Ok("商家已存在")
      }
      case None => {
        excodeInfo.excodeType match {
          case 0 => eir.createWithoutMAccount(excodeInfo.excode, excodeInfo.description, excodeInfo.excodeType).map {
            case Success(_) => Ok("无中间账号商家创建成功")
            case Failure(_) => InternalServerError("商家创建失败")
          }
          case 1 => eir.createWithMAccount(excodeInfo.excode, excodeInfo.description, excodeInfo.excodeType).map {
            case Success(_) => Ok("有中间账号商家创建成功")
            case Failure(_) => InternalServerError("商家创建失败")
          }
          case _ => Future {
            InternalServerError("excodeType 只能为 0 或者 1")
          }
        }
      }

    }

  }

}
