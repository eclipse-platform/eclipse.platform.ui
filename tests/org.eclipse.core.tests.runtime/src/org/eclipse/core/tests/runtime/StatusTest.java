/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import java.util.Arrays;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.*;

public class StatusTest extends RuntimeTest {

	private int status1Severity = IStatus.OK;
	private String status1PluginId = "org.eclipse.core.tests.runtime";
	private int status1Code = -20;
	private String status1Message = "Something was canceled";
	private Exception status1Exception = new OperationCanceledException();
	private Status status1;

	private int status2Severity = IStatus.ERROR;
	private String status2PluginId = " ";
	private int status2Code = IStatus.OK;
	private String status2Message = "";
	private Exception status2Exception = null;
	private Status status2;

	private String multistatus1PluginId = "org.eclipse.core.tests.multistatus1";
	private int multistatus1Code = 20;
	private IStatus[] multistatus1Children = new IStatus[0];
	private String multistatus1Message = "Multistatus #1 message";
	private Throwable multistatus1Exception = new OperationCanceledException();
	private MultiStatus multistatus1;

	private MultiStatus multistatus2;

	public StatusTest() {
		super(null);
	}

	public StatusTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(StatusTest.class.getName());
		suite.addTest(new StatusTest("testSingleStatusReturnValues"));
		suite.addTest(new StatusTest("testMultiStatusReturnValues"));
		suite.addTest(new StatusTest("testAdd"));
		suite.addTest(new StatusTest("testAddAll"));
		suite.addTest(new StatusTest("testIsOK"));
		suite.addTest(new StatusTest("testMerge"));
		return suite;
	}

	protected void setUp() {
		status1 = new Status(status1Severity, status1PluginId, status1Code, status1Message, status1Exception);
		status2 = new Status(status2Severity, status2PluginId, status2Code, status2Message, status2Exception);

		multistatus1 = new MultiStatus(multistatus1PluginId, multistatus1Code, multistatus1Children, multistatus1Message, multistatus1Exception);
		multistatus2 = new MultiStatus(" ", 45, new Status[0], "", null);
		multistatus2.add(status1);
		multistatus2.add(status1);
	}

	public void testMultiStatusReturnValues() {
		assertEquals("1.1", multistatus1PluginId, multistatus1.getPlugin());
		assertEquals("1.2", multistatus1Code, multistatus1.getCode());
		assertTrue("1.3", Arrays.equals(multistatus1Children, multistatus1.getChildren()));
		assertEquals("1.4", multistatus1Message, multistatus1.getMessage());
		assertEquals("1.5", multistatus1Exception, multistatus1.getException());
		assertTrue("1.6", multistatus1.isMultiStatus());
		assertEquals("1.7", IStatus.OK, multistatus1.getSeverity());
		assertTrue("1.8", multistatus1.isOK());
		assertEquals("1.9", false, status1.matches(IStatus.ERROR | IStatus.WARNING | IStatus.INFO));
	}

	public void testSingleStatusReturnValues() {
		assertEquals("1.0", status1Severity, status1.getSeverity());
		assertEquals("1.1", status1PluginId, status1.getPlugin());
		assertEquals("1.2", status1Code, status1.getCode());
		assertEquals("1.3", status1Message, status1.getMessage());
		assertEquals("1.4", status1Exception, status1.getException());
		assertEquals("1.5", 0, status1.getChildren().length);
		assertEquals("1.6", false, status1.isMultiStatus());
		assertEquals("1.7", status1Severity == IStatus.OK, status1.isOK());
		assertEquals("1.8", status1.matches(IStatus.ERROR | IStatus.WARNING | IStatus.INFO), !status1.isOK());

		assertEquals("2.0", status2Severity, status2.getSeverity());
		assertEquals("2.1", status2PluginId, status2.getPlugin());
		assertEquals("2.2", status2Code, status2.getCode());
		assertEquals("2.3", status2Message, status2.getMessage());
		assertEquals("2.4", status2Exception, status2.getException());
		assertEquals("2.5", 0, status2.getChildren().length);
		assertEquals("2.6", false, status2.isMultiStatus());
		assertEquals("2.7", status2Severity == IStatus.OK, status2.isOK());
		assertEquals("2.8", status2.matches(IStatus.ERROR), !status2.isOK());
	}

	public void testAdd() {

		multistatus1.add(status1);
		assertEquals("1.0", status1, (multistatus1.getChildren())[0]);

		multistatus1.add(multistatus2);
		assertEquals("1.1", multistatus2, (multistatus1.getChildren())[1]);

		multistatus1.add(multistatus1);
		assertEquals("1.2", multistatus1, (multistatus1.getChildren())[2]);

	}

	public void testAddAll() {

		multistatus1.add(status2);
		multistatus1.addAll(multistatus2);
		Status[] array = new Status[3];
		array[0] = status2;
		array[1] = status1;
		array[2] = status1;

		assertTrue("1.0", multistatus1.getChildren().length == 3);
		assertTrue("1.1", Arrays.equals(array, multistatus1.getChildren()));

		multistatus1.add(multistatus2);
		multistatus1.addAll(multistatus1);
		Status[] array2 = new Status[8];
		array2[0] = status2;
		array2[1] = status1;
		array2[2] = status1;
		array2[3] = multistatus2;
		array2[4] = status2;
		array2[5] = status1;
		array2[6] = status1;
		array2[7] = multistatus2;

		assertTrue("2.0", multistatus1.getChildren().length == 8);
		assertTrue("2.1", Arrays.equals(array2, multistatus1.getChildren()));

	}

	public void testIsOK() {

		assertTrue("1.0", multistatus2.isOK());

		multistatus1.add(status2);
		multistatus1.addAll(multistatus2);
		assertTrue("1.1", !multistatus1.isOK());

	}

	public void testMerge() {

		multistatus1.merge(status2);
		multistatus1.merge(multistatus2);
		Status[] array = new Status[3];
		array[0] = status2;
		array[1] = status1;
		array[2] = status1;

		assertTrue("1.0", multistatus1.getChildren().length == 3);
		assertTrue("1.1", Arrays.equals(array, multistatus1.getChildren()));

		multistatus1.merge(multistatus1);
		Status[] array2 = new Status[6];
		array2[0] = status2;
		array2[1] = status1;
		array2[2] = status1;
		array2[3] = status2;
		array2[4] = status1;
		array2[5] = status1;

		assertTrue("2.0", multistatus1.getChildren().length == 6);
		assertTrue("2.1", Arrays.equals(array2, multistatus1.getChildren()));

		multistatus2.add(multistatus1);
		assertTrue("3.0", !multistatus2.isOK());
		multistatus2.merge(multistatus2.getChildren()[2]);
		assertTrue("3.1", multistatus2.getChildren().length == 9);

	}

}