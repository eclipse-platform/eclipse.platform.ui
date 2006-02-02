/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import java.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.actions.*;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.operations.ModelSynchronizeParticipant;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.*;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * Implements a Synchronize View that contains multiple synchronize participants. 
 */
public class SynchronizeView extends PageBookView implements ISynchronizeView, ISynchronizeParticipantListener, IPropertyChangeListener, ISaveableModelSource, ISaveablePart {
	
	/**
	 * Suggested maximum length of participant names when shown in certain menus and dialog.
	 */
	public final static int MAX_NAME_LENGTH = 100;
	
	/**
	 * The participant being displayed, or <code>null</code> if none
	 */
	private ISynchronizeParticipant activeParticipantRef = null;
	
	/**
	 * Map of participants to dummy participant parts (used to close pages)
	 */
	private Map fParticipantToPart;
	
	/**
	 * Map of parts to participants
	 */
	private Map fPartToParticipant;

	/**
	 * Drop down action to switch between participants
	 */
	private SynchronizePageDropDownAction fPageDropDown;
	
	/**
	 * Action to remove the selected participant
	 */
	private PinParticipantAction fPinAction;
	
	/**
	 * Action to remove the currently shown partipant
	 */
	private RemoveSynchronizeParticipantAction fRemoveCurrentAction;
	
	/**
	 * Action to remove all non-pinned participants
	 */
	private RemoveSynchronizeParticipantAction fRemoveAllAction;
	
	/**
	 * Preference key to save
	 */
	private static final String KEY_LAST_ACTIVE_PARTICIPANT_ID = "lastactiveparticipant_id"; //$NON-NLS-1$
    private static final String KEY_LAST_ACTIVE_PARTICIPANT_SECONDARY_ID = "lastactiveparticipant_sec_id"; //$NON-NLS-1$
	private static final String KEY_SETTINGS_SECTION= "SynchronizeViewSettings"; //$NON-NLS-1$


	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		Object source = event.getSource();
		if (source instanceof ISynchronizeParticipant) {
			if (event.getProperty().equals(IBasicPropertyConstants.P_TEXT)) {
				if (source.equals(getParticipant())) {
					updateTitle();
				}
			} else if (event.getProperty().equals(ModelSynchronizeParticipant.PROP_DIRTY)) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						firePropertyChange(PROP_DIRTY);
					}
				});
			}
		}
		if (source instanceof ISynchronizePageConfiguration) {
			ISynchronizePageConfiguration configuration = (ISynchronizePageConfiguration) source;
			if (event.getProperty().equals(ISynchronizePageConfiguration.P_PAGE_DESCRIPTION)) {
				if (configuration.getParticipant().equals(getParticipant())) {
					updateTitle();
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		super.partClosed(part);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.sync.ISynchronizeView#getParticipant()
	 */
	public ISynchronizeParticipant getParticipant() {
		return activeParticipantRef;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#showPageRec(org.eclipse.ui.part.PageBookView.PageRec)
	 */
	protected void showPageRec(PageRec pageRec) {
		super.showPageRec(pageRec);
		activeParticipantRef = (ISynchronizeParticipant)fPartToParticipant.get(pageRec.part);
		updateActionEnablements();
		updateTitle();		
	}

	/*
	 * Updates the view title based on the active participant
	 */
	protected void updateTitle() {
		ISynchronizeParticipant participant = getParticipant();
		if (participant == null) {
			setContentDescription(""); //$NON-NLS-1$
		} else {
			SynchronizeViewWorkbenchPart part = (SynchronizeViewWorkbenchPart)fParticipantToPart.get(participant);
			ISynchronizePageConfiguration configuration = part.getConfiguration();
			String description = (String)configuration.getProperty(ISynchronizePageConfiguration.P_PAGE_DESCRIPTION);
			if (description == null)
				description = part.getParticipant().getName();
			// TODO: Get the description from the configuration
			// TODO: listen to the configuration for description changes
			setContentDescription(Utils.shortenText(MAX_NAME_LENGTH, description)); 
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#doDestroyPage(org.eclipse.ui.IWorkbenchPart, org.eclipse.ui.part.PageBookView.PageRec)
	 */
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		IPage page = pageRecord.page;
		page.dispose();
		pageRecord.dispose();
		SynchronizeViewWorkbenchPart syncPart = (SynchronizeViewWorkbenchPart) part;
		ISynchronizeParticipant participant = syncPart.getParticipant();
		clearCrossReferenceCache(part, participant);
	}

	private void clearCrossReferenceCache(IWorkbenchPart part, ISynchronizeParticipant participant) {
		participant.removePropertyChangeListener(this);
		((SynchronizeViewWorkbenchPart)part).getConfiguration().removePropertyChangeListener(this);
		fPartToParticipant.remove(part);
		fParticipantToPart.remove(participant);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#doCreatePage(org.eclipse.ui.IWorkbenchPart)
	 */
	protected PageRec doCreatePage(IWorkbenchPart dummyPart) {
		SynchronizeViewWorkbenchPart part = (SynchronizeViewWorkbenchPart)dummyPart;
		ISynchronizeParticipant participant = part.getParticipant();	
		participant.addPropertyChangeListener(this);
		ISynchronizePageConfiguration configuration = participant.createPageConfiguration();
		part.setConfiguration(configuration);
		configuration.addPropertyChangeListener(this);
		IPageBookViewPage page = participant.createPage(configuration);
		if(page != null) {
			initPage(page);
			initPage(configuration, page);
			page.createControl(getPageBook());
			PageRec rec = new PageRec(dummyPart, page);
			return rec;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#initPage(org.eclipse.ui.part.IPageBookViewPage)
	 */
	protected void initPage(ISynchronizePageConfiguration configuration, IPageBookViewPage page) {
		// A page site does not provide everything the page may need
		// Also provide the synchronize page site if the page is a synchronize view page
		((SynchronizePageConfiguration)configuration).setSite(new WorkbenchPartSynchronizePageSite(this, page.getSite(), getDialogSettings(configuration.getParticipant())));
		if (page instanceof ISynchronizePage) {
			try {
				((ISynchronizePage)page).init(configuration.getSite());
			} catch (PartInitException e) {
				TeamUIPlugin.log(IStatus.ERROR, e.getMessage(), e);
			}
		}
		page.getSite().getActionBars().setGlobalActionHandler(ActionFactory.REFRESH.getId(), fPageDropDown);
		page.getSite().getActionBars().updateActionBars();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#isImportant(org.eclipse.ui.IWorkbenchPart)
	 */
	protected boolean isImportant(IWorkbenchPart part) {
		return part instanceof SynchronizeViewWorkbenchPart;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		TeamUI.getSynchronizeManager().removeSynchronizeParticipantListener(this);
		// Pin action is hooked up to listeners, must call dispose to un-register.
		fPinAction.dispose();
		// Remember the last active participant
		if(activeParticipantRef != null) {
			rememberCurrentParticipant();
		}			
		fParticipantToPart = null;
		fPartToParticipant = null;	
	}

    /**
     * 
     */
    private void rememberCurrentParticipant() {
        IDialogSettings section = getDialogSettings();
        section.put(KEY_LAST_ACTIVE_PARTICIPANT_ID, activeParticipantRef.getId());
        section.put(KEY_LAST_ACTIVE_PARTICIPANT_SECONDARY_ID, activeParticipantRef.getSecondaryId());
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#createDefaultPage(org.eclipse.ui.part.PageBook)
	 */
	protected IPage createDefaultPage(PageBook book) {
		Page page = new MessagePage();
		page.createControl(getPageBook());
		initPage(page);
		return page;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.sync.ISynchronizeParticipantListener#participantsAdded(org.eclipse.team.ui.sync.ISynchronizeParticipant[])
	 */
	public void participantsAdded(final ISynchronizeParticipant[] participants) {
		for (int i = 0; i < participants.length; i++) {
			ISynchronizeParticipant participant = participants[i];
			if (isAvailable() && select(TeamUI.getSynchronizeManager().get(participant.getId(), participant.getSecondaryId()))) {
				SynchronizeViewWorkbenchPart part = new SynchronizeViewWorkbenchPart(participant, getSite());
				fParticipantToPart.put(participant, part);
				fPartToParticipant.put(part, participant);
			}
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				firePropertyChange(PROP_DIRTY);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.sync.ISynchronizeParticipantListener#participantsRemoved(org.eclipse.team.ui.sync.ISynchronizeParticipant[])
	 */
	public void participantsRemoved(final ISynchronizeParticipant[] participants) {
		if (isAvailable()) {
			Runnable r = new Runnable() {
				public void run() {
					for (int i = 0; i < participants.length; i++) {
						ISynchronizeParticipant participant = participants[i];
						if (isAvailable()) {
							SynchronizeViewWorkbenchPart part = (SynchronizeViewWorkbenchPart)fParticipantToPart.get(participant);
							if (part != null) {
								partClosed(part);
								clearCrossReferenceCache(part, participant);
							}
							// Remove any settings created for the participant
							removeDialogSettings(participant);
							if (getParticipant() == null) {
								ISynchronizeParticipantReference[] available = TeamUI.getSynchronizeManager().getSynchronizeParticipants();
								if (available.length > 0) {
									ISynchronizeParticipant p;
									try {
										p = available[available.length - 1].getParticipant();
									} catch (TeamException e) {
										return;
									}
									display(p);
								}
							}
						}
					}
					firePropertyChange(PROP_DIRTY);
				}
			};
			asyncExec(r);
		}
	}

	private ISaveableModelSource getSaveableModelSource(ISynchronizeParticipant participant) {
		if (participant instanceof ISaveableModelSource) {
			return (ISaveableModelSource) participant;
		}
		return null;
	}

	/**
	 * Constructs a synchronize view
	 */
	public SynchronizeView() {
		super();
		fParticipantToPart = new HashMap();
		fPartToParticipant = new HashMap();
		updateTitle();
	}
	
	/**
	 * Create the default actions for the view. These will be shown regardless of the
	 * participant being displayed.
	 */
	protected void createActions() {
		fPageDropDown = new SynchronizePageDropDownAction(this);
		fPinAction = new PinParticipantAction();
		fRemoveCurrentAction = new RemoveSynchronizeParticipantAction(this, false);
		fRemoveAllAction = new RemoveSynchronizeParticipantAction(this, true);
		updateActionEnablements();
	}

	private void updateActionEnablements() {
		if (fPinAction != null) {
			fPinAction.setParticipant(activeParticipantRef);
		}
		if (fRemoveAllAction != null) {
			fRemoveAllAction.setEnabled(getParticipant() != null);
		}
		if (fRemoveCurrentAction != null) {
			fRemoveCurrentAction.setEnabled(getParticipant() != null);
		}
	}

	/**
	 * Add the actions to the toolbar
	 * 
	 * @param mgr toolbar manager
	 */
	protected void configureToolBar(IActionBars bars) {
		IToolBarManager mgr = bars.getToolBarManager();
		mgr.add(fPageDropDown);
		mgr.add(fPinAction);
		IMenuManager menu = bars.getMenuManager();
		menu.add(fRemoveCurrentAction);
		menu.add(fRemoveAllAction);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeView#display(org.eclipse.team.ui.synchronize.ISynchronizeParticipant)
	 */
	public void display(ISynchronizeParticipant participant) {
		SynchronizeViewWorkbenchPart part = (SynchronizeViewWorkbenchPart)fParticipantToPart.get(participant);
		if (part != null) {
			partActivated(part);
			fPageDropDown.update();
            rememberCurrentParticipant();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#getBootstrapPart()
	 */
	protected IWorkbenchPart getBootstrapPart() {
		return null;
	}
	
	/**
	 * Registers the given runnable with the display
	 * associated with this view's control, if any.
	 */
	public void asyncExec(Runnable r) {
		if (isAvailable()) {
			getPageBook().getDisplay().asyncExec(r);
		}
	}
	
	/**
	 * Creates this view's underlying viewer and actions.
	 * Hooks a pop-up menu to the underlying viewer's control,
	 * as well as a key listener. When the delete key is pressed,
	 * the <code>REMOVE_ACTION</code> is invoked. Hooks help to
	 * this view. Subclasses must implement the following methods
	 * which are called in the following order when a view is
	 * created:<ul>
	 * <li><code>createViewer(Composite)</code> - the context
	 *   menu is hooked to the viewer's control.</li>
	 * <li><code>createActions()</code></li>
	 * <li><code>configureToolBar(IToolBarManager)</code></li>
	 * <li><code>getHelpContextId()</code></li>
	 * </ul>
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		createActions();
		configureToolBar(getViewSite().getActionBars());
		updateForExistingParticipants();
		getViewSite().getActionBars().updateActionBars();
		updateTitle();
		
		IWorkbenchSiteProgressService progress = (IWorkbenchSiteProgressService)getSite().getAdapter(IWorkbenchSiteProgressService.class);
		if(progress != null) {
			progress.showBusyForFamily(ISynchronizeManager.FAMILY_SYNCHRONIZE_OPERATION);
		}
	}
	
	/**
	 * Initialize for existing participants
	 */
	private void updateForExistingParticipants() {
		ISynchronizeManager manager = TeamUI.getSynchronizeManager();
		List participants = Arrays.asList(getParticipants());
		boolean errorOccurred = false;
		for (int i = 0; i < participants.size(); i++) {
			try {
				ISynchronizeParticipantReference ref = (ISynchronizeParticipantReference)participants.get(i);
				participantsAdded(new ISynchronizeParticipant[] {ref.getParticipant()});
			} catch (TeamException e) {
				errorOccurred = true;
				continue;
			}
			
		}
		if (errorOccurred) {
			participants = Arrays.asList(getParticipants());
		}
		try {
			// decide which participant to show	on startup
			if (participants.size() > 0) {
				ISynchronizeParticipantReference participantToSelect = (ISynchronizeParticipantReference)participants.get(0);
				IDialogSettings section = getDialogSettings();
				String selectedParticipantId = section.get(KEY_LAST_ACTIVE_PARTICIPANT_ID);
				String selectedParticipantSecId = section.get(KEY_LAST_ACTIVE_PARTICIPANT_SECONDARY_ID);
				if(selectedParticipantId != null) {
					ISynchronizeParticipantReference selectedParticipant = manager.get(selectedParticipantId, selectedParticipantSecId);
					if(selectedParticipant != null) {
						participantToSelect = selectedParticipant;
					}
				}
				display(participantToSelect.getParticipant());
			}
			
			// add as a listener to update when new participants are added
			manager.addSynchronizeParticipantListener(this);
		} catch (TeamException e) {
			Utils.handle(e);
		}
	}
	
	private ISynchronizeParticipantReference[] getParticipants() {
		ISynchronizeManager manager = TeamUI.getSynchronizeManager();
		// create pages
		List participants = new ArrayList();
		ISynchronizeParticipantReference[] refs = manager.getSynchronizeParticipants();
		for (int i = 0; i < refs.length; i++) {
			ISynchronizeParticipantReference ref =refs[i];
			if(select(ref)) {
				participants.add(ref);
			}
		}
		return (ISynchronizeParticipantReference[]) participants.toArray(new ISynchronizeParticipantReference[participants.size()]);
	}
	
	private boolean isAvailable() {
		return getPageBook() != null && !getPageBook().isDisposed();
	}
	
	/*
	 * Method used by test cases to access the page for a participant
	 */
	public IPage getPage(ISynchronizeParticipant participant) {
		IWorkbenchPart part = (IWorkbenchPart)fParticipantToPart.get(participant);
		if (part == null) return null;
		try {
			return getPageRec(part).page;
		} catch (NullPointerException e) {
			// The PageRec class is not visible so we can't do a null check
			// before accessing the page.
			return null;
		}
	}
	
	protected boolean select(ISynchronizeParticipantReference ref) {
		return true;
	}
	
	/*
	 * Return the dialog settings for the view
	 */
	private IDialogSettings getDialogSettings() {
		IDialogSettings workbenchSettings = TeamUIPlugin.getPlugin().getDialogSettings();
		IDialogSettings syncViewSettings = workbenchSettings.getSection(KEY_SETTINGS_SECTION); 
		if (syncViewSettings == null) {
			syncViewSettings = workbenchSettings.addNewSection(KEY_SETTINGS_SECTION);
		}
		return syncViewSettings;
	}
	
	private String getSettingsKey(ISynchronizeParticipant participant) {
		String id = participant.getId();
		String secondaryId = participant.getSecondaryId();
	    return secondaryId == null ? id : id + '.' + secondaryId;
	}
	
	private IDialogSettings getDialogSettings(ISynchronizeParticipant participant) {
		String key = getSettingsKey(participant);
		IDialogSettings viewsSettings = getDialogSettings();
		IDialogSettings settings = viewsSettings.getSection(key);
		if (settings == null) {
			settings = viewsSettings.addNewSection(key);
		}
		return settings;
	}
	
	private void removeDialogSettings(ISynchronizeParticipant participant) {
		String key = getSettingsKey(participant);
		IDialogSettings settings = getDialogSettings();
		if (settings.getSection(key) != null) {
			// There isn't an explicit remove so just make sure
			// That the old settings are forgotten
			getDialogSettings().addSection(new DialogSettings(key));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveableModelSource#getModels()
	 */
	public ISaveableModel[] getModels() {
		Set result = new HashSet();
		for (Iterator iter = fPartToParticipant.keySet().iterator(); iter.hasNext();) {
			SynchronizeViewWorkbenchPart part = (SynchronizeViewWorkbenchPart) iter.next();
			ISaveableModelSource ms = getSaveableModelSource(part.getParticipant());
			if (ms != null) {
				ISaveableModel[] models = ms.getModels();
				for (int i = 0; i < models.length; i++) {
					ISaveableModel model = models[i];
					result.add(model);
				}
			}
		}
		return (ISaveableModel[]) result.toArray(new ISaveableModel[result.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveableModelSource#getActiveModels()
	 */
	public ISaveableModel[] getActiveModels() {
		Set result = new HashSet();
		for (Iterator iter = fPartToParticipant.keySet().iterator(); iter.hasNext();) {
			SynchronizeViewWorkbenchPart part = (SynchronizeViewWorkbenchPart) iter.next();
			ISaveableModelSource ms = getSaveableModelSource(part.getParticipant());
			if (ms != null) {
				ISaveableModel[] models = ms.getActiveModels();
				for (int i = 0; i < models.length; i++) {
					ISaveableModel model = models[i];
					result.add(model);
				}
			}
		}
		return (ISaveableModel[]) result.toArray(new ISaveableModel[result.size()]);
	}

	public void doSave(IProgressMonitor monitor) {
		ISaveableModel[] models = getModels();
		if (models.length == 0)
			return;
		monitor.beginTask(null, 100* models.length);
		for (int i = 0; i < models.length; i++) {
			ISaveableModel model = models[i];
			model.doSave(Policy.subMonitorFor(monitor, 100));
			Policy.checkCanceled(monitor);
		}
		monitor.done();
		firePropertyChange(PROP_DIRTY);
	}

	public void doSaveAs() {
		// Not allowed
	}

	public boolean isDirty() {
		ISaveableModel[] models = getModels();
		for (int i = 0; i < models.length; i++) {
			ISaveableModel model = models[i];
			if (model.isDirty())
				return true;
		}
		return false;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	public boolean isSaveOnCloseNeeded() {
		return true;
	}
}
