/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.ITeamStatus;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.part.PageBook;

/**
 * Section shown in a participant page to show the changes for this participant. This
 * includes a diff viewer for browsing the changes.
 * 
 * @since 3.0
 */
public class ChangesSection extends Composite {
	
	private ISynchronizeParticipant participant;
	private SyncInfoSetSynchronizePage page;
	private FormToolkit forms;
			
	/**
	 * Page book either shows the diff tree viewer if there are changes or
	 * shows a message to the user if there are no changes that would be
	 * shown in the tree.
	 */
	private PageBook changesSectionContainer;
	
	/**
	 * Shows message to user is no changes are to be shown in the diff
	 * tree viewer.
	 */
	private Composite filteredContainer;
	
	/**
	 * Diff tree viewer that shows synchronization changes. This is created
	 * by the participant.
	 */
	private Viewer changesViewer;
	
	/**
	 * Boolean that indicates whether the error page is being shown.
	 * This is used to avoid redrawing the error page when new events come in
	 */
	private boolean showingError;

	/**
	 * Register an action contribution in order to receive model
	 * change notification so that we can update message to user and totals.
	 */
	private SynchronizePageActionGroup changedListener = new SynchronizePageActionGroup() {
		public void modelChanged(ISynchronizeModelElement root) {
			calculateDescription();
		}
	};
	
	/**
	 * Listener registered with the subscriber sync info set which contains
	 * all out-of-sync resources for the subscriber.
	 */
	private ISyncInfoSetChangeListener subscriberListener = new ISyncInfoSetChangeListener() {
		public void syncInfoSetReset(SyncInfoSet set, IProgressMonitor monitor) {
			// Handled by output set listener
		}
		public void syncInfoChanged(ISyncInfoSetChangeEvent event, IProgressMonitor monitor) {
			calculateDescription();
		}
		public void syncInfoSetErrors(SyncInfoSet set, ITeamStatus[] errors, IProgressMonitor monitor) {
			// Handled by output set listener
		}
	};
	
	/**
	 * Listener registered with the output sync info set which contains
	 * only the visible sync info. 
	 */
	private ISyncInfoSetChangeListener outputSetListener = new ISyncInfoSetChangeListener() {
		public void syncInfoSetReset(SyncInfoSet set, IProgressMonitor monitor) {
			calculateDescription();
		}
		public void syncInfoChanged(ISyncInfoSetChangeEvent event, IProgressMonitor monitor) {
			// Input changed listener will call calculateDescription()
			// The input will then react to output set changes
		}
		public void syncInfoSetErrors(SyncInfoSet set, ITeamStatus[] errors, IProgressMonitor monitor) {
			calculateDescription();
		}
	};
	private ISynchronizePageConfiguration configuration;
	
	/**
	 * Create a changes section on the following page.
	 * 
	 * @param parent the parent control 
	 * @param page the page showing this section
	 */
	public ChangesSection(Composite parent, SyncInfoSetSynchronizePage page, ISynchronizePageConfiguration configuration) {
		super(parent, SWT.NONE);
		this.page = page;
		this.configuration = configuration;
		this.participant = configuration.getParticipant();
		
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessVerticalSpace = true;
		setLayoutData(data);
		
		forms = new FormToolkit(parent.getDisplay());
		forms.setBackground(getBackgroundColor());
		HyperlinkGroup group = forms.getHyperlinkGroup();
		group.setBackground(getBackgroundColor());
		
		changesSectionContainer = new PageBook(this, SWT.NONE);
		data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		changesSectionContainer.setLayoutData(data);
	}
	
	public Composite getComposite() {
		return changesSectionContainer;
	}
	
	public void setViewer(Viewer viewer) {
		this.changesViewer = viewer;
		calculateDescription();
		configuration.addActionContribution(changedListener);
		getWorkingSetSyncInfoSet().addSyncSetChangedListener(subscriberListener);
		getSyncInfoTree().addSyncSetChangedListener(outputSetListener);
	}
	
	private void calculateDescription() {
		SyncInfoTree syncInfoTree = getSyncInfoTree();
		if (syncInfoTree.getErrors().length > 0) {
			if (!showingError) {
				TeamUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
					public void run() {
						if (changesSectionContainer.isDisposed()) return;
						if(filteredContainer != null) {
							filteredContainer.dispose();
							filteredContainer = null;
						}
						filteredContainer = getErrorComposite(changesSectionContainer);
						changesSectionContainer.showPage(filteredContainer);
						showingError = true;
					}
				});
			}
			return;
		}
		
		showingError = false;
		if(syncInfoTree.size() == 0) {
			TeamUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
				public void run() {
					if (changesSectionContainer.isDisposed()) return;
					if(filteredContainer != null) {
						filteredContainer.dispose();
						filteredContainer = null;
					}
					filteredContainer = getEmptyChangesComposite(changesSectionContainer);
					changesSectionContainer.showPage(filteredContainer);
				}
			});
		} else {
			TeamUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
				public void run() {
					if(filteredContainer != null) {
						filteredContainer.dispose();
						filteredContainer = null;
					}
					Control control = changesViewer.getControl();
					if (!changesSectionContainer.isDisposed() && !control.isDisposed()) {
						changesSectionContainer.showPage(control);
					}
				}
			});
		}
	}
	
	private boolean isThreeWay() {
		return ISynchronizePageConfiguration.THREE_WAY.equals(configuration.getComparisonType());
	}
	
	private Composite getEmptyChangesComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(getBackgroundColor());
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);
		
		if(! isThreeWay()) {
			createDescriptionLabel(composite,Policy.bind("ChangesSection.noChanges", participant.getName()));	 //$NON-NLS-1$
			return composite;
		}
		
		SyncInfoSet workingSet = getWorkingSetSyncInfoSet();
		SyncInfoSet filteredSet = getSyncInfoTree();
		
		int changesInWorkingSet = workingSet.size();
		int changesInFilter = filteredSet.size();
		
		long outgoingChanges = workingSet.countFor(SyncInfo.OUTGOING, SyncInfo.DIRECTION_MASK);
		long incomingChanges = workingSet.countFor(SyncInfo.INCOMING, SyncInfo.DIRECTION_MASK);		
		
		if(changesInFilter == 0 && changesInWorkingSet != 0) {
			final int newMode = outgoingChanges != 0 ? ISynchronizePageConfiguration.OUTGOING_MODE : ISynchronizePageConfiguration.INCOMING_MODE;
			long numChanges = outgoingChanges != 0 ? outgoingChanges : incomingChanges;
			StringBuffer text = new StringBuffer();
			text.append(Policy.bind("ChangesSection.filterHides", Utils.modeToString(configuration.getMode()))); //$NON-NLS-1$
			if(numChanges > 1) {
				text.append(Policy.bind("ChangesSection.filterHidesPlural", Long.toString(numChanges), Utils.modeToString(newMode))); //$NON-NLS-1$
			} else {
				text.append(Policy.bind("ChangesSection.filterHidesSingular", Long.toString(numChanges), Utils.modeToString(newMode))); //$NON-NLS-1$
			}
			
			Label warning = new Label(composite, SWT.NONE);
			warning.setImage(TeamUIPlugin.getPlugin().getImage(ISharedImages.IMG_WARNING_OVR));
			
			Hyperlink link = forms.createHyperlink(composite, Policy.bind("ChangesSection.filterChange", Utils.modeToString(newMode)), SWT.WRAP); //$NON-NLS-1$
			link.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					configuration.setMode(newMode);
				}
			});
			forms.getHyperlinkGroup().add(link);
			createDescriptionLabel(composite, text.toString());
		} else {
			createDescriptionLabel(composite,Policy.bind("ChangesSection.noChanges", participant.getName()));	 //$NON-NLS-1$
		}		
		return composite;
	}
	
	private Label createDescriptionLabel(Composite parent, String text) {
		Label description = new Label(parent, SWT.WRAP);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		data.widthHint = 100;
		description.setLayoutData(data);
		description.setText(text);
		description.setBackground(getBackgroundColor());
		return description;
	}
	
	public void dispose() {
		super.dispose();
		forms.dispose();
		configuration.removeActionContribution(changedListener);
		getWorkingSetSyncInfoSet().removeSyncSetChangedListener(subscriberListener);
		getSyncInfoTree().removeSyncSetChangedListener(outputSetListener);
	}
	
	private Composite getErrorComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(getBackgroundColor());
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);	

		Hyperlink link = new Hyperlink(composite, SWT.WRAP);
		link.setText(Policy.bind("ChangesSection.8")); //$NON-NLS-1$
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				showErrors();
			}
		});
		link.setBackground(getBackgroundColor());
		link.setUnderlined(true);
		
		link = new Hyperlink(composite, SWT.WRAP);
		link.setText(Policy.bind("ChangesSection.9")); //$NON-NLS-1$
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				page.reset();
			}
		});
		link.setBackground(getBackgroundColor());
		link.setUnderlined(true);
		
		createDescriptionLabel(composite, Policy.bind("ChangesSection.10", participant.getName())); //$NON-NLS-1$

		return composite;
	}
	
	/* private */ void showErrors() {
		ITeamStatus[] status = getSyncInfoTree().getErrors();
		String title = Policy.bind("ChangesSection.11"); //$NON-NLS-1$
		if (status.length == 1) {
			ErrorDialog.openError(getShell(), title, status[0].getMessage(), status[0]);
		} else {
			MultiStatus multi = new MultiStatus(TeamUIPlugin.ID, 0, status, Policy.bind("ChangesSection.12"), null); //$NON-NLS-1$
			ErrorDialog.openError(getShell(), title, null, multi);
		}
	}
	
	protected Color getBackgroundColor() {
		return getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
	}
	
	private SyncInfoTree getSyncInfoTree() {
		return (SyncInfoTree)configuration.getProperty(ISynchronizePageConfiguration.P_SYNC_INFO_SET);
	}
	
	private SyncInfoSet getWorkingSetSyncInfoSet() {
		return (SyncInfoSet)configuration.getProperty(SynchronizePageConfiguration.P_WORKING_SET_SYNC_INFO_SET);
	}
}
