# Fun-Chat
[![Build Status](https://travis-ci.org/lymr/fun-chat.svg?branch=master)](https://travis-ci.org/lymr/fun-chat)

Fun-Chat is a showcase project for an online chat (Instant Messaging). It implements some concepts taken from
well known IM protocols, however its main goal is to demonstrate a client -> server -> client architecture utilizing
Scala, Akka, Akka-Http and PostgreSQL with ScalikeJDBC.

# Technical Features
* REST api with Akka-Http
* Non-blocking I/O with Akka actors
* Storing users credentials at local DB with proper hashing using salt.
* Persistence with PostgreSQL and ScalikeJDBC.
* Usage of Functional Programing paradigm.

# User Features
* Broadcast messages to all friends
* Send / Receive messages from a friends.
* Share a picture / document with a friends.

# Requirements
* Scala 2.11.8 (https://www.scala-lang.org/documentation/)
* JDK 8        (http://www.oracle.com/technetwork/java/javase/downloads/index.html);
* sbt          (http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html);

#Copyright
* Copyright (C) 2016 Mor Levy.
* Distributed under the MIT License.
