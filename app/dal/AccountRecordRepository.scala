package dal

import javax.inject.{Inject, Singleton}

import models.{AccountRecord, MiddleAccountRecord}
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class AccountRecordRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  protected val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  private class AccountRecordTable(tag: Tag) extends Table[AccountRecord](tag, "mock_account_record") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def excode = column[String]("excode")

    def accountId = column[String]("account_id")

    def balanceRecord = column[Int]("balance_record")

    def lastModifiedTime = column[java.sql.Timestamp]("last_modified_time")

    def * = (id, excode, accountId, balanceRecord, lastModifiedTime.?) <>((AccountRecord.apply _).tupled, AccountRecord.unapply)
  }

  private val accountRecord = TableQuery[AccountRecordTable]

  def listByAccount(excode: String, account: String): Future[Seq[AccountRecord]] = db.run {
    accountRecord.filter(_.excode === excode).filter(_.accountId === account).result
  }

  def listByExcode(excode: String): Future[Seq[AccountRecord]] = db.run {
    accountRecord.filter(_.excode === excode).result
  }

  def _create(excode: String, accountId: String, balanceRecord: Int): DBIO[Int] = {
    (accountRecord.map(p => (p.excode, p.accountId, p.balanceRecord))
      ) +=(excode, accountId, balanceRecord)
  }

  def create(excode: String, accountId: String, balanceRecord: Int): Future[AccountRecord] = db.run {
    (accountRecord.map(p => (p.excode, p.accountId, p.balanceRecord))
      returning accountRecord.map(_.id)
      into ((nameAge, id) => AccountRecord(id, nameAge._1, nameAge._2, nameAge._3))
      ) +=(excode, accountId, balanceRecord)
  }

}
