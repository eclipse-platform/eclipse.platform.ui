/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * The GenericListItem is the item for generic list viewers. It 
 * provides a composite which subclasses can populate.
 * <strong>NOTE</strong> This class is experimental and may change
 * without warning.
 * @since 3.1
 */
public abstract class GenericListItem {

	private Object element;

	/**
	 * Create a new instance of the receiver with element.
	 * @param wrappedElement
	 */
	public GenericListItem(Object wrappedElement) {
		super();
		element = wrappedElement;
	}

	/**
	 * Return the element being represented by the receiver.
	 * @return Object
	 */
	public Object getElement() {
		return element;
	}

	/**
	 * Get the control for the receiver.
	 * @return Control
	 */
	public abstract Control getControl();

	/**
	 * Dispose the receiver.
	 */
	public abstract void dispose();

	/**
	 * Create the control for the receiver in the parent.
	 * Set the color to be color.
	 * The control should be no longer be <code>null</code>
	 * after this method is called.
	 * @param parent
	 * @param color
	 */
	public abstract void createControl(Composite parent, Color color);
	
	/**
	 * Add the supplied mouse listener to any widgets that 
	 * can take mouse events. 
	 * @param listener
	 */
	public abstract void addMouseListener(MouseListener listener);
}
