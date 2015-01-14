/*******************************************************************************
 * Copyright (c) 2014, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.tests;

import org.osgi.framework.BundleContext;

import org.eclipse.core.runtime.Plugin;

public class EditorTestPlugin extends Plugin {
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public static String PLUGIN_ID= "org.eclipse.ui.editors.tests";

	// The shared instance
	private static EditorTestPlugin fgPlugin;

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static EditorTestPlugin getDefault() {
		return fgPlugin;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		fgPlugin= this;
	}

	public EditorTestPlugin() {
		super();
	}

}
