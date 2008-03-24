package org.eclipse.ui.internal.views.markers;
/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
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
 *
 */
public class MarkerHelpAdapterFactory implements IAdapterFactory {

	private static final Class[] classes = new Class[] {IContextProvider.class};

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (!(adaptableObject instanceof ExtendedMarkersView))
			return null;

		final ExtendedMarkersView view = (ExtendedMarkersView) adaptableObject;

		return new IContextProvider(){

			public int getContextChangeMask() {
				return SELECTION;
			}

			public IContext getContext(Object target) {
				String contextId = null;
				// See if there is a context registered for the current selection
				IMarker[] markers = view.getSelectedMarkers();
				if(markers.length > 0) {
					contextId = IDE.getMarkerHelpRegistry().getHelp(
							markers[0]);
				}

				//TODO this needs to be migrated to the ide plug-in
				if (contextId == null) 
					contextId = PlatformUI.PLUGIN_ID + ".problem_view_context";//$NON-NLS-1$
				
				return HelpSystem.getContext(contextId);
			}

			

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.help.IContextProvider#getSearchExpression(java.lang.Object)
			 */
			public String getSearchExpression(Object target) {
				return null;
			}
		
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return classes;
	}

}
