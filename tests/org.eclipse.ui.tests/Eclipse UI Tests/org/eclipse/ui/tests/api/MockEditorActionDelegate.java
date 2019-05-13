/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

public class MockEditorActionDelegate extends MockActionDelegate implements
		IEditorActionDelegate {
	private IEditorPart target;

	/**
	 * Constructor for MockEditorActionDelegate
	 */
	public MockEditorActionDelegate() {
		super();
	}

	/**
	 * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		callHistory.add("setActiveEditor");
		target = targetEditor;
	}

	/**
	 * Returns the active editor.
	 */
	public IEditorPart getActiveEditor() {
		return target;
	}

}

