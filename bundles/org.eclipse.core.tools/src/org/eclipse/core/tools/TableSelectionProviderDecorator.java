/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools;

import java.util.Enumeration;
import java.util.Iterator;
import org.eclipse.core.internal.events.EventStats;
import org.eclipse.jface.viewers.*;

/**
 * This class provides a decorator for ISelectionProviders that use
 * TableViewer as their viewer and PluginStats as the basis for their data
 * model. The only affected method is <code>getSelection()</code>, which will
 * return a string that closely resembles the table view of this object.
 * 
 */
public class TableSelectionProviderDecorator implements ISelectionProvider {

	/** The decorated selection provider. */
	private ISelectionProvider selectionProvider;

	/** 
	 * Constructs a <code>TableSelectionProviderDecorator</code> having
	 * the given selection provider as its decorated object.
	 * 
	 * @param selectionProvider the selection provider to be decorated
	 */
	public TableSelectionProviderDecorator(ISelectionProvider selectionProvider) {
		this.selectionProvider = selectionProvider;
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionProvider.addSelectionChangedListener(listener);
	}

	/**
	 * Returns the current selection for this provider. If the selection is a
	 * structured selection made of <code>PluginStats</code> elements, this method
	 * will return a structured selection of strings that resemble the table view
	 * of this data.
	 * 
	 * @return the current selection, printed in table view format
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		// gets the original selection object 
		ISelection selection = selectionProvider.getSelection();

		// in these cases the original selection will be returned
		if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection))
			return selection;

		// constructs a list with the selected elements 
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;

		StringBuffer copyText = new StringBuffer();
		copyText.append(headerPluginStats());
		for (Iterator selectionIter = structuredSelection.iterator(); selectionIter.hasNext();) {
			Object obj = selectionIter.next();
			if (obj instanceof EventStats) {
				copyText.append(prettyPluginStats((EventStats) obj));
				copyText.append("\n"); //$NON-NLS-1$
			}
		}
		return new StructuredSelection(copyText);

	}

	private String headerPluginStats() {
		String retString = ""; //$NON-NLS-1$
		retString += Policy.bind("stats.statIdHeader") + "\t"; //$NON-NLS-1$ //$NON-NLS-2$
		retString += Policy.bind("stats.numberOfNotificationsHeader") + "\t"; //$NON-NLS-1$ //$NON-NLS-2$
		retString += Policy.bind("stats.notifcationTimeHeader") + "\t"; //$NON-NLS-1$ //$NON-NLS-2$
		retString += Policy.bind("stats.numberOfBuildsHeader") + "\t"; //$NON-NLS-1$ //$NON-NLS-2$
		retString += Policy.bind("stats.buildTimeHeader") + "\t"; //$NON-NLS-1$ //$NON-NLS-2$
		retString += Policy.bind("stats.numberOfErrorsHeader") + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		return retString;
	}

	private String prettyPluginStats(EventStats stats) {
		String retString = ""; //$NON-NLS-1$
		retString += stats.getName() + "\t"; //$NON-NLS-1$
		retString += stats.getNotifyCount() + "\t"; //$NON-NLS-1$
		retString += stats.getNotifyRunningTime() + "\t"; //$NON-NLS-1$
		retString += stats.getBuildCount() + "\t"; //$NON-NLS-1$
		retString += stats.getBuildRunningTime() + "\t"; //$NON-NLS-1$
		for (Enumeration excepts = stats.getRuntimeExceptions(); excepts.hasMoreElements();) {
			Exception next = (Exception) excepts.nextElement();
			retString += next.toString() + "\n"; //$NON-NLS-1$
		}
		for (Enumeration excepts = stats.getCoreExceptions(); excepts.hasMoreElements();) {
			Exception next = (Exception) excepts.nextElement();
			retString += next.toString() + "\n"; //$NON-NLS-1$
		}
		return retString;
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionProvider.removeSelectionChangedListener(listener);
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
		selectionProvider.setSelection(selection);
	}

}