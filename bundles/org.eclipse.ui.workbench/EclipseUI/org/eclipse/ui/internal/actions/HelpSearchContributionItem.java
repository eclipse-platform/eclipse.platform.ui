/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.actions;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.osgi.framework.FrameworkUtil;

/**
 * This is the contribution item that is used to add a help search field to the
 * cool bar.
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
	 * @param id     the contribution item id
	 */
	public HelpSearchContributionItem(IWorkbenchWindow window, String id) {
		super(id);
		Assert.isNotNull(window);
		this.window = window;
	}

	@Override
	protected Control createControl(Composite parent) {
		combo = new Combo(parent, SWT.NONE);
		combo.setToolTipText(WorkbenchMessages.WorkbenchWindow_searchCombo_toolTip);
		String[] items = PlatformUI.getDialogSettingsProvider(FrameworkUtil.getBundle(HelpSearchContributionItem.class))
				.getDialogSettings().getArray(ID);
		if (items != null) {
			combo.setItems(items);
		}
		combo.setText(WorkbenchMessages.WorkbenchWindow_searchCombo_text);
		combo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == SWT.CR) {
					doSearch(combo.getText(), true);
				}
			}
		});
		combo.addSelectionListener(widgetSelectedAdapter(e -> {
			int index = combo.getSelectionIndex();
			if (index != -1) {
				doSearch(combo.getItem(index), false);
			}
		}));
		return combo;
	}

	@Override
	protected int computeWidth(Control control) {
		return control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
	}

	private void doSearch(String phrase, boolean updateList) {
		if (phrase.isEmpty()) {
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
				PlatformUI.getDialogSettingsProvider(FrameworkUtil.getBundle(HelpSearchContributionItem.class))
						.getDialogSettings().put(ID, combo.getItems());
			}
		}
		window.getWorkbench().getHelpSystem().search(phrase);
	}
}
