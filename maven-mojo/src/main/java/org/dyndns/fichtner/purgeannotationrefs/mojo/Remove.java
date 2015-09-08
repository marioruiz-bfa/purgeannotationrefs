package org.dyndns.fichtner.purgeannotationrefs.mojo;

import org.apache.maven.plugins.annotations.Parameter;

public class Remove {

	@Parameter(property = PurgeAnnoRefsMojo.PAR + ".removes", required = true)
	public String regexp;

	@Parameter(property = PurgeAnnoRefsMojo.PAR + ".remove.removeFrom")
	public String[] removeFroms;

}
