javac .\src\*
mkdir .\bin\
move /y .\src\*.class .\bin\
copy /y XL_Example.csv .\bin\
cd bin
java Server
