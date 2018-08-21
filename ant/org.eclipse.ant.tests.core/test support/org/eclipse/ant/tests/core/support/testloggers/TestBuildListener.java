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

	/**
	 * @see org.apache.tools.ant.BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)
	 */
	@Override
	public void buildFinished(BuildEvent event) {
		// do nothing
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
	 */
	@Override
	public void buildStarted(BuildEvent event) {
		AntTestChecker.getDefault().addNameOfListener(this.getClass().getName());
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#messageLogged(org.apache.tools.ant.BuildEvent)
	 */
	@Override
	public void messageLogged(BuildEvent event) {
		// do nothing
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#targetFinished(org.apache.tools.ant.BuildEvent)
	 */
	@Override
	public void targetFinished(BuildEvent event) {
		// do nothing
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
	 */
	@Override
	public void targetStarted(BuildEvent event) {
		// do nothing
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#taskFinished(org.apache.tools.ant.BuildEvent)
	 */
	@Override
	public void taskFinished(BuildEvent event) {
		// do nothing
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#taskStarted(org.apache.tools.ant.BuildEvent)
	 */
	@Override
	public void taskStarted(BuildEvent event) {
		// do nothing
	}
}
