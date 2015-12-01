# dynamo-mapper

[![Build Status](https://travis-ci.org/NET-A-PORTER/dynamo-mapper.svg)](https://travis-ci.org/NET-A-PORTER/dynamo-mapper)

An experiment in making the Java SDK DynamoDB Client more Scala-esque. It copies a lot of patterns from Play Framework's JSON Library.

```scala
type DynamoData = java.util.Map[String, AttributeValue]
```

The Java SDK generally uses the type `java.util.Map[String, AttributeValue]` to send data to/from DynamoDB. This has been aliased to `DynamoData`. This library provides helpers to make converting between `DynamoData` and case classes easier.

It isn't anywhere near finished. However, it may now be slightly useful. We can now convert from a case class to `DynamoData`, and from `DynamoData` back to a case class. The latter isn't bulletproof and, if you're lucky, may only give you cryptic error messages.

## Concepts

### DynamoValue
This library contains a representation of [DynamoDB's DataTypes](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DataModel.html#DataModel.DataTypes). They all extend `DynamoValue`.

They are basically wrappers around Scala types that can be easily converted into Dynamo types. For instance, `DynamoString` wraps `String`, and `DynamoMap` wraps `Map`.

### DynamoWrites[T] & DynamoReads[T]

`DynamoWrites[T]` is a trait which specifies how to convert a specific type to a `DynamoValue`.
`DynamoReads[T]` is a trait which specifies how to convert a `DynamoValue` to a specific type.

### DynamoMapper.toDynamo & DynamoMapper.fromDynamo

`toDynamo` is a method to take any `DynamoValue` and convert it into `DynamoData`, which is the type the Java SDK requires. `fromDynamo` performs the inverse of that.

### DynamoReadResult

Reading is slightly more complicated than writing, as you could try and read something that doesn't exist. In order to cope with that fact, `DynamoReadResult` exists. As an algebraic datatype it is either `DynamoReadSuccess`, and contains the thing you've read, or `DynamoReadFailure`, and contains an error message.

## Writing Example

Here is a worked example, taking a case class and writing it to DynamoDB:

```scala
import DynamoMapper._

// define case class
case class User(id: String, name: String, email: String)

// define DynamoWrites (as an implicit)
implicit val writeFormat = new DynamoWrites[User] {
  override def writes(u: User): DynamoValue =
    map("id" -> u.id, "name" -> u.name, "email" -> u.email)
}

// use the toDynamo method, and then use the Java SDK as normal
val dynamoData = toDynamo(User("homer1", "Homer Simpson", "ChunkyLover53@aol.com"))
client.putItem(new PutItemRequest(tableName, dynamoData))

```

## Reading Example

Here is a worked example, taking `DynamoData` from DynamoDB and converting it into a case class.

```scala
import scala.collection.JavaConverters._
import DynamoMapper._

case class User(id: String, name: String, email: String)

implicit val readFormat = new DynamoReads[User] {
  override def reads(d: DynamoValue): DynamoReadResult[User] = 
    for {
      id    <- d.attr[String]("id")
      name  <- d.attr[String]("name")
      email <- d.attr[String]("email")
    } yield User(id, name, email)
}

val result = getItem(new GetItemRequest(tableName, Map("id" -> new AttributeValue("homer1")).asJava)
val user = fromDynamo(result.getItem).as[User]

```


### Macros

It is also possible to define the implicit `DynamoWrites[User]` and `DynamoReads[User]` like this:

```scala
implicit val writeFormat = DynamoMapper.writeFormat[User]
implicit val readFormat  = DynamoMapper.readFormat[User]
```

This automagically generates a standard implementations.

## Work in Progress

This library is a work in progress. As such, don't hold us responsible if it breaks your application in new and horrible ways. It is also very incomplete.

Only some Scala types are supported. Only some DynamoDB types are supported.

Praise, a Pull Request, and/or Constructive Criticism is very welcome.
