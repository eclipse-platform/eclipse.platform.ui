/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.subscriber;

import java.util.Arrays;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.subscribers.SubscriberChangeSetCollector;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ccvs.core.CVSCompareSubscriber;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.actions.ShowAnnotationAction;
import org.eclipse.team.internal.ccvs.ui.actions.ShowResourceInHistoryAction;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;

public class CompareParticipant extends CVSParticipant implements IPropertyChangeListener {
	
	public static final String CONTEXT_MENU_CONTRIBUTION_GROUP = "context_group_1"; //$NON-NLS-1$
	public static final String NON_MODAL_CONTEXT_MENU_CONTRIBUTION_GROUP = "context_group_2"; //$NON-NLS-1$

	/**
	 * Actions for the compare particpant's toolbar
	 */
	public class CompareParticipantActionContribution extends SynchronizePageActionGroup {
		public void initialize(ISynchronizePageConfiguration configuration) {
			super.initialize(configuration);
			
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP,
					new CompareRevertAction(configuration));
			
			if (!configuration.getSite().isModal()) {
				appendToGroup(
						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
						NON_MODAL_CONTEXT_MENU_CONTRIBUTION_GROUP,
						new CVSActionDelegateWrapper(new ShowAnnotationAction(), configuration));
				appendToGroup(
						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
						NON_MODAL_CONTEXT_MENU_CONTRIBUTION_GROUP,
						new CVSActionDelegateWrapper(new ShowResourceInHistoryAction(), configuration));
			}
		}
	}
	
	private SyncInfoFilter contentComparison = new SyncInfoFilter() {
		private SyncInfoFilter contentCompare = new SyncInfoFilter.ContentComparisonSyncInfoFilter();
		public boolean select(SyncInfo info, IProgressMonitor monitor) {
			// Want to select infos whose contents do not match
			return !contentCompare.select(info, monitor);
		}
	};
	
	public CompareParticipant(CVSCompareSubscriber subscriber) {
	    setSubscriber(subscriber);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscriber.SubscriberParticipant#setSubscriber(org.eclipse.team.core.subscribers.Subscriber)
	 */
	protected void setSubscriber(Subscriber subscriber) {
		super.setSubscriber(subscriber);
		if (CVSUIPlugin.getPlugin().getPluginPreferences().getBoolean(ICVSUIConstants.PREF_CONSIDER_CONTENTS)) {
			setSyncInfoFilter(contentComparison);
		}
		try {
			ISynchronizeParticipantDescriptor descriptor = TeamUI.getSynchronizeManager().getParticipantDescriptor(CVSCompareSubscriber.ID);
			setInitializationData(descriptor);
			CVSCompareSubscriber s = getCVSCompareSubscriber();
			setSecondaryId(s.getId().getLocalName());
		} catch (CoreException e) {
			CVSUIPlugin.log(e);
		}
		CVSUIPlugin.getPlugin().getPluginPreferences().addPropertyChangeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeParticipant#getName()
	 */
	public String getName() {
		return Policy.bind("CompareParticipant.0", getSubscriber().getName(), Utils.convertSelection(getSubscriber().roots())); //$NON-NLS-1$
	}
	
	/*
	 * Returns a merge participant that exist and is configured with the given set of resources, start, and end tags.
	 */
	public static CompareParticipant getMatchingParticipant(IResource[] resources, CVSTag tag) {
		ISynchronizeParticipantReference[] refs = TeamUI.getSynchronizeManager().getSynchronizeParticipants();
		for (int i = 0; i < refs.length; i++) {
			ISynchronizeParticipantReference reference = refs[i];
			if (reference.getId().equals(CVSCompareSubscriber.ID)) {
				try {
				    CompareParticipant p = (CompareParticipant) reference.getParticipant();
					if (p.matches(resources, tag)) {
					    return p;
					}
				} catch (TeamException e) {
					continue;
				}
			}
		}
		return null;
	}
	
	/**
	 * Return whether this compare subscriber matches persisly the 
	 * provided list of resources and the single tag.
	 * @param resources the resources
	 * @param tag the tag
	 * @return whether this compare subscriber matches persisly the 
	 * provided list of resources and the single tag
	 */
	protected boolean matches(IResource[] resources, CVSTag tag) {
		CVSTag existingTag = getCVSCompareSubscriber().getTag();
		// The tag can be null if the compare participant has a different tag for each root
		if (existingTag != null) {
			IResource[] roots = getResources();
			Arrays.sort(resources, Utils.resourceComparator);
			Arrays.sort(roots, Utils.resourceComparator);
			if (Arrays.equals(resources, roots) && existingTag.equals(tag)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the subscriber as an instance of CVSCompareSubscriber.
     * @return the subscriber as an instance of CVSCompareSubscriber
     */
    public CVSCompareSubscriber getCVSCompareSubscriber() {
        return (CVSCompareSubscriber)getSubscriber();
    }

    /* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscribers.SubscriberParticipant#initializeConfiguration(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	protected void initializeConfiguration(ISynchronizePageConfiguration configuration) {
		super.initializeConfiguration(configuration);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				CONTEXT_MENU_CONTRIBUTION_GROUP);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				NON_MODAL_CONTEXT_MENU_CONTRIBUTION_GROUP);
		configuration.addActionContribution(new CompareParticipantActionContribution());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SubscriberParticipant#dispose()
	 */
	public void dispose() {
		super.dispose();
		CVSUIPlugin.getPlugin().getPluginPreferences().removePropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(ICVSUIConstants.PREF_CONSIDER_CONTENTS)) {
			if (CVSUIPlugin.getPlugin().getPluginPreferences().getBoolean(ICVSUIConstants.PREF_CONSIDER_CONTENTS)) {
				setSyncInfoFilter(contentComparison);
			} else {
				setSyncInfoFilter(new FastSyncInfoFilter());
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SubscriberParticipant#getLongTaskName()
	 */
	protected String getLongTaskName() {
		return getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SubscriberParticipant#getShortTaskName()
	 */
	protected String getShortTaskName() {
		return Policy.bind("Participant.comparing"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.subscriber.CVSParticipant#createChangeSetCapability()
     */
    protected CVSChangeSetCapability createChangeSetCapability() {
        return new CVSChangeSetCapability() {
            public SubscriberChangeSetCollector getActiveChangeSetManager() {
                return CVSUIPlugin.getPlugin().getChangeSetManager();
            }
            /* (non-Javadoc)
             * @see org.eclipse.team.ui.synchronize.ChangeSetCapability#enableActiveChangeSetsFor(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
             */
            public boolean enableActiveChangeSetsFor(ISynchronizePageConfiguration configuration) {
                return super.enableActiveChangeSetsFor(configuration) ||
                	configuration.getComparisonType() == ISynchronizePageConfiguration.TWO_WAY;
            }
        };
    }
}
