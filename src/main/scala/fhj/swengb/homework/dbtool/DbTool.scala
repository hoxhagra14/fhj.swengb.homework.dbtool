package fhj.swengb.homework.dbtool

import java.net.URL
import java.sql.{Connection, DriverManager, ResultSet, Statement}
import java.util.ResourceBundle
import javafx.application.Application
import javafx.collections.{ObservableList, FXCollections}
import javafx.fxml.{FXML, Initializable, FXMLLoader}
import javafx.scene.control.TableView
import javafx.scene.layout.AnchorPane
import javafx.scene.{Scene, Parent}
import javafx.stage.Stage
import javax.swing.table.TableColumn

import javafx.scene.control.cell.PropertyValueFactory

import fhj.swengb.homework.dbtool.Product._
import fhj.swengb.homework.dbtool.Customer._
import scala.util.Try
import scala.util.control.NonFatal

/**
  * Example to connect to a database.
  *
  * Initializes the database, inserts example data and reads it again.
  *
  */
object Db {

  /**
    * A marker interface for datastructures which should be persisted to a jdbc database.
    *
    * @tparam T the type to be persisted / loaded
    */
  trait DbEntity[T] {

    /**
      * Recreates the table this entity is stored in
      *
      * @param stmt
      * @return
      */
    def reTable(stmt: Statement): Int

    /**
      * Saves given type to the database.
      *
      * @param c
      * @param t
      * @return
      */
    def toDb(c: Connection)(t: T): Int

    /**
      * Given the resultset, it fetches its rows and converts them into instances of T
      *
      * @param rs
      * @return
      */
    def fromDb(rs: ResultSet): List[T]

    /**
      * Queries the database
      *
      * @param con
      * @param query
      * @return
      */
    def query(con: Connection)(query: String): ResultSet = {
      con.createStatement().executeQuery(query)
    }

    /**
      * Sql code necessary to execute a drop table on the backing sql table
      *
      * @return
      */
    def dropTableSql: String

    /**
      * sql code for creating the entity backing table
      */
    def createTableSql: String

    /**
      * sql code for inserting an entity.
      */
    def insertSql: String

  }

  lazy val maybeConnection: Try[Connection] =
    Try(DriverManager.getConnection("jdbc:sqlite::memory:"))

}

object DbTool {

  val c1:Customer = Customer(1000,"Lala Hans", "Hallogasse 1337, 8010 Graz")
  val c2:Customer = Customer(1001,"Hans Hans", "Dawohnichstrasse 0, 8020 Graz")
  val c3:Customer = Customer(1002,"Hans Franz", "Gasse 1, 8010 Graz")
  val c4:Customer = Customer(1003,"Test Hans", "Strasse 1000, 8010 Graz")
  val c5:Customer = Customer(1004,"Test Franz", "Hansgasse 10, 1010 Wien")

  val customers:Set[Customer] = Set(c1,c2,c3,c4,c5)

  val p1:Product = Product(1,"Produkt1",1.0)
  val p2:Product = Product(2,"Produkt2",2.0)
  val p3:Product = Product(3,"Produkt3",3.0)
  val p4:Product = Product(4,"Produkt4",4.0)
  val p5:Product = Product(5,"Produkt5",5.0)
  val p6:Product = Product(6,"Produkt6",6.0)
  val p7:Product = Product(7,"Produkt7",7.0)
  val p8:Product = Product(8,"Produkt8",8.0)
  val p9:Product = Product(9,"Produkt9",9.0)

  val products:Set[Product] = Set(p1,p2,p3,p4,p5,p6,p7,p8,p9)
// test


  def main(args: Array[String]) {
    for {con <- Db.maybeConnection
         _ = Product.reTable(con.createStatement())
         _ = products.map(Product.toDb(con)(_))
         s <- Product.fromDb(Product.queryAll(con))} {
      println(s)
    }
    for {con <- Db.maybeConnection
        _ = Product.toDb(con)(Product(10,"Produkt10",10.0))
        x <- Product.fromDb(Product.queryAll(con))} {
      println(x)
    }
    for {con <- Db.maybeConnection
         _ = Customer.reTable(con.createStatement())
         _ = customers.map(Customer.toDb(con)(_))
         y <- Customer.fromDb(Customer.queryAll(con))} {
      println(y)
    }

    Application.launch(classOf[DbTool], args: _*)
  }


}

class DbTool extends javafx.application.Application {

  val Fxml = "testgui.fxml"
  //val Css = "fhj/swengb/avatarix/Gruppe2Avatarix.css"

  val loader = new FXMLLoader(getClass.getResource(Fxml))

  override def start(stage: Stage): Unit =
    try {
      stage.setTitle("DBTool")
      loader.load[Parent]() // side effect
      val scene = new Scene(loader.getRoot[Parent])
      stage.setScene(scene)
      //stage.getScene.getStylesheets.add(Css)
      stage.show()
    } catch {
      case NonFatal(e) => e.printStackTrace()
    }

}

class DBToolController extends Initializable {
  @FXML var anchorpane: AnchorPane = _
  @FXML var tableview: TableView[Product] = _
  @FXML var c1 : TableColumn[Int] = _
  @FXML var c2 : TableColumn[String] = _
  @FXML var c3 : TableColumn[Double] = _




  override def initialize(location: URL, resources: ResourceBundle): Unit = {

    initializeGUI()
  }

  def initializeGUI():Unit = {
    getProducts()

  }


  def getProducts(): Unit = {

    // http://stackoverflow.com/questions/18497699/populate-a-tableview-using-database-in-javafx



      val products:ObservableList[Product] = FXCollections.observableArrayList()
      val con:Connection = Db.maybeConnection.get
      val rs:ResultSet = Product.queryAll(con)

      while(rs.next()){
        val product = new Product(rs.getInt("id"), rs.getString("name"), rs.getDouble("price"))

        products.add(product)
      }

      tableview.setItems(products)
      tableview.refresh()



  }

}



