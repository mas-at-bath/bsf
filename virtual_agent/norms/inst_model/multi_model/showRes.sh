#!/bin/bash
cat results.txt | tr " " "\n" | grep "$1"
