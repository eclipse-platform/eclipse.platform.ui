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
package org.eclipse.help.internal.webapp.data;

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.help.internal.webapp.servlet.*;
import org.eclipse.help.internal.workingset.*;

/**
 * This class manages help working sets
 */
public class WorkingSetData extends RequestData {
	public final static short STATE_UNCHECKED = 0;
	public final static short STATE_GRAYED = 1;
	public final static short STATE_CHECKED = 2;

	private WebappWorkingSetManager wsmgr;

	private AdaptableToc[] tocs;
	private boolean isEditMode;

	public WorkingSetData(ServletContext context, HttpServletRequest request,
			HttpServletResponse response) {
		super(context, request, response);
		wsmgr = new WebappWorkingSetManager(request, response, getLocale());
		AdaptableTocsArray adaptableTocs = wsmgr.getRoot();
		tocs = (AdaptableToc[]) adaptableTocs.getChildren();
		isEditMode = "edit".equals(getOperation()); //$NON-NLS-1$
	}

	public boolean isEditMode() {
		return isEditMode;
	}

	public String getWorkingSetName() {
		String name = request.getParameter("workingSet"); //$NON-NLS-1$
		if (name == null)
			name = ""; //$NON-NLS-1$
		return name;
	}

	public WorkingSet getWorkingSet() {
		String name = getWorkingSetName();
		if (name != null && name.length() > 0)
			return wsmgr.getWorkingSet(name);
		else
			return null;
	}

	/**
	 * Returns the state of the TOC
	 * 
	 * @return boolean
	 */
	public short getTocState(int toc) {
		if (!isEditMode())
			return STATE_UNCHECKED;
		WorkingSet ws = getWorkingSet();
		if (ws == null)
			return STATE_UNCHECKED;
		if (toc < 0 || toc >= tocs.length)
			return STATE_UNCHECKED;

		// See if the toc is in the working set
		AdaptableToc adaptableToc = tocs[toc];
		AdaptableHelpResource[] elements = ws.getElements();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] == adaptableToc)
				return STATE_CHECKED;
		}

		// Check if it is grayed out
		int topics = adaptableToc.getChildren().length;
		boolean allTheSame = true;
		short baseValue = STATE_UNCHECKED;
		// base value is that of the first topic
		if (topics > 0)
			baseValue = getTopicState(toc, 0);
		for (int i = 1; allTheSame && i < topics; i++)
			allTheSame = allTheSame && (getTopicState(toc, i) == baseValue);

		if (!allTheSame)
			return STATE_GRAYED;
		else
			return STATE_UNCHECKED;
	}

	/**
	 * Returns the state of the topic. The state is not dependent on the parent
	 * toc, but only whether it was part of the working set. To get the real
	 * state, the caller must use the parent state as well. This is not done
	 * here for performance reasons. In the JSP, by the time one looks at the
	 * topic, the parent toc has already been processed.
	 * 
	 * @param toc
	 * @param topic
	 * @return short
	 */
	public short getTopicState(int toc, int topic) {
		if (!isEditMode)
			return STATE_UNCHECKED;
		WorkingSet ws = getWorkingSet();
		if (ws == null)
			return STATE_UNCHECKED;
		if (toc < 0 || toc >= tocs.length)
			return STATE_UNCHECKED;

		AdaptableToc parent = tocs[toc];
		AdaptableTopic[] topics = (AdaptableTopic[]) parent.getChildren();
		if (topic < 0 || topic >= topics.length)
			return STATE_UNCHECKED;
		AdaptableTopic adaptableTopic = topics[topic];
		AdaptableHelpResource[] elements = ws.getElements();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] == adaptableTopic)
				return STATE_CHECKED;
		}
		return STATE_UNCHECKED;
	}

	public String getOperation() {
		return request.getParameter("operation"); //$NON-NLS-1$
	}

	// Accessor methods to avoid exposing help classes directly to JSP.
	// Note: this seems ok for now, but maybe we need to reconsider this
	//       and allow help classes in JSP's.

	public int getTocCount() {
		return tocs.length;
	}

	public String getTocLabel(int i) {
		return tocs[i].getLabel();
	}

	public String getTocHref(int i) {
		return tocs[i].getHref();
	}

	public int getTopicCount(int toc) {
		return tocs[toc].getTopics().length;
	}

	public String getTopicLabel(int toc, int topic) {
		return tocs[toc].getTopics()[topic].getLabel();
	}
}
