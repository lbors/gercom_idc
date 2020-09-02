# Lattice Monitoring Framework
This repository contains the source code of the Lattice Monitoring Framework.

The main components of the framework are:
- The Monitoring Controller
- Data Sources
- Data Consumers


### Build
The provided `build.xml` (under `source/`) can be used both for compiling the source code and generating the deployable jar files.

```sh
$ cd source/
$ ant dist
```

The above command generates three different jar binary files in the jars directory:
- `monitoring-bin-controller.jar` containing all the classes and dependencies related to the controller.
- `monitoring-bin-core.jar` containing a subset of classes and dependencies that can be used for instantiating Data Sources and Data Consumers.
- `monitoring-bin-core-all.jar` adds additional libraries and dependencies to the above bin-core jar.

and also a jar containing the source code
- `monitoring-src.jar`

### Installation
As soon as the Build process is completed, a controller instance (e.g, ZMQController) can be started as follows:
```sh
$ cd jars/
$ java -cp monitoring-bin-controller.jar mon.lattice.control.controller.json.ZMQController controller.properties
```
The `controller.properties` file contains the configuration settings for the controller (example files are under `conf/`)

### Configuration
```
info.localport = 6699
``` 
is the local port used by the Controller when connecting to the Information Plane. Other Lattice entities (e.g., Data Sources) will remotely connect to this port once started.

```
restconsole.localport = 6666
```
is the port where the controller will listen for HTTP control requests.

```
deployment.enabled = true
```
Can be set either to `true` or `false` and enables/disables respectively the automated Data Sources deployment functionality to a remote host (current implementation is based on SSH with public key authentication)

```
deployment.localJarPath = /Users/lattice
deployment.jarFileName = monitoring-bin-core.jar
deployment.remoteJarPath = /tmp
deployment.ds.className = mon.lattice.appl.datasources.ZMQDataSourceDaemon
deployment.dc.className = mon.lattice.appl.dataconsumers.ZMQControllableDataConsumerDaemon
```
The above settings allow to specify (in order):
- the path where the jar (to be used for the Data Sources / Consumers automated remote deployment) is located
- the file name of the above jar file
- the path where the jar will be copied on the remote machine where the Data Source is being deployed
- the class name of the Data Source to be started (it must exist in the specified jar)
- the class name of the Data Consumer to be started (it must exist in the specified jar)
