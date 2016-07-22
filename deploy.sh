#!/bin/bash

#rm -rf $HOME/deploy
git clone -b deploy https://${GH_TOKEN}@github.com/${GH_ACCOUNT}/${GH_REPOSITORY} $HOME/deploy

#'convert' the repository to look like a local repository
find $HOME/deploy -name "maven-metadata.xml" -exec mv {} $(dirname {})/maven-metadata-local.xml \;

#use maven to package and install the artifact in the repository 
mvn package
mvn install:install-file -DpomFile=pom.xml -Dfile=$(ls target/*.jar) -DlocalRepositoryPath=$HOME/deploy

#'convert' the repository back to be a maven repository
find $HOME/deploy -name "maven-metadata-local.xml" -exec mv {} $(dirname {})/maven-metadata.xml \;

#commit and push the changes to the deploy branch
cd $HOME/deploy

git config --global user.email "travis@travis-ci.org"
git config --global user.name "travis-ci"

git add .
git commit -m "travis"
git push -f origin deploy
