/*******************************************************************************
 * Copyright (c) 2006 The Pampered Chef and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.compositetable.day.binding;

import org.eclipse.jface.databinding.observable.IObservable;
import org.eclipse.jface.internal.databinding.provisional.factories.IObservableFactory;

/**
 * An observable factory for EventEditorObservableLazyDataRequestors
 * 
 * @since 3.3
 */
public class EventEditorObservableLazyDataRequestorFactory implements
		IObservableFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.factories.IObservableFactory#createObservable(java.lang.Object)
	 */
	public IObservable createObservable(Object description) {
		if (description instanceof EventEditorBindingDescription) {
			return new EventEditorObservableLazyDataRequestor(
					(EventEditorBindingDescription) description);
		}
		return null;
	}

}
