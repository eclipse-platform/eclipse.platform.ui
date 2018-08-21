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
package org.eclipse.ui.examples.javaeditor;


import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import org.eclipse.ui.editors.text.TextEditor;


/**
 * Java specific text editor.
 */
public class JavaEditor extends TextEditor {


	private class DefineFoldingRegionAction extends TextEditorAction {

		public DefineFoldingRegionAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
			super(bundle, prefix, editor);
		}

		private IAnnotationModel getAnnotationModel(ITextEditor editor) {
			return editor.getAdapter(ProjectionAnnotationModel.class);
		}

		@Override
		public void run() {
			ITextEditor editor= getTextEditor();
			ISelection selection= editor.getSelectionProvider().getSelection();
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection= (ITextSelection) selection;
				if (!textSelection.isEmpty()) {
					IAnnotationModel model= getAnnotationModel(editor);
					if (model != null) {

						int start= textSelection.getStartLine();
						int end= textSelection.getEndLine();

						try {
							IDocument document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
							int offset= document.getLineOffset(start);
							int endOffset= document.getLineOffset(end + 1);
							Position position= new Position(offset, endOffset - offset);
							model.addAnnotation(new ProjectionAnnotation(), position);
						} catch (BadLocationException x) {
							// ignore
						}
					}
				}
			}
		}
	}

	/** The outline page */
	private JavaContentOutlinePage fOutlinePage;
	/** The projection support */
	private ProjectionSupport fProjectionSupport;

	/**
	 * Default constructor.
	 */
	public JavaEditor() {
		super();
	}

	/** The <code>JavaEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method extend the
	 * actions to add those specific to the receiver
	 */
	@Override
	protected void createActions() {
		super.createActions();

		IAction a= new DefineFoldingRegionAction(JavaEditorMessages.getResourceBundle(), "DefineFoldingRegion.", this); //$NON-NLS-1$
		setAction("DefineFoldingRegion", a); //$NON-NLS-1$
	}

	/** The <code>JavaEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method performs any extra
	 * disposal actions required by the java editor.
	 */
	@Override
	public void dispose() {
		if (fOutlinePage != null)
			fOutlinePage.setInput(null);
		super.dispose();
	}

	/** The <code>JavaEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method performs any extra
	 * revert behavior required by the java editor.
	 */
	@Override
	public void doRevertToSaved() {
		super.doRevertToSaved();
		if (fOutlinePage != null)
			fOutlinePage.update();
	}

	/** The <code>JavaEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method performs any extra
	 * save behavior required by the java editor.
	 *
	 * @param monitor the progress monitor
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		if (fOutlinePage != null)
			fOutlinePage.update();
	}

	/** The <code>JavaEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method performs any extra
	 * save as behavior required by the java editor.
	 */
	@Override
	public void doSaveAs() {
		super.doSaveAs();
		if (fOutlinePage != null)
			fOutlinePage.update();
	}

	/** The <code>JavaEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method performs sets the
	 * input of the outline page after AbstractTextEditor has set input.
	 *
	 * @param input the editor input
	 * @throws CoreException in case the input can not be set
	 */
	@Override
	public void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if (fOutlinePage != null)
			fOutlinePage.setInput(input);
	}

	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, "ContentAssistProposal"); //$NON-NLS-1$
		addAction(menu, "ContentAssistTip"); //$NON-NLS-1$
		addAction(menu, "DefineFoldingRegion");  //$NON-NLS-1$
	}

	/** The <code>JavaEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method performs gets
	 * the java content outline page if request is for a an
	 * outline page.
	 *
	 * @param required the required type
	 * @return an adapter for the required type or <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> required) {
		if (IContentOutlinePage.class.equals(required)) {
			if (fOutlinePage == null) {
				fOutlinePage= new JavaContentOutlinePage(getDocumentProvider(), this);
				if (getEditorInput() != null)
					fOutlinePage.setInput(getEditorInput());
			}
			return (T) fOutlinePage;
		}

		if (fProjectionSupport != null) {
			T adapter= fProjectionSupport.getAdapter(getSourceViewer(), required);
			if (adapter != null)
				return adapter;
		}

		return super.getAdapter(required);
	}

	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		setSourceViewerConfiguration(new JavaSourceViewerConfiguration());
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {

		fAnnotationAccess= createAnnotationAccess();
		fOverviewRuler= createOverviewRuler(getSharedColors());

		ISourceViewer viewer= new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ProjectionViewer viewer= (ProjectionViewer) getSourceViewer();
		fProjectionSupport= new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
		fProjectionSupport.install();
		viewer.doOperation(ProjectionViewer.TOGGLE);
	}

	@Override
	protected void adjustHighlightRange(int offset, int length) {
		ISourceViewer viewer= getSourceViewer();
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			extension.exposeModelRange(new Region(offset, length));
		}
	}
}
