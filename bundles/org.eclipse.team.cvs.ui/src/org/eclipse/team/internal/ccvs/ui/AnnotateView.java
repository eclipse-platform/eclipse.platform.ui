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
package org.eclipse.team.internal.ccvs.ui;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.internal.ccvs.core.CVSAnnotateBlock;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A view showing the results of the CVS Annotate Command.  A linked
 * combination of a View of annotations, a source editor and the
 * Resource History View
 */
public class AnnotateView extends ViewPart implements ISelectionChangedListener {

	ITextEditor editor;
	HistoryView historyView;
	IWorkbenchPage page;

	ListViewer viewer;
	IDocument document;
	Collection cvsAnnotateBlocks;
	ICVSResource cvsResource;
	InputStream contents;
	
	IStructuredSelection previousListSelection;
	ITextSelection previousTextSelection;
	boolean lastSelectionWasText = false;
	
	
	public static final String VIEW_ID = "org.eclipse.team.ccvs.ui.AnnotateView"; //$NON-NLS-1$
	private Composite top;

	public AnnotateView() {
		super();
	}

	public void createPartControl(Composite parent) {
		
		this.top = parent;
		
		// Create default contents
		Label label = new Label(top, SWT.WRAP);
		label.setText(Policy.bind("CVSAnnotateView.viewInstructions")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_BOTH));
		top.layout();
	}

	/**
	 * Show the annotation view.
	 * @param cvsResource
	 * @param cvsAnnotateBlocks
	 * @param contents
	 * @throws InvocationTargetException
	 */
	public void showAnnotations(ICVSResource cvsResource, Collection cvsAnnotateBlocks, InputStream contents) throws InvocationTargetException {
		showAnnotations(cvsResource, cvsAnnotateBlocks, contents, true);		
	}
	
	/**
	 * Show the annotation view.
	 * @param cvsResource
	 * @param cvsAnnotateBlocks
	 * @param contents
	 * @param useHistoryView
	 * @throws InvocationTargetException
	 */
	public void showAnnotations(ICVSResource cvsResource, Collection cvsAnnotateBlocks, InputStream contents, boolean useHistoryView) throws InvocationTargetException {

		// Remove old viewer
		Control[] oldChildren = top.getChildren();
		if (oldChildren != null) {
			for (int i = 0; i < oldChildren.length; i++) {
				oldChildren[i].dispose();
			}
		}

		viewer = new ListViewer(top, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new LabelProvider());
		viewer.addSelectionChangedListener(this);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

		WorkbenchHelp.setHelp(viewer.getControl(), IHelpContextIds.ANNOTATE_VIEW);

		top.layout();
		
		this.cvsResource = cvsResource;
		this.contents = contents;
		this.cvsAnnotateBlocks = cvsAnnotateBlocks;
		page = CVSUIPlugin.getActivePage();
		viewer.setInput(cvsAnnotateBlocks);
		editor = (ITextEditor) openEditor();
		IDocumentProvider provider = editor.getDocumentProvider();
		document = provider.getDocument(editor.getEditorInput());

		setTitle(Policy.bind("CVSAnnotateView.showFileAnnotation", new Object[] {cvsResource.getName()})); //$NON-NLS-1$
		
		if (!useHistoryView) {
			return;
		}

		// Get hook to the HistoryView
				
		try {
			historyView = (HistoryView) page.showView(HistoryView.VIEW_ID);
			historyView.showHistory((ICVSRemoteFile) CVSWorkspaceRoot.getRemoteResourceFor(cvsResource));
		} catch (PartInitException e) {
			throw new InvocationTargetException(e);
		} catch (CVSException e) {
			throw new InvocationTargetException(e);
		}
	}
	
	/**
	 * Makes the view visible in the active perspective. If there
	 * isn't a view registered <code>null</code> is returned.
	 * Otherwise the opened view part is returned.
	 */
	public static AnnotateView openInActivePerspective() {
		try {
			return (AnnotateView) CVSUIPlugin.getActivePage().showView(VIEW_ID);
		} catch (PartInitException pe) {
			return null;
		}
	}

	/**
	 * Selection changed in either the Annotate List View or the
	 * Source editor.
	 */
	public void selectionChanged(SelectionChangedEvent event) {
	
		if (event.getSelection() instanceof IStructuredSelection) {
			listSelectionChanged((IStructuredSelection) event.getSelection());
		} else if (event.getSelection() instanceof ITextSelection) {
			textSelectionChanged((ITextSelection) event.getSelection());
		}
		
		
	}
	
	/**
	 * A selection event in the Annotate Source Editor
	 * @param event
	 */
	private void textSelectionChanged(ITextSelection selection) {
		
		// Track where the last selection event came from to avoid
		// a selection event loop.
		lastSelectionWasText = true;
			
		// Locate the annotate block containing the selected line number.
		CVSAnnotateBlock match = null;
		for (Iterator iterator = cvsAnnotateBlocks.iterator(); iterator.hasNext();) {
			CVSAnnotateBlock block = (CVSAnnotateBlock) iterator.next();
			if (block.contains(selection.getStartLine())) {
				match = block;
				break;
			}
		}

		// Select the annotate block in the List View.
		if (match == null) {
			return;
		}
		
		StructuredSelection listSelection = new StructuredSelection(match); 
		viewer.setSelection(listSelection, true);
	}

	/**
	 * A selection event in the Annotate List View
	 * @param selection
	 */
	private void listSelectionChanged(IStructuredSelection selection) {

		// If the editor was closed, reopen it.
		if (editor == null || editor.getSelectionProvider() == null) {
			try {
				contents.reset();
				showAnnotations(cvsResource, cvsAnnotateBlocks, contents, false);
			} catch (InvocationTargetException e) {
				return;
			} catch (IOException e) {
				return;
			}
		}
		
		ISelectionProvider selectionProvider = editor.getSelectionProvider();
		if (selectionProvider == null) {
			// Failed to open the editor but what else can we do.
			return;
		}
		
		ITextSelection textSelection = (ITextSelection) selectionProvider.getSelection();
		CVSAnnotateBlock listSelection = (CVSAnnotateBlock) selection.getFirstElement();

		/**
		 * Ignore event if the current text selection is already equal to the corresponding
		 * list selection.  Nothing to do.  This prevents infinite event looping.
		 *
		 * Extra check to handle single line deltas 
		 */
		
		if (textSelection.getStartLine() == listSelection.getStartLine() && textSelection.getEndLine() == listSelection.getEndLine() && selection.equals(previousListSelection)) {
			return;
		}
		
		// If the last selection was a text selection then bale to prevent a selection loop.
		if (!lastSelectionWasText) {
			try {
				int start = document.getLineOffset(listSelection.getStartLine());
				int end = document.getLineOffset(listSelection.getEndLine() + 1);
				editor.selectAndReveal(start, end - start);
				if (editor != null && !page.isPartVisible(editor)) {
					page.activate(editor);
				}

			} catch (BadLocationException e) {
				// Ignore - nothing we can do.
			}
		}
		
		
		// Select the revision in the history view.
		historyView.selectRevision(listSelection.getRevision());
		lastSelectionWasText = false;			
	}

	/**
	 * Try and open the correct registered editor type for the file.  If the registered
	 * editor is *not* an ITextEditor then open the source in a default text editor.
	 * @return
	 * @throws InvocationTargetException
	 */
	private IEditorPart openEditor() throws InvocationTargetException {
		// Open the editor
		IEditorPart part;
		
		IEditorRegistry registry = CVSUIPlugin.getPlugin().getWorkbench().getEditorRegistry();
		ICVSRemoteFile file;

		try {
			file = (ICVSRemoteFile) CVSWorkspaceRoot.getRemoteResourceFor(cvsResource);
		} catch (CVSException e1) {
			throw new InvocationTargetException(e1);
		}
		
		IEditorDescriptor descriptor = registry.getDefaultEditor(file.getName());
		
		// Determine if the registered editor is an ITextEditor.
		
		String id;
	
		if (descriptor == null || !(descriptor instanceof EditorDescriptor) || !(((EditorDescriptor)descriptor).isInternal())) {
			id = IWorkbenchConstants.DEFAULT_EDITOR_ID; //$NON-NLS-1$
		} else {
			try {
				Object obj = WorkbenchPlugin.createExtension(((EditorDescriptor) descriptor).getConfigurationElement(), "class"); //$NON-NLS-1$
				if (obj instanceof ITextEditor) {
					id = descriptor.getId();
				} else {
					id = IWorkbenchConstants.DEFAULT_EDITOR_ID;
				}
			} catch (CoreException e) {
				id = IWorkbenchConstants.DEFAULT_EDITOR_ID;
			}
		}
		
		// Either reuse an existing editor or open a new editor of the correct type.
		try {
			try {
				if (editor != null && editor instanceof IReusableEditor && page.isPartVisible(editor) && editor.getSite().getId().equals(id)) {
					// We can reuse the editor
					((IReusableEditor) editor).setInput(new RemoteAnnotationEditorInput(file, contents));
					part = editor;
				} else {
					// We can not reuse the editor so close the existing one and open a new one.
					if (editor != null) {
						page.closeEditor(editor, false);
						editor = null;
					}
					part = page.openEditor(new RemoteAnnotationEditorInput(file, contents), id);
				}
			} catch (PartInitException e) {
				if (id.equals(IWorkbenchConstants.DEFAULT_EDITOR_ID)) {
					throw e;
				} else {
					// Could not open desired editor, try a default text editor.
					part = page.openEditor(new RemoteAnnotationEditorInput(file, contents), IWorkbenchConstants.DEFAULT_EDITOR_ID); //$NON-NLS-1$
				}
			}
		} catch (PartInitException e) {
			// Total failure.
			throw new InvocationTargetException(e);
		}
		
		// Hook Editor selection listener.
		ITextEditor editor = (ITextEditor) part;
		editor.getSelectionProvider().addSelectionChangedListener(this);
		return part;
	}

	// This method implemented to be an ISelectionChangeListener but we
	// don't really care when the List or Editor get focus.
	public void setFocus() {
		return;
	}
}
