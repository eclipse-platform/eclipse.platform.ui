/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.performance;

import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.tests.menus.ObjectContributionClasses;
import org.eclipse.ui.tests.menus.ObjectContributionTest;

public class ObjectContributionsPerformance extends BasicPerformanceTest {

	public static Test suite() {
	    return new TestSuite(ObjectContributionsPerformance.class);
	}
	
	public ObjectContributionsPerformance() {
		super("testObjectContributions");
	}
	
	public ObjectContributionsPerformance(String id, int tagging) {
		super("testObjectContributions:" + id, tagging);
	}

	public void testAllContributionTypes() throws Throwable {
		ObjectContributionTest tests = new ObjectContributionTest("testObjectContributions");
		tagIfNecessary("Adding contributions to pop-up menu", new Dimension[]{Dimension.CPU_TIME, Dimension.USED_JAVA_HEAP});
		for (int i = 0; i < 10; i++) {			
			startMeasuring();			
			tests.assertPopupMenus("1", new String[] {"bogus"}, generateHugeAdaptableSelection(5000), null, false);
			stopMeasuring();
		}	
		commitMeasurements();
		assertPerformance();
	}
	
	protected IStructuredSelection generateHugeAdaptableSelection(int size) {
		List selection = new ArrayList();
		for (int i = 0; i < size; i++) {
			switch ((int) Math.round(Math.random() * 5)) {
				case 0 :
					selection.add(new ObjectContributionClasses.A());
					break;
				case 1 :
					selection.add(new ObjectContributionClasses.B());
					break;
				case 2 :
					selection.add(new ObjectContributionClasses.C());
					break;
				case 3 :
					selection.add(new ObjectContributionClasses.Common());
					break;
				case 4 :
					selection.add(new ObjectContributionClasses.D());
					break;
				case 5 :
					selection.add(new ObjectContributionClasses.A1());
					break;
				default :
					selection.add(new Object());
			}
		}
		return new StructuredSelection(selection);
	}
}
