#!/usr/bin/env bash
# Runs every scenario from the talk and saves the REAL terminal output to terminal/*.txt
# Needs: JDK 11+ and sbt on the PATH.  Pitfall 1 really sleeps for 1 minute.
set -u
cd "$(dirname "$0")"
mkdir -p terminal
run() {  # run <file> <command...>
  local out="terminal/$1.txt"; shift
  { echo "\$ $*"; "$@" 2>&1 | sed 's/\x1b\[[0-9;]*m//g'; } | tee "$out"
  echo "---- saved $out" >&2
}

# Naive
run 01-naive-expected      sbt "testOnly naive.NaiveExpectedSuite"
run 02-naive-time          sbt "testOnly naive.NaiveTimeSuite"   # really waits 1 minute
run 04-naive-bleed-alone   sbt "testOnly naive.NaiveStateBleedSuite -- --tests=.*empty.*"
run 05-naive-bleed-suite   sbt "testOnly naive.NaiveStateBleedSuite"
# Pitfall 3: the same test that is run on the refactored cache below (run it a few times)
for i in 1 2 3 4 5; do
  run "06-naive-concurrent-run$i" sbt "testOnly naive.NaiveConcurrentMissesSuite"
done

# Refactored
run 07-refactored-time       sbt "testOnly refactored.RefactoredTimeSuite"
for i in 1 2 3; do
  run "08-refactored-concurrent-run$i" sbt "testOnly refactored.RefactoredConcurrentSuite"
done
echo "Done. Send me the files in terminal/ and I will put them on the slides."
