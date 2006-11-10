/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.ui.IMemento;

/**
 * @since 3.3
 */
class ElementCompareRequest extends MementoUpdate implements IElementCompareRequest {

	private boolean fEqual;
	private ModelDelta fDelta;
	/**
	 * @param context
	 * @param element
	 * @param memento
	 */
	public ElementCompareRequest(ModelContentProvider provider, Object element, IMemento memento, ModelDelta delta) {
		super(provider, provider.getPresentationContext(), element, memento);
		fProvider = provider;
		fDelta = delta;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest#setEqual(boolean)
	 */
	public void setEqual(boolean equal) {
		fEqual = equal;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	public void done() {
		if (isEqual()) {
			fDelta.setElement(getElement());
			fProvider.doRestore(fDelta);
		}
	}
	
	boolean isEqual() {
		return fEqual;
	}

}
