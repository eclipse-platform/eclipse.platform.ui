package org.eclipse.team.internal.ui.target;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.core.target.TargetProvider;
import org.eclipse.team.internal.core.InfiniteSubProgressMonitor;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.ui.DetailsDialog;
import org.eclipse.team.internal.ui.IPromptCondition;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.PromptingDialog;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Action for getting the contents of the selected resources
 */
public class GetAction extends TargetAction {	
	
	private class OutgoingChangesDialog extends DetailsDialog {
		private IResource[] outgoingChanges;
		private org.eclipse.swt.widgets.List detailsList;
		
		public OutgoingChangesDialog(Shell shell, IResource[] outgoingChanges) {
			super(shell, Policy.bind("GetAction.confirmFileOverwriteTitle")); //$NON-NLS-1$
			this.outgoingChanges = outgoingChanges;
		}
			
		protected Composite createDropDownDialogArea(Composite parent) {
			// create a composite with standard margins and spacing
			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
			layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
			layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
			layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
			composite.setLayout(layout);
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			composite.setFont(parent.getFont());
			
			detailsList = new org.eclipse.swt.widgets.List(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);	 
			GridData data = new GridData ();		
			data.heightHint = 75;
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace = true;
			detailsList.setLayoutData(data);
			
			for (int i = 0; i < outgoingChanges.length; i++) {
				detailsList.add(outgoingChanges[i].getFullPath().toString()); //$NON-NLS-1$
			}			
			return composite;
		}

		protected void createMainDialogArea(Composite top) {
			Composite parent = new Composite(top, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.numColumns = 2;
			parent.setLayout(layout);
			parent.setLayoutData(new GridData(GridData.FILL_BOTH));
			parent.setFont(parent.getFont());
			
			// create image
			Image image = getImage(DLG_IMG_QUESTION);
			if (image != null) {
				Label label = new Label(parent, 0);
				image.setBackground(label.getBackground());
				label.setImage(image);
				label.setLayoutData(new GridData(
					GridData.HORIZONTAL_ALIGN_CENTER |
					GridData.VERTICAL_ALIGN_BEGINNING));
			}
			
			Label label = new Label(parent, SWT.WRAP);
			label.setText(Policy.bind("GetAction.confirmFileOverwrite")); //$NON-NLS-1$
			GridData data = new GridData(
				GridData.GRAB_HORIZONTAL |
				GridData.GRAB_VERTICAL |
				GridData.HORIZONTAL_ALIGN_FILL |
				GridData.VERTICAL_ALIGN_CENTER);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			label.setLayoutData(data);
			label.setFont(parent.getFont());				
			setPageComplete(true);
		}

		protected void updateEnablements() {
		}
	}
	
	public void run(IAction action) {	
		if(promptForOutgoingChanges()) {
			run(new WorkspaceModifyOperation() {
				public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
					try {
						Hashtable table = getTargetProviderMapping();
						Set keySet = table.keySet();
						monitor.beginTask(null, keySet.size() * 1000);
						
						// perform the get on each provider
						Iterator iterator = keySet.iterator();
						while (iterator.hasNext()) {					
							TargetProvider provider = (TargetProvider)iterator.next();
							monitor.setTaskName(Policy.bind("GetAction.working", provider.getURL().toExternalForm()));  //$NON-NLS-1$
							List list = (List)table.get(provider);
							IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
							provider.get(providerResources, Policy.subInfiniteMonitorFor(monitor, 1000));
						}
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			}, Policy.bind("GetAction.title"), this.PROGRESS_DIALOG); //$NON-NLS-1$
		}
	}
	
	private boolean promptForOutgoingChanges() {
		try {
			// find any outgoing changes that will be overwritten and prompt
			Hashtable table = getTargetProviderMapping();
			Set keySet = table.keySet();
	
			Iterator iterator = keySet.iterator();
			List outgoingChanges = new ArrayList();
			while (iterator.hasNext()) {					
				TargetProvider provider = (TargetProvider)iterator.next();
				List list = (List)table.get(provider);
				IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
				outgoingChanges.addAll(Arrays.asList(findResourcesWithOutgoingChanges(providerResources)));
			}
			if(! outgoingChanges.isEmpty()) {
				final OutgoingChangesDialog dialog = new OutgoingChangesDialog(getShell(), 
														(IResource[]) outgoingChanges.toArray(new IResource[outgoingChanges.size()]));
				final boolean okToContinue[] = {true};
				getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						if(dialog.open() != dialog.OK) {
							okToContinue[0] = false;
						}
					}
				});
				return okToContinue[0];
			}
			return true;
		} catch(CoreException e) {
			TeamUIPlugin.handle(e);
		} catch(TeamException e) {
			TeamUIPlugin.handle(e);
		}		
		return false;
	}
	
	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0) return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			try {
				TargetProvider provider = TargetManager.getProvider(resource.getProject());			
				if(provider == null)
					return false;
				if(! provider.canPut(resource))
					return false;	//if one can't don't allow for any
			} catch (TeamException e) {
				TeamPlugin.log(IStatus.ERROR, Policy.bind("GetAction.Exception_getting_provider_2"), e); //$NON-NLS-1$
				return false;
			}
		}
		return true;
	}
}