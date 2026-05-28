fun resolveTasks(options: PlannerOptions): Result<List<StudyTask>> {
    val loadedTasks = when {
        options.filePath != null -> loadTasksFromFile(options.filePath)
        options.jsonFilePath != null -> loadTasksFromJsonFile(options.jsonFilePath)
        else -> Result.success(sampleTasks())
    }

    return loadedTasks.fold(
        onSuccess = { applyPlannerOptions(it, options) },
        onFailure = { Result.failure(it) }
    )
}

fun main(args: Array<String>) {
    val options = parseArgs(args).getOrElse { error ->
        if (error.message == "HELP_REQUESTED") {
            return
        }
        System.err.println("Argument error: ${error.message}")
        printUsage()
        return
    }

    val tasks = resolveTasks(options).getOrElse { error ->
        System.err.println("Input error: ${error.message}")
        return
    }

    val plan = buildPlan(tasks, options.currentEnergy)
    printPlan(plan, options.explain)

    if (options.savePath != null) {
        val report = buildPlanReport(plan, options.currentEnergy, options.explain)
        savePlanReport(options.savePath, report).getOrElse { error ->
            System.err.println("Save error: ${error.message}")
            return
        }
        println()
        println("Saved report to ${options.savePath}")
    }

    if (options.exportJsonPath != null) {
        val json = planToJson(plan, options.currentEnergy)
        saveJson(options.exportJsonPath, json).getOrElse { error ->
            System.err.println("Export error: ${error.message}")
            return
        }
        println("Saved JSON to ${options.exportJsonPath}")
    }
}
