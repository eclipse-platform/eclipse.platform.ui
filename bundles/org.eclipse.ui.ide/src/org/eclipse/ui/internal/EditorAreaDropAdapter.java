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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.MarkerTransfer;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * An editor area drop adapter to handle transfer types
 * <code>EditorInputTransfer</code>, <code>MarkerTransfer</code>,
 * and <code>ResourceTransfer</code>.
 */
public class EditorAreaDropAdapter extends DropTargetAdapter {
	private IWorkbenchWindow window;
	
	/**
	 * Constructs a new EditorAreaDropAdapter.
	 * @param window the workbench window
	 */
	public EditorAreaDropAdapter(IWorkbenchWindow window) {
		this.window = window;
	}

	public void dragEnter(DropTargetEvent event) {				
		// always indicate a copy
		event.detail = DND.DROP_COPY;
	}

	public void dragOperationChanged(DropTargetEvent event) {				
		// always indicate a copy
		event.detail = DND.DROP_COPY;
	}

	public void drop(final DropTargetEvent event) {
		Display d = window.getShell().getDisplay();
		final IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			d.asyncExec(new Runnable() {
				public void run() {
					asyncDrop(event, page);
				}
			});
		}
	}

	private void asyncDrop(DropTargetEvent event, IWorkbenchPage page) {

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
					// @issue old implementation did not allow an external editor to open
					page.openEditor(editorInput, editorId);
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
					// @issue need to call appropriate code snippets to open editor on marker
					page.openEditor(null, null);
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
					if (files[i] instanceof IFile) {
						// @issue need to call appropriate code snippets to open editor on IFile
						page.openEditor(null, null);
					}
				}
			} catch (PartInitException e) {
				//do nothing, user may have been trying to drag a folder
			}
		}
			
	}
}

