/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.navigator.INavigatorSaveablesService;
import org.eclipse.ui.navigator.ISaveablesSourceHelper;
import org.osgi.framework.BundleEvent;

/**
 * @since 3.2
 * 
 */
public class NavigatorSaveablesService implements INavigatorSaveablesService {

	public NavigatorSaveablesService() {
	}

	/**
	 * @param event
	 */
	/* package */static void bundleChanged(BundleEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.INavigatorSaveablesService#createHelper(org.eclipse.ui.ISaveablesSource,
	 *      org.eclipse.jface.viewers.StructuredViewer,
	 *      org.eclipse.ui.ISaveablesLifecycleListener)
	 */
	public ISaveablesSourceHelper createHelper(ISaveablesSource source,
			StructuredViewer viewer, ISaveablesLifecycleListener listener) {
		return new SaveablesSourceHelper(source, viewer, listener);
	}

}
