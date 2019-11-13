/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
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
package org.eclipse.ui.internal.views.properties.tabbed.view;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.AbstractOverridableTabListPropertySection;
import org.eclipse.ui.views.properties.tabbed.IOverridableTabListContentProvider;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptor;
import org.eclipse.ui.views.properties.tabbed.ITabItem;
import org.eclipse.ui.views.properties.tabbed.ITabSelectionListener;
import org.eclipse.ui.views.properties.tabbed.TabContents;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * An extended implementation of the content provider for the tabbed property
 * sheet page's list of tabs.
 * <p>
 * This implementation allows the section to override the list of what tabs are
 * displayed, rather than using the default list of tabs from the tabbed
 * property registry.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
class OverridableTabListContentProvider extends TabListContentProvider
		implements IOverridableTabListContentProvider, ITabSelectionListener {

	/**
	 * Constructor for OverrideTabListContentProvider.
	 *
	 * @param registry
	 *            the tabbed property registry.
	 */
	public OverridableTabListContentProvider(TabbedPropertyRegistry registry) {
		super(registry);
	}

	private TabbedPropertySheetPage tabbedPropertySheetPage;

	private TabbedPropertyViewer tabbedPropertyViewer;

	@Override
	public Object[] getElements(Object inputElement) {
		if (tabbedPropertySheetPage.getCurrentTab() == null) {
			/*
			 * In this case, return the default list of tabs from the registry.
			 * The contributor will not have had a chance to load and override
			 * the tabs.
			 */
			return registry.getTabDescriptors(currentPart,
					(ISelection) inputElement);
		}
		return getOverrideTabs(inputElement);
	}

	@Override
	public void dispose() {
		stopListening();
		this.tabbedPropertyViewer = null;
		this.currentPart = null;
		this.tabbedPropertySheetPage = null;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (tabbedPropertyViewer == null) {
			Assert.isTrue(viewer instanceof TabbedPropertyViewer);
			init((TabbedPropertyViewer) viewer);
		}
	}

	/**
	 * Initialize the content provider for a tabbed property viewer.
	 *
	 * @param newTabbedPropertyViewer
	 *            a tabbed property viewer.
	 */
	private void init(TabbedPropertyViewer newTabbedPropertyViewer) {
		this.tabbedPropertyViewer = newTabbedPropertyViewer;
		currentPart = tabbedPropertyViewer.getWorkbenchPart();
		IPropertySheetPage page = Adapters.adapt(currentPart, IPropertySheetPage.class);
		if (page instanceof TabbedPropertySheetPage) {
			tabbedPropertySheetPage = (TabbedPropertySheetPage) page;
		} else {
			/*
			 * Is the part is a IContributedContentsView for the contributor,
			 * for example, outline view.
			 */
			IContributedContentsView view = Adapters.adapt(currentPart, IContributedContentsView.class);
			if (view != null) {
				IWorkbenchPart part = view.getContributingPart();
				if (part != null) {
					page = Adapters.adapt(part, IPropertySheetPage.class);
					if (page instanceof TabbedPropertySheetPage) {
						tabbedPropertySheetPage = (TabbedPropertySheetPage) page;
					}
				}
			}
		}
		Assert
				.isNotNull(
						tabbedPropertySheetPage,
						"DynamicTabListContentProvider could not find the TabbedPropertySheetPage for the active part");//$NON-NLS-1$
		startListening();
	}

	/**
	 * Start listening to tab selection change events.
	 */
	private void startListening() {
		tabbedPropertySheetPage.addTabSelectionListener(this);
	}

	/**
	 * Stop listening to tab selection change events.
	 */
	private void stopListening() {
		tabbedPropertySheetPage.removeTabSelectionListener(this);
	}

	@Override
	public void tabSelected(ITabDescriptor tabDescriptor) {
		AbstractOverridableTabListPropertySection section = getOverridableTabListPropertySection();
		Assert.isNotNull(section);
		int selectedIndex = tabbedPropertyViewer.getSelectionIndex();
		section.selectTab(selectedIndex);
	}

	/**
	 * set the selected tab from the list of tabs provided by the section.
	 */
	private void setSelectedTab() {
		TabDescriptor currentSelectedTabInList = null;
		IStructuredSelection selectionFromList = tabbedPropertyViewer.getStructuredSelection();
		if (!selectionFromList.equals(StructuredSelection.EMPTY)) {
			currentSelectedTabInList = (TabDescriptor) selectionFromList
					.getFirstElement();
		}
		AbstractOverridableTabListPropertySection section = getOverridableTabListPropertySection();
		if (section == null) {
			if (currentSelectedTabInList == null) {
				/*
				 * make sure there is a selected tab in the list.
				 */
				TabDescriptor newSelectedTab = (TabDescriptor) tabbedPropertyViewer
						.getElementAt(0);
				if (newSelectedTab != null) {
					tabbedPropertyViewer.setSelection(new StructuredSelection(
							newSelectedTab), true);
				}
			}
			return;
		}
		ITabItem[] dynamicTabItems = section.getTabs();
		if (dynamicTabItems == null) {
			/*
			 * if the section does not provide overridden tabs, return.
			 */
			return;
		}
		int selectedTabIndex = -1;
		for (int i = 0; i < dynamicTabItems.length; i++) {
			if (dynamicTabItems[i].isSelected()) {
				selectedTabIndex = i;
			}
		}
		if (currentSelectedTabInList == null ||
				!currentSelectedTabInList.getText().equals(
						dynamicTabItems[selectedTabIndex].getText())) {
			TabDescriptor newSelectedTab = (TabDescriptor) tabbedPropertyViewer
					.getElementAt(selectedTabIndex);
			tabbedPropertyViewer.setSelection(new StructuredSelection(
					newSelectedTab), true);
		}
	}

	/**
	 * Get the list of tabs to display in the tabbed property sheet page.
	 *
	 * @param inputElement
	 *            The current input element.
	 * @return the list of tabs.
	 */
	private ITabDescriptor[] getOverrideTabs(Object inputElement) {
		ITabDescriptor tabDescriptors[] = registry.getTabDescriptors(
				currentPart, (ISelection) inputElement);
		if (tabDescriptors == TabbedPropertyRegistry.EMPTY_DESCRIPTOR_ARRAY) {
			/*
			 * We clone the dynamic tabs from the single tab in the registry. If
			 * the registry has no tabs for the selection, return.
			 */
			return tabDescriptors;
		}
		AbstractOverridableTabListPropertySection section = getOverridableTabListPropertySection();
		Assert.isNotNull(section);
		ITabItem[] tabItems = section.getTabs();
		if (tabItems == null) {
			/*
			 * if the section does not provide overridden tabs, return the
			 * default tabs from the registry.
			 */
			return tabDescriptors;
		}
		ITabDescriptor[] overrideTabDescriptors = new ITabDescriptor[tabItems.length];
		TabDescriptor target = (TabDescriptor) tabDescriptors[0];
		for (int i = 0; i < tabItems.length; i++) {
			TabDescriptor cloneTabDescriptor = (TabDescriptor) target.clone();
			cloneTabDescriptor.setLabel(tabItems[i].getText());
			cloneTabDescriptor.setImage(tabItems[i].getImage());
			cloneTabDescriptor.setIndented(tabItems[i].isIndented());
			cloneTabDescriptor.setSelected(tabItems[i].isSelected());
			overrideTabDescriptors[i] = cloneTabDescriptor;
			// System.out.print("override " + i + " [" +  //$NON-NLS-1$//$NON-NLS-2$
			// tabItems[i].getText() + "]");//$NON-NLS-1$
			// if (tabItems[i].isSelected()) {
			// System.out.print(" selected");//$NON-NLS-1$
			// }
			// System.out.println("");//$NON-NLS-1$
		}
		return overrideTabDescriptors;
	}

	@Override
	public void overrideTabs() {
		stopListening();
		Object input = tabbedPropertyViewer.getInput();
		tabbedPropertyViewer.setInput(input);
		setSelectedTab();
		startListening();
	}

	/**
	 * Get the section implementation that provides the list of tabs. In our
	 * implementation, all the sections provide the list of tabs, so we select
	 * the first section from the tab descriptor.
	 *
	 * @return the section.
	 */
	private AbstractOverridableTabListPropertySection getOverridableTabListPropertySection() {
		TabContents tab = tabbedPropertySheetPage.getCurrentTab();
		Assert.isNotNull(tab);
		if (tab != null) {
			ISection section = tab.getSectionAtIndex(0);
			if (section instanceof AbstractOverridableTabListPropertySection) {
				return (AbstractOverridableTabListPropertySection) section;
			}
		}
		return null;
	}
}
