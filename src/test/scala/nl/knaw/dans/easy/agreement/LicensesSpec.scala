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

import nl.knaw.dans.easy.agreement.fixture.TestSupportFixture
import better.files.File.currentWorkingDirectory
import org.apache.commons.configuration.PropertiesConfiguration

import scala.util.{ Failure, Success }

class LicensesSpec extends TestSupportFixture {

  private val licensesProps = currentWorkingDirectory / "target" / "easy-licenses" / "licenses" / "licenses.properties"
  val licenses = new Licenses(new PropertiesConfiguration(licensesProps.toJava))

  "licenseLegalResource" should "map a URL to its legal resource" in {
    licenses.licenseLegalResource("http://opensource.org/licenses/MIT") should matchPattern {
      case Success("/licenses/MIT.txt") =>
    }
  }

  it should "map a URL if 'https' is used instead of 'http'" in {
    licenses.licenseLegalResource("https://opensource.org/licenses/BSD-2-Clause") should matchPattern {
      case Success("/licenses/BSD-2-Clause.txt") =>
    }
  }

  it should "fail if the URL does not occur in the listing" in {
    licenses.licenseLegalResource("http://www.invalid-license.org/licenses/LICENSE-1.0") should matchPattern {
      case Failure(InvalidLicenseException("No legal text found for http://www.invalid-license.org/licenses/LICENSE-1.0")) =>
    }
  }

  "isValidLicense" should "return true if the URL occurs in the listing" in {
    licenses.isValidLicense("http://www.apache.org/licenses/LICENSE-2.0") shouldBe true
  }

  it should "return true if the URL is listed as 'http', but the input uses 'https'" in {
    licenses.isValidLicense("https://dans.knaw.nl/en/about/organisation-and-policy/legal-information/dans-licence.pdf") shouldBe true
  }

  it should "return false if the URL does not occur in the listing" in {
    licenses.isValidLicense("http://www.invalid-license.org/licenses/LICENSE-1.0") shouldBe false
  }

  it should "return false if the input is not a URL" in {
    licenses.isValidLicense("not-a-url") shouldBe false
  }
}
