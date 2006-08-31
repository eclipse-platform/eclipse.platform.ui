/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional;

import org.eclipse.jface.internal.databinding.provisional.observable.IDiff;
import org.eclipse.jface.internal.databinding.provisional.observable.IObservable;

/**
 * The event that is passed to a #bindingEvent method of an IBindingListener.
 * This class is not intended to be subclassed by clients.
 * 
 * @since 1.0
 * @deprecated use {@link org.eclipse.jface.databinding.BindingEvent} instead
 */
public class BindingEvent extends org.eclipse.jface.databinding.BindingEvent {
	
	/**
	 * (Non-API Method) Construct a BindingEvent.
	 * 
	 * @param model 
	 * @param target 
	 * @param diff
	 * @param copyType 
	 * @param pipelinePosition
	 *            The initial processing pipeline position.
	 */
	public BindingEvent(IObservable model, IObservable target, IDiff diff, int copyType,
			int pipelinePosition) {
		super(model, target, diff, copyType, pipelinePosition);
	}

}
