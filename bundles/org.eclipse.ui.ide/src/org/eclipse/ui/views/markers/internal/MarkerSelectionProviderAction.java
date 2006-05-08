/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;

/**
 * MarkerSelectionProviderAction is the abstract super class of the
 * selection provider actions used by marker views.
 *
 */
public abstract class MarkerSelectionProviderAction extends SelectionProviderAction {

	/**
	 * Create a new instance of the receiver.
	 * @param provider
	 * @param text
	 */
	public MarkerSelectionProviderAction(ISelectionProvider provider, String text) {
		super(provider, text);
		
	}
	
	/**
	 * Get the selected markers in the receiver.
	 * @return IMarker[]
	 */
	IMarker[] getSelectedMarkers(){
		
		return getSelectedMarkers(getStructuredSelection());
	}

	/**
	 * Return the selected markers for the structured selection.
	 * @param structured IStructuredSelection
	 * @return IMarker[]
	 */
	IMarker[] getSelectedMarkers(IStructuredSelection structured) {
		Object[] selection = structured.toArray();
		ArrayList markers = new ArrayList();
		for (int i = 0; i < selection.length; i++) {
			Object object = selection[i];
			if(!(object instanceof MarkerNode)) {
				return new IMarker[0];//still pending
			}
			MarkerNode marker =(MarkerNode) object;
			if(marker.isConcrete()) {
				markers.add(((ConcreteMarker) object).getMarker());
			}
		}
		
		return (IMarker[]) markers.toArray(new IMarker[markers.size()]);
	}
	
	/**
	 * Get the selected marker in the receiver.
	 * @return IMarker
	 */
	IMarker getSelectedMarker(){
		
		ConcreteMarker selection = (ConcreteMarker) getStructuredSelection().getFirstElement();
		return selection.getMarker();
	}

}
