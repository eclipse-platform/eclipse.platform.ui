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

public class UniversalScope extends AbstractHelpScope {

	public boolean inScope(IToc toc) {
		return true;
	}

	public boolean inScope(ITopic topic) {
		return true;
	}

	public boolean inScope(IIndexEntry entry) {
		return true;
	}

	public boolean inScope(IIndexSee see) {
		return true;
	}

	public String getName(Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

}
