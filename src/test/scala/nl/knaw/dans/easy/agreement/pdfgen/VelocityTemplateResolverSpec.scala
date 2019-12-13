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

import java.io.ByteArrayOutputStream

import better.files.File
import nl.knaw.dans.easy.agreement.VelocityException
import nl.knaw.dans.easy.agreement.fixture.{ FileSystemSupport, TestSupportFixture }
import org.apache.velocity.exception.MethodInvocationException
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }

import scala.util.{ Failure, Success }

class VelocityTemplateResolverSpec extends TestSupportFixture
  with MockFactory
  with FileSystemSupport
  with BeforeAndAfterEach
  with BeforeAndAfterAll {

  override def beforeEach(): Unit = {
    super.beforeEach()

    File(getClass.getResource("/velocity")).copyTo(templateDir)
  }

  private val templateDir = testDir / "template"
  private val keyword: KeywordMapping = new KeywordMapping {val keyword: String = "name" }

  "createTemplate" should "map the 'name' keyword to 'world' in the template and put the result in a file" in {
    val templateCreator = new VelocityTemplateResolver(templateDir, "AgreementTest.html")

    val map: Map[KeywordMapping, Object] = Map(keyword -> "world")
    val baos = new ByteArrayOutputStream()

    templateCreator.createTemplate(baos, map) shouldBe a[Success[_]]

    new String(baos.toByteArray) should include("<p>hello world</p>")
  }

  it should "fail if not all placeholders are filled in" in {
    val templateCreator = new VelocityTemplateResolver(templateDir, "AgreementTest.html")

    val map: Map[KeywordMapping, Object] = Map.empty
    val baos = new ByteArrayOutputStream()

    inside(templateCreator.createTemplate(baos, map)) {
      case Failure(VelocityException(msg, e: MethodInvocationException)) =>
        msg should include("$name")
        e.getMessage should include("$name")
    }

    new String(baos.toByteArray) should not include "<p>hello world</p>"
  }

  it should "fail when the template does not exist" in {
    the[AssertionError] thrownBy
      new VelocityTemplateResolver(templateDir, "AgreementFailTest.html") should
      have message s"assertion failed: template file ${ templateDir / "AgreementFailTest.html" } does not exist"
  }
}
