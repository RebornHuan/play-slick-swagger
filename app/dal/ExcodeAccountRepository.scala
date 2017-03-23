package dal

import javax.inject.{Inject, Singleton}

import models.{ExcodeAccount, MiddleAccount}
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class ExcodeAccountRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  // We want the JdbcProfile for this provider
  protected val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  private class ExcodeAccountTable(tag: Tag) extends Table[ExcodeAccount](tag, "mock_excode_account") {

    def excode = column[String]("excode")

    def accountId = column[String]("account_id")

    def balance = column[Int]("balance")

    def lastModifiedTime = column[java.sql.Timestamp]("last_modified_time")

    def * = (excode, accountId, balance, lastModifiedTime.?) <>((ExcodeAccount.apply _).tupled, ExcodeAccount.unapply)
  }

  private val excodeAccount = TableQuery[ExcodeAccountTable]


  def list(): Future[Seq[ExcodeAccount]] = db.run {
    excodeAccount.result
  }

  def listAccountsByExcodes(excode: String): Future[Seq[ExcodeAccount]] = {
    db.run(excodeAccount.filter(_.excode === excode).result)
  }


  def selectBalance(excode: String, accountId: String): Future[Int] = db.run {
    (excodeAccount.filter(_.excode === excode)
      .filter(_.accountId === accountId)
      .map(_.balance)
      .result.head
      )
  }

  def selectAccount(excode: String, accountId: String): Future[Option[ExcodeAccount]] = db.run {
    (excodeAccount.filter(_.excode === excode)
      .filter(_.accountId === accountId)
      .result.headOption
      )
  }

  def _addAmount(excode: String, accountId: String, amount: Int): DBIO[Int] = {
    (for {
      one <- excodeAccount.filter(_.excode === excode)
        .filter(_.accountId === accountId)
        .map(_.balance)
        .result.head
      p <- excodeAccount.filter(_.excode === excode)
        .filter(_.accountId === accountId)
        .map(_.balance).update(amount + one)
    } yield p)
  }

  def addAmount(excode: String, accountId: String, amount: Int): Future[Int] = db.run {
    _addAmount(excode, accountId, amount)
  }

  def _create(excode: String, accountId: String, amount: Int = 2000): DBIO[Int] = {
    (excodeAccount.map(p => (p.excode, p.accountId, p.balance))
      ) +=(excode, accountId, amount)
  }

  def create(excode: String, accountId: String, amount: Int = 2000): Future[Try[Int]] = db.run {
    _create(excode, accountId, amount).asTry
  }

}
