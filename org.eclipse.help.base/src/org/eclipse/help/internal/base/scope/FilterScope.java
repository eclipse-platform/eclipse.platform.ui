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

import java.util.Locale;

import org.eclipse.help.IIndexEntry;
import org.eclipse.help.IIndexSee;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.base.AbstractHelpScope;
import org.eclipse.help.internal.base.HelpEvaluationContext;

/**
 * A scope which tests for content filtering
 */

public class FilterScope extends AbstractHelpScope {

	public boolean inScope(IToc toc) {
		if (!toc.isEnabled(HelpEvaluationContext.getContext())) {
			return false;
		}
		return hasInScopeChildren(toc);
	}

	public boolean inScope(ITopic topic) {
		if (!topic.isEnabled(HelpEvaluationContext.getContext())) {
			return false;
		}
		if (topic.getHref() != null) {
			return true;
		}
		return ScopeUtils.hasInScopeDescendent(topic, this);
	}

	public boolean inScope(IIndexEntry entry) {
		return entry.isEnabled(HelpEvaluationContext.getContext());
	}

	public boolean inScope(IIndexSee see) {
		return see.isEnabled(HelpEvaluationContext.getContext());
	}

	public String getName(Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

}
