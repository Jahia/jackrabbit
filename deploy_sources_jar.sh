cd $1
mvn source:jar
mvn deploy:deploy-file -DgroupId=org.apache.jackrabbit -Durl=https://devtools.jahia.com/nexus/content/repositories/thirdparty-releases -DartifactId=$1 -Dversion=$2 -Dclassifier=sources -Dpackaging=jar -DrepositoryId=thirdparty-releases -Dfile=./target/$1-$2-sources.jar
