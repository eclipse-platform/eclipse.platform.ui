/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.actions;

import org.eclipse.core.runtime.Assert;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

import org.eclipse.ltk.internal.ui.refactoring.model.ModelMessages;

import org.eclipse.jface.action.Action;

/**
 * Action to accept pending refactorings to execute them on the local workspace.
 * 
 * @since 3.2
 */
public final class AcceptRefactoringsAction extends Action {

	/** The refactoring descriptor proxies, or <code>null</code> */
	private RefactoringDescriptorProxy[] fProxies= null;

	/**
	 * Creates a new accept refactorings action.
	 */
	public AcceptRefactoringsAction() {
		setText(ModelMessages.AcceptRefactoringsAction_title);
		setToolTipText(ModelMessages.AcceptRefactoringsAction_tool_tip);
		setDescription(ModelMessages.AcceptRefactoringsAction_description);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEnabled() {
		return fProxies != null && fProxies.length > 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public void run() {
		// TODO: implement
	}

	/**
	 * Sets the refactoring descriptor proxies to accept.
	 * 
	 * @param proxies
	 *            the refactoring descriptor proxies
	 */
	public void setRefactoringDescriptors(final RefactoringDescriptorProxy[] proxies) {
		Assert.isNotNull(proxies);
		fProxies= proxies;
	}
}