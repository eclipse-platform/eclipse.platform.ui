/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.subscriber;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.ActionDelegateWrapper;
import org.eclipse.team.internal.ui.synchronize.SynchronizePageConfiguration;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.*;

public class MergeSynchronizeParticipant extends CVSParticipant {
	
	/**
	 * The id of a workspace action group to which additions actions can 
	 * be added.
	 */
	public static final String ACTION_GROUP = "cvs_merge_actions"; //$NON-NLS-1$
	
	
	private final static String CTX_ROOT = "root"; //$NON-NLS-1$
	private final static String CTX_ROOT_PATH = "root_resource"; //$NON-NLS-1$
	private final static String CTX_START_TAG = "start_tag"; //$NON-NLS-1$
	private final static String CTX_START_TAG_TYPE = "start_tag_type"; //$NON-NLS-1$
	private final static String CTX_END_TAG = "end_tag"; //$NON-NLS-1$
	private final static String CTX_END_TAG_TYPE = "end_tag_type"; //$NON-NLS-1$
	
	/**
	 * Actions for the merge particpant's toolbar
	 */
	public class MergeParticipantActionContribution extends SynchronizePageActionGroup {
		private ActionDelegateWrapper updateAdapter;
		public void initialize(ISynchronizePageConfiguration configuration) {
			super.initialize(configuration);
			MergeUpdateAction action = new MergeUpdateAction();
			action.setPromptBeforeUpdate(true);
			updateAdapter = new ActionDelegateWrapper(action, configuration.getSite().getPart());
			Utils.initAction(updateAdapter, "action.SynchronizeViewUpdate.", Policy.getBundle()); //$NON-NLS-1$
			super.initialize(configuration);
		}
		public void modelChanged(ISynchronizeModelElement input) {
			if (updateAdapter == null) return;
			updateAdapter.setSelection(input);
		}
		public void fillActionBars(IActionBars actionBars) {
			IToolBarManager toolbar = actionBars.getToolBarManager();
			appendToGroup(toolbar, ACTION_GROUP, updateAdapter);
			super.fillActionBars(actionBars);
		}
	}
	
	public MergeSynchronizeParticipant() {
		super();
	}
	
	public MergeSynchronizeParticipant(CVSMergeSubscriber subscriber) {
		setSubscriber(subscriber);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.sync.SubscriberParticipant#setSubscriber(org.eclipse.team.core.subscribers.TeamSubscriber)
	 */
	public  void setSubscriber(Subscriber subscriber) {
		super.setSubscriber(subscriber);
		try {
			ISynchronizeParticipantDescriptor descriptor = TeamUI.getSynchronizeManager().getParticipantDescriptor(CVSMergeSubscriber.ID); 
			setInitializationData(descriptor);
			CVSMergeSubscriber s = (CVSMergeSubscriber)getSubscriber();
			setSecondaryId(s.getId().getLocalName());
		} catch (CoreException e) {
			CVSUIPlugin.log(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#init(org.eclipse.ui.IMemento)
	 */
	public void init(String secondayId, IMemento memento) throws PartInitException {
		super.init(secondayId, memento);
		if(memento != null) {
			ISynchronizeParticipantDescriptor descriptor = TeamUI.getSynchronizeManager().getParticipantDescriptor(CVSMergeSubscriber.ID); 
			String qualifier = descriptor.getId();
			String localname = secondayId;
			if(qualifier == null || localname == null) {
				throw new PartInitException(Policy.bind("MergeSynchronizeParticipant.8")); //$NON-NLS-1$
			}
			try {
				setSubscriber(read(new QualifiedName(qualifier, localname), memento));
			} catch (CVSException e) {
				throw new PartInitException(Policy.bind("MergeSynchronizeParticipant.9"), e); //$NON-NLS-1$
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#init(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		super.saveState(memento);
		CVSMergeSubscriber s = (CVSMergeSubscriber)getSubscriber();
		write(s, memento);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.sync.AbstractSynchronizeParticipant#dispose()
	 */
	public void dispose() {
		super.dispose();
		if(TeamUI.getSynchronizeManager().get(getId(), getSecondaryId()) == null) {
			// If the participant isn't managed by the synchronize manager then we
			// must ensure that the state cached in the synchronizer is flushed.
			flushStateCache();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#getName()
	 */
	public String getName() {		
		return ((CVSMergeSubscriber)getSubscriber()).getName();
	}
	
	private void write(CVSMergeSubscriber s, IMemento memento) {
		// start and end tags
		CVSTag start = s.getStartTag();
		CVSTag end = s.getEndTag();
		memento.putString(CTX_START_TAG, start.getName());
		memento.putInteger(CTX_START_TAG_TYPE, start.getType());
		memento.putString(CTX_END_TAG, end.getName());
		memento.putInteger(CTX_END_TAG_TYPE, end.getType());
		
		// resource roots
		IResource[] roots = s.roots();
		for (int i = 0; i < roots.length; i++) {
			IResource resource = roots[i];
			IMemento rootNode = memento.createChild(CTX_ROOT);
			rootNode.putString(CTX_ROOT_PATH, resource.getFullPath().toString());
		}
	}
	
	private CVSMergeSubscriber read(QualifiedName id, IMemento memento) throws CVSException {
		CVSTag start = new CVSTag(memento.getString(CTX_START_TAG), memento.getInteger(CTX_START_TAG_TYPE).intValue()); //$NON-NLS-1$ //$NON-NLS-2$
		CVSTag end = new CVSTag(memento.getString(CTX_END_TAG), memento.getInteger(CTX_END_TAG_TYPE).intValue()); //$NON-NLS-1$ //$NON-NLS-2$
		
		IMemento[] rootNodes = memento.getChildren(CTX_ROOT);
		if(rootNodes == null || rootNodes.length == 0) {
			throw new CVSException(Policy.bind("MergeSynchronizeParticipant.10", id.toString())); //$NON-NLS-1$
		}
		
		List resources = new ArrayList();
		for (int i = 0; i < rootNodes.length; i++) {
			IMemento rootNode = rootNodes[i];
			IPath path = new Path(rootNode.getString(CTX_ROOT_PATH)); //$NON-NLS-1$
			IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path, true /* include phantoms */);
			if(resource != null) {
				resources.add(resource);
			} else {
				// log that a resource previously in the merge set is no longer in the workspace
				CVSProviderPlugin.log(CVSStatus.INFO, Policy.bind("MergeSynchronizeParticipant.11", path.toString()), null); //$NON-NLS-1$
			}
		}
		if(resources.isEmpty()) {
			throw new CVSException(Policy.bind("MergeSynchronizeParticipant.12", id.toString())); //$NON-NLS-1$
		}
		IResource[] roots = (IResource[]) resources.toArray(new IResource[resources.size()]);
		return new CVSMergeSubscriber(id, roots, start, end);
	}
	
	private void flushStateCache() {
		((CVSMergeSubscriber)getSubscriber()).cancel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscribers.SubscriberParticipant#initializeConfiguration(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	protected void initializeConfiguration(ISynchronizePageConfiguration configuration) {
		super.initializeConfiguration(configuration);
		configuration.addMenuGroup(ISynchronizePageConfiguration.P_TOOLBAR_MENU, ACTION_GROUP);
		configuration.addMenuGroup(ISynchronizePageConfiguration.P_TOOLBAR_MENU, ISynchronizePageConfiguration.REMOVE_PARTICPANT_GROUP);
		configuration.setSupportedModes(ISynchronizePageConfiguration.INCOMING_MODE | ISynchronizePageConfiguration.CONFLICTING_MODE);
		configuration.setMode(ISynchronizePageConfiguration.INCOMING_MODE);
		configuration.addActionContribution(new MergeParticipantActionContribution());
		configuration.setProperty(SynchronizePageConfiguration.P_MODEL_MANAGER, new ChangeLogModelManager(configuration));
	}
}