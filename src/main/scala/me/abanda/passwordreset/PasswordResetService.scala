package me.abanda.passwordreset

import cats.effect.IO
import cats.syntax.all._
import me.abanda.email.{EmailData, EmailScheduler, EmailSubjectContent, EmailTemplates}
import me.abanda.infrastructure.Doobie._
import me.abanda.logging.FLogging
import me.abanda.security.Auth
import me.abanda.user.{User, UserModel}
import me.abanda.util._
import me.abanda.security.Auth
import me.abanda.user.{User, UserModel}
import me.abanda.util.{Clock, IdGenerator}

class PasswordResetService(
    userModel: UserModel,
    passwordResetCodeModel: PasswordResetCodeModel,
    emailScheduler: EmailScheduler,
    emailTemplates: EmailTemplates,
    auth: Auth[PasswordResetCode],
    idGenerator: IdGenerator,
    config: PasswordResetConfig,
    clock: Clock,
    xa: Transactor[IO]
) extends FLogging {

  def forgotPassword(loginOrEmail: String): ConnectionIO[Unit] = {
    userModel
      .findByLoginOrEmail(loginOrEmail.lowerCased)
      .flatMap {
        case None       => logger.debug(s"Could not find user with $loginOrEmail login/email")
        case Some(user) => createCode(user).flatMap(pcr => sendCode(user, pcr))
      }
  }

  private def createCode(user: User): ConnectionIO[PasswordResetCode] = {
    for {
      _ <- logger.debug[ConnectionIO](s"Creating password reset code for user: ${user.id}")
      id <- idGenerator.nextId[ConnectionIO, PasswordResetCode]()
      validUntil <- clock
        .now[ConnectionIO]()
        .map { value =>
          value.plusMillis(config.codeValid.toMillis)
        }
      passwordResetCode = PasswordResetCode(id, user.id, validUntil)
      _ <- passwordResetCodeModel.insert(passwordResetCode)
    } yield passwordResetCode
  }

  private def sendCode(user: User, code: PasswordResetCode): ConnectionIO[Unit] = {
    logger.debug[ConnectionIO](s"Scheduling e-mail with reset code for user: ${user.id}") >>
      emailScheduler(EmailData(user.emailLowerCased, prepareResetEmail(user, code)))
  }

  private def prepareResetEmail(user: User, code: PasswordResetCode): EmailSubjectContent = {
    val resetLink = String.format(config.resetLinkPattern, code.id)
    emailTemplates.passwordReset(user.login, resetLink)
  }

  def resetPassword(code: String, newPassword: String): IO[Unit] = {
    for {
      userId <- auth(code.asInstanceOf[Id])
      _ <- logger.debug[IO](s"Resetting password for user: $userId")
      _ <- userModel.updatePassword(userId, User.hashPassword(newPassword)).transact(xa)
    } yield ()
  }
}
