package messages.parser

abstract class AST

abstract class Data extends AST

abstract class Subjects extends AST

abstract class Operation extends AST

case class Entity(value: String) extends AST

case class Content(entity: Entity, op: String) extends Data

case class To(recipients: Seq[Entity]) extends Subjects

case class Send(entities: Seq[Content], to: To) extends Operation