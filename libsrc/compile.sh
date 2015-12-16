#!/bin/bash
# Utilitiy to update all dependencies and build Soot and Booster


# Clear existing jar files
rm -rf ./libs
mkdir libs

# Build Heros
echo "Building Heros"
cd heros
#git pull
ant
cp heros-trunk.jar ../libs/
cd ..
echo "Done building Heros"

# Build Jasmin
echo "Building Jasmin"
cd jasmin
#git pull
ant barebones
ant jasmin-jar
cp libs/java_cup.jar ../libs/
cp lib/jasminclasses-*.jar ../libs/jasminclasses-custom.jar
cd ..
echo "Done building Jasmin"

# Build Soot
echo "Building Soot"
cd soot
#git pull
ant clean
ant barebones
ant fulljar
cp lib/soot-trunk.jar ../libs/
cd ..
echo "Done building soot"

# Building Booster
echo "Building TamiFlex-Booster"
cd ./tamiflex/Booster
#git pull
rm lib/soot-trunk.jar
rm lib/booster-trunk.jar
cp ../../libs/soot-trunk.jar lib/
cp ../../libs/java_cup.jar lib/
ant
cp lib/booster-trunk.jar ../../libs/
echo "Done building Booster"

# Done with everything
echo "Please find all the newly built jar files in the libs folder"
