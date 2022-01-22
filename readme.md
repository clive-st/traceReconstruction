## Reconstrucción de traza

Cada aplicación en un ambiente de microservicios genera una línea log por cada request HTTP que atiende. 

El formato de dicha línea es el siguiente:

`[start-timestamp] [end-timestamp] [trace ID] [service-name] [caller-span]- >[span]`


Trace ID es un string random que se pasa a lo largo de toda una (1) interacción entre servicios. 
El primer servicio que se llama desde afuera genera el string y lo pasa a lo largo de las demás invocaciones a servicios. Los servicios invocados toman la identificación de la traza, de un header HTTP por ejemplo, y lo pasan a los servicios que llaman.

El span ID se genera por cada request. Cuando un servicio llama a otro éste le pasa su span ID al servicio llamado. El servicio llamado genera su propio span ID y usando el span ID del llamador loguea una línea que permite conectar los requests.

#### Con un ejemplo:

```2016-10-20 12:43:34.000 2016-10-20 12:43:35.000 trace1 back-end-3 ac->ad 2016-10-20 12:43:33.000 2016-10-20 12:43:36.000 trace1 back-end-1 aa->ac 2016-10-20 12:43:38.000 2016-10-20 12:43:40.000 trace1 back-end-2 aa->ab 2016-10-20 12:43:32.000 2016-10-20 12:43:42.000 trace1 front-end null->aa```


##### La secuencia se lee como una pila:

```
t0 -> Un servicio “front-end” recibe una llamada identificada con un traceID = “trace1”. A su vez, no hay spanID de su “llamador” (null) pero sí crea su propio spanID con el valor “aa”.
t1 -> “front-end” llama a “back-end-1” quien asigna el span “ac”.
t2 -> “back-end-1” llama a “back-end-3” quien asigna el span “ad” al request recibido.
t3 -> Por último, “front-end” llama a “back-end-2” quien identifica este request con el span “ab”
``` 


Como puede verse, la identificación de la traza “trace1” está presente durante todos los requests involucrados.
Las líneas de log se escriben cuando los requests terminan de manera que las mismas no están en el orden de llamada si no en el orden de terminación. Es más, como el mecanismo de concentración de logs es asincrónico no hay garantías de orden. Más allá de eso dado que los relojes están sincronizados se espera que esto se refleje en los timestamps.
Timestamps están en UTC.

La traza anterior podría estar representada de la siguiente manera:

```
{“trace: “trace1”,
“root”: {
“service”: “front-end”,
“start”: “2016-10-20 12:43:32.000”, “end”: “2016-10-20 12:43:42.000”, “calls”: [
{“service”: “back-end-1”,
“start”: “2016-10-20 12:43:33.000”,
“end”: “2016-10-20 12:43:36.000”,
“calls”: [
{“service”: “back-end-3”,
“start”: “2016-10-20 12:43:34.000”,
“end”: “2016-10-20 12:43:35.000”}]}, {“service”, “back-end-2”,
“start”: “2016-10-20 12:43:38.000”, “end”: “2016-10-20 12:43:40.000”} ]}}
```  

La tarea consiste en producir estructuras JSON como la anterior por cada traza identificada en el archivo de log.





## How to start the application from the command line:

### require java  1.8 (sdk use java 8.0.272-amzn)

### reading a file

```
sbt "runMain traza.Main -i log.txt"
```

### reading the stdin:

```
sbt "runMain traza.Main"
```

## Libraries

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

