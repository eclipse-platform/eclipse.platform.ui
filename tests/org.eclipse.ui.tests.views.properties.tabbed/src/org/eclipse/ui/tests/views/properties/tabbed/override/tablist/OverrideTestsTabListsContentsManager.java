/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed.override.tablist;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.tests.views.properties.tabbed.override.OverrideTestsSelection;
import org.eclipse.ui.tests.views.properties.tabbed.override.OverrideTestsView;
import org.eclipse.ui.tests.views.properties.tabbed.model.Element;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.ui.views.properties.tabbed.ITabItem;
import org.eclipse.ui.views.properties.tabbed.TabContents;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The content manager for the override tests example for the tabbed property
 * sheet page.
 * <p>
 * The OverrideTestsView TabbedPropertySheetPage example is a look at the
 * properties view after the migration of a TabFolder/TabItem framework to the
 * tabbed properties view. In the case of a TabFolder, the folder (provider)
 * knows both the tab labels and tab items. This aligns to the tabbed properties
 * view, but the tab labels are tab descriptors and tab items are section
 * descriptions. This does not work with the default framework as the tabs
 * provide the sections. In this case, the IOverridableTabListContentProvider
 * framework has been provided.
 * <p>
 * The overridable tab list is a content provider that provides both the
 * sections and the tab labels.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public class OverrideTestsTabListsContentsManager {

	private Element activeElement;

	private IOverrideTestsTabList activeFolder;

	private ITabItem[] activeTabs;

	private Composite composite;

	private IOverrideTestsTabList emptyFolder;

	private IOverrideTestsTabList[] folders;

	private boolean processingUpdateTabs;

	private OverrideTestsView sampleView;

	private ISection section;

	private TabbedPropertySheetPage tabbedPropertySheetPage;

	public OverrideTestsTabListsContentsManager(Composite aComposite,
			TabbedPropertySheetPage aTabbedPropertySheetPage, ISection aSection) {
		this.composite = aComposite;
		this.section = aSection;
		this.tabbedPropertySheetPage = aTabbedPropertySheetPage;
		this.folders = new IOverrideTestsTabList[] { new BasicTabList(),
				new AdvancedTabList() };
		this.emptyFolder = new EmptyTabList();
	}

	private boolean compareTab(OverrideTestsTabItem a, OverrideTestsTabItem b) {
		return a.getItem().equals(b.getItem()) &&
				a.isSelected() == b.isSelected() &&
				a.getText().equals(b.getText());
	}

	private boolean compareTabs(ITabItem[] a, ITabItem[] b) {
		if (a != null && b != null) {
			if (a.length != b.length) {
				return false;
			}
			for (int i = 0; i < a.length; i++) {
				if (!compareTab((OverrideTestsTabItem) a[i],
						(OverrideTestsTabItem) b[i])) {
					return false;
				}
			}
			return true;
		}
		return a == null && b == null;
	}

	public void dispose() {
		if (activeFolder != null) {
			activeFolder.dispose();
		}
	}

	public ITabItem[] getTabs() {
		if (activeFolder != null) {
			return activeFolder.getTabs();
		}
		return new ITabItem[] {};
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (composite.isDisposed()) {
			return;
		}
		if (processingUpdateTabs) {
			return;
		}
		if (!validateSection()) {
			return;
		}
		Assert.isTrue(part instanceof OverrideTestsView);
		this.sampleView = (OverrideTestsView) part;
		Assert.isTrue(selection instanceof OverrideTestsSelection);
		Element newElement = ((OverrideTestsSelection) selection).getElement();
		IOverrideTestsTabList newFolder = null;

		if (newElement == null) {
			newFolder = emptyFolder;
		} else {
			for (int i = 0; i < folders.length; i++) {
				if (folders[i].appliesTo(newElement)) {
					newFolder = folders[i];
					break;
				}
			}
		}

		if (newFolder != activeFolder) {
			if (activeFolder != null) {
				activeFolder.dispose();
				activeFolder = null;
			}
			activeFolder = newFolder;
			if (newElement != null) {
				activeFolder.selectionChanged(newElement);
			}
			newFolder.createControls(composite);
			composite.layout(true);
		} else if (newElement != activeElement) {
			activeFolder.dispose();
			if (newElement != null) {
				activeFolder.selectionChanged(newElement);
			}
			activeFolder.createControls(composite);
			composite.layout(true);
			activeElement = newElement;
			return;
		}
		ITabItem[] newTabs = activeFolder.getTabs();
		if (!compareTabs(newTabs, activeTabs)) {
			processingUpdateTabs = true;
			tabbedPropertySheetPage.overrideTabs();
			processingUpdateTabs = false;
			activeTabs = newTabs;
		}

	}

	public void selectTab(int index) {
		if (activeTabs == null) {
			return;
		}
		OverrideTestsTabItem selectedTab = (OverrideTestsTabItem) activeTabs[index];
		sampleView.setSelection(selectedTab.getItem().getElement());

		OverrideTestsSelection selection = (OverrideTestsSelection) sampleView
				.getSelection();
		Element newElement = selection.getElement();

		activeFolder.dispose();
		activeFolder.selectionChanged(newElement);
		activeFolder.createControls(composite);
		composite.layout(true);
		activeElement = newElement;
		activeTabs = getTabs();
	}

	/**
	 * Returns true if the section is valid.
	 */
	private boolean validateSection() {
		TabContents tab = tabbedPropertySheetPage.getCurrentTab();
		if (tab != null) {
			ISection[] sections = tab.getSections();
			if (sections != null) {
				for (int i = 0; i < sections.length; i++) {
					if (sections[i] == section) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
