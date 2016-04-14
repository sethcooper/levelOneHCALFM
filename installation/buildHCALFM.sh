#!/bin/bash
GITREV=`git rev-parse HEAD | head -c 7`
ant -DgitRev="${GITREV}"
