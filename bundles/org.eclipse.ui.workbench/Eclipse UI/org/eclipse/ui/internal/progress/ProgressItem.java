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

import org.eclipse.swt.widgets.Composite;


/**
 * The ProgressItem is the element that is represented in the 
 * FullProgressViewer.
 *
 */
public class ProgressItem extends Composite{
	
	
	private JobTreeElement element;
	
	/**
	 * Create a new instance of the receiver with the 
	 * parent and style specified.
	 * @param parent
	 * @param style
	 */
	public ProgressItem(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Get the element we are representing.
	 * @return JobTreeElement
	 */
	public JobTreeElement getElement() {
		return element;
	}
	
	/**
	 * Set the element to represent.
	 * @param element
	 */
	public void setElement(JobTreeElement element) {
		this.element = element;
	}
	

}
