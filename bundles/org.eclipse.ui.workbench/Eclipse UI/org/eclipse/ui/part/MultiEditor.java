/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.part;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;

/**
 * A MultiEditor is a composite of editors.
 * 
 * This class is intended to be supclassed.
 * 		
 */
public abstract class MultiEditor extends EditorPart {

	private int activeEditorIndex;
	private IEditorPart innerEditors[];

	/**
	 * Constructor for TileEditor.
	 */
	public MultiEditor() {
		super();
	}
	/*
	 * @see IEditorPart#doSave(IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		for (int i = 0; i < innerEditors.length; i++) {
			IEditorPart e = innerEditors[i];
			e.doSave(monitor);
		}
	}
	/**
	 * Create the control of the inner editor.
	 * 
	 * Must be called by subclass.
	 */
	public Composite createInnerPartControl(Composite parent,final IEditorPart e) {
		Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new FillLayout());
		e.createPartControl(content);
		parent.addListener(SWT.Activate, new Listener() {
			public void handleEvent(Event event) {
				if (event.type == SWT.Activate)
					activateEditor(e);
			}
		});
		return content;
	}
			
	/*
	 * @see IEditorPart#doSaveAs()
	 */
	public void doSaveAs() {
	}

	/*
	 * @see IEditorPart#gotoMarker(IMarker)
	 */
	public void gotoMarker(IMarker marker) {
	}

	/*
	 * @see IEditorPart#init(IEditorSite, IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		init(site, (MultiEditorInput) input);
	}
	/*
	 * @see IEditorPart#init(IEditorSite, IEditorInput)
	 */
	public void init(IEditorSite site, MultiEditorInput input) throws PartInitException {
		setInput(input);
		setSite(site);
		setTitle(input.getName());
		setTitleToolTip(input.getToolTipText());
	}
	/*
	 * @see IEditorPart#isDirty()
	 */
	public boolean isDirty() {
		for (int i = 0; i < innerEditors.length; i++) {
			IEditorPart e = innerEditors[i];
			if (e.isDirty())
				return true;
		}
		return false;
	}

	/*
	 * @see IEditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/*
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		innerEditors[activeEditorIndex].setFocus();
		updateGradient(innerEditors[activeEditorIndex]);
	}
	/**
	 * Returns the active inner editor.
	 */
	public final IEditorPart getActiveEditor() {
		return innerEditors[activeEditorIndex];
	}
	/**
	 * Returns an array with all inner editors.
	 */
	public final IEditorPart[] getInnerEditors() {
		return innerEditors;
	}
	/**
	 * Set the inner editors.
	 * 
	 * Should not be called by clients.
	 */
	public final void setChildren(IEditorPart[] children) {
		innerEditors = children;
		activeEditorIndex = 0;
	}
	/**
	 * Set the active editor.
	 */
	private void activateEditor(IEditorPart part) {
		IEditorPart oldEditor = getActiveEditor();
		activeEditorIndex = getIndex(part);
		IEditorPart e = getActiveEditor();
		EditorSite innerSite = (EditorSite) e.getEditorSite();
		((WorkbenchPage) innerSite.getPage()).requestActivation(e);
		updateGradient(oldEditor);
	}
	/*
	 * Return the index of the specified editor
	 */
	private int getIndex(IEditorPart editor) {
		for (int i = 0; i < innerEditors.length; i++) {
			if (innerEditors[i] == editor)
				return i;
		}
		return -1;
	}
	/**
	 * Update the gradient in the title bar.
	 */
	public void updateGradient(IEditorPart editor) {
		boolean activeEditor = editor == getSite().getPage().getActiveEditor();
		boolean activePart = editor == getSite().getPage().getActivePart();

		Gradient g;

		if (activePart) {
			if (getShellActivated())
				g = Gradient.BLUE;
			else
				g = Gradient.BLACK;
		} else {
			if (activeEditor)
				g = Gradient.WHITE;
			else
				g = Gradient.GRAY;
		}
		drawGradient(editor, g);
	}
	/**
	 * Draw the gradient in the title bar.
	 */
	protected abstract void drawGradient(IEditorPart innerEditor,Gradient g);
	
	/**
	 * Return true if the shell is activated.
	 */
	protected boolean getShellActivated() {
		WorkbenchWindow window = (WorkbenchWindow) getSite().getPage().getWorkbenchWindow();
		return window.getShellActivated();
	}
	/*
	 * The colors used to draw the title bar of the inner editors
	 */
	public static class Gradient {
		public Color fgColor;
		public Color[] bgColors;
		public int[] bgPercents;

		private static Gradient BLUE = new Gradient();
		private static Gradient BLACK = new Gradient();
		private static Gradient GRAY = new Gradient();
		private static Gradient WHITE = new Gradient();

		static {
			BLUE.fgColor = WorkbenchColors.getSystemColor(SWT.COLOR_TITLE_FOREGROUND);
			BLUE.bgColors = WorkbenchColors.getActiveEditorGradient();
			BLUE.bgPercents = WorkbenchColors.getActiveEditorGradientPercents();

			BLACK.fgColor = WorkbenchColors.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
			BLACK.bgColors = WorkbenchColors.getDeactivatedEditorGradient();
			BLACK.bgPercents = WorkbenchColors.getDeactivatedEditorGradientPercents();

			WHITE.fgColor = WorkbenchColors.getSystemColor(SWT.COLOR_BLACK);
			WHITE.bgColors = WorkbenchColors.getActiveNoFocusEditorGradient();
			WHITE.bgPercents = WorkbenchColors.getActiveNoFocusEditorGradientPercents();

			GRAY.fgColor = null;
			GRAY.bgColors = null;
			GRAY.bgPercents = null;
		}
	}
}