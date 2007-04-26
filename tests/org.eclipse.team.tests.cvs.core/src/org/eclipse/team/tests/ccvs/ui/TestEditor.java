/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.ui;

import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.TextEditor;

public class TestEditor extends TextEditor {

	public TestEditor() {
		// Nothing to do
	}
	
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (!(input instanceof IFileEditorInput))
			throw new PartInitException("Input is not an IFileEditorInput");
		super.init(site, input);
	}

}
