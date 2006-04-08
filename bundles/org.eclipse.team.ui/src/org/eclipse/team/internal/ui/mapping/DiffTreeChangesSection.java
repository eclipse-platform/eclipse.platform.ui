/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import java.util.*;

import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.mapping.ISynchronizationScope;
import org.eclipse.team.internal.core.subscribers.SubscriberDiffTreeEventHandler;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.registry.TeamContentProviderDescriptor;
import org.eclipse.team.internal.ui.registry.TeamContentProviderManager;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.mapping.*;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class DiffTreeChangesSection extends ForwardingChangesSection implements IDiffChangeListener, IPropertyChangeListener, IEmptyTreeListener {

	private ISynchronizationContext context;
	private IStatus[] errors;
	private boolean showingError;
	
	public interface ITraversalFactory {
		ResourceTraversal[] getTraversals(ISynchronizationScope scope);
	}

	public DiffTreeChangesSection(Composite parent, AbstractSynchronizePage page, ISynchronizePageConfiguration configuration) {
		super(parent, page, configuration);
		context = (ISynchronizationContext)configuration.getProperty(ITeamContentProviderManager.P_SYNCHRONIZATION_CONTEXT);
		context.getDiffTree().addDiffChangeListener(this);
		getConfiguration().addPropertyChangeListener(this);
		Platform.getJobManager().addJobChangeListener(new JobChangeAdapter() {
			public void running(IJobChangeEvent event) {
				if (isJobOfInterest(event.getJob())) {
					if (context.getDiffTree().isEmpty())
						calculateDescription();
				}
			}
			private boolean isJobOfInterest(Job job) {
				if (job.belongsTo(getConfiguration().getParticipant()))
					return true;
				SubscriberDiffTreeEventHandler handler = getHandler();
				if (handler != null && handler.getEventHandlerJob() == job)
					return true;
				return false;
			}
			public void done(IJobChangeEvent event) {
				if (isJobOfInterest(event.getJob())) {
					if (context.getDiffTree().isEmpty())
						calculateDescription();
				}
			}
		});
	}

	public void dispose() {
		context.getDiffTree().removeDiffChangeListener(this);
		getConfiguration().removePropertyChangeListener(this);
		super.dispose();
	}
	
	protected int getChangesCount() {
		return context.getDiffTree().size();
	}

	protected long getChangesInMode(int candidateMode) {
		long numChanges;
		long numConflicts = context.getDiffTree().countFor(IThreeWayDiff.CONFLICTING, IThreeWayDiff.DIRECTION_MASK);
		switch (candidateMode) {
		case ISynchronizePageConfiguration.CONFLICTING_MODE:
			numChanges = numConflicts;
			break;
		case ISynchronizePageConfiguration.OUTGOING_MODE:
			numChanges = numConflicts + context.getDiffTree().countFor(IThreeWayDiff.OUTGOING, IThreeWayDiff.DIRECTION_MASK);
			break;
		case ISynchronizePageConfiguration.INCOMING_MODE:
			numChanges = numConflicts + context.getDiffTree().countFor(IThreeWayDiff.INCOMING, IThreeWayDiff.DIRECTION_MASK);
			break;
		case ISynchronizePageConfiguration.BOTH_MODE:
			numChanges = numConflicts + context.getDiffTree().countFor(IThreeWayDiff.INCOMING, IThreeWayDiff.DIRECTION_MASK) 
				+ context.getDiffTree().countFor(IThreeWayDiff.OUTGOING, IThreeWayDiff.DIRECTION_MASK);
			break;
		default:
			numChanges = 0;
			break;
		}
		return numChanges;
	}
	
	protected boolean hasChangesInMode(String id, ISynchronizationCompareAdapter adapter, int candidateMode) {
		switch (candidateMode) {
		case ISynchronizePageConfiguration.CONFLICTING_MODE:
			return hasChangesFor(id, adapter, context, new int[] { IThreeWayDiff.CONFLICTING }, IThreeWayDiff.DIRECTION_MASK);
		case ISynchronizePageConfiguration.OUTGOING_MODE:
			return hasChangesFor(id, adapter, context, new int[] { IThreeWayDiff.CONFLICTING, IThreeWayDiff.OUTGOING }, IThreeWayDiff.DIRECTION_MASK);
		case ISynchronizePageConfiguration.INCOMING_MODE:
			return hasChangesFor(id, adapter, context, new int[] { IThreeWayDiff.CONFLICTING, IThreeWayDiff.INCOMING }, IThreeWayDiff.DIRECTION_MASK);
		case ISynchronizePageConfiguration.BOTH_MODE:
			return hasChangesFor(id, adapter, context, new int[] { IThreeWayDiff.CONFLICTING, IThreeWayDiff.INCOMING, IThreeWayDiff.OUTGOING }, IThreeWayDiff.DIRECTION_MASK);
		}
		return false;
	}

	private boolean hasChangesFor(String id, ISynchronizationCompareAdapter adapter, ISynchronizationContext context, int[] states, int mask) {
		ITraversalFactory factory = (ITraversalFactory)Utils.getAdapter(adapter, ITraversalFactory.class);
		ResourceTraversal[] traversals;
		if (factory == null) {
			traversals = context.getScope().getTraversals(id);
		} else {
			traversals = factory.getTraversals(context.getScope());
		}
		return (context.getDiffTree().hasMatchingDiffs(traversals, FastDiffFilter.getStateFilter(states, mask)));
	}

	protected long getVisibleChangesCount() {
		ISynchronizePageConfiguration configuration = getConfiguration();
		if (configuration.getComparisonType() == ISynchronizePageConfiguration.TWO_WAY) {
			return context.getDiffTree().size();
		}
		int currentMode =  configuration.getMode();
		String id = (String)configuration.getProperty(ModelSynchronizeParticipant.P_VISIBLE_MODEL_PROVIDER);
		if (id != null && !id.equals(ModelSynchronizeParticipant.ALL_MODEL_PROVIDERS_VISIBLE)) {
			try {
				IModelProviderDescriptor desc = ModelProvider.getModelProviderDescriptor(id);
				ISynchronizationCompareAdapter adapter = Utils.getCompareAdapter(desc.getModelProvider());
				if (adapter != null) {
					return hasChangesInMode(desc.getId(), adapter, getConfiguration().getMode()) ? -1 : 0;
				}
			} catch (CoreException e) {
				TeamUIPlugin.log(e);
			}
			// Use the view state to indicate whether there are visible changes
			return isViewerEmpty() ? 0 : -1;
		}
		return getChangesInMode(currentMode);
	}

	protected int getCandidateMode() {
		SynchronizePageConfiguration configuration = (SynchronizePageConfiguration)getConfiguration();
		long outgoingChanges = context.getDiffTree().countFor(IThreeWayDiff.OUTGOING, IThreeWayDiff.DIRECTION_MASK);
		if (outgoingChanges > 0) {
			if (configuration.isModeSupported(ISynchronizePageConfiguration.OUTGOING_MODE)) {
				return ISynchronizePageConfiguration.OUTGOING_MODE;
			}
			if (configuration.isModeSupported(ISynchronizePageConfiguration.BOTH_MODE)) {
				return ISynchronizePageConfiguration.BOTH_MODE;
			}
		}
		long incomingChanges = context.getDiffTree().countFor(IThreeWayDiff.INCOMING, IThreeWayDiff.DIRECTION_MASK);
		if (incomingChanges > 0) {
			if (configuration.isModeSupported(ISynchronizePageConfiguration.INCOMING_MODE)) {
				return ISynchronizePageConfiguration.INCOMING_MODE;
			}
			if (configuration.isModeSupported(ISynchronizePageConfiguration.BOTH_MODE)) {
				return ISynchronizePageConfiguration.BOTH_MODE;
			}
		}
		return configuration.getMode();
	}

	public void diffsChanged(IDiffChangeEvent event, IProgressMonitor monitor) {
		IStatus[] errors = event.getErrors();
		if (errors.length > 0) {
			this.errors = errors;
		}
		calculateDescription();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.diff.IDiffChangeListener#propertyChanged(int, org.eclipse.core.runtime.IPath[])
	 */
	public void propertyChanged(IDiffTree tree, int property, IPath[] paths) {
		// Do nothing
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(ISynchronizePageConfiguration.P_MODE)
				|| event.getProperty().equals(ModelSynchronizeParticipant.P_VISIBLE_MODEL_PROVIDER)) {
			calculateDescription();
		}
	}
	
	protected Composite getEmptyChangesComposite(Composite parent) {
		if (context.getDiffTree().isEmpty()) {
			SubscriberDiffTreeEventHandler handler = getHandler();
			if (handler != null && handler.getState() == SubscriberDiffTreeEventHandler.STATE_STARTED) {
				// The context has not been initialized yet
				return getInitializationPane(parent);
			}
			if (isRefreshRunning() || (handler != null && handler.getEventHandlerJob().getState() != Job.NONE)) {
				return getInitializingMessagePane(parent);
			}
		} else {
			ISynchronizePageConfiguration configuration = getConfiguration();
			String id = (String)configuration.getProperty(ModelSynchronizeParticipant.P_VISIBLE_MODEL_PROVIDER);
			if (id != null && !id.equals(ModelSynchronizeParticipant.ALL_MODEL_PROVIDERS_VISIBLE)) {
				// A particular model is active so we need to look for a model that has changes in this
				// same mode before offering to change modes.
				ModelProvider[] providers =context.getScope().getModelProviders();
				providers = ModelOperation.sortByExtension(providers);
				for (int i = 0; i < providers.length; i++) {
					ModelProvider provider = providers[i];
					if (isEnabled(provider)) {
						ISynchronizationCompareAdapter adapter = Utils.getCompareAdapter(provider);
						if (adapter != null) {
							boolean hasChanges = hasChangesInMode(provider.getId(), adapter, getConfiguration().getMode());
							if (hasChanges && !provider.getDescriptor().getId().equals(id)) {
								return getPointerToModel(parent, provider, id);
							}
						}
					}
				}
			}
			return createEnableParticipantModelProvidersPane(parent);
		}
		return super.getEmptyChangesComposite(parent);
	}

	private boolean isEnabled(ModelProvider provider) {
		ITeamContentProviderDescriptor desc = TeamUI.getTeamContentProviderManager().getDescriptor(provider.getId());
		return (desc != null && desc.isEnabled());
	}

	private Composite createEnableParticipantModelProvidersPane(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(getBackgroundColor());
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);

		String message;
		int changesCount = getChangesCount();
		if (changesCount == 1) {
			message = TeamUIMessages.DiffTreeChangesSection_8;
		} else {
			message = NLS.bind(TeamUIMessages.DiffTreeChangesSection_9, new Integer(changesCount));
		}
		final ITeamContentProviderDescriptor[] descriptors = getEnabledContentDescriptors();
		if (descriptors.length == 0)
			message = NLS.bind(TeamUIMessages.DiffTreeChangesSection_10, message);
		else
			message = NLS.bind(TeamUIMessages.DiffTreeChangesSection_11, message);
		
		createDescriptionLabel(composite, message);
		
		Label warning = new Label(composite, SWT.NONE);
		warning.setImage(TeamUIPlugin.getPlugin().getImage(ISharedImages.IMG_WARNING_OVR));
		
		Hyperlink link = getForms().createHyperlink(composite, TeamUIMessages.DiffTreeChangesSection_12, SWT.WRAP); 
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				ModelSynchronizeParticipant participant = (ModelSynchronizeParticipant)getConfiguration().getParticipant();
				ModelProvider[] providers = participant.getEnabledModelProviders();
				for (int i = 0; i < providers.length; i++) {
					ModelProvider provider = providers[i];
					ITeamContentProviderDescriptor desc = TeamUI.getTeamContentProviderManager().getDescriptor(provider.getId());
					if (desc != null && !desc.isEnabled())
						((TeamContentProviderDescriptor)desc).setEnabled(true);
				}
				((TeamContentProviderManager)TeamUI.getTeamContentProviderManager()).enablementChanged(
						descriptors,
						getEnabledContentDescriptors());

			}
		});
		getForms().getHyperlinkGroup().add(link);
		
		return composite;
	}

	private ITeamContentProviderDescriptor[] getEnabledContentDescriptors() {
		ModelSynchronizeParticipant participant = (ModelSynchronizeParticipant)getConfiguration().getParticipant();
		ModelProvider[] providers = participant.getEnabledModelProviders();
		Set result = new HashSet();
		for (int i = 0; i < providers.length; i++) {
			ModelProvider provider = providers[i];
			ITeamContentProviderDescriptor desc = TeamUI.getTeamContentProviderManager().getDescriptor(provider.getId());
			if (desc != null && desc.isEnabled())
				result.add(desc);
		}
		return (ITeamContentProviderDescriptor[]) result.toArray(new ITeamContentProviderDescriptor[result.size()]);
	}

	private Composite getInitializationPane(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(getBackgroundColor());
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);	

		createDescriptionLabel(composite, NLS.bind(TeamUIMessages.DiffTreeChangesSection_3, new String[] { getConfiguration().getParticipant().getName() })); 

		Hyperlink link = getForms().createHyperlink(composite, TeamUIMessages.DiffTreeChangesSection_4, SWT.WRAP); 
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				getHandler().initializeIfNeeded();
			}
		});
		getForms().getHyperlinkGroup().add(link);
		
		link = getForms().createHyperlink(composite, TeamUIMessages.DiffTreeChangesSection_5, SWT.WRAP); 
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				getConfiguration().getParticipant().run(getConfiguration().getSite().getPart());
			}
		});
		getForms().getHyperlinkGroup().add(link);
		
		return composite;
	}

	private Composite getInitializingMessagePane(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(getBackgroundColor());
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);
		if (isRefreshRunning()) {
			createDescriptionLabel(composite,NLS.bind(TeamUIMessages.DiffTreeChangesSection_6, new String[] { getConfiguration().getParticipant().getName() }));
		} else {
			createDescriptionLabel(composite,NLS.bind(TeamUIMessages.DiffTreeChangesSection_7, new String[] { getConfiguration().getParticipant().getName() }));
		}
		return composite;
	}

	private boolean isRefreshRunning() {
		return Platform.getJobManager().find(getConfiguration().getParticipant()).length > 0;
	}

	private SubscriberDiffTreeEventHandler getHandler() {
		return (SubscriberDiffTreeEventHandler)Utils.getAdapter(context, SubscriberDiffTreeEventHandler.class);
	}

	private Composite getPointerToModel(Composite parent, final ModelProvider provider, String oldId) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(getBackgroundColor());
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);

		IModelProviderDescriptor oldDesc = ModelProvider.getModelProviderDescriptor(oldId);
		String message;
		String modeToString = Utils.modeToString(getConfiguration().getMode());
        message = NLS.bind(TeamUIMessages.DiffTreeChangesSection_0, new String[] {
            		provider.getDescriptor().getLabel(), 
            		modeToString });
		message = NLS.bind(TeamUIMessages.DiffTreeChangesSection_1, new String[] { modeToString, oldDesc.getLabel(), message });
		
		createDescriptionLabel(composite, message);
		
		Label warning = new Label(composite, SWT.NONE);
		warning.setImage(TeamUIPlugin.getPlugin().getImage(ISharedImages.IMG_WARNING_OVR));
		
		Hyperlink link = getForms().createHyperlink(composite, NLS.bind(TeamUIMessages.DiffTreeChangesSection_2, new String[] { provider.getDescriptor().getLabel() }), SWT.WRAP); 
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				getConfiguration().setProperty(ModelSynchronizeParticipant.P_VISIBLE_MODEL_PROVIDER, provider.getDescriptor().getId() );
			}
		});
		getForms().getHyperlinkGroup().add(link);
		
		new Label(composite, SWT.NONE);
		Hyperlink link2 = getForms().createHyperlink(composite, TeamUIMessages.DiffTreeChangesSection_13, SWT.WRAP); 
		link2.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				getConfiguration().setProperty(ModelSynchronizeParticipant.P_VISIBLE_MODEL_PROVIDER, ModelSynchronizeParticipant.ALL_MODEL_PROVIDERS_VISIBLE );
			}
		});
		getForms().getHyperlinkGroup().add(link2);
		
		return composite;
	}

	public void treeEmpty(TreeViewer viewer) {
		handleEmptyViewer();
	}

	private void handleEmptyViewer() {
		// Override stand behavior to do our best to show something
		TeamUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!getContainer().isDisposed())
					updatePage(getEmptyChangesComposite(getContainer()));
			}
		});
	}
	
	protected void calculateDescription() {
		if (errors != null && errors.length > 0) {
			if (!showingError) {
				TeamUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
					public void run() {
						updatePage(getErrorComposite(getContainer()));
						showingError = true;
					}
				});
			}
			return;
		}
		showingError = false;
		if (isViewerEmpty()) {
			handleEmptyViewer();
		} else {
			super.calculateDescription();
		}
	}

	private boolean isViewerEmpty() {
		Viewer v = getPage().getViewer();
		if (v instanceof CommonViewerAdvisor.NavigableCommonViewer) {
			CommonViewerAdvisor.NavigableCommonViewer cv = (CommonViewerAdvisor.NavigableCommonViewer) v;
			return cv.isEmpty();
		}
		return false;
	}

	public void notEmpty(TreeViewer viewer) {
		calculateDescription();
	}
	
	private Composite getErrorComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(getBackgroundColor());
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);	

		createDescriptionLabel(composite, NLS.bind(TeamUIMessages.ChangesSection_10, new String[] { getConfiguration().getParticipant().getName() })); 

		Hyperlink link = getForms().createHyperlink(composite, TeamUIMessages.ChangesSection_8, SWT.WRAP); 
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				showErrors();
			}
		});
		getForms().getHyperlinkGroup().add(link);
		
		link = getForms().createHyperlink(composite, TeamUIMessages.ChangesSection_9, SWT.WRAP); 
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				errors = null;
				calculateDescription();
				SubscriberDiffTreeEventHandler handler = getHandler();
				if (handler != null)
					handler.initializeIfNeeded();
				else 
					getConfiguration().getParticipant().run(getConfiguration().getSite().getPart());
			}
		});
		getForms().getHyperlinkGroup().add(link);
		
		return composite;
	}

	/* private */ void showErrors() {
		if (errors != null) {
			IStatus[] status = errors;
			String title = TeamUIMessages.ChangesSection_11; 
			if (status.length == 1) {
				ErrorDialog.openError(getShell(), title, status[0].getMessage(), status[0]);
			} else {
				MultiStatus multi = new MultiStatus(TeamUIPlugin.ID, 0, status, TeamUIMessages.ChangesSection_12, null); 
				ErrorDialog.openError(getShell(), title, null, multi);
			}
		}
	}
}
