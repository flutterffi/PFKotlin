# Foundations

This folder is for first-pass Kotlin syntax practice.

## Goal

- learn the shape of Kotlin code
- read short runnable examples
- change one thing at a time

## Study order

1. `01_hello_collections.kt`
2. `02_null_safety_and_when.kt`
3. `03_data_classes_and_extensions.kt`
4. `04_variables_and_types.kt`
5. `05_loops_and_ranges.kt`
6. `06_functions_basics.kt`
7. `07_classes_and_objects.kt`
8. `08_exceptions_basics.kt`
9. `09_interfaces_and_inheritance.kt`

## Good practice loop

1. Run one file.
2. Predict the output before reading it closely.
3. Change one value or branch.
4. Run it again.
5. Write your own variation in `playground/scratch`.

## Commands

```bash
kotlinc foundations/01_hello_collections.kt -include-runtime -d foundations01.jar
java -jar foundations01.jar

kotlinc foundations/02_null_safety_and_when.kt -include-runtime -d foundations02.jar
java -jar foundations02.jar

kotlinc foundations/03_data_classes_and_extensions.kt -include-runtime -d foundations03.jar
java -jar foundations03.jar

kotlinc foundations/04_variables_and_types.kt -include-runtime -d foundations04.jar
java -jar foundations04.jar

kotlinc foundations/05_loops_and_ranges.kt -include-runtime -d foundations05.jar
java -jar foundations05.jar

kotlinc foundations/06_functions_basics.kt -include-runtime -d foundations06.jar
java -jar foundations06.jar

kotlinc foundations/07_classes_and_objects.kt -include-runtime -d foundations07.jar
java -jar foundations07.jar

kotlinc foundations/08_exceptions_basics.kt -include-runtime -d foundations08.jar
java -jar foundations08.jar

kotlinc foundations/09_interfaces_and_inheritance.kt -include-runtime -d foundations09.jar
java -jar foundations09.jar
```
