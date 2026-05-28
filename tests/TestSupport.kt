fun assertEquals(expected: Any?, actual: Any?, label: String) {
    if (expected != actual) {
        throw IllegalStateException("$label expected=$expected actual=$actual")
    }
}

fun assertTrue(condition: Boolean, label: String) {
    if (!condition) {
        throw IllegalStateException("Assertion failed: $label")
    }
}

fun assertContains(text: String, expectedPart: String, label: String) {
    if (!text.contains(expectedPart)) {
        throw IllegalStateException("$label missing '$expectedPart' in '$text'")
    }
}

fun printTestSuccess(name: String) {
    println("ok -> $name")
}
