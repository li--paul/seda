/* 
 * Copyright (c) 2001 by Matt Welsh and The Regents of the University of 
 * California. All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice and the following
 * two paragraphs appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * Author: Matt Welsh <mdw@cs.berkeley.edu>
 * 
 */

package seda.sandStorm.internal;

import seda.sandStorm.api.*;
import seda.sandStorm.api.internal.*;
import seda.sandStorm.main.SandstormConfig;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * TPPThreadManager is a thread manager implementation which provides
 * one thread per CPU.
 * 
 * @author   Matt Welsh
 */

class TPPThreadManager implements ThreadManagerIF {

  private static final boolean DEBUG = false;
  private static final boolean DEBUG_VERBOSE = false;

  private int num_cpus, max_threads;
  private Vector stages;
  private Vector threads;
  private ThreadGroup tg;
  private boolean crashOnException;

  /**
   * An <code>Iterator</code> interface to the list of stages.
   * The {@link #schedule_queue} method uses this variable to implement
   * scheduling policies that are more interesting than round-robin.
   */
  private Iterator run_queue;
  /**
   * All accesses to <code>run_queue</code> must be guarded by this lock,
   * because the former variable may be overwritten.
   */
  private Object run_queue_lock = new Object();
    
  /**
   * Create an TPPThreadManager which attempts to schedule stages on
   * num_cpus CPUs, and caps its thread usage to max_threads.
   */
  TPPThreadManager(SandstormConfig config) {
    this.num_cpus = config.getInt("global.TPPTM.numCpus");
    this.max_threads = config.getInt("global.TPPTM.maxThreads");
    this.crashOnException = config.getBoolean("global.crashOnException");    

    stages = new Vector(1);
    threads = new Vector(num_cpus);

    tg = new ThreadGroup("TPPThreadManager");
    for (int i = 0; i < num_cpus; i++) {
      String name = new String("TPPTM-"+i);
      Thread t = new Thread(tg, new appThread(name), name);
      threads.addElement(t);
      t.start();
    }
  }

  /**
   * Register a stage with this thread manager.
   */
  public void register(StageWrapperIF stage) {
    synchronized (stages) {
      stages.addElement(stage);
      stages.notifyAll();
    } 
  }

  /**
   * Deregister a stage with this thread manager.
   */
  public void deregister(StageWrapperIF stage) {
    if (!stages.removeElement(stage)) throw new IllegalArgumentException("Stage "+stage+" not registered with this TM");
  }

  /**
   * Deregister all stage with this thread manager.
   */
  public void deregisterAll() {
    Enumeration e = stages.elements();
    while (e.hasMoreElements()) {
      StageWrapperIF stage = (StageWrapperIF)e.nextElement();
      deregister(stage);
    }
    tg.stop();
  }

  /**
   * Orders the stages and assigns <code>this.run_queue</code>
   * Currently implements round-robin with the stages ordered by the size
   * of their event queue.
   */
  protected void schedule_queue() {
      synchronized( run_queue_lock ) {
	  TreeMap sorted_queue = new TreeMap();

	  for (int i = 0; i < stages.size(); i++) {
	      StageWrapperIF s = (StageWrapperIF)stages.elementAt(i);
	      SourceIF src = s.getSource();
	      int size = src.size();
	      // Invert ordering so iterator produces decreasing order.
	      sorted_queue.put( new Integer( -1*size ), s );
	  }
	  this.run_queue = sorted_queue.values().iterator();
      }
  }

  /**
   * Internal class representing a single TPPTM-managed thread.
   */
  class appThread implements Runnable {

    private String name;

    appThread(String name) {
      this.name = name;
    }

    public void run() {
      System.err.println(name+": starting");

      while (true) {

	  // Wait until we have some stages 
	  if (stages.size() == 0) {
	    synchronized (stages) {
	      try {
		stages.wait();
	      } catch (InterruptedException ie) {
		// Ignore
	      }
	    }
	    continue;
	  }

	  // Now grab the next stage to run
	  StageWrapperIF s;
	  synchronized( run_queue_lock ) {
	      if( (run_queue == null) || (!run_queue.hasNext()) ) {
		  schedule_queue();
	      }
	      s = (StageWrapperIF)run_queue.next();
	  }

	try {
	    if (DEBUG_VERBOSE) System.err.println(name+": inspecting "+s);

	    SourceIF src = s.getSource();
	    QueueElementIF qelarr[] = src.dequeue_all();
	    if (qelarr != null) {
	      if (DEBUG) System.err.println(name+": dequeued "+qelarr.length+" elements for "+s);
	      EventHandlerIF handler = s.getEventHandler();
	      if( handler instanceof SingleThreadedEventHandlerIF ) {
		synchronized( s ) {
		  handler.handleEvents(qelarr);
		}
	      } else {
		handler.handleEvents(qelarr);		    
	      }
	      if (DEBUG) System.err.println(name+": returned from handleEvents for "+s);
	    } else {
	      if (DEBUG_VERBOSE) System.err.println(name+": got null on dequeue");
	    }

	} catch (Exception e) {
	  System.err.println("TPPThreadManager: appThread ["+name+"] got exception "+e);
	  e.printStackTrace();
	  if (crashOnException) {
	    System.err.println("Sandstorm: Crashing runtime due to exception - goodbye");
	    System.exit(-1);
	  }
	  
	}
      }
    }
  }

}

