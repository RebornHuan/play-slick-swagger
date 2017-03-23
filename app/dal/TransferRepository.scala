package dal

import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class TransferRepository @Inject()(ear: ExcodeAccountRepository,
                                   arr: AccountRecordRepository,
                                   mar: MiddleAccountRepository,
                                   marr: MAccountRecordRepository
                                  )(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  protected val dbConfig = dbConfigProvider.get[JdbcProfile]
  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._


  def transferInto(excode: String, accountId: String, amount: Int,excdeType:Int):Future[Try[Int]] = {
    excdeType match {
      case 0 => transferWithoutMAccount(excode,accountId,-amount)
      case 1 => transferWithMAccount(excode,accountId,-amount)
    }
  }

  def transferOut(excode: String, accountId: String, amount: Int,excdeType:Int):Future[Try[Int]] = {
    excdeType match {
      case 0 => transferWithoutMAccount(excode,accountId,amount)
      case 1 => transferWithMAccount(excode,accountId,amount)
    }
  }

  def transferWithoutMAccount(excode: String, accountId: String, amount: Int):Future[Try[Int]] = {
    val actions = for {
    /**
      * step-1: 更新表 ExcodeAccount中金额
      * step-2: 添加 AccountRecord 流水账记录;
      */
      r1 <- ear._addAmount(excode, accountId, amount)
      r2 <- arr._create(excode, accountId, amount)
    } yield r2
    db.run(actions.transactionally.asTry)
  }

  def transferWithMAccount(excode: String, accountId: String, amount: Int):Future[Try[Int]] = {
    val actions = for {
    /**
      * step-1: 更新表 ExcodeAccount中金额
      * step-2: 更新表 MiddleAccount中金额
      * step-2: 添加 AccountRecord 流水账记录;
      */
      r1 <- ear._addAmount(excode, accountId, amount)
      r2 <- mar._addAmount(excode, -amount)
      r3 <- mar._selectMAccountId(excode)
      r4 <- marr._create(excode, r3, accountId, -amount)
    } yield r4
    db.run(actions.transactionally.asTry)
  }

}
