export PATH=/c/Program\ Files/Java/jdk-10.0.1/bin/:$PATH
javac -version
mkdir -p libs/stage1
cd stage1 
mvn clean install $*
mvn dependency:copy-dependencies -DincludeScope=compile -DexcludeGroupIds=org.scala-lang -DoutputDirectory=../libs/stage1 $*
