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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.expressions.IEvaluationContext;

import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

public class ParticipantDescriptor {
	
	private IConfigurationElement fConfigurationElement;

	private static final String ID= "id"; //$NON-NLS-1$
	private static final String CLASS= "class"; //$NON-NLS-1$
	
	public ParticipantDescriptor(IConfigurationElement element) {
		fConfigurationElement= element;
	}
	
	public String getId() {
		return fConfigurationElement.getAttribute(ID);
	}
	
	public IStatus checkSyntax() {
//		IConfigurationElement[] children= fConfigurationElement.getChildren(SCOPE_STATE);
//		switch(children.length) {
//			case 0:
//				return new Status(IStatus.ERROR, JavaPlugin.getPluginId(), IStatus.ERROR,
//					"Mandantory element <scopeState> missing. Disabling rename participant " + getId(), null);
//			case 1:
//				break;
//			default:
//				return new Status(IStatus.ERROR, JavaPlugin.getPluginId(), IStatus.ERROR,
//					"Only one <scopeState> element allowed. Disabling rename participant " + getId(), null);
//		}
//		children= fConfigurationElement.getChildren(OBJECT_STATE);
//		if (children.length > 1) {
//			return new Status(IStatus.ERROR, JavaPlugin.getPluginId(), IStatus.ERROR,
//				"Only one <objectState> element allowed. Disabling rename participant " + getId(), null);
//		}
		return new Status(IStatus.OK, RefactoringCorePlugin.getPluginId(), IStatus.OK, 
			"Syntactically correct rename participant element", null);
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
	
	private boolean convert(EvaluationResult eval) {
		if (eval == EvaluationResult.FALSE)
			return false;
		return true;
	}
}
