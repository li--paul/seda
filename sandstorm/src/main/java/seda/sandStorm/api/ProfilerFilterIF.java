package seda.sandStorm.api;

/**
 * A ProfilerFilterIF can be used to provide control over what
 * is profiled.
 *
 * The system ProfilerIF can have a filter associated with it.
 * The ProfilerIF will call the isProfilable method to check if
 * the given profilable should be profiled.
 *
 * @author Jean Morissette
 */
public interface ProfilerFilterIF {

  void init(SandstormConfigIF config);

  /**
   * Check if a given profilable should be profiled.
   * @param name the name of the object being profiled.
   * @return true if the value should be published.
   */
  boolean isProfilable(String name);

}