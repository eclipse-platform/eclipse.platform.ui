package org.eclipse.e4.core.commands.tests;

import javax.inject.Named;

import junit.framework.TestCase;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;

public class HandlerTest extends TestCase {

	private static final String ACTIVE_INFO_ID = "activeInfo";
	private static final String TEST_ID2 = "test.id2";
	private static final String TEST_ID1 = "test.id1";
	private static final String TEST_CAT1 = "test.cat1";

	static class CallHandler {
		public boolean q1;
		public boolean q2;

		@CanExecute
		public boolean canExecute() {
			q1 = true;
			return true;
		}

		@Execute
		public Object execute() {
			q2 = true;
			if (q1) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}
	}

	public void testCallHandler() throws Exception {
		EHandlerService hs = (EHandlerService) workbenchContext
				.get(EHandlerService.class.getName());
		CallHandler handler = new CallHandler();
		hs.activateHandler(TEST_ID1, handler);
		assertFalse(handler.q1);
		assertFalse(handler.q2);
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		assertTrue(hs.canExecute(cmd));
		assertFalse(handler.q2);
		assertEquals(Boolean.TRUE, hs.executeHandler(cmd));
		assertTrue(handler.q1);
		assertTrue(handler.q2);
	}

	public void testDeactivateHandler() throws Exception {
		EHandlerService hs = (EHandlerService) workbenchContext
				.get(EHandlerService.class.getName());
		CallHandler handler = new CallHandler();
		hs.activateHandler(TEST_ID1, handler);
		assertFalse(handler.q1);
		assertFalse(handler.q2);
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		assertTrue(hs.canExecute(cmd));
		assertFalse(handler.q2);
		hs.deactivateHandler(TEST_ID1, handler);
		assertNull(hs.executeHandler(cmd));
		assertTrue(handler.q1);
		assertFalse(handler.q2);
	}

	public void testActiveHandlerExecuteWorkbench() throws Exception {
		IEclipseContext c1 = workbenchContext.createChild("c1");
		workbenchContext.set(IContextConstants.ACTIVE_CHILD, c1);
		EHandlerService h1 = (EHandlerService) c1.get(EHandlerService.class
				.getName());
		CallHandler handler1 = new CallHandler();
		h1.activateHandler(TEST_ID1, handler1);
		ECommandService cs = (ECommandService) c1.get(ECommandService.class
				.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		assertTrue(h1.canExecute(cmd));
		EHandlerService wHS = (EHandlerService) workbenchContext
				.get(EHandlerService.class.getName());
		assertTrue(wHS.canExecute(cmd));
	}

	public void testQueryTwoHandlers() throws Exception {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EHandlerService wHS = (EHandlerService) workbenchContext
				.get(EHandlerService.class.getName());

		IEclipseContext c1 = workbenchContext.createChild("c1");
		workbenchContext.set(IContextConstants.ACTIVE_CHILD, c1);
		EHandlerService h1 = (EHandlerService) c1.get(EHandlerService.class
				.getName());
		CallHandler handler1 = new CallHandler();
		h1.activateHandler(TEST_ID1, handler1);

		IEclipseContext c2 = workbenchContext.createChild("c2");
		EHandlerService h2 = (EHandlerService) c2.get(EHandlerService.class
				.getName());
		CallHandler handler2 = new CallHandler();
		h2.activateHandler(TEST_ID1, handler2);

		assertFalse(handler1.q1);
		assertFalse(handler2.q1);
		assertEquals(Boolean.TRUE, wHS.executeHandler(cmd));
		assertTrue(handler1.q1);
		assertTrue(handler1.q2);
		assertFalse(handler2.q1);
		assertFalse(handler2.q2);
	}

	public void testExecuteTwoActiveHandlers() throws Exception {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EHandlerService wHS = (EHandlerService) workbenchContext
				.get(EHandlerService.class.getName());

		IEclipseContext c1 = workbenchContext.createChild("c1");
		workbenchContext.set(IContextConstants.ACTIVE_CHILD, c1);
		EHandlerService h1 = (EHandlerService) c1.get(EHandlerService.class
				.getName());
		CallHandler handler1 = new CallHandler();
		h1.activateHandler(TEST_ID1, handler1);

		IEclipseContext c2 = workbenchContext.createChild("c2");
		EHandlerService h2 = (EHandlerService) c2.get(EHandlerService.class
				.getName());
		CallHandler handler2 = new CallHandler();
		h2.activateHandler(TEST_ID1, handler2);

		assertTrue(h1.canExecute(cmd));
		assertTrue(handler1.q1);

		assertFalse(handler2.q1);
		assertTrue(h2.canExecute(cmd));
		assertTrue(handler2.q1);

		assertFalse(handler1.q2);
		assertFalse(handler2.q2);
		assertEquals(Boolean.TRUE, wHS.executeHandler(cmd));
		assertTrue(handler1.q2);
		assertFalse(handler2.q2);
	}

	public void testSwitchActivationTwoHandlers() throws Exception {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EHandlerService wHS = (EHandlerService) workbenchContext
				.get(EHandlerService.class.getName());

		IEclipseContext c1 = workbenchContext.createChild("c1");
		workbenchContext.set(IContextConstants.ACTIVE_CHILD, c1);
		EHandlerService h1 = (EHandlerService) c1.get(EHandlerService.class
				.getName());
		CallHandler handler1 = new CallHandler();
		h1.activateHandler(TEST_ID1, handler1);

		IEclipseContext c2 = workbenchContext.createChild("c2");
		EHandlerService h2 = (EHandlerService) c2.get(EHandlerService.class
				.getName());
		CallHandler handler2 = new CallHandler();
		h2.activateHandler(TEST_ID1, handler2);

		assertFalse(handler1.q1);
		assertFalse(handler2.q1);
		assertEquals(Boolean.TRUE, wHS.executeHandler(cmd));
		assertTrue(handler1.q1);
		assertTrue(handler1.q2);
		assertFalse(handler2.q1);
		assertFalse(handler2.q2);

		handler1.q1 = false;
		handler1.q2 = false;
		workbenchContext.set(IContextConstants.ACTIVE_CHILD, c2);
		assertEquals(Boolean.TRUE, wHS.executeHandler(cmd));
		assertTrue(handler2.q1);
		assertTrue(handler2.q2);
		assertFalse(handler1.q1);
		assertFalse(handler1.q2);
	}

	static class HandlerWithInfo {
		@CanExecute
		public boolean canExecute(Info h) {
			return h.name.equals("Hello");
		}

		@Execute
		public Object execute(Info h) {
			return h.name.equals("Hello") ? h : null;
		}
	}

	static class HandlerWithAnnotations {
		@CanExecute
		public boolean canExecute(@Named(ACTIVE_INFO_ID) Info h) {
			return h.name.equals("Hello");
		}

		@Execute
		public Object execute(@Named(ACTIVE_INFO_ID) Info h) {
			return h.name.equals("Hello") ? h : null;
		}
	}

	static class Info {
		public String name;

		public Info(String name) {
			this.name = name;
		}
	}

	public void testMethodInfo() throws Exception {
		Info helloInfo = new Info("Hello");
		workbenchContext.set(Info.class.getName(), helloInfo);
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EHandlerService wHS = (EHandlerService) workbenchContext
				.get(EHandlerService.class.getName());
		HandlerWithInfo handler = new HandlerWithInfo();
		wHS.activateHandler(TEST_ID1, handler);

		assertEquals(helloInfo, wHS.executeHandler(cmd));
	}

	public void testMethodWithAnnocation() throws Exception {
		Info helloInfo = new Info("Hello");
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EHandlerService wHS = (EHandlerService) workbenchContext
				.get(EHandlerService.class.getName());
		HandlerWithAnnotations handler = new HandlerWithAnnotations();
		wHS.activateHandler(TEST_ID1, handler);

		workbenchContext.set(Info.class.getName(), helloInfo);
		assertNull(wHS.executeHandler(cmd));
		
		workbenchContext.remove(Info.class.getName());
		assertNull(wHS.executeHandler(cmd));
		
		workbenchContext.set(ACTIVE_INFO_ID, helloInfo);
		assertEquals(helloInfo, wHS.executeHandler(cmd));
		
		workbenchContext.remove(ACTIVE_INFO_ID);
		assertNull(wHS.executeHandler(cmd));
	}

	private IEclipseContext workbenchContext;

	@Override
	protected void setUp() throws Exception {
		IEclipseContext globalContext = TestActivator.getDefault().getGlobalContext();
		workbenchContext = globalContext.createChild("workbenchContext");
		defineCommands(workbenchContext);
	}

	@Override
	protected void tearDown() throws Exception {
		workbenchContext.dispose();
	}

	private void defineCommands(IEclipseContext context) {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		Category category = cs.defineCategory(TEST_CAT1, "CAT1", null);
		cs.defineCommand(TEST_ID1, "ID1", null, category, null);
		cs.defineCommand(TEST_ID2, "ID2", null, category, null);
	}
}
