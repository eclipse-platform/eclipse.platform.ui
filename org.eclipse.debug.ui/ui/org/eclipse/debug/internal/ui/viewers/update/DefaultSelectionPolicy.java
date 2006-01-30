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
package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelSelectionPolicy;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @since 3.2
 */
public class DefaultSelectionPolicy implements IModelSelectionPolicy {
	
	private IDebugElement fDebugElement;
	
	/**
	 * Constructs a new selection policy for the given debug
	 * element.
	 * 
	 * @param element
	 */
	public DefaultSelectionPolicy(IDebugElement element) {
		fDebugElement = element;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.IModelSelectionPolicy#contains(org.eclipse.jface.viewers.ISelection, org.eclipse.debug.ui.viewers.IPresentationContext)
	 */
	public boolean contains(ISelection selection, IPresentationContext context) {
		IWorkbenchPart part = context.getPart();
		if (part != null) {
			if (IDebugUIConstants.ID_DEBUG_VIEW.equals(part.getSite().getId())) {
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection ss = (IStructuredSelection) selection;
					Object element = ss.getFirstElement();
					if (element instanceof IDebugElement) {
						IDebugElement debugElement = (IDebugElement) element;
						return fDebugElement.getDebugTarget().equals(debugElement.getDebugTarget());
					}
				} 
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.IModelSelectionPolicy#overrides(org.eclipse.jface.viewers.ISelection, org.eclipse.jface.viewers.ISelection, org.eclipse.debug.ui.viewers.IPresentationContext)
	 */
	public boolean overrides(ISelection existing, ISelection candidate, IPresentationContext context) {
		IWorkbenchPart part = context.getPart();
		if (part != null) {
			if (IDebugUIConstants.ID_DEBUG_VIEW.equals(part.getSite().getId())) {	
				if (existing instanceof IStructuredSelection && candidate instanceof IStructuredSelection) {
					IStructuredSelection ssExisting = (IStructuredSelection) existing;
					IStructuredSelection ssCandidate = (IStructuredSelection) candidate;
					return overrides(ssExisting.getFirstElement(), ssCandidate.getFirstElement());
				}
			}
		}
		return true;
	}
	
	protected boolean overrides(Object existing, Object candidate) {
		if (existing == null) {
			return true;
		}
		if (existing.equals(candidate)) {
			return true;
		}
		if (existing instanceof IStackFrame && candidate instanceof IStackFrame) {
			IStackFrame curr = (IStackFrame) existing;
			IStackFrame next = (IStackFrame) candidate;
			return curr.getThread().equals(next.getThread()) || !isSticky(existing);
		}
		return !isSticky(existing);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.IModelSelectionPolicy#isSticky(org.eclipse.jface.viewers.ISelection, org.eclipse.debug.ui.viewers.IPresentationContext)
	 */
	public boolean isSticky(ISelection selection, IPresentationContext context) {
		IWorkbenchPart part = context.getPart();
		if (part != null) {
			if (IDebugUIConstants.ID_DEBUG_VIEW.equals(part.getSite().getId())) {	
				if (selection instanceof IStructuredSelection) {
					return isSticky(((IStructuredSelection)selection).getFirstElement());
				}
			}
		}
		return false;
	}
	
	protected boolean isSticky(Object element) {
		if (element instanceof IStackFrame) {
			IStackFrame frame = (IStackFrame) element;
			return frame.isSuspended();
		}
		return false;
	}

}
