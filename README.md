# Cats Ref cache demo

The code and tests from the talk, as a runnable sbt project. It has two versions of a TTL cache:

- **`naive`**: a global `Ref` with wall-clock time. Its tests show three common testing pitfalls.
- **`refactored`**: a cache you create per use, with an injected `Clock` and memoized computations. Its tests are fast and reliable.

## Requirements

- JDK 11+ (tested on Java 21)
- sbt 1.10.2 (set in `project/build.properties`)

Dependencies are Scala 2.13.14, cats-effect 3.5.4 and munit-cats-effect 2.0.0.

## Running

```bash
sbt test                 # all suites; takes about a minute because Pitfall 1 really sleeps
./run-scenarios.sh       # runs each scenario on its own and saves the output to terminal/*.txt
```

Run one suite, or one test inside a suite:

```bash
sbt "testOnly naive.NaiveExpectedSuite"
sbt "testOnly naive.NaiveStateBleedSuite -- --tests=.*empty.*"
```

Munit's `--tests` filter is a regex, not a glob.

## Scenarios

| Slide             | Suite                                | Expected result | Output file(s)                      |
|-------------------|--------------------------------------|-----------------|-------------------------------------|
| Expected test     | `naive.NaiveExpectedSuite`           | ✅ passes        | `01-naive-expected.txt`             |
| Pitfall 1: Time   | `naive.NaiveTimeSuite`               | ❌ fails after a real 1-minute sleep | `02-naive-time.txt` |
| Pitfall 2: Bleed  | `naive.NaiveStateBleedSuite`         | ✅ alone, ❌ with the whole suite | `04-naive-bleed-alone.txt`, `05-naive-bleed-suite.txt` |
| Pitfall 3: Race   | `naive.NaiveConcurrentMissesSuite`   | ❌ usually, but flaky | `06-naive-concurrent-run1…5.txt` |
| Controlled clock  | `refactored.RefactoredTimeSuite`     | ✅ passes, no sleep | `07-refactored-time.txt`         |
| Concurrent misses | `refactored.RefactoredConcurrentSuite` | ✅ passes every time | `08-refactored-concurrent-run1…3.txt` |

So a full `sbt test` is expected to report failures. The failing tests are the naive pitfalls.

### What each pitfall shows

1. **Time.** The test sleeps 1 real minute, which is shorter than the 5-minute TTL, so the entry hasn't expired and the assertion fails. Testing time this way is slow, and the result depends on how long you wait. `RefactoredTimeSuite` passes in a fake `Clock` and moves it forward 6 minutes instead.
2. **State bleed.** `CacheService.cacheRef` is one global `Ref`, created when the class loads. The "empty cache" test passes on its own but fails after "populates the cache" has run in the same JVM. The refactored cache is created with `CacheService.create`, so each test gets its own.
3. **Race.** Two concurrent misses for the same key can both compute a value, so the results differ. Because it is a race, the test does not fail every time: in the 4 Oct run, 3 of 5 runs failed. `RefactoredConcurrentSuite` runs the same test, and it passes because the refactored cache stores a memoized `IO` with `Ref.modify`.

## Project layout

```
src/main/scala/naive/CacheService.scala        global Ref, Instant.now()
src/main/scala/refactored/CacheService.scala   injected Clock, memoized entries, evictExpired, sweep
src/test/scala/naive/                          Expected test and Pitfalls 1–3
src/test/scala/refactored/                     the fixed versions
run-scenarios.sh                               runs each scenario and saves the output
terminal/                                      saved output quoted on the slides
```

`Test / parallelExecution := false` in `build.sbt` runs one suite at a time, so the naive version's global `Ref` is not shared between suites.

## Keeping the slides and code in sync

- Every code block in the deck is taken from these files (some are trimmed with `// ...`).
- The terminal slides quote line numbers from the test files, so edit those files in place and don't add or remove lines above the assertions.
- `evictExpired` and `sweep` (Cleanup slides) are in `refactored/CacheService.scala` but no test covers them.
- The current `terminal/` output was captured on 4 Oct 2026 with sbt 1.10.2 and Java 21. After changing any code, run `./run-scenarios.sh` again.

## License

MIT. See [LICENSE](LICENSE).
