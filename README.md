# TCP-Based DNS Simulator
###### [open this md file at gist](https://gist.github.com/hmny/25d74bde648e8eb9ba52fd64c6d4ed6f)

## How to use:
- Run the Server-Side code.
- Do the queries using the Client-Side code.
- Use `q` to quit from the Client-Side. The Server-Side part shuts down using `exit` command (on the server-side cl).

## How it works:
- New valid queries will be cached in `DNS_MAPPING.txt` located in `root` of the project. If the file doesn't exit it will be generated.
- Each user has a unique ID since we would have more than one doing the queries.
- `DNSServer` class creates new `ServerSocket` object to listen on a specific port `(5001)`. 
- In `dnsQuery` class, `accept` method waits until a client starts up and requests a connection on the host and port of this server. (Let's assume that you ran the server program KnockKnockServer on the computer named knockknockserver.example.com.) In this example, the server is running on the port number specified in previous step. When a connection is requested and successfully established, the accept method returns a new `Socket` object which is bound to the same local port and has its remote address and remote port set to that of the client. The server can communicate with the client over this new `Socket` and continue to listen for client connection requests on the original `ServerSocket`.
- `dnsQuery` can handle multiple sockets' queries since each query runs on a seperate thread and each thread has its own socket.

## Methods Used:
### ipLookup:
- Using a host address provided by client, returns an output in `<hostName>:<hostAddress>` or `Host not found` format. If the output has been cached in cache file, it will use the cached output, if it's not cached and it's a valid output, it will return the result and cache it into the file.
- In case the result hasn't been cached, `getHostName()` will be used to retrieve the `<hostName>` and `getHostAddress()` will be used for the `<hostAddress>` part.

### cacheGenerator:
- Simple File Writer, generated the cache file.

### cacheReader:
- Simple File Reader, finds a match (using Regex) based on the host name provided by user. Returns the host or `null` in case it doesn't find a match.
- In case we find a match we won't do the DNS query again and use the value in the cache file.

### sample output (single user - empty cache):
- client-side:
```
Type in a domain name to query, or 'q' to quit:
twitter.com
Received: 'Root DNS: twitter.com:199.16.156.70'

Type in a domain name to query, or 'q' to quit:
twitter.com
Received: 'Local DNS: twitter.com:199.16.156.70'

Type in a domain name to query, or 'q' to quit:
www.google.com
Received: 'Root DNS: www.google.com:172.217.5.4'

Type in a domain name to query, or 'q' to quit:
www.google.com
Received: 'Local DNS: www.google.com:172.217.5.4'

Type in a domain name to query, or 'q' to quit:
q

Process finished with exit code 0
```

- server-side:
```
Server is listening...
>> User 1 connected:
- Cache File is generated.
Server to user 1: Root DNS: twitter.com:199.16.156.70
Server to user 1: Local DNS: twitter.com:199.16.156.70
Server to user 1: Root DNS: www.google.com:172.217.5.4
Server to user 1: Local DNS: www.google.com:172.217.5.4
>> User 1 disconnected.
```

### sample output (multiple user):
- user#1:
```
Type in a domain name to query, or 'q' to quit:
twitter.com
Received: 'Root DNS: twitter.com:199.16.156.102'

Type in a domain name to query, or 'q' to quit:
twitter.com
Received: 'Local DNS: twitter.com:199.16.156.102'

Type in a domain name to query, or 'q' to quit:
q

Process finished with exit code 0
```

- user#2:
```
Type in a domain name to query, or 'q' to quit:
facebook.com
Received: 'Root DNS: facebook.com:69.171.230.68'

Type in a domain name to query, or 'q' to quit:
q

Process finished with exit code 0
```

- server:
```
Server is listening...
>> User 1 connected:
>> User 2 connected:
- Cache File is generated.
Server to user 1: Root DNS: twitter.com:199.16.156.102
Server to user 2: Root DNS: facebook.com:69.171.230.68
Server to user 1: Local DNS: twitter.com:199.16.156.102
>> User 2 disconnected.
>> User 1 disconnected.
```