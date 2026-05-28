fun parseDuration(raw: String): Result<Int> = runCatching {
    raw.removeSuffix("m").toInt().also {
        require(it > 0) { "Duration must be positive" }
    }
}

fun main() {
    val rawDurations = sequenceOf("15m", "25m", "oops", "45m", "-5m")

    val parsed = rawDurations
        .map { token -> token to parseDuration(token) }
        .toList()

    parsed.forEach { (token, result) ->
        val message = result.fold(
            onSuccess = { "ok -> $it minutes" },
            onFailure = { "error -> ${it.message}" }
        )
        println("$token : $message")
    }
}
