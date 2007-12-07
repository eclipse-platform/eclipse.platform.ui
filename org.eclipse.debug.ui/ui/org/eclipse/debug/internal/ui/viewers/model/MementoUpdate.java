/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import org.eclipse.debug.internal.core.commands.Request;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IMemento;

/**
 * @since 3.3
 */
abstract class MementoUpdate extends Request implements IViewerUpdate {
	
	private IPresentationContext fContext;
	private Object fElement;
	private TreePath fElementPath;
	private IMemento fMemento;
	protected ModelContentProvider fProvider;
	protected Object fViewerInput;
	
	/**
	 * Constructs a viewer state request.
	 * 
	 * @param viewer viewer
	 * @param element element
	 * @param memento memento
	 */
	public MementoUpdate(ModelContentProvider provider, Object viewerInput, IPresentationContext context, Object element, TreePath elementPath, IMemento memento) {
		fContext = context;
		fElement = element;
		fElementPath = elementPath;
		fMemento = memento;
		fProvider = provider;
		fViewerInput = viewerInput;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getPresentationContext()
	 */
	public IPresentationContext getPresentationContext() {
		return fContext;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getElement()
	 */
	public Object getElement() {
		return fElement;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getElementPath()
	 */
	public TreePath getElementPath() {
		return fElementPath;
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getViewerInput()
	 */
	public Object getViewerInput() {
		return fViewerInput;
	}
	
	
	
}
