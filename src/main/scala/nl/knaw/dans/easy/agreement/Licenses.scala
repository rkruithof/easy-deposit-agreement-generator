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

import org.apache.commons.configuration.PropertiesConfiguration

import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }

class Licenses(licenses: PropertiesConfiguration) {

  private val licenseUrlPrefixRegExp = "https?://(www.)?"
  private val licencesMap: Map[String, String] = Try {
    licenses.getKeys.asScala.map(key =>
      normalizeURL(key) -> s"/licenses/${ licenses.getString(key) }"
    ).toMap
  }.getOrElse(Map.empty)

  private def normalizeURL(url: String): String = {
    url.replaceAll(licenseUrlPrefixRegExp, "")
  }

  def licenseLegalResource(url: String): Try[String] = {
    licencesMap.get(normalizeURL(url))
      .map(Success(_))
      .getOrElse(Failure(InvalidLicenseException(s"No legal text found for $url")))
  }

  def isValidLicense(url: String): Boolean = licencesMap contains normalizeURL(url)
}
