/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.debug.tests.stepfilters;

import org.eclipse.debug.core.model.IStepFilter;

public class TestStepFilter implements IStepFilter {

	@Override
	public boolean isFiltered(Object object) {
		if (object instanceof Boolean) {
			return (Boolean) object;
		}
		return false;
	}

}
