fun testParseConfigLineSuccess() {
    when (val result = parseConfigLine("mode: focus")) {
        is ParseOutcome.Success -> {
            assertEquals("mode", result.value.key, "config key")
            assertEquals("focus", result.value.value, "config value")
        }
        is ParseOutcome.Failure -> {
            throw IllegalStateException("Expected success but got ${result.reason.message}")
        }
    }

    printTestSuccess("testParseConfigLineSuccess")
}

fun testParseConfigLineMissingSeparator() {
    when (val result = parseConfigLine("broken line")) {
        is ParseOutcome.Success -> {
            throw IllegalStateException("Expected failure for missing separator")
        }
        is ParseOutcome.Failure -> {
            assertTrue(result.reason is ParseFailure.MissingSeparator, "missing separator failure type")
        }
    }

    printTestSuccess("testParseConfigLineMissingSeparator")
}

fun testParseConfigLineEmptyValue() {
    when (val result = parseConfigLine("energy: ")) {
        is ParseOutcome.Success -> {
            throw IllegalStateException("Expected failure for empty value")
        }
        is ParseOutcome.Failure -> {
            assertTrue(result.reason is ParseFailure.EmptyValue, "empty value failure type")
            assertContains(result.reason.message, "energy", "empty value message")
        }
    }

    printTestSuccess("testParseConfigLineEmptyValue")
}

fun main() {
    testParseConfigLineSuccess()
    testParseConfigLineMissingSeparator()
    testParseConfigLineEmptyValue()
    println("All parser tests passed.")
}
