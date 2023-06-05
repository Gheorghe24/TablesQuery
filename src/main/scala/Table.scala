import util.Util.{Line, Row}

trait FilterCond {
  def &&(other: FilterCond): FilterCond = {
    And(this, other)
  }

  def ||(other: FilterCond): FilterCond = {
    Or(this, other)
  }

  // fails if the column name is not present in the row
  def eval(r: Row): Option[Boolean]
}

case class Field(colName: String, predicate: String => Boolean) extends FilterCond {
  override def eval(r: Row): Option[Boolean] = {
    if (r.contains(colName)) {
      val value = r(colName)
      Some(predicate(value))
    } else {
      None
    }
  }
}


case class And(f1: FilterCond, f2: FilterCond) extends FilterCond {
  // 2.2.
  override def eval(r: Row): Option[Boolean] = {
    // evaluate the first filter condition
    f1.eval(r) match {
      case Some(true) => f2.eval(r)
      case Some(false) => Some(false)
      case None => None
    }
  }


}

case class Or(f1: FilterCond, f2: FilterCond) extends FilterCond {
  // 2.2.
  override def eval(r: Row): Option[Boolean] = {
    f1.eval(r) match {
      case Some(false) => f2.eval(r)
      case Some(true) => Some(true)
      case None => None
    }
  }
}

trait Query {
  def eval: Option[Table]
}

/*
  Atom query which evaluates to the input table
  Always succeeds
 */
case class Value(t: Table) extends Query {
  override def eval: Option[Table] = Some(t)
}

/*
  Selects certain columns from the result of a target query
  Fails with None if some rows are not present in the resulting table
 */
case class Select(columns: Line, target: Query) extends Query {
  override def eval: Option[Table] = {
    target.eval match {
      case Some(t) => t.select(columns)
      case None => None
    }
  }
}

/*
  Filters rows from the result of the target query
  Success depends only on the success of the target
 */
case class Filter(condition: FilterCond, target: Query) extends Query {
  override def eval: Option[Table] = {
    target.eval match {
      case Some(t) => t.filter(condition)
      case None => None
    }
  }
}

/*
  Creates a new column with default values
  Success depends only on the success of the target
 */
case class NewCol(name: String, defaultVal: String, target: Query) extends Query {
  override def eval: Option[Table] = {
    target.eval match {
      case Some(t) => Some(t.newCol(name, defaultVal))
      case None => None
    }
  }
}

/*
  Combines two tables based on a common key
  Success depends on whether the key exists in both tables or not AND on the success of the target
 */
case class Merge(key: String, t1: Query, t2: Query) extends Query {
  override def eval: Option[Table] = {
    (t1.eval, t2.eval) match {
      // call merge function on this tables and return the result
      case (Some(t1), Some(t2)) => t1.merge(key, t2)
      case _ => None
    }
  }
}


class Table(columnNames: Line, tabular: List[List[String]]) {
  def getColumnNames: Line = columnNames

  def getTabular: List[List[String]] = tabular

  // 1.1
  override def toString: String = {
    val tabularString = tabular.map(_.mkString(",")).mkString("\n")
    columnNames.mkString(",") + "\n" + tabularString
  }

  // 2.1
  def select(columns: Line): Option[Table] = {
    // check if all columns are present in the table
    if (columns.forall(columnNames.contains)) {

      // get the indices of the columns
      // use filter to get the indices of the columns
      val indices = columns
                          .filter(columnNames.contains)
                          .map(columnNames.indexOf(_))

      // get the rows
      val rows = tabular
                        .map(row => indices.map(row(_)))

      // create the new table
      Some(new Table(columns, rows))
    } else {
      None
    }
  }

  private def rowToMap(row: List[String]): Row = {
    columnNames.zip(row).toMap
  }

  // create the method that check if all columns are present in the table
  private def allColumnsPresent(cond: FilterCond): Boolean = {
    cond match {
      case Field(colName, _) => columnNames.contains(colName)
      case And(f1, f2) => allColumnsPresent(f1) && allColumnsPresent(f2)
      case Or(f1, f2) => allColumnsPresent(f1) && allColumnsPresent(f2)
    }
  }

  // 2.2
  def filter(cond: FilterCond): Option[Table] = {
    if (!allColumnsPresent(cond)) {
      return None
    }

    val foundRows = tabular.filter(row => cond.eval(rowToMap(row)).getOrElse(false))

    if (foundRows.nonEmpty) {
      Some(new Table(columnNames, foundRows))
    } else {
      None
    }

  }

  // 2.3.
  def newCol(name: String, defaultVal: String): Table = {
    val newColumnNames = columnNames :+ name

    val newRows = tabular.map(row => row :+ defaultVal)

    new Table(newColumnNames, newRows)
  }

  // 2.4.
  def merge(key: String, other: Table): Option[Table] = {

    val defaultSeparator = ""
    val concatSeparator = ";"

    def createNewColumnsFromTheTables(uniqueColumns: List[String], default: String): List[List[String]] = {
      uniqueColumns.map(columnName => {
        val columnIndex = columnNames.indexOf(columnName)
        val otherColumnIndex = other.getColumnNames.indexOf(columnName)

        val column = (columnIndex, otherColumnIndex) match {
          case (-1, otherIdx) => List.fill(tabular.length)(default) ++ other.getTabular.map(_(otherIdx))
          case (thisIdx, -1) => tabular.map(_(thisIdx)) ++ List.fill(other.getTabular.length)(default)
          case (thisIdx, otherIdx) => tabular.map(_(thisIdx)) ++ other.getTabular.map(_(otherIdx))
        }

        column
      })
    }

    def concatenateRows(row1: List[String], row2: List[String]) = {
      row1.zip(row2).map {
        case (x, y) =>
          if (x == y) x
          else if (x == defaultSeparator || y == defaultSeparator) x + y
          else x + concatSeparator + y
      }
    }

    if (columnNames.contains(key) && other.getColumnNames.contains(key)) {
      // get the indices of the key
      val keyIndex = columnNames.indexOf(key)

      // get the columns that are not duplicated
      val uniqueColumns = columnNames ++ other.getColumnNames.filterNot(columnNames.contains)

      val newColumns = createNewColumnsFromTheTables(uniqueColumns, defaultSeparator)

      val newRows = newColumns.transpose

      val concatenatedRows = newRows.groupBy(row => row(keyIndex)).values.map { rowsWithSameKey =>
        rowsWithSameKey.reduce((row1, row2) => {
          concatenateRows(row1, row2)
        })
      }.toList

      // create the new table
      Some(new Table(uniqueColumns, concatenatedRows))

    } else {
      None
    }
  }


}


object Table {
  // 1.2
  def apply(s: String): Table = {
    val lines = s.split("\n").toList
    val columnNames = lines.head.split(",").toList
    val rows = lines.tail.map(_.split(",").toList)
    // check if all rows have the same length as the column names
    if (!rows.forall(_.length == columnNames.length)) {
      // add default values to the rows
      val defaultVal = ""
      val newRows = rows.map(row => row ++ List.fill(columnNames.length - row.length)(defaultVal))
      new Table(columnNames, newRows)
    }
    else {
      new Table(columnNames, rows)
    }
  }
}
