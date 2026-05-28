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
            "--export-json", "reports/out.json",
            "--explain",
            "--topic", "collections",
            "--top", "1"
        )
    ).getOrThrow()

    assertEquals(EnergyLevel.HIGH, options.currentEnergy, "energy option")
    assertEquals("data/study_tasks.txt", options.filePath, "file option")
    assertEquals("reports/out.txt", options.savePath, "save option")
    assertEquals("reports/out.json", options.exportJsonPath, "export json option")
    assertEquals(true, options.explain, "explain option")
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
    val report = buildPlanReport(plan, EnergyLevel.HIGH, false)

    assertContains(report, "Study Planner Report", "report header")
    assertContains(report, "Energy level: HIGH", "report energy")
    assertContains(report, "Block summary", "report summary")
    printTestSuccess("testBuildPlanReportContainsSummary")
}

fun testApplyPlannerOptionsFiltersByTopicAndTopCount() {
    val options = PlannerOptions(
        currentEnergy = EnergyLevel.MEDIUM,
        filePath = null,
        jsonFilePath = null,
        savePath = null,
        exportJsonPath = null,
        explain = false,
        topicFilter = TopicType.COLLECTIONS,
        topCount = 1
    )

    val filtered = applyPlannerOptions(sampleTasks(), options).getOrThrow()
    assertEquals(1, filtered.size, "filtered size")
    assertEquals(TopicType.COLLECTIONS, filtered.first().topic, "filtered topic")
    printTestSuccess("testApplyPlannerOptionsFiltersByTopicAndTopCount")
}

fun testLoadTasksFromJsonFileParsesArray() {
    val tasks = loadTasksFromJsonFile(
        "/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/data/study_tasks.json"
    ).getOrThrow()

    assertEquals(5, tasks.size, "json task count")
    assertEquals("Review smart casts and null checks", tasks.first().title, "json first title")
    assertEquals(null, tasks[3].deadlineDays, "json nullable deadline")
    printTestSuccess("testLoadTasksFromJsonFileParsesArray")
}

fun testPlanToJsonContainsPlanFields() {
    val plan = buildPlan(sampleTasks(), EnergyLevel.MEDIUM)
    val json = planToJson(plan, EnergyLevel.MEDIUM)

    assertContains(json, "\"energyLevel\": \"MEDIUM\"", "json energy field")
    assertContains(json, "\"plan\": [", "json plan array")
    assertContains(json, "\"score\":", "json score field")
    assertContains(json, "\"breakdown\":{", "json breakdown field")
    printTestSuccess("testPlanToJsonContainsPlanFields")
}

fun testBuildPlanReportContainsBreakdownWhenExplainIsEnabled() {
    val plan = buildPlan(sampleTasks(), EnergyLevel.MEDIUM)
    val report = buildPlanReport(plan, EnergyLevel.MEDIUM, true)

    assertContains(report, "breakdown=", "report breakdown line")
    assertContains(report, "urgency=+", "report urgency detail")
    printTestSuccess("testBuildPlanReportContainsBreakdownWhenExplainIsEnabled")
}

fun testScoreBreakdownMatchesSessionScore() {
    val plan = buildPlan(sampleTasks(), EnergyLevel.MEDIUM)
    val first = plan.first()

    assertEquals(first.score, first.breakdown.totalScore(), "breakdown total")
    assertContains(first.breakdown.asLine(), "difficulty=-", "breakdown difficulty detail")
    printTestSuccess("testScoreBreakdownMatchesSessionScore")
}

fun main() {
    testBuildPlanPrioritizesUrgentCollectionTask()
    testParseArgsReadsEnergyFileAndSavePath()
    testLoadTasksFromFileSkipsComments()
    testBuildPlanReportContainsSummary()
    testApplyPlannerOptionsFiltersByTopicAndTopCount()
    testLoadTasksFromJsonFileParsesArray()
    testPlanToJsonContainsPlanFields()
    testBuildPlanReportContainsBreakdownWhenExplainIsEnabled()
    testScoreBreakdownMatchesSessionScore()
    println("All study planner tests passed.")
}
