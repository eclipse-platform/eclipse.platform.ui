/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ant.internal.core.ant;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

public class ProgressBuildListener implements BuildListener {

	protected IProgressMonitor monitor;

public ProgressBuildListener(IProgressMonitor monitor) {
	this.monitor = Policy.monitorFor(monitor);
}

public void buildStarted(BuildEvent event) {
	checkCanceled();
}

public void buildFinished(BuildEvent event) {
	monitor.done();
}

public void targetStarted(BuildEvent event) {
	checkCanceled();
}

public void targetFinished(BuildEvent event) {
	checkCanceled();
	monitor.worked(1);
}

public void taskStarted(BuildEvent event) {
	checkCanceled();
}

public void taskFinished(BuildEvent event) {
	checkCanceled();
}

public void messageLogged(BuildEvent event) {
}

protected void checkCanceled() {
	if (monitor.isCanceled())
		throw new OperationCanceledException(Policy.bind("exception.canceled")); //$NON-NLS-1$
}
}