fun main() {
    val plan = buildPlan(sampleTasks(), EnergyLevel.MEDIUM)

    println("JSON Demo")
    println("---------")
    println("Single task JSON:")
    println(taskToJson(sampleTasks().first()))
    println()
    println("Plan JSON:")
    println(planToJson(plan, EnergyLevel.MEDIUM))
}
