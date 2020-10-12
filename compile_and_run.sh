#!/bin/bash

javac -d out/ src/TripFinderAlgorithm/*.java

java -cp ./out TripFinderAlgorithm.TripGenerator $1


