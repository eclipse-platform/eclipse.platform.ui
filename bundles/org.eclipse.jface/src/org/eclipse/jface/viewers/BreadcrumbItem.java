/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fabio Zadrozny - Bug 465666
 *******************************************************************************/
package org.eclipse.jface.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;

/**
 * An item in a breadcrumb viewer.
 * <p>
 * The item shows a label and an image. It also has the ability to expand, that
 * is to open a drop down menu.
 * </p>
 * <p>
 * The drop down allows to select any child of the items input element. The item
 * shows the label and icon of its data element, if any.
 * </p>
 *
 * @since 3.41
 */
class BreadcrumbItem extends Item {

	private final BreadcrumbViewer fParent;
	private Composite fContainer;

	private BreadcrumbItemDetails fDetailsBlock;
	private BreadcrumbItemDropDown fExpandBlock;
	private boolean fIsLast;

	/**
	 * A new breadcrumb item which is shown inside the given viewer.
	 *
	 * @param viewer the items viewer
	 * @param parent the container containing the item
	 */
	public BreadcrumbItem(BreadcrumbViewer viewer, Composite parent) {
		super(parent, SWT.NONE);

		fParent = viewer;

		fContainer = new Composite(parent, SWT.NONE);
		fContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		GridLayout layout = new GridLayout(2, false);
		layout.marginBottom = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		fContainer.setLayout(layout);

		fDetailsBlock = new BreadcrumbItemDetails(this, fContainer);

		fExpandBlock = new BreadcrumbItemDropDown(this, fContainer);

		fContainer.setData("org.eclipse.e4.ui.css.id", "BreadcrumbItemComposite"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns this items viewer.
	 *
	 * @return the viewer showing this item
	 */
	public BreadcrumbViewer getViewer() {
		return fParent;
	}

	/*
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	@Override
	public void dispose() {
		Shell shell = fExpandBlock.getDropDownShell();
		if (shell != null && !shell.isDisposed()) {
			shell.dispose();
		}
		fContainer.dispose();
		super.dispose();
	}

	/**
	 * Should this item show a text label.
	 *
	 * @param enabled true if it should
	 */
	void setShowText(boolean enabled) {
		fDetailsBlock.setTextVisible(enabled);
	}

	/**
	 * Does this item show a text label?
	 *
	 * @return true if it does.
	 */
	boolean isShowText() {
		return fDetailsBlock.isTextVisible();
	}

	/**
	 * Returns the width of this item.
	 *
	 * @return the width of this item
	 */
	int getWidth() {
		return fDetailsBlock.getWidth() + fExpandBlock.getWidth() + 2;
	}

	/**
	 * Sets whether this item has to be marked as selected or not.
	 *
	 * @param selected true if marked as selected
	 */
	void setSelected(boolean selected) {
		fDetailsBlock.setSelected(selected);
	}

	/**
	 * Sets whether this item has the keyboard focus.
	 *
	 * @param state <code>true</code> if it has focus, <code>false</code> otherwise
	 */
	void setFocus(boolean state) {
		fDetailsBlock.setFocus(state);
	}

	/**
	 * Returns whether this item has the keyboard focus.
	 *
	 * @return <code>true</code> if this item has the keyboard focus
	 */
	boolean hasFocus() {
		return fDetailsBlock.hasFocus();
	}

	/**
	 * Redraws this item using the viewer's current providers.
	 */
	void refresh() {
		IBaseLabelProvider labelProvider = fParent.getLabelProvider();

		if (labelProvider instanceof CellLabelProvider cellLabelProvider) {
			BreadcrumbViewerRow row = new BreadcrumbViewerRow(fParent, this);
			ViewerCell cell = row.getCell(0);
			cellLabelProvider.update(cell);
		} else {
			ILabelProvider simpleLabelProvider = (ILabelProvider) labelProvider;
			String text = simpleLabelProvider.getText(getData());
			Image image = simpleLabelProvider.getImage(getData());

			setText(text);
			setImage(image);
		}

		refreshToolTip();
		refreshArrow();
	}

	void refreshToolTip() {
		ILabelProvider toolTipLabelProvider = fParent.getToolTipLabelProvider();
		if (toolTipLabelProvider != null) {
			fDetailsBlock.setToolTip(toolTipLabelProvider.getText(getData()));
		} else {
			fDetailsBlock.setToolTip(getText());
		}
	}

	/**
	 * Refresh the arrows visibility.
	 */
	void refreshArrow() {
		ITreeContentProvider contentProvider = (ITreeContentProvider) fParent.getContentProvider();
		fExpandBlock.setEnabled(contentProvider.hasChildren(getData()));
	}

	/**
	 * Set whether this is the last item in the breadcrumb item chain or not.
	 *
	 * @param isLast <code>true</code> if this is the last item, <code>false</code>
	 *               otherwise
	 */
	void setIsLastItem(boolean isLast) {
		fIsLast = isLast;

		GridData data = (GridData) fContainer.getLayoutData();
		data.grabExcessHorizontalSpace = isLast;
	}

	/**
	 * Sets whether or not the this item should show the details (name and label).
	 *
	 * @param visible true if the item shows details
	 */
	void setDetailsVisible(boolean visible) {
		fDetailsBlock.setVisible(visible);
	}

	/**
	 * Expand this item, shows the drop down menu.
	 */
	void openDropDownMenu() {
		fExpandBlock.showMenu();
	}

	/**
	 * @return true if this item is expanded
	 */
	boolean isMenuShown() {
		return fExpandBlock.isMenuShown();
	}

	/**
	 * Returns the drop down shell.
	 *
	 * @return the shell of the drop down if shown, <code>null</code> otherwise
	 */
	Shell getDropDownShell() {
		return fExpandBlock.getDropDownShell();
	}

	/**
	 * Returns the drop down selection provider of this item.
	 *
	 * @return the selection provider of the drop down or <code>null</code>
	 */
	ISelectionProvider getDropDownSelectionProvider() {
		return fExpandBlock.getDropDownSelectionProvider();
	}

	/**
	 * Returns the bounds of this item.
	 *
	 * @return the bounds of this item
	 */
	public Rectangle getBounds() {
		return fContainer.getBounds();
	}

	/**
	 * Set the tool tip of the item to the given text.
	 *
	 * @param text the tool tip for the item
	 */
	public void setToolTip(String text) {
		fDetailsBlock.setToolTip(text);
	}

	Color getForeground() {
		return fDetailsBlock.getForeground();
	}

	void setForeground(Color color) {
		fDetailsBlock.setForeground(color);
	}

	Color getBackground() {
		return fDetailsBlock.getBackground();
	}

	void setBackground(Color color) {
		fDetailsBlock.setBackground(color);
	}

	Font getFont() {
		return fDetailsBlock.getFont();
	}

	void setFont(Font font) {
		fDetailsBlock.setFont(font);
	}

	/*
	 * @see org.eclipse.swt.widgets.Item#setText(java.lang.String)
	 */
	@Override
	public void setText(String string) {
		if (string == null) {
			string = ""; //$NON-NLS-1$
		}

		super.setText(string);
		fDetailsBlock.setText(string);

		// more or less space might be required for the label
		if (fIsLast)
			fContainer.layout(true, true);
	}

	/*
	 * @see org.eclipse.swt.widgets.Item#setImage(org.eclipse.swt.graphics.Image)
	 */
	@Override
	public void setImage(Image image) {
		super.setImage(image);
		fDetailsBlock.setImage(image);
	}

}
