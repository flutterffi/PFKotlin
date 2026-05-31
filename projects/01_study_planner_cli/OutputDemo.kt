fun main() {
    val plan = buildPlan(sampleTasks(), EnergyLevel.HIGH)

    println("Output Demo")
    println("-----------")
    printPlan(plan, explain = true)

    println()
    println("Report Preview")
    println("--------------")
    println(buildPlanReport(plan, EnergyLevel.HIGH, explain = true))
}
