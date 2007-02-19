/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * yyyymmdd bug      Email and other contact information
 * -------- -------- -----------------------------------------------------------
 * 20070201   154100 pmoogk@ca.ibm.com - Peter Moogk, Port internet code from WTP to Eclipse base.
 *******************************************************************************/

package org.eclipse.net.internal.ui;

import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Monitor content provider.
 */
public class NonProxyHostsContentProvider implements IStructuredContentProvider 
{
	/**
	 * MonitorContentProvider constructor comment.
	 */
	public NonProxyHostsContentProvider() 
	{
		super();
	}

	/*
	 * Disposes of this content provider.  
	 */
	public void dispose() 
	{
		// do nothing
	}

	/*
	 * Returns the elements to display in the viewer 
	 * when its input is set to the given element. 
	 */
	public Object[] getElements(Object inputElement) 
	{
		Collection coll = (Collection)inputElement;
    
		return coll.toArray( new String[0] );
	}

	/*
	 * Notifies this content provider that the given viewer's input
	 * has been switched to a different element.
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) 
	{
		// do nothing
	}
}
