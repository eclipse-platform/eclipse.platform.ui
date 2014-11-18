/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.tests;

import java.io.BufferedInputStream;
import java.lang.reflect.Field;

import org.osgi.framework.BundleContext;

import org.eclipse.core.runtime.ContributorFactorySimple;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
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
		IExtensionRegistry registry= Platform.getExtensionRegistry();
		IContributor pointContributor= ContributorFactorySimple.createContributor(Long.toString(fgPlugin.getBundle().getBundleId()));

		try{
			BufferedInputStream bis= new BufferedInputStream(getClass().getResourceAsStream("plugin.xml"));

			Field field=
					org.eclipse.core.internal.registry.ExtensionRegistry.class
							.getDeclaredField("masterToken");
			field.setAccessible(true);
			Object masterToken= field.get(registry);
			registry.addContribution(bis, pointContributor, true, null, null, masterToken);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	public EditorTestPlugin() {
		super();
	}

}
