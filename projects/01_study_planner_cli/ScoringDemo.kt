fun main() {
    val energy = EnergyLevel.MEDIUM
    val plan = buildPlan(sampleTasks(), energy)

    println("Scoring Demo")
    println("------------")
    plan.forEach { session ->
        println("${session.task.title} -> score=${session.score}")
        println("  ${session.breakdown.asLine()}")
    }
}
