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

package org.eclipse.ui.navigator;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.ui.ISaveablesLifecycleListener;

/**
 * Used to create SaveablesProvider objects for content providers
 * contributed by navigatorContent elements in extensions to the
 * org.eclipse.ui.navigator.navigatorContent extension point. Subclasses are
 * registered using the org.eclipse.ui.navigator.saveableModelProviderService
 * extension point.
 * 
 * <p>
 * The navigator content IDs used when registering the service refer to the IDs
 * of navigatorContent elements defined in navigator content extensions for
 * which this SaveablesProviderFactory can return SaveablesProvider
 * objects.
 * </p>
 * <p>
 * Intended to be subclassed by clients.
 * </p>
 * 
 * @since 3.2
 * 
 * @deprecated
 * 
 */
public abstract class SaveablesProviderFactory {

	/**
	 * Returns a SaveablesProvider for the given content provider and
	 * listener, or null if there is no SaveablesProvider associated with
	 * the content provider. Callers are responsible for disposing of the
	 * returned odel provider.
	 * 
	 * @param contentProvider
	 * @param listener
	 *            a listener that must be notified of changes to the state of
	 *            the returned SaveablesProvider
	 * @return a SaveablesProvider
	 */
	public abstract SaveablesProvider createSaveablesProvider(
			IContentProvider contentProvider, ISaveablesLifecycleListener listener);

}
