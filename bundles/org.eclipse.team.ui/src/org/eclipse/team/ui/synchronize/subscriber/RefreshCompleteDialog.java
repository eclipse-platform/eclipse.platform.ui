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
package org.eclipse.team.ui.synchronize.subscriber;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.subscribers.FilteredSyncInfoCollector;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.dialogs.DetailsDialog;
import org.eclipse.team.ui.synchronize.viewers.SynchronizeCompareInput;
import org.eclipse.team.ui.synchronize.viewers.TreeViewerAdvisor;

public class RefreshCompleteDialog extends DetailsDialog {
	private SyncInfoFilter filter;
	private FilteredSyncInfoCollector collector;
	private SynchronizeCompareInput compareEditorInput;
	private IRefreshEvent event;
	private SubscriberParticipant participant;

	private Button dontShowAgainButton;
	
	private IDialogSettings settings;
	private SyncInfoTree syncInfoSet = new SyncInfoTree();
	
	public RefreshCompleteDialog(Shell parentShell, IRefreshEvent event, SubscriberParticipant participant) {
		super(parentShell, Policy.bind("RefreshCompleteDialog.4", participant.getName())); //$NON-NLS-1$
		this.participant = participant;
		int shellStyle = getShellStyle();
		setShellStyle(shellStyle | SWT.RESIZE | SWT.MAX);
		this.event = event;
		setImageKey(DLG_IMG_INFO);
		// Set-up a sync info set that contains the resources that where found
		// when the refresh occured.
		filter = new SyncInfoFilter() {
			public boolean select(SyncInfo info, IProgressMonitor monitor) {
				IResource[] resources = getResources();
				for (int i = 0; i < resources.length; i++) {
					IResource resource = resources[i];
					if (info.getLocal().equals(resource)) {
						return true;
					}
				}
				return false;
			}
		};
		this.collector = new FilteredSyncInfoCollector(
				participant.getSubscriberSyncInfoCollector().getSubscriberSyncInfoSet(), 
				syncInfoSet, 
				filter);		
		IDialogSettings workbenchSettings = TeamUIPlugin.getPlugin().getDialogSettings();
		this.settings = workbenchSettings.getSection("RefreshCompleteDialog");//$NON-NLS-1$
		if (settings == null) {
			this.settings = workbenchSettings.addNewSection("RefreshCompleteDialog");//$NON-NLS-1$
		}
	}

	/**
	 * Populate the dialog with the new changes discovered during the refresh
	 */
	public void initialize() {
		this.collector.start(new NullProgressMonitor());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
	}

	protected Combo createCombo(Composite parent, int widthChars) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		GC gc = new GC(combo);
		gc.setFont(combo.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		data.widthHint = Dialog.convertWidthInCharsToPixels(fontMetrics, widthChars);
		gc.dispose();
		combo.setLayoutData(data);
		return combo;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#createDropDownDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Composite createDropDownDialogArea(Composite parent) {
		try {
			CompareConfiguration compareConfig = new CompareConfiguration();
			TreeViewerAdvisor viewerAdvisor = new TreeViewerAdvisor(syncInfoSet);
			compareEditorInput = new SynchronizeCompareInput(compareConfig, viewerAdvisor) {
				public String getTitle() {
					return Policy.bind("RefreshCompleteDialog.9"); //$NON-NLS-1$
				}
			};
			// Preparing the input should be fast since we haven't started the collector
			compareEditorInput.run(new NullProgressMonitor());
			// Starting the collector will populate the dialog in the background
			initialize();
		} catch (InterruptedException e) {
			Utils.handle(e);
		} catch (InvocationTargetException e) {
			Utils.handle(e);
		}
		Composite result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		result.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.heightHint = 350;
		//data.widthHint = 700;
		result.setLayoutData(data);
		Control c = compareEditorInput.createContents(result);
		data = new GridData(GridData.FILL_BOTH);
		c.setLayoutData(data);
		
		Button onlyNewChangesButton = new Button(result, SWT.CHECK);
		onlyNewChangesButton.setText(Policy.bind("RefreshCompleteDialog.21")); //$NON-NLS-1$
		onlyNewChangesButton.setSelection(true);
		onlyNewChangesButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if(((Button)e.getSource()).getSelection()) {
					collector.setFilter(filter, new NullProgressMonitor());
				} else {
					collector.setFilter(new FastSyncInfoFilter(), new NullProgressMonitor());
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#createMainDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected void createMainDialogArea(Composite parent) {
		StringBuffer text = new StringBuffer();
		SyncInfo[] changes = event.getChanges();
		IResource[] resources = event.getResources();
		SyncInfoSet set = getSubscriberSyncInfoSet();
		if (! set.isEmpty()) {
			String outgoing = Long.toString(set.countFor(SyncInfo.OUTGOING, SyncInfo.DIRECTION_MASK));
			String incoming = Long.toString(set.countFor(SyncInfo.INCOMING, SyncInfo.DIRECTION_MASK));
			String conflicting = Long.toString(set.countFor(SyncInfo.CONFLICTING, SyncInfo.DIRECTION_MASK));
			if(event.getChanges().length > 0) {
				String numNewChanges = Integer.toString(event.getChanges().length);
				text.append(Policy.bind("RefreshCompleteDialog.5a", new Object[] {numNewChanges, participant.getName(), outgoing, incoming, conflicting})); //$NON-NLS-1$
			} else {
				text.append(Policy.bind("RefreshCompleteDialog.5", new Object[] {participant.getName(), outgoing, incoming, conflicting})); //$NON-NLS-1$
			}
			createLabel(parent, text.toString(), 2);
		} else {
			text.append(Policy.bind("RefreshCompleteDialog.6")); //$NON-NLS-1$
			createLabel(parent, text.toString(), 2);
		}
			
		dontShowAgainButton = new Button(parent, SWT.CHECK);
		dontShowAgainButton.setText(Policy.bind("RefreshCompleteDialog.22")); //$NON-NLS-1$
	
		initializeSettings();
		Dialog.applyDialogFont(parent);
	}

	private void initializeSettings() {
		if(dontShowAgainButton != null) {
			dontShowAgainButton.setSelection(! TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCHRONIZING_COMPLETE_SHOW_DIALOG));
		}
	}

	protected SyncInfoSet getSubscriberSyncInfoSet() {
		return participant.getSubscriberSyncInfoCollector().getSubscriberSyncInfoSet();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#includeCancelButton()
	 */
	protected boolean includeCancelButton() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#includeDetailsButton()
	 */
	protected boolean includeDetailsButton() {
		return event.getChanges().length > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#includeErrorMessage()
	 */
	protected boolean includeErrorMessage() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		if(dontShowAgainButton != null) {
			TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.SYNCHRONIZING_COMPLETE_SHOW_DIALOG, ! dontShowAgainButton.getSelection());		
		}
		TeamUIPlugin.getPlugin().savePluginPreferences();		
		super.okPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#updateEnablements()
	 */
	protected void updateEnablements() {
	}

	private Label createLabel(Composite parent, String text, int columns) {
		Label label = new Label(parent, SWT.WRAP);
		label.setText(text);
		GridData data =
			new GridData(
				GridData.GRAB_HORIZONTAL
					| GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_BEGINNING);
		data.widthHint =
			convertHorizontalDLUsToPixels(
				IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		data.horizontalSpan = columns;
		label.setLayoutData(data);
		return label;
	}

	private IResource[] getResources() {
		SyncInfo[] changes = event.getChanges();
		IResource[] resources = new IResource[changes.length];
		for (int i = 0; i < changes.length; i++) {
			SyncInfo info = changes[i];
			resources[i] = info.getLocal();
		}
		return resources;
	}
}