package seda.sandStorm.internal;

import seda.sandStorm.api.ProfilerHandlerIF;

import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Stream based profiler handler. This is primarily intended
 * as a base class to be used in implementing other profiler
 * handlers.
 *
 * @author Jean Morissette
 */
public abstract class StreamProfilerHandler implements ProfilerHandlerIF {

  private PrintWriter writer;

  /**
   * @param autoFlush  automatic line flushing flag
   */
  protected void setOutputStream(OutputStream out, boolean autoFlush) {
    if (out == null) {
      throw new NullPointerException();
    }
    destroy();
    writer = new PrintWriter(out, autoFlush);
    writer.write(getHead());
  }

  protected String getHead() {
    return "##### Profiling started at " + new Date().toString();
//       + "\n##### Sample delay " + delay + " msec";
  }

  protected String getTail() {
    return "##### Profiling stoped at " + new Date().toString();
  }

  public void profilableAdded(String name) {
      writer.println("'" + name + "' registered");
  }

  public void profilableRemoved(String name) {
    writer.println("'" + name + "' unregistered");
  }

  public void profilablesSnapshot(int[] snapshot) {
    for (int i = 0; i < snapshot.length; i++) {
      writer.print(snapshot[i] + " ");
    }
    writer.println();
  }

  public void destroy() {
    if (writer != null) {
      writer.flush();
      writer.close();
      writer = null;
    }
  }

  public void flush() throws IOException {
    writer.flush();
  }
}

