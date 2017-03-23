package dal

import javax.inject.{Inject, Singleton}

import models.MiddleAccount
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


@Singleton
class MiddleAccountRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  protected val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  private class MiddleAccountTable(tag: Tag) extends Table[MiddleAccount](tag, "mock_maccount") {

    def excode = column[String]("excode", O.PrimaryKey)

    def mAccountId = column[String]("middle_account_id")

    def balance = column[Int]("balance")

    def lastModifiedTime = column[java.sql.Timestamp]("last_modified_time")

    def * = (excode, mAccountId, balance, lastModifiedTime.?) <>((MiddleAccount.apply _).tupled, MiddleAccount.unapply)
  }

  private val mAccount = TableQuery[MiddleAccountTable]

  def list(): Future[Seq[MiddleAccount]] = db.run {
    mAccount.result
  }

  def selectBalance(excode: String): Future[Seq[Int]] = db.run {
    (mAccount.filter(_.excode === excode)
      .map(_.balance)
      .result
      )
  }

  def update(excode: String, amount: Int): Future[Int] = {
    val q = mAccount.filter(_.excode === excode).map(_.balance).update(amount)
    db.run(q)
  }

  def _selectMAccountId(excode: String): DBIO[String] = {
    mAccount.filter(_.excode === excode)
      .map(_.mAccountId)
      .result
      .head
  }

  def _addAmount(excode: String, amount: Int): DBIO[Int] = {
    (for {
      one <- mAccount.filter(_.excode === excode)
        .map(_.balance)
        .take(1).result.head
      p <- mAccount.filter(_.excode === excode).map(_.balance).update(amount + one)
    } yield p)
  }

  def addAmount(excode: String, amount: Int): Future[Int] = db.run {
    _addAmount(excode, amount)
  }

  def addMAccount(excode: String, amount: Int): Future[Try[Int]] = db.run {
    _addMAccount(excode, amount).asTry
  }

  def _addMAccount(excode: String, amount: Int = 2000): DBIO[Int] = {
    val mAccountId = excode + "123456789"
    (mAccount.map(p => (p.excode, p.mAccountId, p.balance))
      ) +=(excode, mAccountId, amount)
  }
}
