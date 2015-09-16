# dynamo-mapper
An experiment in making the Java SDK DynamoDB Client more Scala-esque.

```scala
type DynamoData = java.util.Map[String, AttributeValue]
```

The Java SDK generally uses the type `java.util.Map[String, AttributeValue]` to send data to/from DynamoDB. This has been aliased to `DynamoData`. This library provides helpers to make converting between `DynamoData` and case classes easier.

It isn't anywhere near finished. However, it may now be slightly useful. We can now convert from a case class to `DynamoData`. Converting the other way is not supported yet. That is coming...

## Concepts

### DynamoValue
This library contains a representation of [DynamoDB's DataTypes](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DataModel.html#DataModel.DataTypes). They all extend `DynamoValue`.

They are basically wrappers around Scala types that can be easily converted into Dynamo types. For instance, `DynamoString` wraps `String`, and `DynamoMap` wraps `Map`.

### DynamoWrites[T]

`DynamoWrites[T]` is a trait which specifies how to convert a specific type to a `DynamoValue`.

### DynamoMapper.toDynamo

This is a method to take any `DynamoValue` and convert it into `DynamoData`, which is the type the Java SDK requires.

## Example

Here is a worked example:

```scala
import DynamoMapper._

// define case class
case class User(name: String, email: String)

// define DynamoWrites (as an implicit)
implicit val writeFormat = new DynamoWrites[User] {
  override def writes(u: User): DynamoValue =
    map("name" -> u.name, "email" -> u.email)
}

// use the toDynamo method, and then use the Java SDK as normal
val dynamoData = toDynamo(User("Homer Simpson", "ChunkyLover53@aol.com"))
client.putItem(new PutItemRequest(tableName, dynamoData)
```

### Macros

It is also possible to define the implicit `DynamoWrites[User]` like this:

```scala
implicit val writeFormat = DynamoMapper.writeFormat[User]
```

This automagically generates a standard `DynamoWrites` implementation. 

## Work in Progress

This library is a work in progress. As such, don't hold us responsible if it breaks your application in new and horrible ways. It is also very incomplete.

Only some Scala types (Map, String) are supported. Only some DynamoDB types are supported (Map, String).

Praise, a Pull Request, and/or Constructive Criticism is very welcome.

