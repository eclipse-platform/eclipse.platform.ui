/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.expressions.IEvaluationContext;

import org.eclipse.ltk.internal.core.refactoring.Assert;

public class ProcessorDescriptor {
	
	private IConfigurationElement fConfigurationElement;

	private static final String ID= "id"; //$NON-NLS-1$
	private static final String OVERRIDE= "override"; //$NON-NLS-1$
	private static final String CLASS= "class"; //$NON-NLS-1$
	
	public ProcessorDescriptor(IConfigurationElement element) {
		fConfigurationElement= element;
	}
	
	public String getId() {
		return fConfigurationElement.getAttribute(ID);
	}
	
	public boolean overrides() {
		return fConfigurationElement.getAttribute(OVERRIDE) != null;
	}
	
	public String getOverrideId() {
		return fConfigurationElement.getAttribute(OVERRIDE);
	}

	public boolean matches(IEvaluationContext context) throws CoreException {
		Assert.isNotNull(context);
		IConfigurationElement[] configElements= fConfigurationElement.getChildren(ExpressionTagNames.ENABLEMENT);
		IConfigurationElement enablement= configElements.length > 0 ? configElements[0] : null; 
		if (enablement != null) {
			Expression exp= ExpressionConverter.getDefault().perform(enablement);
			return (convert(exp.evaluate(context)));
		}
		return false;
	}

	public RefactoringProcessor createProcessor() throws CoreException {
		return (RefactoringProcessor)fConfigurationElement.createExecutableExtension(CLASS);
	}
	
	private boolean convert(EvaluationResult eval) {
		if (eval == EvaluationResult.FALSE)
			return false;
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * For debugging purpose only
	 */
	public String toString() {
		return "Processor: " + getId(); //$NON-NLS-1$
	}
}
