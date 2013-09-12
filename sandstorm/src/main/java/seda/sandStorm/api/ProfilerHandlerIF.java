package seda.sandStorm.api;

/**
 * A <tt>ProfilerHandlerIF</tt> object receives values from a
 * <tt>ProfilerIF</tt> and exports them.  It might for example,
 * show them graphically on the screen or write them to a file.
 */
public interface ProfilerHandlerIF {

  void init(ManagerIF mgr, int sampleDelay);

  /**
   * This method is called when a profilable object has been
   * added by the ProfilerIF.
   */
  void profilableAdded(String name);

  /**
   * This method is called when a profilable object has been
   * removed by the ProfilerIF.
   */
  void profilableRemoved(String name);

  /**
   * This method is called when the system profiler has profiled
   * the added profilable objects.  The given array is sorted in
   * order of which profilable objects has been added.
   */
  void profilablesSnapshot(int[] sizes);

  void destroy();

}
