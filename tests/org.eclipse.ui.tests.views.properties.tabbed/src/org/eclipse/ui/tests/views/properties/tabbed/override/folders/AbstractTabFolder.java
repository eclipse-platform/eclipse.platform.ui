/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
package org.eclipse.ui.tests.views.properties.tabbed.override.folders;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.tests.views.properties.tabbed.model.Element;
import org.eclipse.ui.tests.views.properties.tabbed.override.items.IOverrideTestsItem;

/**
 * The abstract implementation of a TabFolder.
 * <p>
 * The OverrideTestsTabFolderPropertySheetPage example is a before look at the
 * properties view before the migration to the tabbed properties view and the
 * override tabs support. When elements are selected in the OverrideTestsView,
 * TabFolder/TabItem are displayed for the elements.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public abstract class AbstractTabFolder implements IOverrideTestsTabFolder {

	private ListenerList itemSelectionListeners = new ListenerList();

	private CTabFolder tabFolder;

	@Override
	public void addItemSelectionListener(
			IOverrideTestsTabItemSelectionListener listener) {
		itemSelectionListeners.add(listener);
	}

	/**
	 * Determines if this folder applies to the element.
	 *
	 * @param element
	 *            the element.
	 * @return <code>true</code> if this folder applies to the element.
	 */
	@Override
	public boolean appliesTo(Element element) {
		return false;
	}

	@Override
	public void createControls(Composite composite) {
		tabFolder = new CTabFolder(composite, SWT.NONE);

		IOverrideTestsItem[] items = getItem();

		for (IOverrideTestsItem item : items) {
			CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
			item.createControls(tabFolder);
			tabItem.setText(item.getText());
			tabItem.setImage(item.getImage());
			tabItem.setControl(item.getComposite());
			tabItem.setData(item);
		}
		tabFolder.setSelection(0);

		tabFolder.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				CTabItem aTabItem = (CTabItem) e.item;
				IOverrideTestsTabItemSelectionListener[] listeners = (IOverrideTestsTabItemSelectionListener[]) itemSelectionListeners.getListeners();
				for (IOverrideTestsTabItemSelectionListener listener : listeners) {
					listener.itemSelected((IOverrideTestsItem) aTabItem.getData());
				}
			}
		});
	}

	@Override
	public void dispose() {
		tabFolder.dispose();
	}

	@Override
	public void removeItemSelectionListener(
			IOverrideTestsTabItemSelectionListener listener) {
		itemSelectionListeners.remove(listener);
	}

	@Override
	public void selectionChanged(Element element) {
		CTabItem[] items = tabFolder.getItems();
		for (CTabItem tabItem : items) {
			if (((IOverrideTestsItem) tabItem.getData()).getText().equals(element.getName())) {
				tabFolder.setSelection(tabItem);
			}
		}
	}

}
