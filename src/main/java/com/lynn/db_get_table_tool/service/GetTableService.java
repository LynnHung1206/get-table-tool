package com.lynn.db_get_table_tool.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class GetTableService {

  private static final String SCHEMA_COND = "TABLE_SCHEMA = DATABASE()";
  private static final String DASH_REPEAT = "-".repeat(100);

  private final JdbcTemplate jdbc;

  public void getTable(String tableName) {
    var columns = queryColumns(tableName);
    this.printGrid(
        "\nTable: " + tableName,
        new String[]{"Column Name", "Data Type", "Nullable", "Comment"},
        columns,
        row -> new Object[]{
            row.get("COLUMN_NAME"),
            row.get("DATA_TYPE"),
            row.get("IS_NULLABLE"),
            row.get("COLUMN_COMMENT")
        }
    );

    var indexes = queryIndexes(tableName);
    if (!indexes.isEmpty()) {
      this.printGrid(
          "\nIndexes",
          new String[]{"Index Name", "Columns", "Type"},
          indexes,
          row -> new Object[]{
              row.get("INDEX_NAME"),
              row.get("COLUMNS"),
              ((Number) row.get("NON_UNIQUE")).intValue() == 0 ? "UNIQUE" : "INDEX"
          }
      );
    }

    var fks = queryForeignKeys(tableName);
    if (!fks.isEmpty()) {
      this.printGrid(
          "\nForeign Keys",
          new String[]{"Constraint Name", "Column", "Referenced Table", "Referenced Column"},
          fks,
          row -> new Object[]{
              row.get("CONSTRAINT_NAME"),
              row.get("COLUMN_NAME"),
              row.get("REFERENCED_TABLE_NAME"),
              row.get("REFERENCED_COLUMN_NAME")
          }
      );
    }
  }

  private List<Map<String, Object>> queryColumns(String table) {
    String sql = """
        SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT, IS_NULLABLE, COLUMN_KEY
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE %s AND TABLE_NAME = ?
        ORDER BY ORDINAL_POSITION
        """.formatted(SCHEMA_COND);
    var list = jdbc.queryForList(sql, table);
    if (CollectionUtils.isEmpty(list)) {
      throw new IllegalArgumentException("Table not found: " + table);
    }
    return list;
  }

  private List<Map<String, Object>> queryIndexes(String table) {
    String sql = """
        SELECT INDEX_NAME, GROUP_CONCAT(COLUMN_NAME) AS COLUMNS, NON_UNIQUE
        FROM INFORMATION_SCHEMA.STATISTICS
        WHERE %s AND TABLE_NAME = ? AND INDEX_NAME != 'PRIMARY'
        GROUP BY INDEX_NAME, NON_UNIQUE
        """.formatted(SCHEMA_COND);
    return jdbc.queryForList(sql, table);
  }

  private List<Map<String, Object>> queryForeignKeys(String table) {
    String sql = """
        SELECT CONSTRAINT_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
        FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
        WHERE %s AND TABLE_NAME = ? AND REFERENCED_TABLE_NAME IS NOT NULL
        """.formatted(SCHEMA_COND);
    return jdbc.queryForList(sql, table);
  }

  private void printGrid(
      String title,
      String[] headers,
      List<Map<String, Object>> rows,
      Function<Map<String, Object>, Object[]> mapper
  ) {
    System.out.println(title);
    System.out.println(DASH_REPEAT);
    String fmt = this.buildFormat(headers.length);
    System.out.printf(fmt, (Object[]) headers);
    System.out.println(DASH_REPEAT);
    for (var row : rows) {
      System.out.printf(fmt, mapper.apply(row));
    }
    System.out.println(DASH_REPEAT);
  }

  private String buildFormat(int cols) {
    return "%-20s ".repeat(cols).trim() + "%n";
  }
}


