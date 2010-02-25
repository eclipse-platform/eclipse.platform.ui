/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.forms.widgets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.widgets.Display;

/**
 * Class which can get the appropriate resource manager for a Display
 * In most applications there will be only one display but see
 * Bug 295981 for an example where this is not the case
 */

public class ResourceManagerManger {
	
	private HashMap resourceManagers;

	public LocalResourceManager getResourceManager(Display display) {
		if (resourceManagers == null) {
			resourceManagers = new HashMap();
		}
		LocalResourceManager resources = (LocalResourceManager)resourceManagers.get(display);
		if (resources == null) {
			pruneResourceManagers();
			resources = new LocalResourceManager(JFaceResources.getResources(display));
			resourceManagers.put(display, resources);
		}
		return resources;
	}

	private void pruneResourceManagers() {
		Set displays = resourceManagers.keySet();
		for (Iterator iter = displays.iterator(); iter.hasNext();) {
			Display display = (Display)iter.next();
			if (display.isDisposed()) {
				resourceManagers.remove(display);
				iter = displays.iterator();
			}
		}
	}
}
