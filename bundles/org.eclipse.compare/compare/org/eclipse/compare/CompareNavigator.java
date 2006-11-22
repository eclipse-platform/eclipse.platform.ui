/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare;

import org.eclipse.compare.internal.Utilities;

/**
 * Supports cross-pane navigation through the differences contained in a {@link CompareEditorInput}
 * or a similar type of compare container.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * @see INavigatable
 * @since 3.3
 */
public class CompareNavigator implements ICompareNavigator {
	
	private Object[] fPanes;
	// Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
	private boolean fNextFirstTime= true;
	
	/**
	 * Create a navigator for navigating the given panes
	 * @param panes the panes to navigate.
	 */
	public CompareNavigator(Object[] panes) {
		fPanes= panes;
	}

	/**
	 * Return the set of panes that this navigator is navigating.
	 * The {@link INavigatable} is obtain from each pane using the
	 * adaptable mechanism.
	 * @return the set of panes that this navigator is navigating
	 */
	public Object[] getPanes() {
		return fPanes;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.ICompareNavigator#selectChange(boolean)
	 */
	public boolean selectChange(boolean next) {

		// Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
		if (next && fNextFirstTime && mustOpen()) {
			fNextFirstTime= false;
			if (openElement())
				return false;
		}
		
		// find most down stream CompareViewerPane
		int n= 0;
		INavigatable[] navigators= new INavigatable[4];
		for (int i= 0; i < fPanes.length; i++) {
			navigators[n]= getNavigator(fPanes[i]);
			if (navigators[n] != null)
				n++;
		}
		
		Object downStreamInput = null;						
		while (n > 0) {
			n--;
			if (navigators[n].getInput() == downStreamInput) {
				// Skip to up stream pane if it has the same input
				continue;
			}
			if (navigators[n].selectChange(next ? INavigatable.NEXT_CHANGE : INavigatable.PREVIOUS_CHANGE)) {
				// at end of this navigator
				downStreamInput = navigators[n].getInput();
				continue;
			}
			// not at end
			if (n + 1 < navigators.length && navigators[n+1] != null && navigators[n+1].getInput() != downStreamInput) {
				// The navigation has invoked a change in a downstream pane.
				// Set the selected change depending on the direction we are navigating
				navigators[n+1].selectChange(next ? INavigatable.FIRST_CHANGE : INavigatable.LAST_CHANGE);
			}
			return false;
		}
		
		return true;
	}
	
	private INavigatable getNavigator(Object object) {
		if (object == null)
			return null;
		Object data= Utilities.getAdapter(object, INavigatable.class);
		if (data instanceof INavigatable)
			return (INavigatable) data;
		return null;
	}
	
	/*
	 * Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
	 */
	private boolean mustOpen() {
		if (fPanes == null || fPanes.length == 0)
			return false;
		for (int i= 1; i < fPanes.length; i++) {
			Object pane= fPanes[i];
			INavigatable nav = getNavigator(pane);
			if (nav != null && nav.getInput() != null)
				return false;
		}
		return true;
	}
	
	/*
	 * Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
	 */
	private boolean openElement() {
		if (fPanes == null || fPanes.length == 0)
			return false;
		INavigatable nav = getNavigator(fPanes[0]);
		if (nav != null)
			return nav.openSelectedChange();
		return false;
	}

	/**
	 * Return whether a call to {@link ICompareNavigator#selectChange(boolean)} with the same parameter
	 * would succeed.
	 * @param next if <code>true</code> the next change is selected, otherwise the previous change
	 * @return whether a call to {@link ICompareNavigator#selectChange(boolean)} with the same parameter
	 * would succeed.
	 * @since 3.3
	 */
	public boolean hasChange(boolean next) {

		// find most down stream CompareViewerPane
		int n= 0;
		INavigatable[] navigators= new INavigatable[4];
		for (int i= 0; i < fPanes.length; i++) {
			navigators[n]= getNavigator(fPanes[i]);
			if (navigators[n] != null)
				n++;
		}
		
		Object downStreamInput = null;						
		while (n > 0) {
			n--;
			if (navigators[n].getInput() == downStreamInput) {
				// Skip to up stream pane if it has the same input
				continue;
			}
			if (navigators[n].hasChange(next ? INavigatable.NEXT_CHANGE : INavigatable.PREVIOUS_CHANGE)) {
				return true;
			}
			// at end of this navigator
			downStreamInput = navigators[n].getInput();
		}
		
		return false;
	}	
}
