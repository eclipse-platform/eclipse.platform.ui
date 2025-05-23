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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440893
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 436344
 *******************************************************************************/
package org.eclipse.e4.ui.bindings.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.bindings.internal.BindingTable;
import org.eclipse.e4.ui.bindings.internal.BindingTableManager;
import org.eclipse.e4.ui.bindings.internal.ContextSet;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("restriction")
public class BindingTableTests {
	private static final String ID_DIALOG = "org.eclipse.ui.contexts.dialog";
	private static final String ID_DIALOG_AND_WINDOW = "org.eclipse.ui.contexts.dialogAndWindow";
	private static final String ID_WINDOW = "org.eclipse.ui.contexts.window";
	private static final String ID_TEXT = "org.eclipse.ui.textScope";
	private static final String ID_JAVA = "org.eclipse.jdt.ui.javaScope";
	private static final String ID_JS = "org.eclipse.wst.jsdt.ui.javaScriptScope";

	private static final String ABOUT_ID = "org.eclipse.ui.help.aboutAction";
	private static final String EXIT_ID = "org.eclipse.ui.file.exit";
	private static final String REFRESH_ID = "org.eclipse.ui.file.refresh";
	private static final String PASTE_ID = "org.eclipse.ui.edit.paste";
	private static final String CUT_ID = "org.eclipse.ui.edit.cut";
	private static final String COPY_ID = "org.eclipse.ui.edit.copy";
	private static final String TAG_ID = "org.eclipse.ui.text.wordCompletion";
	private static final String RENAME_ID = "org.eclipse.ui.file.rename";
	private static final String SHOW_TOOLTIP_ID = "org.eclipse.ui.text.showToolTip";
	private static final String CORR_INDENT_ID = "org.eclipse.jdt.ui.correctIndentation";
	private static final String INDENT_LINE_ID = "org.eclipse.wst.jsdt.ui.indentLine";

	static final String[] CONTEXTS = { ID_DIALOG_AND_WINDOW, "DAW", null,
			ID_DIALOG, "Dialog", ID_DIALOG_AND_WINDOW, ID_WINDOW, "Window",
			ID_DIALOG_AND_WINDOW, ID_TEXT, "Text Scope", ID_WINDOW, ID_JAVA,
			"Java scope", ID_TEXT, ID_JS, "JavaScript scope", ID_TEXT, };
	static final String[] ORDERED_IDS = { ID_DIALOG_AND_WINDOW, ID_DIALOG,
			ID_WINDOW, ID_TEXT, ID_JAVA, ID_JS, };

	static final String[] COMMANDS = { COPY_ID, "Copy", CUT_ID, "Cut",
			PASTE_ID, "Paste", REFRESH_ID, "Refresh", EXIT_ID, "Exit",
			ABOUT_ID, "About", TAG_ID, "Word Completion", RENAME_ID, "Rename",
			SHOW_TOOLTIP_ID, "Show Tooltip Description", CORR_INDENT_ID,
			"Correct Indentation", INDENT_LINE_ID, "Indent Line", };

	static final String[] BINDINGS = { COPY_ID, "M1+C", ID_WINDOW, CUT_ID,
			"M1+X", ID_WINDOW, PASTE_ID, "M1+V", ID_WINDOW, REFRESH_ID, "F5",
			ID_DIALOG_AND_WINDOW, EXIT_ID, "CTRL+Q", ID_DIALOG_AND_WINDOW,
			ABOUT_ID, "CTRL+5 A", ID_DIALOG_AND_WINDOW, TAG_ID, "ALT+/",
			ID_TEXT, RENAME_ID, "F2", ID_WINDOW, RENAME_ID, "M2+M3+R", ID_TEXT, SHOW_TOOLTIP_ID, "F2",
			ID_TEXT, CORR_INDENT_ID, "CTRL+I", ID_JAVA, INDENT_LINE_ID,
			"CTRL+I", ID_JS, PASTE_ID, "SHIFT+Insert", ID_WINDOW, PASTE_ID,
			"CTRL+5 V", ID_TEXT, };

	static ArrayList<Binding> loadedBindings = null;
	static CommandManager commandManager = null;
	static ContextManager contextManager = null;
	static IEclipseContext workbenchContext;
	private MApplication application;

	@BeforeEach
	public void setUp() throws Exception {
		IEclipseContext globalContext = TestUtil.getGlobalContext();
		workbenchContext = globalContext.createChild("workbenchContext");
		application = globalContext.get(MApplication.class);
		loadedBindings = new ArrayList<>();
		contextManager = new ContextManager();
		ContextSet.setComparator(new ContextSet.CComp(contextManager));
		for (int i = 0; i < CONTEXTS.length; i += 3) {
			Context context = contextManager.getContext(CONTEXTS[i]);
			context.define(CONTEXTS[i + 1], null, CONTEXTS[i + 2]);
		}

		commandManager = new CommandManager();
		Category category = commandManager.getCategory("bogus");
		category.define("Bogus", null);
		for (int i = 0; i < COMMANDS.length; i += 2) {
			Command cmd = commandManager.getCommand(COMMANDS[i]);
			cmd.define(COMMANDS[i + 1], null, category);
		}
		for (int i = 0; i < BINDINGS.length; i += 3) {
			KeySequence seq = KeySequence.getInstance(BINDINGS[i + 1]);
			Command cmd = commandManager.getCommand(BINDINGS[i]);
			loadedBindings.add(new KeyBinding(seq,
					new ParameterizedCommand(cmd, null),
					"org.eclipse.ui.defaultAcceleratorConfiguration",
					BINDINGS[i + 2], null, null, null, Binding.SYSTEM));
		}
	}

	@Test
	public void testOneTable() throws Exception {
		Binding about = getTestBinding(ABOUT_ID);
		KeySequence aboutSeq = KeySequence.getInstance("CTRL+5 A");
		KeySequence prefix = KeySequence.getInstance("CTRL+5");

		BindingTable table = loadTable(ID_DIALOG_AND_WINDOW);

		assertNotNull(about);
		Binding match = table.getPerfectMatch(aboutSeq);
		assertEquals(about, match);
		assertEquals(aboutSeq, table.getBestSequenceFor(about.getParameterizedCommand()).getTriggerSequence());

		Collection<Binding> sequences = table.getSequencesFor(about.getParameterizedCommand());
		assertEquals(1, sequences.size());
		assertEquals(aboutSeq, ((List<Binding>) sequences).get(0).getTriggerSequence());

		Collection<Binding> partialMatches = table.getPartialMatches(prefix);
		assertEquals(1, partialMatches.size());
		assertEquals(about, ((List<Binding>) partialMatches).get(0));
	}

	@Test
	public void testTwoKeysOneCommand() throws Exception {
		BindingTable table = loadTable(ID_WINDOW);
		Binding paste = getTestBinding(PASTE_ID);
		ParameterizedCommand pasteCmd = paste.getParameterizedCommand();
		KeySequence ctrlV = KeySequence.getInstance("M1+V");
		KeySequence shiftIns = KeySequence.getInstance("Shift+Insert");
		Binding match1 = table.getPerfectMatch(ctrlV);
		assertEquals(pasteCmd, match1.getParameterizedCommand());
		Binding match2 = table.getPerfectMatch(shiftIns);
		assertNotEquals(match1, match2);
		assertEquals(pasteCmd, match2.getParameterizedCommand());
	}

	@Test
	public void testLookupShortcut() {
		BindingTable table = loadTable(ID_WINDOW);
		Binding paste = getTestBinding(PASTE_ID);

		Binding match = table.getBestSequenceFor(paste.getParameterizedCommand());
		assertEquals(paste, match);
	}

	@Test
	public void testLookupShortcuts() throws Exception {
		BindingTable table = loadTable(ID_WINDOW);
		Binding paste = getTestBinding(PASTE_ID);

		Collection<Binding> sequences = table.getSequencesFor(paste.getParameterizedCommand());
		assertEquals(2, sequences.size());

		KeySequence second = KeySequence.getInstance("SHIFT+INSERT");
		Iterator<Binding> it = sequences.iterator();
		assertEquals(paste.getTriggerSequence(), it.next().getTriggerSequence());
		assertEquals(second, it.next().getTriggerSequence());
	}

	@Test
	public void testPartialMatch() throws Exception {
		BindingTable table = loadTable(ID_DIALOG_AND_WINDOW);
		KeySequence ctrl5 = KeySequence.getInstance("CTRL+5");
		KeySequence ctrl8 = KeySequence.getInstance("CTRL+8");
		assertTrue(table.isPartialMatch(ctrl5));
		assertFalse(table.isPartialMatch(ctrl8));
	}

	@Test
	public void testContextSet() {
		BindingTableManager manager = ContextInjectionFactory.make(BindingTableManager.class, workbenchContext);
		ArrayList<Context> window = new ArrayList<>();
		Context winContext = contextManager.getContext(ID_WINDOW);
		Context dawContext = contextManager.getContext(ID_DIALOG_AND_WINDOW);
		window.add(winContext);
		window.add(dawContext);
		ContextSet windowSet = manager.createContextSet(window);
		assertContextSet(windowSet, new String[] { ID_DIALOG_AND_WINDOW, ID_WINDOW });

		ArrayList<Context> text = new ArrayList<>(window);
		Context textContext = contextManager.getContext(ID_TEXT);
		text.add(textContext);
		ContextSet textSet = manager.createContextSet(text);
		assertContextSet(textSet, new String[] { ID_DIALOG_AND_WINDOW, ID_WINDOW, ID_TEXT });
	}

	@Test
	public void testContextSetSibling() {
		BindingTableManager manager = ContextInjectionFactory.make(BindingTableManager.class, workbenchContext);
		ArrayList<Context> all = new ArrayList<>();
		for (int i = 0; i < CONTEXTS.length; i += 3) {
			Context context = contextManager.getContext(CONTEXTS[i]);
			all.add(context);
		}
		ContextSet set = manager.createContextSet(all);
		assertContextSet(set, ORDERED_IDS);
	}

	@Test
	public void testSingleParentChainPerfectMatch() {
		BindingTableManager manager = ContextInjectionFactory.make(BindingTableManager.class, workbenchContext);

		manager.addTable(loadTable(ID_DIALOG_AND_WINDOW));
		manager.addTable(loadTable(ID_WINDOW));
		manager.addTable(loadTable(ID_TEXT));

		ArrayList<Context> window = new ArrayList<>();
		Context winContext = contextManager.getContext(ID_WINDOW);
		Context dawContext = contextManager.getContext(ID_DIALOG_AND_WINDOW);
		window.add(winContext);
		window.add(dawContext);
		ContextSet windowSet = manager.createContextSet(window);

		ArrayList<Context> text = new ArrayList<>(window);
		Context textContext = contextManager.getContext(ID_TEXT);
		text.add(textContext);
		ContextSet textSet = manager.createContextSet(text);

		Binding rename = getTestBinding(RENAME_ID);
		assertNotNull(rename);
		Binding showTooltip = getTestBinding(SHOW_TOOLTIP_ID);
		assertNotNull(showTooltip);

		Binding match = manager.getPerfectMatch(windowSet, rename.getTriggerSequence());
		assertEquals(rename, match);

		match = manager.getPerfectMatch(textSet, rename.getTriggerSequence());
		assertEquals(showTooltip, match);

		Binding about = getTestBinding(ABOUT_ID);
		match = manager.getPerfectMatch(textSet, about.getTriggerSequence());
		assertEquals(about, match);
	}

	@Test
	public void testSiblingsPerfectMatch() throws Exception {
		BindingTableManager manager = createManager();

		Binding correctIndent = getTestBinding(CORR_INDENT_ID);
		Binding indentLine = getTestBinding(INDENT_LINE_ID);

		assertEquals(correctIndent.getTriggerSequence(), indentLine.getTriggerSequence());
		ArrayList<Context> all = new ArrayList<>();
		for (int i = 0; i < CONTEXTS.length; i += 3) {
			Context context = contextManager.getContext(CONTEXTS[i]);
			all.add(context);
		}
		ContextSet set = manager.createContextSet(all);
		Binding match = manager.getPerfectMatch(set, correctIndent.getTriggerSequence());
		assertEquals(indentLine, match);
	}

	@Test
	public void testOneSiblingAtATimePerfectMatch() throws Exception {
		BindingTableManager manager = createManager();

		Binding correctIndent = getTestBinding(CORR_INDENT_ID);
		Binding indentLine = getTestBinding(INDENT_LINE_ID);
		assertEquals(correctIndent.getTriggerSequence(), indentLine.getTriggerSequence());

		ContextSet javaSet = createJavaSet(manager);

		ArrayList<Context> jsList = new ArrayList<>();
		for (int i = 0; i < CONTEXTS.length; i += 3) {
			Context context = contextManager.getContext(CONTEXTS[i]);
			if (!ID_JAVA.equals(context.getId())) {
				jsList.add(context);
			}
		}
		ContextSet jsSet = manager.createContextSet(jsList);

		Binding match = manager.getPerfectMatch(javaSet, correctIndent.getTriggerSequence());
		assertEquals(correctIndent, match);

		match = manager.getPerfectMatch(jsSet, correctIndent.getTriggerSequence());
		assertEquals(indentLine, match);
	}

	@Test
	public void testManagerLookupShortcut() throws Exception {
		BindingTableManager manager = createManager();
		Binding paste = getTestBinding(PASTE_ID);

		ArrayList<Context> window = new ArrayList<>();
		Context winContext = contextManager.getContext(ID_WINDOW);
		Context dawContext = contextManager.getContext(ID_DIALOG_AND_WINDOW);
		window.add(winContext);
		window.add(dawContext);
		ContextSet windowSet = manager.createContextSet(window);

		Binding match = manager.getBestSequenceFor(windowSet, paste.getParameterizedCommand());
		assertEquals(paste, match);
	}

	@Test
	public void testManagerLookupShortcutDeeperContext() throws Exception {
		BindingTableManager manager = createManager();
		// Given a BindingTableManager with default active scheme and context manager:
		manager.setActiveSchemes(new String[] { "org.eclipse.ui.defaultAcceleratorConfiguration" }, contextManager);
		Binding paste = getTestBinding(RENAME_ID, ID_TEXT);
		assertNotNull(paste);

		ArrayList<Context> window = new ArrayList<>();
		Context winContext = contextManager.getContext(ID_WINDOW);
		Context dawContext = contextManager.getContext(ID_TEXT);
		window.add(winContext);
		window.add(dawContext);
		ContextSet windowSet = manager.createContextSet(window);
		// When getting the best sequence for the paste command:
		Binding match = manager.getBestSequenceFor(windowSet, paste.getParameterizedCommand());
		// Then the paste binding with the ID_TEXT context should be returned, because
		// it's more specific than the ID_WINDOW context:
		assertEquals(paste, match);

		// disable context manager in BindingComparator for other tests:
		manager.setActiveSchemes(null, null);
	}

	@Test
	public void testManagerLookupShortcutLongChain() throws Exception {
		BindingTableManager manager = createManager();
		Binding paste = getTestBinding(PASTE_ID);

		ContextSet javaSet = createJavaSet(manager);

		Binding match = manager.getBestSequenceFor(javaSet, paste.getParameterizedCommand());
		assertEquals(paste, match);
	}

	@Test
	public void testManagerLookupAllShortcuts() throws Exception {
		BindingTableManager manager = createManager();
		Binding paste = getTestBinding(PASTE_ID);

		ContextSet javaSet = createJavaSet(manager);

		Collection<Binding> sequences = manager.getSequencesFor(javaSet, paste.getParameterizedCommand());
		assertEquals(3, sequences.size());

		KeySequence second = KeySequence.getInstance("SHIFT+INSERT");
		KeySequence third = KeySequence.getInstance("CTRL+5 V");
		Iterator<Binding> it = sequences.iterator();
		assertEquals(paste.getTriggerSequence(), it.next().getTriggerSequence());
		assertEquals(second, it.next().getTriggerSequence());
		assertEquals(third, it.next().getTriggerSequence());
	}

	@Test
	public void testManagerPartialMatch() throws Exception {
		BindingTableManager manager = createManager();
		Binding about = getTestBinding(ABOUT_ID);
		Binding paste = getTestBinding(PASTE_ID);
		ContextSet javaSet = createJavaSet(manager);
		Binding pasteCtrl5 = manager.getPerfectMatch(javaSet, KeySequence.getInstance("CTRL+5 V"));
		assertEquals(paste.getParameterizedCommand(), pasteCtrl5.getParameterizedCommand());

		KeySequence ctrl5 = KeySequence.getInstance("CTRL+5");
		KeySequence ctrl8 = KeySequence.getInstance("CTRL+8");
		assertTrue(manager.isPartialMatch(javaSet, ctrl5));
		assertFalse(manager.isPartialMatch(javaSet, ctrl8));

		Collection<Binding> partialMatches = manager.getPartialMatches(javaSet, ctrl5);
		assertEquals(2, partialMatches.size());
		Iterator<Binding> it = partialMatches.iterator();
		assertEquals(pasteCtrl5, it.next());
		assertEquals(about, it.next());
	}

	private BindingTable loadTable(String contextId) {
		Context context = contextManager.getContext(contextId);
		BindingTable table = new BindingTable(context, application);
		for (Binding b : loadedBindings) {
			if (context.getId().equals(b.getContextId())) {
				table.addBinding(b);
			}
		}
		return table;
	}

	private Binding getTestBinding(String commandId) {
		for (Binding binding : loadedBindings) {
			if (commandId.equals(binding.getParameterizedCommand().getId())) {
				return binding;
			}
		}
		return null;
	}

	private Binding getTestBinding(String commandId, String contextId) {
		for (Binding binding : loadedBindings) {
			if (commandId.equals(binding.getParameterizedCommand().getId())
					&& contextId.equals(binding.getContextId())) {
				return binding;
			}
		}
		return null;
	}

	private void assertContextSet(ContextSet set, String[] contextIds) {
		List<Context> contexts = set.getContexts();
		assertEquals(contextIds.length, contexts.size(), contexts.toString());
		for (int i = 0; i < contextIds.length; i++) {
			assertEquals(contextIds[i], contexts.get(i).getId());
		}
	}

	private BindingTableManager createManager() throws Exception {
		BindingTableManager manager = ContextInjectionFactory.make(BindingTableManager.class, workbenchContext);

		for (int i = 0; i < CONTEXTS.length; i += 3) {
			manager.addTable(loadTable(CONTEXTS[i]));
		}
		return manager;
	}

	private ContextSet createJavaSet(BindingTableManager manager) {
		ArrayList<Context> javaList = new ArrayList<>();
		for (int i = 0; i < CONTEXTS.length; i += 3) {
			if (CONTEXTS[i].equals(ID_JS) || CONTEXTS[i].equals(ID_DIALOG)) {
				continue;
			}
			Context context = contextManager.getContext(CONTEXTS[i]);
			javaList.add(context);
		}
		return manager.createContextSet(javaList);
	}
}
