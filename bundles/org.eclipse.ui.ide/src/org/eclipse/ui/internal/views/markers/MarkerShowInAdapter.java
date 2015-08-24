/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrey Loskutov <loskutov@gmx.de> - generified interface, bug 461762
 *******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;

/**
 * MarkerShowInAdapter is the adapter for ExtendedMarkersViews to get an
 * IShowInSource.
 *
 * @since 3.4
 *
 */
public class MarkerShowInAdapter implements IAdapterFactory {

	private static Class<?>[] classes = new Class[] { IShowInSource.class };

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (!(adaptableObject instanceof ExtendedMarkersView)) {
			return null;
		}

		final ExtendedMarkersView view = (ExtendedMarkersView) adaptableObject;

		return adapterType.cast((IShowInSource) () -> {
			IMarker[] markers = view.getSelectedMarkers();
			Collection<IResource> resources = new HashSet<>();
			for (int i = 0; i < markers.length; i++) {
				resources.add(markers[i].getResource());
			}
			return new ShowInContext(view.getViewerInput(), new StructuredSelection(resources.toArray()));
		});

	}

	@Override
	public Class<?>[] getAdapterList() {
		return classes;
	}

}
