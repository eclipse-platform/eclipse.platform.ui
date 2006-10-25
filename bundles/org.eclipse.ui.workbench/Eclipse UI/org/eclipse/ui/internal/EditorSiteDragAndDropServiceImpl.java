/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dnd.IDragAndDropService;
import org.eclipse.ui.services.IDisposable;

/**
 * Implementation for the <code>IDragAndDropService</code> to be used from
 * <code>EditorSite</code>'s.
 * </p><p>
 * Adds a drop target to the given control that merges the site's
 * drop behaviour with that specified by the <code>addMergedDropTarget</code> call.
 * </p><p>
 * The current implementation is only defined for EditorSite's and merges the
 * given drop handling with the existing EditorSashContainer's behaviour.
 * </p><p>
 * NOTE: There is no cleanup (i.e. 'dispose') handling necessary for merged
 * Drop Targets but the hooks are put into place to maintain the consistency
 * of the implementation pattern.
 * </p>
 * @since 3.3
 *
 */
public class EditorSiteDragAndDropServiceImpl implements IDragAndDropService, IDisposable {
	/**
	 * Implementation of a DropTarget wrapper that will either delegate to the
	 * <code>primaryListener</code> if the event's <code>currentDataType</code>
	 * can be handled by it; otherwise the event is forwarded on to the
	 * listener specified by <code>secondaryListener</code>. 
	 * </p><p>
	 * NOTE: we should perhaps refactor this out into an external class
	 * </p>
	 * @since 3.3
	 *
	 */
	private static class MergedDropTarget {
		private DropTarget realDropTarget;
		
		private Transfer[] secondaryTransfers;
		private DropTargetListener secondaryListener;
		
		private Transfer[] primaryTransfers;
		private DropTargetListener primaryListener;
		
		public MergedDropTarget(Control control, 
				int priOps, Transfer[] priTransfers, DropTargetListener priListener,
				int secOps, Transfer[] secTransfers, DropTargetListener secListener) {
			realDropTarget = new DropTarget(control, priOps | secOps);
			
			// Cache the editor's transfers and listener
			primaryTransfers = priTransfers;
			primaryListener = priListener;
			
			// Capture the editor area's current transfers & listener
			WorkbenchWindow ww = (WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	        WorkbenchWindowConfigurer winConfigurer = ww.getWindowConfigurer();
	        secondaryTransfers = winConfigurer.getTransfers();
	        secondaryListener = winConfigurer.getDropTargetListener();
			
			// Combine the two sets of transfers into one array
			Transfer[] allTransfers = new Transfer[secondaryTransfers.length+primaryTransfers.length];
			int curTransfer = 0;
			for (int i = 0; i < primaryTransfers.length; i++) {
				allTransfers[curTransfer++] = primaryTransfers[i];
			}
			for (int i = 0; i < secondaryTransfers.length; i++) {
				allTransfers[curTransfer++] = secondaryTransfers[i];
			}
			realDropTarget.setTransfer(allTransfers);
			
			// Create a listener that will delegate to the appropriate listener
			// NOTE: the -editor- wins (i.e. it can over-ride WB behaviour if it wants
			realDropTarget.addDropListener(new DropTargetListener() {
				public void dragEnter(DropTargetEvent event) {
					getAppropriateListener(event).dragEnter(event);
				}
				public void dragLeave(DropTargetEvent event) {
					getAppropriateListener(event).dragLeave(event);
				}
				public void dragOperationChanged(DropTargetEvent event) {
					getAppropriateListener(event).dragOperationChanged(event);
				}
				public void dragOver(DropTargetEvent event) {
					getAppropriateListener(event).dragOver(event);
				}
				public void drop(DropTargetEvent event) {
					getAppropriateListener(event).drop(event);
				}
				public void dropAccept(DropTargetEvent event) {
					getAppropriateListener(event).dropAccept(event);
				}
			});
		}

		private DropTargetListener getAppropriateListener(DropTargetEvent event) {
			if (isSupportedType(primaryTransfers, event.currentDataType))
				return primaryListener;
			
			return secondaryListener;
		}
		
		private boolean isSupportedType(Transfer[] transfers, TransferData transferType) {
			for (int i = 0; i < transfers.length; i++) {
				if (transfers[i].isSupportedType(transferType))
					return true;
			}
			return false;
		}

		/**
		 * Clean up...
		 */
		public void dispose() {
		}
	}
	
	// Cache any listeners for cleanup
	List addedListeners = new ArrayList();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dnd.IEditorDropTargetService#addDropTarget(org.eclipse.swt.widgets.Control, int, org.eclipse.swt.dnd.Transfer[], org.eclipse.swt.dnd.DropTargetListener)
	 */
	public void addMergedDropTarget(Control control, int ops, Transfer[] transfers,
			DropTargetListener listener) {
		// Capture the editor area's current ops, transfers & listener
		int editorSiteOps = DND.DROP_DEFAULT | DND.DROP_COPY | DND.DROP_LINK;

		WorkbenchWindow ww = (WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        WorkbenchWindowConfigurer winConfigurer = ww.getWindowConfigurer();
        Transfer[] editorSiteTransfers = winConfigurer.getTransfers();
        DropTargetListener editorSiteListener = winConfigurer.getDropTargetListener();
        
        // Create a new 'merged' drop Listener using hte 
		MergedDropTarget newTarget = new MergedDropTarget(control, ops, transfers, listener,
				editorSiteOps, editorSiteTransfers, editorSiteListener);
		addedListeners.add(newTarget);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	public void dispose() {
		// Clean up the listeners
		for (Iterator iterator = addedListeners.iterator(); iterator.hasNext();) {
			MergedDropTarget target = (MergedDropTarget) iterator.next();
			target.dispose();
		}
		addedListeners.clear();
	}

}
