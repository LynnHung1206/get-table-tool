### mysql db terminal取得 table schema 小工具

環境需要 java21
請自行改動 [properties](./src/main/resources/application.properties)
然後包版
```
mvn clean package -DskipTests   
java -jar target/db_get_table_tool-0.0.1-SNAPSHOT.jar 
```
