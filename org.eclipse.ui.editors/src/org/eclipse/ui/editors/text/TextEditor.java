/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		setEditorContextMenuId("#TextEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#TextRulerContext"); //$NON-NLS-1$
		setHelpContextId(ITextEditorHelpContextIds.TEXT_EDITOR);
		configureInsertMode(SMART_INSERT, false);
		setInsertMode(INSERT);
	}

	@Override
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

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	protected void createActions() {
		installEncodingSupport();
		super.createActions();
	}

	@Override
	protected String getStatusHeader(IStatus status) {
		if (fEncodingSupport != null) {
			String message= fEncodingSupport.getStatusHeader(status);
			if (message != null)
				return message;
		}
		return super.getStatusHeader(status);
	}

	@Override
	protected String getStatusBanner(IStatus status) {
		if (fEncodingSupport != null) {
			String message= fEncodingSupport.getStatusBanner(status);
			if (message != null)
				return message;
		}
		return super.getStatusBanner(status);
	}

	@Override
	protected String getStatusMessage(IStatus status) {
		if (fEncodingSupport != null) {
			String message= fEncodingSupport.getStatusMessage(status);
			if (message != null)
				return message;
		}
		return super.getStatusMessage(status);
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if (fEncodingSupport != null)
			fEncodingSupport.reset();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (IEncodingSupport.class.equals(adapter))
			return (T) fEncodingSupport;
		return super.getAdapter(adapter);
	}

	@Override
	protected void updatePropertyDependentActions() {
		super.updatePropertyDependentActions();
		if (fEncodingSupport != null)
			fEncodingSupport.reset();
	}

	@Override
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

	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.SHIFT_RIGHT);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.SHIFT_LEFT);
	}
}
