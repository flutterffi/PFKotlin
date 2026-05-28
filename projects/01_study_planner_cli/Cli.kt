fun printUsage() {
    println("Study Planner CLI")
    println("Usage:")
    println("  --help")
    println("  --energy <LOW|MEDIUM|HIGH>")
    println("  --file <path>")
    println("  --import-json <path>")
    println("  --save <path>")
    println("  --export-json <path>")
    println("  --explain")
    println("  --topic <SYNTAX|OOP|COLLECTIONS|CONCURRENCY|TESTING>")
    println("  --top <count>")
    println()
    println("Examples:")
    println("  java -jar study-planner.jar")
    println("  java -jar study-planner.jar --energy HIGH")
    println("  java -jar study-planner.jar --energy LOW --file data/study_tasks.txt")
    println("  java -jar study-planner.jar --import-json data/study_tasks.json")
    println("  java -jar study-planner.jar --file data/study_tasks.txt --save reports/today.txt")
    println("  java -jar study-planner.jar --export-json reports/today.json")
    println("  java -jar study-planner.jar --topic COLLECTIONS --top 1 --explain")
}

fun parseArgs(args: Array<String>): Result<PlannerOptions> = runCatching {
    var energy = EnergyLevel.MEDIUM
    var filePath: String? = null
    var jsonFilePath: String? = null
    var savePath: String? = null
    var exportJsonPath: String? = null
    var explain = false
    var topicFilter: TopicType? = null
    var topCount: Int? = null
    var index = 0

    while (index < args.size) {
        when (args[index]) {
            "--help" -> {
                printUsage()
                throw IllegalStateException("HELP_REQUESTED")
            }
            "--energy" -> {
                require(index + 1 < args.size) { "Missing value after --energy" }
                energy = parseEnergyLevel(args[index + 1]).getOrElse {
                    throw IllegalArgumentException("Invalid energy level: ${args[index + 1]}")
                }
                index += 2
            }
            "--file" -> {
                require(index + 1 < args.size) { "Missing value after --file" }
                filePath = args[index + 1]
                index += 2
            }
            "--import-json" -> {
                require(index + 1 < args.size) { "Missing value after --import-json" }
                jsonFilePath = args[index + 1]
                index += 2
            }
            "--save" -> {
                require(index + 1 < args.size) { "Missing value after --save" }
                savePath = args[index + 1]
                index += 2
            }
            "--export-json" -> {
                require(index + 1 < args.size) { "Missing value after --export-json" }
                exportJsonPath = args[index + 1]
                index += 2
            }
            "--explain" -> {
                explain = true
                index += 1
            }
            "--topic" -> {
                require(index + 1 < args.size) { "Missing value after --topic" }
                topicFilter = parseTopicType(args[index + 1]).getOrElse {
                    throw IllegalArgumentException("Invalid topic: ${args[index + 1]}")
                }
                index += 2
            }
            "--top" -> {
                require(index + 1 < args.size) { "Missing value after --top" }
                topCount = parsePositiveInt(args[index + 1], "top").getOrElse {
                    throw IllegalArgumentException("Invalid top count: ${args[index + 1]}")
                }
                index += 2
            }
            else -> {
                throw IllegalArgumentException("Unknown argument: ${args[index]}")
            }
        }
    }

    require(!(filePath != null && jsonFilePath != null)) {
        "Use either --file or --import-json, not both"
    }

    PlannerOptions(
        currentEnergy = energy,
        filePath = filePath,
        jsonFilePath = jsonFilePath,
        savePath = savePath,
        exportJsonPath = exportJsonPath,
        explain = explain,
        topicFilter = topicFilter,
        topCount = topCount
    )
}
