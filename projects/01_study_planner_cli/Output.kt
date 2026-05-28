import java.io.File

fun printPlan(plan: List<PlannedSession>, explain: Boolean) {
    println("Study Planner Result")
    println("--------------------")

    plan.forEachIndexed { index, session ->
        val task = session.task
        val deadlineText = task.deadlineDays?.let { "$it day(s)" } ?: "flexible"

        println(
            "${index + 1}. ${task.title} | " +
                "topic=${task.topic} | " +
                "score=${session.score} | " +
                "block=${session.block} | " +
                "time=${task.estimatedMinutes}m | " +
                "deadline=$deadlineText"
        )

        if (explain) {
            println("   breakdown: ${session.breakdown.asLine()}")
        }
    }

    val grouped = plan.groupBy { it.block }
    println()
    println("Block summary")
    grouped.forEach { (block, sessions) ->
        val totalMinutes = sessions.sumOf { it.task.estimatedMinutes }
        println("- $block: ${sessions.size} task(s), $totalMinutes minutes")
    }
}

fun buildPlanReport(
    plan: List<PlannedSession>,
    currentEnergy: EnergyLevel,
    explain: Boolean
): String {
    val lines = mutableListOf<String>()
    lines += "Study Planner Report"
    lines += "===================="
    lines += "Energy level: $currentEnergy"
    lines += "Total tasks: ${plan.size}"
    lines += ""

    plan.forEachIndexed { index, session ->
        val task = session.task
        val deadlineText = task.deadlineDays?.let { "$it day(s)" } ?: "flexible"
        lines += "${index + 1}. ${task.title}"
        lines += "   topic=${task.topic}, score=${session.score}, block=${session.block}, time=${task.estimatedMinutes}m, deadline=$deadlineText"
        if (explain) {
            lines += "   breakdown=${session.breakdown.asLine()}"
        }
    }

    lines += ""
    lines += "Block summary"
    plan.groupBy { it.block }.forEach { (block, sessions) ->
        val totalMinutes = sessions.sumOf { it.task.estimatedMinutes }
        lines += "- $block: ${sessions.size} task(s), $totalMinutes minutes"
    }

    return lines.joinToString("\n")
}

fun savePlanReport(path: String, content: String): Result<Unit> = runCatching {
    val file = File(path)
    file.parentFile?.mkdirs()
    file.writeText(content)
}
