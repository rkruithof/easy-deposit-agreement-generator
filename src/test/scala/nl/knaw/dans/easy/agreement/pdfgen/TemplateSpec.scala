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
import nl.knaw.dans.easy.agreement.fixture.{ FileSystemSupport, TestSupportFixture }
import nl.knaw.dans.easy.agreement.pdfgen.Placeholders.encodeImage
import org.joda.time.{ DateTime, DateTimeZone }
import org.scalamock.scalatest.MockFactory
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }

import scala.util.Success

class TemplateSpec extends TestSupportFixture
  with TableDrivenPropertyChecks
  with MockFactory
  with FileSystemSupport
  with BeforeAndAfterEach
  with BeforeAndAfterAll {

  private val templateDir = testDir / "template"

  // TODO a proper integration test starts with AgreementInput to test it matches the template variations
  def placeholderMap(isOpenAccess: Boolean, isSample: Boolean, available: Int): PlaceholderMap = Map(
    IsSample -> isSample.asInstanceOf[Object],
    OpenAccess -> isOpenAccess.asInstanceOf[Object],
    DateAvailable -> new DateTime(available, 12, 6, 0, 0, 0, DateTimeZone.UTC).asInstanceOf[Object],
    UnderEmbargo -> (available > 2019).asInstanceOf[Object],
    DateSubmitted -> "1992-07-30",
    DepositorCity -> "Zürich",
    Title -> "about &lt;a href='javascript:alert('XSS')'&gt;XSS&lt;/a&gt; escape &lt;b&gt;bold&lt;/b&gt; &lt;a href='http.dans.knaw.nl'&gt;linked&lt;/a&gt; content",
    DepositorEmail -> "nobody@dans.knaw.nl",
    DepositorOrganisation -> "Eidgenössische Technische Hochschule",
    Appendix3 -> "/licenses/BY-NC-SA-3.0.txt",
    TermsLicenseUrl -> "http://creativecommons.org/licenses/by-nc-sa/3.0",
    DepositorPostalCode -> "8092",
    TermsLicense -> "BY-NC-SA-3.0",
    DepositorTelephone -> "+41 44 632 11 11",
    DepositorName -> "N.O. Body",
    DepositorCountry -> "Schweiz",
    DepositorAddress -> "Rämistrasse 101",
    DansManagedDoi -> "10.17026/dans-xn3-ptsa",
    encodeImage(DansLogo, File("src/main/assembly/dist/res/dans_logo.png")).get,
    encodeImage(DrivenByData, File("src/main/assembly/dist/res/DrivenByData.png")).get,
  )

  "template" should "find placeholders for all variants" in {
    File("src/main/assembly/dist/res/template").copyTo(templateDir)
    File("target/easy-licenses/licenses").copyTo(templateDir / "licenses")
    val templateCreator = new VelocityTemplateResolver(templateDir, "Agreement.html")

    val maps = for {
      isOpenAccess <- Seq(true, false)
      isSample <- Seq(true, false)
      available <- Seq(2019, 2029)
    } yield placeholderMap(isSample, isOpenAccess, available)

    maps.map { map =>
      val output = new ByteArrayOutputStream()
      templateCreator.createTemplate(output, map).map { _ =>
        // files are saved for (css) debugging purposes
        // in case variants are broken, the message shows their file names
        (testDir / docName(map)).write(new String(output.toByteArray)).name
      }
    } should matchPattern { case List(Success(_), Success(_), Success(_), Success(_), Success(_), Success(_), Success(_), Success(_)) => }

    // just checking the length prevents huge messages when broken, and the length should be different anyway
    testDir.list
      .withFilter(_.name.endsWith(".html")).toArray
      .map(_.contentAsString.length)
      .distinct should have size 8
    // TODO none of the documents contains badly serialized content like "Option(...)", "Seq(...)"?
  }

  private def docName(map: PlaceholderMap): String = {
    val part2 = if (map(IsSample).asInstanceOf[Boolean]) "sample-"
                else ""
    val part1 = if (map(UnderEmbargo).asInstanceOf[Boolean]) "embargo-"
                else ""
    val part3 = if (map(OpenAccess).asInstanceOf[Boolean]) "open"
                else "restricted"
    s"$testDir/$part1$part2$part3.html"
  }
}
