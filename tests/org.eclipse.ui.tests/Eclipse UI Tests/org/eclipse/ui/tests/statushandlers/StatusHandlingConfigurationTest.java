/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.statushandlers;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.statushandlers.AbstractStatusHandler;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.statushandlers.StatusManager.INotificationTypes;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @since 3.5
 *
 */
public class StatusHandlingConfigurationTest extends TestCase {
	public static TestSuite suite() {
		TestSuite ts = new TestSuite("org.eclipse.ui.tests.statushandlers.StatusHandlingConfigurationTest");
		ts.addTest(new StatusHandlingConfigurationTest("testFreeStatusHandler"));
		ts.addTest(new StatusHandlingConfigurationTest("testDefaultNotification"));
		return ts;
	}

	public StatusHandlingConfigurationTest(String name) {
		super(name);
	}

	public void testFreeStatusHandler(){
		final StatusAdapter adapter = new StatusAdapter(new Status(IStatus.ERROR,"fakeplugin","testmessage"));
		final boolean[] called = new boolean[]{false};
		AbstractStatusHandler tester = new AbstractStatusHandler(){
			@Override
			public void handle(StatusAdapter statusAdapter, int style) {
				if(statusAdapter == adapter){
					called[0] = true;
				}
			}
		};
		FreeStatusHandler.setTester(tester);
		StatusManager.getManager().handle(adapter);
		assertEquals(true, called[0]);
	}

	public void testDefaultNotification(){
		final StatusAdapter adapter = new StatusAdapter(new Status(IStatus.ERROR,"fakeplugin","testmessage"));
		adapter.setProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.TRUE);
		final StatusAdapter adapter2 = new StatusAdapter(new Status(IStatus.ERROR,"fakeplugin2","testmessage2"));
		final boolean[] called = new boolean[]{false};
		StatusManager.getManager().addListener(new StatusManager.INotificationListener(){
					@Override
					public void statusManagerNotified(int type,
							StatusAdapter[] adapters) {
						if (type == INotificationTypes.HANDLED) {
							called[0] = true;
						}
					}
		});
		StatusManager.getManager().handle(adapter, StatusManager.SHOW);
		assertEquals(false, called[0]);
		StatusManager.getManager().handle(adapter2, StatusManager.SHOW);
		assertEquals(true, called[0]);

	}
}
