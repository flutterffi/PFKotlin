sealed class ParseOutcome<out T> {
    data class Success<T>(val value: T) : ParseOutcome<T>()
    data class Failure(val reason: ParseFailure) : ParseOutcome<Nothing>()
}

sealed class ParseFailure(val message: String) {
    data class MissingSeparator(val line: String) :
        ParseFailure("Missing ':' separator in '$line'")

    data class EmptyKey(val line: String) :
        ParseFailure("Key is empty in '$line'")

    data class EmptyValue(val key: String) :
        ParseFailure("Value is empty for key '$key'")
}

data class ConfigEntry(
    val key: String,
    val value: String
)

fun parseConfigLine(line: String): ParseOutcome<ConfigEntry> {
    val separatorIndex = line.indexOf(':')
    if (separatorIndex == -1) {
        return ParseOutcome.Failure(ParseFailure.MissingSeparator(line))
    }

    val key = line.substring(0, separatorIndex).trim()
    val value = line.substring(separatorIndex + 1).trim()

    if (key.isEmpty()) {
        return ParseOutcome.Failure(ParseFailure.EmptyKey(line))
    }

    if (value.isEmpty()) {
        return ParseOutcome.Failure(ParseFailure.EmptyValue(key))
    }

    return ParseOutcome.Success(
        ConfigEntry(
            key = key,
            value = value
        )
    )
}

fun main() {
    val lines = listOf(
        "mode: focus",
        "timeout: 25",
        "broken line",
        ": no-key",
        "energy: "
    )

    lines.forEach { line ->
        when (val result = parseConfigLine(line)) {
            is ParseOutcome.Success -> {
                println("ok -> ${result.value.key} = ${result.value.value}")
            }
            is ParseOutcome.Failure -> {
                println("error -> ${result.reason.message}")
            }
        }
    }
}
