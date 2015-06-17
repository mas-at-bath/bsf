#!/bin/bash
mencoder "mf://GapVidFrame*.png" -mf fps=1 -o test.avi -ovc lavc -lavcopts vcodec=mpeg4

