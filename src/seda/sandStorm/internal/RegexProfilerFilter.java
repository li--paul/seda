package seda.sandStorm.internal;

import seda.sandStorm.api.SandstormConfigIF;
import seda.sandStorm.api.ProfilerFilterIF;
import seda.sandStorm.main.SandstormConfig;

import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * A ProfilerFilterIF implementation that use regular expressions, specified
 * in the given SandstormConfig, to accept and/or reject profilable objects.
 *
 * Accept regexes have precedence over reject ones if intersecting.
 * If no accept nor reject clause is specified, all profilable will be
 * accepted.
 *
 * By exemple, for rejecting all but 2 particular stages, you can add
 * in the configuration file, into the global.profile.filter section:
 * acceptregex ^(.*)(StageXXX)(.+)$ ^(.*)(StageYYY)(.+)$
 *
 * Note because the way SandstormConfig reads the configuration file,
 * we can't use spaces in regexes.
 *
 * @author Jean Morissette and others
 */
public class RegexProfilerFilter implements ProfilerFilterIF {

  HashSet includeNamePatterns = new HashSet();
  HashSet excludeNamePatterns = new HashSet();

  public void init(SandstormConfigIF config) {
    // TODO: use SandstormConfigIF instead of SandstormConfig
    readFilters((SandstormConfig)config);
  }

  public boolean isProfilable(String name) {
    if (includeNamePatterns.size() > 0) {
      for (Iterator it = includeNamePatterns.iterator(); it.hasNext();) {
        Pattern p = (Pattern)it.next();
        if (p.matcher(name).matches())
          return true;
      }
    }
    if (excludeNamePatterns.size() > 0) {
      for (Iterator it = excludeNamePatterns.iterator(); it.hasNext();) {
        Pattern p = (Pattern)it.next();
        if (p.matcher(name).matches())
          return false;
      }
    }
    return true;
  }

  private void readFilters(SandstormConfig cfg) {
    String[] ar = cfg.getStringList("global.profile.filter.acceptregex");
    for (int i = 0; ar != null && i <ar.length; i++){
      Pattern p = null;
      try {
        p = Pattern.compile(ar[i]);
      } catch (Exception e) { e.printStackTrace(); }
      if (p != null)
        includeNamePatterns.add(p);
    }
    String[] rr = cfg.getStringList("global.profile.filter.rejectregex");
    for (int i = 0; rr != null && i < rr.length; i++){
      Pattern p = null;
      try {
        p = Pattern.compile(rr[i]);
      } catch (Exception e) { e.printStackTrace(); }
      if (p != null)
        excludeNamePatterns.add(p);
    }
  }
}

