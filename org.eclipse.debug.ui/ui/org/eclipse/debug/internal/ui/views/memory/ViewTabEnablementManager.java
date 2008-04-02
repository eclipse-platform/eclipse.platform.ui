/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - Ted Williams - [Memory View] Memory View: Workflow Enhancements (Bug 215432)
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

/**
 * Controls view tab enablement based on tab folder selection
 * 
 * @since 3.0
 */
public class ViewTabEnablementManager implements SelectionListener {

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() instanceof CTabFolder)
		{
			handleTabFolderSelection(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		 
		
	}
	
	private void handleTabFolderSelection(SelectionEvent event)
	{
		CTabFolder folder = (CTabFolder)event.getSource();
		
		CTabItem selectedItem = folder.getSelection();
		IMemoryViewTab selectedViewTab;
		
		// get selected view tab
		if (selectedItem != null)
		{			
			Object obj = selectedItem.getData();
			
			if (obj instanceof IMemoryViewTab)
			{
				selectedViewTab = (IMemoryViewTab)obj;
			}
			else
			{
				return;
			}
		}
		else
		{
			return;
		}
		
		// get all tabs
		CTabItem[] allTabs = folder.getItems();
		
		// check all tabs to make sure they are enabled/disabled properly
		for (int i=0; i<allTabs.length; i++)
		{	
			IMemoryViewTab viewTab;
			Object obj = allTabs[i].getData();
			
			if (obj instanceof IMemoryViewTab)
			{	
				viewTab = (IMemoryViewTab)obj;
				
				// if view tab matches the selected item
				if (viewTab == selectedViewTab && !viewTab.isEnabled() )
				{
					// if the item has been selected and this tab is not enabled
					// enable it.
					viewTab.setEnabled(true);
					viewTab.getRendering().activated();
					
				}
				else if (viewTab != selectedViewTab && viewTab.isEnabled())
				{
					// if the tab is not selected, disable it
					viewTab.setEnabled(false);
					viewTab.getRendering().deactivated();
				}	
			}
		}
	}

}
