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
package org.eclipse.team.internal.ccvs.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IPostSelectionProvider;
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
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
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
	
	private IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {
		}
		public void partBroughtToTop(IWorkbenchPart part) {
		}
		public void partClosed(IWorkbenchPart part) {
			if (editor != null && part == editor) {
				disconnect();
			}
		}
		public void partDeactivated(IWorkbenchPart part) {
		}
		public void partOpened(IWorkbenchPart part) {
		}
	};

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
	 * @throws PartInitException, CVSException
	 */
	public void showAnnotations(ICVSResource cvsResource, Collection cvsAnnotateBlocks, InputStream contents) throws PartInitException, CVSException {
		showAnnotations(cvsResource, cvsAnnotateBlocks, contents, true);		
	}
	
	/**
	 * Show the annotation view.
	 * @param cvsResource
	 * @param cvsAnnotateBlocks
	 * @param contents
	 * @param useHistoryView
	 * @throws PartInitException, CVSException
	 */
	public void showAnnotations(ICVSResource cvsResource, Collection cvsAnnotateBlocks, InputStream contents, boolean useHistoryView) throws PartInitException, CVSException {

		// Disconnect from old annotation editor
		disconnect();
		
		// Remove old viewer
		Control[] oldChildren = top.getChildren();
		if (oldChildren != null) {
			for (int i = 0; i < oldChildren.length; i++) {
				oldChildren[i].dispose();
			}
		}

		viewer = new ListViewer(top, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
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

		setPartName(Policy.bind("CVSAnnotateView.showFileAnnotation", new Object[] {cvsResource.getName()})); //$NON-NLS-1$
		try {
			IResource localResource = cvsResource.getIResource();
			if (localResource != null) {
				setTitleToolTip(localResource.getFullPath().toString());
			} else {
				setTitleToolTip(cvsResource.getName());
			}
		} catch (CVSException e) {
			setTitleToolTip(cvsResource.getName());
		}
		
		if (!useHistoryView) {
			return;
		}

		// Get hook to the HistoryView
				
		historyView = (HistoryView) page.showView(HistoryView.VIEW_ID);
		historyView.showHistory((ICVSRemoteFile) CVSWorkspaceRoot.getRemoteResourceFor(cvsResource), false /* don't refetch */);
	}
	
	protected void disconnect() {
		if(editor != null) {
			if (editor.getSelectionProvider() instanceof IPostSelectionProvider) {
				((IPostSelectionProvider) editor.getSelectionProvider()).removePostSelectionChangedListener(this);
			}
			editor.getSite().getPage().removePartListener(partListener);
			editor = null;
			document = null;
		}
	}
	
	/**
	 * Makes the view visible in the active perspective. If there
	 * isn't a view registered <code>null</code> is returned.
	 * Otherwise the opened view part is returned.
	 */
	public static AnnotateView openInActivePerspective() throws PartInitException {
		return (AnnotateView) CVSUIPlugin.getActivePage().showView(VIEW_ID);
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		disconnect();
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
			} catch (CVSException e) {
				return;
			} catch (PartInitException e) {
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
		if(historyView != null) {
			historyView.selectRevision(listSelection.getRevision());
		}
		lastSelectionWasText = false;			
	}

	/**
	 * Try and open the correct registered editor type for the file.
	 * @throws CVSException, PartInitException
	 */
	private IEditorPart openEditor() throws CVSException, PartInitException {
		 ICVSRemoteFile file = (ICVSRemoteFile) CVSWorkspaceRoot.getRemoteResourceFor(cvsResource);

		// Determine if the registered editor is an ITextEditor.	
		// There is currently no support from UI to determine this information. This
		// problem has been logged in: https://bugs.eclipse.org/bugs/show_bug.cgi?id=47362
		// For now, use internal classes.
		String id = getEditorId(file);
		ITextEditor editor = getEditor(id, file);
		
		// Hook Editor post selection listener.
		if (editor.getSelectionProvider() instanceof IPostSelectionProvider) {
			((IPostSelectionProvider) editor.getSelectionProvider()).addPostSelectionChangedListener(this);
		}
		editor.getSite().getPage().addPartListener(partListener);
		return editor;
	}

    private ITextEditor getEditor(String id, ICVSRemoteFile file) throws PartInitException {
        // Either reuse an existing editor or open a new editor of the correct type.
		if (editor != null && editor instanceof IReusableEditor && page.isPartVisible(editor) && editor.getSite().getId().equals(id)) {
		    // We can reuse the editor
		    ((IReusableEditor) editor).setInput(new RemoteAnnotationEditorInput(file, contents));
		    return editor;
		} else {
		    // We can not reuse the editor so close the existing one and open a new one.
		    if (editor != null) {
		        page.closeEditor(editor, false);
		        editor = null;
		    }
		    IEditorPart part = page.openEditor(new RemoteAnnotationEditorInput(file, contents), id);
		    if (part instanceof ITextEditor) {
	        	return (ITextEditor)part;
		    } else {
		        // We asked for a text editor but didn't get one
		        // so open a vanilla text editor
		        page.closeEditor(part, false);
		        part = page.openEditor(new RemoteAnnotationEditorInput(file, contents), IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID);
		        if (part instanceof ITextEditor) {
		            return (ITextEditor)part;
		        } else {
		            // There is something really wrong so just bail
		            throw new PartInitException(Policy.bind("AnnotateView.0")); //$NON-NLS-1$
		        }
		    }
		}
    }

    private String getEditorId(ICVSRemoteFile file) {
        String id;
		IEditorRegistry registry = CVSUIPlugin.getPlugin().getWorkbench().getEditorRegistry();
		IEditorDescriptor descriptor = registry.getDefaultEditor(file.getName());
		if (descriptor == null || !(descriptor instanceof EditorDescriptor) || !(((EditorDescriptor)descriptor).isInternal())) {
			id = IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID; //$NON-NLS-1$
		} else {
			try {
				Object obj = IDEWorkbenchPlugin.createExtension(((EditorDescriptor) descriptor).getConfigurationElement(), "class"); //$NON-NLS-1$
				if (obj instanceof ITextEditor) {
					id = descriptor.getId();
				} else {
					id = IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID;
				}
			} catch (CoreException e) {
				id = IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID;
			}
		}
        return id;
    }

    // This method implemented to be an ISelectionChangeListener but we
	// don't really care when the List or Editor get focus.
	public void setFocus() {
		return;
	}
}
