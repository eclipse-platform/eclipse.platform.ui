/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.MarkerTransfer;
import org.eclipse.ui.part.ResourceTransfer;

/* package */ class EditorAreaDropAdapter extends DropTargetAdapter {
	
	private WorkbenchPage page;
	/**
	 * Constructor EditorAreaDropAdapter.
	 * @param page
	 */
	public EditorAreaDropAdapter(WorkbenchPage page) {
		this.page = page;
	}

	public void drop(DropTargetEvent event) {

		/* Open Editor for resource */
		if (ResourceTransfer.getInstance().isSupportedType(event.currentDataType)) {
			IResource [] files = (IResource[]) event.data;
			try { //open all the files
				for (int i = 0; i < files.length; i++) {
					if (files[i] instanceof IFile)
						page.openInternalEditor((IFile)files[i]);	
				}
			} catch (PartInitException e) {
				//do nothing, user may have been trying to drag a folder
			}
			
		/* Open Editor for Marker (e.g. Tasks, Bookmarks, etc) */
		} else if (MarkerTransfer.getInstance().isSupportedType(event.currentDataType)) {
			IMarker [] markers = (IMarker[]) event.data;
			try { //open all the markers
				for (int i = 0; i < markers.length; i++) {
					page.openInternalEditor(markers[i]);	
				}
			} catch (PartInitException e) {
				//do nothing, user may have been trying to drag a marker with no associated file
			}

		/* Open Editor for generic IEditorInput */
		} else if (EditorInputTransfer.getInstance().isSupportedType(event.currentDataType)) {
			/* event.data is an object array, the first element is the editorId (String)
			 * and the second is the input (IEditorInput) */
			Object [] objectArray = (Object []) event.data;
			if (objectArray[0] instanceof String && objectArray[1] instanceof IEditorInput) {
				String editorId = (String) objectArray[0];
				IEditorInput input = (IEditorInput) objectArray[1];
				try {
					page.openInternalEditor(input, editorId);
				} catch (PartInitException e) {
				}
				
			}
		}	
	}
	
	public void dragOver(DropTargetEvent event) {				
		//make sure the file is never moved; always do a copy
		event.detail = DND.DROP_COPY;
	}
}

