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

import nl.knaw.dans.easy.agreement._
import nl.knaw.dans.easy.agreement.fixture.{ FileSystemSupport, TestSupportFixture }
import org.apache.commons.configuration.PropertiesConfiguration
import org.joda.time.{ DateTime, DateTimeZone, LocalDate }

import scala.util.{ Failure, Success }

class V4AgreementPlaceholdersSpec extends TestSupportFixture with FileSystemSupport {

  def testInstance: V4AgreementPlaceholders = {
    new V4AgreementPlaceholders(
      dansLogoFile = testDir,
      drivenByDataFile = testDir,
      licenses = new Licenses(new PropertiesConfiguration() {
        setProperty("https://non-existing-license.url", "license.html")
      }),
    )
  }

  def testInput: AgreementInput = AgreementInput(
    depositor = Depositor(
      name = "my name",
      address = "my address",
      zipcode = "my zipcode",
      city = "my city",
      country = "my country",
      organisation = "my organisation",
      phone = "my phone number",
      email = "my email",
    ),
    doi = "10.17026/dans-xn3-ptsa",
    title = "Test title",
    dateSubmitted = new DateTime(2019, 12, 6, 14, 38, 0, DateTimeZone.UTC),
    dateAvailable = new DateTime(2018, 12, 6, 14, 38, 0, DateTimeZone.UTC),
    accessCategory = AccessCategory.OPEN_ACCESS,
    license = "https://non-existing-license.url",
    sample = false,
    agreementVersion = AgreementVersion.FOUR_ZERO,
    agreementLanguage = AgreementLanguage.EN,
  )

  "header" should "yield a map of the DOI, date and title" in {
    testInstance.header(testInput) should contain only(
      IsSample -> false,
      DansManagedDoi -> "10.17026/dans-xn3-ptsa",
      DateSubmitted -> "2019-12-06",
      Title -> "Test title",
    )
  }

  "sampleHeader" should "yield a map of the date and title" in {
    testInstance.sampleHeader(testInput) should contain only(
      IsSample -> true,
      DateSubmitted -> "2019-12-06",
      Title -> "Test title",
    )
  }

  "depositor" should "yield a map with the Depositor fields" in {
    testInstance.depositor(testInput.depositor) should contain only(
      DepositorName -> "my name",
      DepositorAddress -> "my address",
      DepositorPostalCode -> "my zipcode",
      DepositorCity -> "my city",
      DepositorCountry -> "my country",
      DepositorOrganisation -> "my organisation",
      DepositorTelephone -> "my phone number",
      DepositorEmail -> "my email",
    )
  }

  "isOpenAccess" should "test whether the dataset is open access" in {
    testInstance.isOpenAccess(testInput) shouldBe true
  }

  it should "return false when the dataset is not open access" in {
    testInstance.isOpenAccess(testInput.copy(accessCategory = AccessCategory.REQUEST_PERMISSION)) shouldBe false
  }

  "embargo" should "yield a Map with embargo information" in {
    testInstance.embargo(testInput) should contain only(
      UnderEmbargo -> false,
      DateAvailable -> "2018-12-06",
    )
  }

  it should "yield a Map with embargo information when it is under embargo" in {
    testInstance.embargo(testInput.copy(dateAvailable = DateTime.now().plusDays(5))) should contain only(
      UnderEmbargo -> true,
      DateAvailable -> LocalDate.now().plusDays(5).toString,
    )
  }

  it should "yield a Map with non-embargo information when the embargo date is today" in {
    testInstance.embargo(testInput.copy(dateAvailable = DateTime.now())) should contain only(
      UnderEmbargo -> false,
      DateAvailable -> LocalDate.now().toString,
    )
  }

  "termsLicenseMap" should "yield a Map with license information" in {
    inside(testInstance.termsLicenseMap(testInput)) {
      case Success(placeholderMap) =>
        placeholderMap should contain only(
          TermsLicenseUrl -> "https://non-existing-license.url",
          TermsLicense -> "license",
          Appendix3 -> "/licenses/license.txt",
        )
    }
  }

  it should "fail with a invalid license url" in {
    testInstance.termsLicenseMap(testInput.copy(license = "errr")) should matchPattern {
      case Failure(InvalidLicenseException("invalid license: errr")) =>
    }
  }
}
