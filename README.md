# Project



Welcome to my project page for the Jetbrain internship test assignment ,

So my selected project is : 

```
If you are aiming for the **Machine Learning model for in-IDE type hint suggestion in TypeScript** project, this is your task:

\----------------------------------------------------------

### Coding challenge/Test assignment

Implement a CLI tool in Java or Kotlin that runs inference using published pre-trained LambdaNet model \w user-defined types though the Java API. The tool should receive a path to the pre-trained model, a path to the parsingFromFile.ts (already pre-compiled with tsc) and an input Typescript file as named CLI arguments and print the results to stdout. 



Its output should include stats of the loaded model (on-disk/RAM size, number of trainable parameters) and all the predictions for the given file. Think about a user-friendly way to show the predictions (and errors, if any) that includes: 

- a specific line of code
- a place in that line, where the type suggestion should be added
- Top5 most probably model type suggestions



The solution should be presented as a public git repository (e.g hosted on Github). The repository should only include your source code and a description of how to run it with java -jar, in a minimal number of steps, on a computer that has only the JVM and Node installed. Compile-time dependencies should be downloaded automatically if they are published, otherwise a manual installation should be documented, along with any run-time dependency that may be needed to run your tool.

\---------------------------------------------------------- 
```



So this repository is my version of this project, it needs 3 arguments, the model folder with a prams.serialized and model.serialized file, a path to a parsingFromFile.ts compiled so to parsingFromFile.js and a path to a typescript project . 

This use Lambda net, after all the process done by lambda net, you will have a prompt to see the statistics or the result line by line. 

# Version :

Java version used is **java 14**, if you are on windows this is not a problem if you run it with the [first solution](#WindowsSol)  .



So you have 3 solution to run this program :

## <a id="WindowsSol"> </a>On Windows

Download the .zip file available on the release page or a the link below : 

https://github.com/pa1007/TestAssignementJetbrain/tags/



1. Unzip it in a directory 

2. Launch a terminal and go to this directory 

3. Type in the terminal : 

   Assignment.exe {Path to the model Folder} {Path to the parsingFromFile.ts already compiled} {Input typescript project directory} 


   Example : 

   ```bash
   Assignment.exe example/models/testModel example/script/ts/parsingFromFile.js example/data/ts/
   ```

   



## On Linux or Windows (if the first solution doesn't work) :

Download the .zip file available on the release page or a the link below : 

https://github.com/pa1007/TestAssignementJetbrain/tags/



1. Unzip it in a directory 

2. launch a terminal and go to this directory

3. Type this command in a terminal and add the paths of the files :

   ```
   java -Xms2G -Xmx5G -Dorg.bytedeco.javacpp.maxbytes=7G -Dorg.bytedeco.javacpp.maxphysicalbytes=13G -jar libs/Assignement.jar {Path to the model Folder} {Path to the parsingFromFile.ts already compiled} {Input typescript project directory} 
   ```

    Example : 
   
      ```bash
   java -Xms2G -Xmx5G -Dorg.bytedeco.javacpp.maxbytes=7G -Dorg.bytedeco.javacpp.maxphysicalbytes=13G -jar Assignement.jar example/models/testModel example/script/ts/parsingFromFile.js example/data/ts/
      ```
   
      



## Last solution if neither works

1. Install sbt from the link : https://www.scala-sbt.org/download.html

2. Clone the project 

3. in the project directory run sbt command:

   ```
   sbt
   ```

4. Wait that sbt load the project and then run 

   ```
   runMain dev.pa1007.app.Main {Path to the model Folder} {Path to the parsingFromFile.ts already compiled} {Input typescript project directory}
   ```

   For example : 

   ```
   runMain dev.pa1007.app.Main example/models/testModel example/script/ts/parsingFromFile.js example/data/ts/
   ```

   

## Thank you

Thank you from taking the time to read and try my fun project, I learned a lot from working with those technologies .