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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.sync.AndSyncInfoFilter;
import org.eclipse.team.ui.sync.AutomergableFilter;
import org.eclipse.team.ui.sync.OrSyncInfoFilter;
import org.eclipse.team.ui.sync.SyncInfoChangeTypeFilter;
import org.eclipse.team.ui.sync.SyncInfoDirectionFilter;
import org.eclipse.team.ui.sync.SyncInfoFilter;
import org.eclipse.team.ui.sync.SyncInfoSet;

/**
 * This dialog prompts for the type of update which should take place
 * (i.e. update of auto-mergable files or update of all ignore local
 * changes.
 */
public class UpdateDialog extends SyncInfoSetDetailsDialog {

	private Button radio1;
	private Button radio2;
	
	private boolean autoMerge;
	private SyncInfoFilter automergableFilter;

	/**
	 * @param parentShell
	 * @param dialogTitle
	 * @param dialogTitleImage
	 * @param dialogMessage
	 * @param dialogImageType
	 * @param dialogButtonLabels
	 * @param defaultIndex
	 */
	public UpdateDialog(Shell parentShell, SyncInfoSet syncSet) {
		super(parentShell, "Overwrite Local Changes?", syncSet);
		this.autoMerge = hasAutomergableResources();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.DetailsDialog#createMainDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected void createMainDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		
		// TODO: set F1 help
		//WorkbenchHelp.setHelp(composite, IHelpContextIds.ADD_TO_VERSION_CONTROL_DIALOG);
		
		createWrappingLabel(composite, Policy.bind("UpdateSyncAction.You_have_local_changes_you_are_about_to_overwrite_2"));
		
		if (hasAutomergableResources()) {
			
			SelectionListener selectionListener = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Button button = (Button)e.widget;
					if (button.getSelection()) {
						setAutomerge(button == radio1);
					}
				}
			};
			
			radio1 = new Button(composite, SWT.RADIO);
			radio1.addSelectionListener(selectionListener);
			
			radio1.setText(Policy.bind("UpdateSyncAction.Only_update_resources_that_can_be_automatically_merged_3")); //$NON-NLS-1$
	
			radio2 = new Button(composite, SWT.RADIO);
			radio2.addSelectionListener(selectionListener);
	
			radio2.setText(Policy.bind("UpdateSyncAction.Update_all_resources,_overwriting_local_changes_with_remote_contents_4")); //$NON-NLS-1$
			
			// set initial state
			radio1.setSelection(autoMerge);
			radio2.setSelection(!autoMerge);
		} else {
			createWrappingLabel(composite, Policy.bind("UpdateSyncAction.Update_all_resources,_overwriting_local_changes_with_remote_contents_4"));
		}
	}

	/**
	 * @return
	 */
	private boolean hasAutomergableResources() {
		return getSyncSet().hasNodes(getAutomergableFilter());
	}

	/**
	 * TODO: Could be a method on SyncInfoSet
	 * @param resources2
	 */
	protected IResource[] getAllResources() {
		SyncInfo[] resources;
		if (autoMerge) {
			resources = getSyncSet().getNodes(getAutomergableFilter());
		} else {
			resources = getSyncSet().getSyncInfos();
		}
		List result = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			SyncInfo info = resources[i];
			result.add(info.getLocal());
		}
		return (IResource[]) result.toArray(new IResource[result.size()]);
		
	}

	/**
	 * Set the filter used to determine if a change is automergable
	 * @param automergableFilter
	 */
	public void setAutomergableFilter(SyncInfoFilter automergableFilter) {
		this.automergableFilter = automergableFilter;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.sync.SubscriberAction#getSyncInfoFilter()
	 */
	public SyncInfoFilter getAutomergableFilter() {
		if (automergableFilter == null) {
			// By default, filter for all incoming infos and automergable conflicting changes
			automergableFilter = new OrSyncInfoFilter(new SyncInfoFilter[] {
				new SyncInfoDirectionFilter(SyncInfo.INCOMING),
				new AndSyncInfoFilter(new SyncInfoFilter[] {
					new AutomergableFilter(),
					new SyncInfoDirectionFilter(SyncInfo.CONFLICTING),
					new SyncInfoChangeTypeFilter(SyncInfo.CHANGE)
				})
			});
		} 
		return automergableFilter;
	}
	
	private void setAutomerge(boolean b) {
		this.autoMerge = b;
		resetViewerInput();
	}
	
	public boolean getAutomerge() {
		return autoMerge;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.SyncInfoSetDetailsDialog#keepSelectedResources()
	 */
	protected void filterSyncSet() {
		if (autoMerge) {
			getSyncSet().selectNodes(getAutomergableFilter());
		}
		super.filterSyncSet();
	}

}
