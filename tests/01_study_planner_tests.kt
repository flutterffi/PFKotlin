import java.io.File

fun testBuildPlanPrioritizesUrgentCollectionTask() {
    val plan = buildPlan(sampleTasks(), EnergyLevel.MEDIUM)

    assertEquals(5, plan.size, "plan size")
    assertEquals("Solve collection transformation drills", plan.first().task.title, "top ranked task")
    assertEquals("Deep Work", plan.last().block, "last block")
    printTestSuccess("testBuildPlanPrioritizesUrgentCollectionTask")
}

fun testParseArgsReadsEnergyFileAndSavePath() {
    val options = parseArgs(
        arrayOf(
            "--energy", "high",
            "--file", "data/study_tasks.txt",
            "--save", "reports/out.txt",
            "--topic", "collections",
            "--top", "1"
        )
    ).getOrThrow()

    assertEquals(EnergyLevel.HIGH, options.currentEnergy, "energy option")
    assertEquals("data/study_tasks.txt", options.filePath, "file option")
    assertEquals("reports/out.txt", options.savePath, "save option")
    assertEquals(TopicType.COLLECTIONS, options.topicFilter, "topic option")
    assertEquals(1, options.topCount, "top option")
    printTestSuccess("testParseArgsReadsEnergyFileAndSavePath")
}

fun testLoadTasksFromFileSkipsComments() {
    val tempFile = File.createTempFile("pfkotlin-study", ".txt")
    tempFile.writeText(
        """
        # comment
        Review basics|SYNTAX|2|15|1|LOW

        Practice grouping|COLLECTIONS|3|25|0|MEDIUM
        """.trimIndent()
    )

    val tasks = loadTasksFromFile(tempFile.absolutePath).getOrThrow()
    assertEquals(2, tasks.size, "loaded task count")
    assertEquals(TopicType.COLLECTIONS, tasks[1].topic, "second task topic")
    tempFile.delete()
    printTestSuccess("testLoadTasksFromFileSkipsComments")
}

fun testBuildPlanReportContainsSummary() {
    val plan = buildPlan(sampleTasks(), EnergyLevel.HIGH)
    val report = buildPlanReport(plan, EnergyLevel.HIGH)

    assertContains(report, "Study Planner Report", "report header")
    assertContains(report, "Energy level: HIGH", "report energy")
    assertContains(report, "Block summary", "report summary")
    printTestSuccess("testBuildPlanReportContainsSummary")
}

fun testApplyPlannerOptionsFiltersByTopicAndTopCount() {
    val options = PlannerOptions(
        currentEnergy = EnergyLevel.MEDIUM,
        filePath = null,
        savePath = null,
        topicFilter = TopicType.COLLECTIONS,
        topCount = 1
    )

    val filtered = applyPlannerOptions(sampleTasks(), options).getOrThrow()
    assertEquals(1, filtered.size, "filtered size")
    assertEquals(TopicType.COLLECTIONS, filtered.first().topic, "filtered topic")
    printTestSuccess("testApplyPlannerOptionsFiltersByTopicAndTopCount")
}

fun main() {
    testBuildPlanPrioritizesUrgentCollectionTask()
    testParseArgsReadsEnergyFileAndSavePath()
    testLoadTasksFromFileSkipsComments()
    testBuildPlanReportContainsSummary()
    testApplyPlannerOptionsFiltersByTopicAndTopCount()
    println("All study planner tests passed.")
}
