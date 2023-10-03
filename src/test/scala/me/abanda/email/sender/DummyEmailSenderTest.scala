package me.abanda.email.sender

import cats.effect.unsafe.implicits.global
import me.abanda.email.EmailData
import me.abanda.test.BaseTest

class DummyEmailSenderTest extends BaseTest {
  it should "send scheduled email" in {
    DummyEmailSender(EmailData("test@sml.com", "subject", "content")).unsafeRunSync()
    DummyEmailSender.findSentEmail("test@sml.com", "subject").isDefined shouldBe true
  }
}
