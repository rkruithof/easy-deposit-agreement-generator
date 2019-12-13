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

import nl.knaw.dans.easy.agreement.AccessCategory.AccessCategory
import org.joda.time.LocalDate

import scala.annotation.tailrec
import scala.util.{ Failure, Success, Try }

class Interact(licenses: Licenses) {

  private def interactString(question: String): String = {
    print(question)
    Console.in.readLine()
  }

  @tailrec
  private def interactBoolean(question: String): Boolean = {
    val input = interactString(question)
    Try { input.toBoolean } match {
      case Success(value) => value
      case Failure(_) =>
        println("    >> invalid input, please enter a boolean ('true' or 'false')")
        interactBoolean(question)
    }
  }

  @tailrec
  private def interactLocalDate(question: String): LocalDate = {
    val input = interactString(question)
    Try { LocalDate.parse(input) } match {
      case Success(value) => value
      case Failure(_) =>
        println("    >> invalid input, please enter a valid date (yyyy-MM-dd)")
        interactLocalDate(question)
    }
  }

  @tailrec
  private def interactAccessCategory(question: String): AccessCategory = {
    val input = interactString(question)
    Try { AccessCategory.withName(input) } match {
      case Success(value) => value
      case Failure(_) =>
        println("    >> invalid input, please enter a valid access category")
        interactAccessCategory(question)
    }
  }

  @tailrec
  private def interactLicense(question: String): String = {
    val input = interactString(question)
    if (licenses.isValidLicense(input))
      input
    else {
      println("    >> invalid input, please enter a valid license URL")
      interactLicense(question)
    }
  }

  private def print(s: String): Unit = Console.err.print(s)

  private def println(s: String): Unit = Console.err.println(s)

  def interactiveAgreementInput: AgreementInput = {
    println("You're entering interactive mode for generating a deposit agreement.")
    val depositor = interactiveDepositor
    println("Please enter dataset related information.")
    AgreementInput(
      depositor = depositor,
      doi = interactString("  doi: "),
      title = interactString("  dataset title: "),
      dateSubmitted = interactLocalDate("  date submitted: ").toDateTimeAtStartOfDay,
      dateAvailable = interactLocalDate("  date available: ").toDateTimeAtStartOfDay,
      accessCategory = interactAccessCategory("  access category: "),
      license = interactLicense("  license URL: "),
      sample = interactBoolean("  deposit agreement is sample (true/false): "),
      agreementVersion = AgreementVersion.FOUR_ZERO,
      agreementLanguage = AgreementLanguage.EN,
    )
  }

  def interactiveDepositor: Depositor = {
    println("Please enter depositor related information.")
    Depositor(
      name = interactString("  name: "),
      address = interactString("  address: "),
      zipcode = interactString("  zipcode: "),
      city = interactString("  city: "),
      country = interactString("  country: "),
      organisation = interactString("  organisation: "),
      phone = interactString("  phone: "),
      email = interactString("  email: "),
    )
  }
}
