package org.eclipse.ant.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.apache.tools.ant.BuildException;

public class BuildCanceledException extends BuildException {

public BuildCanceledException() {
	super(Policy.bind("exception.canceled"));
}

}
