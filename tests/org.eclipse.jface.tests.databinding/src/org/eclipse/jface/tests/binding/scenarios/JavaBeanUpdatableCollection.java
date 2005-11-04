/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.tests.binding.scenarios;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.eclipse.jface.databinding.IChangeEvent;
import org.eclipse.jface.databinding.IUpdatableCollection;
import org.eclipse.jface.databinding.Updatable;

/**
 * @since 3.2
 * 
 */
public class JavaBeanUpdatableCollection extends Updatable implements
		IUpdatableCollection {
	private final Object object;

	private Method getMethod;

	private Method setMethod;

	private PropertyChangeListener listener;

	private boolean updating = false;

	private PropertyDescriptor descriptor;

	public JavaBeanUpdatableCollection(Object object,
			PropertyDescriptor descriptor) {
		this.object = object;
		this.descriptor = descriptor;
		hookListener();
	}

	private void hookListener() {
		listener = new PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent event) {
				fireChangeEvent(IChangeEvent.CHANGE, event.getOldValue(), event
						.getNewValue(), -1);
			}
		};

		// See if the object implements the API for property change listener
	}

	public void dispose() {
		super.dispose();
	}

	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int addElement(Object value, int index) {
		updating = true;
		try {
			// TODO add element
			fireChangeEvent(IChangeEvent.CHANGE, null, null,
					IChangeEvent.POSITION_UNKNOWN);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			updating = false;
		}
		return 0;
	}

	public void removeElement(int index) {
		// TODO Auto-generated method stub

	}

	public void setElement(int index, Object value) {
		// TODO Auto-generated method stub

	}

	public Object getElement(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public Class getElementType() {
		// TODO Auto-generated method stub
		return null;
	}

}