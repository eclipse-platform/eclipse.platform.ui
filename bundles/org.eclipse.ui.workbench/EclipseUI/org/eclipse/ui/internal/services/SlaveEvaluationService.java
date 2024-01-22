/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 */
public class SlaveEvaluationService implements IEvaluationService {

	private IEvaluationService parentService;

	private Collection sourceProviders = new ArrayList();

	private Collection serviceListeners = new ArrayList();

	private Collection evaluationReferences = new ArrayList();

	public SlaveEvaluationService(IEvaluationService parent) {
		parentService = parent;
	}

	/**
	 * @see org.eclipse.ui.services.IEvaluationService#addEvaluationListener(org.eclipse.core.expressions.Expression,
	 *      org.eclipse.jface.util.IPropertyChangeListener, java.lang.String)
	 */
	@Override
	public IEvaluationReference addEvaluationListener(Expression expression, IPropertyChangeListener listener,
			String property) {
		IEvaluationReference ref = parentService.addEvaluationListener(expression, listener, property);
		if (!evaluationReferences.contains(ref)) {
			evaluationReferences.add(ref);
		}
		return ref;
	}

	@Override
	public void addEvaluationReference(IEvaluationReference ref) {
		if (!evaluationReferences.contains(ref)) {
			evaluationReferences.add(ref);
		}
		parentService.addEvaluationReference(ref);
	}

	/**
	 * @see org.eclipse.ui.services.IEvaluationService#addServiceListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	@Override
	public void addServiceListener(IPropertyChangeListener listener) {
		if (!serviceListeners.contains(listener)) {
			serviceListeners.add(listener);
		}
		parentService.addServiceListener(listener);
	}

	/**
	 * @see org.eclipse.ui.services.IServiceWithSources#addSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	@Override
	public void addSourceProvider(ISourceProvider provider) {
		if (!sourceProviders.contains(provider)) {
			sourceProviders.add(provider);
		}
		parentService.addSourceProvider(provider);
	}

	/**
	 * @see org.eclipse.ui.services.IEvaluationService#getCurrentState()
	 */
	@Override
	public IEvaluationContext getCurrentState() {
		return parentService.getCurrentState();
	}

	/**
	 * @see org.eclipse.ui.services.IEvaluationService#removeEvaluationListener(org.eclipse.ui.services.IEvaluationReference)
	 */
	@Override
	public void removeEvaluationListener(IEvaluationReference ref) {
		evaluationReferences.remove(ref);
		parentService.removeEvaluationListener(ref);
	}

	/**
	 * @see org.eclipse.ui.services.IEvaluationService#removeServiceListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	@Override
	public void removeServiceListener(IPropertyChangeListener listener) {
		serviceListeners.remove(listener);
		parentService.removeServiceListener(listener);
	}

	/**
	 * @see org.eclipse.ui.services.IServiceWithSources#removeSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	@Override
	public void removeSourceProvider(ISourceProvider provider) {
		sourceProviders.remove(provider);
		parentService.removeSourceProvider(provider);
	}

	@Override
	public void dispose() {
		if (!evaluationReferences.isEmpty()) {
			for (Object evaluationListener : evaluationReferences.toArray()) {
				parentService.removeEvaluationListener((IEvaluationReference) evaluationListener);
			}
		}
		if (!serviceListeners.isEmpty()) {
			for (Object serviceListener : serviceListeners.toArray()) {
				parentService.removeServiceListener((IPropertyChangeListener) serviceListener);
			}
			serviceListeners.clear();
		}
		// Remove any "resource", like listeners, that were associated
		// with this service.
		if (!sourceProviders.isEmpty()) {
			for (Object sourceProvider : sourceProviders.toArray()) {
				parentService.removeSourceProvider((ISourceProvider) sourceProvider);
			}
			sourceProviders.clear();
		}
	}

	@Override
	public void requestEvaluation(String propertyName) {
		parentService.requestEvaluation(propertyName);
	}
}
