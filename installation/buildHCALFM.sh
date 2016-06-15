#!/bin/bash
# Script to keep the HCALFM builds organized
# Production HCALFMs must be built from an unmodified git commit, and are indexed by their commit hash.
# Test HCALFMs are organized by date, appended with a version number.
# Usage: ./buildHCALFM.sh test
# The option can be either "test" or "release".
# John Hakala 4/14/2016

if [ "$1" = "release" ]; then
  git diff-index --quiet HEAD
  if [ "$?" = "0" ]; then
    GITREV=`git rev-parse HEAD | head -c 7`
    sed -i '$ d' ../gui/jsp/footer.jspf
    echo "<div id='hcalfmVersion'>HCALFM version: ${GITREV}</div>" >> ../gui/jsp/footer.jspf
    ant -DgitRev="${GITREV}"
  else
    echo "No changes since the last commit are permitted when building a release FM. Please commit your changes or stash them."
    exit 1
  fi
    
elif [ "$1" = "test" ]; then
  DATE=`date  +%m-%d-%y`
  ITERATION=1
  
  while [ -f jars/HCALFM_${DATE}_v${ITERATION}.jar ];
    do ITERATION=$(($ITERATION + 1))
  done
  sed -i '$ d' ../gui/jsp/footer.jspf
  echo "<div id='hcalfmVersion'>HCALFM version: ${DATE}_v${ITERATION}</div>" >> ../gui/jsp/footer.jspf
  ant -DgitRev="${DATE}_v${ITERATION}"

else 
  echo "Please run buildHCALFM with either the 'release' or 'test' option. Example:"
  echo "./buildHCALFM.sh test"
  exit 1

fi
