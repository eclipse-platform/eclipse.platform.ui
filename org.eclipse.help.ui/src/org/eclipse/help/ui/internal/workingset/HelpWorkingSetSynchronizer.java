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

	private IWorkingSetManager eclipseWSM;
	private WorkingSetManager helpWSM;
	private WorkingSetMapping mapping;

	private static class WorkingSetPair {
		public WorkingSet helpWorkingSet;
		public IWorkingSet eclipseWorkingSet;

		public WorkingSetPair(WorkingSet hws, IWorkingSet ews) {
			helpWorkingSet = hws;
			eclipseWorkingSet = ews;
		}
	}

	private static class WorkingSetMapping extends ArrayList {

		private void addMapping(WorkingSet hws, IWorkingSet ews) {
			add(new WorkingSetPair(hws, ews));
		}

		private void removeMapping(WorkingSet hws, IWorkingSet ews) {

		}

		private IWorkingSet findMapping(WorkingSet hws) {
			for (Iterator it = iterator(); it.hasNext();) {
				WorkingSetPair wsp = (WorkingSetPair) it.next();
				if (wsp.helpWorkingSet == hws)
					return wsp.eclipseWorkingSet;
			}
			return null;
		}

		private WorkingSet findMapping(IWorkingSet ews) {
			for (Iterator it = iterator(); it.hasNext();) {
				WorkingSetPair wsp = (WorkingSetPair) it.next();
				if (wsp.eclipseWorkingSet == ews)
					return wsp.helpWorkingSet;
			}
			return null;
		}
	}

	/**
	 * Constructor
	 */
	public HelpWorkingSetSynchronizer() {
		eclipseWSM = PlatformUI.getWorkbench().getWorkingSetManager();
		helpWSM = HelpSystem.getWorkingSetManager();
		mapping = new WorkingSetMapping();
	}
	/**
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event
			.getProperty()
			.equals(IWorkingSetManager.CHANGE_WORKING_SET_ADD)) {
			// add the eclipse working set to the help working sets
			IWorkingSet ws = (IWorkingSet) event.getNewValue();
			if (helpWSM.getWorkingSet(ws.getName()) == null) {
				WorkingSet w = createHelpWorkingSet(ws);
				if (w != null) {
					helpWSM.addWorkingSet(w);
					mapping.addMapping(w, ws);
				}
			}
		} else if (event.getProperty().equals(IWorkingSetManager.CHANGE_WORKING_SET_REMOVE)) {
			// remove the help working set
			IWorkingSet ws = (IWorkingSet) event.getOldValue();
			WorkingSet w = helpWSM.getWorkingSet(ws.getName());
			if (w != null) {
				helpWSM.removeWorkingSet(w);
				mapping.removeMapping(w, ws);
			}
		} else if (event.getProperty().equals(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE)) {
			// change the content of the help working set
			IWorkingSet ws = (IWorkingSet) event.getNewValue();
			WorkingSet w = mapping.findMapping(ws);
			if (w == null)
				System.out.println("error");
			else
				w.setElements(getElements(ws));

		} else if (event.getProperty().equals(IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE)) {
			// change the name of the help working set
			IWorkingSet ws = (IWorkingSet) event.getNewValue();
			WorkingSet w = mapping.findMapping(ws);
			if (w == null)
				System.out.println("error");
			else
				w.setName(ws.getName());
		}
	}

	/**
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChange.PropertyChangeEvent event) {
		if (event.getProperty().equals(WorkingSetManager.CHANGE_WORKING_SET_ADD)) {
			// add an eclipse working set
			WorkingSet ws = (WorkingSet) event.getNewValue();
			if (eclipseWSM.getWorkingSet(ws.getName()) == null) {
				IWorkingSet w = createEclipseWorkingSet(ws);
				eclipseWSM.addWorkingSet(w);
				mapping.addMapping(ws, w);
			}
		} else if (event.getProperty().equals(WorkingSetManager.CHANGE_WORKING_SET_REMOVE)) {
			// remove the eclipse working set
			WorkingSet ws = (WorkingSet) event.getOldValue();
			if (eclipseWSM.getWorkingSet(ws.getName()) != null) {
				IWorkingSet w = eclipseWSM.getWorkingSet(ws.getName());
				eclipseWSM.removeWorkingSet(w);
				mapping.removeMapping(ws, w);
			}
		} else if (event.getProperty().equals(WorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE)) {
			// change the content of the eclipse working set
			WorkingSet ws = (WorkingSet) event.getNewValue();
			IWorkingSet w = mapping.findMapping(ws);
			if (w == null)
				System.out.println("error: cannot find IWorkingSet " + ws);
			else
				w.setElements(ws.getElements());
		} else if (event.getProperty().equals(WorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE)) {
			// change the name of the eclipse working set
			WorkingSet ws = (WorkingSet) event.getNewValue();
			IWorkingSet w = mapping.findMapping(ws);
			if (w == null)
				System.out.println("error: cannot find WorkingSet " + ws);
			else
				w.setName(ws.getName());
		}
	}

	private IWorkingSet createEclipseWorkingSet(WorkingSet ws) {
		IWorkingSet w = eclipseWSM.createWorkingSet(ws.getName(), ws.getElements());
		if (w instanceof org.eclipse.ui.internal.WorkingSet)
			// the id of the workingSet extension point in plugin.xml
			((org.eclipse.ui.internal.WorkingSet) w).setEditPageId("org.eclipse.help.ui.HelpWorkingSetPage");
		return w;
	}

	private WorkingSet createHelpWorkingSet(IWorkingSet ws) {
		if (!isHelpWorkingSet(ws))
			return null;

		return helpWSM.createWorkingSet(ws.getName(), getElements(ws));
	}

	private AdaptableHelpResource[] getElements(IWorkingSet ws) {
		IAdaptable[] elements = ws.getElements();
		AdaptableHelpResource[] helpResources = new AdaptableHelpResource[elements.length];
		for (int i=0; i<elements.length; i++)
			if (elements[i] instanceof AdaptableHelpResource)
				helpResources[i] = (AdaptableHelpResource)elements[i];
			else
				return new AdaptableHelpResource[0];
				
		return helpResources;
	}
	private boolean isHelpWorkingSet(IWorkingSet ws) {
		IAdaptable[] elements = ws.getElements();
		return (elements.length > 0 && elements[0] instanceof AdaptableHelpResource);
	}

}
