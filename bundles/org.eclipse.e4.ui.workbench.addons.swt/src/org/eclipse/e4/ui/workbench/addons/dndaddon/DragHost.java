/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.addons.dndaddon;

import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

class DragHost {
	public static final String DragHostId = "dragHost"; //$NON-NLS-1$

	MUIElement dragElement;
	MElementContainer<MUIElement> originalParent;
	int originalIndex;
	MWindow baseWindow;

	int xOffset = 20;
	int yOffset = 20;

	private MWindow dragWindow;

	public DragHost(Shell shell) {
		dragWindow = (MWindow) shell.getData(AbstractPartRenderer.OWNING_ME);
		baseWindow = (MWindow) shell.getParent().getData(
				AbstractPartRenderer.OWNING_ME);
		dragElement = dragWindow.getChildren().get(0);
	}

	public DragHost(MUIElement element) {
		assert (dragElement != null);

		dragElement = element;
		originalParent = dragElement.getParent();
		originalIndex = originalParent.getChildren().indexOf(element);

		baseWindow = getWindow();
		assert (baseWindow != null && baseWindow.getWidget() != null);

		attach();
	}

	public Shell getShell() {
		return (Shell) dragWindow.getWidget();
	}

	public MWindow getModel() {
		return dragWindow;
	}

	public void setLocation(int x, int y) {
		getShell().setLocation(x + xOffset, y + yOffset);
	}

	private MWindow getWindow() {
		MUIElement pe = originalParent;
		while (pe != null && !(pe instanceof MApplication)) {
			if (((Object) pe) instanceof MWindow)
				return (MWindow) pe;
			pe = pe.getParent();
		}

		return null;
	}

	private void attach() {
		dragElement.getParent().getChildren().remove(dragElement);
		((Shell) baseWindow.getWidget()).getDisplay().update();
		dragWindow = MBasicFactory.INSTANCE.createWindow();
		dragWindow.getTags().add(DragHostId);
		formatModel(dragWindow);

		// define the initial location and size for the window
		Point cp = ((Shell) baseWindow.getWidget()).getDisplay()
				.getCursorLocation();
		Point size = new Point(200, 200);
		if (dragElement.getWidget() instanceof Control) {
			Control ctrl = (Control) dragElement.getWidget();
			size = ctrl.getSize();
		} else if (dragElement.getWidget() instanceof ToolItem) {
			ToolItem ti = (ToolItem) dragElement.getWidget();
			Rectangle bounds = ti.getBounds();
			size = new Point(bounds.width + 3, bounds.height + 3);
		}

		dragWindow.setX(cp.x + xOffset);
		dragWindow.setY(cp.y + yOffset);
		dragWindow.setWidth(size.x);
		dragWindow.setHeight(size.y);

		// add the window as a child of the base window
		baseWindow.getWindows().add(dragWindow);

		getShell().layout(getShell().getChildren(), SWT.CHANGED | SWT.DEFER);
		getShell().setVisible(true);
	}

	private void formatModel(MWindow dragWindow) {
	}

	public void drop(MElementContainer<MUIElement> newContainer, int itemIndex) {
		if (dragElement.getParent() != null)
			dragElement.getParent().getChildren().remove(dragElement);
		if (itemIndex >= 0)
			newContainer.getChildren().add(itemIndex, dragElement);
		else
			newContainer.getChildren().add(dragElement);

		newContainer.setSelectedElement(dragElement);
		if (dragElement.getWidget() instanceof ToolItem) {
			ToolItem ti = (ToolItem) dragElement.getWidget();
			ToolBar tb = ti.getParent();
			tb.layout(true);
			tb.getParent()
					.layout(new Control[] { tb }, SWT.CHANGED | SWT.DEFER);
		}

		baseWindow.getChildren().remove(dragWindow);

		newContainer.setSelectedElement(dragElement);

		if (getShell() != null)
			getShell().dispose();
	}

	public void cancel() {
		drop(originalParent, originalIndex);
	}

	public MUIElement getDragElement() {
		return dragElement;
	}

	/**
	 * 
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}
}
