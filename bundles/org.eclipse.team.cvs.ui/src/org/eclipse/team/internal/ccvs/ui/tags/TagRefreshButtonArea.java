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
package org.eclipse.team.internal.ccvs.ui.tags;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ui.dialogs.DialogArea;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * An area that displays the Refresh and Configure Tags buttons
 */
public class TagRefreshButtonArea extends DialogArea {
    
    private TagSource tagSource;
    private final Shell shell;
    private Button refreshButton;
    private IRunnableContext context;

    public TagRefreshButtonArea(Shell shell, TagSource tagSource) {
        Assert.isNotNull(shell);
        Assert.isNotNull(tagSource);
        this.shell = shell;
        this.tagSource = tagSource;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.dialogs.DialogArea#createArea(org.eclipse.swt.widgets.Composite)
     */
    public void createArea(Composite parent) {
	 	Composite buttonComp = new Composite(parent, SWT.NONE);
		GridData data = new GridData ();
		data.horizontalAlignment = GridData.END;		
		buttonComp.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonComp.setLayout (layout);
	 	
	 	refreshButton = createTagRefreshButton(buttonComp, Policy.bind("TagConfigurationDialog.20")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalAlignment = GridData.END;
		data.horizontalSpan = 1;
		refreshButton.setLayoutData (data);		

		Button addButton = new Button(buttonComp, SWT.PUSH);
		addButton.setText (Policy.bind("TagConfigurationDialog.21")); //$NON-NLS-1$
		data = new GridData ();
		data.horizontalAlignment = GridData.END;
		data.horizontalSpan = 1;
		addButton.setLayoutData (data);
		addButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					TagConfigurationDialog d = new TagConfigurationDialog(shell, tagSource);
					d.open();
				}
			});		
		
		WorkbenchHelp.setHelp(refreshButton, IHelpContextIds.TAG_CONFIGURATION_REFRESHACTION);
		WorkbenchHelp.setHelp(addButton, IHelpContextIds.TAG_CONFIGURATION_OVERVIEW);		
		Dialog.applyDialogFont(buttonComp);
    }
	
	/*
	 * Returns a button that implements the standard refresh tags operation. The runnable is run immediatly after 
	 * the tags are fetched from the server. A client should refresh their widgets that show tags because they
	 * may of changed. 
	 */
	private Button createTagRefreshButton(Composite composite, String title) {
		Button refreshButton = new Button(composite, SWT.PUSH);
		refreshButton.setText (title);
		refreshButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					try {
						getRunnableContext().run(true, true, new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								try {
								    monitor.beginTask(Policy.bind("TagRefreshButtonArea.5"), 100); //$NON-NLS-1$
								    CVSTag[] tags = tagSource.refresh(false, Policy.subMonitorFor(monitor, 70));
								    if (tags.length == 0 && promptForBestEffort()) {
								        tagSource.refresh(true, Policy.subMonitorFor(monitor, 30));
								    }
								} catch (TeamException e) {
									throw new InvocationTargetException(e);
								} finally {
								    monitor.done();
								}
							}
						});
					} catch (InterruptedException e) {
						// operation cancelled
					} catch (InvocationTargetException e) {
						CVSUIPlugin.openError(shell, Policy.bind("TagConfigurationDialog.14"), null, e); //$NON-NLS-1$
					}
				}
			});
		return refreshButton;		
	 }
	
    private boolean promptForBestEffort() {
        final boolean[] prompt = new boolean[] { false };
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
		        MessageDialog dialog = new MessageDialog(shell, Policy.bind("TagRefreshButtonArea.0"), null, //$NON-NLS-1$
		                getNoTagsFoundMessage(),
		                MessageDialog.INFORMATION,
		                new String[] {
		            		Policy.bind("TagRefreshButtonArea.1"), //$NON-NLS-1$
		            		Policy.bind("TagRefreshButtonArea.2"), //$NON-NLS-1$
		            		Policy.bind("TagRefreshButtonArea.3") //$NON-NLS-1$
		        		}, 1);
		        int code = dialog.open();
		        if (code == 0) {
		            prompt[0] = true;
		        } else if (code == 1) {
					TagConfigurationDialog d = new TagConfigurationDialog(shell, tagSource);
					d.open();
		        }

            }
        });
        return prompt[0];
    }
    
    private String getNoTagsFoundMessage() {
        return Policy.bind("TagRefreshButtonArea.4", tagSource.getShortDescription()); //$NON-NLS-1$
    }
    
	protected static ICVSFolder getSingleFolder(TagSource tagSource) {
	    if (tagSource instanceof SingleFolderTagSource)
	        return ((SingleFolderTagSource)tagSource).getFolder();
	    return null;
	}
    public TagSource getTagSource() {
        return tagSource;
    }
    public void setTagSource(TagSource tagSource) {
        Assert.isNotNull(tagSource);
        this.tagSource = tagSource;
    }

    public IRunnableContext getRunnableContext() {
        if (context == null)
            return PlatformUI.getWorkbench().getProgressService();
        return context;
    }
    
    public void setRunnableContext(IRunnableContext context) {
        this.context = context;
    }
}
