/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.text;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;

import org.eclipse.ui.IEditorInput;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.spelling.SpellingProblem;
import org.eclipse.ui.texteditor.spelling.SpellingService;


/**
 * The standard/default text editor.
 * <p>
 * This editor has id "{@link EditorsUI#DEFAULT_TEXT_EDITOR_ID org.eclipse.ui.DefaultTextEditor}".
 * The editor's context menu has id <code>#TextEditorContext</code>. The editor's ruler context menu
 * has id <code>#TextRulerContext</code>.
 * </p>
 * <p>
 * The workbench will automatically instantiate this class when the default editor is needed for a
 * workbench window.
 * </p>
 */
public class TextEditor extends AbstractDecoratedTextEditor {

	/**
	 * The encoding support for the editor.
	 * @since 2.0
	 */
	protected DefaultEncodingSupport fEncodingSupport;


	/**
	 * Creates a new text editor.
	 * 
	 * @see #initializeEditor()
	 * @see #initializeKeyBindingScopes()
	 */
	public TextEditor() {
		if (getSourceViewerConfiguration() == null) {
			// Configuration not yet set by subclass in initializeEditor()
			setSourceViewerConfiguration(new TextSourceViewerConfiguration(getPreferenceStore()));
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method configures the editor but does not define a
	 * <code>SourceViewerConfiguration</code>. When only interested in
	 * providing a custom source viewer configuration, subclasses may extend
	 * this method.
	 */
	protected void initializeEditor() {
		super.initializeEditor();
		setEditorContextMenuId("#TextEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#TextRulerContext"); //$NON-NLS-1$
		setHelpContextId(ITextEditorHelpContextIds.TEXT_EDITOR);
		configureInsertMode(SMART_INSERT, false);
		setInsertMode(INSERT);
	}

	/*
	 * @see IWorkbenchPart#dispose()
	 * @since 2.0
	 */
	public void dispose() {
		if (fEncodingSupport != null) {
			fEncodingSupport.dispose();
			fEncodingSupport= null;
		}

		super.dispose();
	}

	/**
	 * Installs the encoding support on the given text editor.
	 * <p>
 	 * Subclasses may override to install their own encoding
 	 * support or to disable the default encoding support.
 	 * </p>
	 * @since 2.1
	 */
	protected void installEncodingSupport() {
		fEncodingSupport= new DefaultEncodingSupport();
		fEncodingSupport.initialize(this);
	}

	/*
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}

	/*
	 * @see AbstractTextEditor#createActions()
	 * @since 2.0
	 */
	protected void createActions() {
		installEncodingSupport();
		super.createActions();
	}

	/*
	 * @see StatusTextEditor#getStatusHeader(IStatus)
	 * @since 2.0
	 */
	protected String getStatusHeader(IStatus status) {
		if (fEncodingSupport != null) {
			String message= fEncodingSupport.getStatusHeader(status);
			if (message != null)
				return message;
		}
		return super.getStatusHeader(status);
	}

	/*
	 * @see StatusTextEditor#getStatusBanner(IStatus)
	 * @since 2.0
	 */
	protected String getStatusBanner(IStatus status) {
		if (fEncodingSupport != null) {
			String message= fEncodingSupport.getStatusBanner(status);
			if (message != null)
				return message;
		}
		return super.getStatusBanner(status);
	}

	/*
	 * @see StatusTextEditor#getStatusMessage(IStatus)
	 * @since 2.0
	 */
	protected String getStatusMessage(IStatus status) {
		if (fEncodingSupport != null) {
			String message= fEncodingSupport.getStatusMessage(status);
			if (message != null)
				return message;
		}
		return super.getStatusMessage(status);
	}

	/*
	 * @see AbstractTextEditor#doSetInput(IEditorInput)
	 * @since 2.0
	 */
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if (fEncodingSupport != null)
			fEncodingSupport.reset();
	}

	/*
	 * @see IAdaptable#getAdapter(java.lang.Class)
	 * @since 2.0
	 */
	public Object getAdapter(Class adapter) {
		if (IEncodingSupport.class.equals(adapter))
			return fEncodingSupport;
		return super.getAdapter(adapter);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#updatePropertyDependentActions()
	 * @since 2.0
	 */
	protected void updatePropertyDependentActions() {
		super.updatePropertyDependentActions();
		if (fEncodingSupport != null)
			fEncodingSupport.reset();
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#handlePreferenceStoreChanged(org.eclipse.jface.util.PropertyChangeEvent)
	 * @since 3.3
	 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		if (event.getProperty().equals(SpellingService.PREFERENCE_SPELLING_ENABLED)) {
			ISourceViewer viewer= getSourceViewer();

			if (!(viewer instanceof ISourceViewerExtension2))
				return; // cannot unconfigure - do nothing

			// XXX: this is pretty heavy-weight
			((ISourceViewerExtension2)viewer).unconfigure();
			viewer.configure(getSourceViewerConfiguration());

			if (Boolean.FALSE.equals(event.getNewValue()))
				SpellingProblem.removeAll(getSourceViewer(), null);

			IAction quickAssistAction= getAction(ITextEditorActionConstants.QUICK_ASSIST);
			if (quickAssistAction instanceof IUpdate)
				((IUpdate)quickAssistAction).update();

			return;
		}
		super.handlePreferenceStoreChanged(event);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 * @since 3.0
	 */
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.SHIFT_RIGHT);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.SHIFT_LEFT);
	}
}
