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
2. Docker Build, Push & Run
   ````
   docker build -t alokkusingh/statement-parser:latest -t alokkusingh/statement-parser:2.2.0 --build-arg JAR_FILE=target/spring-batch-pdf-parser-1.0.3-SNAPSHOT.jar .
   ````
   ````
   docker push alokkusingh/statement-parser:latest
   ````
   ````
   docker push alokkusingh/statement-parser:2.2.0
   ````
   ````
   docker run -d -v /home/alok/data/git/BankStatements:/Users/aloksingh/BankStatements:rw,Z -p 8081:8081 --rm --name statement-parser alokkusingh/statement-parser
   ````
   
### Manual commands
````
docker run -it --entrypoint /bin/bash -v /home/alok/data/git/BankStatements:/Users/aloksingh/BankStatements:rw,Z -p 8081:8081 --rm --name statement-parser alokkusingh/statement-parser
````
````
java -Djava.security.egd=file:/dev/urandom -Dspring.profiles.active=prod -Dspring.datasource.url=jdbc:mysql://192.168.0.200:32306/home-stack -Dspring.datasource.hikari.minimum-idle=5 -Dspring.datasource.hikari.connection-timeout=20000 -Dspring.datasource.hikari.maximum-pool-size=10 -Dspring.datasource.hikari.idle-timeout=10000 -Dpring.datasource.hikari.max-lifetime=1000 -Dspring.datasource.hikari.auto-commit=true -jar /opt/app.jar
````
````
docker run -v /home/alok/data/git/BankStatements:/Users/aloksingh/BankStatements:rw,Z -p 8081:8081 --rm --name statement-parser alokkusingh/statement-parser --java.security.egd=file:/dev/urandom --spring.profiles.active=prod --spring.datasource.url=jdbc:mysql://192.168.0.200:32306/home-stack --spring.datasource.hikari.minimum-idle=5 --spring.datasource.hikari.connection-timeout=20000 --spring.datasource.hikari.maximum-pool-size=10 --spring.datasource.hikari.idle-timeout=10000 --pring.datasource.hikari.max-lifetime=1000 --spring.datasource.hikari.auto-commit=true
````

## MQTT Commands
### Root Certificate - for client signer and domain signer
````
openssl genrsa -des3 -out mqtt-signer-ca.key 2048
````
````
openssl req -x509 -new -nodes -key mqtt-signer-ca.key -sha256 -days 365 -out mqtt-signer-ca.crt -subj /C=IN/ST=KA/L=Bengalury/O=Home/CN=alok-signer
````
#### Client Cert - alok
````
openssl genrsa -out mqtt.client.alok.key 2048
````
````
openssl req -new -sha256 -key mqtt.client.alok.key -subj /C=IN/ST=KA/L=S=Bengaluru/O=Home/CN=alok -out mqtt.client.alok.csr
````
````
openssl x509 -req -in mqtt.client.alok.csr -CA mqtt-signer-ca.crt -CAkey mqtt-signer-ca.key -CAcreateserial -out mqtt.client.alok.crt -days 365 -sha256
````

####  Server Domain Cert - localhost
````
openssl genrsa -out server.key 2048
````
````
openssl req -new -sha256 -out server.csr -key server.key -subj /C=IN/ST=KA/L=S=Bengaluru/O=Home/CN=localhost
````
````
openssl x509 -req -in server.csr -CA mqtt-signer-ca.crt -CAkey mqtt-signer-ca.key -CAcreateserial -out server.crt -days 360 -sha256
````

#### Add client alok cert to PKCS 12 keystore - then it is imported in JKS using KeyStore Explorer
````
openssl pkcs12 -export -out mqtt.client.alok.p12 -name "alok" -inkey mqtt.client.alok.key -in mqtt.client.alok.crt
````

#### Start Mosquito Broker
````
/opt/homebrew/opt/mosquitto/sbin/mosquitto -c /opt/homebrew/etc/mosquitto/mosquitto.conf
````

#### Publish using alok cert
````
mosquitto_pub --cafile mqtt-signer-ca.crt --cert mqtt.client.alok.crt --key mqtt.client.alok.key -d -h localhost -p 8883 -t test -m "Hello" --tls-version tlsv1.2 --debug
````

#### Client Cert - rachna
````
openssl genrsa -out mqtt.client.rachna.key 2048
````
````
openssl req -new -sha256 -key mqtt.client.rachna.key -subj /C=IN/ST=KA/L=S=Bengaluru/O=Home/CN=rachna -out mqtt.client.rachna.csr
````
````
openssl x509 -req -in mqtt.client.rachna.csr -CA mqtt-signer-ca.crt -CAkey mqtt-signer-ca.key -CAcreateserial -out mqtt.client.rachna.crt -days 365 -sha256
````

#### Publish/Subscribe using rachna cert
````
mosquitto_sub --cafile mqtt-signer-ca.crt --cert mqtt.client.rachna.crt --key mqtt.client.rachna.key -d -h localhost -p 8883 -t home/stack/stmt-res --tls-version tlsv1.2 --debug
````
````
mosquitto_pub --cafile mqtt-signer-ca.crt --cert mqtt.client.rachna.crt --key mqtt.client.rachna.key -d -h localhost -p 8883 -t home/stack/stmt-req -m "Hello" --tls-version tlsv1.2 --debug
````

#### Request Topic
````
home/stack/stmt-req
````
#### Sample Request Payload
````
{
"correlationId": "sdcsd1234",
"httpMethod": "GET",
"uri": "/fin/expense?yearMonth=current_month",
"body": ""
}
````