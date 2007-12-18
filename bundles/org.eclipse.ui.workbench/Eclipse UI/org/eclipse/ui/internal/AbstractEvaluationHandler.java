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

package org.eclipse.ui.internal;

import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationReference;
import org.eclipse.ui.services.IEvaluationService;

/**
 * This internal class serves as a foundation for any handler that would like
 * its enabled state controlled by core expressions and the IEvaluationService.
 * 
 * @since 3.3
 */
public abstract class AbstractEvaluationHandler extends AbstractEnabledHandler {
	private final static String PROP_ENABLED = "enabled"; //$NON-NLS-1$
	private IEvaluationService evaluationService;
	private IPropertyChangeListener enablementListener;
	private IEvaluationReference enablementRef;

	protected IEvaluationService getEvaluationService() {
		if (evaluationService == null) {
			evaluationService = (IEvaluationService) PlatformUI.getWorkbench()
					.getService(IEvaluationService.class);
		}
		return evaluationService;
	}

	protected void registerEnablement() {
		enablementRef = getEvaluationService().addEvaluationListener(
				getEnabledWhenExpression(), getEnablementListener(),
				PROP_ENABLED);
	}

	protected abstract Expression getEnabledWhenExpression();

	/**
	 * @return
	 */
	private IPropertyChangeListener getEnablementListener() {
		if (enablementListener == null) {
			enablementListener = new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (event.getProperty() == PROP_ENABLED) {
						if (event.getNewValue() instanceof Boolean) {
							setEnabled(((Boolean) event.getNewValue())
									.booleanValue());
						} else {
							setEnabled(false);
						}
					}
				}
			};
		}
		return enablementListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.AbstractHandler#dispose()
	 */
	public void dispose() {
		if (enablementRef != null) {
			evaluationService.removeEvaluationListener(enablementRef);
			enablementRef = null;
			enablementListener = null;
			evaluationService = null;
		}
		super.dispose();
	}
}
