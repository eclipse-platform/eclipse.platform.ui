/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 436344
 *******************************************************************************/
package org.eclipse.e4.ui.bindings.tests;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.CommandServiceAddon;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.bindings.BindingServiceAddon;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.services.ContextServiceAddon;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BindingCreateTest {
	private static final String DEFAULT_SCHEME_ID = "org.eclipse.ui.defaultAcceleratorConfiguration";
	private static final String ID_WINDOW = "org.eclipse.ui.contexts.window";

	private static final String TEST_ID1 = "test.id1";

	private IEclipseContext workbenchContext;
	private EBindingService bs;
	private ParameterizedCommand cmd;
	private TriggerSequence seq, emptySeq;
	private Map<String,String> emptyAttrs, schemeOnly, schemeAndTypeAttrs;

	@Before
	public void setUp() {
		IEclipseContext globalContext = Activator.getDefault()
				.getGlobalContext();
		workbenchContext = globalContext.createChild("workbenchContext");
		ContextInjectionFactory.make(CommandServiceAddon.class,
				workbenchContext);
		ContextInjectionFactory.make(ContextServiceAddon.class, workbenchContext);
		ContextInjectionFactory.make(BindingServiceAddon.class, workbenchContext);

		setupTestVars();
	}

	public void setupTestVars() {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		bs = (EBindingService) workbenchContext
				.get(EBindingService.class.getName());

		cmd = cs.createCommand(TEST_ID1, null);
		emptySeq = bs.createSequence("");
		seq = bs.createSequence("CTRL+5 T");

		emptyAttrs = new HashMap<>();

		schemeOnly = new HashMap<>();
		schemeOnly.put(EBindingService.SCHEME_ID_ATTR_TAG, DEFAULT_SCHEME_ID);

		schemeAndTypeAttrs = new HashMap<>();;
		schemeAndTypeAttrs.put(EBindingService.SCHEME_ID_ATTR_TAG, DEFAULT_SCHEME_ID);
		schemeAndTypeAttrs.put(EBindingService.TYPE_ATTR_TAG, "user");
	}

	@After
	public void tearDown() {
		workbenchContext.dispose();
		workbenchContext = null;
	}

	// *** TESTS *** //
	@Test
	public void testNullSequence() {
		Binding b = bs.createBinding(null, cmd, ID_WINDOW, schemeOnly);
		assertNull(b);
	}

	@Test
	public void testNullCommand() {
		// should still work since the binding manager in the keys pref
		// page will need some way to recognize unbound system bindings
		Binding b = bs.createBinding(seq, null, ID_WINDOW, schemeOnly);
		assertNotNull(b);
	}

	@Test
	public void testNullContext() {
		Binding b = bs.createBinding(seq, cmd, null, schemeOnly);
		assertNull(b);
	}

	@Test
	public void testNoAttrs() {
		Binding b = bs.createBinding(seq, cmd, ID_WINDOW, null);
		assertNotNull(b);
		assertTrue(seq.equals(b.getTriggerSequence()));
		assertTrue(cmd.equals(b.getParameterizedCommand()));
		assertTrue(ID_WINDOW.equals(b.getContextId()));
		assertNotNull(b.getSchemeId());
		assertTrue(DEFAULT_SCHEME_ID.equals(b.getSchemeId()));
		assertNull(b.getLocale());
		assertNull(b.getPlatform());
		assertTrue(b.getType() == Binding.SYSTEM);
	}

	@Test
	public void testEmptySequence() {
		Binding b = bs.createBinding(emptySeq, cmd, ID_WINDOW, null);
		assertNull(b);
	}

	@Test
	public void testBindingNoScheme() {
		Binding b = bs.createBinding(seq, cmd, ID_WINDOW, emptyAttrs);
		assertNotNull(b);
		assertTrue(seq.equals(b.getTriggerSequence()));
		assertTrue(cmd.equals(b.getParameterizedCommand()));
		assertTrue(ID_WINDOW.equals(b.getContextId()));
		assertNotNull(b.getSchemeId());
		assertTrue(DEFAULT_SCHEME_ID.equals(b.getSchemeId()));
		assertNull(b.getLocale());
		assertNull(b.getPlatform());
		assertTrue(b.getType() == Binding.SYSTEM);
	}

	@Test
	public void testSchemeonly() {
		Binding b = bs.createBinding(seq, cmd, ID_WINDOW, schemeOnly);
		assertNotNull(b);
		assertTrue(seq.equals(b.getTriggerSequence()));
		assertTrue(cmd.equals(b.getParameterizedCommand()));
		assertTrue(ID_WINDOW.equals(b.getContextId()));
		assertNotNull(b.getSchemeId());
		assertTrue(DEFAULT_SCHEME_ID.equals(b.getSchemeId()));
		assertNull(b.getLocale());
		assertNull(b.getPlatform());
		assertTrue(b.getType() == Binding.SYSTEM);
	}

	@Test
	public void testSchemeAndTypeAttrs() {
		Binding b = bs.createBinding(seq, cmd, ID_WINDOW, schemeAndTypeAttrs);
		assertNotNull(b);
		assertTrue(seq.equals(b.getTriggerSequence()));
		assertTrue(cmd.equals(b.getParameterizedCommand()));
		assertTrue(ID_WINDOW.equals(b.getContextId()));
		assertNotNull(b.getSchemeId());
		assertTrue(DEFAULT_SCHEME_ID.equals(b.getSchemeId()));
		assertNull(b.getLocale());
		assertNull(b.getPlatform());
		assertTrue(b.getType() == Binding.USER);
	}

}
