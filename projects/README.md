# Projects

This folder is for hands-on Kotlin practice that feels closer to real work.

## Current project

1. `01_study_planner_cli`
2. `02_architecture_app`
3. `03_course_tracker_mvvm`

## How to use it

- run the planner with sample data
- switch between text input and JSON input
- try filters like `--topic`, `--top`, and `--explain`
- export a text report or JSON result

## Recommended practice sequence

1. Run the CLI with no arguments.
2. Run it with `--topic COLLECTIONS --top 1`.
3. Run it with `--import-json`.
4. Run it with `--explain`.
5. Export both text and JSON.
6. Make one scoring change and rerun the tests.

## Commands

```bash
kotlinc projects/01_study_planner_cli/*.kt -include-runtime -d study-planner.jar
java -jar study-planner.jar
java -jar study-planner.jar --topic COLLECTIONS --top 1 --explain
java -jar study-planner.jar --import-json data/study_tasks.json
java -jar study-planner.jar --file data/study_tasks.txt --save reports/today.txt
java -jar study-planner.jar --file data/study_tasks.txt --export-json reports/today.json

kotlinc projects/02_architecture_app/*.kt -include-runtime -d architecture-app.jar
java -jar architecture-app.jar

kotlinc projects/03_course_tracker_mvvm/AppModels.kt \
  projects/03_course_tracker_mvvm/DataLayer.kt \
  projects/03_course_tracker_mvvm/DomainLayer.kt \
  projects/03_course_tracker_mvvm/PresentationLayer.kt \
  projects/03_course_tracker_mvvm/Main.kt \
  -include-runtime -d course-tracker-app.jar
java -jar course-tracker-app.jar

kotlinc projects/03_course_tracker_mvvm/AppModels.kt \
  projects/03_course_tracker_mvvm/DataLayer.kt \
  projects/03_course_tracker_mvvm/DomainLayer.kt \
  projects/03_course_tracker_mvvm/PresentationLayer.kt \
  projects/03_course_tracker_mvvm/ViewPreview.kt \
  -include-runtime -d course-tracker-preview.jar
java -jar course-tracker-preview.jar
```

## Layer demos

If you want to inspect one layer without running the full CLI flow, use these demo entry files:

- `ScoringDemo.kt`: inspect ranking and score breakdowns
- `OutputDemo.kt`: inspect terminal rendering and text report output
- `JsonDemo.kt`: inspect JSON shaping for a single task and a full plan

Example commands:

```bash
kotlinc projects/01_study_planner_cli/*.kt -include-runtime -d planner-demos.jar
java -cp planner-demos.jar ScoringDemoKt
java -cp planner-demos.jar OutputDemoKt
java -cp planner-demos.jar JsonDemoKt
```
