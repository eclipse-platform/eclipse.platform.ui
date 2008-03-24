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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.services.IEvaluationReference;

/**
 * @since 3.4
 * 
 */
public class RestrictionListener implements IPropertyChangeListener {
	public static final String PROP = "restrict"; //$NON-NLS-1$

	private EvaluationReference reference;

	public RestrictionListener(IEvaluationReference ref) {
		Assert.isLegal(ref instanceof EvaluationReference, "Invalid type: " //$NON-NLS-1$
				+ ref.getClass().getName());
		reference = (EvaluationReference) ref;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getNewValue() instanceof Boolean) {
			reference.setPostingChanges(((Boolean) event.getNewValue())
					.booleanValue());
		} else {
			reference.setPostingChanges(false);
		}
	}
}
