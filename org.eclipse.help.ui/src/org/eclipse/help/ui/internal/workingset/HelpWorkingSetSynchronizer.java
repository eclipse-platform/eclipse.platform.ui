package org.eclipse.help.ui.internal.workingset;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.workingset.*;
import org.eclipse.jface.util.*;
import org.eclipse.ui.*;

/*
 * (c) Copyright IBM Corp. 2002. 
 * All Rights Reserved.
 */

/**
 * Listener for changes in the help and ui working set managers to keep them in
 * synch.
 */
public class HelpWorkingSetSynchronizer
	implements IPropertyChangeListener, PropertyChange.IPropertyChangeListener {

	private IWorkingSetManager eclipseWorkingSetManager;
	private WorkingSetManager helpWorkingSetManager;
	/**
	 * Constructor
	 */
	public HelpWorkingSetSynchronizer() {
		eclipseWorkingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		helpWorkingSetManager = HelpSystem.getWorkingSetManager();
	}
	/**
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IWorkingSetManager.CHANGE_WORKING_SET_REMOVE)) {
			// remove the help working set
			IWorkingSet iws = (IWorkingSet) event.getOldValue();
			WorkingSet ws = helpWorkingSetManager.getWorkingSet(iws.getName());
			if (ws != null) {
				helpWorkingSetManager.removeWorkingSet(ws);
			}
		}
	}

	/**
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChange.PropertyChangeEvent event) {
		if (event.getProperty().equals(WorkingSetManager.CHANGE_WORKING_SET_ADD)) {
			// add an eclipse working set
			WorkingSet ws = (WorkingSet) event.getNewValue();
			if (eclipseWorkingSetManager.getWorkingSet(ws.getName()) == null) {
				IWorkingSet iws = createEclipseWorkingSet(ws);
				eclipseWorkingSetManager.addWorkingSet(iws);
			}
		} else if (event.getProperty().equals(WorkingSetManager.CHANGE_WORKING_SET_REMOVE)) {
			// remove the eclipse working set
			WorkingSet ws = (WorkingSet) event.getOldValue();
			if (eclipseWorkingSetManager.getWorkingSet(ws.getName()) != null) {
				IWorkingSet iws = eclipseWorkingSetManager.getWorkingSet(ws.getName());
				eclipseWorkingSetManager.removeWorkingSet(iws);
			}
		} 
	}

	/**
	 * NOTE: DEPENDENCY ON  INTERNAL UI CLASS.
	 * TODO: change this when ui bug is fixed.
	 * @param ws
	 * @return IWorkingSet
	 */
	private IWorkingSet createEclipseWorkingSet(WorkingSet ws) {
		IWorkingSet iws = eclipseWorkingSetManager.createWorkingSet(ws.getName(), ws.getElements());
		if (iws instanceof org.eclipse.ui.internal.WorkingSet)
			// the id of the workingSet extension point in plugin.xml
			((org.eclipse.ui.internal.WorkingSet) iws).setEditPageId("org.eclipse.help.ui.HelpWorkingSetPage");
		return iws;
	}

	private boolean isHelpWorkingSet(IWorkingSet ws) {
		IAdaptable[] elements = ws.getElements();
		return (elements.length > 0 && elements[0] instanceof AdaptableHelpResource);
	}

}
