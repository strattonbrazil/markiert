# markiert
A unique-ID generator using Twitter's [Snowflake implementation](https://blog.twitter.com/engineering/en_us/a/2010/announcing-snowflake). 

Use this generator when IDs must:
* be generated on fault-tolerant system
* fit into a 64-bit number
* be sortable

# Schema of 64-bit number
* 1 bit reserved
* 41 bits timestamp (in milliseconds from reference)
* 5 bits datacenter id
* 5 bits machine id
* 12 bits sequence number

# Configuration

Most app management systems such as Kubernetes provide ways to set environment variables dynamically. 

The generator uses four environment variables:
* `MARKIERT_HTTP_PORT` - the HTTP port to retrieve the IDs (defaults to 8080)
* `MARKIERT_DATACENTER_ID` - the integer ID of the datacenter (defaults to 1)
* `MARKIERT_MACHINE_ID` - the integer ID of the machine (defaults to 1)
* `MARKIERT_EPOCH_OFFSET` - the UNIX epoch value the timestamp will reference (defaults to 1288834974657, the original Snowflake epoch)
    * for example, an ID generated 3 milliseconds after the `MARKIERT_EPOCH_OFFSET` will have a timestamp of 3

I recommend choosing a `MARKIERT_EPOCH_OFFSET` relative to the current time and use that across deployments for the life of the ID space. 

Here's an example one-liner to print the current epoch time: 
```
python -c "import time; print(int(time.time()*1000))"
```

# API Usage
*Markiert* provides a single REST endpoint for fetching an ID. 
```
http://<IP_ADDRESS:PORT>/id
```
This endpoint provides a JSON-formatted response, which includes the 64-bit number in a string. For example: 
```
{"id":"6816559434522038272"}
```

# Building
```
mvn compile package
```

# Unit Testing
```
mvn test
```

# Integration Testing
There's a simple integration test for verifying unique and sequencial ids. 

Start the packaged jar file:
```
java --jar target/markiert-1.0-SNAPSHOT-fat.jar
```

And then launch the script in a separate terminal:
```
nodejs ./request_test.js
```
