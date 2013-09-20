#!/bin/sh
SRCDIR=$1
TARGETDIR=$2
OUTPUTDIR=$3

for FILE in $( cd $TARGETDIR && find . -name *.java ); do
    BASE=${FILE#${TARGETDIR}}
    

    if [ -f "$SRCDIR/$BASE" ]; then
        mkdir -p "$OUTPUTDIR/${BASE%/*}"
        echo "Making patch $OUTPUTDIR/${BASE%.*}.patch"
        diff -au "$SRCDIR/$BASE" "$TARGETDIR/$BASE" > "$OUTPUTDIR/${BASE%.*}.patch"
    fi
done
