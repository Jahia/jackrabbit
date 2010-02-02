cd $1
mvn source:jar
mvn deploy:deploy-file -DgroupId=org.apache.jackrabbit -Durl=scpexe://maven.jahia.org/var/www/vhosts/maven.jahia.org/html/maven2 -DartifactId=$1 -Dversion=$2 -Dclassifier=sources -Dpackaging=jar -DrepositoryId=jahiaRepository -Dfile=./target/$1-$2-sources.jar
