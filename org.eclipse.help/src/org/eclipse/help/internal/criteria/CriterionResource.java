/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.criteria;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.help.ICriteria;

import org.eclipse.help.internal.HelpPlugin;

/**
 * A class represents one criterion, which has the name and values
 * 
 * @since 3.5
 */
public class CriterionResource {

	private String criterionName;
	private List criterionValues;
	
	public CriterionResource(String criterionName){
		this(criterionName, null);
	}
	
	public CriterionResource (String criterionName, List criterionValues){
		this.criterionName = criterionName;
		this.criterionValues = new ArrayList();
		if(null != criterionValues) {
			this.addCriterionValues(criterionValues);
		}
	}
	
	public String getCriterionName(){
		return this.criterionName;
	}
	
	public List getCriterionValues(){
		return this.criterionValues;
	}
	
	public void addCriterionValue(String criterionValue){
		if(null != criterionValue && 0 != criterionValue.length() && !criterionValues.contains(criterionValue)){
			criterionValues.add(criterionValue);
		}
	}
	
	public void addCriterionValues(List criterionValues){
		for(Iterator iterator = criterionValues.iterator(); iterator.hasNext();){
			String criterionValue = (String) iterator.next();
			this.addCriterionValue(criterionValue);
		}
	}
	
	public static CriterionResource[] toCriterionResource(ICriteria[] criteriaElements) {
		List criteriaList = new ArrayList();
		outer: for (int i = 0; i < criteriaElements.length; ++i) {
			String elementName = criteriaElements[i].getName();
			String elementValue = criteriaElements[i].getValue();
			if (null != elementName && 0 != elementName.length() && null != elementValue
					&& 0 != elementValue.length()) {
				if (HelpPlugin.getCriteriaManager().isSupportedCriterion(elementName)) {
					elementName = elementName.toLowerCase();
					StringTokenizer tokenizer = new StringTokenizer(elementValue, ","); //$NON-NLS-1$ 
					List values = new ArrayList();
					while (tokenizer.hasMoreTokens()) {
						values.add(tokenizer.nextToken().trim());
					}
					for(int j = 0; j < criteriaList.size(); ++j){
						CriterionResource criterion = (CriterionResource) criteriaList.get(j);
						if(elementName.equals(criterion.getCriterionName())){
							criterion.addCriterionValues(values);
							continue outer;
						}
					}
				    CriterionResource criterionResource = new CriterionResource(elementName, values);
				    criteriaList.add(criterionResource);
				}
			}
		}
		CriterionResource[] criteria = new CriterionResource[criteriaList.size()];                                        		
		criteriaList.toArray(criteria);
		return criteria;
	}

}
