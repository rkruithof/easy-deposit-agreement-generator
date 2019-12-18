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

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, OutputStream }

import nl.knaw.dans.easy.agreement.pdfgen._
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import scala.sys.process.ProcessLogger
import scala.util.Try

class EasyDepositAgreementGeneratorApp(configuration: Configuration) extends DebugEnhancedLogging {

  private val placeholders: Placeholders = new V4AgreementPlaceholders(configuration.dansLogoFile, configuration.drivenByDataFile, configuration.licenses)
  private val templateResolver: TemplateResolver = new VelocityTemplateResolver(configuration.templateDir, configuration.templateFilename)
  private val pdfGenerator: PdfGenerator = new WeasyPrintPdfGenerator(configuration.pdfRunScript)
  private val processLogger: ProcessLogger = ProcessLogger(s => logger.info(s), e => logger.error(e))

  def generateAgreement(input: AgreementInput, docOutputStream: => OutputStream, contentType: String = "application/pdf"): Try[Unit] = {
    trace(input)
    resource.managed(new ByteArrayOutputStream())
      .map(templateOS => {
        if (contentType.contains("/html"))
          for {
            placeholderMap <- placeholders.inputToPlaceholderMap(input)
            _ = logger.info(s"generating HTML: $contentType")
            _ <- templateResolver.createTemplate(docOutputStream, placeholderMap)
          } yield ()
        else
          for {
            placeholderMap <- placeholders.inputToPlaceholderMap(input)
            _ <- templateResolver.createTemplate(templateOS, placeholderMap)
            _ = logger.info(s"generating PDF: $contentType")
            pdfInput = new ByteArrayInputStream(templateOS.toByteArray)
            _ = pdfGenerator.createPdf(pdfInput, docOutputStream) ! processLogger
          } yield ()
      })
      .tried
      .flatten
  }
}
