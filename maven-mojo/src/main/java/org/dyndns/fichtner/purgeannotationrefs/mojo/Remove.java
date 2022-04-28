package org.dyndns.fichtner.purgeannotationrefs.mojo;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

public class Remove {

  @Parameter(property = PurgeAnnoRefsMojo.PAR + ".remove.regexp", required = true)
  public String regexp;

  @Parameter(property = PurgeAnnoRefsMojo.PAR + ".remove.removeFroms")
  public List<String> removeFroms;

}
