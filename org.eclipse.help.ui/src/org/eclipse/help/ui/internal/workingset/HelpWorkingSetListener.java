package org.eclipse.help.ui.internal.workingset;

import org.eclipse.core.boot.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
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
public class HelpWorkingSetListener
	implements IPropertyChangeListener, PropertyChange.IPropertyChangeListener {

	private IWorkingSetManager eclipseWSM;
	private WorkingSetManager helpWSM;

	/**
	 * Constructor
	 */
	public HelpWorkingSetListener() {
		eclipseWSM = PlatformUI.getWorkbench().getWorkingSetManager();
		helpWSM = HelpSystem.getWorkingSetManager();
	}
	/**
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event
			.getProperty()
			.equals(IWorkingSetManager.CHANGE_WORKING_SET_ADD)) {
			IWorkingSet ws = (IWorkingSet)event.getNewValue();
			if (helpWSM.getWorkingSet(ws.getName()) == null) {
				WorkingSet w = asHelpWorkingSet(ws);
				if (w != null)	
					helpWSM.addWorkingSet(w);
			}
		} else if (
			event.getProperty().equals(
				IWorkingSetManager.CHANGE_WORKING_SET_REMOVE)) {
			IWorkingSet ws = (IWorkingSet)event.getOldValue();
			if (helpWSM.getWorkingSet(ws.getName()) != null)
				helpWSM.removeWorkingSet(helpWSM.getWorkingSet(ws.getName()));
		} else if (
			event.getProperty().equals(
				IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE)) {
		} else if (
			event.getProperty().equals(
				IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE)) {
		}
	}

	/**
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChange.PropertyChangeEvent event) {
		if (event
			.getProperty()
			.equals(WorkingSetManager.CHANGE_WORKING_SET_ADD)) {
			WorkingSet ws = (WorkingSet)event.getNewValue();
			if (eclipseWSM.getWorkingSet(ws.getName()) == null)
				eclipseWSM.addWorkingSet(asEclipseWorkingSet(ws));
		} else if (
			event.getProperty().equals(
				WorkingSetManager.CHANGE_WORKING_SET_REMOVE)) {
			WorkingSet ws = (WorkingSet)event.getOldValue();
			if (eclipseWSM.getWorkingSet(ws.getName()) != null)
				eclipseWSM.removeWorkingSet(eclipseWSM.getWorkingSet(ws.getName()));
		} else if (
			event.getProperty().equals(
				WorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE)) {
		} else if (
			event.getProperty().equals(
				WorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE)) {
		}
	}
	
	private IWorkingSet asEclipseWorkingSet(WorkingSet ws) {
		IHelpResource[] resources = ws.getElements();
		HelpResource[] elements = new HelpResource[resources.length];
		for (int i=0; i<elements.length; i++)
			elements[i] = new HelpResource(resources[i]);
		return eclipseWSM.createWorkingSet(ws.getName(), elements);
	}
	
	private WorkingSet asHelpWorkingSet(IWorkingSet ws) {
		IAdaptable[] elements = ws.getElements();
		if (elements.length == 0 || !(elements[0] instanceof HelpResource))
			return null;
		IHelpResource[] helpResources = new IHelpResource[elements.length];
		for (int i=0; i<elements.length; i++) {
			Object adapter = elements[i].getAdapter(IHelpResource.class);
			if (adapter != null)
				helpResources[i] = (IHelpResource)adapter;
			else
				return null;
		}
			
		return helpWSM.createWorkingSet(ws.getName(), helpResources);
	}
}
