/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor.link;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedUIControl.LinkedUITarget;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.ITextEditor;



/**
 * An <code>ILinkedUITarget</code> with an associated editor, which is 
 * brought to the top when a linked position in its viewer is jumped to.
 * 
 * @since 3.0
 */
public class EditorTarget extends LinkedUITarget {

	/** The text viewer. */
	protected final ITextViewer fTextViewer;
	/** The editor displaying the viewer. */
	protected final ITextEditor fTextEditor;

	/**
	 * Creates a new instance.
	 * 
	 * @param viewer the viewer
	 * @param editor the editor displaying <code>viewer</code>, or <code>null</code>
	 */
	public EditorTarget(ITextViewer viewer, ITextEditor editor) {
		Assert.isNotNull(viewer);
		Assert.isNotNull(editor);
		fTextViewer= viewer;
		fTextEditor= editor;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.link2.LinkedUIControl.ILinkedUITarget#getViewer()
	 */
	public ITextViewer getViewer() {
		return fTextViewer;
	}

	/*
	 * @see org.eclipse.jface.text.link.LinkedUIControl.ILinkedFocusListener#linkedFocusGained(org.eclipse.jface.text.link.LinkedPosition, org.eclipse.jface.text.link.LinkedUIControl.LinkedUITarget)
	 */
	public void linkedFocusGained(LinkedPosition position, LinkedUITarget target) {
		IWorkbenchPage page= fTextEditor.getEditorSite().getPage();
		if (page != null) {
			page.bringToTop(fTextEditor);
		}
		fTextEditor.setFocus();
	}
	
	/*
	 * @see org.eclipse.jface.text.link.LinkedUIControl.ILinkedFocusListener#linkedFocusLost(org.eclipse.jface.text.link.LinkedPosition, org.eclipse.jface.text.link.LinkedUIControl.LinkedUITarget)
	 */
	public void linkedFocusLost(LinkedPosition position, LinkedUITarget target) {
	}
	
}