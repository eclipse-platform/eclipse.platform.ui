/*******************************************************************************
 * Copyright (c) 2012, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 473184
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.dndaddon;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.widgets.ImageBasedFrame;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.addons.minmax.TrimStack;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

/**
 *
 */
public class IBFDragAgent extends DragAgent {

	private ImageBasedFrame frame;
	private Shell ds;

	/**
	 * @param manager
	 */
	public IBFDragAgent(DnDManager manager) {
		super(manager);
	}

	@Override
	public MUIElement getElementToDrag(DnDInfo info) {
		if (!(info.curCtrl instanceof ImageBasedFrame)) {
			return null;
		}

		if (!(info.curElement instanceof MTrimElement)) {
			return null;
		}

		// Prevents dragging of trim elements tagged with 'NoMove'.
		if (info.curElement.getTags().contains(IPresentationEngine.NO_MOVE)) {
			return null;
		}

		ImageBasedFrame frame = (ImageBasedFrame) info.curCtrl;
		Rectangle handleRect = frame.getHandleRect();
		handleRect = frame.getDisplay().map(frame, null, handleRect);

		if (handleRect.contains(info.cursorPos)) {
			dragElement = info.curElement;
			return info.curElement;
		}

		return null;
	}

	@Override
	public void dragStart(DnDInfo info) {
		super.dragStart(info);

		if (dragElement instanceof MToolControl) {
			MToolControl tc = (MToolControl) dragElement;
			if (tc.getObject() instanceof TrimStack) {
				TrimStack ts = (TrimStack) tc.getObject();
				ts.showStack(false);
			}
		}

		if (dropAgent == null) {
			attachToCursor(info);
		}
	}

	private void attachToCursor(DnDInfo info) {
		frame = (ImageBasedFrame) dragElement.getWidget();
		dragElement.setVisible(false);
		dragElement.getTags().add("LockVisibility");

		if (ds == null) {
			ds = new Shell(dndManager.getDragShell(), SWT.NO_TRIM);
		}

		frame.setParent(ds);
		frame.setLocation(0, 0);
		ds.setSize(frame.getSize());
		ds.setLocation(info.cursorPos.x - 5, info.cursorPos.y - 5);

		ds.open();
		info.update();
	}

	@Override
	public void track(DnDInfo info) {
		super.track(info);

		if (dropAgent != null && ds != null && !ds.isDisposed() && ds.getChildren().length == 0) {
			ds.dispose();
			ds = null;
		}
		if (dropAgent == null) {
			attachToCursor(info);
		}

		if (ds != null) {
			ds.setLocation(info.cursorPos.x - 5, info.cursorPos.y - 5);
		}
	}

	@Override
	public void dragFinished(boolean performDrop, DnDInfo info) {
		dragElement.getTags().remove("LockVisibility");
		dragElement.setVisible(true);

		super.dragFinished(performDrop, info);

		// NOTE: the dragElement should no longer be a child of the shell
		if (ds != null && !ds.isDisposed()) {
			ds.dispose();
		}
		ds = null;
	}
}
