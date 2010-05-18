/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IMemento;

/**
 * @since 3.3
 */
class ElementMementoRequest extends MementoUpdate implements IElementMementoRequest {
	
	private IMementoManager fManager;
	private ModelDelta fDelta;

	/**
	 * @param context
	 * @param element
	 * @param memento
	 */
	public ElementMementoRequest(ModelContentProvider provider, Object viewerInput, IMementoManager manager, IPresentationContext context, Object element, TreePath elementPath, IMemento memento, ModelDelta delta) {
		super(provider, viewerInput, context, element, elementPath, memento);
		fManager = manager;
		fDelta = delta;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	public void done() {
		if (!isCanceled() && (getStatus() == null || getStatus().isOK())) {
			// replace the element with a memento
			fDelta.setElement(getMemento());
		}
		fManager.requestComplete(this);
	}

	public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("IElementMementoRequest: "); //$NON-NLS-1$
        buf.append(getElement());
        return buf.toString();
	}
}
