package com.lynn.db_get_table_tool;

import com.lynn.db_get_table_tool.service.GetTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
@RequiredArgsConstructor
public class DbGetTableToolApplication implements CommandLineRunner {

  private final GetTableService getTableService;
  public static void main(String[] args) {
    SpringApplication.run(DbGetTableToolApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    Scanner sc = new Scanner(System.in, "UTF-8");
    System.out.println("請輸入 table 名稱（輸入 q 離開）：");
    while (true) {
      System.out.print("> ");
      String input = sc.nextLine().trim().toLowerCase();
      if ("q".equals(input)) {
        break;
      }
      try {
        getTableService.getTable(input);
      }catch (Exception e) {
        System.err.println("Table bot found: " + e.getMessage());
      }
    }
    sc.close();
    System.out.println("Bye!");
    System.exit(0);
  }
}
