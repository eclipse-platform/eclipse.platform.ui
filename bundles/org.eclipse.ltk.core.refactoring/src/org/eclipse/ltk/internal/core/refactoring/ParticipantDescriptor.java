/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.expressions.IEvaluationContext;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.IParticipantDescriptorFilter;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;

public class ParticipantDescriptor {

	private IConfigurationElement fConfigurationElement;
	private boolean fEnabled;

	private static final String ID= "id"; //$NON-NLS-1$
	private static final String NAME= "name";  //$NON-NLS-1$
	private static final String CLASS= "class"; //$NON-NLS-1$
	private static final String PROCESS_ON_CANCEL= "processOnCancel";  //$NON-NLS-1$

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
				RefactoringCoreMessages.ParticipantDescriptor_error_id_missing, null);
		}
		if (fConfigurationElement.getAttribute(NAME) == null) {
			return new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), IStatus.ERROR,
				Messages.format( RefactoringCoreMessages.ParticipantDescriptor_error_name_missing, getId()),
				null);
		}
		if (fConfigurationElement.getAttribute(CLASS) == null) {
			return new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), IStatus.ERROR,
				Messages.format( RefactoringCoreMessages.ParticipantDescriptor_error_class_missing, getId()),
				null);
		}
		return Status.OK_STATUS;
	}

	public boolean matches(IEvaluationContext context, IParticipantDescriptorFilter filter, RefactoringStatus status) throws CoreException {
		IConfigurationElement[] elements= fConfigurationElement.getChildren(ExpressionTagNames.ENABLEMENT);
		if (elements.length == 0)
			return false;
		Assert.isTrue(elements.length == 1);
		Expression exp= ExpressionConverter.getDefault().perform(elements[0]);
		if (!convert(exp.evaluate(context)))
			return false;
		if (filter != null && !filter.select(fConfigurationElement, status))
			return false;

		return true;
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

	public boolean processOnCancel() {
		String attr= fConfigurationElement.getAttribute(PROCESS_ON_CANCEL);
		if (attr == null)
			return false;
		return Boolean.parseBoolean(attr);
	}

	private boolean convert(EvaluationResult eval) {
		if (eval == EvaluationResult.FALSE)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "name= " + getName() + (isEnabled() ? " (enabled)" : " (disabled)") + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"\nid= " + getId() + //$NON-NLS-1$
				"\nclass= " + fConfigurationElement.getAttribute(CLASS); //$NON-NLS-1$
	}
}