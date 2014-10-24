/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 446965
 ******************************************************************************/

package org.eclipse.ui.internal.ide.handlers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * Test the active part to see if:
 * <ol>
 * <li>The part is an editor</li>
 * <li>It has a valid editor input</li>
 * <li>The editor input adapts to an IResource</li>
 * </ol>
 *
 * @since 3.9.100
 */
public class EditorInputPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (!(receiver instanceof IEditorPart)) {
			return false;
		}
		IEditorPart editor = (IEditorPart) receiver;
		IEditorInput input = editor.getEditorInput();
		if (input instanceof IFileEditorInput) {
			return true;
		}
		if (input == null) {
			IDEWorkbenchPlugin.log("IEditorPart passed in without IEditorInput set for  " + editor.getClass()); //$NON-NLS-1$
			return false;
		}
		Object obj = input.getAdapter(IResource.class);
		return obj != null;
	}

}
