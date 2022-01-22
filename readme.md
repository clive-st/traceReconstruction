## How to start the application from the command line:


### reading a file

```
sbt "runMain traza.Main -i log.txt"
```

### reading the stdin:

```
sbt "runMain traza.Main"
```

## Lirary

### cats:
used to compose Map( String -> List(xxx)) with another Map( String -> List(xxx))

### json4s:
to generate the json. note: the array is present as [] unlike in the
example, as the document say:
<< ***podría*** estar representada de la siguiente manera>>

### scopt:
to read the input.

### Monix:
Used for the multi thread implementation ( Observable pattern).

## Report:
on stderr, show nbr of errors parsing the line,
error incomplete trace, complete trace and how many times
we remove a trace for timeout.



## Timeout
implemented, use readLine to check the timeout as we need to remove
data from it only if we use it otherwise, those trace can stay here
forever without any impact for us.

## Multi thread

implemented only as experiment, using the test file,
you can pass the parametrs "-x y" to use it. Is more about concept
than performance, using Monix observable pattern to consume any source,
transform it (flatMap) into an empty observable if we do not have a new
trace of an observable based on a Future{ recontructtrace... }
seem to works. This version do not generate any reports.....

```
sbt "runMain traza.Main" -x y
```

