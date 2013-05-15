/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.statushandlers;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

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

public class SupportTrayTest extends TestCase {
	

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		Policy.setErrorSupportProvider(null);
	}

	private final class NullErrorSupportProvider extends ErrorSupportProvider {
		public Control createSupportArea(Composite parent, IStatus status) {
			return null;
		}
	}

	private final class NullListener implements Listener {
		public void handleEvent(Event event) {
			
		}
	}

	public void testDefaultSupportProviderEnablement(){
		Map dialogState = new HashMap();
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
	
	public void testJFacePolicySupportProvider(){
		Map dialogState = new HashMap();
		StatusAdapter sa = new StatusAdapter(Status.OK_STATUS);
		dialogState.put(IStatusDialogConstants.CURRENT_STATUS_ADAPTER, sa);
		SupportTray st = new SupportTray(dialogState, new NullListener());
		
		assertNull(st.providesSupport(sa));
		
		final IStatus[] _status = new IStatus[]{null};
		
		Policy.setErrorSupportProvider(new ErrorSupportProvider() {
			
			public Control createSupportArea(Composite parent, IStatus status) {
				_status[0] = status;
				return new Composite(parent, SWT.NONE);
			}
		});
		
		assertNotNull(st.providesSupport(sa));

		TrayDialog td = null;
		try {
			td = new TrayDialog(new Shell()) {
			};
			td.setBlockOnOpen(false);
			td.open();
			td.openTray(st);
		} finally {
			if (td != null)
				td.close();
		}

		assertEquals(Status.OK_STATUS, _status[0]);
	}
	
	public void testJFacePolicyOverDefaultPreference() {
		Map dialogState = new HashMap();
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
	
	public void testSelfClosure(){
		final TrayDialog td[] = new TrayDialog[] { null };
		try {
			td[0] = new TrayDialog(new Shell()) {
			};
			Map dialogState = new HashMap();
			dialogState.put(IStatusDialogConstants.CURRENT_STATUS_ADAPTER, new StatusAdapter(Status.OK_STATUS));
			SupportTray st = new SupportTray(dialogState, new Listener() {
				public void handleEvent(Event event) {
					td[0].closeTray();
				}
			});
			td[0].setBlockOnOpen(false);
			td[0].open();
			td[0].openTray(st);
		} finally {
			if (td != null)
				td[0].close();
		}
	}

}
