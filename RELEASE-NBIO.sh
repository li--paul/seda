#!/bin/sh

# Generate a snapshot release of the 'mdw/nbio' directory

RELEASE_VERSION=1.5
BASE_DIR=$HOME/src/releases
RELEASE=nbio-$RELEASE_VERSION
RELEASE_DIR=$BASE_DIR/$RELEASE
PUBLIC_DIR=$HOME/public_html/proj/java-nbio/

echo "Creating release in $RELEASE_DIR"

rm -rf $RELEASE_DIR
mkdir -p $RELEASE_DIR
cd $RELEASE_DIR
echo "Unpacking from CVS archive..."
#CVSROOT=ninja.cs:/disks/ninja/.CVS-ninja cvs -Q co mdw/nbio mdw/lib mdw/Makefile mdw/Makefile.include
CVSROOT=/home/cs/mdw/repository cvs -Q co mdw/nbio mdw/lib mdw/Makefile mdw/Makefile.include
find . -name CVS | xargs rm -r

echo "Performing test build..."
cd mdw
make clean
rm $RELEASE_DIR/mdw/lib/*
export CLASSPATH=.:$RELEASE_DIR
export LD_LIBRARY_PATH=$RELEASE_DIR/mdw/lib
make || { echo "Build of release failed with errors, exiting"; exit 1; }
make clean
rm $RELEASE_DIR/mdw/lib/*
rm -rf $RELEASE_DIR/mdw/nbio/test/bench

echo "Publishing javadoc documentation..."
cd $RELEASE_DIR/mdw/nbio/javadoc
make || { echo "Build of docs failed with errors, exiting"; exit 1; }
rm -rf $PUBLIC_DIR/javadoc
cd $RELEASE_DIR/mdw/nbio
tar cf - javadoc | (cd $PUBLIC_DIR; tar xf -)
cd $RELEASE_DIR/mdw/nbio/javadoc
make clean

echo "Creating $RELEASE.tar.gz..."
cd $BASE_DIR
tar cfz $RELEASE.tar.gz $RELEASE
rm -rf $RELEASE

echo "Copying $RELEASE.tar.gz to $PUBLIC_DIR..."
cp $RELEASE.tar.gz $PUBLIC_DIR

#echo "Don't forget to tag CVS: cvs tag $RELEASE"
#echo "Done."
