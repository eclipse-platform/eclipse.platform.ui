/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.expressions.IEvaluationContext;

import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;

public class ParticipantDescriptor {
	
	private IConfigurationElement fConfigurationElement;
	private boolean fEnabled;

	private static final String ID= "id"; //$NON-NLS-1$
	private static final String NAME= "name";  //$NON-NLS-1$
	private static final String CLASS= "class"; //$NON-NLS-1$
	
	public ParticipantDescriptor(IConfigurationElement element) {
		fConfigurationElement= element;
		fEnabled= true;
	}
	
	public String getId() {
		return fConfigurationElement.getAttribute(ID);
	}
	
	public String getName() {
		return fConfigurationElement.getAttribute(NAME);
	}
	
	public IStatus checkSyntax() {
		if (fConfigurationElement.getAttribute(ID) == null) {
			return new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), IStatus.ERROR,
				RefactoringCoreMessages.getString("ParticipantDescriptor.error.id_missing"), null); //$NON-NLS-1$
		}
		if (fConfigurationElement.getAttribute(NAME) == null) {
			return new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), IStatus.ERROR,
				RefactoringCoreMessages.getFormattedString( "ParticipantDescriptor.error.name_missing", getId()),  //$NON-NLS-1$
				null);
		}
		if (fConfigurationElement.getAttribute(CLASS) == null) {
			return new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), IStatus.ERROR,
				RefactoringCoreMessages.getFormattedString( "ParticipantDescriptor.error.class_missing", getId()),  //$NON-NLS-1$
				null);
		}
		return new Status(IStatus.OK, RefactoringCorePlugin.getPluginId(), IStatus.OK, 
			RefactoringCoreMessages.getString("ParticipantDescriptor.correct"), null); //$NON-NLS-1$
	}
	
	public boolean matches(IEvaluationContext context) throws CoreException {
		IConfigurationElement[] elements= fConfigurationElement.getChildren(ExpressionTagNames.ENABLEMENT);
		if (elements.length == 0)
			return false;
		Assert.isTrue(elements.length == 1);
		Expression exp= ExpressionConverter.getDefault().perform(elements[0]);
		return convert(exp.evaluate(context));
	}

	public RefactoringParticipant createParticipant() throws CoreException {
		return (RefactoringParticipant)fConfigurationElement.createExecutableExtension(CLASS);
	}
	
	public boolean isEnabled() {
		return fEnabled;
	}
	
	public void disable() {
		fEnabled= false;
	}
	
	private boolean convert(EvaluationResult eval) {
		if (eval == EvaluationResult.FALSE)
			return false;
		return true;
	}
}
