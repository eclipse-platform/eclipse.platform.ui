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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.ui.IEditorReference;

public class DropDownEditorWorkbook2 extends EditorWorkbook {

	private ViewForm viewForm;
	private Composite topLeftComposite;
	private ToolBar closeBar;
	private ViewForm labelComposite;
	private CLabel label;
	private Composite dummy;
	
public DropDownEditorWorkbook2(EditorArea editorArea) {
	super(editorArea);
}

protected void createPresentation(Composite parent) {
	viewForm = new ViewForm(parent, SWT.BORDER);
	
	topLeftComposite = new Composite(viewForm, SWT.NONE);
	
	labelComposite = new ViewForm(topLeftComposite, SWT.BORDER | SWT.FLAT);
	label = new CLabel(labelComposite, SWT.NONE);
	Listener labelListener = new Listener() {
		public void handleEvent(Event event) {
			dropDown();
		}
	};
	label.addListener(SWT.MouseDown, labelListener);

	ToolBar arrowBar = new ToolBar(labelComposite, SWT.FLAT);
	ToolItem arrowItem = new ToolItem(arrowBar, SWT.NONE);
	arrowItem.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU));
	Listener arrowListener = new Listener() {
		public void handleEvent(Event event) {
			dropDown();
		}
	};
	arrowBar.addListener(SWT.MouseDown, arrowListener);
	
	labelComposite.setTopLeft(label);
	labelComposite.setTopRight(arrowBar);
	
	closeBar = new ToolBar(viewForm, SWT.FLAT);
	ToolItem closeItem = new ToolItem(closeBar, SWT.NONE);
	closeItem.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_CLOSE_VIEW));
	Listener closeListener = new Listener() {
		public void handleEvent(Event event) {
			EditorPane visibleEditor = getVisibleEditor();
			if (visibleEditor != null) {
				visibleEditor.doHide();
			}
		}
	};
	closeItem.addListener(SWT.Selection, closeListener);
	
	// ViewForm needs a control in order to get the horizontal line.
	// Also used for sizing of the visible EditorPane in setControlSize().
	dummy = new Composite(viewForm, SWT.NONE);
	dummy.setVisible(false);

	viewForm.setContent(dummy);

	// Layout
	FormLayout layout = new FormLayout();
	topLeftComposite.setLayout(layout);

	FormData formData = new FormData();
	formData.top = new FormAttachment(0, -1);
	formData.left = new FormAttachment(33);
	formData.bottom = new FormAttachment(100, 1);
	formData.right = new FormAttachment(67);
	labelComposite.setLayoutData(formData);
}

protected void setControlSize() {
	EditorPane visibleEditor = getVisibleEditor();
	if (visibleEditor == null || getControl() == null) 
		return;
	Rectangle bounds = viewForm.getBounds();
	Rectangle offset = dummy.getBounds();
	bounds.x += offset.x;
	bounds.y += offset.y;
	bounds.width = offset.width;
	bounds.height = offset.height;
	visibleEditor.setBounds(bounds);
	visibleEditor.moveAbove(viewForm);
}

public void showPaneMenu() {
	EditorPane visibleEditor = getVisibleEditor();
	if (visibleEditor != null) {
		Point location = label.toDisplay(0, label.getSize().y);
		visibleEditor.showPaneMenu(viewForm, location);
	}
}

public boolean isDragAllowed(EditorPane pane, Point p) {
	// TODO Auto-generated method stub
	return false;
}

protected void checkEnableDrag() {
	// TODO Auto-generated method stub
}

protected void disposePresentation() {
	viewForm.dispose();
	viewForm = null;
	topLeftComposite = null;
	label = null;
}

protected void drawGradient(Color fgColor, Color[] bgColors, int[] bgPercents) {
	label.setForeground(fgColor);
	label.setBackground(bgColors, bgPercents);
}

/**
 * @see LayoutPart#getMinimumHeight()
 */
public int getMinimumHeight() {
	// TODO: implement this
	return super.getMinimumHeight();
}

public Control getControl() {
	return viewForm;
}

public Control[] getTabList() {
	// TODO Auto-generated method stub
	return new Control[0];
}

public void showVisibleEditor() {
	// do nothing since the selected item is always visible
}

public void openTracker(LayoutPart part) {
	// TODO Auto-generated method stub
}

protected Object createItem(EditorPane editorPane) {
	if (getEditorList().size() >= 1 && viewForm.getTopLeft() == null) {
		viewForm.setTopLeft(topLeftComposite);
		viewForm.setTopRight(closeBar);
		viewForm.redraw();  // TODO Should not be necessary
	}
	// Do nothing
	return null;
}

protected void disposeItem(EditorPane editorPane) {
	if (editorPane == getVisibleEditor()) {
		label.setText(""); //$NON-NLS-1$
		label.setImage(null);
		label.setToolTipText(null);
	}
	if (getEditorList().size() == 0 && viewForm.getTopLeft() != null) {
		viewForm.setTopLeft(null);
		viewForm.setTopRight(null);
		viewForm.redraw();  // TODO Should not be necessary
	}
}

public void setContainer(ILayoutContainer container) {
	super.setContainer(container);
	// TODO Need to add mouse down listener
}

protected void setVisibleItem(EditorPane editorPane) {
	if (label != null) {
		IEditorReference ref = editorPane.getEditorReference();
		label.setText(ref.getTitle());
		label.setImage(ref.getTitleImage());
		label.setToolTipText(ref.getTitleToolTip());
	}
}

protected void updateItem(EditorPane editorPane) {
	if (getVisibleEditor() == editorPane && label != null) {
		IEditorReference ref = editorPane.getEditorReference();
		
		// Update title.
		String title = ref.getTitle();
		if (ref.isDirty())
			title = "*" + title;//$NON-NLS-1$
		label.setText(title);

		// Update the tab image
		Image image = ref.getTitleImage();
		if (image == null || image.isDisposed()) {
			label.setImage(null);
		} else {
			label.setImage(image);
		}
		label.setToolTipText(ref.getTitleToolTip());
	}
}

protected void disposeAllItems() {
	label.setText(""); //$NON-NLS-1$
	label.setImage(null);
	label.setToolTipText(null);
	viewForm.setTopLeft(null);
	viewForm.setTopRight(null);
	viewForm.redraw();  // TODO Should not be necessary
}

protected PartDragDrop createDragSource(LayoutPart part) {
	// TODO Auto-generated method stub
	return null;
}

public void reorderTab(EditorPane pane, int x, int y) {
	// TODO Auto-generated method stub
	
}

public void reorderTab(EditorPane pane, int newIndex) {
	// TODO Auto-generated method stub
	
}

public void dropDown() {
	int shellStyle= SWT.RESIZE;
	int tableStyle= SWT.V_SCROLL | SWT.H_SCROLL;
	final EditorsInformationControl info = new EditorsInformationControl(viewForm.getShell(), shellStyle, tableStyle);
	info.setInput(this);
	Point size= info.computeSizeHint();
	int minX = labelComposite.getSize().x;
	int minY = 300;
	if (size.x < minX) size.x = minX;
	if (size.y < minY) size.y = minY;
	info.setSize(size.x, size.y);
	info.setLocation(label.toDisplay(1, label.getSize().y + 3));
	info.setVisible(true);
	info.setFocus();
	info.getTableViewer().getTable().getShell().addListener(SWT.Deactivate, new Listener() {
		public void handleEvent(Event event) {
			info.setVisible(false);
		}
	});
}

}
