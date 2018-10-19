export PATH=/c/Program\ Files/Java/jdk-10.0.1/bin/:$PATH
javac -version
mkdir -p libs/stage2
cd stage2
#mvn dependency:copy-dependencies package -DincludeScope=compile -DexcludeGroupIds=org.scala-lang -DoutputDirectory=../libs/stage2 $*
mvn package
cp target/indesign-stage* ../libs/stage2
