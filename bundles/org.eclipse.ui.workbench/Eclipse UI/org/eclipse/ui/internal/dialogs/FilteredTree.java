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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A simple control that provides a text widget and a tree viewer.  The contents
 * of the text widget are used to drive a PatternFilter that is on the viewer.
 * 
 * @see org.eclipse.ui.internal.dialogs.PatternFilter
 * @since 3.0
 */
public class FilteredTree extends Composite {

	private Text filterField;

	private TreeViewer treeViewer;

	private PatternFilter patternFilter;
	
	private FocusListener listener;

	/**
	 * Create a new instance of the receiver.  It will be created with a default
	 * pattern filter.
	 * 
	 * @param parent the parent composite
	 * @param treeStyle the SWT style bits to be passed to the tree viewer 
	 */
	public FilteredTree(Composite parent, int treeStyle) {
		this(parent, treeStyle, new PatternFilter());
	}

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param parent parent <code>Composite</code>
	 * @param treeStyle the style bits for the <code>Tree</code>
	 * @param filter the filter to be used
	 */
	public FilteredTree(Composite parent, int treeStyle, PatternFilter filter) {
		super(parent, SWT.NONE);
		patternFilter = filter;
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);

		filterField = new Text(this, SWT.SINGLE | SWT.BORDER);
		filterField.addKeyListener(new KeyAdapter() {

			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
			 */
			public void keyReleased(KeyEvent e) {
				patternFilter.setPattern(filterField.getText());
				treeViewer.refresh(false);
				if (filterField.getText().length() > 0)
					treeViewer.expandAll();
			}
		});
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		filterField.setLayoutData(data);
		treeViewer = new TreeViewer(this, treeStyle);
		data = new GridData(GridData.FILL_BOTH);
		treeViewer.getControl().setLayoutData(data);
		treeViewer.addFilter(patternFilter);

		Label sep = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData data2 = new GridData(GridData.FILL_HORIZONTAL);
		sep.setLayoutData(data2);

	}

	/**
	 * Get the tree viewer associated with this control.
	 * 
	 * @return the tree viewer 
	 */
	public TreeViewer getViewer() {
		return treeViewer;
	}

	/**
	 * Get the filter text field associated with this contro.
	 * 
	 * @return the text field
	 */
	public Text getFilterField() {
		return filterField;
	}

	/**
	 * Set the text that will be shown until the first focus.
	 * @param text
	 */
	public void setInitialText(String text) {
		
		
		listener = new FocusListener() {
			public void focusGained(FocusEvent event) {
				filterField.setText(""); //$NON-NLS-1$
				filterField.removeFocusListener(listener);
			}

			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
			 */
			public void focusLost(FocusEvent e) {
			}
		};
		filterField.addFocusListener(listener);
		filterField.setText(text);
	}
}