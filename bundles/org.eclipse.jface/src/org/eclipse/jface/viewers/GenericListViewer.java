/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * The GenericListViewer is a list viewer that is made up of a series of
 * GenericListItems.
 * <strong>NOTE</strong> This class is experimental and may change
 * without warning.
 * @since 3.1
 */
public abstract class GenericListViewer extends ContentViewer {

	//The items being displayed
	private GenericListItem[] items = new GenericListItem[0];

	//The control the receiver creates the list items in.
	private Composite control;

	/**
	 * Create a new instance of the receiver.
	 * @param parent The parent of the control.
	 * @param style The sytle bits to use for the control
	 */
	public GenericListViewer(Composite parent, int style) {
		super();
		control = new Composite(parent, style);
		control.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 1;
		layout.marginWidth = 1;
		control.setLayout(layout);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#inputChanged(java.lang.Object, java.lang.Object)
	 */
	protected void inputChanged(Object input, Object oldInput) {
		super.inputChanged(input, oldInput);
		refresh();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#getControl()
	 */
	public Control getControl() {
		return getComposite();
	}

	/**
	 * Return the composite for the receiver.
	 * @return Composite
	 */
	public Composite getComposite() {
		return control;
	}

	/**
	 * Create the list item for this element.
	 * @param element
	 * @param viewer (usually the receiver) to create this element in.
	 * @param color The color for created children
	 * @return GenericListItem
	 */
	public abstract GenericListItem createListItem(Object element, Color color,
			GenericListViewer viewer);

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#refresh()
	 */
	public void refresh() {

		if (getContentProvider() == null)
			return;

		if (getInput() == null)
			return;

		getControl().setRedraw(false);

		for (int i = 0; i < items.length; i++) {
			items[i].dispose();
		}

		Object[] elements = ((IStructuredContentProvider) getContentProvider())
				.getElements(getInput());
		items = new GenericListItem[elements.length];

		Color[] colors = new Color[] {
				getControl().getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW),
				getControl().getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW) };

		for (int i = 0; i < elements.length; i++) {
			Color color = colors[ i % 2];
			items[i] = createListItem(elements[i],color, this);
			items[i].getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			final GenericListItem finalItem = items[i];
			items[i].addMouseListener(new MouseAdapter(){
				/* (non-Javadoc)
				 * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
				 */
				public void mouseDown(MouseEvent e) {
					itemSelected(finalItem);
				}
			});
		}
		getControl().setRedraw(true);
	}

	/**
	 * One of the items has been selected. Update as required.
	 * @param item
	 */
	protected abstract void itemSelected(GenericListItem item);


	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#getSelection()
	 */
	public ISelection getSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#setSelection(org.eclipse.jface.viewers.ISelection, boolean)
	 */
	public void setSelection(ISelection selection, boolean reveal) {
		// TODO Auto-generated method stub

	}
}
