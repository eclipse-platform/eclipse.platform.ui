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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

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
	private Collection items = new HashSet();

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
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 1;
		control.setLayout(layout);
		control.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));

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

	/**
	 * Create the contents for the receiver.
	 *
	 */
	public void createContents(){

		if (getContentProvider() == null)
			return;

		if (getInput() == null)
			return;

		getControl().setRedraw(false);

		Iterator itemsIterator = items.iterator();
		while (itemsIterator.hasNext()) {
			((GenericListItem) itemsIterator.next()).dispose();
		}

		items = new HashSet();
		Object[] elements = ((IStructuredContentProvider) getContentProvider())
				.getElements(getInput());

		createItems(elements,null,0);
		getControl().setRedraw(true);
	
	}

	/**
	 * Create items from the supplied elements.
	 * @param elements
	 * @param parent
	 * @param indent
	 */
	private void createItems(Object[] elements, GenericListItem parent, int indent) {
		for (int i = 0; i < elements.length; i++) {
			Color color = getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
			final GenericListItem newItem = createListItem(elements[i], color, this);
					
			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.horizontalIndent = indent * 20;
			newItem.getControl().setLayoutData(layoutData);
			if(parent != null)
				newItem.getControl().moveBelow(parent.getControl());
			newItem.addMouseListener(new MouseAdapter() {
				/* (non-Javadoc)
				 * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
				 */
				public void mouseDown(MouseEvent e) {
					itemSelected(newItem);
				}
			});
			
			items.add(newItem);
		}
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#refresh()
	 */
	public void refresh() {
		if(items.isEmpty())
			createContents();

	}
}
