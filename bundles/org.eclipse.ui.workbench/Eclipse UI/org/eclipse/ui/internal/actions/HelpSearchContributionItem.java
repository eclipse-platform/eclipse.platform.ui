/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.actions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * This is the contribution item that is used to add a help search field to
 * the cool bar.
 * 
 * @since 3.1
 */
public class HelpSearchContributionItem extends ControlContribution {
	private static final String ID = "org.eclipse.ui.helpSearch"; //$NON-NLS-1$
	
	private IWorkbenchWindow window;

	private Combo combo;

	private int MAX_ITEM_COUNT = 10;
	
	/**
	 * Creates the contribution item.
	 * 
	 * @param window the window
	 */
	public HelpSearchContributionItem(IWorkbenchWindow window) {
		this(window, ID);
	}

	/**
	 * Creates the contribution item.
	 * 
	 * @param window the window
	 * @param id the contribution item id
	 */
	public HelpSearchContributionItem(IWorkbenchWindow window, String id) {
		super(id);
        Assert.isNotNull(window);
		this.window = window;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ControlContribution#createControl(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createControl(Composite parent) {
		combo = new Combo(parent, SWT.NONE);
		combo.setToolTipText(WorkbenchMessages.WorkbenchWindow_searchCombo_toolTip); 
		String[] items = WorkbenchPlugin.getDefault().getDialogSettings()
				.getArray(ID);
		if (items != null) {
			combo.setItems(items);
		}
		combo.setText(WorkbenchMessages.WorkbenchWindow_searchCombo_text); 
		combo.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.character == SWT.CR) {
					doSearch(combo.getText(), true);
				}
			}
		});
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = combo.getSelectionIndex();
				if (index != -1) {
					doSearch(combo.getItem(index), false);
				}
			}
		});
		return combo;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ControlContribution#computeWidth(org.eclipse.swt.widgets.Control)
	 */
	protected int computeWidth(Control control) {
		return control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
	}

	private void doSearch(String phrase, boolean updateList) {
		if (phrase.length() == 0) {
            window.getWorkbench().getHelpSystem().displaySearch();
			return;
		}
		if (updateList) {
			boolean exists = false;
			for (int i = 0; i < combo.getItemCount(); i++) {
				String item = combo.getItem(i);
				if (item.equalsIgnoreCase(phrase)) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				combo.add(phrase, 0);
				if (combo.getItemCount() > MAX_ITEM_COUNT) {
					combo.remove(combo.getItemCount() - 1);
				}
				WorkbenchPlugin.getDefault().getDialogSettings().put(ID,
						combo.getItems());
			}
		}
		window.getWorkbench().getHelpSystem().search(phrase);
	}
}
