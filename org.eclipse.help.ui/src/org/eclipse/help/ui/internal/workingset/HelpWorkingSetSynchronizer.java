package org.eclipse.help.ui.internal.workingset;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.workingset.*;
import org.eclipse.help.ui.internal.*;
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
	 * @see org.eclipse.help.internal.workingset.PropertyChange.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event
			.getProperty()
			.equals(IWorkingSetManager.CHANGE_WORKING_SET_ADD)) {

			// add the help working set
			IWorkingSet iws = (IWorkingSet) event.getNewValue();
			addWorkingSet(iws);
		} else if (
			event.getProperty().equals(
				IWorkingSetManager.CHANGE_WORKING_SET_REMOVE)) {

			// remove the help working set
			IWorkingSet iws = (IWorkingSet) event.getOldValue();
			removeWorkingSet(iws);
		} else if (
			event.getProperty().equals(
				IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE)) {

			// rename the help working set
			IWorkingSet iws = (IWorkingSet) event.getNewValue();
			renameWorkingSet(iws);
		} else if (
			event.getProperty().equals(
				IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE)) {

			// change the content of the help working set
			IWorkingSet iws = (IWorkingSet) event.getNewValue();
			changeWorkingSet(iws);
		}
	}

	/**
	 * @see org.eclipse.help.internal.workingset.PropertyChange.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChange.PropertyChangeEvent event) {
		if (event
			.getProperty()
			.equals(WorkingSetManager.CHANGE_WORKING_SET_ADD)) {

			// add an eclipse working set
			WorkingSet ws = (WorkingSet) event.getNewValue();
			addWorkingSet(ws);
		} else if (
			event.getProperty().equals(
				WorkingSetManager.CHANGE_WORKING_SET_REMOVE)) {

			// remove the eclipse working set
			WorkingSet ws = (WorkingSet) event.getOldValue();
			removeWorkingSet(ws);
		} else if (
			event.getProperty().equals(
				WorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE)) {

			// change the name of the eclipse working set
			WorkingSet ws = (WorkingSet) event.getNewValue();
			renameWorkingSet(ws);
		} else if (
			event.getProperty().equals(
				WorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE)) {

			// change the content of the eclipse working set
			WorkingSet ws = (WorkingSet) event.getNewValue();
			changeWorkingSet(ws);
		} else if (
			event.getProperty().equals(
				WorkingSetManager.CHANGE_WORKING_SETS_SYNCH)) {

			// remove working sets not present in the UI
			WorkingSet[] baseWorkingSets =
				getHelpWorkingSetManager().getWorkingSets();
			for (int i = 0; i < baseWorkingSets.length; i++) {
				IWorkingSet iws =
					getEclipseWorkingSetManager().getWorkingSet(
						baseWorkingSets[i].getName());
				if (iws == null)
					getHelpWorkingSetManager().removeWorkingSet(
						baseWorkingSets[i]);
			}
		}
	}

	public void renameWorkingSet(IWorkingSet iws) {
		HelpWorkingSet hws = findWorkingSet(iws);
		if (hws != null) {
			hws.getWorkingSet().setName(iws.getName());
		}
	}
	public void changeWorkingSet(IWorkingSet iws) {
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

	public void addWorkingSet(IWorkingSet iws) {
		WorkingSet ws = getHelpWorkingSetManager().getWorkingSet(iws.getName());
		if (ws == null && isHelpWorkingSet(iws)) {
			HelpWorkingSet hws = new HelpWorkingSet(iws);
			workingSets.add(hws);
			getHelpWorkingSetManager().addWorkingSet(hws.getWorkingSet());
		}
		// see if this is happening upon workbench startup 
		if (ws != null && findWorkingSet(iws) == null) {
			HelpWorkingSet hws = new HelpWorkingSet(ws, iws);
			workingSets.add(hws);
			iws.setElements(ws.getElements());
		}
	}
	public void removeWorkingSet(IWorkingSet iws) {
		WorkingSet ws = getHelpWorkingSetManager().getWorkingSet(iws.getName());
		if (ws != null) {
			HelpWorkingSet hws = findWorkingSet(iws);
			if (hws != null)
				workingSets.remove(hws);
			getHelpWorkingSetManager().removeWorkingSet(ws);
		}
	}

	public void renameWorkingSet(WorkingSet ws) {
		HelpWorkingSet hws = findWorkingSet(ws);
		if (hws != null) {
			hws.getIWorkingSet().setName(ws.getName());
		}
	}
	public void changeWorkingSet(WorkingSet ws) {
		HelpWorkingSet hws = findWorkingSet(ws);
		if (hws != null) {
			hws.getIWorkingSet().setElements(ws.getElements());
		}
	}
	public void removeWorkingSet(WorkingSet ws) {
		IWorkingSet iws =
			getEclipseWorkingSetManager().getWorkingSet(ws.getName());
		if (iws != null) {
			HelpWorkingSet hws = findWorkingSet(ws);
			if (hws != null)
				workingSets.remove(hws);
			getEclipseWorkingSetManager().removeWorkingSet(iws);
		}
	}
	public void addWorkingSet(WorkingSet ws) {
		IWorkingSet iws =
			getEclipseWorkingSetManager().getWorkingSet(ws.getName());
		if (iws == null) {
			HelpWorkingSet hws = new HelpWorkingSet(ws);
			workingSets.add(hws);
			getEclipseWorkingSetManager().addWorkingSet(hws.getIWorkingSet());
		} else if (findWorkingSet(ws) == null) {
			HelpWorkingSet hws = new HelpWorkingSet(ws, iws);
			workingSets.add(hws);
		}
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
