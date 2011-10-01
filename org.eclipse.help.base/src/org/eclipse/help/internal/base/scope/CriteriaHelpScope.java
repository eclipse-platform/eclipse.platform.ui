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

package org.eclipse.help.internal.base.scope;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.help.ICriteria;
import org.eclipse.help.IIndexEntry;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.base.AbstractHelpScope;
import org.eclipse.help.internal.base.util.CriteriaUtilities;
import org.eclipse.help.internal.criteria.CriteriaProviderRegistry;
import org.eclipse.help.internal.criteria.CriterionResource;

public class CriteriaHelpScope extends AbstractHelpScope {

	private static final String UNCATEGORIZED = "Uncategorized"; //$NON-NLS-1$

	private CriterionResource[] criteriaScope;

	public CriteriaHelpScope(CriterionResource[] criteriaScope) {
		this.criteriaScope = criteriaScope;
	}

	public CriteriaHelpScope(List criteriaScope){
		if(null == criteriaScope) {
			this.criteriaScope = new CriterionResource[0];
		} else {
			this.criteriaScope = new CriterionResource[criteriaScope.size()];                                        		
			criteriaScope.toArray(this.criteriaScope);
		}
	}
	
	public boolean inScope(IToc toc) {
		if(null == toc){
			if(null == criteriaScope || 0 == criteriaScope.length){
				return true;
			}
			return false;
		}
		ICriteria[] criteriaOfToc = CriteriaProviderRegistry.getInstance().getAllCriteria(toc);
		return isCriteriaInScope(criteriaOfToc);
	}

	public boolean inScope(ITopic topic) {
		if(null == topic){
			if(null == criteriaScope || 0 == criteriaScope.length){
				return true;
			}
			return false;
		}
		ICriteria[] criteriaOfTopic = CriteriaProviderRegistry.getInstance().getAllCriteria(topic);
		return isCriteriaInScope(criteriaOfTopic);
	}

	private boolean isCriteriaInScope(ICriteria[] criteriaOfTopic) {
		if(null == criteriaScope){
			return true;
		}
		Map ownCriteria = getCriteriaInfo(criteriaOfTopic);
		Map scope = getCriteriaInfo(criteriaScope);
		outer: for (Iterator keyIterator = scope.keySet().iterator(); keyIterator.hasNext();) {
			String key = String.valueOf(keyIterator.next());
			for (Iterator valueIterator = ((Set)scope.get(key)).iterator(); valueIterator.hasNext();) {
				String value = String.valueOf(valueIterator.next());
				if (value.equals(UNCATEGORIZED)) {
					if (!ownCriteria.containsKey(key)) {
						continue outer;						
					}
				} else {
					if (null != ownCriteria.get(key) && ((Set)ownCriteria.get(key)).contains(value))
						continue outer;					
				}
			}
			return false;
		}
		return true;
	}

	private Map getCriteriaInfo(CriterionResource[] criteria) {
		Map criteriaMap = new HashMap();
		CriteriaUtilities.addCriteriaToMap(criteriaMap, criteria);
		return criteriaMap;
	}
	
	private Map getCriteriaInfo(ICriteria[] criteria) {
		Map criteriaMap = new HashMap();
		CriteriaUtilities.addCriteriaToMap(criteriaMap, criteria);
	    return criteriaMap;
	}

	public boolean inScope(IIndexEntry entry) {
		return hasInScopeChildren(entry);
	}

	public String getName(Locale locale) {
		return null;
	}
	
}
