package org.dyndns.fichtner.purgeannotationrefs.mojo;

import java.util.List;

import org.apache.maven.plugins.annotations.Parameter;

public class Remove {

	@Parameter(property = PurgeAnnoRefsMojo.PAR + ".remove.regexp", required = true)
	public String regexp;

	@Parameter(property = PurgeAnnoRefsMojo.PAR + ".remove.removeFroms")
	public List<String> removeFroms;

}
