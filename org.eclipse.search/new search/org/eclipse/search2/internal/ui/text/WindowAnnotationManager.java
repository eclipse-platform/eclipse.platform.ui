/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.search.ui.text.AbstractTextSearchResult;

public class WindowAnnotationManager {
	private IWorkbenchWindow fWindow;
	private Map fAnnotationManagers;
	private IPartListener2 fPartListener;
	private ArrayList fSearchResults;

	public WindowAnnotationManager(IWorkbenchWindow window) {
		fWindow = window;
		fAnnotationManagers = new HashMap();

		fSearchResults= new ArrayList();

		initEditors();
		fPartListener= new IPartListener2() {
			public void partActivated(IWorkbenchPartReference partRef) {
				startHighlighting(getEditor(partRef));
			}

			public void partBroughtToTop(IWorkbenchPartReference partRef) {
				startHighlighting(getEditor(partRef));
			}

			public void partClosed(IWorkbenchPartReference partRef) {
				stopHighlighting(getEditor(partRef));
			}

			public void partDeactivated(IWorkbenchPartReference partRef) {
			}

			public void partOpened(IWorkbenchPartReference partRef) {
			}

			public void partHidden(IWorkbenchPartReference partRef) {
				stopHighlighting(getEditor(partRef));
			}

			public void partVisible(IWorkbenchPartReference partRef) {
				startHighlighting(getEditor(partRef));
			}

			public void partInputChanged(IWorkbenchPartReference partRef) {
				updateHighlighting(getEditor(partRef));
			}
		};
		fWindow.getPartService().addPartListener(fPartListener);

	}

	private void startHighlighting(IEditorPart editor) {
		if (editor == null)
			return;
		EditorAnnotationManager mgr= (EditorAnnotationManager) fAnnotationManagers.get(editor);
		if (mgr == null) {
			mgr= new EditorAnnotationManager(editor);
			fAnnotationManagers.put(editor, mgr);
			mgr.setSearchResults(fSearchResults);
		}
	}

	private void updateHighlighting(IEditorPart editor) {
		if (editor == null)
			return;
		EditorAnnotationManager mgr= (EditorAnnotationManager) fAnnotationManagers.get(editor);
		if (mgr != null) {
			mgr.doEditorInputChanged();
		}
	}


	private void initEditors() {
		IWorkbenchPage[] pages= fWindow.getPages();
		for (int i = 0; i < pages.length; i++) {
			IEditorReference[] editors= pages[i].getEditorReferences();
			for (int j = 0; j < editors.length; j++) {
				IEditorPart editor= editors[j].getEditor(false);
				if (editor != null && pages[i].isPartVisible(editor)) {
					startHighlighting(editor);
				}
			}
		}
	}

	private void stopHighlighting(IEditorPart editor) {
		if (editor == null)
			return;
		EditorAnnotationManager mgr= (EditorAnnotationManager) fAnnotationManagers.remove(editor);
		if (mgr != null)
			mgr.dispose();
	}

	private IEditorPart getEditor(IWorkbenchPartReference partRef) {
		if (partRef instanceof IEditorReference) {
			return ((IEditorReference)partRef).getEditor(false);
		}
		return null;
	}

	void dispose() {
		fWindow.getPartService().removePartListener(fPartListener);
		for (Iterator mgrs = fAnnotationManagers.values().iterator(); mgrs.hasNext();) {
			EditorAnnotationManager mgr = (EditorAnnotationManager) mgrs.next();
			mgr.dispose();
		}
		fAnnotationManagers= null;
	}

	void addSearchResult(AbstractTextSearchResult result) {
		boolean alreadyShown= fSearchResults.contains(result);
		fSearchResults.add(result);
		if (!alreadyShown) {
			for (Iterator mgrs = fAnnotationManagers.values().iterator(); mgrs.hasNext();) {
				EditorAnnotationManager mgr = (EditorAnnotationManager) mgrs.next();
				mgr.addSearchResult(result);
			}
		}
	}

	void removeSearchResult(AbstractTextSearchResult result) {
		fSearchResults.remove(result);
		boolean stillShown= fSearchResults.contains(result);
		if (!stillShown) {
			for (Iterator mgrs = fAnnotationManagers.values().iterator(); mgrs.hasNext();) {
				EditorAnnotationManager mgr = (EditorAnnotationManager) mgrs.next();
				mgr.removeSearchResult(result);
			}
		}
	}

}
