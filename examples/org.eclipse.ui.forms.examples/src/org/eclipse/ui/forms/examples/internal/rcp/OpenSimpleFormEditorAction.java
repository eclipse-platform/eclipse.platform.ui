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
package org.eclipse.ui.forms.examples.internal.rcp;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.forms.examples.internal.OpenFormEditorAction;
/**
 * @see IWorkbenchWindowActionDelegate
 */
public class OpenSimpleFormEditorAction extends OpenFormEditorAction {
	public void run(IAction action) {
		openEditor(new SimpleFormEditorInput("Simple Editor"), "org.eclipse.ui.forms.examples.base-editor");
	}
}
