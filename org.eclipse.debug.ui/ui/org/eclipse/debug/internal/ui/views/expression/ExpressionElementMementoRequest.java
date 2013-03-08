/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - Fix for viewer state save/restore [188704] 
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.expression;

import org.eclipse.debug.internal.core.commands.Request;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;

/**
 * @since 3.3
 */
public class ExpressionElementMementoRequest extends Request implements IElementMementoRequest {

	final ExpressionView fView;
	final IPresentationContext fContext;
	final Object fElement;
	IMemento fMemento;
	final String[] fWorkingSets;
	
	public ExpressionElementMementoRequest(ExpressionView view, IPresentationContext context, Object element, IMemento memento, String[] workingSets) {
		fView = view;
		fContext = context;
		fElement = element;
		fWorkingSets = workingSets;
		fMemento = memento;
	}

	public Object getElement() {
		return fElement;
	}

	public TreePath getElementPath() {
		return TreePath.EMPTY;
	}

	public IMemento getMemento() {
		return fMemento;
	}

	public Object getViewerInput() {
		return fElement;
	}
	
	public void done() {
		Display display = fView.getSite().getShell().getDisplay();
		if (display == null || display.isDisposed()) return;
		
		if (display.getThread() != Thread.currentThread()) {
			display.asyncExec(new Runnable() {
				public void run() {
					performFinished();
				}
			});
		} else {
			performFinished();
		}
	}	    
	
	protected void performFinished() {
		fView.mementoRequestFinished(this);	
	}
	
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("IElementCompareRequest: "); //$NON-NLS-1$
        buf.append(getElement());
        return buf.toString();
    }

    public IPresentationContext getPresentationContext() {
    	return fContext;
    }
    
    public String[] getWorkingSets() {
    	return fWorkingSets;
    }
}
