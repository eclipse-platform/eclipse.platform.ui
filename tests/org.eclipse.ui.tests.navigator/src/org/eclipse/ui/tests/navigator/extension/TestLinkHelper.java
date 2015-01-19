/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.navigator.ILinkHelper;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Instrumented ILinkHelper that's just like the resource one
 */
public class TestLinkHelper implements ILinkHelper {


	public IEditorInput findSelectionEditorInput;
	public int findSelectionCount;

	public IStructuredSelection activateEditorSelection;
	public int activateEditorCount;

	public static TestLinkHelper instance;

	public TestLinkHelper() {
		instance = this;
	}

	public void resetTest() {
		findSelectionEditorInput = null;
		findSelectionCount = 0;
		activateEditorSelection = null;
		activateEditorCount = 0;
	}

	@Override
	public IStructuredSelection findSelection(IEditorInput anInput) {
		findSelectionEditorInput = anInput;
		findSelectionCount++;
		System.out.println("findSelection: " + this);
		IFile file = ResourceUtil.getFile(anInput);
		if (file != null) {
			return new StructuredSelection(file);
		}
		return StructuredSelection.EMPTY;
	}

	@Override
	public void activateEditor(IWorkbenchPage aPage,
			IStructuredSelection aSelection) {
		activateEditorCount++;
		activateEditorSelection = aSelection;
		System.out.println("activateEditor: " + this);
		if (aSelection == null || aSelection.isEmpty())
			return;
		if (aSelection.getFirstElement() instanceof IFile) {
			IEditorInput fileInput = new FileEditorInput((IFile) aSelection.getFirstElement());
			IEditorPart editor = null;
			if ((editor = aPage.findEditor(fileInput)) != null)
				aPage.bringToTop(editor);
		}
	}

	@Override
	public String toString() {
		return "findSel: " + findSelectionEditorInput + " (" + findSelectionCount + ") activate: " + activateEditorSelection + " (" + activateEditorCount + ")";
	}

}
