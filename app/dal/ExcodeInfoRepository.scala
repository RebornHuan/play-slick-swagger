package dal

import javax.inject.{Inject, Singleton}

import models.ExcodeInfo
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * A repository for people.
  *
  * @param dbConfigProvider The Play db config provider. Play will inject this for you.
  */
@Singleton
class ExcodeInfoRepository @Inject()(mar: MiddleAccountRepository)(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  protected val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  /**
    * Here we define the table. It will have a name of people
    */
  private class ExcodeInfoTable(tag: Tag) extends Table[ExcodeInfo](tag, "mock_excode_info") {

    /** The ID column, which is the primary key, and auto incremented */
    def excode = column[String]("excode", O.PrimaryKey)

    /** The name column */
    def description = column[String]("description")

    /** The age column */
    def excodeType = column[Int]("excode_type")


    def lastModifiedTime = column[java.sql.Timestamp]("last_modified_time")

    /**
      * This is the tables default "projection".
      *
      * It defines how the columns are converted to and from the Person object.
      *
      * In this case, we are simply passing the id, name and page parameters to the Person case classes
      * apply and unapply methods.
      */
    def * = (excode, description, excodeType, lastModifiedTime.?) <>((ExcodeInfo.apply _).tupled, ExcodeInfo.unapply)
  }

  /**
    * The starting point for all queries on the people table.
    */
  private val excodeInfo = TableQuery[ExcodeInfoTable]


  private val queryById = Compiled(
    (excode: Rep[String]) => excodeInfo.filter(_.excode === excode))

  def createWithoutMAccount(excode: String, description: String, excodeType: Int): Future[Try[Int]] = db.run {
    ((excodeInfo.map(p => (p.excode, p.description, p.excodeType))
      ) += (excode, description, excodeType)).asTry
  }

  def createWithMAccount(excode: String, description: String, excodeType: Int): Future[Try[Int]] = {
    val actions = for {
      r1 <- _create(excode, description, excodeType)
      r2 <- mar._addMAccount(excode)
    } yield r2
    db.run(actions.transactionally.asTry)
  }

  def _create(excode: String, description: String, excodeType: Int): DBIO[Int] = {
    (excodeInfo.map(p => (p.excode, p.description, p.excodeType))
      ) +=(excode, description, excodeType)
  }


  def queryByExcode(excode: String): Future[Option[ExcodeInfo]] = db.run(queryById(excode).result.headOption)

  def queryTypeByExcode(excode: String): Future[Option[Int]] = db.run(excodeInfo.filter(_.excode === excode).map(_.excodeType).result.headOption)


  /**
    * List all the people in the database.
    */
  def list(): Future[Seq[ExcodeInfo]] = db.run {
    excodeInfo.result
  }
}
