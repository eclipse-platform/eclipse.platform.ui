/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.PixelConverter;
import org.eclipse.team.internal.ui.SWTUtils;
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
	private Label fMessageLabel;

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
    	
    	final String addButtonLabel= Policy.bind("TagConfigurationDialog.21"); //$NON-NLS-1$
    	final String refreshButtonLabel= Policy.bind("TagConfigurationDialog.20"); //$NON-NLS-1$
    	
    	final PixelConverter converter= SWTUtils.createDialogPixelConverter(parent);
    	
    	final Composite buttonComp = new Composite(parent, SWT.NONE);
	 	buttonComp.setLayoutData(SWTUtils.createHFillGridData());//SWT.DEFAULT, SWT.DEFAULT, SWT.END, SWT.TOP, false, false));
	 	buttonComp.setLayout(SWTUtils.createGridLayout(3, converter, SWTUtils.MARGINS_NONE));
	 	
		fMessageLabel= SWTUtils.createLabel(buttonComp, null);
		refreshButton = new Button(buttonComp, SWT.PUSH);
		refreshButton.setText (refreshButtonLabel);
		
		final Button addButton = new Button(buttonComp, SWT.PUSH);
		addButton.setText (addButtonLabel);
		
		
		Dialog.applyDialogFont(buttonComp);
		final int buttonWidth= SWTUtils.calculateControlSize(converter, new Button [] { addButton, refreshButton });
		refreshButton.setLayoutData(SWTUtils.createGridData(buttonWidth, SWT.DEFAULT, SWT.END, SWT.CENTER, false, false));
		addButton.setLayoutData(SWTUtils.createGridData(buttonWidth, SWT.DEFAULT, SWT.END, SWT.CENTER, false, false));
		
		refreshButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				refresh(false);						
			}
		});
	 	
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
    
    
    public void refresh(final boolean background) {
		try {
			getRunnableContext().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						setBusy(true);
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								fMessageLabel.setText(Policy.bind("TagRefreshButtonArea.6")); //$NON-NLS-1$
							}
						});
						monitor.beginTask(Policy.bind("TagRefreshButtonArea.5"), 100); //$NON-NLS-1$
						final CVSTag[] tags = tagSource.refresh(false, Policy.subMonitorFor(monitor, 70));
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								fMessageLabel.setText(background && tags.length == 0 ? Policy.bind("TagRefreshButtonArea.7") : ""); //$NON-NLS-1$ //$NON-NLS-2$
							}
						});
						if (!background && tags.length == 0 && promptForBestEffort()) {
							tagSource.refresh(true, Policy.subMonitorFor(monitor, 30));
						}
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					} finally {
						setBusy(false);
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
    
    private void setBusy(final boolean busy) {
    	if (shell != null && !shell.isDisposed())
			shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					refreshButton.setEnabled(!busy);
				}
			});
    }
	
	/*
	 * Returns a button that implements the standard refresh tags operation. The
	 * runnable is run immediatly after the tags are fetched from the server. A
	 * client should refresh their widgets that show tags because they may of
	 * changed.
	 */
	private Button createTagRefreshButton(Composite composite, String title) {
		Button refreshButton = new Button(composite, SWT.PUSH);
		refreshButton.setText (title);
		refreshButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					refresh(false);						
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
