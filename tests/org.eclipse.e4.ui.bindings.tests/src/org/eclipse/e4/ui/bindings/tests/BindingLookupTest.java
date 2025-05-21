/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.e4.core.commands.CommandServiceAddon;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.bindings.BindingServiceAddon;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.internal.BindingTable;
import org.eclipse.e4.ui.bindings.internal.BindingTableManager;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.ContextServiceAddon;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("restriction")
public class BindingLookupTest {
	private static final String ID_DIALOG = "org.eclipse.ui.contexts.dialog";
	private static final String ID_DIALOG_AND_WINDOW = "org.eclipse.ui.contexts.dialogAndWindow";
	private static final String ID_WINDOW = "org.eclipse.ui.contexts.window";
	private static final String ID_TEXT = "org.eclipse.ui.textScope";
	private static final String ID_JAVA = "org.eclipse.jdt.ui.javaScope";
	private static final String ID_JS = "org.eclipse.wst.jsdt.ui.javaScriptScope";

	static final String[] CONTEXTS = { ID_DIALOG_AND_WINDOW, "DAW", null,
			ID_DIALOG, "Dialog", ID_DIALOG_AND_WINDOW, ID_WINDOW, "Window",
			ID_DIALOG_AND_WINDOW, ID_TEXT, "Text Scope", ID_WINDOW, ID_JAVA,
			"Java scope", ID_TEXT, ID_JS, "JavaScript scope", ID_TEXT, };

	private static final String TEST_CAT1 = "test.cat1";
	private static final String TEST_ID1 = "test.id1";
	private static final String TEST_ID2 = "test.id2";

	private IEclipseContext workbenchContext;
	private MApplication application;

	private void defineCommands(IEclipseContext context) {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		Category category = cs.defineCategory(TEST_CAT1, "CAT1", null);
		cs.defineCommand(TEST_ID1, "ID1", null, category, null);
		cs.defineCommand(TEST_ID2, "ID2", null, category, null);
	}

	@BeforeEach
	public void setUp() {
		IEclipseContext globalContext = TestUtil.getGlobalContext();
		workbenchContext = globalContext.createChild("workbenchContext");
		ContextInjectionFactory.make(CommandServiceAddon.class, workbenchContext);
		ContextInjectionFactory.make(ContextServiceAddon.class, workbenchContext);
		ContextInjectionFactory.make(BindingServiceAddon.class, workbenchContext);
		application = globalContext.get(MApplication.class);
		defineCommands(workbenchContext);
		defineContexts(workbenchContext);
		defineBindingTables(workbenchContext);
	}

	private void defineContexts(IEclipseContext context) {
		ContextManager contextManager = context.get(ContextManager.class);
		for (int i = 0; i < CONTEXTS.length; i += 3) {
			Context c = contextManager.getContext(CONTEXTS[i]);
			c.define(CONTEXTS[i + 1], null, CONTEXTS[i + 2]);
		}

		EContextService cs = context
				.get(EContextService.class);
		cs.activateContext(ID_DIALOG_AND_WINDOW);
	}

	private void defineBindingTables(IEclipseContext context) {
		BindingTableManager btm = context.get(BindingTableManager.class);
		ContextManager cm = context.get(ContextManager.class);
		btm.addTable(new BindingTable(cm.getContext(ID_DIALOG_AND_WINDOW), application));
		btm.addTable(new BindingTable(cm.getContext(ID_WINDOW), application));
		btm.addTable(new BindingTable(cm.getContext(ID_DIALOG), application));
	}

	@AfterEach
	public void tearDown() {
		workbenchContext.dispose();
		workbenchContext = null;
	}

	@Test
	public void testFindBinding() {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EBindingService bs = workbenchContext.get(EBindingService.class);
		TriggerSequence seq = bs.createSequence("CTRL+5 T");
		Binding db = createDefaultBinding(bs, seq, cmd, ID_DIALOG_AND_WINDOW);
		bs.activateBinding(db);
		Binding perfectMatch = bs.getPerfectMatch(seq);
		assertEquals(cmd, perfectMatch.getParameterizedCommand());
		bs.deactivateBinding(db);
		assertNull(bs.getPerfectMatch(seq));

		bs.activateBinding(db);
		assertEquals(cmd, bs.getPerfectMatch(seq).getParameterizedCommand());
	}

	@Test
	public void testFindBindingInParent_Bug562263() {

		EContextService es = workbenchContext.get(EContextService.class);
		es.deactivateContext(ID_DIALOG_AND_WINDOW);
		es.activateContext(ID_WINDOW);

		ECommandService cs = workbenchContext.get(ECommandService.class);
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EBindingService bs = workbenchContext.get(EBindingService.class);
		TriggerSequence seq = bs.createSequence("CTRL+5 T");
		Binding db = createDefaultBinding(bs, seq, cmd, ID_DIALOG_AND_WINDOW);
		bs.activateBinding(db);
		Binding perfectMatch = bs.getPerfectMatch(seq);
		assertEquals(cmd, perfectMatch.getParameterizedCommand());
		bs.deactivateBinding(db);
		assertNull(bs.getPerfectMatch(seq));

		bs.activateBinding(db);
		assertEquals(cmd, bs.getPerfectMatch(seq).getParameterizedCommand());
	}

	@Test
	public void testMultipleBindings() {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EBindingService bs = workbenchContext.get(EBindingService.class);
		TriggerSequence seq = bs.createSequence("CTRL+5 T");
		TriggerSequence seq2 = bs.createSequence("CTRL+2 X");
		Binding db = createDefaultBinding(bs, seq, cmd, ID_DIALOG_AND_WINDOW);
		bs.activateBinding(db);
		db = createDefaultBinding(bs, seq2, cmd, ID_DIALOG_AND_WINDOW);
		bs.activateBinding(db);

		assertEquals(cmd, bs.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd, bs.getPerfectMatch(seq2).getParameterizedCommand());
	}

	@Test
	public void testLookupChildBinding() {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		IEclipseContext c1 = workbenchContext.createChild("c1");
		c1.activate();

		EBindingService bs1 = c1.get(EBindingService.class);
		TriggerSequence seq = bs1.createSequence("CTRL+5 T");
		Binding db = createDefaultBinding(bs1, seq, cmd, ID_DIALOG_AND_WINDOW);
		bs1.activateBinding(db);
		EBindingService wBS = workbenchContext.get(EBindingService.class);
		assertEquals(cmd, wBS.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd, bs1.getPerfectMatch(seq).getParameterizedCommand());

		bs1.deactivateBinding(db);
		assertNull(wBS.getPerfectMatch(seq));
		assertNull(bs1.getPerfectMatch(seq));
	}

	@Test
	public void testLookupWithTwoChildren() {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		ParameterizedCommand cmd1 = cs.createCommand(TEST_ID1, null);
		ParameterizedCommand cmd2 = cs.createCommand(TEST_ID2, null);

		EBindingService wBS = workbenchContext.get(EBindingService.class);
		TriggerSequence seq = wBS.createSequence("CTRL+5 T");

		IEclipseContext c1 = workbenchContext.createChild("c1");
		c1.activate();
		EContextService es = c1.get(EContextService.class);
		es.activateContext(ID_WINDOW);

		EBindingService bs1 = c1.get(EBindingService.class);
		Binding db = createDefaultBinding(bs1, seq, cmd1, ID_WINDOW);
		bs1.activateBinding(db);

		IEclipseContext c2 = workbenchContext.createChild("c2");
		EContextService es2 = c2.get(EContextService.class);
		es2.activateContext(ID_DIALOG);

		EBindingService bs2 = c2.get(EBindingService.class);
		db = createDefaultBinding(bs1, seq, cmd2, ID_DIALOG);
		bs2.activateBinding(db);

		assertEquals(cmd1, wBS.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd1, bs1.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd2, bs2.getPerfectMatch(seq).getParameterizedCommand());
	}

	@Test
	public void testLookupWithDifferentActiveChild() {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		ParameterizedCommand cmd1 = cs.createCommand(TEST_ID1, null);
		ParameterizedCommand cmd2 = cs.createCommand(TEST_ID2, null);

		EBindingService wBS = workbenchContext
				.get(EBindingService.class);
		TriggerSequence seq = wBS.createSequence("CTRL+5 T");

		IEclipseContext c1 = workbenchContext.createChild("c1");
		c1.activate();
		EContextService es = c1.get(EContextService.class);
		es.activateContext(ID_WINDOW);

		EBindingService bs1 = c1.get(EBindingService.class);
		Binding db = createDefaultBinding(bs1, seq, cmd1, ID_WINDOW);
		bs1.activateBinding(db);

		IEclipseContext c2 = workbenchContext.createChild("c2");
		EContextService es2 = c2.get(EContextService.class);
		es2.activateContext(ID_DIALOG);

		EBindingService bs2 = c2.get(EBindingService.class);
		db = createDefaultBinding(bs1, seq, cmd2, ID_DIALOG);
		bs2.activateBinding(db);

		assertEquals(cmd1, bs1.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd2, bs2.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd1, wBS.getPerfectMatch(seq).getParameterizedCommand());

		c2.activate();
		assertEquals(cmd1, bs1.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd2, bs2.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd2, wBS.getPerfectMatch(seq).getParameterizedCommand());

		c1.activate();
		assertEquals(cmd1, bs1.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd2, bs2.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd1, wBS.getPerfectMatch(seq).getParameterizedCommand());

		c2.activate();
		assertEquals(cmd1, bs1.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd2, bs2.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd2, wBS.getPerfectMatch(seq).getParameterizedCommand());

		assertTrue(wBS.isPerfectMatch(seq));
	}

	@Test
	public void testLookupShortcut() {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EBindingService bs = workbenchContext.get(EBindingService.class);
		TriggerSequence seq = bs.createSequence("CTRL+5 T");
		Binding db = createDefaultBinding(bs, seq, cmd, ID_DIALOG_AND_WINDOW);
		bs.activateBinding(db);

		assertEquals(seq, bs.getBestSequenceFor(cmd));
	}

	@Test
	public void testLookupShortcuts() {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EBindingService bs = workbenchContext.get(EBindingService.class);
		TriggerSequence seq = bs.createSequence("CTRL+5 T");
		TriggerSequence seq2 = bs.createSequence("CTRL+2 X");
		Binding db = createDefaultBinding(bs, seq, cmd, ID_DIALOG_AND_WINDOW);
		bs.activateBinding(db);
		Binding db2 = createDefaultBinding(bs, seq2, cmd, ID_DIALOG_AND_WINDOW);
		bs.activateBinding(db2);

		TriggerSequence foundSequence = bs.getBestSequenceFor(cmd);
		assertNotNull(foundSequence);
		assertEquals(seq, foundSequence);
	}

	@Test
	public void testLookupBestShortcut() {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EBindingService bs = workbenchContext.get(EBindingService.class);
		TriggerSequence seq2 = bs.createSequence("ALT+5 X");
		Binding db2 = createDefaultBinding(bs, seq2, cmd, ID_DIALOG_AND_WINDOW);
		bs.activateBinding(db2);

		TriggerSequence seq = bs.createSequence("CTRL+5 T");
		Binding db = createDefaultBinding(bs, seq, cmd, ID_DIALOG_AND_WINDOW);
		bs.activateBinding(db);

		TriggerSequence foundSequence = bs.getBestSequenceFor(cmd);
		assertNotNull(foundSequence);
		assertEquals(seq, foundSequence);
	}

	@Test
	public void testLookupBestShortcutWithChild() {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EBindingService bs = workbenchContext.get(EBindingService.class);
		TriggerSequence seq2 = bs.createSequence("CTRL+5 T");
		Binding db2 = createDefaultBinding(bs, seq2, cmd, ID_DIALOG_AND_WINDOW);
		bs.activateBinding(db2);

		IEclipseContext c1 = workbenchContext.createChild("c1");
		c1.activate();
		EBindingService bs1 = c1.get(EBindingService.class);

		TriggerSequence seq = bs1.createSequence("ALT+5 X");
		Binding db = createDefaultBinding(bs, seq, cmd, ID_DIALOG_AND_WINDOW);
		bs1.activateBinding(db);

		TriggerSequence foundSequence = bs.getBestSequenceFor(cmd);
		assertNotNull(foundSequence);
		assertEquals(seq2, foundSequence);
	}

	@Test
	public void testLookupShortcutsTwoChildren() {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		ParameterizedCommand cmd1 = cs.createCommand(TEST_ID1, null);
		ParameterizedCommand cmd2 = cs.createCommand(TEST_ID2, null);

		EBindingService wBS = workbenchContext.get(EBindingService.class);
		TriggerSequence seq = wBS.createSequence("CTRL+5 T");

		IEclipseContext c1 = workbenchContext.createChild("c1");
		c1.activate();
		EContextService es = c1.get(EContextService.class);
		es.activateContext(ID_WINDOW);

		EBindingService bs1 = c1.get(EBindingService.class);
		Binding db = createDefaultBinding(bs1, seq, cmd1, ID_WINDOW);
		bs1.activateBinding(db);

		IEclipseContext c2 = workbenchContext.createChild("c2");
		EContextService es2 = c2.get(EContextService.class);
		es2.activateContext(ID_DIALOG);

		EBindingService bs2 = c2.get(EBindingService.class);
		Binding db2 = createDefaultBinding(bs2, seq, cmd2, ID_DIALOG);
		bs2.activateBinding(db2);

		assertEquals(seq, wBS.getBestSequenceFor(cmd1));
		assertNull(wBS.getBestSequenceFor(cmd2));

		assertEquals(seq, bs1.getBestSequenceFor(cmd1));
		assertNull(bs1.getBestSequenceFor(cmd2));

		assertEquals(seq, bs2.getBestSequenceFor(cmd2));
		assertNull(bs2.getBestSequenceFor(cmd1));
	}

	@Test
	public void testLookupAllShortcuts() {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EBindingService bs = workbenchContext.get(EBindingService.class);
		TriggerSequence seq2 = bs.createSequence("ALT+5 X");
		Binding db2 = createDefaultBinding(bs, seq2, cmd, ID_DIALOG_AND_WINDOW);
		bs.activateBinding(db2);

		TriggerSequence seq = bs.createSequence("CTRL+5 T");
		Binding db = createDefaultBinding(bs, seq, cmd, ID_DIALOG_AND_WINDOW);
		bs.activateBinding(db);

		// the list will always be ordered
		ArrayList<TriggerSequence> list = new ArrayList<>();
		list.add(seq);
		list.add(seq2);

		assertEquals(list, bs.getSequencesFor(cmd));
	}

	@Test
	public void testLookupAllShortcutsWithChild() {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);

		EBindingService wBS = workbenchContext.get(EBindingService.class);
		TriggerSequence seq2 = wBS.createSequence("ALT+5 X");
		Binding db2 = createDefaultBinding(wBS, seq2, cmd, ID_DIALOG_AND_WINDOW);
		wBS.activateBinding(db2);

		IEclipseContext c1 = workbenchContext.createChild("c1");
		c1.activate();
		EContextService es = c1.get(EContextService.class);
		es.activateContext(ID_WINDOW);
		EBindingService bs1 = c1.get(EBindingService.class);

		TriggerSequence seq = bs1.createSequence("CTRL+5 T");
		Binding db = createDefaultBinding(wBS, seq, cmd, ID_WINDOW);
		bs1.activateBinding(db);

		// the list will always be ordered
		ArrayList<TriggerSequence> list = new ArrayList<>();
		list.add(seq);
		list.add(seq2);

		assertEquals(list, wBS.getSequencesFor(cmd));
	}

	@Test
	public void testPartialMatch() {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);

		EBindingService wBS = workbenchContext.get(EBindingService.class);
		TriggerSequence seq2 = wBS.createSequence("ALT+5 X");
		Binding db2 = createDefaultBinding(wBS, seq2, cmd, ID_DIALOG_AND_WINDOW);
		wBS.activateBinding(db2);

		IEclipseContext c1 = workbenchContext.createChild("c1");
		c1.activate();
		EBindingService bs1 = c1.get(EBindingService.class);

		TriggerSequence seq = bs1.createSequence("CTRL+5 T");
		Binding db = createDefaultBinding(wBS, seq, cmd, ID_DIALOG_AND_WINDOW);
		bs1.activateBinding(db);

		TriggerSequence partialMatch = bs1.createSequence("CTRL+5");
		TriggerSequence partialNoMatch = bs1.createSequence("CTRL+8");
		assertFalse(bs1.isPartialMatch(partialNoMatch));
		assertTrue(bs1.isPartialMatch(partialMatch));
	}

	@Test
	public void testGetPartialMatches() {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		ParameterizedCommand cmd2 = cs.createCommand(TEST_ID2, null);

		EBindingService wBS = workbenchContext.get(EBindingService.class);
		TriggerSequence seq2 = wBS.createSequence("ALT+5 X");
		Binding wbBind = createDefaultBinding(wBS, seq2, cmd, ID_DIALOG_AND_WINDOW);
		wBS.activateBinding(wbBind);

		IEclipseContext c1 = workbenchContext.createChild("c1");
		c1.activate();
		EBindingService bs1 = c1.get(EBindingService.class);

		TriggerSequence seq = bs1.createSequence("CTRL+5 T");
		Binding b1 = createDefaultBinding(wBS, seq, cmd, ID_DIALOG_AND_WINDOW);
		bs1.activateBinding(b1);
		TriggerSequence sseq = bs1.createSequence("CTRL+5 Y");
		Binding b2 = createDefaultBinding(bs1, sseq, cmd2, ID_DIALOG_AND_WINDOW);
		bs1.activateBinding(b2);
		ArrayList<Binding> commandMatches = new ArrayList<>();
		commandMatches.add(b1);
		commandMatches.add(b2);

		TriggerSequence partialMatch = bs1.createSequence("CTRL+5");
		TriggerSequence partialNoMatch = bs1.createSequence("CTRL+8");
		assertFalse(bs1.isPartialMatch(partialNoMatch));
		assertTrue(bs1.isPartialMatch(partialMatch));

		Collection<Binding> matches = bs1.getPartialMatches(partialMatch);
		assertEquals(commandMatches, matches);
	}

	private Binding createDefaultBinding(EBindingService bs,
			TriggerSequence sequence, ParameterizedCommand command,
			String contextId) {
		Map<String, String> attrs = new HashMap<>();
		attrs.put("schemeId", "org.eclipse.ui.defaultAcceleratorConfiguration");

		return bs.createBinding(sequence, command, contextId, attrs);
	}

}
