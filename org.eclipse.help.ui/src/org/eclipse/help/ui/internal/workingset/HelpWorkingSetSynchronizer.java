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

	private ArrayList workingSets;

	/**
	 * Constructor
	 */
	public HelpWorkingSetSynchronizer() {
		workingSets = new ArrayList();
	}
	/**
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event
			.getProperty()
			.equals(IWorkingSetManager.CHANGE_WORKING_SET_ADD)) {

			// add the help working set
			IWorkingSet iws = (IWorkingSet) event.getNewValue();
			WorkingSet ws = getHelpWorkingSetManager().getWorkingSet(iws.getName());
			if (ws == null && isHelpWorkingSet(iws)) {
				HelpWorkingSet hws = new HelpWorkingSet(iws);
				workingSets.add(hws);
				getHelpWorkingSetManager().addWorkingSet(hws.getWorkingSet());
			}
			// see if this is happening upon workbench startup 
			if (ws != null && findWorkingSet(iws) == null) {
				getEclipseWorkingSetManager().removeWorkingSet(iws);
				HelpWorkingSet hws = new HelpWorkingSet(ws);
				workingSets.add(hws);
				getEclipseWorkingSetManager().addWorkingSet(hws.getIWorkingSet());
			}
		} else if (
			event.getProperty().equals(
				IWorkingSetManager.CHANGE_WORKING_SET_REMOVE)) {

			// remove the help working set
			IWorkingSet iws = (IWorkingSet) event.getOldValue();
			WorkingSet ws = getHelpWorkingSetManager().getWorkingSet(iws.getName());
			if (ws != null) {
				HelpWorkingSet hws = findWorkingSet(iws);
				if (hws != null)
					workingSets.remove(hws);
				getHelpWorkingSetManager().removeWorkingSet(ws);
			}
		} else if (
			event.getProperty().equals(
				IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE)) {

			// rename the help working set
			IWorkingSet iws = (IWorkingSet) event.getNewValue();
			HelpWorkingSet hws = findWorkingSet(iws);
			if (hws != null) {
				hws.getWorkingSet().setName(iws.getName());
			}
		} else if (
			event.getProperty().equals(
				IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE)) {

			// change the content of the help working set
			IWorkingSet iws = (IWorkingSet) event.getNewValue();
			HelpWorkingSet hws = findWorkingSet(iws);
			if (hws != null) {
				AdaptableHelpResource[] elements =
					new AdaptableHelpResource[iws.getElements().length];
				System.arraycopy(
					iws.getElements(),
					0,
					elements,
					0,
					elements.length);
				hws.getWorkingSet().setElements(elements);
			}
		}
	}

	/**
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChange.PropertyChangeEvent event) {
		if (event
			.getProperty()
			.equals(WorkingSetManager.CHANGE_WORKING_SET_ADD)) {

			// add an eclipse working set
			WorkingSet ws = (WorkingSet) event.getNewValue();
			IWorkingSet iws = getEclipseWorkingSetManager().getWorkingSet(ws.getName());
			if (iws == null) {
				HelpWorkingSet hws = new HelpWorkingSet(ws);
				workingSets.add(hws);
				getEclipseWorkingSetManager().addWorkingSet(hws.getIWorkingSet());
			} else if (findWorkingSet(ws) == null) {
				HelpWorkingSet hws = new HelpWorkingSet(ws, iws);
				workingSets.add(hws);
			}
		} else if (
			event.getProperty().equals(
				WorkingSetManager.CHANGE_WORKING_SET_REMOVE)) {

			// remove the eclipse working set
			WorkingSet ws = (WorkingSet) event.getOldValue();
			IWorkingSet iws =
				getEclipseWorkingSetManager().getWorkingSet(ws.getName());
			if (iws != null) {
				HelpWorkingSet hws = findWorkingSet(ws);
				if (hws != null)
					workingSets.remove(hws);
				getEclipseWorkingSetManager().removeWorkingSet(iws);
			}
		} else if (
			event.getProperty().equals(
				WorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE)) {

			// change the name of the eclipse working set
			WorkingSet ws = (WorkingSet) event.getNewValue();
			HelpWorkingSet hws = findWorkingSet(ws);
			if (hws != null) {
				hws.getIWorkingSet().setName(ws.getName());
			}
		} else if (
			event.getProperty().equals(
				WorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE)) {

			// change the content of the eclipse working set
			WorkingSet ws = (WorkingSet) event.getNewValue();
			HelpWorkingSet hws = findWorkingSet(ws);
			if (hws != null) {
				hws.getIWorkingSet().setElements(ws.getElements());
			}
		}
	}

	private IWorkingSet createEclipseWorkingSet(WorkingSet ws) {
		IWorkingSet w =
			getEclipseWorkingSetManager().createWorkingSet(
				ws.getName(),
				ws.getElements());
		if (w instanceof org.eclipse.ui.internal.WorkingSet)
			// the id of the workingSet extension point in plugin.xml
			((org.eclipse.ui.internal.WorkingSet) w).setEditPageId(
				"org.eclipse.help.ui.HelpWorkingSetPage");
		return w;
	}

	private WorkingSet createHelpWorkingSet(IWorkingSet ws) {
		if (!isHelpWorkingSet(ws))
			return null;

		return getHelpWorkingSetManager().createWorkingSet(
			ws.getName(),
			getElements(ws));
	}

	private AdaptableHelpResource[] getElements(IWorkingSet ws) {
		IAdaptable[] elements = ws.getElements();
		AdaptableHelpResource[] helpResources =
			new AdaptableHelpResource[elements.length];
		for (int i = 0; i < elements.length; i++)
			if (elements[i] instanceof AdaptableHelpResource)
				helpResources[i] = (AdaptableHelpResource) elements[i];
			else
				return new AdaptableHelpResource[0];

		return helpResources;
	}
	private boolean isHelpWorkingSet(IWorkingSet ws) {
		IAdaptable[] elements = ws.getElements();
		return (
			elements.length > 0
				&& elements[0] instanceof AdaptableHelpResource);
	}

	private HelpWorkingSet findWorkingSet(WorkingSet ws) {
		for (Iterator it = workingSets.iterator(); it.hasNext();) {
			HelpWorkingSet hws = (HelpWorkingSet) it.next();
			if (hws.getWorkingSet() == ws)
				return hws;
		}
		return null;
	}

	private HelpWorkingSet findWorkingSet(IWorkingSet iws) {
		for (Iterator it = workingSets.iterator(); it.hasNext();) {
			HelpWorkingSet hws = (HelpWorkingSet) it.next();
			if (hws.getIWorkingSet() == iws)
				return hws;
		}
		return null;
	}

	private IWorkingSetManager getEclipseWorkingSetManager() {
		return PlatformUI.getWorkbench().getWorkingSetManager();
	}

	private WorkingSetManager getHelpWorkingSetManager() {
		return HelpSystem.getWorkingSetManager();
	}
}
