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
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * @since 3.3
 * 
 */
public class EvaluationReference extends EvaluationResultCache implements
		IEvaluationReference {

	private IPropertyChangeListener listener;
	private String property;
	private boolean postingChanges = true;
	private IEvaluationReference targetReference;

	/**
	 * @param expression
	 */
	public EvaluationReference(Expression expression,
			IPropertyChangeListener listener, String property, IEvaluationReference targetReference) {
		super(expression);
		this.listener = listener;
		this.property = property;
		this.targetReference = targetReference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.IEvaluationReference#getListener()
	 */
	public IPropertyChangeListener getListener() {
		return listener;
	}
	
	public String getProperty() {
		return property;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.services.IEvaluationReference#setFlopping(boolean)
	 */
	public void setPostingChanges(boolean evaluationEnabled) {
		this.postingChanges = evaluationEnabled;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.services.IEvaluationReference#isFlopping()
	 */
	public boolean isPostingChanges() {
		return postingChanges;
	}
	
	public IEvaluationReference getTargetReference() {
		return targetReference;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.services.IEvaluationReference#setTargetReference(org.eclipse.ui.internal.services.IEvaluationReference)
	 */
	public void setTargetReference(IEvaluationReference targetReference) {
		this.targetReference = targetReference;
	}
}
