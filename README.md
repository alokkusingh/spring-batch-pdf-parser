# Bank Account Statement Reader
Spring Boot Batch Processor

The app:
- Reades downloaded bank PDF statements (with password/without password protection) or imported CSV files from bank site
- Parse it (based on plugable prsing logic)
- Process the records (transaction categorization, amount extraction)
- Writes to H2 DB
- Finaly export to csv format order by transaction date to be imported to Excel or Google Sheet
