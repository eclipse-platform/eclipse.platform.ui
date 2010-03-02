/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.jface.viewers.StructuredViewer;

/**
 * The AbstractProgressViewer is the abstract superclass of the viewers that
 * show progress.
 * 
 */
public abstract class AbstractProgressViewer extends StructuredViewer {

	/**
	 * Add the elements to the receiver.
	 * @param elements
	 */
	public abstract void add(Object[] elements);

	/**
	 * Remove the elements from the receiver.
	 * @param elements
	 */
	public abstract void remove(Object[] elements);
}
