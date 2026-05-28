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
```
