/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.examples.navigator.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.examples.navigator.PropertiesTreeData;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.examples.navigator.Activator;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @since 3.2
 *
 */
public class OpenPropertyAction extends Action {

	private IWorkbenchPage page;
	private PropertiesTreeData data;
	private ISelectionProvider provider;


	/**
	 * Construct the OpenPropertyAction with the given page.
	 * @param p The page to use as context to open the editor.
	 * @param selectionProvider The selection provider
	 */
	public OpenPropertyAction(IWorkbenchPage p, ISelectionProvider selectionProvider) {
		setText("Open Property"); //$NON-NLS-1$
		page = p;
		provider = selectionProvider;
	}

	@Override
	public boolean isEnabled() {
		ISelection selection = provider.getSelection();
		if(!selection.isEmpty()) {
			IStructuredSelection sSelection = (IStructuredSelection) selection;
			if(sSelection.size() == 1 &&
				sSelection.getFirstElement() instanceof PropertiesTreeData)
			{
				data = ((PropertiesTreeData)sSelection.getFirstElement());
				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		/* In production code, you should always externalize strings,
		 * 	but this is an example. */
		try {
			if(isEnabled()) {
				IFile propertiesFile = data.getFile();
				IEditorPart editor = IDE.openEditor(page, propertiesFile);

				if (editor instanceof ITextEditor) {
					ITextEditor textEditor = (ITextEditor) editor;

					IDocumentProvider documentProvider =
						textEditor.getDocumentProvider();
					IDocument document =
						documentProvider.getDocument(editor.getEditorInput());

					FindReplaceDocumentAdapter searchAdapter =
						new FindReplaceDocumentAdapter(document);

					try {
						String searchText = data.getName()+"="; //$NON-NLS-1$
						IRegion region = searchAdapter.find(0,
															searchText,
															true /* forwardSearch */,
															true /* caseSensitive */,
															false /* wholeWord */,
															false /* regExSearch */);

						((ITextEditor)editor).selectAndReveal(region.getOffset(), region.getLength());

					} catch (BadLocationException e) {
						Activator.logError(0, "Could not open property!", e); //$NON-NLS-1$
						MessageDialog.openError(Display.getDefault().getActiveShell(),
								"Error Opening Property",  //$NON-NLS-1$
								"Could not open property!");   //$NON-NLS-1$
					}
					return;
				}
			}
		} catch (PartInitException e) {
			Activator.logError(0, "Could not open property!", e); //$NON-NLS-1$
			MessageDialog.openError(Display.getDefault().getActiveShell(),
					"Error Opening Property",  //$NON-NLS-1$
					"Could not open property!");   //$NON-NLS-1$
		}
	}
}
