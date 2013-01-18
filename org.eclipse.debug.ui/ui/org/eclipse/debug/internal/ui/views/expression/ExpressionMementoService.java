/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River - Pawel Piech - NPE when closing the Variables view (Bug 213719)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.expression;

import org.eclipse.debug.internal.ui.viewers.model.ViewerAdapterService;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate;

/**
 */
public class ExpressionMementoService {
	
    /**
     * An input object which will yield a null input element. 
     * 
     * @since 3.6
     */
    public final static Object NULL_INPUT = new IViewerInputProvider() {
        public void update(IViewerInputUpdate update) {
            update.setInputElement(null);
            update.done();
        }
    };
    
	// previous update request, cancelled when a new request comes in
	private ExpressionElementCompareRequest[] fPendingRequests = null;
	
	private int fOutstandingRequestCount = 0;
	
	private IElementCompareRequestor fRequestor = null;
	
	private ITreeModelViewer fViewer;
	
	/**
	 * Constructs a viewer input service for the given requester and presentation context.
	 * 
     * @param viewer for which inputs are required
	 * @param requestor client requesting viewer inputs 
	 */
	public ExpressionMementoService(ITreeModelViewer viewer, IElementCompareRequestor requestor) {
		fRequestor = requestor;
		fViewer = viewer;
	}
	
	/**
	 * Resolves a viewer input derived from the given source object.
	 * Reports the result to the given this service's requester. A requester may be called back
	 * in the same or thread, or asynchronously in a different thread. Cancels any previous
	 * incomplete request from this service's requester.
	 * 
	 * @param source source from which to derive a viewer input
	 */
	public void compareInputMementos(Object source, IElementCompareRequest[] requests) {
		IElementMementoProvider provdier = ViewerAdapterService.getMementoProvider(source);
		synchronized (this) {
			// cancel any pending update
			//cancelPendingUpdates();
			fOutstandingRequestCount = requests.length;
			fPendingRequests = new ExpressionElementCompareRequest[requests.length];
			for (int i = 0; i < requests.length; i++) {
				fPendingRequests[i] = new ExpressionElementCompareRequest(
						fViewer.getPresentationContext(), requests[i].getElement(), requests[i].getMemento(), null) 
				{
					public void done() {
						if (!isCanceled()) {
							synchronized(ExpressionMementoService.this) {
								fOutstandingRequestCount--;
								if (fOutstandingRequestCount == 0) {
									fRequestor.elementCompareComplete(fPendingRequests);
									fPendingRequests = null;
								}
							}
						}
					}
					
				};
			}
		}
		if (provdier == null) {
			for (int i = 0; i < requests.length; i++) {
				fPendingRequests[i].setEqual(false);
				fPendingRequests[i].done();
			}
		} else {
			provdier.compareElements(fPendingRequests);
		}
	}

	private synchronized void cancelPendingUpdates() {
		if (fPendingRequests != null) {
			for (int i = 0; i < fPendingRequests.length; i++) {
				fPendingRequests[i].cancel();
			}
		}
		fPendingRequests = null;
	}
	
	/**
	 * Disposes this viewer input service, canceling any pending jobs.
	 */
	public synchronized void dispose() {
		cancelPendingUpdates();
	}
}
