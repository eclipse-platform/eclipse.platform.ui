/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.resources;

import org.eclipse.core.runtime.CoreException;

/**
 */
public class SimpleNature extends TestNature {
	private static SimpleNature instance;
	public boolean wasConfigured;
	public boolean wasDeconfigured;

	/**
	 * Returns the instance of this nature.
	 */
	public static SimpleNature getInstance() {
		if (instance == null)
			new SimpleNature();
		return instance;
	}

	/**
	 * Constructor for SimpleNature.
	 */
	public SimpleNature() {
		super();
		instance = this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		super.configure();
		wasConfigured = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		super.deconfigure();
		wasDeconfigured = true;
	}

	/**
	 * Resets validation flags
	 */
	public void reset() {
		wasConfigured = false;
		wasDeconfigured = false;
	}
}
