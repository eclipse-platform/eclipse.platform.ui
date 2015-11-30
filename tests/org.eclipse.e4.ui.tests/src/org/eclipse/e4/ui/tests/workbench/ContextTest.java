/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.commands.contexts.Context;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.services.ContextServiceAddon;
import org.eclipse.e4.ui.services.EContextService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class ContextTest {
	private static final String WINDOW_ID = "org.eclipse.ui.contexts.window";
	private static final String DIALOG_ID = "org.eclipse.ui.contexts.dialog";
	private static final String DIALOG_AND_WINDOW_ID = "org.eclipse.ui.contexts.dialogAndWindow";
	private IEclipseContext appContext;

	@Before
	public void setUp() throws Exception {
		appContext = E4Application.createDefaultContext();
		ContextInjectionFactory.make(ContextServiceAddon.class, appContext);
	}

	@After
	public void tearDown() throws Exception {
		appContext.dispose();
	}

	@Test
	public void testOneContext() throws Exception {

		defineContexts(appContext);

		EContextService cs = (EContextService) appContext
				.get(EContextService.class.getName());
		assertEquals(0, cs.getActiveContextIds().size());

		cs.activateContext(DIALOG_AND_WINDOW_ID);
		assertEquals(1, cs.getActiveContextIds().size());
	}

	@Test
	public void testTwoContexts() throws Exception {

		defineContexts(appContext);

		EContextService cs = (EContextService) appContext
				.get(EContextService.class.getName());
		assertEquals(0, cs.getActiveContextIds().size());

		IEclipseContext window = appContext.createChild("windowContext");
		window.activate();

		EContextService windowService = (EContextService) window
				.get(EContextService.class.getName());
		cs.activateContext(DIALOG_AND_WINDOW_ID);

		assertEquals(1, cs.getActiveContextIds().size());
		assertEquals(1, windowService.getActiveContextIds().size());

		cs.deactivateContext(DIALOG_AND_WINDOW_ID);
		assertEquals(0, windowService.getActiveContextIds().size());
		assertEquals(0, cs.getActiveContextIds().size());
	}

	@Test
	public void testTwoContextsBottom() throws Exception {

		defineContexts(appContext);

		EContextService cs = (EContextService) appContext
				.get(EContextService.class.getName());
		assertEquals(0, cs.getActiveContextIds().size());

		IEclipseContext window = appContext.createChild("windowContext");
		window.activate();

		EContextService windowService = (EContextService) window
				.get(EContextService.class.getName());
		windowService.activateContext(DIALOG_AND_WINDOW_ID);

		assertEquals(1, cs.getActiveContextIds().size());
		assertEquals(1, windowService.getActiveContextIds().size());

		windowService.deactivateContext(DIALOG_AND_WINDOW_ID);
		assertEquals(0, windowService.getActiveContextIds().size());
		assertEquals(0, cs.getActiveContextIds().size());

		window.deactivate();
		cs.activateContext(DIALOG_AND_WINDOW_ID);
		assertEquals(1, cs.getActiveContextIds().size());
		assertEquals(1, windowService.getActiveContextIds().size());

		cs.deactivateContext(DIALOG_AND_WINDOW_ID);
		assertEquals(0, windowService.getActiveContextIds().size());
		assertEquals(0, cs.getActiveContextIds().size());
	}

	@Test
	public void testThreeContexts() throws Exception {

		defineContexts(appContext);

		EContextService cs = (EContextService) appContext
				.get(EContextService.class.getName());

		IEclipseContext window = appContext.createChild("windowContext");
		EContextService windowService = (EContextService) window
				.get(EContextService.class.getName());

		IEclipseContext dialog = appContext.createChild("dialogContext");
		dialog.activate();

		EContextService dialogService = (EContextService) dialog
				.get(EContextService.class.getName());

		cs.activateContext(DIALOG_AND_WINDOW_ID);
		windowService.activateContext(WINDOW_ID);

		assertEquals(1, cs.getActiveContextIds().size());
		assertEquals(1, dialogService.getActiveContextIds().size());
		assertEquals(2, windowService.getActiveContextIds().size());

		dialogService.activateContext(DIALOG_ID);
		assertEquals(2, cs.getActiveContextIds().size());
		assertEquals(2, dialogService.getActiveContextIds().size());
		assertEquals(2, windowService.getActiveContextIds().size());

		assertTrue(cs.getActiveContextIds().contains(DIALOG_AND_WINDOW_ID));
		assertTrue(cs.getActiveContextIds().contains(DIALOG_ID));
		assertFalse(cs.getActiveContextIds().contains(WINDOW_ID));

		assertTrue(dialogService.getActiveContextIds().contains(
				DIALOG_AND_WINDOW_ID));
		assertTrue(dialogService.getActiveContextIds().contains(DIALOG_ID));
		assertFalse(dialogService.getActiveContextIds().contains(WINDOW_ID));

		assertTrue(windowService.getActiveContextIds().contains(
				DIALOG_AND_WINDOW_ID));
		assertFalse(windowService.getActiveContextIds().contains(DIALOG_ID));
		assertTrue(windowService.getActiveContextIds().contains(WINDOW_ID));

		// switch to window active :-)
		window.activate();
		assertEquals(2, cs.getActiveContextIds().size());
		assertEquals(2, dialogService.getActiveContextIds().size());
		assertEquals(2, windowService.getActiveContextIds().size());

		assertTrue(cs.getActiveContextIds().contains(DIALOG_AND_WINDOW_ID));
		assertFalse(cs.getActiveContextIds().contains(DIALOG_ID));
		assertTrue(cs.getActiveContextIds().contains(WINDOW_ID));

		assertTrue(dialogService.getActiveContextIds().contains(
				DIALOG_AND_WINDOW_ID));
		assertTrue(dialogService.getActiveContextIds().contains(DIALOG_ID));
		assertFalse(dialogService.getActiveContextIds().contains(WINDOW_ID));

		assertTrue(windowService.getActiveContextIds().contains(
				DIALOG_AND_WINDOW_ID));
		assertFalse(windowService.getActiveContextIds().contains(DIALOG_ID));
		assertTrue(windowService.getActiveContextIds().contains(WINDOW_ID));
	}

	private void defineContexts(IEclipseContext appContext) {
		EContextService cs = (EContextService) appContext
				.get(EContextService.class.getName());
		Context daw = cs.getContext(DIALOG_AND_WINDOW_ID);
		daw.define("Dialog and Window", null, null);
		Context d = cs.getContext(DIALOG_ID);
		d.define("Dialog", null, DIALOG_AND_WINDOW_ID);
		Context w = cs.getContext(WINDOW_ID);
		w.define("Window", null, DIALOG_AND_WINDOW_ID);
	}

}
