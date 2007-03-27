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
package org.eclipse.debug.internal.ui.model.elements;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.ui.IMemento;

/**
 * @since 3.3
 */
public abstract class ElementMementoProvider implements IElementMementoProvider {


	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#compareElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest[])
	 */
	public void compareElements(final IElementCompareRequest[] requests) {
		Job job = new Job("compare element") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				for (int i = 0; i < requests.length; i++) {
					IElementCompareRequest request = requests[i];
					try {
						request.setEqual(isEqual(request.getElement(), request.getMemento(), request.getPresentationContext()));
					} catch (CoreException e) {
						request.setStatus(e.getStatus());
					}
					request.done();
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		// TODO: rule
		job.schedule();
	}

	/**
	 * Returns whether the memento represents the given element.
	 * 
	 * @param element the element to compare to the memento
	 * @param memento memento
	 * @param context the context the compare is in
	 * @return whether the memento represents the given element
	 */
	protected abstract boolean isEqual(Object element, IMemento memento, IPresentationContext context) throws CoreException;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#encodeElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest[])
	 */
	public void encodeElements(final IElementMementoRequest[] requests) {
		Job job = new Job("encode element") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				for (int i = 0; i < requests.length; i++) {
					IElementMementoRequest request = requests[i];
					try {
						if (!encodeElement(request.getElement(), request.getMemento(), request.getPresentationContext())) {
							request.cancel();
						}
					} catch (CoreException e) {
						request.setStatus(e.getStatus());
					}
					request.done();
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		// TODO: rule
		job.schedule();
	}
	
	/**
	 * Encodes the specified element into the given memento.
	 * Returns whether the element could be encoded
	 * 
	 * @param element the element to encode
	 * @param memento the memento to write to 
	 * @param context presentation context
	 * @return false if cancelled/not supported
	 * @throws CoreException
	 */
	protected abstract boolean encodeElement(Object element, IMemento memento, IPresentationContext context) throws CoreException;

}
