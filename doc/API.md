### Authentication & Authorization:

We use JWT. The way this works, you submit your credentials to 
the server at the `/api/account/login` endpoint. 

Then you get an token that the server (but not you) can 
later decrypt and determine your privileges:

```
https://$SERVER/api/account/login

POST: 

{username: "joeuser", password: "somepass"}
 
response:
{
  "token" : "eyJhbGciOiJIUzUxMiJ9.....",
  "admin" : false
}
```

Use the token in REST calls by putting it in the HTTP headers:

`Authentication: eyJhbGciOiJIUzUxMiJ9.....`

Some REST endpoints don't require a token at all. It doesn't hurt to send it though, 
it just gets ignored. 

Some endpoints might require a token that has admin privileges. 
(You should know whether you have admin or not by the response 
of the `api/account/login` call.)

Token requirements follow the conventions below:
```
https://$SERVER_URL/api/...         : public API, no credentials needed.
https://$SERVER_URL/protected/...   : protected API, token required.
https://$SERVER_URL/admin/...       : admin endpoints, token with admin required.
```


## Requesting a connection
The general workflow would be:
* (Optional) Determine available topology & decide fixtures
* (Optional) Decide paths for pipes
* (Optional) Decide connection id
* Successfully hold resources
* Commit connection request

We will skip the optional steps for now and assume that we have a 
connection id, know that resources are available, and we the EROs 
for our paths are valid. 


###Holding resources: 

You will want to hold resources with the `protected/hold` POST call. 

If you receive an HTTP error response, you have probably made some mistake in 
JSON formatting or tried to use a previously existing connection id; check
the error message.

Otherwise, your request will first be validated in regards of its contents. The
server will check your schedule, requested resources, etc. Ideally you are not
requesting something that can't be held; in that case you will receive what
has been held. In case something is not acceptable, the `validity` field 
will be populated with `valid` set to false. 

Performing a second or further `protected/hold` call using the same connectionId
in the request will overwrite the previous one. You can use this to:
* clear out previously held resources before the hold expires
* progressively hold things while getting feedback from the server
(i.e. ensure you can hold the fixtures first, then perform pathfinding, 
then hold the pipes. this is how the web UI does things)

###Committing resources.

After you have successfully held all the resources you will need, perform a commit
operation. The server will make a further validation of what you have requested,
and if everything is OK it will return the new phase of your connection 
(should be 'RESERVED'). Otherwise, an HTTP error response will be returned.

```
POST https://$SERVER/protected/conn/commit
Request: 
ZXYW   // the connectionId

Response: 
RESERVED
```


## Hold examples

### Holding a "hairpin" on a single router, annotated:
```
POST https://$SERVER/protected/hold
{
  "connectionId" : "9FGR",          // mandatory, not-null not-empty
  "begin" : 1528996933,             // mandatory, start time, in seconds from epoch
  "end" : 1560532033,               // mandatory, end time
  "mode" : "AUTOMATIC",             // mandatory, 'AUTOMATIC' or 'MANUAL' 
  "pipes" : [ ],                    // null or empty in this case
  "junctions" : [ {
    "device" : "denv-cr5",          // only a single junction
  } ],
  "fixtures" : [ {                  // two fixtures, one per port / VLAN
    "junction" : "denv-cr5",        // should refer to an entry in the 'junctions' array
    "port" : "denv-cr5:10/1/11",    // a port in the topology
    "vlan" : 8,                     // integer vlan id
    "mbps" : 100,                   // use this value for both ingress and egress
  }, {
    "junction" : "denv-cr5",
    "port" : "denv-cr5:10/1/11",
    "vlan" : 9,
    "inMbps" : 100,                 // use in / outMbps if you want asymmetrical bandwidth
    "outMbps" : 200
  } ],
  "description" : "a description",  // mandatory not-null 
}
```

### Holding a simple point-to-point two-device connection:
```
POST https://$SERVER/protected/hold
{
  "connectionId" : "G272",
  "begin" : 1528998413,
  "end" : 1560533513,
  "mode" : "MANUAL",
  "junctions" : [ {             // two junctions this time,
    "device" : "pnwg-cr5",
  }, {
    "device" : "denv-cr5",
  } ],
  "pipes" : [ {                 // a single pipe in this case
    "a" : "denv-cr5",           // a and z point to the two different junction devices
    "z" : "pnwg-cr5",   
    "mbps" : null,              // if set, would override any az or zaMbps values
    "azMbps" : 100,             // a to z Mbps
    "zaMbps" : 200,             
    "ero" : [                   // the ERO you want the pipe to take
        "denv-cr5", 
        "denv-cr5:10/1/4", 
        "pnwg-cr5:10/1/6", 
        "pnwg-cr5" 
    ],
  } ],
  "fixtures" : [ {
    "junction" : "pnwg-cr5",
    "port" : "pnwg-cr5:10/1/12",
    "vlan" : 8,
    "inMbps" : 100,
    "outMbps" : 100,
  }, {
    "junction" : "denv-cr5",
    "port" : "denv-cr5:10/1/1",
    "vlan" : 8,
    "mbps" : 200,
  } ],
  "description" : "some description",
}

```


## Pipes, EROs, and pathfinding
Each pipe has an Explicit Route Object, or ERO. This is a sequence that goes:
```
A,              // starts with the A device URN,
A:1/1/1         // then a port URN outgoing from device A,
B:xe-1/2/0      // then a port URN incoming to the next device
B               // then the next device URN
B:xe-2/0/0

...             // and so on and so forth

Z:2/1/1         // penultimate is the port URN incoming to the Z device
Z               // and finally the Z device URN

```

* An ERO has a length of 1 modulo 3,
* An ERO is at least 4 elements long
* The first two elements must be:
  * A device,
  * A outgoing port
* The last two elements must be:
  * Z incoming port,
  * Z device
* Each other device on the path would add 3 more entries: 
  * incoming port, 
  * device
  * outgoing port

You can construct your own ERO, or, preferably, ask OSCARS to perform 
pathfinding for you. Do that with the `/api/pce/paths` endpoint

### Pathfinding example:

The pathfinding is performed for a specific A & Z junction, bandwidth per direction and 
schedule. It does not hold any resources; it is up to the caller to do that 
once they have evaluated the response. It returns several different alternate 
paths to choose from.

```
POST http://$SERVER/api/pce/paths
Reques
{
   "interval": {
      "beginning": 1528998413.928,      // your schedule's begin 
      "ending": 1560533513.928          //       & end times
   },
   "a": "denv-cr5",                     // the a and z junctions
   "z": "pnwg-cr5",                     //    of the pipe

   "azBw": 100,                         // the directional bandwidth a -> z
   "zaBw": 200,                         //     and z -> a

   "include": ["denv-cr5", "pnwg-cr5"], // optional: topology URNs the resulting 
                                        //     paths must follow, in order. 

   "exclude": []                        // optional, topology URNS the resulting 
                                        //    paths should not go over. unordered.
                                        //    forces the PCE to avoid these ports 
                                        //    devices
}
Response:
{
  "evaluated" : 27,                 // how many paths were evaluated by the PCE. info only
  "shortest" : {                    // the shortest path by metric cost
                                    //      this path only depends on A and Z 
                                    //      IMPORTANT: it might NOT fit the bandwidth 
                                    //      constraints, verify az/zaAvailable
    "cost" : 3.0,                   // the path's actual cost (by metric)
    "azEro" : [ {                   // the azERO; use that in your hold request's pipe ERO
      "urn" : "denv-cr5"            // you will need to fish out the contents of the "ero" fields though
    }, {
      "urn" : "denv-cr5:10/1/4"
    }, {
      "urn" : "pnwg-cr5:10/1/6"
    }, {
      "urn" : "pnwg-cr5"
    } ],
    "zaEro" : [ {                   // the zaERO is (for now) just the inverse of the azERO
      "urn" : "pnwg-cr5"            //      feel free to ignore it!
    }, {
      "urn" : "pnwg-cr5:10/1/6"
    }, {
      "urn" : "denv-cr5:10/1/4"
    }, {
      "urn" : "denv-cr5"
    } ],
    "azAvailable" : 10000,          // how much bandwidth is available over the 
    "zaAvailable" : 10000,          //      in each direction
    "azBaseline" : 10000,           // and how much bandwidth would be available
    "zaBaseline" : 10000            //      if no other reservations existed
  },
  "leastHops" : {                   // the shortest path by hop count
                                    //      this path only depends on A and Z 
                                    //      IMPORTANT: it might NOT fit the bandwidth 
                                    //      constraints, verify az/zaAvailable
    "cost" : 3.0,
    "azEro" : [ {
    ...                             // trimmed output for brevity
    } ],
    "zaEro" : [ {
    ...
    } ],
    "azAvailable" : 10000,
    "zaAvailable" : 10000,
    "azBaseline" : 10000,
    "zaBaseline" : 10000
  },
  "fits" : {                        // the shortest path that matches constraints
                                    //      the path depends on A, Z, bandwidth and schedule 
                                    //      if found, it WILL match or exceed the specified bw
    "cost" : 27.0,
    "azEro" : [ {
    ...
    } ],
    "zaEro" : [ {
    ...
    } ],
    "azAvailable" : 10000,
    "zaAvailable" : 10000,
    "azBaseline" : 10000,
    "zaBaseline" : 10000
  },
  "widestSum" : {                   // the widest possible path by sum of both directions
                                    //      this path only depends on A and Z 
                                    //      IMPORTANT: it might NOT fit the bandwidth 
                                    //      constraints, verify az/zaAvailable
    "cost" : 27.0,
    "azEro" : [ {
    ...
    } ],
    "zaEro" : [ {
    ...
    } ],
    "azAvailable" : 100000,
    "zaAvailable" : 100000,
    "azBaseline" : 100000,
    "zaBaseline" : 100000
  },
  "widestAZ" : {                    // the widest possible path in the A->Z direction
                                    //      this path only depends on A and Z 
                                    //      IMPORTANT: it might NOT fit the bandwidth 
                                    //      constraints, verify az/zaAvailable
    "cost" : 27.0,
    "azEro" : [ {
    ...
    } ],
    "zaEro" : [ {
    ...
    } ],
    "azAvailable" : 100000,
    "zaAvailable" : 100000,
    "azBaseline" : 100000,
    "zaBaseline" : 100000
  },
  "widestZA" : {                    // the widest possible path in the Z->A direction
                                    //      this path only depends on A and Z 
                                    //      IMPORTANT: it might NOT fit the bandwidth 
                                    //      constraints, verify az/zaAvailable
    "cost" : 27.0,
    "azEro" : [ {
    ...
    } ],
    "zaEro" : [ {
    ...
    } ],
    "azAvailable" : 100000,
    "zaAvailable" : 100000,
    "azBaseline" : 100000,
    "zaBaseline" : 100000
  }
}

```


 




## Fixtures, junctions and pipes
When committing, your held resources are verified one last time.

The model and restrictions are:

* A junction represents a device (router / switch). 
  * One or more junctions are required. For a simple point-to-point connection 
  you'll need either one junction (for a same-device 'hairpin'), or two.
* A fixture represents a customer-facing endpoint, i.e. a port / VLAN pair.
  * A total of at least two fixtures is required.
  * Each fixture belongs to a junction 
* A pipe represents a tunnel in the network that interconnects junctions
  * A single-junction service doesn't need (and shouldn't have) any pipes,
  * For multiple junctions, every junction must be reachable from any other 
  junction. i.e.:
  ```
     A ---- B  : ok!

     A ---- B ---- C : ok!

     A ---- B      C ---- D : not acceptable

     A ---- B ---- C      D : not acceptable

     +-------------+
     |             |
     A ---- B      C      D : ok
     |                    |
     +--------------------+

  ```




## Holding resources, in detail
Once resources have been held, they are (tentatively) yours until the hold
expires (which is either 15 minutes after your hold, or the start time, whichever
one is sooner). You can only commit after you have a successful hold and 
before your hold has expired.

You will receive the hold expiration time in the `heldUntil` field in the hold response.

If needed you can extend the expiration time. You will receive the new time in the response:
```
GET https://$SERVER/protected/extend_hold/WXYZ
Response: 

1528996933.000000000

```

To clear out held resources, submit a hold with empty junctions / fixtures / pipes:
```
POST https://$SERVER/protected/hold
Request: 
{
  "connectionId" : "WXYZ",
  "begin" : 1528993045,
  "end" : 1560528145,
  "mode" : "MANUAL",
  "description" : "",
  "fixtures": []
  "junctions": []
  "pipes": []
}
```


## Hold Request Validation
Example of invalid request:
```
POST https://$SERVER/protected/hold
Request: 
{
  "connectionId" : "WXYZ",
  "begin" : 1528993045,
  "end" : 1560528145,
  "mode" : "MANUAL",
  "description" : "",
  "fixtures": [
    {
        "junction" : "pnwg-cr5",
        "port" : "pnwg-cr5:10/1/12",
        "vlan" : 8,
        "mbps" : 10000000,
    }
  ]
}

Response:
{
  "connectionId" : "WXYZ",
  "begin" : 1528993045,
  "end" : 1560528145,
  "heldUntil" : 1528996045,
  "username" : "joeuser",
  "phase" : "HELD",
  "mode" : "MANUAL",
  "state" : "WAITING",
  "pipes" : [ ],
  "junctions" : [ ],
  "fixtures" : [ ],
  "tags" : [ ],
  "description" : "",
  "validity" : {
     "valid": false,
     "message": "insufficient bandwidth on pnwg-cr5:10/1/12"
  }
}
```

In general the requirements for validity are:
* Fixtures: 
  * mandatory fields are non-null and with sane values
    * `connectionId` is a non-empty string, 
      * either no connection exists with that id, or
      * it points to a connection in HELD phase to be overwritten
    * `begin` and `end` times are in the future, in correct order,
    * `description` is non-null
    * `mode` is 'AUTOMATIC' or 'MANUAL'
  * junctions and ports must refer to correct topology URNs
  * VLANs and bandwidths must be available for all fixtures and pipes


## Determine connection id
Recommended: ask OSCARS to generate a connection id. You could provide one of your choice, 
but if you were to reuse an existing one things won't work. 
```
GET https://$SERVER/protected/conn/generateId
Response:
WXYZ
```



## Gathering topology information

You will likely want to try to hold resources that are actually available for use. 

You can ask OSCARS what is available to hold  over
a certain time interval. The server will give you back the entire 
topology, subtracting from the baseline resources anything that is 
either reserved or tentatively held (even by you!).

The web UI primarily uses this to decide what VLANs and bandwidths
can be reserved on a particular port. 


```
GET https://$SERVER/api/topo/available
Request:
{
    "beginning":1528998413.928,
    "ending":1560533513.928
}
Response:

{
  "bost-cr5:10/2/1" : {                 // keyed by port URN 
    "vlanRanges" : [ {                  //  if this is not empty, you can hold it as a fixture
      "floor" : 2,                      //  will contain all the vlans available 
      "ceiling" : 4094
    } ],
    "vlanExpression" : "2:4094",        // a human-readable expression of the vlanRanges
    "ingressBandwidth" : 10000,         // bandwidth available for ingress..
    "egressBandwidth" : 10000           //     and egress
  },
  "slac-mr2:xe-1/1/0" : {
    "vlanRanges" : [ ],                 // no vlan ranges so it's a backbone link
    "vlanExpression" : "",
    "ingressBandwidth" : 10000,
    "egressBandwidth" : 10000
  },
  "lbl-mr2:xe-8/2/0" : {
    "vlanRanges" : [ {
      "floor" : 2,
      "ceiling" : 4094
    } ],
    "vlanExpression" : "2:4094",
    "ingressBandwidth" : 10000,
    "egressBandwidth" : 10000
  },
  .....
  ...
}
```

You can get similar information but *without* the reservations subtracted with a 
`GET https://$SERVER/api/topo/baseline` if you want.

If you want to do your own pathfinding or other graph-related operation, 
you can also retrieve topology adjacencies. Combine with the 
availability view above to decide your own EROs, visualize the topology, etc.




```
GET https://$SERVER/api/topo/adjacencies

Response:
[ {
  "a" : "eqx-ash-cr5",
  "b" : "eqx-ash-cr5:2/1/1",
  "y" : "eqx-chi-cr5:2/1/1",
  "z" : "eqx-chi-cr5"
}, {
  "a" : "ornl-rt4",
  "b" : "ornl-rt4:xe-1/0/0",
  "y" : "ornl-cr5:10/1/1",
  "z" : "ornl-cr5"
}, 
...
]

```