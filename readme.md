# Overview
This is a simple DNS forwarder that forwards DNS requests to a specified DNS server (in this case Google DNS servers). This is part of the code challenge to [Build Your Own DNS Forwarder](https://codingchallenges.fyi/challenges/challenge-dns-forwarder/) by John Crickett:

> A DNS Forwarder is a nameserver used to resolve DNS queries instead of directly using the authoritative nameserver chain. Often they are used to sit on the edge of a local area network and provide DNS resolution to the computers on the network, reducing external traffic and speeding up external access by serving the answer from a local cache.

> ### The Challenge - Building A DNS Forwarder
> In this challenge we’re going build a simple DNS Forwarder that can resolve the IP address for a host either from it’s local cache, or by forwarding the request to an authoritative nameserver.

## Setup
To run the app locally, you will need Java 17 and Maven 3.x installed.  To get started, clone the repository and navigate to the project directory
```bash
git clone https://github.com/zjcz/dns-forwarder.git
cd dns-forwarder
```
Next, build and run the project.  On Linux/macOS:
```bash
./run.sh
```

On Windows (untested, sorry!):
```
run.bat
```

## Running the app
The app will listen on port 1053.  This can be changed by passing a port number as a startup argument.

```bash
./run.sh 1053
```

To call the app, use the `dig` command.  For example, to query the DNS server for the IP address of www.google.com, use the following command:

```bash
dig @127.0.0.1 -p 1053 www.google.com
```

The response should look similar to this:

```bash     
;; Warning: query response not set

; <<>> DiG 9.10.6 <<>> @127.0.0.1 -p 1053 www.google.com
; (1 server found)
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 7275
;; flags:; QUERY: 1, ANSWER: 1, AUTHORITY: 0, ADDITIONAL: 0

;; QUESTION SECTION:
;www.google.com.                        IN      A

;; ANSWER SECTION:
www.google.com.         130     IN      A       216.58.201.100

;; Query time: 34 msec
;; SERVER: 127.0.0.1#1053(127.0.0.1)
;; WHEN: Tue Jun 04 18:12:41 BST 2024
;; MSG SIZE  rcvd: 62
```

## Progress
All steps are complete but unfortunately the DNS server doesn't seem to work with a domain other than www.google.com.  When requesting any other domain the Google DNS server always returns an RCODE error response of `2 - Server failure - The name server was unable to process this query due to a problem with the name server.`  I'm unsure why this is.  If it was due to the app sending an invalid message I would expect to receive an RCODE of `1 - Format error...`  

When I have more time I will investigate further.  

## References
I got a bit stuck unpacking the byte array messages.  Luckily I came across the articles [DNS response in Java](https://levelup.gitconnected.com/dns-response-in-java-a6298e3cc7d9) and [DNS requests in Java](https://levelup.gitconnected.com/dns-request-and-response-in-java-acbd51ad3467) by [Andrei Popa](https://medium.com/@junkypic) which pointed me in the right direction.

