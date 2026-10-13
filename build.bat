@echo off
echo =^> Compiling Connect Dots...
if not exist out mkdir out
javac -d out src\connectdots\*.java
if errorlevel 1 (
    echo Compilation FAILED.
    pause
    exit /b 1
)
echo =^> Compilation successful. Launching game...
java -cp out connectdots.Main
