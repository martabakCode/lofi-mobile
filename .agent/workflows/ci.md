---
description: Run full CI pipeline (Spotless, Unit Tests, Build)
---

1. Run Spotless to check/apply code formatting
// turbo
./gradlew spotlessApply

2. Run all unit tests
// turbo
./gradlew test

3. Build the application
// turbo
./gradlew assembleDebug
