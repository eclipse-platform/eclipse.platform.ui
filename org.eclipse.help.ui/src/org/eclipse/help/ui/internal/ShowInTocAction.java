/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal;

import java.util.StringTokenizer;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.ILiveHelpAction;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.ui.internal.views.AllTopicsPart;
import org.eclipse.help.ui.internal.views.ReusableHelpPart;

public class ShowInTocAction implements ILiveHelpAction {

	private String path;
	
	public void setInitializationString(String data) {
		path = data;
	}

	public void run() {
		final IHelpResource res = getHelpResource();
		final ReusableHelpPart helpPart = ReusableHelpPart.getLastActiveInstance();
		if (helpPart != null) {
			helpPart.getControl().getDisplay().syncExec(new Runnable() {
				public void run() {
					helpPart.showPage(IHelpUIConstants.HV_ALL_TOPICS_PAGE);
					AllTopicsPart part = (AllTopicsPart)helpPart.findPart(IHelpUIConstants.HV_TOPIC_TREE);
					if (part != null) {
						part.selectReveal(res);
					}
				}
			});
		}
	}
	
	private IHelpResource getHelpResource() {
		StringTokenizer tok = new StringTokenizer(path, "_"); //$NON-NLS-1$
		int index = Integer.parseInt(tok.nextToken());
		IToc[] tocs = HelpSystem.getTocs();
		IToc toc = tocs[index];
		if (tok.hasMoreTokens()) {
			ITopic topic = toc.getTopic(null);
			while (tok.hasMoreTokens()) {
				index = Integer.parseInt(tok.nextToken());
				topic = topic.getSubtopics()[index];
			}
			return topic;
		}
		return toc;
	}
}