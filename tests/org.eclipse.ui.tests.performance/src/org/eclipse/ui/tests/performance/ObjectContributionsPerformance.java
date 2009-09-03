/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.test.performance.Dimension;

public class ObjectContributionsPerformance extends BasicPerformanceTest {

	public  static final int SEED = 1001001;
	private IStructuredSelection selection;
	
	public static Test suite() {
		TestSuite suite = new TestSuite("Object contribution performance");
        suite.addTest(new ObjectContributionsPerformance(
                "large selection, limited contributors",
                generateAdaptableSelection(SEED, 5000),
                BasicPerformanceTest.NONE));
        suite
                .addTest(new ObjectContributionsPerformance(
                        "limited selection, limited contributors",
                        generateAdaptableSelection(SEED, 50),
                        BasicPerformanceTest.NONE));
        return suite;
	}
	
	public ObjectContributionsPerformance(String label, IStructuredSelection selection, int tagging) {
		super("testObjectContributions:" + label, tagging);
		this.selection = selection;
	}

	protected void runTest() {
		ObjectContributionTest tests = new ObjectContributionTest(
                "testObjectContributions");
        tagIfNecessary("UI - " + selection.size() + " contribution(s)",
                Dimension.ELAPSED_PROCESS);
        startMeasuring();
        for (int i = 0; i < 5000; i++) {
            tests.assertPopupMenus("1", new String[] { "bogus" }, selection,
                    null, false);
        }
        stopMeasuring();
        commitMeasurements();
        assertPerformance();
	}
	
	protected static IStructuredSelection generateAdaptableSelection(int seed, int size) {
		Random rand = new Random(seed);
		List selection = new ArrayList();
		for (int i = 0; i < size; i++) {
			switch ((int) Math.round(rand.nextDouble() * 5)) {
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
