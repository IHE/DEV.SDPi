package org.sdpi.asciidoc.extension

data class VariableDeclaration(
    val terminate: Boolean,
    val variableName: String,
    val variableValue: String
) {
    companion object {
        fun fromLine(line: String): VariableDeclaration? {
            return VariableDeclarationRegex().find(line)?.let {
                VariableDeclaration(
                    terminate = it.groupValues[1] == "!",
                    variableName = it.groupValues[2].trim(),
                    variableValue = it.groupValues[3].trim()
                )
            }
        }
    }
}

object VariableDeclarationRegex {
    operator fun invoke() = """^:(!?)(.+?):(.*)$""".toRegex()
}