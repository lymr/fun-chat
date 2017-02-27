package messages

import base.TestFlatSpec
import messages.parser.MessageConstants._
import messages.parser._
class MessageParserSpec extends TestFlatSpec {

  val parser = new MessageParser()

  "Parser" should "parse simple send with text" in {
    parser.parse("send to \"user\" message \"sample-text\"") should equal(
      Some(Send(Seq(Content(Entity("sample-text"), MessageOperator)), To(Seq(Entity("user"))))))
  }

  "Parser" should "fail if message does not contains subject" in {
    parser.parse("send message \"sample-text\"") should equal(None)
  }

  "Parser" should "parse send request with multiple users and text" in {
    parser.parse("send to \"user-1\", \"user-2\", \"user-3\" message \"sample-text\"") should equal(
      Some(Send(Seq(Content(Entity("sample-text"), MessageOperator)),
                To(Seq(Entity("user-1"), Entity("user-2"), Entity("user-3"))))))
  }

  "Parser" should "parse send request with multiple users and content" in {
    parser.parse("send to \"user-1\", \"user-2\", \"user-3\" " +
                 "message \"sample-text-1\", message \"sample-text-2\", " +
                 "attachment \"../some-path/some-file\"") should equal(
      Some(
        Send(Seq(Content(Entity("sample-text-1"), MessageOperator),
                 Content(Entity("sample-text-2"), MessageOperator),
                 Content(Entity("../some-path/some-file"), AttachmentOperator)),
             To(Seq(Entity("user-1"), Entity("user-2"), Entity("user-3"))))))
  }
}
