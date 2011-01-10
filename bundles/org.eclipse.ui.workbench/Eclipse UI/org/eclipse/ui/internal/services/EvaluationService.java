/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.services;

import java.util.LinkedList;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.workbench.modeling.ExpressionContext;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.services.IEvaluationReference;
import org.eclipse.ui.services.IEvaluationService;

/**
 * @since 3.3
 * 
 */
public final class EvaluationService implements IEvaluationService {
	private ExpressionContext legacyContext;
	private int notifying = 0;

	private ListenerList serviceListeners = new ListenerList(ListenerList.IDENTITY);
	private IEclipseContext context;
	LinkedList<EvaluationReference> refs = new LinkedList<EvaluationReference>();

	public EvaluationService(IEclipseContext c) {
		context = c;
		legacyContext = new ExpressionContext(c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.services.IServiceWithSources#addSourceProvider(org.eclipse
	 * .ui.ISourceProvider)
	 */
	public void addSourceProvider(ISourceProvider provider) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.services.IServiceWithSources#removeSourceProvider(org.
	 * eclipse.ui.ISourceProvider)
	 */
	public void removeSourceProvider(ISourceProvider provider) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	public void dispose() {
		for (EvaluationReference ref : refs) {
			invalidate(ref, false);
		}
		refs.clear();
		serviceListeners.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.services.IEvaluationService#addServiceListener(org.eclipse
	 * .jface.util.IPropertyChangeListener)
	 */
	public void addServiceListener(IPropertyChangeListener listener) {
		serviceListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.services.IEvaluationService#removeServiceListener(org.
	 * eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removeServiceListener(IPropertyChangeListener listener) {
		serviceListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.services.IEvaluationService#addEvaluationListener(org.
	 * eclipse.core.expressions.Expression,
	 * org.eclipse.jface.util.IPropertyChangeListener, java.lang.String)
	 */
	public IEvaluationReference addEvaluationListener(Expression expression,
			IPropertyChangeListener listener, String property) {
		EvaluationReference ref = new EvaluationReference(context, expression, listener, property);
		addEvaluationReference(ref);
		return ref;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.services.IEvaluationService#addEvaluationReference(org
	 * .eclipse.ui.services.IEvaluationReference)
	 */
	public void addEvaluationReference(IEvaluationReference ref) {
		EvaluationReference eref = (EvaluationReference) ref;
		refs.add(eref);
		eref.participating = true;
		context.runAndTrack(eref);
	}

	private void invalidate(IEvaluationReference ref, boolean remove) {
		if (remove) {
			refs.remove(ref);
		}
		EvaluationReference eref = (EvaluationReference) ref;
		eref.participating = false;
		eref.evaluate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.services.IEvaluationService#removeEvaluationListener(org
	 * .eclipse.ui.services.IEvaluationReference)
	 */
	public void removeEvaluationListener(IEvaluationReference ref) {
		invalidate(ref, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IEvaluationService#getCurrentState()
	 */
	public IEvaluationContext getCurrentState() {
		return legacyContext;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IEvaluationService#requestEvaluation(java.lang.String)
	 */
	public void requestEvaluation(String propertyName) {
		String[] sourceNames = new String[] { propertyName };
		startSourceChange(sourceNames);
		// TODO compat: we need to go through and re-evaluate all expressions
		// with property tester. Possible to do, but also possibly painful
		endSourceChange(sourceNames);
	}

	/**
	 * @param sourceNames
	 */
	private void startSourceChange(final String[] sourceNames) {
		notifying++;
		if (notifying == 1) {
			fireServiceChange(IEvaluationService.PROP_NOTIFYING, Boolean.FALSE, Boolean.TRUE);
		}
	}

	/**
	 * @param sourceNames
	 */
	private void endSourceChange(final String[] sourceNames) {
		if (notifying == 1) {
			fireServiceChange(IEvaluationService.PROP_NOTIFYING, Boolean.TRUE, Boolean.FALSE);
		}
		notifying--;
	}

	private void fireServiceChange(final String property, final Object oldValue,
			final Object newValue) {
		Object[] listeners = serviceListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IPropertyChangeListener listener = (IPropertyChangeListener) listeners[i];
			SafeRunner.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					WorkbenchPlugin.log(exception);
				}

				public void run() throws Exception {
					listener.propertyChange(new PropertyChangeEvent(EvaluationService.this,
							property, oldValue, newValue));
				}
			});
		}
	}
}
