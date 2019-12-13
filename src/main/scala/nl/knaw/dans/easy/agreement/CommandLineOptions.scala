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

import java.nio.file.Path

import better.files.File
import org.rogach.scallop.{ ScallopConf, ScallopOption, Subcommand }

class CommandLineOptions(args: Array[String], configuration: Configuration) extends ScallopConf(args) {
  appendDefaultToDescription = true
  editBuilder(_.setHelpWidth(110))
  printedName = "easy-deposit-agreement-generator"
  version(configuration.version)
  private val SUBCOMMAND_SEPARATOR = "---\n"
  val description: String = s"""Create a deposit agreement file for EASY deposits"""
  val synopsis: String =
    s"""
       |  $printedName generate [{--input|-i} <path>] [{--output|-o} <path>]
       |  $printedName run-service""".stripMargin

  version(s"$printedName v${ configuration.version }")
  banner(
    s"""
       |  $description
       |
       |Usage:
       |
       |$synopsis
       |
       |Options:
       |""".stripMargin)

  val generate = new Subcommand("generate") {
    descr("Generate a deposit agreement")
    private val inputPath: ScallopOption[Path] = opt(name = "input", short = 'i',
      descr = "The location of the JSON file containing the request")
    val inputFile: ScallopOption[File] = inputPath.map(File(_))
    private val outputPath: ScallopOption[Path] = opt(name = "output", short = 'o',
      descr = "The location for resulting PDF containing the Deposit Agreement")
    val outputFile: ScallopOption[File] = outputPath.map(File(_))

    validatePathExists(inputPath)

    footer(SUBCOMMAND_SEPARATOR)
  }
  addSubcommand(generate)

  val runService = new Subcommand("run-service") {
    descr("Starts EASY Deposit Agreement Generator as a daemon that services HTTP requests")
    footer(SUBCOMMAND_SEPARATOR)
  }
  addSubcommand(runService)

  footer("")
}
