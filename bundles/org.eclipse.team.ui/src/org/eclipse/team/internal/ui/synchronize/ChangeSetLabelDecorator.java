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
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontDecorator;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.subscribers.ActiveChangeSet;
import org.eclipse.team.core.subscribers.ChangeSet;
import org.eclipse.team.core.subscribers.SubscriberChangeSetCollector;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Label decorator that decorates the default active change set.
 */
public class ChangeSetLabelDecorator extends LabelProvider implements ILabelDecorator, IFontDecorator{

	private Font boldFont;
	private SubscriberChangeSetCollector collector;

    public ChangeSetLabelDecorator(ISynchronizePageConfiguration configuration) {
        this.collector = configuration.getParticipant().getChangeSetCapability().getActiveChangeSetManager();
    }
    
    public String decorateText(String input, Object element) {
		String text = input;
		if (element instanceof ChangeSetDiffNode) {
		    ChangeSet set = ((ChangeSetDiffNode)element).getSet();
		    if (set instanceof ActiveChangeSet && isDefaultActiveSet((ActiveChangeSet)set)) {
		        text = Policy.bind("CommitSetDiffNode.0", text); //$NON-NLS-1$
		    }
		}
		return text;
	}

	public void dispose() {
		if(boldFont != null) {
			boldFont.dispose();
		}
	}

	public Font decorateFont(Object element) {
		if (element instanceof ChangeSetDiffNode) {
		    ChangeSet set = ((ChangeSetDiffNode)element).getSet();
		    if (set instanceof ActiveChangeSet && isDefaultActiveSet((ActiveChangeSet)set)) {
		    	if (boldFont == null) {
					Font defaultFont = JFaceResources.getDefaultFont();
					FontData[] data = defaultFont.getFontData();
					for (int i = 0; i < data.length; i++) {
						data[i].setStyle(SWT.BOLD);
					}				
					boldFont = new Font(TeamUIPlugin.getStandardDisplay(), data);
				}
				return boldFont;
		    }
		}
		return null;
	}

	private boolean isDefaultActiveSet(ActiveChangeSet set) {
	    return collector.isDefault(set);
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
     */
    public Image decorateImage(Image image, Object element) {
        return image;
    }
	
}
