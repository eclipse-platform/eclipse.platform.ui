package org.eclipse.ant.internal.ui;

import org.apache.tools.ant.BuildException;

public class BuildCanceledException extends BuildException {

	public BuildCanceledException() {
		super("Canceled");
	}
}
