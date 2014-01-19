#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

cd $DIR/jni
ndk-build
cd $DIR
mkdir -p res/raw/

cd $DIR/libs
ARCHS=$(ls)

cd $DIR

for ARCH in $ARCHS
do
    echo "Working for $ARCH"
    NORM_ARCH=$(echo $ARCH | tr '-' '_')

    for FILE in $DIR/libs/$ARCH/*
    do
        echo "Coping $FILE in $DIR/res/raw/$(basename $FILE)_$NORM_ARCH"
        cp $FILE $DIR/res/raw/$(basename $FILE)_$NORM_ARCH
    done
done