/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.runtime.locks;

import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IProgressProvider;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.tests.harness.FussyProgressMonitor;

/**
 * Dispatches fussy progress monitors, and sanity checks them when finished.
 */
public class FussyProgressProvider extends Assert implements IProgressProvider {
	private ArrayList monitors = new ArrayList();
	public IProgressMonitor createMonitor(Job job) {
		IProgressMonitor result = new FussyProgressMonitor();
		monitors.add(result);
		return result;
	}
	public void sanityCheck() {
		for (Iterator it = monitors.iterator(); it.hasNext();) {
			((FussyProgressMonitor) it.next()).sanityCheck();
		}
	}
}