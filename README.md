# Water Analytics Service and QRCode App

## Secure Query Service DEMO

[![IMAGE ALT TEXT HERE](http://img.youtube.com/vi/Hth-0bQnZ_Q/0.jpg)](http://www.youtube.com/watch?v=Hth-0bQnZ_Q)

## QRCode App DEMO

[![IMAGE ALT TEXT HERE](http://img.youtube.com/vi/sja-CciUnu4/0.jpg)](http://www.youtube.com/watch?v=sja-CciUnu4)

# Requirements
These are the following requirements:
* Play Framework 1.2.5
* Play requires Java 1.6.
To check that you have the latest JDK, please run:

>$java -version

To check the installed java versions on the machine, please run:

>$ls /usr/lib/jvm/

To change the active java version, please run:

>$export JAVA_HOME=/usr/lib/jvm/[your java version]/

>$export PATH=${JAVA_HOME}/bin:${PATH}


# Start and Stop the application
### Start
The application is play framework app to start it use the following command:

>$cd [location of the app]

>$[Path to Play Installation/play-1.2.5.3]/play run &> logger.log &

### Stop
The application runs in the background using port 8011.
Stopping the application can be done by killing the process id using that port.

To stop this application use the following command:

>$sudo netstat -tunlp | grep 8011

>$sudo kill -9 [Process ID]

