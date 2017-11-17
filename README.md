# Project Luna - deprecated (will not update)

Project Luna is a free big data platform which providing support for building pipeline of Apache Hadoop Map Reduce jobs and Apache Spark jobs. Project Luna also provide an intuitive, interactive and highly customizable charts visualization tool for users to visualize and review data. 

The platform is written in Java, JavaScript and HTML, the pipeline workflow editor is implemented based on [the-graph](https://github.com/the-grid/the-graph), and the visualization tool is implemented based on [echarts](https://github.com/ecomfe/echarts).

## Features
+ Provide a GUI based pipeline workflow editor for users to build their pipeline.
+ Provide a GUI based interactive window to upload files and display error.
+ Workflow executor can dispatch jobs in the same stage in parallel.
+ Provide a GUI based customizable visualization tool.
+ Logs produced by user and programs will be stored in a place and ready to be reviewed for debugging.
+ Scalable software architecture for front end and back end design.

## Preparation
1. Install [Apahce Tomcat 8](http://tomcat.apache.org/).
2. Install [Eclipse](https://eclipse.org/).
3. Instal [M2Ecplise](http://www.eclipse.org/m2e/) on Eclipse.
4. Install [Apache Hadoop](http://hadoop.apache.org/).
5. Install [Apache Spark](http://spark.apache.org/).
6. Make sure you have these directories under your tomcat home.
    > ./$TOMCAT/webapps/ROOT/Luna/Logs

    > ./$TOMCAT/webapps/ROOT/Luna/MR-Jars

    > ./$TOMCAT/webapps/ROOT/Luna/Pending-Files

    > ./$TOMCAT/webapps/ROOT/Luna/Spark-Jars
6. Make sure your Eclipse is using Tomcat Installation Path as it's Tomcat Server. Otherwise the project doesn't have acess to above directories.
7. Make sure your Hadoop has already set up, ready to use and running in the background.
8. Make sure your Spark has already set up and ready to use.

## Installation
1. Import the project as 'Existing Maven Project' in your Eclipse.
2. Right click on the project -> 'Run as' -> 'Maven Install'.
3. Make sure Hadoop Home and Spark Home are correctly configured in the source code. This is important, you must change relevant strings to match your computer setting. Otherwise Map Reduce jobs and Spark jobs may not be able to execute.
4. Now you can compile the project by right click -> 'Run as' -> 'Run on Server' and select Apache Tomcat Server 8.0 which you installed for running this project.
5. You should go to [http://localthost:8080/Luna](http://localthost:8080/Luna) and see if it's online.

## Tutorial
#### Import Map Reduce Jar jobs, Spark jar jobs and Files
+ You should noticed there are three tabs on nav bar: 'Hadoop', 'Spark' and 'Files'.
+ These tabs provide upload functions for Map Reduce Jars, Spark Jars and Files correspondingly.
+ Simply drag jars or files into corresponding drop zone area, waiting them to be processed by Javascript. Click 'Upload' button when its appeared.
+ Map Reduce Jars, Spark Jars and Files will be stored in directories you prepared before. And Files will be put onto your HDFS server and your file system as well.

#### Run a Map Reduce Job or Spark Job in WorkFlow Editor
+ Go to 'WorkFlow' tab.
+ Click 'Add Map Reduce' or 'Add Spark' button on top left corner.
+ Fill in the form as you wish. NOTE: Parameters' order should be the same as you input them in regular command line.
+ If your **Spark Job** invlove file operation (Reading/Writing), you'll need put **'-f:'** before your file name.
+ Click 'Save' button in the form to save changes.
+ Right click the little left bubble of the job block you just created, click 'Export'. And you'll see the bubble is connected with a 'in' block. (**'in' block indicates the entry of the pipeline. Sholud only have one entry for a pipeline.**)
+ Do the same thing to the right bubble of the job block. You'll see the bubble is connected to a 'out' block. (**'out' block indicates the exit of the pipeline. Should only have one exit for a pipeline.**)
+ Click 'Run' button on the top left corner to submit the pipeline.
+ Error with explanation will popup if the pipeline definition is wrong.
+ If a long random string popup, it indicates the unique id of this submission. You can go to 'Logs' directory to find output and system logs.

#### Visualize your data
+ Go to 'Visualizer' tab.
+ Browse the left vertical nav bar and find the kind of graph you want to display.
+ Example will be loaded and you'll see code editor fulfilled with example code.
+ Convert and copy your data into the data area in the code editor so that you'll see visualized data on the right panel.
+ This [Spreadsheet Data Convert Tool](http://echarts.baidu.com/echarts2/doc/spreadsheet-en.html) made officially by Baidu Echarts might be useful for you.

## Known Issues and Limitations
+ Workflow front end doesn't have log display function.
+ Workflow front end doesn't have stop button for execution after submission.
+ 'Autolayout' button in workflow editor may turns the graph into some terrible graph.
+ The platform doesn't have file explorer for uploaded files and output.
+ The platform doesn't embed the data convert tool. The tool's link may be lost in the future.

## Credit
Echarts by Baidu - [https://github.com/ecomfe/echarts](https://github.com/ecomfe/echarts)

the-graph by the-grid - [https://github.com/the-grid/the-graph](https://github.com/the-grid/the-graph)

bootstrap-fileinput by Kartik Visweswaran - [https://github.com/kartik-v/bootstrap-fileinput](https://github.com/kartik-v/bootstrap-fileinput)

## License
MIT License

Copyright (c) 2016 Yancheng Zhu

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
