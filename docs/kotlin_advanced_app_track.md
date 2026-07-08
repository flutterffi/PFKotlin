# Kotlin Advanced App Track

This guide helps you move from Kotlin basics into architecture-focused app practice.

## Stage 1: Foundation refresh

Read these first:

- Official docs: [Basic syntax](https://kotlinlang.org/docs/basic-syntax.html)
- Official docs: [Basic types](https://kotlinlang.org/docs/basic-types.html)
- Official docs: [Control flow](https://kotlinlang.org/docs/control-flow.html)
- Local practice: `foundations/04_variables_and_types.kt`
- Local practice: `foundations/05_loops_and_ranges.kt`
- Local practice: `foundations/06_functions_basics.kt`

## Stage 2: Object modeling

Read these next:

- Official docs: [Classes](https://kotlinlang.org/docs/classes.html)
- Official docs: [Interfaces](https://kotlinlang.org/docs/interfaces.html)
- Official docs: [Extensions](https://kotlinlang.org/docs/extensions.html)
- Local practice: `foundations/03_data_classes_and_extensions.kt`
- Local practice: `foundations/07_classes_and_objects.kt`
- Local practice: `foundations/09_interfaces_and_inheritance.kt`

## Stage 3: Expressive Kotlin

Use these to level up your modeling style:

- Official docs: [Sealed classes and interfaces](https://kotlinlang.org/docs/sealed-classes.html)
- Official docs: [Generics](https://kotlinlang.org/docs/generics.html)
- Official docs: [Sequences](https://kotlinlang.org/docs/sequences.html)
- Official docs: [Exceptions](https://kotlinlang.org/docs/exceptions.html)
- Local practice: `advanced/01_task_pipeline.kt`
- Local practice: `advanced/02_result_and_sequences.kt`
- Local practice: `advanced/03_parser_error_modeling.kt`

## Stage 4: Project practice

Build confidence with these projects in order:

1. `projects/01_study_planner_cli`
2. `projects/02_architecture_app`
3. `projects/03_course_tracker_mvvm`

## Stage 5: What to practice inside the MVVM app

When you open `projects/03_course_tracker_mvvm`, focus on:

- state ownership in `CourseTrackerViewModel`
- repository boundaries in `DataLayer.kt`
- use case separation in `DomainLayer.kt`
- rendering without business logic in `PresentationLayer.kt`
- running the full flow from `Main.kt`
- rendering only the view from `ViewPreview.kt`

## Suggested sprint

Day 1:
- run one foundations file
- rewrite one loop with `map` or `filter`

Day 2:
- add one class or interface variation
- write one tiny assertion in `tests/`

Day 3:
- run `advanced/03_parser_error_modeling.kt`
- add one new parser error case

Day 4:
- run `projects/02_architecture_app`
- add one new dashboard action

Day 5:
- run `projects/03_course_tracker_mvvm`
- add one new filter or sorting rule

Day 6:
- run the MVVM tests
- break one use case on purpose and fix it

Day 7:
- create your own app variation from the same structure
- keep the console view and replace the domain
