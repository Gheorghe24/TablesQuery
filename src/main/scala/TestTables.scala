import scala.io.Source

object TestTables {
  val table1: Table = new Table(
    List("col1", "col2"), List(
      List("a", "2"),
      List("b", "3"),
      List("c", "4"),
      List("d", "5")
    ))

  val table1String: String = {
    val src = Source.fromFile("tables/table1.csv")
    val str = src.mkString
    src.close()
    str.replace("\r", "")
  }

  val table2: Table = {
    val src = Source.fromFile("tables/table2.csv")
    val str = src.mkString
    src.close()
    Table(str.replace("\r", ""))
  }

  val table3: Table = {
    val src = Source.fromFile("tables/table3.csv")
    val str = src.mkString
    src.close()
    Table(str.replace("\r", ""))
  }

  val table4: Table = {
    val src = Source.fromFile("tables/table4.csv")
    val str = src.mkString
    src.close()
    Table(str.replace("\r", ""))
  }

  val table3_4_merged: Table = {
    val src = Source.fromFile("tables/table3_4_merged.csv")
    val str = src.mkString
    src.close()
    Table(str.replace("\r", ""))
  }

  val test3_newCol_Value: Table = {
    val src = Source.fromFile("tables/test_3_newCol_Value.csv")
    val str = src.mkString
    src.close()
    Table(str.replace("\r", ""))
  }

  val test3_Select_Value: Table = {
    val src = Source.fromFile("tables/test_3_Select_Value.csv")
    val str = src.mkString
    src.close()
    Table(str.replace("\r", ""))
  }

  val test3_Filter_Value: Table = {
    val src = Source.fromFile("tables/test_3_Filter_Value.csv")
    val str = src.mkString
    src.close()
    Table(str.replace("\r", ""))
  }

  val test3_Merge_Value: Table = {
    val src = Source.fromFile("tables/test_3_Merge_Value.csv")
    val str = src.mkString
    src.close()
    Table(str.replace("\r", ""))
  }

  val tableFunctional: Table = {
    val src = Source.fromFile("tables/Functional.csv")
    val str = src.mkString
    src.close()
    Table(str.replace("\r", ""))
  }

  val tableObjectOriented: Table = {
    val src = Source.fromFile("tables/Object-Oriented.csv")
    val str = src.mkString
    src.close()
    Table(str.replace("\r", ""))
  }

  val tableImperative: Table = {
    val src = Source.fromFile("tables/Imperative.csv")
    val str = src.mkString
    src.close()
    Table(str.replace("\r", ""))
  }

  val ref_programmingLanguages1: Table = {
    val src = Source.fromFile("tables/test_3_1.csv")
    val str = src.mkString
    src.close()
    Table(str.replace("\r", ""))
  }

  val ref_programmingLanguages2: Table = {
    val src = Source.fromFile("tables/test_3_2.csv")
    val str = src.mkString
    src.close()
    Table(str.replace("\r", ""))
  }

  val ref_programmingLanguages3: Table = {
    val src = Source.fromFile("tables/test_3_3.csv")
    val str = src.mkString
    src.close()
    Table(str.replace("\r", ""))
  }

  private val defaultTable = new Table(List(), List(List()))


  // 3.1
  def programmingLanguages1: Table = {
    val defaultValue = "Yes"

    val newCol1 = tableFunctional.newCol("Functional", defaultValue)
    val newCol2 = tableObjectOriented.newCol("Object-Oriented", defaultValue)
    val newCol3 = tableImperative.newCol("Imperative", defaultValue)

    val merge1 = newCol1.merge("Language", newCol2)
    merge1 match {
      case None => defaultTable
      case Some(value) => value.merge("Language", newCol3) match {
        case None => defaultTable
        case Some(value) => value
      }
    }
  }

  // 3.2
  val programmingLanguages2: Table = {
    val containsApplication: String => Boolean = (s: String) => s.contains("Application")
    val languageField: Field = Field("Original purpose", containsApplication)

    val containsConcurrent: String => Boolean = (s: String) => s.contains("concurrent")
    val languageField2: Field = Field("Other paradigms", containsConcurrent)

    programmingLanguages1.filter(languageField) match {
      case None => defaultTable
      case Some(value) => {
        value.filter(languageField2) match {
          case None => defaultTable
          case Some(value) => value
        }
      }
    }

  }

  // 3.3
  val programmingLanguages3: Table = {
    val select1 = programmingLanguages2.select(List("Language", "Object-Oriented", "Functional"))
    select1 match {
      case None => defaultTable
      case Some(value) => value
    }

  }

}
