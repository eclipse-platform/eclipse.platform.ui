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
package org.eclipse.search.internal.ui.text;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.text.ISearchElementPresentation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Thomas Mäder
 *
 */
public class FileUIAdapter implements ISearchElementPresentation {
	private static final String PATH_ATTRIBUTE= "Path";
	private static final String NAME_ATTRIBUTE= "Name";
	private static final String[] FLAT_ATTRIBUTES= { PATH_ATTRIBUTE, NAME_ATTRIBUTE};
	private static final String[] STRUCTURED_ATTRIBUTES= { NAME_ATTRIBUTE};
	
	private FileLabelProvider fLabelProvider;
	private ActionGroup fActionGroup;

	public FileUIAdapter(IViewPart view) {
		fLabelProvider= new FileLabelProvider(FileLabelProvider.SHOW_PATH_LABEL);
		fActionGroup= new NewTextSearchActionGroup(view);
	}

	public void showMatch(Object element, int offset, int length) throws PartInitException {
		IFile file= (IFile) element;
		IWorkbenchPage page= SearchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart editor= IDE.openEditor(page, file, false);
		if (!(editor instanceof ITextEditor))
			return;
		ITextEditor textEditor= (ITextEditor) editor;
		textEditor.selectAndReveal(offset, length);
	}

	public String[] getSortingAtributes(boolean flatMode) {
		if (flatMode)
			return FLAT_ATTRIBUTES;
		else
			return STRUCTURED_ATTRIBUTES;
	}

	public void setSortOrder(String[] attributeNames, boolean flatMode) {
		if (!flatMode) {
			fLabelProvider.setOrder(FileLabelProvider.SHOW_LABEL);
		} else {
			for (int i= 0; i < attributeNames.length; i++) {
				if (attributeNames[i].equals(NAME_ATTRIBUTE)) {
					fLabelProvider.setOrder(FileLabelProvider.SHOW_LABEL_PATH);
					return;
				} else if (attributeNames[i].equals(PATH_ATTRIBUTE)) {
					fLabelProvider.setOrder(FileLabelProvider.SHOW_PATH_LABEL);
					return;
				}
			}
		}
	}

	public String getAttribute(Object underlyingElement, String attributeName) {
		IResource resource= (IResource)underlyingElement;
		if (PATH_ATTRIBUTE.equals(attributeName))
			return resource.getFullPath().toString();
		else if (NAME_ATTRIBUTE.equals(attributeName))
			return resource.getName();
		return "";
	}

	public void dispose() {
		fLabelProvider.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search2.ui.text.ISearchElementPresentation#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		return fLabelProvider.getImage(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search2.ui.text.ISearchElementPresentation#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		return fLabelProvider.getText(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchCategory#createActionGroup()
	 */
	public ActionGroup getActionGroup() {
		return fActionGroup;
	}
}
