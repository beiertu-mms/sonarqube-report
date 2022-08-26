package com.github.beiertumms.sonarqubereport

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice

fun main(args: Array<String>) = App().main(args)

// See https://ajalt.github.io/clikt/ for documentation
class App : CliktCommand(
    name = "java -jar app.jar",
    help = "A simple template to create cli. (*) = required",
    printHelpOnEmptyArgs = false,
) {
    private val name: String by argument(
        name = "name",
        help = "User name to be greeted",
    )

    private val shout: String? by option(
        "-s",
        "--shout",
        help = "Whether to shout out the name",
    ).choice("y", "n", ignoreCase = true)

    override fun run() {
        println("Hello ${if (shout == "y") name.uppercase() else name}")
    }
}
