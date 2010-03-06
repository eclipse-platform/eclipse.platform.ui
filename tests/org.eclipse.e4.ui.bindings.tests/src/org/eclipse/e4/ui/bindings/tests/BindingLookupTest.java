package org.eclipse.e4.ui.bindings.tests;

import java.util.Collection;
import java.util.HashSet;

import junit.framework.TestCase;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;

public class BindingLookupTest extends TestCase {
	private static final String TEST_CAT1 = "test.cat1";
	private static final String TEST_ID1 = "test.id1";
	private static final String TEST_ID2 = "test.id2";

	private IEclipseContext workbenchContext;

	private IEclipseContext createWorkbenchContext(IEclipseContext globalContext) {
		IEclipseContext wb = TestUtil.createContext(globalContext,
				"workbenchContext");
		return wb;
	}

	private void defineCommands(IEclipseContext context) {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		Category category = cs.defineCategory(TEST_CAT1, "CAT1", null);
		cs.defineCommand(TEST_ID1, "ID1", null, category, null);
		cs.defineCommand(TEST_ID2, "ID2", null, category, null);
	}

	@Override
	protected void setUp() throws Exception {
		workbenchContext = createWorkbenchContext(Activator.getDefault()
				.getGlobalContext());
		defineCommands(workbenchContext);
	}

	@Override
	protected void tearDown() throws Exception {
		if (workbenchContext instanceof IDisposable) {
			((IDisposable) workbenchContext).dispose();
		}
		workbenchContext = null;
	}

	public void testFindBinding() throws Exception {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EBindingService bs = (EBindingService) workbenchContext
				.get(EBindingService.class.getName());
		TriggerSequence seq = bs.createSequence("CTRL+5 T");
		bs.activateBinding(seq, cmd);
		Binding perfectMatch = bs.getPerfectMatch(seq);
		assertEquals(cmd, perfectMatch.getParameterizedCommand());
		bs.deactivateBinding(seq, cmd);
		assertNull(bs.getPerfectMatch(seq));

		bs.activateBinding(seq, cmd);
		assertEquals(cmd, bs.getPerfectMatch(seq).getParameterizedCommand());
	}

	public void testMultipleBindings() throws Exception {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EBindingService bs = (EBindingService) workbenchContext
				.get(EBindingService.class.getName());
		TriggerSequence seq = bs.createSequence("CTRL+5 T");
		TriggerSequence seq2 = bs.createSequence("CTRL+2 X");
		bs.activateBinding(seq, cmd);
		bs.activateBinding(seq2, cmd);

		assertEquals(cmd, bs.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd, bs.getPerfectMatch(seq2).getParameterizedCommand());
	}

	public void testLookupChildBinding() throws Exception {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		IEclipseContext c1 = TestUtil.createContext(workbenchContext, "c1");
		workbenchContext.set(IContextConstants.ACTIVE_CHILD, c1);

		EBindingService bs1 = (EBindingService) c1.get(EBindingService.class
				.getName());
		TriggerSequence seq = bs1.createSequence("CTRL+5 T");
		bs1.activateBinding(seq, cmd);
		EBindingService wBS = (EBindingService) workbenchContext
				.get(EBindingService.class.getName());
		assertEquals(cmd, wBS.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd, bs1.getPerfectMatch(seq).getParameterizedCommand());

		bs1.deactivateBinding(seq, cmd);
		assertNull(wBS.getPerfectMatch(seq));
		assertNull(bs1.getPerfectMatch(seq));
	}

	public void testLookupWithTwoChildren() throws Exception {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd1 = cs.createCommand(TEST_ID1, null);
		ParameterizedCommand cmd2 = cs.createCommand(TEST_ID2, null);

		EBindingService wBS = (EBindingService) workbenchContext
				.get(EBindingService.class.getName());
		TriggerSequence seq = wBS.createSequence("CTRL+5 T");

		IEclipseContext c1 = TestUtil.createContext(workbenchContext, "c1");
		workbenchContext.set(IContextConstants.ACTIVE_CHILD, c1);

		EBindingService bs1 = (EBindingService) c1.get(EBindingService.class
				.getName());
		bs1.activateBinding(seq, cmd1);

		IEclipseContext c2 = TestUtil.createContext(workbenchContext, "c2");

		EBindingService bs2 = (EBindingService) c2.get(EBindingService.class
				.getName());
		bs2.activateBinding(seq, cmd2);

		assertEquals(cmd1, wBS.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd1, bs1.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd2, bs2.getPerfectMatch(seq).getParameterizedCommand());
	}

	public void testLookupWithDifferentActiveChild() throws Exception {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd1 = cs.createCommand(TEST_ID1, null);
		ParameterizedCommand cmd2 = cs.createCommand(TEST_ID2, null);

		EBindingService wBS = (EBindingService) workbenchContext
				.get(EBindingService.class.getName());
		TriggerSequence seq = wBS.createSequence("CTRL+5 T");

		IEclipseContext c1 = TestUtil.createContext(workbenchContext, "c1");
		workbenchContext.set(IContextConstants.ACTIVE_CHILD, c1);

		EBindingService bs1 = (EBindingService) c1.get(EBindingService.class
				.getName());
		bs1.activateBinding(seq, cmd1);

		IEclipseContext c2 = TestUtil.createContext(workbenchContext, "c2");

		EBindingService bs2 = (EBindingService) c2.get(EBindingService.class
				.getName());
		bs2.activateBinding(seq, cmd2);

		assertEquals(cmd1, bs1.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd2, bs2.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd1, wBS.getPerfectMatch(seq).getParameterizedCommand());

		workbenchContext.set(IContextConstants.ACTIVE_CHILD, c2);
		assertEquals(cmd1, bs1.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd2, bs2.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd2, wBS.getPerfectMatch(seq).getParameterizedCommand());

		workbenchContext.set(IContextConstants.ACTIVE_CHILD, c1);
		assertEquals(cmd1, bs1.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd2, bs2.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd1, wBS.getPerfectMatch(seq).getParameterizedCommand());

		workbenchContext.set(IContextConstants.ACTIVE_CHILD, c2);
		assertEquals(cmd1, bs1.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd2, bs2.getPerfectMatch(seq).getParameterizedCommand());
		assertEquals(cmd2, wBS.getPerfectMatch(seq).getParameterizedCommand());

		assertTrue(wBS.isPerfectMatch(seq));
	}

	public void testLookupShortcut() throws Exception {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EBindingService bs = (EBindingService) workbenchContext
				.get(EBindingService.class.getName());
		TriggerSequence seq = bs.createSequence("CTRL+5 T");
		bs.activateBinding(seq, cmd);

		assertEquals(seq, bs.getBestSequenceFor(cmd));
	}

	public void testLookupShortcuts() throws Exception {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EBindingService bs = (EBindingService) workbenchContext
				.get(EBindingService.class.getName());
		TriggerSequence seq = bs.createSequence("CTRL+5 T");
		TriggerSequence seq2 = bs.createSequence("CTRL+2 X");
		bs.activateBinding(seq, cmd);
		bs.activateBinding(seq2, cmd);

		TriggerSequence foundSequence = bs.getBestSequenceFor(cmd);
		assertNotNull(foundSequence);
		assertEquals(seq, foundSequence);
	}

	public void testLookupBestShortcut() throws Exception {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EBindingService bs = (EBindingService) workbenchContext
				.get(EBindingService.class.getName());
		TriggerSequence seq2 = bs.createSequence("ALT+5 X");
		bs.activateBinding(seq2, cmd);

		TriggerSequence seq = bs.createSequence("CTRL+5 T");
		bs.activateBinding(seq, cmd);

		TriggerSequence foundSequence = bs.getBestSequenceFor(cmd);
		assertNotNull(foundSequence);
		assertEquals(seq, foundSequence);
	}

	public void testLookupBestShortcutWithChild() throws Exception {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EBindingService bs = (EBindingService) workbenchContext
				.get(EBindingService.class.getName());
		TriggerSequence seq2 = bs.createSequence("CTRL+5 T");
		bs.activateBinding(seq2, cmd);

		IEclipseContext c1 = TestUtil.createContext(workbenchContext, "c1");
		workbenchContext.set(IContextConstants.ACTIVE_CHILD, c1);
		EBindingService bs1 = (EBindingService) c1.get(EBindingService.class
				.getName());

		TriggerSequence seq = bs1.createSequence("ALT+5 X");
		bs1.activateBinding(seq, cmd);

		TriggerSequence foundSequence = bs.getBestSequenceFor(cmd);
		assertNotNull(foundSequence);
		assertEquals(seq, foundSequence);
	}

	public void testLookupShortcutsTwoChildren() throws Exception {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd1 = cs.createCommand(TEST_ID1, null);
		ParameterizedCommand cmd2 = cs.createCommand(TEST_ID2, null);

		EBindingService wBS = (EBindingService) workbenchContext
				.get(EBindingService.class.getName());
		TriggerSequence seq = wBS.createSequence("CTRL+5 T");

		IEclipseContext c1 = TestUtil.createContext(workbenchContext, "c1");
		workbenchContext.set(IContextConstants.ACTIVE_CHILD, c1);

		EBindingService bs1 = (EBindingService) c1.get(EBindingService.class
				.getName());
		bs1.activateBinding(seq, cmd1);

		IEclipseContext c2 = TestUtil.createContext(workbenchContext, "c2");

		EBindingService bs2 = (EBindingService) c2.get(EBindingService.class
				.getName());
		bs2.activateBinding(seq, cmd2);

		assertEquals(seq, wBS.getBestSequenceFor(cmd1));
		assertNull(wBS.getBestSequenceFor(cmd2));

		assertEquals(seq, bs1.getBestSequenceFor(cmd1));
		assertNull(bs1.getBestSequenceFor(cmd2));

		assertEquals(seq, bs2.getBestSequenceFor(cmd2));
		assertNull(bs2.getBestSequenceFor(cmd1));
	}

	public void testLookupAllShortcuts() throws Exception {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EBindingService bs = (EBindingService) workbenchContext
				.get(EBindingService.class.getName());
		TriggerSequence seq2 = bs.createSequence("ALT+5 X");
		bs.activateBinding(seq2, cmd);

		TriggerSequence seq = bs.createSequence("CTRL+5 T");
		bs.activateBinding(seq, cmd);

		HashSet<TriggerSequence> set = new HashSet<TriggerSequence>();
		set.add(seq);
		set.add(seq2);

		assertEquals(set, bs.getSequencesFor(cmd));
	}

	public void testLookupAllShortcutsWithChild() throws Exception {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);

		EBindingService wBS = (EBindingService) workbenchContext
				.get(EBindingService.class.getName());
		TriggerSequence seq2 = wBS.createSequence("ALT+5 X");
		wBS.activateBinding(seq2, cmd);

		IEclipseContext c1 = TestUtil.createContext(workbenchContext, "c1");
		workbenchContext.set(IContextConstants.ACTIVE_CHILD, c1);
		EBindingService bs1 = (EBindingService) c1.get(EBindingService.class
				.getName());

		TriggerSequence seq = bs1.createSequence("CTRL+5 T");
		bs1.activateBinding(seq, cmd);

		HashSet<TriggerSequence> set = new HashSet<TriggerSequence>();
		set.add(seq);
		set.add(seq2);

		assertEquals(set, wBS.getSequencesFor(cmd));
	}

	public void testPartialMatch() throws Exception {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);

		EBindingService wBS = (EBindingService) workbenchContext
				.get(EBindingService.class.getName());
		TriggerSequence seq2 = wBS.createSequence("ALT+5 X");
		wBS.activateBinding(seq2, cmd);

		IEclipseContext c1 = TestUtil.createContext(workbenchContext, "c1");
		workbenchContext.set(IContextConstants.ACTIVE_CHILD, c1);
		EBindingService bs1 = (EBindingService) c1.get(EBindingService.class
				.getName());

		TriggerSequence seq = bs1.createSequence("CTRL+5 T");
		bs1.activateBinding(seq, cmd);

		TriggerSequence partialMatch = bs1.createSequence("CTRL+5");
		TriggerSequence partialNoMatch = bs1.createSequence("CTRL+8");
		assertFalse(bs1.isPartialMatch(partialNoMatch));
		assertTrue(bs1.isPartialMatch(partialMatch));
	}

	public void testGetPartialMatches() throws Exception {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		ParameterizedCommand cmd2 = cs.createCommand(TEST_ID2, null);

		EBindingService wBS = (EBindingService) workbenchContext
				.get(EBindingService.class.getName());
		TriggerSequence seq2 = wBS.createSequence("ALT+5 X");
		wBS.activateBinding(seq2, cmd);

		IEclipseContext c1 = TestUtil.createContext(workbenchContext, "c1");
		workbenchContext.set(IContextConstants.ACTIVE_CHILD, c1);
		EBindingService bs1 = (EBindingService) c1.get(EBindingService.class
				.getName());

		TriggerSequence seq = bs1.createSequence("CTRL+5 T");
		Binding b1 = bs1.activateBinding(seq, cmd);
		TriggerSequence sseq = bs1.createSequence("CTRL+5 Y");
		Binding b2 = bs1.activateBinding(sseq, cmd2);
		HashSet<Binding> commandMatches = new HashSet<Binding>();
		commandMatches.add(b2);
		commandMatches.add(b1);
		
		TriggerSequence partialMatch = bs1.createSequence("CTRL+5");
		TriggerSequence partialNoMatch = bs1.createSequence("CTRL+8");
		assertFalse(bs1.isPartialMatch(partialNoMatch));
		assertTrue(bs1.isPartialMatch(partialMatch));
		
		Collection<Binding> matches = bs1.getPartialMatches(partialMatch);
		assertEquals(commandMatches, matches);
	}
}
