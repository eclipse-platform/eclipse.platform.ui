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

package org.eclipse.help.ui.internal;

import java.util.*;

import org.eclipse.help.internal.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.model.*;
import org.eclipse.ui.*;
import org.eclipse.ui.activities.*;

/**
 * Wrapper for eclipse ui activity support
 */
public class HelpActivitySupport implements IHelpActivitySupport {
	private IWorkbenchActivitySupport activitySupport;
	
	public HelpActivitySupport(IWorkbench workbench) {
		activitySupport = (IWorkbenchActivitySupport) workbench.getActivitySupport();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.base.IHelpActivitySupport#isEnabled()
	 */
	public boolean isEnabled(String href) {
		if (href.startsWith("/")){
			href = href.substring(1);
		}
        
        return activitySupport.getActivityManager().getIdentifier(href).isEnabled();
	}

	/**
	 * Checks whether topic belongs to a TOC that mathes
	 * enabled activity.  Enabled children TOCs are searched if linked by
	 * also enabled TOCs.
	 * Additionally topic may match description topic of a root TOC.
	 * @return true if topic belongs to an enabled TOC
	 * @param href
	 * @param locale locale for which TOCs are checked
	 */
	public boolean isEnabledTopic(String href, String locale){
		if (href == null) {
			return false;
		}
		int ix = href.indexOf("?resultof=");
		if (ix >= 0) {
			href = href.substring(0, ix);
		}
		// Find out if description topic for enabled top level TOCs matches the
		// topic
		ITocElement[] tocs = HelpPlugin.getTocManager().getTocs(locale);
		for (int t = 0; t < tocs.length; t++) {
			String descriptionHref = tocs[t].getTocTopicHref();
			if (descriptionHref != null && descriptionHref.length()>0
					&& descriptionHref.equals(href)
					&& HelpBasePlugin.getActivitySupport().isEnabled(
							tocs[t].getHref())) {
				return true;
			}
		}
		// Find out if any contributed toc that is enabled contains the topic
		return isInTocSubtree(href, Arrays.asList(tocs));
	}
	/**
	 * @param href
	 *            href of a topic
	 * @param tocList
	 *            List of ITocElement
	 * @return true if given topic belongs to one of enabled ITocElements or
	 *         their children
	 */
	private boolean isInTocSubtree(String href, List tocList) {
		for (Iterator it = tocList.iterator(); it.hasNext();) {
			ITocElement toc = (ITocElement) it.next();
			if (!HelpBasePlugin.getActivitySupport().isEnabled(toc.getHref())) {
				// TOC is not enabled, check other TOCs
				continue;
			}
			// Check topics in navigation
			if (toc.getOwnedTopic(href) != null) {
				return true;
			}
			// Check extra dir
			if (toc.getOwnedExtraTopic(href)!=null){
				return true;
			}
			// check children TOCs
			if (isInTocSubtree(href, toc.getChildrenTocs())) {
				return true;
			} else {
				// try other TOCs at this level
			}
		}
		return false;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.base.IHelpActivitySupport#enableActivities(java.lang.String)
	 */
	public void enableActivities(String href) {
		if (href.startsWith("/")){
			href = href.substring(1);
		}
     
        IIdentifier identifier = activitySupport.getActivityManager().getIdentifier(href);
        Set activitityIds = identifier.getActivityIds();
        if (activitityIds.isEmpty()) { // if there are no activities that match this identifier, do nothing.
            return;
        }
        
        Set enabledIds = new HashSet(activitySupport.getActivityManager().getEnabledActivityIds());
        enabledIds.addAll(activitityIds);
        activitySupport.setEnabledActivityIds(enabledIds);
	}

}
