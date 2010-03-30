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
package org.eclipse.ua.tests.help.criteria;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.help.AbstractCriteriaProvider;
import org.eclipse.help.ICriteria;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.ua.tests.help.other.UserCriteria;

public class SampleCriteriaProvider extends AbstractCriteriaProvider {

	public ICriteria[] getCriteria(ITopic topic) {
		return getCriteriaFromLabel(topic.getLabel());
	}

	public ICriteria[] getCriteria(IToc toc) {
		return getCriteriaFromLabel(toc.getLabel());
	}

	private UserCriteria[] getCriteriaFromLabel(String label) {
		List criteria = new ArrayList();
		if (label == null) {
			return new UserCriteria[0];
		}
		if (label.toLowerCase().indexOf('t') >= 0) {
			criteria.add( new UserCriteria("containsLetter", "t", true) );
		}
		if (label.toLowerCase().indexOf('k') >= 0) {
			criteria.add( new UserCriteria("containsLetter", "k", true) );
		}
		if (label.toLowerCase().indexOf('v') >= 0) {
			criteria.add( new UserCriteria("containsLetter", "v", true) );
		}
		if (label.toLowerCase().indexOf('c') >= 0) {
			criteria.add( new UserCriteria("containsLetter", "c", true) );
		}
		return (UserCriteria[]) criteria.toArray(new UserCriteria[criteria.size()]);
	}	

}
