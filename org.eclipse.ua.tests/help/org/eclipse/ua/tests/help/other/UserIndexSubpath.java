/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.other;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.help.IIndexSubpath;
import org.eclipse.help.IUAElement;


public class UserIndexSubpath implements IIndexSubpath {
	
	private String keyword;
	
	public UserIndexSubpath(String keyword) {
		this.keyword = keyword;
	}
	

	public String getKeyword() {
		return keyword;
	}

	public IUAElement[] getChildren() {
		return new IUAElement[0];
	}

	public boolean isEnabled(IEvaluationContext context) {
		return true;
	}

}
