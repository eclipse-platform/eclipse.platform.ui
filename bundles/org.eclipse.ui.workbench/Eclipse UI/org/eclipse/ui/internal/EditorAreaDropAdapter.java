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

package org.eclipse.ui.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.MarkerTransfer;
import org.eclipse.ui.part.ResourceTransfer;

/* package */ class EditorAreaDropAdapter extends DropTargetAdapter {
	
	private WorkbenchPage page;
	
	/**
	 * Constructs a new EditorAreaDropAdapter.
	 * @param page the workbench page
	 */
	public EditorAreaDropAdapter(WorkbenchPage page) {
		this.page = page;
	}


	public void drop(final DropTargetEvent event) {
		Display d = page.getWorkbenchWindow().getShell().getDisplay();
		d.asyncExec(new Runnable() {
			public void run() {
				asyncDrop(event);
			}
		});
	}

	private void asyncDrop(DropTargetEvent event) {

		/* Open Editor for generic IEditorInput */
		if (EditorInputTransfer.getInstance().isSupportedType(event.currentDataType)) {
			/* event.data is an array of EditorInputData, which contains an IEditorInput and 
			 * the corresponding editorId */
			Assert.isTrue(event.data instanceof EditorInputTransfer.EditorInputData[]);
			EditorInputTransfer.EditorInputData[] editorInputs = (EditorInputTransfer.EditorInputData []) event.data;

			try { //open all the markers
				for (int i = 0; i < editorInputs.length; i++) {
					String editorId = editorInputs[i].editorId;
					IEditorInput editorInput = editorInputs[i].input;
					page.openInternalEditor(editorInput, editorId);	
				}
			} catch (PartInitException e) {
				//do nothing, user may have been trying to drag a marker with no associated file
			}
		}
			
		/* Open Editor for Marker (e.g. Tasks, Bookmarks, etc) */
		else if (MarkerTransfer.getInstance().isSupportedType(event.currentDataType)) {
			Assert.isTrue(event.data instanceof IMarker[]);
			IMarker[] markers = (IMarker[]) event.data;
			try { //open all the markers
				for (int i = 0; i < markers.length; i++) {
					page.openInternalEditor(markers[i]);	
				}
			} catch (PartInitException e) {
				//do nothing, user may have been trying to drag a marker with no associated file
			}
		}

		/* Open Editor for resource */
		else if (ResourceTransfer.getInstance().isSupportedType(event.currentDataType)) {
			Assert.isTrue(event.data instanceof IResource[]);
			IResource[] files = (IResource[]) event.data;
			try { //open all the files
				for (int i = 0; i < files.length; i++) {
					if (files[i] instanceof IFile)
						page.openInternalEditor((IFile)files[i]);	
				}
			} catch (PartInitException e) {
				//do nothing, user may have been trying to drag a folder
			}
		}
			
	}
	
	public void dragOver(DropTargetEvent event) {				
		//make sure the file is never moved; always do a copy
		event.detail = DND.DROP_COPY;
	}
}

