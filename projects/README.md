# Projects

This folder is for hands-on Kotlin practice that feels closer to real work.

## Current project

1. `01_study_planner_cli`

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
```
