SEDA: An Architecture for Highly Concurrent Server Applications
---------------------------------------------------------------

originally by Matt Welsh, Harvard University

See:
http://www.eecs.harvard.edu/~mdw/proj/seda/
http://seda.sourceforge.net


This github repo is an attempt at updating SEDA to a modern JDK 
and buildsystem, and possibly also updating the thread messaging 
to java.nio or something like Disruptor or MentaQueue.


While this still uses NBIO, to run, do:

    gradle build
    cd scripts
    ./sandstorm examples/src/main/java/seda/examples/basic/sandstorm.cfg
