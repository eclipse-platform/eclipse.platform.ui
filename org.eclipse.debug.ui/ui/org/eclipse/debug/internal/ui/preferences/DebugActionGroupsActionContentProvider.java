package org.eclipse.debug.internal.ui.preferences;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.preferences.DebugActionGroupsManager.DebugActionGroup;
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
			List allActionIds= actionSet.getActionIds();
			Iterator actionIds= allActionIds.iterator();
			actionContributionItems= new ArrayList(allActionIds.size());
			while (actionIds.hasNext()) {
				String actionId= (String)actionIds.next();
				Map idsToActions= DebugActionGroupsManager.getDefault().fDebugActionGroupActions;
				List actions= (List)idsToActions.get(actionId);
				if (actions != null) {
					actionContributionItems.addAll(actions);
				}
			}
		}
		if (actionContributionItems != null) {
			if (actionContributionItems.isEmpty()) {
				return new String[]{DebugUIMessages.getString("DebugActionGroupsActionContentProvider.Updated_when_Debug_perspective_activated_1")}; //$NON-NLS-1$
			} else {
				return actionContributionItems.toArray();
			}
		} else {
			return new String[]{DebugUIMessages.getString("DebugActionGroupsActionContentProvider.Updated_when_Debug_perspective_activated_1")}; //$NON-NLS-1$
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