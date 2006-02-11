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
package org.eclipse.jface.internal.databinding.nonapi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.internal.databinding.api.BindingEvent;
import org.eclipse.jface.internal.databinding.api.IBinding;
import org.eclipse.jface.internal.databinding.api.IBindingListener;

/**
 * @since 3.2
 *
 */
abstract public class Binding implements IBinding {

	protected final DataBindingContext context;

	/**
	 * @param context
	 */
	public Binding(DataBindingContext context) {
		this.context = context;
	}
	
	/**
	 * 
	 */
	abstract public void updateTargetFromModel();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IBinding#addBindingEventListener(org.eclipse.jface.databinding.IBindingListener)
	 */
	public void addBindingEventListener(IBindingListener listener) {
		bindingEventListeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IBinding#removeBindingEventListener(org.eclipse.jface.databinding.IBindingListener)
	 */
	public void removeBindingEventListener(IBindingListener listener) {
		bindingEventListeners.remove(listener);
	}
	
	private List bindingEventListeners = new ArrayList();
	
	protected String fireBindingEvent(BindingEvent event) {
		String result = null;
		for (Iterator bindingEventIter = bindingEventListeners.iterator(); bindingEventIter.hasNext();) {
			IBindingListener listener = (IBindingListener) bindingEventIter.next();
			result = listener.bindingEvent(event);
			if (result != null)
				break;
		}
		if (result == null)
			result = context.fireBindingEvent(event);
		return result;
	}
}
