#!/bin/sh

# Generate a snapshot release of the Haboob directory

BASE_DIR=$HOME/src/releases
RELEASE=haboob-snapshot-`date +%Y%m%d`
RELEASE_DIR=$BASE_DIR/$RELEASE
PUBLIC_DIR=$HOME/public_html/proj/seda/

echo "Creating release in $RELEASE_DIR"

rm -rf $RELEASE_DIR
mkdir -p $RELEASE_DIR
cd $RELEASE_DIR
echo "Unpacking from CVS archive..."
CVSROOT=ninja.cs:/disks/ninja/.CVS-ninja cvs -Q co ninja2/personal/mdw/apps/Haboob
find . -name CVS | xargs rm -rf
#rm -rf client/DATA-SOSP01

echo "Performing test build..."
cd ninja2/personal/mdw/apps/Haboob
make clean
export CLASSPATH=.:$RELEASE_DIR:$HOME/src:$HOME/src/mdw/lib/collections.jar
make || { echo "Build of release failed with errors, exiting"; exit 1; }
make clean

echo "Creating $RELEASE.tar.gz..."
cd $BASE_DIR
tar cfz $RELEASE.tar.gz $RELEASE
rm -rf $RELEASE

echo "Copying $RELEASE.tar.gz to $PUBLIC_DIR..."
cp $RELEASE.tar.gz $PUBLIC_DIR

echo "Don't forget to tag CVS: cvs tag $RELEASE"
echo "Done."
