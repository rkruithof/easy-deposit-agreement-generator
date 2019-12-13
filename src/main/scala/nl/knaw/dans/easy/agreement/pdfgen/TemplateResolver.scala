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

import java.io.{ OutputStream, OutputStreamWriter }
import java.nio.charset.Charset

import better.files.File
import nl.knaw.dans.easy.agreement.VelocityException
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine

import scala.util.{ Failure, Try }

trait TemplateResolver {

  /**
   * Create the template and write it to `out` after filling in the placeholders with `map`.
   *
   * @param out      The `OutputStream` where the filled in template is written to
   * @param map      The mapping between placeholders and actual values
   * @param encoding The encoding to be used in writing to `out`
   * @return `Success` if filling in the template succeeded, `Failure` otherwise
   */
  def createTemplate(out: OutputStream, map: PlaceholderMap, encoding: Charset = encoding): Try[Unit]
}

class VelocityTemplateResolver(templateDir: File, templateFilename: String) extends TemplateResolver with DebugEnhancedLogging {

  logger.debug("creating template")
  logger.debug(s"template folder: $templateDir")

  assert((templateDir / templateFilename).exists, s"template file ${ templateDir / templateFilename } does not exist")

  private val engine: VelocityEngine = new VelocityEngine() {
    setProperty("runtime.references.strict", "true")
    setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute")
    setProperty("file.resource.loader.path", templateDir.pathAsString)
    init()
  }

  override def createTemplate(out: OutputStream, map: PlaceholderMap, encoding: Charset): Try[Unit] = {
    logger.debug("resolve template placeholders")

    resource.managed(new OutputStreamWriter(out, encoding))
      .map(writer => {
        val context = new VelocityContext
        map.foreach { case (kw, o) => context.put(kw.keyword, o) }

        engine.getTemplate(templateFilename, encoding.displayName()).merge(context, writer)
      })
      .tried
      .recoverWith { case e => Failure(VelocityException(e.getMessage, e)) }
  }
}
