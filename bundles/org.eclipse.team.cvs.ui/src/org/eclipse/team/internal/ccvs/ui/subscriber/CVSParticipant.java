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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.internal.ccvs.ui.CVSLightweightDecorator;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.team.ui.synchronize.subscribers.SubscriberParticipant;

/**
 * Superclass for all CVS particpants (workspace, merge and compare)
 */
public class CVSParticipant extends SubscriberParticipant {
	
	private static class CVSLabelDecorator extends AbstractSynchronizeLabelDecorator implements IPropertyChangeListener  {
		public CVSLabelDecorator(ISynchronizePageConfiguration configuration) {
			super(configuration);
			// Listen for decorator changed to refresh the viewer's labels.
			CVSUIPlugin.addPropertyChangeListener(this);
		}
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
		public void propertyChange(PropertyChangeEvent event) {
			String property = event.getProperty();
			if(property.equals(CVSUIPlugin.P_DECORATORS_CHANGED)) {
				refreshLabels();
			}
		}
		public void dispose() {
			CVSUIPlugin.removePropertyChangeListener(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscribers.SubscriberParticipant#initializeConfiguration(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	protected void initializeConfiguration(ISynchronizePageConfiguration configuration) {
		super.initializeConfiguration(configuration);
		// The decorator adds itself to the configuration
		new CVSLabelDecorator(configuration);
	}
}
