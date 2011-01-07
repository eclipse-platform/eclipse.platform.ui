/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.criteria;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.help.AbstractCriteriaProvider;
import org.eclipse.help.ICriteria;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.ua.tests.help.other.UserCriteria;

public class SampleCriteriaProvider extends AbstractCriteriaProvider {

	public static final String CONTAINS_LETTER = "containsLetter";

	public ICriteria[] getCriteria(ITopic topic) {
		return getCriteriaFromLabel(topic.getLabel());
	}

	public ICriteria[] getCriteria(IToc toc) {
		return getCriteriaFromLabel(toc.getLabel());
	}

	private UserCriteria[] getCriteriaFromLabel(String label) {
		List<UserCriteria> criteria = new ArrayList<UserCriteria>();
		if (label == null) {
			return new UserCriteria[0];
		}
		if (label.toLowerCase().indexOf('t') >= 0) {
			criteria.add( new UserCriteria(CONTAINS_LETTER, "t", true) );
		}
		if (label.toLowerCase().indexOf('k') >= 0) {
			criteria.add( new UserCriteria(CONTAINS_LETTER, "k", true) );
		}
		if (label.toLowerCase().indexOf('v') >= 0) {
			criteria.add( new UserCriteria(CONTAINS_LETTER, "v", true) );
		}
		if (label.toLowerCase().indexOf('c') >= 0) {
			criteria.add( new UserCriteria(CONTAINS_LETTER, "c", true) );
		}
		return criteria.toArray(new UserCriteria[criteria.size()]);
	}	

}
