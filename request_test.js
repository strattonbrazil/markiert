#!/usr/bin/env node

const http = require('http');

const options = {
    hostname: 'localhost',
    port: 8080,
    path: '/id',
    method: 'GET'
}

const totalRequests = 2000;

let responses = {};

for (var i = 0; i < totalRequests; i++) {
    const req = http.request(options, res => {
        res.on('data', data => {
            
            const id = BigInt(JSON.parse(data).id);
            let asBinary = id.toString(2);
            while (asBinary.length < 64) {
                asBinary = "0" + asBinary;
            }

            const timestamp = parseInt(asBinary.slice(1, 42), 2);
            const dataCenterId = parseInt(asBinary.slice(42, 47), 2);
            const machineId = parseInt(asBinary.slice(47, 52), 2);
            const sequence = parseInt(asBinary.slice(53, 65), 2);

            if (!(timestamp in responses)) {
                responses[timestamp] = [];
            }
            responses[timestamp].push(sequence)

            // process.stdout.write(asBinary + "\n");
            process.stdout.write(timestamp + " " + dataCenterId + " " + machineId + " " + sequence + "\n");
            // process.stdout.write("--\n");
        });
    });
    req.end();
}

