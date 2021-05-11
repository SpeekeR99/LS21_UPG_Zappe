@mkdir .\bin
javac --release 8 lib\jfreechart-1.5.3.jar;lib\jfreesvg-4.2.jar;.\src -encoding UTF-8 -d .\bin src\*.java
