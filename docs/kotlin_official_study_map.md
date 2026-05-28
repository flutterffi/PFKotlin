# Kotlin Official Study Map

This guide collects official Kotlin learning links in one place so you can study the language and this repository side by side.

## 1. Start Here

- Kotlin docs home: [https://kotlinlang.org/docs/home.html](https://kotlinlang.org/docs/home.html)
- Get started with Kotlin: [https://kotlinlang.org/docs/getting-started.html](https://kotlinlang.org/docs/getting-started.html)
- Basic syntax overview: [https://kotlinlang.org/docs/basic-syntax.html](https://kotlinlang.org/docs/basic-syntax.html)

Use these together with:

- [foundations/01_hello_collections.kt](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/foundations/01_hello_collections.kt)
- [foundations/02_null_safety_and_when.kt](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/foundations/02_null_safety_and_when.kt)
- [foundations/03_data_classes_and_extensions.kt](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/foundations/03_data_classes_and_extensions.kt)

## 2. Core Basics

### Types and values

- Types overview: [https://kotlinlang.org/docs/types-overview.html](https://kotlinlang.org/docs/types-overview.html)
- Numbers: [https://kotlinlang.org/docs/numbers.html](https://kotlinlang.org/docs/numbers.html)
- Null safety: [https://kotlinlang.org/docs/null-safety.html](https://kotlinlang.org/docs/null-safety.html)

Good local matches:

- [foundations/02_null_safety_and_when.kt](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/foundations/02_null_safety_and_when.kt)

### Control flow

- Conditions and loops: [https://kotlinlang.org/docs/control-flow.html](https://kotlinlang.org/docs/control-flow.html)

Good local matches:

- [foundations/02_null_safety_and_when.kt](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/foundations/02_null_safety_and_when.kt)

### Functions

- Functions: [https://kotlinlang.org/docs/functions.html](https://kotlinlang.org/docs/functions.html)
- Higher-order functions and lambdas: [https://kotlinlang.org/docs/lambdas.html](https://kotlinlang.org/docs/lambdas.html)
- Scope functions: [https://kotlinlang.org/docs/scope-functions.html](https://kotlinlang.org/docs/scope-functions.html)

Good local matches:

- [advanced/01_task_pipeline.kt](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/advanced/01_task_pipeline.kt)
- [advanced/02_result_and_sequences.kt](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/advanced/02_result_and_sequences.kt)

## 3. Object Modeling

- Classes: [https://kotlinlang.org/docs/classes.html](https://kotlinlang.org/docs/classes.html)
- Packages and imports: [https://kotlinlang.org/docs/packages.html](https://kotlinlang.org/docs/packages.html)

Good local matches:

- [foundations/03_data_classes_and_extensions.kt](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/foundations/03_data_classes_and_extensions.kt)
- [projects/01_study_planner_cli/Models.kt](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/projects/01_study_planner_cli/Models.kt)

## 4. Collections

- Collections overview: [https://kotlinlang.org/docs/collections-overview.html](https://kotlinlang.org/docs/collections-overview.html)
- Constructing collections: [https://kotlinlang.org/docs/constructing-collections.html](https://kotlinlang.org/docs/constructing-collections.html)
- Collection operations overview: [https://kotlinlang.org/docs/collection-operations.html](https://kotlinlang.org/docs/collection-operations.html)

Good local matches:

- [foundations/01_hello_collections.kt](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/foundations/01_hello_collections.kt)
- [projects/01_study_planner_cli/Scoring.kt](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/projects/01_study_planner_cli/Scoring.kt)

## 5. Coroutines and async basics

- Coroutines overview: [https://kotlinlang.org/docs/coroutines-overview.html](https://kotlinlang.org/docs/coroutines-overview.html)
- Coroutines basics: [https://kotlinlang.org/docs/coroutines-basics.html](https://kotlinlang.org/docs/coroutines-basics.html)
- Coroutines guide: [https://kotlinlang.org/docs/coroutines-guide.html](https://kotlinlang.org/docs/coroutines-guide.html)
- Asynchronous Flow: [https://kotlinlang.org/docs/flow.html](https://kotlinlang.org/docs/flow.html)

Repository note:

- This repo does not yet have a coroutine project, so these are ideal next-step official readings after the current syntax and CLI practice.

## 6. Suggested study order

1. Read [Basic syntax overview](https://kotlinlang.org/docs/basic-syntax.html).
2. Run the three files in [foundations](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/foundations).
3. Read [Null safety](https://kotlinlang.org/docs/null-safety.html) and [Functions](https://kotlinlang.org/docs/functions.html).
4. Run the files in [advanced](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/advanced).
5. Read the collection docs and revisit [Scoring.kt](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/projects/01_study_planner_cli/Scoring.kt).
6. Read the class and package docs and inspect the split planner files in [projects/01_study_planner_cli](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/projects/01_study_planner_cli).
7. Use the tests in [tests](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/tests) to confirm your changes while studying.

## 7. One simple routine

- Read one official page.
- Open the matching local practice file.
- Predict the output before running it.
- Change one rule.
- Run it again.
- Write one note about what Kotlin feature became clearer.
