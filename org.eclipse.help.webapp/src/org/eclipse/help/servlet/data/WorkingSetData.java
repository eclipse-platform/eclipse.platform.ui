package org.eclipse.help.servlet.data;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.workingset.*;

/**
 * This class manages help working sets
 */
public class WorkingSetData extends RequestData {

	private WorkingSetManager wsmgr =
		HelpSystem.getWorkingSetManager(getLocale());
		
	private AdaptableToc[] tocs;

	public WorkingSetData(ServletContext context, HttpServletRequest request) {
		super(context, request);
		AdaptableTocs adaptableTocs = HelpSystem.getWorkingSetManager(getLocale()).getRoot();
		tocs = (AdaptableToc[])adaptableTocs.getChildren();
	}


	public String getWorkingSetName() {
		String name = request.getParameter("workingSet");
		if (name == null )
			name = "";
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
	 * Returns true if the specified toc is included in the working set.
	 * @param href
	 * @return boolean
	 */
	public boolean isTocIncluded(int toc) {
		WorkingSet ws = getWorkingSet();
		if (ws == null)
			return false;
		if (toc <0 || toc >= tocs.length)
			return false;
		AdaptableToc adaptableToc = tocs[toc];
		AdaptableHelpResource[] elements = ws.getElements();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] == adaptableToc)
				return true;
		}
		return false;
	}

	public boolean isTopicIncluded(int toc, int topic) {
		WorkingSet ws = getWorkingSet();
		if (ws == null)
			return false;
		if (toc <0 || toc >= tocs.length)
			return false;
			
		AdaptableToc parent = tocs[toc];
		AdaptableTopic[] topics = (AdaptableTopic[])parent.getChildren();
		if (topic < 0 || topic >= topics.length)
			return false;
		AdaptableTopic adaptableTopic = topics[topic];
		AdaptableHelpResource[] elements = ws.getElements();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] == adaptableTopic)
				return true;
		}
		return false;
	}

	public String getOperation() {
		return request.getParameter("operation");
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
