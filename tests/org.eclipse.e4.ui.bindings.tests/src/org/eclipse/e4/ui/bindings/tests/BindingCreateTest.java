package org.eclipse.e4.ui.bindings.tests;

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

import junit.framework.TestCase;

public class BindingCreateTest extends TestCase {
	private static final String DEFAULT_SCHEME_ID = "org.eclipse.ui.defaultAcceleratorConfiguration";
	private static final String ID_WINDOW = "org.eclipse.ui.contexts.window";

	private static final String TEST_ID1 = "test.id1";
	
	private IEclipseContext workbenchContext;
	private EBindingService bs;
	private ParameterizedCommand cmd;
	private TriggerSequence seq, emptySeq;
	private Map<String,String> emptyAttrs, schemeOnly, schemeAndTypeAttrs, fullAttrs;
	
	@Override
	protected void setUp() throws Exception {
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
		
		emptyAttrs = new HashMap<String,String>();
		
		schemeOnly = new HashMap<String,String>();
		schemeOnly.put(EBindingService.SCHEME_ID_ATTR_TAG, DEFAULT_SCHEME_ID);
		
		schemeAndTypeAttrs = new HashMap<String,String>();;
		schemeAndTypeAttrs.put(EBindingService.SCHEME_ID_ATTR_TAG, DEFAULT_SCHEME_ID);
		schemeAndTypeAttrs.put(EBindingService.TYPE_ATTR_TAG, "user");
	}
	
	@Override
	protected void tearDown() throws Exception {
		workbenchContext.dispose();
		workbenchContext = null;
	}
	
	// *** TESTS *** //
	
	public void testNullSequence() {
		Binding b = bs.createBinding(null, cmd, ID_WINDOW, schemeOnly);
		assertNull(b);
	}
	
	public void testNullCommand() {
		// should still work since the binding manager in the keys pref 
		// page will need some way to recognize unbound system bindings
		Binding b = bs.createBinding(seq, null, ID_WINDOW, schemeOnly);
		assertNotNull(b);
	}
	
	public void testNullContext() {
		Binding b = bs.createBinding(seq, cmd, null, schemeOnly);
		assertNull(b);
	}
	
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
	
	public void testEmptySequence() {
		Binding b = bs.createBinding(emptySeq, cmd, ID_WINDOW, null);
		assertNull(b);
	}
	
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
