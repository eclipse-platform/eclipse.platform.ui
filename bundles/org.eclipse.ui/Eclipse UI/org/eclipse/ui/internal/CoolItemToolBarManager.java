package org.eclipse.ui.internal;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

/**
 * @author Lynne
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class CoolItemToolBarManager extends ToolBarManager {
	public CoolItemToolBarManager(int style) {
		super(style);
	}
	protected void relayout(ToolBar toolBar, int oldCount, int newCount) {
		if (oldCount == newCount) return;
		CoolBar coolBar = (CoolBar)toolBar.getParent();
		CoolItem[] coolItems = coolBar.getItems();
		CoolItem coolItem = null;
		for (int i = 0; i < coolItems.length; i++) {	
			CoolItem item = coolItems[i];
			if (item.getControl() == toolBar) {
				coolItem = item;
				break;
			}						
		}
		// recompute preferred size so chevron will work correctly when
		// items are added/removed from the toolbar, don't set the size of
		// the coolItem since that would affect the position of other
		// coolItems on the toolbar
		if (coolItem != null) {
			Point size = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			Point coolSize = coolItem.computeSize (size.x, size.y);
			coolItem.setPreferredSize(coolSize);
		}
		coolBar.layout();
	} 
}      
