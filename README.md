# Bank Account Statement Reader
Spring Boot Batch Processor

## Functionality
- Reads downloaded bank PDF statements (with password/without password protection) or imported CSV files from bank site
- Parses it (based on plugable parsing logic)
- Process the records (transaction categorization, amount extraction)
- Writes to H2 DB
- Finaly export to csv format order by transaction date to be imported to Excel or Google Sheet

### Supported Bank Statemetnts
1. Citi Bank Saving Account 
2. Kotak Mahindra Bank Saving Account 

### How to run
````
java -jar target/spring-batch-pdf-parser-0.0.2-SNAPSHOT.jar --file.path.base.dir=/home/alok/data/git/BankStatements
````

### Enhancements - 18 Feb 2022
#### Current Status - as on 18 Feb 2022
1. Parses all files on startup
2. File polling?
3. Manual pulling statements from private GitHub repo
4. Manual pushing generated report to private GitHub repo
#### Phases
1. Stop automatic file polling - if any
2. Expose API to upload file to be parsed
3. Expose API to download CSV report
4. Implement ReactJS UI 
   
   4.1 To upload the bank statement
   
   4.2 To download CSV report
   
   4.3 Tp see the detailed reports

#### Build
1. Maven Package
   ````
   mvn clean package
   ````
2. Docker Build & Run
   ````
   docker build -t alokkusingh/statement-parser:latest -t alokkusingh/statement-parser:1.0.0 --build-arg JAR_FILE=target/spring-batch-pdf-parser-0.0.2-SNAPSHOT.jar .
   ````
   ````
   docker run -d -v /home/alok/data/git/BankStatements:/Users/aloksingh/BankStatements:rw,Z -p 8081:8081 --rm --name statement-parser alokkusingh/statement-parser
   ````