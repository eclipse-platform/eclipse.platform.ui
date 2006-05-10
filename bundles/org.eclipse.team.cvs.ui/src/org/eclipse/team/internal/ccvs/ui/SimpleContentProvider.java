/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

 
import org.eclipse.jface.viewers.*;

/**
 * A default content provider to prevent subclasses from
 * having to implement methods they don't need.
 */
public class SimpleContentProvider implements IStructuredContentProvider {

	/**
	 * SimpleContentProvider constructor.
	 */
	public SimpleContentProvider() {
		super();
	}
	
	/*
	 * @see SimpleContentProvider#dispose()
	 */
	public void dispose() {
	}
	
	/*
	 * @see SimpleContentProvider#getElements()
	 */
	public Object[] getElements(Object element) {
		return new Object[0];
	}
	
	/*
	 * @see SimpleContentProvider#inputChanged()
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
