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

import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.*;
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
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.mapping.*;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class DiffTreeChangesSection extends ForwardingChangesSection implements IDiffChangeListener, IPropertyChangeListener, IEmptyTreeListener {

	private ISynchronizationContext context;

	public DiffTreeChangesSection(Composite parent, AbstractSynchronizePage page, ISynchronizePageConfiguration configuration) {
		super(parent, page, configuration);
		context = (ISynchronizationContext)configuration.getProperty(ITeamContentProviderManager.P_SYNCHRONIZATION_CONTEXT);
		context.getDiffTree().addDiffChangeListener(this);
		getConfiguration().addPropertyChangeListener(this);
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
		ResourceTraversal[] traversals = context.getScope().getTraversals(id);
		return context.getDiffTree().hasMatchingDiffs(traversals, FastDiffFilter.getStateFilter(states, mask));
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
		calculateDescription();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.diff.IDiffChangeListener#propertyChanged(int, org.eclipse.core.runtime.IPath[])
	 */
	public void propertyChanged(int property, IPath[] paths) {
		// Do nothing
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(ISynchronizePageConfiguration.P_MODE)
				|| event.getProperty().equals(ModelSynchronizeParticipant.P_VISIBLE_MODEL_PROVIDER)) {
			calculateDescription();
		}
	}
	
	protected Composite getEmptyChangesComposite(Composite parent) {
		ISynchronizePageConfiguration configuration = getConfiguration();
		String id = (String)configuration.getProperty(ModelSynchronizeParticipant.P_VISIBLE_MODEL_PROVIDER);
		if (id != null && !id.equals(ModelSynchronizeParticipant.ALL_MODEL_PROVIDERS_VISIBLE)) {
			// A particular model is active so we need to look for a model that has changes in this
			// same mode before offering to change modes.
			ModelProvider[] providers =context.getScope().getModelProviders();
			providers = ModelOperation.sortByExtension(providers);
			for (int i = 0; i < providers.length; i++) {
				ModelProvider provider = providers[i];
				ISynchronizationCompareAdapter adapter = Utils.getCompareAdapter(provider);
				if (adapter != null) {
					boolean hasChanges = hasChangesInMode(provider.getId(), adapter, getConfiguration().getMode());
					if (hasChanges && !provider.getDescriptor().getId().equals(id)) {
						return getPointerToModel(parent, provider, id);
					}
				}
			}
			// TODO: More to do but we'll do it later
		}
		return super.getEmptyChangesComposite(parent);
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
		
		Label warning = new Label(composite, SWT.NONE);
		warning.setImage(TeamUIPlugin.getPlugin().getImage(ISharedImages.IMG_WARNING_OVR));
		
		Hyperlink link = getForms().createHyperlink(composite, NLS.bind(TeamUIMessages.DiffTreeChangesSection_2, new String[] { provider.getDescriptor().getLabel() }), SWT.WRAP); 
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				//getConfiguration().setProperty(ISynchronizationConstants.P_ACTIVE_MODEL_PROVIDER, provider.getDescriptor().getId() );
				new ShowModelProviderAction(getConfiguration(), provider).run();
			}
		});
		getForms().getHyperlinkGroup().add(link);
		createDescriptionLabel(composite, message);
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

}
