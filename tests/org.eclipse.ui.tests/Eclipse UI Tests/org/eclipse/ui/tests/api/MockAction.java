/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.tests.TestPlugin;

public class MockAction extends Action {

	private boolean hasRun = false;

	/**
	 * Constructor for MockAction
	 */
	protected MockAction(String text) {
		super(text);
		TestPlugin plugin = TestPlugin.getDefault();
		setImageDescriptor(plugin.getImageDescriptor("anything.gif"));
		setToolTipText(text);
	}

	/**
	 * Constructor for MockAction
	 */
	protected MockAction(String text, ImageDescriptor image) {
		super(text, image);
		setToolTipText(text);
	}

	@Override
	public void run() {
		hasRun = true;
	}

	public void clearRun() {
		hasRun = false;
	}

	public boolean getRun() {
		return hasRun;
	}

}

