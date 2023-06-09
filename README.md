 # TableQuery
 
## Grosu Gheorghe

### Description

This is a simple program that creates a table in a database and then inserts some values into it. After that, it queries the table and prints the result.

### Classes

#### Table

The Table class represents a tabular data structure with columns and rows. It has the following properties and methods:

- columnNames: Line: A list of column names in the table.
- tabular: List[List[String]]: A list of rows, where each row is a list of string values.

Methods:

- `getColumnNames`: Line: Returns the column names of the table.
- `getTabular`: List[List[String]]: Returns the rows of the table.
- `select(columns: Line)`: Option[Table]: Selects specified columns from the table and returns a new table with only the selected columns. Returns None if any of the specified columns are not present in the table.
- `filter(cond: FilterCond)`: Option[Table]: Filters rows from the table based on the given condition and returns a new table with the filtered rows. Returns None if the condition references columns that are not present in the table.
- `newCol(colName: String, col: Line)`: Option[Table]: Creates a new column in the table with the specified name and default value. Returns a new table with the additional column.
- `merge(key: String, other: Table)`: Option[Table]: Merges two tables based on a common key column. Returns a new table with the merged data. Returns None if the key column is not present in both tables.

#### Query
The Query trait represents a query that can be executed on a table. It has the following methods:
- `eval`: Option[Table]: Evaluates the query and returns the result as an Option[Table]. The result may be None if the query fails or encounters an error.

#### FilterCond
The FilterCond trait represents a filter condition used in the Filter operation. It defines methods for combining conditions (&& and ||) and evaluating a condition against a row in the table.

- `&&(other: FilterCond)`: FilterCond: Combines the current condition with another condition using logical AND.
- `||(other: FilterCond)`: FilterCond: Combines the current condition with another condition using logical OR.
- `eval(r: Row): Option[Boolean]`: Evaluates the condition against a row in the table and returns the result as an Option[Boolean]. Returns None if the condition references a column that is not present in the row.

#### Line
The Line type alias represents a list of strings, typically used for column names or row values.

#### Row
The Row type alias represents a mapping of column names to values, used for evaluating filter conditions against rows.

#### Usage
To use the table and query operations, you can create a Table object by providing column names and tabular data, either directly or by using the Table companion object. You can then perform various query operations on the table using the defined Query classes.