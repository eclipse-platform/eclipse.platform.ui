/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.services;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.services.IEvaluationReference;

/**
 * @since 3.3
 * 
 */
public final class EvaluationService implements IRestrictionService {

	private EvaluationAuthority evaluationAuthority;

	public EvaluationService() {
		evaluationAuthority = new EvaluationAuthority();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.IEvaluationService#addEvaluationListener(org.eclipse.core.expressions.Expression,
	 *      org.eclipse.jface.util.IPropertyChangeListener, java.lang.String)
	 */
	public IEvaluationReference addEvaluationListener(Expression expression,
			IPropertyChangeListener listener, String property) {
		IEvaluationReference expressionReference = new EvaluationReference(
				expression, listener, property);

		evaluationAuthority.addEvaluationListener(expressionReference);
		return expressionReference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IEvaluationService#addEvaluationReference(org.eclipse.ui.services.IEvaluationReference)
	 */
	public void addEvaluationReference(IEvaluationReference ref) {
		Assert.isLegal(ref instanceof EvaluationReference, "Invalid type: " //$NON-NLS-1$
				+ ref.getClass().getName());
		evaluationAuthority.addEvaluationListener(ref);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.IEvaluationService#removeEvaluationListener(org.eclipse.ui.internal.services.IEvaluationReference)
	 */
	public void removeEvaluationListener(IEvaluationReference ref) {
		evaluationAuthority.removeEvaluationListener(ref);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IServiceWithSources#addSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	public void addSourceProvider(ISourceProvider provider) {
		evaluationAuthority.addSourceProvider(provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IServiceWithSources#removeSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	public void removeSourceProvider(ISourceProvider provider) {
		evaluationAuthority.removeSourceProvider(provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	public void dispose() {
		evaluationAuthority.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.IEvaluationService#getCurrentState()
	 */
	public IEvaluationContext getCurrentState() {
		return evaluationAuthority.getCurrentState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.IEvaluationService#addServiceListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addServiceListener(IPropertyChangeListener listener) {
		evaluationAuthority.addServiceListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.IEvaluationService#removeServiceListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removeServiceListener(IPropertyChangeListener listener) {
		evaluationAuthority.removeServiceListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IEvaluationService#requestEvaluation(java.lang.String)
	 */
	public void requestEvaluation(String propertyName) {
		evaluationAuthority.sourceChanged(new String[] { propertyName });
	}

	public void updateShellKludge() {
		evaluationAuthority.updateShellKludge();
	}

	/**
	 * <p>
	 * Bug 95792. A mechanism by which the key binding architecture can force an
	 * update of the handlers (based on the active shell) before trying to
	 * execute a command. This mechanism is required for GTK+ only.
	 * </p>
	 * <p>
	 * DO NOT CALL THIS METHOD.
	 * </p>
	 * 
	 * @param shell
	 *            The shell that should be considered active; must not be
	 *            <code>null</code>.
	 */
	public final void updateShellKludge(final Shell shell) {
		final Shell currentActiveShell = evaluationAuthority.getActiveShell();
		if (currentActiveShell != shell) {
			evaluationAuthority.sourceChanged(ISources.ACTIVE_SHELL,
					ISources.ACTIVE_SHELL_NAME, shell);
		}
	}
}
