/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.operations;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.internal.ui.mapping.CommonViewerAdvisor;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.ui.mapping.ICompareAdapter;
import org.eclipse.team.ui.mapping.ISynchronizationContext;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.IContributorResourceAdapter;
import org.eclipse.ui.ide.IContributorResourceAdapter2;

/**
 * A synchronize page for displaying a {@link ModelSynchronizeParticipant}.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 **/
public class ModelSynchronizePage extends AbstractSynchronizePage {

	private ModelSynchronizeParticipant participant;

	/**
	 * Create a page from the given configuration
	 * @param configuration a page configuration
	 */
	protected ModelSynchronizePage(ISynchronizePageConfiguration configuration) {
		super(configuration);
		this.participant = (ModelSynchronizeParticipant)configuration.getParticipant();
		configuration.setComparisonType(isThreeWay() 
						? ISynchronizePageConfiguration.THREE_WAY 
						: ISynchronizePageConfiguration.TWO_WAY);
		configuration.setProperty(ISynchronizePageConfiguration.P_SYNC_INFO_SET, getParticipant().getContext().getSyncInfoTree());
		// TODO: This is a hack to get something working
		configuration.setProperty(SynchronizePageConfiguration.P_WORKING_SET_SYNC_INFO_SET, getParticipant().getContext().getSyncInfoTree());
	}

	private boolean isThreeWay() {
		return getParticipant().getContext().getType() == ISynchronizationContext.THREE_WAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizePage#reset()
	 */
	public void reset() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizePage#updateMode(int)
	 */
	protected void updateMode(int mode) {
		if (isThreeWay()) {
			CommonViewerAdvisor advisor = (CommonViewerAdvisor)getConfiguration().getProperty(SynchronizePageConfiguration.P_ADVISOR);
			advisor.setExtentionProperty(ISynchronizePageConfiguration.P_MODE, mode);
		}
	}

	/**
	 * Return the participant of this page.
	 * @return the participant of this page
	 */
	protected ModelSynchronizeParticipant getParticipant() {
		return participant;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizePage#createViewerAdvisor(org.eclipse.swt.widgets.Composite)
	 */
	protected AbstractViewerAdvisor createViewerAdvisor(Composite parent) {
		CommonViewerAdvisor commonViewerAdvisor = new CommonViewerAdvisor(parent, getConfiguration());
		updateMode(getConfiguration().getMode());
		return commonViewerAdvisor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizePage#asCompareInput(java.lang.Object)
	 */
	public ICompareInput asCompareInput(Object o) {
		if (o instanceof ICompareInput) {
			return (ICompareInput) o;
		}
		// Get a compare input from the model provider's compare adapter
		ICompareAdapter adapter = getModelProviderCompareAdapter(o);
		if (adapter != null)
			return adapter.asCompareInput(getParticipant().getContext(), o);
		return null;
	}

	private ICompareAdapter getModelProviderCompareAdapter(Object element) {
		ModelProvider provider = getModelProvider(element);
		if (provider != null) {
			Object o = provider.getAdapter(ICompareAdapter.class);
			if (o instanceof ICompareAdapter) {
				return (ICompareAdapter) o;
			}
		}
		return null;
	}

	private ModelProvider getModelProvider(Object o) {
		ResourceMapping mapping = getResourceMapping(o);
		if (mapping != null)
			return mapping.getModelProvider();
		return null;
	}
	
	private ResourceMapping getResourceMapping(Object o) {
		if (o instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) o;
			Object adapted = adaptable.getAdapter(ResourceMapping.class);
			if (adapted instanceof ResourceMapping) {
				return(ResourceMapping) adapted;
			}
			adapted = adaptable.getAdapter(IContributorResourceAdapter.class);
			if (adapted instanceof IContributorResourceAdapter2) {
				IContributorResourceAdapter2 cra = (IContributorResourceAdapter2) adapted;
				return cra.getAdaptedResourceMapping(adaptable);
			}
		} else {
			Object adapted = Platform.getAdapterManager().getAdapter(o, ResourceMapping.class);
			if (adapted instanceof ResourceMapping) {
				return(ResourceMapping) adapted;
			}
		}
		return null;
	}

	/**
	 * Return a structure viewer for viewing the structure of the given compare input
	 * @param parent the parent composite of the viewer
	 * @param oldViewer the current viewer which can be returned if it is appropriate for use with the given input
	 * @param input the compare input to be viewed
	 * @param configuration the compare configuration information
	 * @return a viewer for viewing the structure of the given compare input
	 */ 
	public Viewer findStructureViewer(Composite parent, Viewer oldViewer, ICompareInput input, CompareConfiguration configuration) {
		// Get a structure viewer from the model provider's compare adapter
		ICompareAdapter adapter = getModelProviderCompareAdapter(input);
		if (adapter != null)
			return adapter.findStructureViewer(parent, oldViewer, input, configuration);
		return null;
	}

	/**
	 * Return a viewer for comparing the content of the given compare input.
	 * @param parent the parent composite of the viewer
	 * @param oldViewer the current viewer which can be returned if it is appropriate for use with the given input
	 * @param input the compare input to be viewed
	 * @param configuration the compare configuration information
	 * @return a viewer for comparing the content of the given compare input
	 */ 
	public Viewer findContentViewer(Composite parent, Viewer oldViewer, ICompareInput input, CompareConfiguration configuration) {
		// Get a content viewer from the model provider's compare adapter
		ICompareAdapter adapter = getModelProviderCompareAdapter(input);
		if (adapter != null)
			return adapter.findContentViewer(parent, oldViewer, input, configuration);
		return null;
	}

}
