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
import org.eclipse.help.internal.base.HelpBaseResources;

/**
 * This is a dummy scope which is used by the UI to allow the user to turn on
 * and off filtering by search scope. The inScope functions should never be called
 */

public class SearchScopeScope extends AbstractHelpScope {

	public boolean inScope(IToc toc) {
		return false;
	}

	public boolean inScope(ITopic topic) {
		return false;
	}

	public boolean inScope(IIndexEntry entry) {
		return false;
	}

	public boolean inScope(IIndexSee see) {
		return false;
	}
	
	/**
	 * @return the name for the system locale. This filter only applies to
	 * the help system running in workbench mode so there is not need to
	 * be able to return a name in any locale, just the current one.
	 */
	public String getName(Locale locale) {
		return HelpBaseResources.SearchScopeFilterName;
	}

}
