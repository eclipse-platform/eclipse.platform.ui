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

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * A ShortcutBarPart acts as a target for drag and drop events in
 * a PerspectivePresentation.  It is not intended to act as a real part.
 */
public class ShortcutBarPart extends LayoutPart {
	private ToolBarManager tbm;
	private ToolBar toolbar;

	/**
	 * Implements drop behavior for drag and drop operations on the
	 * fast view buttons of the shortcut bar.
	 */
	class ShortcutBarDropAdapter extends DropTargetAdapter {
		private static final long hoverThreshold = 250;

		private ToolBar toolBar;
		private ToolItem currentDropTarget;
		private int lastValidOperation;
		private IViewPart lastFastView = null;
		private IWorkbenchPart deactivatedPart = null;
		private long hoverStart;

		/**
		 * Creates a new instance of the receiver using the given drop
		 * target.
		 * @param dropTarget drop target the receiver gets drag&drop 
		 * 	events from
		 */
		public ShortcutBarDropAdapter(ToolBar dropTarget) {
			toolBar = dropTarget;
		}
		/**
		 * Returns the <code>ToolItem</code> at the given position.
		 * @param position x,y position of the desired <code>ToolItem</code> 
		 * @return <code>ToolItem</code> at the given position.or null
		 * 	if there is no item at the position.
		 */
		private ToolItem getDropTarget(Point position) {
			return toolBar.getItem(toolBar.toControl(position));
		}
		/**
		 * Check whether the drop operation can be accepted and set the
		 * drop event feedback accordingly.
		 * @param event drop event
		 */
		private void doDropValidation(DropTargetEvent event) {
			if (event.detail != DND.DROP_NONE) {
				lastValidOperation = event.detail;
			}
			if (validateTarget()) {
				event.detail = lastValidOperation;
			} else {
				event.detail = DND.DROP_NONE;
			}
		}
		/**
		 * The mouse entered the drop target. Perform drop validation
		 * to give drag and drop feedback to the user.
		 * @param event drop event
		 */
		public void dragEnter(DropTargetEvent event) {
			currentDropTarget = getDropTarget(new Point(event.x, event.y));
			doDropValidation(event);
		}
		/**
		 * The mouse is hovering over the drop target.
		 * If it has been hovering over a fast view button for the 
		 * defined hover time, open the fast view. If a fast view has 
		 * been previously opened by a hovering mouse and the mouse has 
		 * not been on any fast view button within the defined hover 
		 * time, close the fast view.
		 * @param event drop event
		 */
		public void dragOver(DropTargetEvent event) {
			Object oldTarget = currentDropTarget;

			currentDropTarget = getDropTarget(new Point(event.x, event.y));
			if (currentDropTarget == null) {
				event.detail = DND.DROP_NONE;
				if (oldTarget != null) {
					// mouse moved from contribution item to somewhere else
					hoverStart = System.currentTimeMillis();
				} else if (lastFastView != null && (System.currentTimeMillis() - hoverStart) > hoverThreshold) {
					hideView();
				}
			} else {
				long currentTime = System.currentTimeMillis();
				doDropValidation(event);
				if (oldTarget != currentDropTarget) {
					hoverStart = currentTime;
				} else if (validateTarget() && (currentTime - hoverStart) > hoverThreshold) {
					showView();
				}
			}
		}
		/**
		 * Shows the fast view that the mouse has been hovering on.
		 */
		private void showView() {
			Object itemData = currentDropTarget.getData(ShowFastViewContribution.FAST_VIEW);

			if (itemData != null) {
				IViewReference ref = (IViewReference) itemData;
				WorkbenchPage page = (WorkbenchPage) getWorkbenchWindow().getActivePage();

				if (deactivatedPart == null && page != null) {
					deactivatedPart = page.getActivePart();
				}
				lastFastView = (IViewPart)ref.getPart(true);
				if (page != null) {
					page.activate(lastFastView);
				}
			}
		}
		/**
		 * Hides the fast view by activating the view that was active before
		 * the fast view.
		 */
		private void hideView() {
			if (deactivatedPart != null) {
				WorkbenchPage activePage = (WorkbenchPage) getWorkbenchWindow().getActivePage();
				WorkbenchPage partPage = (WorkbenchPage) deactivatedPart.getSite().getPage();

				// activate the part that was active prior to the fast view.
				// hides the fast view.
				if (activePage == partPage && partPage != null) {
					partPage.activate(deactivatedPart);
				}
				deactivatedPart = null;
			}
			lastFastView = null;
		}
		/**
		 * Returns whether the current drop target is valid.
		 * @return true=the current target is a valid drop target, 
		 * 	false otherwise
		 */
		private boolean validateTarget() {
			return (currentDropTarget != null && currentDropTarget.getData() instanceof ShowFastViewContribution);
		}
	}
	/**
	 * ShortcutBarPart constructor comment.
	 * @param id java.lang.String
	 */
	public ShortcutBarPart(ToolBarManager tbm) {
		super("ShortcutBarPart"); //$NON-NLS-1$
		this.tbm = tbm;
		this.toolbar = tbm.getControl();
		toolbar.setData((IPartDropTarget) this);
		initDragAndDrop();
	}
	/**
	 * Creates the SWT control
	 */
	final public void createControl(Composite parent) {
		// Nothing to do.
	}
	/**
	 * Get the part control.  This method may return null.
	 */
	final public Control getControl() {
		return toolbar;
	}
	/**
	 * Adds drag and drop support to the shortcut bar.
	 */
	private void initDragAndDrop() {
		DropTarget dropTarget = new DropTarget(toolbar, DND.DROP_COPY | DND.DROP_MOVE);
		dropTarget.addDropListener(new ShortcutBarDropAdapter(toolbar));
	}
	/**
	 * @see IPartDropTarget::targetPartFor
	 */
	public LayoutPart targetPartFor(LayoutPart dragSource) {
		return this;
	}
}
