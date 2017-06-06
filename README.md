# README



## build util model

cd unit

mvn clean install

## build SourceInstrument model

cd SourceInstrumt

mvn clean package

java -jar target\SourceInstrumt-1.0-SNAPSHOT-jar-with-dependencies.jar -i F:\FSE-Tools\LabDemo\demo\src\main\java -java-output F:\FSE-Tools\LabDemo\demo\src\main\java



## build History 

cd LogcatModel

mvn clean package

java -jar xxx.jar -logFile  xxxx -manifestFile xxxxx -apkFile xxxx







## 完整的记录



cd /mnt/f/Workspace/FaultProject/ 

F:

cd  F:\FSE-Tools

dir

cd 

cd FaultProject

cd units

mvn clean install



cd SourceInstrumt

mvn clean package

java -jar target\SourceInstrumt-1.0-SNAPSHOT-jar-with-dependencies.jar -i F:\FSE-Tools\LabDemo\demo\src\main\java -java-output F:\FSE-Tools\LabDemo\demo\src\main\java

java -jar SourceInstrumt-1.0-SNAPSHOT-jar-with-dependencies.jar -i F:\FSE-Tools\LabDemo\demo\src\main\java -java-output F:\FSE-Tools\LabDemo\demo\src\main\java







cd LogcatModel

mvn clean package

java -jar target\LogcatModel-1.0-SNAPSHOT-jar-with-dependencies.jar -logFile  F:\FSE-Tools\demo.logs -manifestFile F:\FSE-Tools\LabDemo\demo\src\main\AndroidManifest.xml -apkFile F:\FSE-Tools\LabDemo\demo\build\outputs\apk\debug\demo-debug.apk