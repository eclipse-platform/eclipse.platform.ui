/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.localhistory;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;

public class LocalHistoryParticipant extends SubscriberParticipant {
	
	public static final String ID = "org.eclipse.team.synchronize.example"; //$NON-NLS-1$
	public static final String CONTEXT_MENU_CONTRIBUTION_GROUP = "context_group_1"; //$NON-NLS-1$
	
	private class LocalHistoryActionContribution extends SynchronizePageActionGroup {
		public void initialize(ISynchronizePageConfiguration configuration) {
			super.initialize(configuration);
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, CONTEXT_MENU_CONTRIBUTION_GROUP, 
					new SynchronizeModelAction("Revert to latest in local history", configuration) { //$NON-NLS-1$
						protected SynchronizeModelOperation getSubscriberOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
							return new RevertAllOperation(configuration, elements);
						}
					});
		}
	}
	
	private class LocalHistoryDecorator extends LabelProvider implements ILabelDecorator {
		public String decorateText(String text, Object element) {
			if(element instanceof ISynchronizeModelElement) {
				ISynchronizeModelElement node = (ISynchronizeModelElement)element;
				if(node instanceof IAdaptable) {
					SyncInfo info = (SyncInfo)((IAdaptable)node).getAdapter(SyncInfo.class);
					if(info != null) {
						LocalHistoryVariant state = (LocalHistoryVariant)info.getRemote();
						return text+ " ("+ state.getContentIdentifier() + ")";
					}
				}
			}
			return text;
		}
		
		public Image decorateImage(Image image, Object element) {
			return null;
		}
	}
	
	public LocalHistoryParticipant() {
		setSubscriber(new LocalHistorySubscriber());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscriber.SubscriberParticipant#setSubscriber(org.eclipse.team.core.subscribers.Subscriber)
	 */
	protected void setSubscriber(Subscriber subscriber) {
		super.setSubscriber(subscriber);
		try {
			ISynchronizeParticipantDescriptor descriptor = TeamUI.getSynchronizeManager().getParticipantDescriptor(ID);
			setInitializationData(descriptor);
			setSecondaryId(Long.toString(System.currentTimeMillis()));
		} catch (CoreException e) {
		}
	}
	
	protected void initializeConfiguration(ISynchronizePageConfiguration configuration) {
		super.initializeConfiguration(configuration);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				CONTEXT_MENU_CONTRIBUTION_GROUP);
		configuration.addActionContribution(new LocalHistoryActionContribution());
		configuration.addLabelDecorator(new LocalHistoryDecorator());	
	}
}
