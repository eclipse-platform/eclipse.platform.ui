package org.eclipse.help.servlet.data;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.help.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.workingset.*;

/**
 * This class manages help working sets
 */
public class WorkingSetData extends RequestData {

	private WorkingSetManager wsmgr =
		HelpSystem.getWorkingSetManager(getLocale());

	public WorkingSetData(ServletContext context, HttpServletRequest request) {
		super(context, request);
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
	public boolean isTocIncluded(String href) {
		WorkingSet ws = getWorkingSet();
		if (ws == null)
			return false;

		IHelpResource[] elements = ws.getElements();
		for (int i = 0; i < elements.length; i++)
			if (elements[i].getHref().equals(href))
				return true;
		return false;
	}


	public String getOperation() {
		return request.getParameter("operation");
	}
}
