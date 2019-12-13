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

import java.io.Reader
import java.text.SimpleDateFormat

import nl.knaw.dans.easy.agreement.AccessCategory.AccessCategory
import nl.knaw.dans.easy.agreement.AgreementLanguage.AgreementLanguage
import nl.knaw.dans.easy.agreement.AgreementVersion.AgreementVersion
import org.joda.time.{ DateTime, LocalDate }
import org.json4s.ext.{ DateTimeSerializer, EnumNameSerializer }
import org.json4s.native.Serialization
import org.json4s.{ DefaultFormats, Formats }

import scala.util.{ Failure, Try }

case class Depositor(name: String,
                     address: String,
                     zipcode: String,
                     city: String,
                     country: String,
                     organisation: String,
                     phone: String,
                     email: String,
                    )

object AccessCategory extends Enumeration {
  type AccessCategory = Value
  val ANONYMOUS_ACCESS, OPEN_ACCESS_FOR_REGISTERED_USERS,
  GROUP_ACCESS, REQUEST_PERMISSION, ACCESS_ELSEWHERE, NO_ACCESS,
  FREELY_AVAILABLE, OPEN_ACCESS = Value
}

object AgreementVersion extends Enumeration {
  type AgreementVersion = Value
  val FOUR_ZERO: AgreementVersion = Value("4.0")
}

object AgreementLanguage extends Enumeration {
  type AgreementLanguage = Value
  val EN, NL = Value
}

case class AgreementInput(depositor: Depositor,
                          doi: String,
                          title: String,
                          private val dateSubmitted: DateTime,
                          private val dateAvailable: DateTime,
                          accessCategory: AccessCategory,
                          license: String,
                          sample: Boolean,
                          agreementVersion: AgreementVersion,
                          agreementLanguage: AgreementLanguage,
                         ) {
  lazy val submitted: LocalDate = dateSubmitted.toLocalDate
  lazy val available: LocalDate = dateAvailable.toLocalDate
}
object AgreementInput {

  private object FormatsWithDate extends DefaultFormats {
    override protected def dateFormatter: SimpleDateFormat = {
      new SimpleDateFormat("yyyy-MM-dd") {
        setTimeZone(DefaultFormats.UTC)
      }
    }
  }

  private implicit val jsonFormats: Formats = FormatsWithDate + DateTimeSerializer ++
    List(
      AccessCategory,
      AgreementVersion,
      AgreementLanguage,
    ).map(new EnumNameSerializer(_))

  def fromJSON(json: String): Try[AgreementInput] = Try {
    Serialization.read[AgreementInput](json)
  } recoverWith {
    case e => Failure(AgreementInputException(e.getMessage, e))
  }

  def fromJSON(reader: Reader): Try[AgreementInput] = Try {
    Serialization.read[AgreementInput](reader)
  }
}
