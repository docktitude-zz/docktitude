#!/bin/sh -

gradle clean build buildDeb buildRpm shadowJar buildBinary
#pandoc -s --latex-engine=xelatex -o docktitude.pdf README.md
