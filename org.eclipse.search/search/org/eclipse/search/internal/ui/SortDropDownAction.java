/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Drop down action that holds the currently registered sort actions.
 */
class SortDropDownAction extends Action implements IMenuCreator {

	private static Map fgLastCheckedForType= new HashMap(5);

	private SearchResultViewer fViewer;
	private String fPageId;
	private Menu fMenu;
	private String fCheckedId;
	private Map fLastCheckedForType;

	public SortDropDownAction(SearchResultViewer viewer) {
		super(SearchMessages.getString("SortDropDownAction.label")); //$NON-NLS-1$
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_SORT);
		fViewer= viewer;
		fLastCheckedForType= new HashMap(5);
		setToolTipText(SearchMessages.getString("SortDropDownAction.tooltip")); //$NON-NLS-1$
		setMenuCreator(this);
	}

	public void dispose() {
		fViewer= null;
		fPageId= null;
		fLastCheckedForType= null;
	}

	public Menu getMenu(Control parent) {
		return null;
	}

	void setPageId(String pageId) {
		fPageId= pageId;
		SorterDescriptor sorterDesc= (SorterDescriptor)fLastCheckedForType.get(pageId);
		if (sorterDesc == null)
			sorterDesc= (SorterDescriptor)fgLastCheckedForType.get(pageId);
		if (sorterDesc == null)
			sorterDesc= findSorter(fPageId);
		if (sorterDesc != null) {
			fLastCheckedForType.put(fPageId, sorterDesc);
			fgLastCheckedForType.put(fPageId, sorterDesc);
			fViewer.setSorter(sorterDesc.createObject());
		}
	}

	public Menu getMenu(final Menu parent) {
		boolean hasEntries= false;
		Menu menu= new Menu(parent);
		Iterator iter= SearchPlugin.getDefault().getSorterDescriptors().iterator();
		while (iter.hasNext()) {
			Object value= fLastCheckedForType.get(fPageId);
			final String checkedId;
			if (value instanceof SorterDescriptor)
				checkedId= ((SorterDescriptor)value).getId();
			else
				checkedId= ""; //$NON-NLS-1$
			
			final SorterDescriptor sorterDesc= (SorterDescriptor) iter.next();
			if (!sorterDesc.getPageId().equals(fPageId) && !sorterDesc.getPageId().equals("*")) //$NON-NLS-1$
				continue;
			final ViewerSorter sorter= sorterDesc.createObject();
			if (sorter != null) {
				final Action action= new Action() {
					public void run() {
						if (!checkedId.equals(sorterDesc.getId())) {
							fLastCheckedForType.put(fPageId, sorterDesc);
							fgLastCheckedForType.put(fPageId, sorterDesc);
							BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
								public void run() {
									fViewer.setSorter(sorter);
								}
							});
						}
					}
				};
				action.setText(sorterDesc.getLabel());
				action.setImageDescriptor(sorterDesc.getImage());
				action.setToolTipText(sorterDesc.getToolTipText());
				action.setChecked(checkedId.equals(sorterDesc.getId()));
				addActionToMenu(menu, action);
				hasEntries= true;
			}
		}
		setEnabled(hasEntries);
		return menu;
	}

	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

    public void run() {
		// nothing to do
	    }

	private SorterDescriptor findSorter(String pageId) {
		Iterator iter= SearchPlugin.getDefault().getSorterDescriptors().iterator();
		while (iter.hasNext()) {
			SorterDescriptor sorterDesc= (SorterDescriptor)iter.next();
			if (sorterDesc.getPageId().equals(pageId) || sorterDesc.getPageId().equals("*")) //$NON-NLS-1$
				return sorterDesc;
		}
		return null;
	}
}
