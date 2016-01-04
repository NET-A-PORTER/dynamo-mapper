package com.netaporter.dynamomapper

import scala.language.higherKinds
import scala.reflect.macros._
import scala.language.experimental.macros

object FormatsMacroImpl {

  def formatWriteImpl[T: c.WeakTypeTag](c: whitebox.Context): c.Expr[DynamoWrites[T]] = {
    import c.universe._

    val tpe = weakTypeOf[T]

    val fields = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }.get.paramLists.head

    val toMapParams = fields.map { field =>
      val name = field.name.toTermName
      val decoded = name.decodedName.toString
      q"($decoded -> o.$name)"
    }

    c.Expr[DynamoWrites[T]] { q"""
      new _root_.com.netaporter.dynamomapper.DynamoWrites[$tpe] {
        override def writes(o: $tpe) = map(..$toMapParams)
      }
      """
    }
  }

  def formatReadImpl[T: c.WeakTypeTag](c: whitebox.Context): c.Expr[DynamoReads[T]] = {
    import c.universe._

    val tpe = weakTypeOf[T]

    val fields = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }.get.paramLists.head

    val companionObj = tpe.typeSymbol.companion

    val (names, fromMapParams) = fields.map { field =>
      val name = field.asTerm.name
      val decoded = name.decodedName.toString
      val returnType = tpe.decl(name).typeSignature
      (q"$name", fq"""$name <- o.attr[$returnType]($decoded)""")
    }.unzip

    c.Expr[DynamoReads[T]] { q"""
      new _root_.com.netaporter.dynamomapper.DynamoReads[$tpe] {
        override def reads(o: _root_.com.netaporter.dynamomapper.DynamoValue) =
         for (..$fromMapParams) yield $companionObj(..$names)
      }
      """
    }
  }
}
