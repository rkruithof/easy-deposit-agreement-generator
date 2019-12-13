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
package nl.knaw.dans.easy.agreement.pdfgen

import java.io.{ File => JFile }

import better.files.File
import nl.knaw.dans.easy.agreement._
import nl.knaw.dans.lib.error._
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime

import scala.util.{ Failure, Try }

trait Placeholders {

  def inputToPlaceholderMap(input: AgreementInput): Try[PlaceholderMap]
}

class V4AgreementPlaceholders(dansLogoFile: File, drivenByDataFile: File, licenses: Licenses) extends Placeholders with DebugEnhancedLogging {
  override def inputToPlaceholderMap(input: AgreementInput): Try[PlaceholderMap] = {
    logger.debug("create placeholder map")

    val placeholderMap = for {
      dansLogo <- encodeImage(DansLogo, dansLogoFile)
      drivenByData <- encodeImage(DrivenByData, drivenByDataFile)
      termsLicenseMap <- termsLicenseMap(input)
      headerMap = if (input.sample) sampleHeader(input)
                  else header(input)
      depositorMap = depositor(input.depositor)
      openAccess = OpenAccess -> boolean2Boolean(isOpenAccess(input))
      embargoMap = embargo(input)
    } yield headerMap + dansLogo + drivenByData ++ depositorMap + openAccess ++ termsLicenseMap ++ embargoMap

    placeholderMap
      .doIfSuccess(placeholders => {
        logger.debug {
          placeholders
            .withFilter { case (keyword, _) => keyword != DansLogo }
            .withFilter { case (keyword, _) => keyword != DrivenByData }
            .map { case (keyword, obj) => s" - $keyword: $obj" }
            .mkString("placeholders:\n", "\n", "")
        }
      })
  }

  def header(input: AgreementInput): PlaceholderMap = {
    Map(
      IsSample -> boolean2Boolean(false),
      DansManagedDoi -> input.doi,
      DateSubmitted -> input.submitted.toString,
      Title -> input.title,
    )
  }

  def sampleHeader(input: AgreementInput): PlaceholderMap = {
    Map(
      IsSample -> boolean2Boolean(true),
      DateSubmitted -> input.submitted.toString,
      Title -> input.title,
    )
  }

  def encodeImage(keyword: KeywordMapping, file: File): Try[(KeywordMapping, String)] = Try {
    keyword -> new String(Base64.encodeBase64(FileUtils.readFileToByteArray(file.toJava)))
  } recoverWith {
    case e => Failure(PlaceholderException(e.getMessage, Option(e)))
  }

  def depositor(depositor: Depositor): PlaceholderMap = {
    Map(
      DepositorName -> depositor.name,
      DepositorOrganisation -> depositor.organisation,
      DepositorAddress -> depositor.address,
      DepositorPostalCode -> depositor.zipcode,
      DepositorCity -> depositor.city,
      DepositorCountry -> depositor.country,
      DepositorTelephone -> depositor.phone,
      DepositorEmail -> depositor.email
    )
  }

  def isOpenAccess(input: AgreementInput): Boolean = {
    List(AccessCategory.OPEN_ACCESS, AccessCategory.ANONYMOUS_ACCESS, AccessCategory.FREELY_AVAILABLE)
      .contains(input.accessCategory)
  }

  def embargo(input: AgreementInput): PlaceholderMap = {
    val dateAvailable = input.available
    Map(
      // because Velocity requires Java objects, we transform Scala's Boolean into a Java Boolean
      UnderEmbargo -> boolean2Boolean(new DateTime().plusMinutes(1).isBefore(dateAvailable.toDateTimeAtStartOfDay)),
      DateAvailable -> dateAvailable.toString("YYYY-MM-dd"),
    )
  }

  def termsLicenseMap(input: AgreementInput): Try[PlaceholderMap] = {
    for {
      url <- Try {
        if (input.license.startsWith("http")) input.license
        else throw InvalidLicenseException(s"invalid license: ${ input.license }")
      }
      file <- licenses.licenseLegalResource(url)
      extensionRegExp = ".[^.]+$"
    } yield Map(
      TermsLicenseUrl -> url,
      TermsLicense -> new JFile(file).getName.replaceAll(extensionRegExp, ""),
      Appendix3 -> file.toString.replaceAll(extensionRegExp, ".txt"),
    )
  }
}
