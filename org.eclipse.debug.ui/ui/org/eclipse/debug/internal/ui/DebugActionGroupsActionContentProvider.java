package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.internal.ui.DebugActionGroupsManager.DebugActionGroup;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class DebugActionGroupsActionContentProvider implements IStructuredContentProvider {

	/**
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object element) {
		List actionContributionItems= null;
		if (element instanceof DebugActionGroup) {
			DebugActionGroup actionSet= (DebugActionGroup)element;
			List actions= actionSet.getActionIds();
			Iterator actionIds= actions.iterator();
			actionContributionItems= new ArrayList(actions.size());
			while (actionIds.hasNext()) {
				String actionId= (String)actionIds.next();
				Map items= DebugActionGroupsManager.getDefault().fDebugActionGroupActions;
				Object item= items.get(actionId);
				if (item != null) {
					actionContributionItems.add(item);
				}
			}
		}
		if (actionContributionItems != null) {
			return actionContributionItems.toArray();
		} else {
			return new Object[]{};
		}
	}
	/**
	 * @see IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}