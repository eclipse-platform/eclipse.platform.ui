/*******************************************************************************
 * Copyright (c) 2013, 2023 IBM Corporation and others.
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
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 436344
 *******************************************************************************/
package org.eclipse.e4.ui.bindings.tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

@SuppressWarnings("restriction")
public class BindingCreateTest {
	private static final String DEFAULT_SCHEME_ID = "org.eclipse.ui.defaultAcceleratorConfiguration";
	private static final String ID_WINDOW = "org.eclipse.ui.contexts.window";

	private static final String TEST_ID1 = "test.id1";

	private IEclipseContext workbenchContext;
	private EBindingService bs;
	private ParameterizedCommand cmd;
	private TriggerSequence seq, emptySeq;
	private Map<String,String> emptyAttrs, schemeOnly, schemeAndTypeAttrs;

	@BeforeEach
	public void setUp() {
		IEclipseContext globalContext = TestUtil.getGlobalContext();
		workbenchContext = globalContext.createChild("workbenchContext");
		ContextInjectionFactory.make(CommandServiceAddon.class, workbenchContext);
		ContextInjectionFactory.make(ContextServiceAddon.class, workbenchContext);
		ContextInjectionFactory.make(BindingServiceAddon.class, workbenchContext);

		setupTestVars();
	}


	public void setupTestVars() {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		bs = workbenchContext.get(EBindingService.class);

		cmd = cs.createCommand(TEST_ID1, null);
		emptySeq = bs.createSequence("");
		seq = bs.createSequence("CTRL+5 T");

		emptyAttrs = new HashMap<>();

		schemeOnly = new HashMap<>();
		schemeOnly.put(EBindingService.SCHEME_ID_ATTR_TAG, DEFAULT_SCHEME_ID);

		schemeAndTypeAttrs = new HashMap<>();
		schemeAndTypeAttrs.put(EBindingService.SCHEME_ID_ATTR_TAG, DEFAULT_SCHEME_ID);
		schemeAndTypeAttrs.put(EBindingService.TYPE_ATTR_TAG, "user");
	}

	@AfterEach
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
		assertEquals(seq, b.getTriggerSequence());
		assertEquals(cmd, b.getParameterizedCommand());
		assertEquals(ID_WINDOW, b.getContextId());
		assertNotNull(b.getSchemeId());
		assertEquals(DEFAULT_SCHEME_ID, b.getSchemeId());
		assertNull(b.getLocale());
		assertNull(b.getPlatform());
		assertEquals(Binding.SYSTEM, b.getType());
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
		assertEquals(seq, b.getTriggerSequence());
		assertEquals(cmd, b.getParameterizedCommand());
		assertEquals(ID_WINDOW, b.getContextId());
		assertNotNull(b.getSchemeId());
		assertEquals(DEFAULT_SCHEME_ID, b.getSchemeId());
		assertNull(b.getLocale());
		assertNull(b.getPlatform());
		assertEquals(Binding.SYSTEM, b.getType());
	}

	@Test
	public void testSchemeonly() {
		Binding b = bs.createBinding(seq, cmd, ID_WINDOW, schemeOnly);
		assertNotNull(b);
		assertEquals(seq, b.getTriggerSequence());
		assertEquals(cmd, b.getParameterizedCommand());
		assertEquals(ID_WINDOW, b.getContextId());
		assertNotNull(b.getSchemeId());
		assertEquals(DEFAULT_SCHEME_ID, b.getSchemeId());
		assertNull(b.getLocale());
		assertNull(b.getPlatform());
		assertEquals(Binding.SYSTEM, b.getType());
	}

	@Test
	public void testSchemeAndTypeAttrs() {
		Binding b = bs.createBinding(seq, cmd, ID_WINDOW, schemeAndTypeAttrs);
		assertNotNull(b);
		assertEquals(seq, b.getTriggerSequence());
		assertEquals(cmd, b.getParameterizedCommand());
		assertEquals(ID_WINDOW, b.getContextId());
		assertNotNull(b.getSchemeId());
		assertEquals(DEFAULT_SCHEME_ID, b.getSchemeId());
		assertNull(b.getLocale());
		assertNull(b.getPlatform());
		assertEquals(Binding.USER, b.getType());
	}

}
