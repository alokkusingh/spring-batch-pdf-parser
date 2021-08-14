# Bank Account Statement Reader
Spring Boot Batch Processor

## Functionality
- Reads downloaded bank PDF statements (with password/without password protection) or imported CSV files from bank site
- Parses it (based on plugable prsing logic)
- Process the records (transaction categorization, amount extraction)
- Writes to H2 DB
- Finaly export to csv format order by transaction date to be imported to Excel or Google Sheet

### Supported Bank Statemetnts
1. Citi Bank Saving Account 
2. Kotak Mahindra Bank Saving Account 

### How to run
java -jar target/spring-batch-pdf-parser-0.0.2-SNAPSHOT.jar --file.path.base.dir=/home/alok/data/git/BankStatements
