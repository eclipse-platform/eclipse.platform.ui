package org.eclipse.ui.internal.views.markers;
/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 *     Andrey Loskutov <loskutov@gmx.de> - generified interface, bug 461762
 *******************************************************************************/

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * MarkerHelpAdapterFactory is the adapter factory for the markerHelp.
 * @since 3.4
 */
public class MarkerHelpAdapterFactory implements IAdapterFactory {

	private static final Class<?>[] classes = new Class[] { IContextProvider.class };

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (!(adaptableObject instanceof ExtendedMarkersView)) {
			return null;
		}

		final ExtendedMarkersView view = (ExtendedMarkersView) adaptableObject;

		return adapterType.cast(new IContextProvider() {

			@Override
			public int getContextChangeMask() {
				return SELECTION;
			}

			@Override
			public IContext getContext(Object target) {
				String contextId = null;
				// See if there is a context registered for the current selection
				IMarker[] markers = view.getSelectedMarkers();
				if(markers.length > 0) {
					contextId = IDE.getMarkerHelpRegistry().getHelp(markers[0]);
				}

				//TODO this needs to be migrated to the ide plug-in
				if (contextId == null) {
					contextId = PlatformUI.PLUGIN_ID + ".problem_view_context";//$NON-NLS-1$
				}

				return HelpSystem.getContext(contextId);
			}

			@Override
			public String getSearchExpression(Object target) {
				return null;
			}

		});
	}

	@Override
	public Class<?>[] getAdapterList() {
		return classes;
	}

}
