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

import java.lang.reflect.InvocationTargetException;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.subscribers.FilteredSyncInfoCollector;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.dialogs.DetailsDialog;
import org.eclipse.team.ui.synchronize.subscriber.SubscriberParticipant;
import org.eclipse.team.ui.synchronize.viewers.TreeViewerAdvisor;
import org.eclipse.team.ui.synchronize.viewers.SynchronizeCompareInput;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class RefreshCompleteDialog extends DetailsDialog {
	private static final String HEIGHT_KEY = "width-key"; //$NON-NLS-1$
	private final static int RESOURCE_LIST_SIZE = 10;
	private static final String WIDTH_KEY = "height-key"; //$NON-NLS-1$
	private FilteredSyncInfoCollector collector;
	private SynchronizeCompareInput compareEditorInput;
	private IRefreshEvent event;
	private SubscriberParticipant participant;

	private Button promptWhenNoChanges;
	private Button promptWithChanges;
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
		SyncInfoFilter filter = new SyncInfoFilter() {

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
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		collector.dispose();
		Rectangle bounds = getShell().getBounds();
		settings.put(HEIGHT_KEY, bounds.height);
		settings.put(WIDTH_KEY, bounds.width);
		return super.close();
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
			TreeViewerAdvisor viewerAdvisor = new TreeViewerAdvisor(participant.getId(), null, syncInfoSet);
			compareEditorInput = new SynchronizeCompareInput(compareConfig, viewerAdvisor) {

				public String getTitle() {
					return "Resources found during last refresh";
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
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#createMainDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected void createMainDialogArea(Composite parent) {
		StringBuffer text = new StringBuffer();
		SyncInfo[] changes = event.getChanges();
		IResource[] resources = event.getResources();
		if (changes.length != 0) {
			text.append(Policy.bind("RefreshCompleteDialog.5", Integer.toString(changes.length))); //$NON-NLS-1$
		} else {
			text.append(Policy.bind("RefreshCompleteDialog.6")); //$NON-NLS-1$
		}
		text.append(Policy.bind("RefreshCompleteDialog.7", Integer.toString(resources.length))); //$NON-NLS-1$ //$NON-NLS-2$
		createLabel(parent, text.toString(), 2);
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
		data.horizontalSpan = 2;
		data.widthHint = 200;
		data.heightHint = 125;
		table.setLayoutData(data);
		TableViewer resourceList = new TableViewer(table);
		resourceList.setContentProvider(new ArrayContentProvider());
		resourceList.setLabelProvider(new WorkbenchLabelProvider());
		resourceList.setInput(resources);
		createLabel(parent, "", 2); //$NON-NLS-1$
		promptWhenNoChanges = new Button(parent, SWT.CHECK);
		data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		data.horizontalSpan = 2;
		promptWhenNoChanges.setLayoutData(data);
		promptWithChanges = new Button(parent, SWT.CHECK);
		data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		data.horizontalSpan = 2;
		promptWithChanges.setLayoutData(data);
		if (event.getRefreshType() == IRefreshEvent.USER_REFRESH) {
			promptWhenNoChanges.setText(Policy.bind(Policy.bind("RefreshCompleteDialog.13"))); //$NON-NLS-1$
			promptWhenNoChanges.setSelection(TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCVIEW_VIEW_PROMPT_WHEN_NO_CHANGES));
			promptWithChanges.setText(Policy.bind(Policy.bind("RefreshCompleteDialog.14"))); //$NON-NLS-1$
			promptWithChanges.setSelection(TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCVIEW_VIEW_PROMPT_WITH_CHANGES));
		} else {
			promptWhenNoChanges.setText(Policy.bind(Policy.bind("RefreshCompleteDialog.15"))); //$NON-NLS-1$
			promptWhenNoChanges.setSelection(TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCVIEW_VIEW_BKG_PROMPT_WHEN_NO_CHANGES));
			promptWithChanges.setText(Policy.bind(Policy.bind("RefreshCompleteDialog.16"))); //$NON-NLS-1$
			promptWithChanges.setSelection(TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCVIEW_VIEW_BKG_PROMPT_WITH_CHANGES));
		}
		Dialog.applyDialogFont(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#getDetailsButtonLabelHide()
	 */
	protected String getDetailsButtonLabelHide() {
		return Policy.bind("RefreshCompleteDialog.18");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#getDetailsButtonLabelShow()
	 */
	protected String getDetailsButtonLabelShow() {
		return Policy.bind("RefreshCompleteDialog.17");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		int width, height;
		try {
			height = settings.getInt(HEIGHT_KEY);
			width = settings.getInt(WIDTH_KEY);
		} catch (NumberFormatException e) {
			return super.getInitialSize();
		}
		Point p = super.getInitialSize();
		return new Point(width, p.y);
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
		return event.getChanges().length != 0;
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
		if (event.getRefreshType() == IRefreshEvent.USER_REFRESH) {
			TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.SYNCVIEW_VIEW_PROMPT_WHEN_NO_CHANGES, promptWhenNoChanges.getSelection());
			TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.SYNCVIEW_VIEW_PROMPT_WITH_CHANGES, promptWithChanges.getSelection());
		} else {
			TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.SYNCVIEW_VIEW_BKG_PROMPT_WHEN_NO_CHANGES, promptWhenNoChanges.getSelection());
			TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.SYNCVIEW_VIEW_BKG_PROMPT_WITH_CHANGES, promptWithChanges.getSelection());
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
		GridData data = new GridData();
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