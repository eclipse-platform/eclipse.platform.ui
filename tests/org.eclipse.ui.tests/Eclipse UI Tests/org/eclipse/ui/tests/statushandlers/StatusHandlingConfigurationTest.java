/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
import org.eclipse.ui.statushandlers.AbstractStatusHandler;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

import junit.framework.TestCase;

/**
 * @since 3.5
 *
 */
public class StatusHandlingConfigurationTest extends TestCase {
	public void testFreeStatusHandler(){
		final StatusAdapter adapter = new StatusAdapter(new Status(IStatus.ERROR,"fakeplugin","testmessage"));
		final boolean[] called = new boolean[]{false};
		AbstractStatusHandler tester = new AbstractStatusHandler(){
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
}
