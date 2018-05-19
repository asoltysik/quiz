package quiz.utils

import cats.effect.IO
import cats.syntax.foldable._
import cats.instances.list._
import doobie.util.pretty.Block
import doobie.util.query.Query0
import doobie.util.testing.{AnalysisArgs, AnalysisReport, Analyzable, analyzeIO}
import doobie.util.transactor.Transactor
import doobie.util.update.{Update, Update0}

import scala.reflect.runtime.universe.TypeTag

trait DoobieTestUtils {
  // ported from doobie-scalatest

  private def formatReport(
      args: AnalysisArgs,
      report: AnalysisReport
  ): Block = {
    val sql = args.cleanedSql
      .wrap(68)
      // SQL should use the default color
      .padLeft(Console.RESET.toString)
    val items = report.items.foldMap(formatItem)
    Block
      .fromString(args.header)
      .above(sql)
      .above(items)
  }

  private val formatItem: AnalysisReport.Item => Block = {
    case AnalysisReport.Item(desc, None) =>
      Block.fromString(s"${Console.GREEN}✓${Console.RESET} $desc")
    case AnalysisReport.Item(desc, Some(err)) =>
      Block
        .fromString(s"${Console.RED}✕${Console.RESET} $desc")
        // No color for error details - ScalaTest paints each line of failure
        // red by default.
        .above(err.wrap(66).padLeft("  "))
  }

  case class SqlAnalysisError(msg: String) extends java.lang.AssertionError(msg)

  def checkSql(q: Update0, transactor: Transactor[IO]): Unit =
    checkImpl(Analyzable.unpack(q), transactor)

  def checkSql[A](q: Query0[A], transactor: Transactor[IO])(
      implicit A: TypeTag[A]): Unit =
    checkImpl(Analyzable.unpack(q), transactor)

  private def checkImpl(args: AnalysisArgs,
                        transactor: Transactor[IO]): Unit = {
    val report = analyzeIO(args, transactor).unsafeRunSync()
    if (!report.succeeded) {
      throw SqlAnalysisError(formatReport(args, report).toString)
    }
  }
}
