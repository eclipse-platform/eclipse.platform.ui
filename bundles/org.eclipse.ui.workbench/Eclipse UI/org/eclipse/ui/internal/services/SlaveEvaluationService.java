/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.services;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.services.IEvaluationReference;
import org.eclipse.ui.services.IEvaluationService;

/**
 * @since 3.4
 * 
 */
public class SlaveEvaluationService implements IEvaluationService {

	private IEvaluationService parentService;

	private Collection sourceProviders = new ArrayList();

	private Collection serviceListeners = new ArrayList();

	private Collection evaluationReferences = new ArrayList();

	/**
	 * @param parent
	 */
	public SlaveEvaluationService(IEvaluationService parent) {
		parentService = parent;
	}

	/**
	 * @see org.eclipse.ui.services.IEvaluationService#addEvaluationListener(org.eclipse.core.expressions.Expression,
	 *      org.eclipse.jface.util.IPropertyChangeListener, java.lang.String)
	 */
	public IEvaluationReference addEvaluationListener(Expression expression,
			IPropertyChangeListener listener, String property) {
		IEvaluationReference ref = parentService.addEvaluationListener(
				expression, listener, property);
		if (!evaluationReferences.contains(ref)) {
			evaluationReferences.add(ref);
		}
		return ref;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IEvaluationService#addEvaluationReference(org.eclipse.ui.services.IEvaluationReference)
	 */
	public void addEvaluationReference(IEvaluationReference ref) {
		if (!evaluationReferences.contains(ref)) {
			evaluationReferences.add(ref);
		}
		parentService.addEvaluationReference(ref);
	}

	/**
	 * @see org.eclipse.ui.services.IEvaluationService#addServiceListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addServiceListener(IPropertyChangeListener listener) {
		if (!serviceListeners.contains(listener)) {
			serviceListeners.add(listener);
		}
		parentService.addServiceListener(listener);
	}

	/**
	 * @see org.eclipse.ui.services.IServiceWithSources#addSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	public void addSourceProvider(ISourceProvider provider) {
		if (!sourceProviders.contains(provider)) {
			sourceProviders.add(provider);
		}
		parentService.addSourceProvider(provider);
	}

	/**
	 * @see org.eclipse.ui.services.IEvaluationService#getCurrentState()
	 */
	public IEvaluationContext getCurrentState() {
		return parentService.getCurrentState();
	}

	/**
	 * @see org.eclipse.ui.services.IEvaluationService#removeEvaluationListener(org.eclipse.ui.services.IEvaluationReference)
	 */
	public void removeEvaluationListener(IEvaluationReference ref) {
		evaluationReferences.remove(ref);
		parentService.removeEvaluationListener(ref);
	}

	/**
	 * @see org.eclipse.ui.services.IEvaluationService#removeServiceListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removeServiceListener(IPropertyChangeListener listener) {
		serviceListeners.remove(listener);
		parentService.removeServiceListener(listener);
	}

	/**
	 * @see org.eclipse.ui.services.IServiceWithSources#removeSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	public void removeSourceProvider(ISourceProvider provider) {
		sourceProviders.remove(provider);
		parentService.removeSourceProvider(provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	public void dispose() {
		if (!evaluationReferences.isEmpty()) {
			Object[] array = evaluationReferences.toArray();
			for (int i = 0; i < array.length; i++) {
				parentService
						.removeEvaluationListener((IEvaluationReference) array[i]);
			}
		}
		if (!serviceListeners.isEmpty()) {
			Object[] array = serviceListeners.toArray();
			for (int i = 0; i < array.length; i++) {
				parentService
						.removeServiceListener((IPropertyChangeListener) array[i]);
			}
			serviceListeners.clear();
		}
		// Remove any "resource", like listeners, that were associated
		// with this service.
		if (!sourceProviders.isEmpty()) {
			Object[] array = sourceProviders.toArray();
			for (int i = 0; i < array.length; i++) {
				parentService.removeSourceProvider((ISourceProvider) array[i]);
			}
			sourceProviders.clear();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IEvaluationService#requestEvaluation(java.lang.String)
	 */
	public void requestEvaluation(String propertyName) {
		parentService.requestEvaluation(propertyName);
	}
}
