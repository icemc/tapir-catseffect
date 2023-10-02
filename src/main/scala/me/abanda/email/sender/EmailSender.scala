package me.abanda.email.sender

import cats.effect.IO
import me.abanda.email.{EmailConfig, EmailData}

trait EmailSender {
  def apply(email: EmailData): IO[Unit]
}

object EmailSender {
  def create(sttpBackend: SttpBackend[IO, Any], config: EmailConfig): EmailSender = if (config.mailgun.enabled) {
    new MailgunEmailSender(config.mailgun, sttpBackend)
  } else if (config.smtp.enabled) {
    new SmtpEmailSender(config.smtp)
  } else {
    DummyEmailSender
  }
}
