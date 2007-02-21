/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.tweaklets;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * @since 3.3
 * 
 */
public class Tweaklets {

	private static Map defaults = new HashMap();
	private static Map tweaklets = new HashMap();

	public static void setDefault(Class definition, Object implementation) {
		defaults.put(definition, implementation);
	}
	
	public static Object get(Class definition) {
		Object result = tweaklets.get(definition);
		if (result == null) {
			result = createTweaklet(definition);
			if (result == null) {
				result = getDefault(definition);
			}
			Assert.isNotNull(result);
			tweaklets.put(definition, result);
		}
		return result;
	}

	/**
	 * @param definition
	 * @return
	 */
	private static Object getDefault(Class definition) {
		return defaults.get(definition);
	}

	/**
	 * @param definition
	 * @return
	 */
	private static Object createTweaklet(Class definition) {
		IConfigurationElement[] elements = Platform
				.getExtensionRegistry()
				.getConfigurationElementsFor("org.eclipse.ui.internalTweaklets"); //$NON-NLS-1$
		for (int i = 0; i < elements.length; i++) {
			if (definition.getName().equals(
					elements[i].getAttribute("definition"))) { //$NON-NLS-1$
				try {
					Object tweaklet = elements[i].createExecutableExtension("implementation"); //$NON-NLS-1$
					tweaklets.put(definition, tweaklet);
					return tweaklet;
				} catch (CoreException e) {
					StatusManager.getManager().handle(
							StatusUtil.newStatus(IStatus.ERROR,
									"Error with extension " + elements[i], e), //$NON-NLS-1$
							StatusManager.LOG);
				}
			}
		}
		return null;
	}

}
