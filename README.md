# Ticket Server #

This is inspired by twitter snowflake (https://github.com/twitter/snowflake)

## Functions ##

* continuously generate unique positive long integers, e.g 63-bits
* have one or multiple instances running simutaneously.
* no co-ordination between instances of the server on matter of ticket 
  generation.
* generated number must be k-sortable.
* provide a reasonable external interface to call on multiple language, at least
  Java, Python, Ruby and PHP.
* generate unique numbers upon multiple requests even at same clock.
* service call is less than 2ms.


## Implementation So Far ##

* a http call is implemented to get ticket:

    GET /ticket
    
* zookeeper is used to co-ordinate server during startup time, not on number generation.


