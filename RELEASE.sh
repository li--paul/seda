#!/bin/sh

# Generate a snapshot release of the 'mdw' directory

BASE_DIR=$HOME/src/releases
RELEASE=seda-release-`date +%Y%m%d`
RELEASE_DIR=$BASE_DIR/$RELEASE
PUBLIC_DIR=$HOME/public_html/proj/seda/

echo "Creating release in $RELEASE_DIR"

rm -rf $RELEASE_DIR
mkdir -p $RELEASE_DIR
cd $RELEASE_DIR
echo "Unpacking from CVS archive..."
CVSROOT=/home/cs/mdw/repository cvs -Q co mdw 
find . -name CVS | xargs rm -r

echo "Performing test build..."
cd mdw
make clean
export CLASSPATH=.:$RELEASE_DIR:$RELEASE_DIR/mdw/lib/collections.jar
export LD_LIBRARY_PATH=$RELEASE_DIR/mdw/lib
make || { echo "Build of release failed with errors, exiting"; exit 1; }
make clean

echo "Publishing javadoc documentation..."
cd $RELEASE_DIR/mdw/sandStorm/docs/javadoc
make || { echo "Build of release failed with errors, exiting"; exit 1; }
rm -rf $PUBLIC_DIR/javadoc
cd $RELEASE_DIR/mdw/sandStorm/docs
tar cf - javadoc | (cd $PUBLIC_DIR; tar xf -)
cd $RELEASE_DIR/mdw/sandStorm/docs/javadoc
make clean

echo "Creating $RELEASE.tar.gz..."
cd $BASE_DIR
tar cfz $RELEASE.tar.gz $RELEASE
rm -rf $RELEASE

echo "Copying $RELEASE.tar.gz to $PUBLIC_DIR..."
cp $RELEASE.tar.gz $PUBLIC_DIR
cd $PUBLIC_DIR
ln -sf $RELEASE.tar.gz seda-release-current.tar.gz

echo "Don't forget to tag CVS: cvs tag $RELEASE"
echo "Done."
