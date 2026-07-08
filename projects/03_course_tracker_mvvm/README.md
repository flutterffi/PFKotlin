# 03 Course Tracker MVVM

This project is an advanced Kotlin app practice focused on MVVM-style state handling.

## Goal

Practice a more realistic app flow with:

- repository and fake remote data
- use cases for sync, status changes, and bookmarking
- a view model that owns query and filter state
- a console view that can be rendered independently

## Files

- `AppModels.kt`: shared app models and actions
- `DataLayer.kt`: repository and remote source
- `DomainLayer.kt`: use cases
- `PresentationLayer.kt`: view model and console rendering
- `Main.kt`: runnable app flow
- `ViewPreview.kt`: render-only preview entry for the view layer

## Commands

```bash
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

## Suggested exercises

1. Add a level filter beside the status filter.
2. Persist the last query inside a local text file.
3. Add a new action for clearing all bookmarks.
4. Replace the fake remote source with parsed local JSON input.
