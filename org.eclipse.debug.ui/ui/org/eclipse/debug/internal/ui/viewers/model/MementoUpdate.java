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

import org.eclipse.debug.internal.ui.commands.actions.AbstractRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IMemento;

/**
 * @since 3.3
 */
abstract class MementoUpdate extends AbstractRequestMonitor implements IViewerUpdate {
	
	private IPresentationContext fContext;
	private Object fElement;
	private IMemento fMemento;
	protected ModelContentProvider fProvider;
	
	/**
	 * Constructs a viewer state request.
	 * 
	 * @param viewer viewer
	 * @param element element
	 * @param memento memento
	 */
	public MementoUpdate(ModelContentProvider provider, IPresentationContext context, Object element, IMemento memento) {
		fContext = context;
		fElement = element;
		fMemento = memento;
		fProvider = provider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getPresentationContext()
	 */
	public IPresentationContext getPresentationContext() {
		return fContext;
	}
	
	public Object getElement() {
		return fElement;
	}
	
	public IMemento getMemento() {
		return fMemento;
	}
	
	public ModelContentProvider getContentProvider() {
		return fProvider;
	}

	public Object getElement(TreePath path) {
		return fProvider.getElement(path);
	}
	
}
