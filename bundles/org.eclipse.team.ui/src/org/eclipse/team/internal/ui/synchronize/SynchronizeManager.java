/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import java.io.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.registry.SynchronizeParticipantDescriptor;
import org.eclipse.team.internal.ui.registry.SynchronizeParticipantRegistry;
import org.eclipse.team.ui.ITeamUIConstants;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.*;

/**
 * Manages the registered synchronize participants. It handles notification of
 * participant lifecycles, creation of <code>static</code> participants, and
 * the re-creation of persisted participants.
 * 
 * @see ISynchronizeView
 * @see ISynchronizeParticipant
 * @since 3.0
 */
public class SynchronizeManager implements ISynchronizeManager {
	/**
	 * Synchronize participants listeners
	 */
	private ListenerList fListeners = null;

	/**
	 * List of registered synchronize view pages {String id -> List participant
	 * instances}}
	 */
	private Map synchronizeParticipants = Collections.synchronizedMap(new HashMap(10));
	private SynchronizeParticipantRegistry participantRegistry = new SynchronizeParticipantRegistry();

	// change notification constants
	private final static int ADDED = 1;
	private final static int REMOVED = 2;

	// save context constants
	private final static String CTX_PARTICIPANTS = "syncparticipants"; //$NON-NLS-1$
	private final static String CTX_PARTICIPANT = "participant"; //$NON-NLS-1$
	private final static String CTX_ID = "id"; //$NON-NLS-1$
	private final static String CTX_PARTICIPANT_DATA = "data"; //$NON-NLS-1$
	private final static String FILENAME = "syncParticipants.xml"; //$NON-NLS-1$

	/**
	 * Notifies a participant listeners of additions or removals
	 */
	class SynchronizeViewPageNotifier implements ISafeRunnable {

		private ISynchronizeParticipantListener fListener;
		private int fType;
		private ISynchronizeParticipant[] fChanged;

		public void handleException(Throwable exception) {
			TeamUIPlugin.log(IStatus.ERROR, Policy.bind("SynchronizeManager.7"), exception); //$NON-NLS-1$
		}

		public void run() throws Exception {
			switch (fType) {
				case ADDED :
					fListener.participantsAdded(fChanged);
					break;
				case REMOVED :
					fListener.participantsRemoved(fChanged);
					break;
			}
		}

		/**
		 * Notifies the given listener of the adds/removes
		 * @param participants the participants that changed
		 * @param update the type of change
		 */
		public void notify(ISynchronizeParticipant[] participants, int update) {
			if (fListeners == null) {
				return;
			}
			fChanged = participants;
			fType = update;
			Object[] copiedListeners = fListeners.getListeners();
			for (int i = 0; i < copiedListeners.length; i++) {
				fListener = (ISynchronizeParticipantListener) copiedListeners[i];
				Platform.run(this);
			}
			fChanged = null;
			fListener = null;
		}
	}

	/**
	 * Represents a paticipant instance and allows lazy initialization of the instance
	 * only when the participant is required.
	 */
	static class ParticipantInstance {
		private ISynchronizeParticipant participant;
		private IMemento savedState;
		private SynchronizeParticipantDescriptor descriptor;
		
		public ParticipantInstance(SynchronizeParticipantDescriptor descriptor, IMemento savedState) {
			this.savedState = savedState;
			this.descriptor = descriptor;
		}
		
		public void setParticipant(ISynchronizeParticipant participant) {
			this.participant = participant;
		}
		
		public ISynchronizeParticipant getParticipant() throws TeamException {
			if (participant == null) {
				try {
					participant = (ISynchronizeParticipant) TeamUIPlugin.createExtension(descriptor.getConfigurationElement(), SynchronizeParticipantDescriptor.ATT_CLASS);
					participant.setInitializationData(descriptor.getConfigurationElement(), null, null);
					participant.init(savedState);
				} catch (PartInitException e2) {
					participant = null;					
					throw new TeamException(Policy.bind("SynchronizeManager.11"), e2);  //$NON-NLS-1$
				} catch (CoreException e) {
					participant = null;
					throw TeamException.asTeamException(e);
				}
			}
			return participant;
		}
		
		public boolean isParticipantInitialized() {
			return participant != null;
		}
		
		public IMemento getSavedState() {
			return savedState;
		}
		
		public boolean equals(Object other) {
			try {
				if(other == this) return true;
				if (other instanceof ISynchronizeParticipant) {
					return other == this.getParticipant();
				} else if(other instanceof ParticipantInstance) {
					return ((ParticipantInstance)other).getParticipant() == this.getParticipant();
				}
				return false;
			} catch (TeamException e) {
				return false;
			}
		}
		
		public void dispose() {
			if(participant != null) {
				participant.dispose();
			}
		}
		
		public String getId() {
			return descriptor.getId();
		}
	}

	public SynchronizeManager() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.ui.sync.ISynchronizeManager#addSynchronizeParticipantListener(org.eclipse.team.ui.sync.ISynchronizeParticipantListener)
	 */
	public void addSynchronizeParticipantListener(ISynchronizeParticipantListener listener) {
		if (fListeners == null) {
			fListeners = new ListenerList(5);
		}
		fListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.ui.sync.ISynchronizeManager#removeSynchronizeParticipantListener(org.eclipse.team.ui.sync.ISynchronizeParticipantListener)
	 */
	public void removeSynchronizeParticipantListener(ISynchronizeParticipantListener listener) {
		if (fListeners != null) {
			fListeners.remove(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeManager#getParticipantDescriptor(java.lang.String)
	 */
	public ISynchronizeParticipantDescriptor getParticipantDescriptor(String id) {
		return participantRegistry.find(id);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.ui.sync.ISynchronizeManager#addSynchronizeParticipants(org.eclipse.team.ui.sync.ISynchronizeParticipant[])
	 */
	public synchronized void addSynchronizeParticipants(ISynchronizeParticipant[] participants) {
		List added = new ArrayList(participants.length);
		for (int i = 0; i < participants.length; i++) {
			ISynchronizeParticipant participant = participants[i];
			ParticipantInstance instance = new ParticipantInstance(participantRegistry.find(participant.getId()), null /* no saved state */);
			instance.setParticipant(participant);
			addParticipant(instance);
			try {
				participant.init(null);
			} catch (PartInitException e) {
				TeamUIPlugin.log(IStatus.ERROR, Policy.bind("SynchronizeManager.13"), e); //$NON-NLS-1$
				continue;
			}
			added.add(participant);
		}
		if (!added.isEmpty()) {
			saveState();
			fireUpdate((ISynchronizeParticipant[]) added.toArray(new ISynchronizeParticipant[added.size()]), ADDED);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.ui.sync.ISynchronizeManager#removeSynchronizeParticipants(org.eclipse.team.ui.sync.ISynchronizeParticipant[])
	 */
	public synchronized void removeSynchronizeParticipants(ISynchronizeParticipant[] participants) {
		List removed = new ArrayList(participants.length);
		for (int i = 0; i < participants.length; i++) {
			ISynchronizeParticipant participant = participants[i];
			if (removeParticipant(participant)) {
				removed.add(participant);
			}
		}
		if (!removed.isEmpty()) {
			saveState();
			fireUpdate((ISynchronizeParticipant[]) removed.toArray(new ISynchronizeParticipant[removed.size()]), REMOVED);
			for (Iterator it = removed.iterator(); it.hasNext(); ) {
				ISynchronizeParticipant participant = (ISynchronizeParticipant) it.next();
				participant.dispose();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.ui.sync.ISynchronizeManager#getSynchronizeParticipants()
	 */
	public synchronized ISynchronizeParticipant[] getSynchronizeParticipants() {
		List participants = new ArrayList();
		for (Iterator it = synchronizeParticipants.keySet().iterator(); it.hasNext(); ) {
			String id = (String) it.next();
			ISynchronizeParticipant[] instances = find(id);
			participants.addAll(Arrays.asList(instances));
		}
		return (ISynchronizeParticipant[]) participants.toArray(new ISynchronizeParticipant[participants.size()]);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.ui.sync.ISynchronizeManager#find(java.lang.String)
	 */
	public ISynchronizeParticipant[] find(String id) {
		List instances = (List) synchronizeParticipants.get(id);
		if (instances == null) {
			return new ISynchronizeParticipant[0];
		}
		List participants = new ArrayList(instances.size());
		for (Iterator it = instances.iterator(); it.hasNext(); ) {
			ParticipantInstance instance = (ParticipantInstance) it.next();
			ISynchronizeParticipant participant;
			try {
				participant = instance.getParticipant();
				if(participant != null) {
					participants.add(participant);
				}
			} catch (TeamException e) {
				// Participant instance is invalid - remove from list
				it.remove();
				TeamUIPlugin.log(e);
			}				
		}
		return (ISynchronizeParticipant[]) participants.toArray(new ISynchronizeParticipant[participants.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeManager#showSynchronizeViewInActivePage()
	 */
	public ISynchronizeView showSynchronizeViewInActivePage() {
		IWorkbench workbench = TeamUIPlugin.getPlugin().getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

		boolean switchPerspectives = promptForPerspectiveSwitch();
		IWorkbenchPage activePage = null;
		if(switchPerspectives) {
			try {
				String pId = TeamUIPlugin.getPlugin().getPreferenceStore().getString(IPreferenceIds.SYNCVIEW_DEFAULT_PERSPECTIVE);
				activePage = workbench.showPerspective(pId, window);
			} catch (WorkbenchException e) {
				Utils.handleError(window.getShell(), e, Policy.bind("SynchronizeView.14"), e.getMessage()); //$NON-NLS-1$
			}
		}
		try {
			if (activePage == null) {
				activePage = TeamUIPlugin.getActivePage();
				if (activePage == null)
					return null;
			}
			return (ISynchronizeView) activePage.showView(ISynchronizeView.VIEW_ID);
		} catch (PartInitException pe) {
			Utils.handleError(window.getShell(), pe, Policy.bind("SynchronizeView.16"), pe.getMessage()); //$NON-NLS-1$
			return null;
		}
	}
	
	/**
	 * Decides what action to take when switching perspectives and showing the synchronize view. Basically there are a
	 * set of user preferences that control how perspective switching.
	 */
	private boolean promptForPerspectiveSwitch() {
		// Decide if a prompt is even required
		String option = TeamUIPlugin.getPlugin().getPreferenceStore().getString(IPreferenceIds.SYNCHRONIZING_COMPLETE_PERSPECTIVE);	
		if(option.equals(IPreferenceIds.SYNCHRONIZING_COMPLETE_PERSPECTIVE_ALWAYS)) {
			return true;
		} else if(option.equals(IPreferenceIds.SYNCHRONIZING_COMPLETE_PERSPECTIVE_NEVER)) {
			return false;
		}
		
		// Otherwise determine if a prompt is required
		IPerspectiveRegistry registry= PlatformUI.getWorkbench().getPerspectiveRegistry();
		String defaultSyncPerspectiveId = TeamUIPlugin.getPlugin().getPreferenceStore().getString(IPreferenceIds.SYNCVIEW_DEFAULT_PERSPECTIVE);
		IPerspectiveDescriptor perspectiveDescriptor = registry.findPerspectiveWithId(defaultSyncPerspectiveId);
		IWorkbenchPage page = TeamUIPlugin.getActivePage();
		if(page != null) {
			IPerspectiveDescriptor p = page.getPerspective();
			if(p != null && p.getId().equals(defaultSyncPerspectiveId)) {
				// currently in default perspective
				return false;
			}
		}
		
		if(perspectiveDescriptor != null) {
			String perspectiveName = perspectiveDescriptor.getLabel();
			
			MessageDialog m = new MessageDialog(Display.getDefault().getActiveShell(),
						Policy.bind("SynchronizeManager.27"),  //$NON-NLS-1$
						null,	// accept the default window icon
						Policy.bind("SynchronizeManager.30", perspectiveDescriptor.getLabel()), //$NON-NLS-1$
						MessageDialog.QUESTION, 
						new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, Policy.bind("SynchronizeManager.29"), Policy.bind("SynchronizeManager.28")}, //$NON-NLS-1$ //$NON-NLS-2$
						0); 	// yes is the default
		
			int result = m.open();
			switch (result) {
				// yes
				case 0 :
					return true;
				// no
				case 1 :
					return false;
				// always
				case 2 :
					TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.SYNCHRONIZING_COMPLETE_PERSPECTIVE, IPreferenceIds.SYNCHRONIZING_COMPLETE_PERSPECTIVE_ALWAYS);
					return true;
				// never
				case 3 :
					TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.SYNCHRONIZING_COMPLETE_PERSPECTIVE, IPreferenceIds.SYNCHRONIZING_COMPLETE_PERSPECTIVE_NEVER);
					return false;
			}
		}
		return false;
	}

	/**
	 * Creates the participant registry and restore any saved participants.
	 * Will also instantiate any static participants.
	 */
	public void init() {
		try {
			// Initialize the participant registry - reads all participant
			// extension descriptions.
			participantRegistry.readRegistry(Platform.getPluginRegistry(), TeamUIPlugin.ID, ITeamUIConstants.PT_SYNCPARTICIPANTS);

			// Instantiate and register any dynamic participants saved from a
			// previous session.
			restoreSavedParticipants();

			// Instantiate and register any static participant that has not
			// already been created.
			initializeStaticParticipants();
		} catch (CoreException e) {
			TeamUIPlugin.log(new Status(IStatus.ERROR, TeamUIPlugin.ID, 1, Policy.bind("SynchronizeManager.8"), e)); //$NON-NLS-1$
		}
	}

	/**
	 * Allow participant instances to clean-up.
	 */
	public void dispose() {
		for (Iterator it = synchronizeParticipants.keySet().iterator(); it.hasNext(); ) {
			String id = (String) it.next();
			List participants = (List) synchronizeParticipants.get(id);
			for (Iterator it2 = participants.iterator(); it2.hasNext(); ) {
				ParticipantInstance instance = (ParticipantInstance) it2.next();
				instance.dispose();
			}
		}	
		
		// save state and settings for existing participants.
		saveState();
	}
	
	private void initializeStaticParticipants() throws CoreException {
		SynchronizeParticipantDescriptor[] desc = participantRegistry.getSynchronizeParticipants();
		List participants = new ArrayList();
		for (int i = 0; i < desc.length; i++) {
			SynchronizeParticipantDescriptor descriptor = desc[i];
			if (descriptor.isStatic() && !synchronizeParticipants.containsKey(descriptor.getId())) {
				addParticipant(new ParticipantInstance(descriptor, null /* no saved state */));
			}
		}
	}

	/**
	 * Restores participants that have been saved between sessions.
	 */
	private void restoreSavedParticipants() throws TeamException, CoreException {
		File file = getStateFile();
		Reader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			return;
		}
		List participants = new ArrayList();
		IMemento memento = XMLMemento.createReadRoot(reader);
		IMemento[] participantNodes = memento.getChildren(CTX_PARTICIPANT);
		for (int i = 0; i < participantNodes.length; i++) {
			IMemento memento2 = participantNodes[i];
			String id = memento2.getString(CTX_ID);
			SynchronizeParticipantDescriptor desc = participantRegistry.find(id);
			if (desc != null) {
				IConfigurationElement cfgElement = desc.getConfigurationElement();
				addParticipant(new ParticipantInstance(desc, memento2.getChild(CTX_PARTICIPANT_DATA)));
			} else {
				TeamUIPlugin.log(new Status(IStatus.ERROR, TeamUIPlugin.ID, 1, Policy.bind("SynchronizeManager.9", id), null)); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Saves a file containing the list of participant ids that are registered
	 * with this manager. Each initialized participant is also given the chance to save
	 * it's state.
	 */
	private void saveState() {
		XMLMemento xmlMemento = XMLMemento.createWriteRoot(CTX_PARTICIPANTS);
		List children = new ArrayList();
		for (Iterator it = synchronizeParticipants.keySet().iterator(); it.hasNext(); ) {
			String id = (String) it.next();
			List participants = (List) synchronizeParticipants.get(id);
			for (Iterator it2 = participants.iterator(); it2.hasNext(); ) {
				ParticipantInstance instance = (ParticipantInstance) it2.next();

				// Participants can opt out of being saved between sessions
				if(instance.isParticipantInitialized()) {
					ISynchronizeParticipant participant;
					try {
						participant = instance.getParticipant();
					} catch (TeamException e1) {
						continue;
					}
					if(! participant.isPersistent()) continue;
				}
				
				// Create the state placeholder for a participant 
				IMemento participantNode = xmlMemento.createChild(CTX_PARTICIPANT);
				participantNode.putString(CTX_ID, instance.getId());				
				IMemento participantData = participantNode.createChild(CTX_PARTICIPANT_DATA);
				
				// Allow the participant to save it's state. If the participant exists
				// but isn't instantiated any loaded state will be re-saved.
				if(instance.isParticipantInitialized()) {
					ISynchronizeParticipant participant;
					try {
						participant = instance.getParticipant();
					} catch (TeamException e1) {
						// Continue with the next participant instance.
						continue;
					}
					participant.saveState(participantData);
				} else {
					IMemento savedState = instance.getSavedState();
					if(savedState != null) {
						participantData.putMemento(savedState);
					}
				}
			}
		}
		try {
			Writer writer = new BufferedWriter(new FileWriter(getStateFile()));
			try {
				xmlMemento.save(writer);
			} finally {
				writer.close();
			}
		} catch (IOException e) {
			TeamUIPlugin.log(new Status(IStatus.ERROR, TeamUIPlugin.ID, 1, Policy.bind("SynchronizeManager.10"), e)); //$NON-NLS-1$
		}
	}

	private File getStateFile() {
		IPath pluginStateLocation = TeamUIPlugin.getPlugin().getStateLocation();
		return pluginStateLocation.append(FILENAME).toFile(); //$NON-NLS-1$	
	}
	
	private synchronized void addParticipant(ParticipantInstance instance) {
		String id = instance.getId();
		List instances = (List) synchronizeParticipants.get(id);
		if (instances == null) {
			instances = new ArrayList(2);
			synchronizeParticipants.put(id, instances);
		}
		instances.add(instance);
	}
	
	private synchronized boolean removeParticipant(ISynchronizeParticipant participant) {
		boolean removed = false;
		String id = participant.getId();
		List instances = (List) synchronizeParticipants.get(id);
		if (instances != null) {
			Iterator it = instances.iterator();
			ParticipantInstance instance = null;
			while (it.hasNext()) {
				ParticipantInstance tempInstance = (ParticipantInstance) it.next();
				try {
					if(tempInstance.getParticipant() == participant) {
						instance = tempInstance;
					}
				} catch (TeamException e) {
					// Participant instance is invalid - remove from list
					synchronizeParticipants.remove(tempInstance);
					TeamUIPlugin.log(e);
				}
			}
			if(instance != null) {
				removed = instances.remove(instance);
				if (instances.isEmpty()) {
					synchronizeParticipants.remove(id);
				}
			}
		}
		return removed;
	}
	
	/**
	 * Fires notification.
	 * @param participants participants added/removed
	 * @param type ADD or REMOVE
	 */
	private void fireUpdate(ISynchronizeParticipant[] participants, int type) {
		new SynchronizeViewPageNotifier().notify(participants, type);
	}
}