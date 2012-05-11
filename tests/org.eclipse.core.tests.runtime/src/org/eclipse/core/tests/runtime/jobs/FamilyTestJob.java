/*******************************************************************************
 * Copyright (c) 2003, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.jobs;

import org.eclipse.core.tests.harness.TestJob;

/**
 * A test job that belongs to a particular family.
 */
class FamilyTestJob extends TestJob {
	private int familyType = TestJobFamily.TYPE_NONE;

	public FamilyTestJob(String name, int type) {
		super(name);
		familyType = type;
	}

	public FamilyTestJob(String name, int ticks, int tickLength, int type) {
		super(name, ticks, tickLength);
		familyType = type;
	}

	public boolean belongsTo(Object family) {
		return ((family instanceof TestJobFamily) && (((TestJobFamily) family).getType() == familyType));
	}

}
