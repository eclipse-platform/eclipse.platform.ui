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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.internal.ccvs.ui.CVSLightweightDecorator;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ui.synchronize.ActionDelegateManager;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.team.ui.synchronize.subscribers.SubscriberParticipant;
import org.eclipse.team.ui.synchronize.subscribers.SynchronizeViewerAdvisor;

public class CVSSynchronizeViewerAdvisor extends SynchronizeViewerAdvisor implements ISynchronizeModelChangeListener {

	private boolean isGroupIncomingByComment = false;
	
	private List delegates = new ArrayList(2);
	private CVSSynchronizeViewerAdvisor config;
	private Action groupByComment;
	private ActionDelegateManager delegateManager;

	private static class CVSLabelDecorator extends LabelProvider implements ILabelDecorator  {
		public String decorateText(String input, Object element) {
			String text = input;
			if (element instanceof ISynchronizeModelElement) {
				IResource resource =  ((ISynchronizeModelElement)element).getResource();
				if(resource != null && resource.getType() != IResource.ROOT) {
					CVSLightweightDecorator.Decoration decoration = new CVSLightweightDecorator.Decoration();
					CVSLightweightDecorator.decorateTextLabel(resource, decoration, false, true);
					StringBuffer output = new StringBuffer(25);
					if(decoration.prefix != null) {
						output.append(decoration.prefix);
					}
					output.append(text);
					if(decoration.suffix != null) {
						output.append(decoration.suffix);
					}
					return output.toString();
				}
			}
			return text;
		}
		public Image decorateImage(Image base, Object element) {
			return base;
		}
	}
	
	public CVSSynchronizeViewerAdvisor(ISynchronizeView view, SubscriberParticipant participant) {
		super(view, participant);
		participant.addPropertyChangeListener(this);
		
		// Sync changes are used to update the action state for the update/commit buttons.
		addInputChangedListener(this);
		
		// Listen for decorator changed to refresh the viewer's labels.
		CVSUIPlugin.addPropertyChangeListener(this);
		this.delegateManager = new ActionDelegateManager();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.TreeViewerAdvisor#getLabelProvider()
	 */
	protected ILabelProvider getLabelProvider() {
		ILabelProvider oldProvider = super.getLabelProvider();
		return new DecoratingColorLabelProvider(oldProvider, new CVSLabelDecorator());
	}
	
	public boolean isGroupIncomingByComment() {
		return isGroupIncomingByComment;
	}
	
	public void setGroupIncomingByComment(boolean enabled) {
		this.isGroupIncomingByComment = enabled;
		if(getParticipant().getMode() == SubscriberParticipant.INCOMING_MODE) {
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.TreeViewerAdvisor#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if(property.equals(SubscriberParticipant.P_SYNCVIEWPAGE_MODE) && isGroupIncomingByComment()) {
			int oldMode = ((Integer)event.getOldValue()).intValue();
			int newMode = ((Integer)event.getNewValue()).intValue();
			if(newMode == SubscriberParticipant.INCOMING_MODE || 
			   oldMode == SubscriberParticipant.INCOMING_MODE) {
				aSyncExec(new Runnable() {
					public void run() {
					}
				});				
			}
		}
		if(property.equals(CVSUIPlugin.P_DECORATORS_CHANGED) && getViewer() != null && getSyncInfoSet() != null) {
			((StructuredViewer)getViewer()).refresh(true /* update labels */);
		}
		super.propertyChange(event);
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.ui.sync.AbstractSynchronizeParticipant#dispose()
	 */
	public void dispose() {
		super.dispose();
		removeInputChangedListener(this);
		CVSUIPlugin.removePropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.presentation.ISynchronizeModelChangeListener#inputChanged(org.eclipse.team.ui.synchronize.presentation.SynchronizeModelProvider)
	 */
	public void modelChanged(ISynchronizeModelElement root) {
		delegateManager.updateActionEnablement(root);
	}
	
	protected ActionDelegateManager getDelegateManager() {
		return delegateManager;
	}
}
