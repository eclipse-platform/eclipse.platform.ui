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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.preference.IPreferenceNode;

/**
 * The PreferencesPageContainer is the container object for 
 * the preference pages in a node.
 */
public class PreferencePagesArea  {

	private Composite control;

	private FilteredPreferenceDialog dialog;

	/**
	 * Create a new instance of the receiver.
	 */
	public PreferencePagesArea(FilteredPreferenceDialog preferenceDialog) {
		super();
		dialog = preferenceDialog;
	}

	/**
	 * Create the contents area of the composite.
	 * @param parent
	 * @param style
	 */
	void createContents(Composite parent, int style) {
		ScrolledComposite scrolled = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);

		scrolled.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_MAGENTA));

		GridData newPageData = new GridData(GridData.FILL_BOTH);
		scrolled.setLayoutData(newPageData);

		control = new Composite(scrolled, style);
		
		scrolled.setContent(control);
		scrolled.setExpandHorizontal(true);
		scrolled.setExpandVertical(true);
		scrolled.setMinHeight(300);
		scrolled.setMinWidth(300);

		GridData controlData = new GridData(GridData.FILL_BOTH);
		control.setLayoutData(controlData);

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 1;
		control.setLayout(layout);

	}

	/**
	 * Return the top level control
	 * @return Control
	 */
	Control getControl() {
		return control;
	}

	/**
	 * Show the selected node. Return whether or
	 * not this succeeded.
	 * @param node
	 * @return <code>true</code> if the page selection was sucessful
	 *         <code>false</code> is unsuccessful
	 * @see org.eclipse.jface.preference.PreferenceDialog#showPage(IPreferenceNode)
	 */
	boolean show(IPreferenceNode node) {
		node.createPage();
		
		PreferenceAreaEntry newEntry = new PreferenceAreaEntry(node,dialog);
		newEntry.createContents(control);

		control.layout(true);

		return true;
	}

	
}
