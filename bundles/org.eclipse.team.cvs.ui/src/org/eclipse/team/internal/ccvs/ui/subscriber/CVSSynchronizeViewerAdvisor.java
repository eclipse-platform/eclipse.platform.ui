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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.internal.ccvs.ui.CVSLightweightDecorator;
import org.eclipse.team.ui.synchronize.ISynchronizeView;
import org.eclipse.team.ui.synchronize.subscriber.SubscriberParticipant;
import org.eclipse.team.ui.synchronize.subscriber.SynchronizeViewerAdvisor;
import org.eclipse.team.ui.synchronize.viewers.SynchronizeModelElement;
import org.eclipse.team.ui.synchronize.viewers.SynchronizeModelElementLabelProvider;

public class CVSSynchronizeViewerAdvisor extends SynchronizeViewerAdvisor {

	private boolean isGroupIncomingByComment = false;

	private static class CVSLabelDecorator extends LabelProvider implements ILabelDecorator  {
		public String decorateText(String input, Object element) {
			String text = input;
			if (element instanceof SynchronizeModelElement) {
				IResource resource =  ((SynchronizeModelElement)element).getResource();
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
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.TreeViewerAdvisor#getLabelProvider()
	 */
	protected ILabelProvider getLabelProvider() {
		return new SynchronizeModelElementLabelProvider.DecoratingColorLabelProvider(new SynchronizeModelElementLabelProvider(), new CVSLabelDecorator());
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
		super.propertyChange(event);
	}
}
