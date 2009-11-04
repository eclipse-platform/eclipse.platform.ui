package org.eclipse.e4.ui.bindings.tests;

import junit.framework.TestCase;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ContextUtil;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.TriggerSequence;

public class BindingLookupTest extends TestCase {
	private static final String TEST_CAT1 = "test.cat1";
	private static final String TEST_ID1 = "test.id1";
	private static final String TEST_ID2 = "test.id2";

	private IEclipseContext workbenchContext;

	private IEclipseContext createWorkbenchContext(IEclipseContext globalContext) {
		IEclipseContext wb = TestUtil.createContext(globalContext,
				"workbenchContext");
		ContextUtil.commandSetup(wb);
		ContextUtil.handlerSetup(wb);
		org.eclipse.e4.ui.bindings.ContextUtil.bindingSetup(wb);
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
		ParameterizedCommand perfectMatch = bs.getPerfectMatch(seq);
		assertEquals(cmd, perfectMatch);
		bs.deactivateBinding(seq, cmd);
		assertNull(bs.getPerfectMatch(seq));

		bs.activateBinding(seq, cmd);
		assertEquals(cmd, bs.getPerfectMatch(seq));
	}

	public void testLookupChildBinding() throws Exception {
		IEclipseContext c1 = TestUtil.createContext(workbenchContext, "c1");
		workbenchContext.set(IContextConstants.ACTIVE_CHILD, c1);
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);

		EBindingService bs1 = (EBindingService) c1.get(EBindingService.class
				.getName());
		TriggerSequence seq = bs1.createSequence("CTRL+5 T");
		bs1.activateBinding(seq, cmd);
		EBindingService wBS = (EBindingService) workbenchContext
				.get(EBindingService.class.getName());
		assertEquals(cmd, wBS.getPerfectMatch(seq));
		assertEquals(cmd, bs1.getPerfectMatch(seq));
	}
}
