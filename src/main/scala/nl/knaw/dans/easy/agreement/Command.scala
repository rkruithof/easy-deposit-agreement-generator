/**
 * Copyright (C) 2019 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.agreement

import better.files.File
import nl.knaw.dans.lib.error._
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import resource.managed

import scala.language.reflectiveCalls
import scala.util.control.NonFatal
import scala.util.{ Failure, Success, Try }

object Command extends App with DebugEnhancedLogging {
  type FeedBackMessage = String

  val configuration = Configuration(File(System.getProperty("app.home")))
  val commandLine: CommandLineOptions = new CommandLineOptions(args, configuration) {
    verify()
  }
  val app = new EasyDepositAgreementGeneratorApp(configuration)

  runSubcommand(app)
    .doIfSuccess(msg => Console.err.println(s"OK: $msg"))
    .doIfFailure { case e => logger.error(e.getMessage, e) }
    .doIfFailure { case NonFatal(e) => Console.err.println(s"FAILED: ${ e.getMessage }") }

  private def runSubcommand(app: EasyDepositAgreementGeneratorApp): Try[FeedBackMessage] = {
    commandLine.subcommand
      .collect {
        case commandLine.runService =>
          runAsService(app)
        case generate @ commandLine.generate =>
          runGenerate(generate.inputFile.toOption, generate.outputFile.toOption)(app)
        case _ =>
          Failure(new IllegalArgumentException(s"Unknown command: ${ commandLine.subcommand }"))
      }
      .getOrElse(Success(s"Missing subcommand. Please refer to '${ commandLine.printedName } --help'."))
  }

  private def runGenerate(inputFile: Option[File], outputFile: Option[File])
                         (app: EasyDepositAgreementGeneratorApp): Try[FeedBackMessage] = {
    outputFile
      .map(file => managed(file.createFileIfNotExists().newOutputStream))
      .getOrElse(managed(Console.out))
      .map(os =>
        inputFile
          .map(_.fileReader.apply(AgreementInput.fromJSON))
          .getOrElse { Success(new Interact(configuration.licenses).interactiveAgreementInput) }
          .flatMap(app.generateAgreement(_, os))
          .map(_ => "Successfully generated deposit agreement")
      )
      .tried
      .flatten
  }

  private def runAsService(app: EasyDepositAgreementGeneratorApp): Try[FeedBackMessage] = Try {
    val service = new EasyDepositAgreementGeneratorService(configuration.serverPort, Map(
      "/" -> new EasyDepositAgreementGeneratorServlet(app, configuration.version),
    ))
    Runtime.getRuntime.addShutdownHook(new Thread("service-shutdown") {
      override def run(): Unit = {
        service.stop()
        service.destroy()
      }
    })

    service.start()
    Thread.currentThread.join()
    "Service terminated normally."
  }
}
