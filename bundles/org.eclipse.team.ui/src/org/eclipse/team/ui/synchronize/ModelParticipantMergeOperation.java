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
package org.eclipse.team.ui.synchronize;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.resources.mapping.RemoteResourceMappingContext;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.mapping.ISynchronizationScopeManager;
import org.eclipse.team.core.mapping.provider.SynchronizationContext;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.mapping.ModelParticipantPageDialog;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.mapping.ModelMergeOperation;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.WorkbenchJob;

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
public abstract class ModelParticipantMergeOperation extends ModelMergeOperation {
	
	/**
	 * Status code that can be returned from the {@link #performMerge(IProgressMonitor)}
	 * method to indicate that a subclass would like to force a preview of the merge.
	 * The message of such a status should be ignored.
	 */
	public static final int REQUEST_PREVIEW = 1024;

	private ModelSynchronizeParticipant participant;
	private boolean ownsParticipant = true;
	
	/**
	 * Create a merge participant operation for the scope of the given manager.
	 * @param part the workbench part from which the merge was launched or <code>null</code>
	 * @param manager the scope manager
	 */
	protected ModelParticipantMergeOperation(IWorkbenchPart part, ISynchronizationScopeManager manager) {
		super(part, manager);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ModelMergeOperation#initializeContext(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void initializeContext(IProgressMonitor monitor) throws CoreException {
		if (participant == null) {
			participant = createParticipant();
			participant.getContext().refresh(getScope().getTraversals(), 
					RemoteResourceMappingContext.FILE_CONTENTS_REQUIRED, monitor);
			try {
				Platform.getJobManager().join(participant.getContext(), monitor);
			} catch (InterruptedException e) {
				throw new OperationCanceledException();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ModelMergeOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			super.execute(monitor);
		} finally {
			if (ownsParticipant && participant != null)
				participant.dispose();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ModelMergeOperation#handlePreviewRequest()
	 */
	protected void handlePreviewRequest() {
		Job job = new WorkbenchJob(getJobName()) {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (isPreviewInDialog()) {
					CompareConfiguration cc = new CompareConfiguration();
					ISynchronizePageConfiguration pageConfiguration = participant.createPageConfiguration();
					// Restrict preview page to only support incomign and conflict modes
					if (pageConfiguration.getComparisonType() == ISynchronizePageConfiguration.THREE_WAY) {
						pageConfiguration.setSupportedModes(ISynchronizePageConfiguration.INCOMING_MODE | ISynchronizePageConfiguration.CONFLICTING_MODE);
						pageConfiguration.setMode(ISynchronizePageConfiguration.INCOMING_MODE);
					}
					ParticipantPageDialog dialog = new ModelParticipantPageDialog(getShell(), participant, cc, pageConfiguration);
					dialog.open();
				} else {				
					ISynchronizeManager mgr = TeamUI.getSynchronizeManager();
					ISynchronizeView view = mgr.showSynchronizeViewInActivePage();
					mgr.addSynchronizeParticipants(new ISynchronizeParticipant[] {participant});
					view.display(participant);
				}
				return Status.OK_STATUS;
			}
		};
		job.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				// Ensure that the participant is disposed it it didn't go to the sync view
				if (TeamUI.getSynchronizeManager().get(participant.getId(), participant.getSecondaryId()) == null)
					participant.dispose();
			}
			
		});
		ownsParticipant = false;
		job.schedule();
	}

	/**
	 * Return whether previews should occur in a dialog or in the synchronize view.
	 * @return whether previews should occur in a dialog or in the synchronize view
	 */
	protected boolean isPreviewInDialog() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ResourceMappingOperation#getContext()
	 */
	protected ISynchronizationContext getContext() {
		if (participant != null)
			return participant.getContext();
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ResourceMappingOperation#getPreviewRequestMessage()
	 */
	protected String getPreviewRequestMessage() {
		if (!isPreviewRequested()) {
			return TeamUIMessages.ResourceMappingMergeOperation_4; 
		}
		return super.getPreviewRequestMessage();
	}

	/**
	 * Create the synchronize pariticipant to be used by this operation
	 * to preview changes. By default, a {@link ModelSynchronizeParticipant}
	 * is created using the scope manager ({@link #getScopeManager()}) context 
	 * from ({@link #createMergeContext()}) and job name ({@link #getJobName()})
	 * of this operation. Subclasses may override this method.
	 * <p>
	 * Once created, it is the responsibility of the participant to dispose of the 
	 * synchronization context when it is no longer needed. 
	 * @return a newly created synchronize pariticipant to be used by this operation
	 */
	protected ModelSynchronizeParticipant createParticipant() {
		return ModelSynchronizeParticipant.createParticipant(createMergeContext(), getJobName());
	}

	/**
	 * Create a merge context for use by this operation. This method
	 * is not long running so the operation should not refresh the 
	 * context or perform other long running operations in this thread.
	 * However the context may start initializing in another thread as long
	 * as the job used to perform the initialization belongs to the
	 * family that matches the context.
	 * @return a merge context for use by this operation
	 */
	protected abstract SynchronizationContext createMergeContext();

}
