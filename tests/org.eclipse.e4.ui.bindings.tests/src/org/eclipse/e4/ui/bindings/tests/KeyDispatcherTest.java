package org.eclipse.e4.ui.bindings.tests;

import junit.framework.TestCase;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class KeyDispatcherTest extends TestCase {
	private static final String TEST_CAT1 = "test.cat1";
	private static final String TEST_ID1 = "test.id1";
	private static final String TEST_ID2 = "test.id2";

	static class CallHandler {
		public boolean q1;
		public boolean q2;

		public boolean canExecute() {
			q1 = true;
			return true;
		}

		public Object execute() {
			q2 = true;
			if (q1) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}
	}

	private Display display;
	private IEclipseContext workbenchContext;
	private CallHandler handler;
	private CallHandler twoStrokeHandler;

	private void defineCommands(IEclipseContext context) {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		Category category = cs.defineCategory(TEST_CAT1, "CAT1", null);
		cs.defineCommand(TEST_ID1, "ID1", null, category, null);
		cs.defineCommand(TEST_ID2, "ID2", null, category, null);
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EHandlerService hs = (EHandlerService) workbenchContext
				.get(EHandlerService.class.getName());
		handler = new CallHandler();
		hs.activateHandler(TEST_ID1, handler);
		EBindingService bs = (EBindingService) workbenchContext
				.get(EBindingService.class.getName());
		TriggerSequence seq = bs.createSequence("CTRL+A");
		bs.activateBinding(seq, cmd);

		ParameterizedCommand cmd2 = cs.createCommand(TEST_ID2, null);
		twoStrokeHandler = new CallHandler();
		hs.activateHandler(TEST_ID2, twoStrokeHandler);
		TriggerSequence twoKeys = bs.createSequence("CTRL+5 CTRL+A");
		bs.activateBinding(twoKeys, cmd2);
	}

	private IEclipseContext createWorkbenchContext(IEclipseContext globalContext) {
		IEclipseContext wb = TestUtil.createContext(globalContext,
				"workbenchContext");
		return wb;
	}

	@Override
	protected void setUp() throws Exception {
		display = new Display();
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
		display.dispose();
		display = null;
	}

	public void testExecuteOneCommand() throws Exception {
		KeyBindingDispatcher dispatcher = new KeyBindingDispatcher();
		ContextInjectionFactory.inject(dispatcher, workbenchContext);
		final Listener listener = dispatcher.getKeyDownFilter();
		display.addFilter(SWT.KeyDown, listener);
		display.addFilter(SWT.Traverse, listener);

		assertFalse(handler.q2);

		Shell shell = new Shell(display, SWT.NONE);

		Event event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = SWT.CTRL;
		shell.notifyListeners(SWT.KeyDown, event);

		event = new Event();
		event.type = SWT.KeyDown;
		event.stateMask = SWT.CTRL;
		event.keyCode = 'A';
		shell.notifyListeners(SWT.KeyDown, event);

		assertTrue(handler.q2);
	}

	public void testExecuteMultiStrokeBinding() throws Exception {
		KeyBindingDispatcher dispatcher = new KeyBindingDispatcher();
		ContextInjectionFactory.inject(dispatcher, workbenchContext);
		final Listener listener = dispatcher.getKeyDownFilter();
		display.addFilter(SWT.KeyDown, listener);
		display.addFilter(SWT.Traverse, listener);

		assertFalse(twoStrokeHandler.q2);

		Shell shell = new Shell(display, SWT.NONE);

		Event event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = SWT.CTRL;
		shell.notifyListeners(SWT.KeyDown, event);

		event = new Event();
		event.type = SWT.KeyDown;
		event.stateMask = SWT.CTRL;
		event.keyCode = '5';
		shell.notifyListeners(SWT.KeyDown, event);

		assertFalse(twoStrokeHandler.q2);

		event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = SWT.CTRL;
		shell.notifyListeners(SWT.KeyDown, event);

		event = new Event();
		event.type = SWT.KeyDown;
		event.stateMask = SWT.CTRL;
		event.keyCode = 'A';
		shell.notifyListeners(SWT.KeyDown, event);

		processEvents();

		assertTrue(twoStrokeHandler.q2);
		assertFalse(handler.q2);
	}

	public void testKeyDispatcherReset() throws Exception {
		KeyBindingDispatcher dispatcher = new KeyBindingDispatcher();
		ContextInjectionFactory.inject(dispatcher, workbenchContext);
		final Listener listener = dispatcher.getKeyDownFilter();
		display.addFilter(SWT.KeyDown, listener);
		display.addFilter(SWT.Traverse, listener);

		assertFalse(twoStrokeHandler.q2);

		Shell shell = new Shell(display, SWT.NONE);

		Event event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = SWT.CTRL;
		shell.notifyListeners(SWT.KeyDown, event);

		event = new Event();
		event.type = SWT.KeyDown;
		event.stateMask = SWT.CTRL;
		event.keyCode = '5';
		shell.notifyListeners(SWT.KeyDown, event);

		assertFalse(twoStrokeHandler.q2);
		Thread.sleep(2000L);
		processEvents();

		event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = SWT.CTRL;
		shell.notifyListeners(SWT.KeyDown, event);

		event = new Event();
		event.type = SWT.KeyDown;
		event.stateMask = SWT.CTRL;
		event.keyCode = 'A';
		shell.notifyListeners(SWT.KeyDown, event);

		processEvents();

		assertFalse(twoStrokeHandler.q2);
		assertTrue(handler.q2);
	}

	public void testSendKeyStroke() throws Exception {
		KeyBindingDispatcher dispatcher = (KeyBindingDispatcher) ContextInjectionFactory
				.make(KeyBindingDispatcher.class, workbenchContext);
		final Listener listener = dispatcher.getKeyDownFilter();
		display.addFilter(SWT.KeyDown, listener);
		display.addFilter(SWT.Traverse, listener);
		Shell shell = new Shell(display, SWT.NONE);
		shell.setLayout(new FillLayout());
		StyledText text = new StyledText(shell, SWT.WRAP | SWT.MULTI);
		shell.setBounds(100, 100, 100, 100);
		shell.layout();
		processEvents();
		assertEquals("", text.getText());

		Event event = new Event();
		event.type = SWT.KeyDown;
		event.stateMask = SWT.SHIFT;
		event.keyCode = '(';
		event.character = '(';
		text.notifyListeners(SWT.KeyDown, event);

		event = new Event();
		event.type = SWT.KeyUp;
		event.stateMask = SWT.SHIFT;
		event.keyCode = '(';
		event.character = '(';
		text.notifyListeners(SWT.KeyUp, event);

		processEvents();

		assertEquals("(", text.getText());
	}

	private void processEvents() {
		while (display.readAndDispatch())
			;
	}

	// KEYS >>> Listener.handleEvent(type = KeyDown, stateMask = 0x0, keyCode =
	// 0x40000, time = 2986140, character = 0x0)
	// KEYS >>> Listener.handleEvent(type = KeyDown, stateMask = 0x40000,
	// keyCode = 0x10000, time = 2986218, character = 0x0)
	// KEYS >>> Listener.handleEvent(type = KeyDown, stateMask = 0x50000,
	// keyCode = 0x6c, time = 2986515, character = 0xc)

}
