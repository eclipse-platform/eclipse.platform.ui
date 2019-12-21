/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.core.support.testloggers;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.eclipse.ant.tests.core.testplugin.AntTestChecker;

public class TestBuildListener implements BuildListener {

	@Override
	public void buildFinished(BuildEvent event) {
		// do nothing
	}

	@Override
	public void buildStarted(BuildEvent event) {
		AntTestChecker.getDefault().addNameOfListener(this.getClass().getName());
	}

	@Override
	public void messageLogged(BuildEvent event) {
		// do nothing
	}

	@Override
	public void targetFinished(BuildEvent event) {
		// do nothing
	}

	@Override
	public void targetStarted(BuildEvent event) {
		// do nothing
	}

	@Override
	public void taskFinished(BuildEvent event) {
		// do nothing
	}

	@Override
	public void taskStarted(BuildEvent event) {
		// do nothing
	}
}
