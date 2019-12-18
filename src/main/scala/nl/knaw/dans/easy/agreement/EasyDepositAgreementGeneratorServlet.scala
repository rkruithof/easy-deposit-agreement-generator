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

import nl.knaw.dans.lib.error._
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import nl.knaw.dans.lib.logging.servlet.{ LogResponseBodyOnError, PlainLogFormatter, ServletLogger }
import org.scalatra._

class EasyDepositAgreementGeneratorServlet(app: EasyDepositAgreementGeneratorApp,
                                           version: String)
  extends ScalatraServlet
    with ServletLogger
    with PlainLogFormatter
    with LogResponseBodyOnError
    with DebugEnhancedLogging {

  get("/") {
    contentType = "text/plain"
    Ok(s"EASY Deposit Agreement Generator Service running ($version)")
  }

  post("/agreement") {
    contentType = if (request.getHeader("Accept").contains("/html"))
                    "text/html;charset=utf-8"
                  else "application/pdf"

    AgreementInput.fromJSON(request.body)
      .flatMap(app.generateAgreement(_, response.outputStream, contentType))
      .map(_ => Ok())
      .doIfFailure {
        case e =>
          contentType = "text/plain"
          logger.error(e.getMessage, e)
      }
      .getOrRecover {
        case InvalidLicenseException(msg) => BadRequest(msg)
        case e: AgreementInputException => BadRequest(e.getMessage)
        case e: PlaceholderException => InternalServerError(e.getMessage)
        case e: VelocityException => InternalServerError(e.getMessage)
        case e => InternalServerError(e.getMessage)
      }
  }
}
