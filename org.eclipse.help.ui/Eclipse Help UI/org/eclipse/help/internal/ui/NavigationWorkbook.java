package org.eclipse.help.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.ui.search.SearchPage;
import org.eclipse.help.internal.*;

/**
 * Navigation workbook.
 */
public class NavigationWorkbook implements ISelectionProvider {

	private CTabItem selectedTab;
	private CTabFolder tabFolder;
	private Collection selectionChangedListeners = new ArrayList();

	/**
	 * NavigationWorkbook constructor.
	 */
	public NavigationWorkbook(Composite parent) {
		tabFolder = new CTabFolder(parent, SWT.FLAT | SWT.SMOOTH | SWT.BOTTOM);
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				CTabItem newSelectedTab = (CTabItem) event.item;
				if (selectedTab == newSelectedTab)
					// Do nothing if the selection did not change.
					return;
				if (selectedTab != null && (!selectedTab.isDisposed())) {
					NavigationPage selectedPage = getPage(selectedTab);
					if (!selectedPage.deactivate()) {
						tabFolder.setSelection(selectedTab);
						return;
					}
				}
				selectedTab = newSelectedTab;
				NavigationPage newSelectedPage = getPage(newSelectedTab);
				if (newSelectedPage != null)
					newSelectedPage.activate();
			}
		});
	}
	/**
	 * Adds a listener for selection changes in this selection provider.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener a selection changed listener
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		addSelectionChangedListenersToPages();
		// due to pages being created and destroyed dynamically, we need to store listeners
		// and register to newly created pages later
		selectionChangedListeners.add(listener);
	}
	/**
	 * Registers all listeners of this object to each page
	 */
	private void addSelectionChangedListenersToPages() {
		NavigationPage[] pages = getPages();
		for (int i = 0; i < pages.length; i++) {
			for (Iterator it = selectionChangedListeners.iterator(); it.hasNext();) {
				pages[i].addSelectionChangedListener((ISelectionChangedListener) it.next());
			}
		}
	}
	/**
	 * @param input an InfoSet or Contribution[]
	 *  if Infoset, then Pages will be created containing trees representations
	 *   of Infoset's children (InfoViews)
	 *  If Array of Contribution, the elements can be either InfoView or InfoSet.
	 *   Pages will be created containing trees representations
	 *   of InfoView element and each of Infoset's children (InfoViews)
	 */
	public void display(Object input) {
		Iterator views = null;

		if (input instanceof InfoSet) {
			views = ((InfoSet) input).getChildren();

			removeAllPages();

			while (views.hasNext())
				new TopicsPage(this, views.next());
			if (HelpSystem.getSearchManager() != null)
				new SearchPage(this);

			addSelectionChangedListenersToPages();
			setSelectedPage(getPages()[0]);
		} else
			if (input instanceof Contribution[]) {
				removeAllPages();

				for (int i = 0; i < ((Contribution[]) input).length; i++) {
					if (((Contribution[]) input)[i] instanceof InfoSet) {
						views = ((InfoSet) ((Contribution[]) input)[i]).getChildren();
						while (views.hasNext())
							new TopicsPage(this, views.next());
						if (HelpSystem.getSearchManager() != null)
							new SearchPage(this);
					} else
						if (((Contribution[]) input)[i] instanceof InfoView) {
							new TopicsPage(this, ((Contribution[]) input)[i]);
						}
				}
				addSelectionChangedListenersToPages();
				setSelectedPage(getPages()[0]);
			}
		return;
	}
	public Control getControl() {
		return tabFolder;
	}
	private NavigationPage getPage(CTabItem item) {

		try {
			return (NavigationPage) item.getData();
		} catch (ClassCastException e) {
			return null;
		}
	}
	private NavigationPage[] getPages() {

		CTabItem[] tabItems = tabFolder.getItems();
		int nItems = tabItems.length;
		NavigationPage[] pages = new NavigationPage[nItems];
		for (int i = 0; i < nItems; i++)
			pages[i] = getPage(tabItems[i]);
		return pages;
	}
	private NavigationPage getSelectedPage() {

		int index = tabFolder.getSelectionIndex();
		if (index == -1)
			return null;

		CTabItem selectedItem = tabFolder.getItem(index);

		return (NavigationPage) selectedItem.getData();
	}
	/**
	 * Returns the current selection for this provider.
	 * 
	 * @return the current selection
	 */
	public ISelection getSelection() {
		NavigationPage[] pages = getPages();
		for (int i = 0; i < pages.length; i++) {
			NavigationPage aPage = (NavigationPage) pages[i];
			if ((aPage.getSelection() != null) && !aPage.getSelection().isEmpty())
				return aPage.getSelection();
		}
		return new StructuredSelection();
	}
	protected CTabFolder getTabFolder() {

		return tabFolder;

	}
	private void removeAllPages() {
		NavigationPage[] pages = getPages();
		for (int i = 0; i < pages.length; i++)
			pages[i].dispose();
	}
	/**
	 * Removes the given selection change listener from this selection provider.
	 * Has no affect if an identical listener is not registered.
	 *
	 * @param listener a selection changed listener
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		NavigationPage[] pages = getPages();
		for (int i = 0; i < pages.length; i++) {
			for (Iterator it = selectionChangedListeners.iterator(); it.hasNext();) {
				pages[i].removeSelectionChangedListener((ISelectionChangedListener) it.next());
			}
		}
		// do not add this listener to newly created listener anymore
		selectionChangedListeners.remove(listener);
	}
	protected void setSelectedPage(NavigationPage page) {
		CTabItem newSelectedTab = page.getTabItem();
		// ****************
		// THIS IS COMMENTED OUT IN DRIVER 039.
		// NEED TO INVESTIGATE IF IT IS NEEDED
		/////if (selectedTab == newSelectedTab)
		/////   return;
		selectedTab = newSelectedTab;
		page.activate();
		tabFolder.setSelection(newSelectedTab);
	}
	/**
	 * Sets the selection current selection for this selection provider.
	 *
	 * @param selection the new selection
	 */
	public void setSelection(ISelection selection) {
		// if selection contains infoset htmlviewer may need be updated
		if (selection instanceof IStructuredSelection) {
			Object o = ((IStructuredSelection) selection).getFirstElement();
			if (o != null && o instanceof InfoSet)
				for (Iterator it = selectionChangedListeners.iterator(); it.hasNext();) {
					((ISelectionChangedListener) it.next()).selectionChanged(
						new SelectionChangedEvent(this, selection));
				}
		}
		// inform navigation pages
		NavigationPage[] pages = getPages();
		for (int i = 0; i < pages.length; i++) {
			NavigationPage aPage = (NavigationPage) pages[i];
			aPage.setSelection(selection);
		}
	}
}
