/*******************************************************************************
 * Copyright (c) 2009, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.statushandlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorSupportProvider;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.statushandlers.IStatusDialogConstants;
import org.eclipse.ui.internal.statushandlers.StackTraceSupportArea;
import org.eclipse.ui.internal.statushandlers.SupportTray;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.tests.SwtLeakTestWatcher;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

public class SupportTrayTest {

	@Rule
	public TestWatcher swtLeakTestWatcher = new SwtLeakTestWatcher();

	@After
	public void tearDown() throws Exception {
		Policy.setErrorSupportProvider(null);
	}

	private static final class NullErrorSupportProvider extends ErrorSupportProvider {
		@Override
		public Control createSupportArea(Composite parent, IStatus status) {
			return null;
		}
	}

	private static final class NullListener implements Listener {
		@Override
		public void handleEvent(Event event) {

		}
	}

	@Test
	public void testDefaultSupportProviderEnablement(){
		Map<Object, Object> dialogState = new HashMap<>();
		Status status = new Status(IStatus.ERROR, "org.eclipse.ui.test",
				"Message.", new NullPointerException());
		StatusAdapter sa = new StatusAdapter(status);
		dialogState.put(IStatusDialogConstants.CURRENT_STATUS_ADAPTER, sa);
		SupportTray st = new SupportTray(dialogState, new NullListener());
		assertNull(st.providesSupport(sa));

		dialogState.put(IStatusDialogConstants.ENABLE_DEFAULT_SUPPORT_AREA, Boolean.TRUE);
		assertNotNull(st.providesSupport(sa));

		assertTrue(st.getSupportProvider() instanceof StackTraceSupportArea);
	}

	@Test
	public void testJFacePolicySupportProvider(){
		Map<Object, Object> dialogState = new HashMap<>();
		StatusAdapter sa = new StatusAdapter(Status.OK_STATUS);
		dialogState.put(IStatusDialogConstants.CURRENT_STATUS_ADAPTER, sa);
		SupportTray st = new SupportTray(dialogState, new NullListener());

		assertNull(st.providesSupport(sa));

		final IStatus[] _status = new IStatus[]{null};

		Policy.setErrorSupportProvider(new ErrorSupportProvider() {

			@Override
			public Control createSupportArea(Composite parent, IStatus status) {
				_status[0] = status;
				return new Composite(parent, SWT.NONE);
			}
		});

		assertNotNull(st.providesSupport(sa));

		Shell shell = new Shell();
		TrayDialog td = null;
		try {
			td = new TrayDialog(shell) {
			};
			td.setBlockOnOpen(false);
			td.open();
			td.openTray(st);
		} finally {
			if (td != null) {
				td.close();
			}
			shell.close();
		}

		assertEquals(Status.OK_STATUS, _status[0]);
	}

	@Test
	public void testJFacePolicyOverDefaultPreference() {
		Map<Object, Object> dialogState = new HashMap<>();
		StatusAdapter sa = new StatusAdapter(Status.OK_STATUS);
		dialogState.put(IStatusDialogConstants.CURRENT_STATUS_ADAPTER, sa);
		SupportTray st = new SupportTray(dialogState, new NullListener());

		assertNull(st.providesSupport(sa));

		ErrorSupportProvider provider = new NullErrorSupportProvider();

		Policy.setErrorSupportProvider(provider);

		dialogState.put(IStatusDialogConstants.ENABLE_DEFAULT_SUPPORT_AREA, Boolean.TRUE);
		assertNotNull(st.providesSupport(sa));

		assertEquals(provider, st.getSupportProvider());
	}

	@Test
	public void testSelfClosure(){
		final TrayDialog td[] = new TrayDialog[] { null };
		Shell shell = new Shell();
		try {
			td[0] = new TrayDialog(shell) {
			};
			Map<Object, Object> dialogState = new HashMap<>();
			dialogState.put(IStatusDialogConstants.CURRENT_STATUS_ADAPTER, new StatusAdapter(Status.OK_STATUS));
			SupportTray st = new SupportTray(dialogState, event -> td[0].closeTray());
			td[0].setBlockOnOpen(false);
			td[0].open();
			td[0].openTray(st);
		} finally {
			if (td != null) {
				td[0].close();
			}
			shell.close();
		}
	}

}
