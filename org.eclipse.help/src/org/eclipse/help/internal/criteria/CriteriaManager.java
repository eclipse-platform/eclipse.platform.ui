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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.ICriteria;
import org.eclipse.help.internal.HelpPlugin;

/**
 * Get criteria related parameter from preferences.ini
 * 
 * @since 3.5
 */
public class CriteriaManager {
	
	private final static String SUPPORTED_CRITERIA = "supportedCriteria"; //$NON-NLS-1$
	private final static String ENABLE_CRITERIA = "enableCriteria"; //$NON-NLS-1$
	
	private List supportedCriteria;
	private boolean criteriaEnabled;
	private Map allCriteriaValues;
	
	private CriteriaDefinitionManager criteriaDefinitionManager;
	
	public CriteriaManager() {
		criteriaEnabled = Platform.getPreferencesService().getBoolean(HelpPlugin.PLUGIN_ID, ENABLE_CRITERIA, false, null);
		
		supportedCriteria = new ArrayList();
		StringTokenizer criteria = new StringTokenizer(Platform.getPreferencesService().getString(HelpPlugin.PLUGIN_ID, SUPPORTED_CRITERIA, "", null), ",;"); //$NON-NLS-1$ //$NON-NLS-2$
		while (criteria.hasMoreTokens()) {
			supportedCriteria.add(criteria.nextToken().toLowerCase().trim());
		}
		
		allCriteriaValues = new HashMap();
		
		if (criteriaDefinitionManager == null){
			criteriaDefinitionManager = new CriteriaDefinitionManager();
		}
	}

	public boolean isSupportedCriterion(String criterion){
		if(null != criterion && supportedCriteria.contains(criterion.toLowerCase())){
			return true;
		}
		return false;
	}
	
	public boolean isCriteriaEnabled(){
		return criteriaEnabled;
	}
	
	public void addCriteriaValues(ICriteria[] criteria, String locale){
		Map criteriaInLocale = (HashMap)allCriteriaValues.get(locale);
		if(null == criteriaInLocale) {
			criteriaInLocale = new HashMap();
		}
		CriterionResource[] resources = CriterionResource.toCriterionResource(criteria);
		for(int i = 0; i < resources.length; ++ i){
			CriterionResource criterion = resources[i];
			String criterionName = criterion.getCriterionName();
			List criterionValues = criterion.getCriterionValues();
			
			Set existedValues = (Set)criteriaInLocale.get(criterionName);
			if (null == existedValues)
				existedValues = new HashSet();
			existedValues.addAll(criterionValues);
			criteriaInLocale.put(criterionName, existedValues);
		}
		allCriteriaValues.put(locale, criteriaInLocale);
	}
	
	public Map getAllCriteriaValues(String locale){
		Map criteria = (Map) allCriteriaValues.get(locale);
		if(null == criteria) {
			criteria = new HashMap();
		}
		return criteria;
	}
	
	
	public String getCriterionDisplayName (String criterionId, String locale){
		return criteriaDefinitionManager.getCriterionName(criterionId, locale);
	}
	
	public String getCriterionValueDisplayName(String criterionId, String criterionValueId, String locale) {
		return criteriaDefinitionManager.getCriterionValueName(criterionId, criterionValueId, locale);
	}

}
