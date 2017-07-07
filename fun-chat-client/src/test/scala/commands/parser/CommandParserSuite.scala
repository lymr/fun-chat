package commands.parser

import tests.TestSuite

class CommandParserSuite extends TestSuite {

  val parser: CommandParser = new CommandParser()

  test("some arg with no tokens, result is empty") {
    val input: String       = "some-args-input"
    val tokens: Set[String] = Set.empty[String]

    val result: Map[String, String] = parser.parse(input, tokens)

    assert(result.isEmpty)
  }

  test("parsing a valueA -b valueB, an error is returned") {
    val input: String       = "a valueA b valueB"
    val tokens: Set[String] = Set("a", "b")

    assertThrows[IllegalArgumentException] {
      val result: Map[String, String] = parser.parse(input, tokens)
    }
  }

  test("parsing -a valueA -b valueB, returns args map") {
    val input: String       = "-a valueA -b valueB"
    val tokens: Set[String] = Set("a", "b")

    val result: Map[String, String] = parser.parse(input, tokens)

    assertResult(Map("a" -> "valueA", "b" -> "valueB"))(result)
  }

  test("parsing input with missing token, error is received") {
    val input: String       = "-a valueA -b valueB"
    val tokens: Set[String] = Set("a")

    assertThrows[IllegalArgumentException] {
      val result: Map[String, String] = parser.parse(input, tokens)
    }
  }

  test("parsing input with some spaces, returns args map") {
    val input: String       = "-a  valueA  -b valueB   -c    ValueC"
    val tokens: Set[String] = Set("a", "b", "c")

    val result: Map[String, String] = parser.parse(input, tokens)

    assertResult(Map("a" -> "valueA", "b" -> "valueB", "c" -> "ValueC"))(result)
  }

  test("parsing -a valueA -b valueB starting with spaces, returns args map") {
    val input: String       = "   -a valueA -b valueB"
    val tokens: Set[String] = Set("a", "b")

    val result: Map[String, String] = parser.parse(input, tokens)

    assertResult(Map("a" -> "valueA", "b" -> "valueB"))(result)
  }

}
