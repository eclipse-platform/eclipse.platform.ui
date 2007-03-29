/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.services;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.ISourceProvider;

/**
 * @since 3.3
 * 
 */
public final class EvaluationService implements IEvaluationService {
	
	private EvaluationAuthority restrictionAuthority;
	private EvaluationAuthority evaluationAuthority;

	private IPropertyChangeListener restrictionListener = new IPropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent event) {
			IEvaluationReference source = (IEvaluationReference) event
					.getSource();
			if (source == null)
				return;
			
			IEvaluationReference mappedReference = source.getTargetReference();
			if (mappedReference == null)
				return;

			boolean evaluationEnabled = false;
			if (event.getNewValue() != null) {
				evaluationEnabled = ((Boolean) event.getNewValue()).booleanValue();
			} else {
				evaluationEnabled = false;
			}
			mappedReference.setPostingChanges(evaluationEnabled);
		}
	};
	

	public EvaluationService() {
		evaluationAuthority = new EvaluationAuthority();
		restrictionAuthority = new EvaluationAuthority();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.IEvaluationService#addEvaluationListener(org.eclipse.core.expressions.Expression,
	 *      org.eclipse.jface.util.IPropertyChangeListener, java.lang.String)
	 */
	public IEvaluationReference addEvaluationListener(Expression expression,
			IPropertyChangeListener listener, String property, Expression restrictEvaluation) {
		IEvaluationReference expressionReference = new EvaluationReference(expression, listener, property, null);
		
		evaluationAuthority.addEvaluationListener(expressionReference);
		if (restrictEvaluation != null) {
			IPropertyChangeListener restrictionListener = getRestrictionListener();
			// create a binding from the restriction to the expression
			IEvaluationReference restrictRef = new EvaluationReference(
					restrictEvaluation, restrictionListener,
					"evaluate", expressionReference); //$NON-NLS-1$

			// now set the pair in the opposite configuration for later cleanup
			expressionReference.setTargetReference(restrictRef);
			restrictionAuthority.addEvaluationListener(restrictRef);
		}
		return expressionReference;
	}

	/**
	 * @return
	 */
	private IPropertyChangeListener getRestrictionListener() {
		return restrictionListener ;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.IEvaluationService#removeEvaluationListener(org.eclipse.ui.internal.services.IEvaluationReference)
	 */
	public void removeEvaluationListener(IEvaluationReference ref) {
		evaluationAuthority.removeEvaluationListener(ref);
		IEvaluationReference target = ref.getTargetReference();
		if (target != null) {
			restrictionAuthority
					.removeEvaluationListener(target);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IServiceWithSources#addSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	public void addSourceProvider(ISourceProvider provider) {
		restrictionAuthority.addSourceProvider(provider);
		evaluationAuthority.addSourceProvider(provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IServiceWithSources#removeSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	public void removeSourceProvider(ISourceProvider provider) {
		restrictionAuthority.removeSourceProvider(provider);
		evaluationAuthority.removeSourceProvider(provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	public void dispose() {
		restrictionAuthority.dispose();
		evaluationAuthority.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.services.IEvaluationService#getCurrentState()
	 */
	public IEvaluationContext getCurrentState() {
		return evaluationAuthority.getCurrentState();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.services.IEvaluationService#addServiceListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addServiceListener(IPropertyChangeListener listener) {
		restrictionAuthority.addServiceListener(listener);
		evaluationAuthority.addServiceListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.services.IEvaluationService#removeServiceListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removeServiceListener(IPropertyChangeListener listener) {
		restrictionAuthority.removeServiceListener(listener);
		evaluationAuthority.removeServiceListener(listener);
	}
}