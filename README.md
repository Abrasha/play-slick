# About

This plugin makes [Slick](http://slick.typesafe.com/) a first-class citizen of Play 2.2.

The play-slick plugins consists of 2 parts: 
 - A wrapper DB object that uses the datasources defined in the Play config files, and pulls them from a connection pool. It is there so it is possible to use Slick sessions in the same fashion as you would Anorm JDBC connections.

[![Build Status](https://travis-ci.org/freekh/play-slick.png?branch=master)](https://travis-ci.org/freekh/play-slick)

# Setup

In the `project/Build.scala` file add::

In your application, add `"com.typesafe.play" %% "play-slick" % "0.6.0.1"` to the appDependencies in your `project/Build.scala` file:

to your `play.Project`.

Example :

```scala
 val appDependencies = Seq(
   //other deps
  "com.typesafe.play" %% "play-slick" % "0.6.0.1" 
 )
```

This branch supports slick 2.0 but does not support evolutions quiet yet. (It is on my TODO)

```
db.default.driver=org.h2.Driver
db.default.url="jdbc:h2:mem:play"
db.default.user=sa
db.default.password=""
```
to **application.conf** and create a model. Creating models are described in the Slick documentation: http://slick.typesafe.com/doc/1.0.1/lifted-embedding.html#tables. 

NOTE: the Slick documentation is slightly outdated. 
The computer-database contains a proposal that better describes the current reality: https://github.com/freekh/play-slick/blob/master/samples/computer-database/app/models/Models.scala#L20.

# Versioning
Play 2.2.x is supported by the 0.5.x series.
The Play 2.1 was supported in the 0.4.x series.

From Play 2.3 this module will be integrated into Play.

Please read more about usage on the [wiki](https://github.com/freekh/play-slick/wiki/Usage)

Copyright
---------

Copyright: Typesafe 2013
License: Apache License 2.0, http://www.apache.org/licenses/LICENSE-2.0.html
