package seda.sandStorm.internal;

import seda.sandStorm.api.ManagerIF;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Simple file profiler handler that write to a file specified
 * in SandstormConfigIF. 
 *
 * @author Jean Morissette
 */
public class FileProfilerHandler extends StreamProfilerHandler {

  public void init(ManagerIF mgr, int delay) {
    try {
      String filename = mgr.getConfig().getString("global.profiler.filename");
      FileOutputStream out = new FileOutputStream(filename);
      setOutputStream(out, false);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}

