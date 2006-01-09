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
package org.eclipse.team.ui.operations;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.mapping.ICompareAdapter;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.IWorkbenchPart;

/**
 * The steps of an optimistic merge operation are:
 * <ol>
 * <li>Obtain the selection to be operated on.
 * <li>Determine the projection of the selection onto resources
 * using resource mappings and traversals.
 * 		<ul>
 * 		<li>this will require traversals using both the ancestor and remote
 *      for three-way merges.
 *      <li>for model providers with registered merger, mapping set need 
 *      not be expanded (this is tricky if one of the model providers doesn't
 *      have a merge but all others do).
 *      <li>if the model does not have a custom merger, ensure that additional
 *      mappings are included (i.e. for many model elements to one resource case)
 * 		</ul>
 * <li>Create a MergeContext for the merge
 *      <ul>
 * 		<li>Determine the synchronization state of all resources
 *      covered by the input.
 *      <li>Pre-fetch the required contents.
 * 		</ul>
 * <li>Obtain and invoke the merger for each provider
 *      <ul>
 * 		<li>This will auto-merge as much as possible
 *      <li>If everything was merged, cleanup and stop
 *      <li>Otherwise, a set of un-merged resource mappings is returned
 * 		</ul>
 * <li>Delegate manual merge to the model provider
 *      <ul>
 * 		<li>This hands off the context to the manual merge
 *      <li>Once completed, the manual merge must clean up
 * 		</ul>
 * </ol>
 * 
 * <p>
 * Handle multiple model providers where one extends all others by using
 * the top-most model provider. The assumption is that the model provider
 * will delegate to lower level model providers when appropriate.
 * <p>
 * Special case to support sub-file merges.
 * <ul>
 * <li>Restrict when sub-file merging is supported
 * 		<ul>
 * 		<li>Only one provider involved (i.e. consulting participants results
 * 		in participants that are from the model provider or below).
 * 		<li>The provider has a custom auto and manual merger.
 * 		</ul>
 * <li>Prompt to warn when sub-file merging is not possible.
 * <li>Need to display the additional elements that will be affected.
 * This could be done in a diff tree or some other view. It needs to
 * consider incoming changes including additions.
 * </ul>
 * <p>
 * Special case to handle conflicting model providers.
 * <ul>
 * <li>Prompt user to indicate the conflict
 * <li>Allow user to exclude one of the models?
 * <li>Allow use to choose order of evaluation?
 * <li>Support tabbed sync view
 * </ul>
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public abstract class ResourceMappingMergeOperation extends ResourceMappingOperation {

	private IMergeContext context;

	protected ResourceMappingMergeOperation(IWorkbenchPart part, ResourceMapping[] selectedMappings, ResourceMappingContext context) {
		super(part, selectedMappings, context);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ResourceMappingOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			monitor.beginTask(null, 100);
			context = buildMergeContext(Policy.subMonitorFor(monitor, 75));
			if (!hasChangesOfInterest()) {
				promptForNoChanges();
				context.dispose();
				return;
			}
			if (!isPreviewRequested()) {
				IStatus status = validateMerge(context, Policy.subMonitorFor(monitor, 5));
				if (status.isOK()) {
					if (performMerge(Policy.subMonitorFor(monitor, 20)))
						// The merge was sucessful so we can just return
						return;
				}
			}
			// Either auto-merging was not attemped or it was not 100% sucessful
			// TODO If there is a problem between here and when the preview is shown, the context may be leaked
			showPreview(getJobName(), Policy.subMonitorFor(monitor, 25));
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Return whether the context of this operation has changes that are
	 * of interest to the operation. Sublcasses may override.
	 * @return whether the context of this operation has changes that are
	 * of interest to the operation
	 */
	protected boolean hasChangesOfInterest() {
		return !context.getDiffTree().isEmpty() && hasIncomingChanges(context.getDiffTree());
	}

	private boolean hasIncomingChanges(IDiffTree tree) {
		return hasChangesMatching(tree, new FastDiffNodeFilter() {
			public boolean select(IDiffNode node) {
				if (node instanceof IThreeWayDiff) {
					IThreeWayDiff twd = (IThreeWayDiff) node;
					int direction = twd.getDirection();
					if (direction == IThreeWayDiff.INCOMING || direction == IThreeWayDiff.CONFLICTING) {
						return true;
					}
				} else {
					// Return true for any two-way change
					return true;
				}
				return false;
			}
		});
	}
	
	private IStatus validateMerge(IMergeContext context, IProgressMonitor monitor) {
		ModelProvider[] providers = getScope().getModelProviders();
		monitor.beginTask(null, 100 * providers.length);
		List notOK = new ArrayList();
		for (int i = 0; i < providers.length; i++) {
			ModelProvider provider = providers[i];
			IStatus status = validateMerge(provider, context, Policy.subMonitorFor(monitor, 100));
			if (!status.isOK())
				notOK.add(status);
		}
		if (notOK.isEmpty())
			return Status.OK_STATUS;
		if (notOK.size() == 1)
			return (IStatus)notOK.get(0);
		return new MultiStatus(TeamUIPlugin.ID, 0, (IStatus[]) notOK.toArray(new IStatus[notOK.size()]), "Multiple potential side effects of this operation have been detected.", null);
	}

	/**
	 * Method invoked when the context contains no changes so that the user
	 * can be informed.
	 */
	protected void promptForNoChanges() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(getShell(), TeamUIMessages.ResourceMappingMergeOperation_0, TeamUIMessages.ResourceMappingMergeOperation_1);
			};
		});
	}

	/**
	 * Preview the merge so the user can perform the merge manually.
	 * @param title the title of the merge.
	 * @param monitor a progress monitor
	 */
	protected void showPreview(final String title, IProgressMonitor monitor) {
		calculateStates(context, Policy.subMonitorFor(monitor, 5));
			Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				ModelSynchronizeParticipant participant = new ModelSynchronizeParticipant(context, title);
				if (isPreviewInDialog()) {
					CompareConfiguration cc = new CompareConfiguration();
					ModelParticipantPageSavablePart input = new ModelParticipantPageSavablePart(getShell(), cc, participant.createPageConfiguration(), participant);
					ParticipantPageDialog dialog = new ParticipantPageDialog(getShell(), input, participant) {
						private Button doneButton;
	
						protected void createButtonsForButtonBar(Composite parent) {
							doneButton = createButton(parent, 10, TeamUIMessages.ResourceMappingMergeOperation_2, true); 
							doneButton.setEnabled(true); 
							// Don't call super because we don't want the OK button to appear
						}
						protected void buttonPressed(int buttonId) {
							if (buttonId == 10)
								super.buttonPressed(IDialogConstants.OK_ID);
							else 
								super.buttonPressed(buttonId);
						}
					};
					int result = dialog.open();
					input.dispose();
					if (TeamUI.getSynchronizeManager().get(participant.getId(), participant.getSecondaryId()) == null)
						participant.dispose();
				} else {				
					ISynchronizeManager mgr = TeamUI.getSynchronizeManager();
					ISynchronizeView view = mgr.showSynchronizeViewInActivePage();
					mgr.addSynchronizeParticipants(new ISynchronizeParticipant[] {participant});
					view.display(participant);
				}
			}
		});
	}

	/**
	 * Return whether previews should occur in a dialog or in the synchronize view.
	 * @return whether previews should occur in a dialog or in the synchronize view
	 */
	protected boolean isPreviewInDialog() {
		return true;
	}

	private void calculateStates(ISynchronizationContext context, IProgressMonitor monitor) {
		monitor.beginTask(null, IProgressMonitor.UNKNOWN);
		ModelProvider[] providers = getScope().getModelProviders();
		for (int i = 0; i < providers.length; i++) {
			ModelProvider provider = providers[i];
			calculateStates(context, provider, Policy.subMonitorFor(monitor, IProgressMonitor.UNKNOWN));
		}
		monitor.done();
	}

	private void calculateStates(ISynchronizationContext context, ModelProvider provider, IProgressMonitor monitor) {
		Object o = provider.getAdapter(ICompareAdapter.class);
		if (o instanceof ICompareAdapter) {
			ICompareAdapter calculator = (ICompareAdapter) o;
			try {
				calculator.prepareContext(context, monitor);
			} catch (CoreException e) {
				TeamUIPlugin.log(e);
			}
		}
		monitor.done();
	}

	/**
	 * Perform the merge for the context of the operation. If the merge was not
	 * succesful in it's entirety, there are still changes left to be merged.
	 * Clients can decide how to handle this.
	 * 
	 * @param monitor a prohress monitor
	 * @return whether the merge was successful in it's entirety
	 * @throws CoreException
	 */
	protected boolean performMerge(IProgressMonitor monitor) throws CoreException {
		boolean merged = performMerge(context, monitor);
		if (merged) {
			context.dispose();
		}
		return merged;
	}

	/**
	 * Build and initialize a merge context for the input of this operation.
	 * @param monitor a progress monitor
	 * @return a merge context for merging the mappings of the input
	 */ 
	protected abstract IMergeContext buildMergeContext(IProgressMonitor monitor) throws CoreException;

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ResourceMappingOperation#getContext()
	 */
	protected ISynchronizationContext getContext() {
		return context;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ResourceMappingOperation#getPreviewRequestMessage()
	 */
	protected String getPreviewRequestMessage() {
		if (!isPreviewRequested()) {
			return "Preview the merge before it is performed"; 
		}
		return super.getPreviewRequestMessage();
	}

}
