package dal

import javax.inject.{Inject, Singleton}

import models.MiddleAccountRecord
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MAccountRecordRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  protected val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  private class MAccountRecordTable(tag: Tag) extends Table[MiddleAccountRecord](tag, "mock_maccount_record") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def excode = column[String]("excode")

    def mAccountId = column[String]("middle_account_id")

    def accountId = column[String]("account_id")

    def balanceRecord = column[Int]("balance_record")

    def lastModifiedTime = column[java.sql.Timestamp]("last_modified_time")

    def * = (id, excode, mAccountId, accountId, balanceRecord, lastModifiedTime.?) <>((MiddleAccountRecord.apply _).tupled, MiddleAccountRecord.unapply)
  }

  private val mAccountRecord = TableQuery[MAccountRecordTable]

  def listByExcode(excode: String): Future[Seq[MiddleAccountRecord]] = db.run {
    mAccountRecord.filter(_.excode === excode).result
  }

  def create(excode: String, mAccountId: String, accountId: String, balanceRecord: Int): Future[MiddleAccountRecord] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (mAccountRecord.map(p => (p.excode, p.mAccountId, p.accountId, p.balanceRecord))
      // Now define it to return the id, because we want to know what id was generated for the person
      returning mAccountRecord.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((nameAge, id) => MiddleAccountRecord(id, nameAge._1, nameAge._2, nameAge._3, nameAge._4))
      // And finally, insert the person into the database
      ) +=(excode, mAccountId, accountId, balanceRecord)
  }

  def _create(excode: String, mAccountId: String, accountId: String, balanceRecord: Int): DBIO[Int] = {
    (mAccountRecord.map(p => (p.excode, p.mAccountId, p.accountId, p.balanceRecord))
      ) +=(excode, mAccountId, accountId, balanceRecord)
  }
}
