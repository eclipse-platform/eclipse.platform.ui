/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * extension of <code>FilteredTree</code> to allow us to update the filtering label and maintain selection
 * @since 3.2
 */
public class LaunchConfigurationFilteredTree extends FilteredTree {

	private final ILaunchConfigurationDialog fDialog = LaunchConfigurationsDialog.getCurrentlyVisibleLaunchConfigurationDialog();
	private Job fRefreshJob;
	
	/**
	 * Constructor
	 * @param parent the parent for this control
	 * @param treeStyle the style fo the tree
	 * @param filter the initial pattern filter
	 */
	public LaunchConfigurationFilteredTree(Composite parent, int treeStyle, PatternFilter filter) {
		super(parent, treeStyle, filter);
		createRefreshJob();
	}

	 /**
     * Create the refresh job for the receiver.
     */
	private void createRefreshJob() {
		fRefreshJob = new WorkbenchJob("Refresh Filter"){//$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if(!treeViewer.getControl().isDisposed()) {
					String text = getFilterString();
					if (text == null) {
						return Status.OK_STATUS;
					}
			        boolean initial = initialText != null && initialText.equals(text); 
			        if (initial) {
			            getPatternFilter().setPattern(null);
			        } 
			        else {
			            getPatternFilter().setPattern(text);
			        }       
			        boolean update = text.length() > 0 && !initial;
			        updateToolbar(update);
			        treeViewer.refresh(true);
		            if(fDialog != null && fDialog instanceof LaunchConfigurationsDialog) {
			        	treeViewer.expandAll();
			        	((LaunchConfigurationsDialog)fDialog).refreshFilteringLabel();
			        }
			        return Status.OK_STATUS;
				}
		        return Status.CANCEL_STATUS;
			}
		};
		fRefreshJob.setSystem(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredTree#textChanged()
	 */
	protected void textChanged() {
		if(fRefreshJob != null) {
			fRefreshJob.schedule();
		}
	}
}
