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

import java.util.List;
import java.util.ArrayList;

/**
 * sandStormProfiler is an implementation of the ProfilerIF interface
 * for Sandstorm. It is implemented using a thread that periodically
 * samples the set of ProfilableIF's registered with it, and outputs
 * the profile to registered ProfilerHandlerIF.
 * *
 * @see ProfilerIF
 * @see ProfilableIF
 * @see ProfilerHandlerIF
 * @see ProfilerFilterIF
 * @author Matt Welsh and Jean Morissette
 */
class sandStormProfiler extends Thread implements sandStormConst, ProfilerIF {

  private int delay;
  private List<profile> profiles;
  private ProfilerFilterIF filter;
  private List<profile> filteredProfiles;
  private List<ProfilerHandlerIF> handlers;
  private ManagerIF mgr;
  private boolean enable = false;
  private StageGraph graphProfiler;

  sandStormProfiler(ManagerIF mgr) {
    this.mgr = mgr;
    handlers = new ArrayList<ProfilerHandlerIF>();
    profiles = new ArrayList<profile>();
    filteredProfiles = new ArrayList<profile>();

    graphProfiler = new StageGraph(mgr);
    SandstormConfigIF config = mgr.getConfig();
    delay = config.getInt("global.profile.delay");

    enable = config.getBoolean("global.profile.enable");
    if (enable) {
      String filterClassname = config.getString("global.profile.filter.class");
      String handlerClassname = config.getString("global.profile.handler.class");
      try {
        filter = (ProfilerFilterIF) Class.forName(filterClassname).newInstance();
        filter.init(config);
        ProfilerHandlerIF handler = (ProfilerHandlerIF) Class.forName(handlerClassname).newInstance();
        addHandler(handler);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    add("usedmem(kb)", new UsedMemory());
    add("freemem(kb)", new FreeMemory());
    add("totalmem(kb)", new TotalMemory());
  }

  /**
   * Returns true if the profiler is enabled.
   */
  public synchronized boolean enabled() {
    return enable;
  }

  public synchronized void setEnable(boolean value) {
    if (value != enable) {
      enable = value;
      if (enable)
        this.notify();  // wake up the profiler
    }
  }

  /**
   * Add a class to this profiler.
   */
  public synchronized void add(String name, ProfilableIF pr) {
    if (name == null || pr == null)
      return;
    profile p = new profile(name, pr);
    if (profiles.contains(p)) {
      System.err.println("Duplicate ProfilableIF name");
      return;
    }
    profiles.add(p);
    if (filter == null || filter.isProfilable(p.name)) {
      filteredProfiles.add(p);
      fireProfilableAdded(p.name);
    }
  }

  public synchronized void removeProfilable(String name) {
    if (name == null)
      return;
    profile dummy = new profile(name, null);
    if (profiles.remove(dummy)) {
      if (filteredProfiles.remove(dummy)) {
        fireProfilableRemoved(name);
      }
    }
  }

  public synchronized void addHandler(ProfilerHandlerIF handler) {
    if (handler == null)
      return;
    if (handlers.contains(handler)) {
      System.err.println("Duplicate ProfilerHandlerIF");
      return;
    }
    handler.init(mgr, delay);
    handlers.add(handler);
    for (int i = 0; i < filteredProfiles.size(); i++) {
      profile profile = (profile) filteredProfiles.get(i);
      handler.profilableAdded(profile.name);
    }
  }

  public synchronized void removeHandler(ProfilerHandlerIF handler) {
    if (handler == null)
      return;
    if (handlers.remove(handler))
      handler.destroy();
  }

  public void run() {
    while (true) {
      try {
        synchronized (this) {
          while (!enable) {
            wait();
          }

          // To have a snapshot of the system state, we call profileSize()
          // for all ProfilableIF objects in a very short amount of time.
          int size = filteredProfiles.size();
          int[] snapshot = new int[size];
          for (int i = 0; i < size; i++) {
            profile p = filteredProfiles.get(i);
            snapshot[i] = p.pr.profileSize();
          }
          fireProfilablesSnapshot(snapshot);
        }

        Thread.sleep(delay);
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    } //while true

  }

  public StageGraph getGraphProfiler() {
    return graphProfiler;
  }

  private void fireProfilableAdded(String name) {
    for (int i = 0; i < handlers.size(); i++) {
      ProfilerHandlerIF handler = handlers.get(i);
      handler.profilableAdded(name);
    }
  }

  private void fireProfilableRemoved(String name) {
    for (int i = 0; i < handlers.size(); i++) {
      ProfilerHandlerIF handler = handlers.get(i);
      handler.profilableRemoved(name);
    }
  }

  private void fireProfilablesSnapshot(int[] values) {
    for (int i = 0; i < handlers.size(); i++) {
      ProfilerHandlerIF handler = handlers.get(i);
      handler.profilablesSnapshot(values);
    }
  }

  class profile {
    String name;
    ProfilableIF pr;

    profile(String name, ProfilableIF pr) {
      this.name = name;
      this.pr = pr;
    }

    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof profile)) return false;

      final profile profile = (profile) o;

      return name.equals(profile.name);
    }

    public int hashCode() {
      return name.hashCode();
    }
  }

  class TotalMemory implements ProfilableIF {
    public int profileSize() {
      return (int) (Runtime.getRuntime().totalMemory() / 1024);
    }
  }

  class FreeMemory implements ProfilableIF {
    public int profileSize() {
      return (int) (Runtime.getRuntime().freeMemory() / 1024);
    }
  }

  class UsedMemory implements ProfilableIF {
    public int profileSize() {
      long totalmem = Runtime.getRuntime().totalMemory() / 1024;
      long freemem = Runtime.getRuntime().freeMemory() / 1024;
      return (int) (totalmem - freemem);
    }
  }

}
