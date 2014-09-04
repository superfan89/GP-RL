GP-RL
=====

GP-RL is a simple DEMO of the GPSARSA reinforcement learning algorithm described in ''Reinforcement learning with Gaussian processes'' by Yaakov Engel.

In the learning environment, the agent is put into a `1.0 * 1.0` coutinuous 2D maze and keeps roaming by 0.1 per step until it reaches the goal. 

Under the default settings the agent gets `-1.0` reward in upon every step outside the goal reagion and gets `10.0` reward when reaching the goals. After that the agent is flung randomly to start a new episode.

The DEMO has a GUI which displays the agent's movement and the learnt strategy. There is also a console panel available for testing different learning parameters and change the shape of obstacles and goals.

Compile & Run
=============

The main RL algorithm is written in Scala and the GUI part in Java, and thus a JVM is needed for running the program. [SBT](http://www.scala-sbt.org/) needs to be installed for handling dependencis and compilation.

Use sbt `sbt compile ` to download the dependencies and compile the project and use `sbt run` to start the main GUI.

Note
====

[Breeze](https://github.com/scalanlp/breeze) is used for number crunching, which in turn calls [netlib-java](https://github.com/fommil/netlib-java) in doing linear algebra computation. Netlib-java can optionally choose to use native or Java implementation of BLAS and LAPACK, which can be specified by user in java command line arguments by setting `-Dcom.github.fommil.netlib.BLAS=com.github.fommil.netlib.F2jBLAS -Dcom.github.fommil.netlib.LAPACK=com.github.fommil.netlib.F2jLAPACK -Dcom.github.fommil.netlib.ARPACK=com.github.fommil.netlib.F2jARPACK`, and it will try to read these system properties in initialization. This feature however violates the requirements of Java applet for safety reasons. Thus the corresponding codes in `com.github.fommil.netlib.{BLAS, LAPACK, ARPACK}` must be set to the pure Java implementation (written in the `FALLBACK` fields) to be used in an applet. I managed to do this by recompiling netlib-java (only the `core` module is needed, and the Java code is generated from Fortran codes by plugin) using Maven and substitute `core-1.1.2.jar` in the local ivy2 repository, and the applet can be deployed in a browser.
