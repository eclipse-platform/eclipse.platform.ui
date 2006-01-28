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
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.core.mapping.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.mapping.DefaultResourceMappingMerger;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.mapping.ICompareAdapter;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

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
public abstract class ModelMergeOperation extends ModelOperation {
	
	/*
	 * Ids for custom buttons when previewing a merge/replace
	 */
	private static final int DONE_ID = IDialogConstants.CLIENT_ID + 1;
	private static final int REPLACE_ID = IDialogConstants.CLIENT_ID + 2;

	private IMergeContext context;
	private boolean ownsContext = true;

	/**
	 * Validate the merge context with the model providers that have mappings in
	 * the scope of the context. The {@link IResourceMappingMerger} for each
	 * model provider will be consulted and any non-OK status will be
	 * accumulated and returned,
	 * 
	 * @param context
	 *            the merge context being validated
	 * @param monitor
	 *            a progress monitor
	 * @return a status or multi-status that identify any conditions that should
	 *         force a preview of the merge
	 */
	public static IStatus validateMerge(IMergeContext context, IProgressMonitor monitor) {
		ModelProvider[] providers = context.getScope().getModelProviders();
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
		return new MultiStatus(TeamUIPlugin.ID, 0, (IStatus[]) notOK.toArray(new IStatus[notOK.size()]), TeamUIMessages.ResourceMappingMergeOperation_3, null);
	}
	
	/*
	 * Validate the merge by obtaining the {@link IResourceMappingMerger} for the
	 * given provider.
	 * @param provider the model provider
	 * @param context the merge context
	 * @param monitor a progress monitor
	 * @return the status obtained from the merger for the provider
	 */
	private static IStatus validateMerge(ModelProvider provider, IMergeContext context, IProgressMonitor monitor) {
		IResourceMappingMerger merger = getMerger(provider);
		return merger.validateMerge(context, monitor);
	}
	
	/**
	 * Return the auto-merger associated with the given model provider using the
	 * adaptable mechanism. If the model provider does not have a merger
	 * associated with it, a default merger that performs the merge at the file
	 * level is returned.
	 * 
	 * @param provider
	 *            the model provider of the elements to be merged (must not be
	 *            <code>null</code>)
	 * @return a merger
	 */
	public static IResourceMappingMerger getMerger(ModelProvider provider) {
		Assert.isNotNull(provider);
		IResourceMappingMerger merger = (IResourceMappingMerger)Utils.getAdapter(provider, IResourceMappingMerger.class);
		if (merger != null)
			return merger;
		return new DefaultResourceMappingMerger(provider);
	}
	
	/**
	 * Create a merge operation
	 * @param part the workbench part from which the merge was launched or <code>null</code>
	 * @param selectedMappings the selected mappings
	 */
	protected ModelMergeOperation(IWorkbenchPart part, ResourceMapping[] selectedMappings) {
		super(part, selectedMappings);
	}

	/**
	 * Create the model merge operation for the given context.
	 * The merge will be performed for the given context but
	 * the context will not be disposed by this operation
	 * (i.e. it is considered to be owned by the client).
	 * @param part the workbench part from which the merge was launched or <code>null</code>
	 * @param context the merge context to be merged
	 */
	public ModelMergeOperation(IWorkbenchPart part, IMergeContext context) {
		super(part, context.getScope());
		this.context = context;
		ownsContext = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ResourceMappingOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			monitor.beginTask(null, 100);
			if (context != null)
				context = buildMergeContext(Policy.subMonitorFor(monitor, 75));
			if (!hasChangesOfInterest()) {
				promptForNoChanges();
				return;
			}
			if (!isPreviewRequested()) {
				IStatus status = validateMerge(context, Policy.subMonitorFor(monitor, 5));
				if (status.isOK()) {
					status = performMerge(Policy.subMonitorFor(monitor, 20));
					if (status.isOK()) {
						// The merge was sucessful so we can just return
						return;
					} else {
						if (status.getCode() == IMergeStatus.CONFLICTS) {
							promptForMergeFailure(status);
						}
					}
				} else {
					// TODO prompt with validation message
				}
			}
			// Either auto-merging was not attemped or it was not 100% sucessful
			showPreview(Policy.subMonitorFor(monitor, 25));
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		} finally {
			if (ownsContext)
				context.dispose();
			monitor.done();
		}
	}

	/**
	 * Prompt for a failure to auto-merge
	 * @param status the status returned from the merger that reported the conflict
	 */
	protected void promptForMergeFailure(IStatus status) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(getShell(), TeamUIMessages.MergeIncomingChangesAction_0, TeamUIMessages.MergeIncomingChangesAction_1);
			};
		});
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
		return hasChangesMatching(tree, new FastDiffFilter() {
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
	 * @param monitor a progress monitor
	 */
	protected void showPreview(IProgressMonitor monitor) {
		calculateStates(context, Policy.subMonitorFor(monitor, 5));
		// TODO Ownership of the context is being transferred to the participant
		final ModelSynchronizeParticipant participant = createParticipant();
		ownsContext = false;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (isPreviewInDialog()) {
					CompareConfiguration cc = new CompareConfiguration();
					ISynchronizePageConfiguration pageConfiguration = participant.createPageConfiguration();
					// Restrict preview page to only support incomign and conflict modes
					if (pageConfiguration.getComparisonType() == ISynchronizePageConfiguration.THREE_WAY) {
						pageConfiguration.setSupportedModes(ISynchronizePageConfiguration.INCOMING_MODE | ISynchronizePageConfiguration.CONFLICTING_MODE);
						pageConfiguration.setMode(ISynchronizePageConfiguration.INCOMING_MODE);
					}
					ParticipantPageSaveablePart input = new ParticipantPageSaveablePart(getShell(), cc, pageConfiguration, participant);
					ParticipantPageDialog dialog = new ParticipantPageDialog(getShell(), input, participant) {
						private Button doneButton;
						private Button replaceButton;
	
						protected boolean isOfferToRememberParticipant() {
							boolean isReplace = context.getMergeType() == ISynchronizationContext.TWO_WAY;
							if (isReplace)
								return false;
							return super.isOfferToRememberParticipant();
						}
						
						protected void createButtonsForButtonBar(Composite parent) {
							boolean isReplace = context.getMergeType() == ISynchronizationContext.TWO_WAY;
							if (isReplace) {
								replaceButton = createButton(parent, REPLACE_ID, "&Replace", true); 
								replaceButton.setEnabled(true); 
							}
							doneButton = createButton(parent, DONE_ID, TeamUIMessages.ResourceMappingMergeOperation_2, !isReplace); 
							doneButton.setEnabled(true); 
							// Don't call super because we don't want the OK button to appear
						}
						protected void buttonPressed(int buttonId) {
							if (buttonId == DONE_ID) {
								super.buttonPressed(IDialogConstants.OK_ID);
							} else if (buttonId == REPLACE_ID) {
								try {
									// Do this inline so we don't have to manage disposing of the context
									PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
										public void run(IProgressMonitor monitor) throws InvocationTargetException,
												InterruptedException {
											try {
												performMerge(monitor);
											} catch (CoreException e) {
												throw new InvocationTargetException(e);
											}
										}
									
									});
								} catch (InvocationTargetException e) {
									Throwable t = e.getTargetException();
									IStatus status;
									if (t instanceof CoreException) {
										CoreException ce = (CoreException) t;
										status = ce.getStatus();
									} else {
										status = new Status(IStatus.ERROR, TeamUIPlugin.ID, 0, TeamUIMessages.internal, t);
										TeamUIPlugin.log(status);
									}
									ErrorDialog.openError(getShell(), null, null, status);
									return;
								} catch (InterruptedException e) {
									// Operation was cancelled. Leave the dialog open
									return;
								}
								super.buttonPressed(IDialogConstants.OK_ID);
							} else {
								super.buttonPressed(buttonId);
							}
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
	 * Attempt a headless merge of the elements in the context of this
	 * operation. The merge is performed by obtaining the
	 * {@link IResourceMappingMerger} for the model providers in the context's
	 * scope. The merger of the model providers are invoked in the order
	 * determined by the {@link ModelOperation#sortByExtension(ModelProvider[])}
	 * method. The method will stop on the first conflict encountered.
	 * This method will gthrow a runtime exception
	 * if the operation does not have a merge context.
	 * 
	 * @param monitor
	 *            a progress monitor
	 * @return a status that indicates whether the merge succeeded.
	 * @throws CoreException
	 *             if an error occurred
	 */
	protected IStatus performMerge(IProgressMonitor monitor) throws CoreException {
		ISynchronizationContext sc = getContext();
		if (sc instanceof IMergeContext) {
			IMergeContext context = (IMergeContext) sc;		
			final ModelProvider[] providers = sortByExtension(context.getScope().getModelProviders());
			final IStatus[] result = new IStatus[] { Status.OK_STATUS };
			context.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					try {
						monitor.beginTask(null, IProgressMonitor.UNKNOWN);
						for (int i = 0; i < providers.length; i++) {
							ModelProvider provider = providers[i];
							IStatus status = performMerge(provider, Policy.subMonitorFor(monitor, IProgressMonitor.UNKNOWN));
							if (!status.isOK()) {
								// Stop at the first failure
								result[0] = status;
								return;
							}
							// TODO: Need to wait until diff tree is up-to-date
						}
					} finally {
						monitor.done();
					}
				}
			}, null /* scheduling rule */, IResource.NONE, monitor);
			return result[0];
		}
		return noMergeContextAvailable();
	}
	
	/**
	 * Attempt to merge all the mappings that come from the given provider.
	 * Return a status which indicates whether the merge succeeded or if
	 * unmergeable conflicts were found. This method will gthrow a runtime exception
	 * if the operation does not have a merge context.
	 * @param provider the model provider whose mappings are to be merged
	 * @param monitor a progress monitor
	 * @return a non-OK status if there were unmergable conflicts
	 * @throws CoreException if an error occurred
	 */
	protected IStatus performMerge(ModelProvider provider, IProgressMonitor monitor) throws CoreException {
		ISynchronizationContext sc = getContext();
		if (sc instanceof IMergeContext) {
			IMergeContext context = (IMergeContext) sc;
			IResourceMappingMerger merger = getMerger(provider);
			IStatus status = merger.merge(context, monitor);
			if (status.isOK() || status.getCode() == IMergeStatus.CONFLICTS) {
				return status;
			}
			throw new TeamException(status);
		}
		return noMergeContextAvailable();
	}

	private IStatus noMergeContextAvailable() {
		throw new IllegalStateException("Merges should only be attemped for operations that have a merge context");
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
			return TeamUIMessages.ResourceMappingMergeOperation_4; 
		}
		return super.getPreviewRequestMessage();
	}
	
	/**
	 * Return whether the given diff tree contains any deltas that match the given filter.
	 * @param tree the diff tree
	 * @param filter the diff node filter
	 * @return whether the given diff tree contains any deltas that match the given filter
	 */
	protected boolean hasChangesMatching(IDiffTree tree, final FastDiffFilter filter) {
		final CoreException found = new CoreException(Status.OK_STATUS);
		try {
			tree.accept(ResourcesPlugin.getWorkspace().getRoot().getFullPath(), new IDiffVisitor() {
				public boolean visit(IDiffNode delta) throws CoreException {
					if (filter.select(delta)) {
						throw found;
					}
					return false;
				}
			
			}, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			if (e == found)
				return true;
			TeamUIPlugin.log(e);
		}
		return false;
	}

	/**
	 * Create the synchronize pariticipant to be used by this operation
	 * to preview changes. By default, a {@link ModelSynchronizeParticipant}
	 * is created using he context ({@link #getContext()}) and job name ({@link #getJobName()})
	 * of this operation. Subclasses may override this method.
	 * <p>
	 * Once created, it is the responsibility of the participant to dispose of the 
	 * synchronization context when it is no longer needed. 
	 * @return a newly created synchronize pariticipant to be used by this operation
	 */
	protected ModelSynchronizeParticipant createParticipant() {
		return ModelSynchronizeParticipant.createParticipant(getContext(), getJobName());
	}

}
