package org.sdpi.asciidoc.extension

data class VariableDeclaration(
    val terminate: Boolean,
    val variableName: String,
    val variableValue: String
) {
    companion object {
        fun fromLine(line: String): VariableDeclaration? {
            return when (val match = VariableDeclarationRegex().find(line)) {
                null -> null
                else -> VariableDeclaration(
                    terminate = match.groupValues[1] == "!",
                    variableName = match.groupValues[2].trim(),
                    variableValue = match.groupValues[3].trim()
                )
            }
        }
    }
}

object VariableDeclarationRegex {
    operator fun invoke() = """^:(!?)(.+?):(.*)$""".toRegex()
}