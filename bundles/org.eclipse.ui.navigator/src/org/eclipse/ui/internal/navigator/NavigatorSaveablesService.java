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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

	private final NavigatorContentService contentService;

	private static List helpers = new ArrayList();

	/**
	 * @param contentService
	 */
	public NavigatorSaveablesService(NavigatorContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * @param event
	 */
	/* package */static void bundleChanged(BundleEvent event) {
		for (Iterator it = helpers.iterator(); it.hasNext();) {
			SaveablesSourceHelper helper = (SaveablesSourceHelper) it.next();
			helper.recomputeSaveablesAndNotify(true);
		}
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
		SaveablesSourceHelper saveablesSourceHelper = new SaveablesSourceHelper(
				contentService, source, viewer, listener);
		add(saveablesSourceHelper);
		return saveablesSourceHelper;
	}

	/**
	 * @param saveablesSourceHelper
	 */
	private void add(SaveablesSourceHelper saveablesSourceHelper) {
		helpers.add(saveablesSourceHelper);
	}

	/**
	 * @param helper
	 */
	public static void remove(SaveablesSourceHelper helper) {
		helpers.remove(helper);
	}

}
