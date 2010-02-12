/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.help.internal.criteria.CriterionResource;
import org.eclipse.help.internal.webapp.servlet.WebappWorkingSetManager;
import org.eclipse.help.internal.workingset.AdaptableHelpResource;
import org.eclipse.help.internal.workingset.WorkingSet;

/**
 * This class manages help working sets
 */
public class WorkingSetManagerData extends RequestData {
	private final static int NONE = 0;
	private final static int ADD = 1;
	private final static int REMOVE = 2;
	private final static int EDIT = 3;

	private String name;
	private WebappWorkingSetManager wsmgr;
	// Indicates whether operation specified in the request failed
	private boolean saved = true;

	public WorkingSetManagerData(ServletContext context,
			HttpServletRequest request, HttpServletResponse response) {
		super(context, request, response);
		wsmgr = new WebappWorkingSetManager(request, response, getLocale());
		name = request.getParameter("workingSet"); //$NON-NLS-1$
		try {
			switch (getOperation()) {
			case ADD:
				addWorkingSet();
				break;
			case REMOVE:
				removeWorkingSet();
				break;
			case EDIT:
				editWorkingSet();
				break;
			default:
				break;
			}
		} catch (IOException ioe) {
			saved = false;
		}
	}

	public void addWorkingSet() throws IOException {
		if (name != null && name.length() > 0) {

			String[] hrefs = request.getParameterValues("hrefs"); //$NON-NLS-1$
			if (hrefs == null)
				hrefs = new String[0];

			ArrayList selectedElements = new ArrayList(hrefs.length);
			for (int i = 0; i < hrefs.length; i++) {
				AdaptableHelpResource res = getAdaptableHelpResource(hrefs[i]);
				if (res != null)
					selectedElements.add(res);
			}

			AdaptableHelpResource[] elements = new AdaptableHelpResource[selectedElements
					.size()];
			selectedElements.toArray(elements);
			
			WorkingSet ws = null;
			if(!isCriteriaScopeEnabled()) {
				ws = wsmgr.createWorkingSet(name, elements);
			} else {
				CriterionResource[] criteria = getCriteriaResource();
				ws = wsmgr.createWorkingSet(name, elements, criteria);
			}
			
			wsmgr.addWorkingSet(ws);
		}
	}

	public void removeWorkingSet() {
		if (name != null && name.length() > 0) {

			WorkingSet ws = wsmgr.getWorkingSet(name);
			if (ws != null)
				wsmgr.removeWorkingSet(ws);
		}
	}

	public void editWorkingSet() throws IOException {
		if (name != null && name.length() > 0) {

			String oldName = request.getParameter("oldName"); //$NON-NLS-1$
			if (oldName == null || oldName.length() == 0)
				oldName = name;
			WorkingSet ws = wsmgr.getWorkingSet(oldName);
			if (ws != null) {
				String[] hrefs = request.getParameterValues("hrefs"); //$NON-NLS-1$
				if (hrefs == null)
					hrefs = new String[0];

				ArrayList selectedElements = new ArrayList(hrefs.length);
				for (int i = 0; i < hrefs.length; i++) {
					AdaptableHelpResource res = getAdaptableHelpResource(hrefs[i]);
					if (res != null)
						selectedElements.add(res);
				}

				AdaptableHelpResource[] elements = new AdaptableHelpResource[selectedElements
						.size()];
				selectedElements.toArray(elements);

				ws.setElements(elements);
				ws.setName(name);
				
				if(isCriteriaScopeEnabled()){
					ws.setCriteria(getCriteriaResource());
				}
				// should also change the name....

				// We send this notification, so that the manager fires to its
				// listeners
				wsmgr.workingSetChanged(ws);
			}
		}
	}

	public String[] getWorkingSets() {
		WorkingSet[] workingSets = wsmgr.getWorkingSets();
		String[] sets = new String[workingSets.length];
		for (int i = 0; i < workingSets.length; i++)
			sets[i] = workingSets[i].getName();

		return sets;
	}

	public String getWorkingSetName() {
		if (name == null || name.length() == 0) {
			// See if anything is set in the preferences
			name = wsmgr.getCurrentWorkingSet();
			if (name == null || name.length() == 0
					|| wsmgr.getWorkingSet(name) == null)
				name = ServletResources.getString("All", request); //$NON-NLS-1$
		}
		return name;
	}

	public WorkingSet getWorkingSet() {
		if (name != null && name.length() > 0)
			return wsmgr.getWorkingSet(name);
		return null;
	}

	public boolean isCurrentWorkingSet(int i) {
		WorkingSet[] workingSets = wsmgr.getWorkingSets();
		return workingSets[i].getName().equals(name);
	}

	private int getOperation() {
		String op = request.getParameter("operation"); //$NON-NLS-1$
		if ("add".equals(op)) //$NON-NLS-1$
			return ADD;
		else if ("remove".equals(op)) //$NON-NLS-1$
			return REMOVE;
		else if ("edit".equals(op)) //$NON-NLS-1$
			return EDIT;
		else
			return NONE;
	}

	private AdaptableHelpResource getAdaptableHelpResource(String internalId) {
		AdaptableHelpResource res = wsmgr.getAdaptableToc(internalId);
		if (res == null)
			res = wsmgr.getAdaptableTopic(internalId);
		return res;
	}

	/**
	 * @return null or error message if saving saved
	 */
	public String getSaveError() {
		if (saved) {
			return null;
		}
		return UrlUtil.JavaScriptEncode(ServletResources.getString(
				"cookieSaveFailed", request)); //$NON-NLS-1$
	}

	private CriterionResource[] getCriteriaResource(){
		// all values in one criterion selected: version
		// one criterion value selected(based on criterion category name and index of the value)
		// eg:version_1_
		List category = Arrays.asList(getCriterionIds());
		
		String[] criteria = request.getParameterValues("criteria"); //$NON-NLS-1$
		if (criteria == null)
			criteria = new String[0];

		Map selectedElements = new HashMap();
		for (int i = 0; i < criteria.length; ++i) {
			String criterion = criteria[i];
			if(category.contains(criterion)){
				List allValuesInCategory = Arrays.asList(getCriterionValueIds(criterion));
				if(0 == allValuesInCategory.size()){
					continue;
				}
				Set elements = (Set)selectedElements.get(criterion);
				if(null == elements){
					elements = new HashSet();
				}
				elements.addAll(allValuesInCategory);
				selectedElements.put(criterion, elements);
			}else{
				int len = criterion.length();
				if (criterion.charAt(len - 1) == '_') {
					String indexStr = criterion.substring(criterion.lastIndexOf('_', len - 2) + 1, len - 1);
					int index = 0;
					try {
						index = Integer.parseInt(indexStr);
					} catch (Exception e) {
						continue;
					}

					String criterionName = criterion.substring(0, criterion.lastIndexOf('_', len - 2));
					if(category.contains(criterionName)){
						String values[] = getCriterionValueIds(criterionName);
						if (index < 0 || index >= values.length)
							continue;
						String selectedValue = values[index];
						if(null == selectedValue || 0 == selectedValue.length())
							continue;
						Set existedElements = (Set)selectedElements.get(criterionName);
						if(null == existedElements){
							existedElements = new HashSet();
						}
						existedElements.add(selectedValue);
						selectedElements.put(criterionName, existedElements);	
					}
				}
			}
		}

		
		List resources = new ArrayList();
		for(Iterator iter = selectedElements.keySet().iterator(); iter.hasNext();){
			String key = (String)iter.next();
			Set values = (Set) selectedElements.get(key);
			CriterionResource resource = new CriterionResource(key, new ArrayList(values));
			resources.add(resource);
		}
		
		CriterionResource[] processedResources = new CriterionResource[resources.size()];
		resources.toArray(processedResources);
		return processedResources;
		
	}

	public boolean isCriteriaScopeEnabled() {
		return wsmgr.isCriteriaScopeEnabled();
	}

	private String[] getCriterionIds() {
		return wsmgr.getCriterionIds();
	}

	private String[] getCriterionValueIds(String criterionId) {
		return wsmgr.getCriterionValueIds(criterionId);
	}
}
