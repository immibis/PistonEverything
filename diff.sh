#!/bin/sh
SRCDIR=$1
TARGETDIR=$2
OUTPUTDIR=$3

echo "Generating patches for $TARGETDIR from $SRCDIR in $OUTPUTDIR..."
for FILE in $( cd $TARGETDIR && find . -name *.java ); do
    BASE=${FILE#${TARGETDIR}}
    BASE=${BASE#./}

    if [ -f "$SRCDIR/$BASE" ]; then
        mkdir -p "$OUTPUTDIR/${BASE%/*}" 
        echo "Making patch $OUTPUTDIR/${BASE%.*}.patch"
        diff -au --label $BASE "$SRCDIR/$BASE" --label $BASE "$TARGETDIR/$BASE" > "$OUTPUTDIR/${BASE%.*}.patch" 
    fi
done
exit 0
