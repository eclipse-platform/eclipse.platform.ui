/*******************************************************************************
 * Copyright (c) 2007, 2019 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433603
 *******************************************************************************/

package org.eclipse.ui.tests.services;

import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.eclipse.ui.tests.harness.util.UITestUtil.forceActive;
import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.eclipse.ui.tests.harness.util.UITestUtil.waitForJobs;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.expressions.TestExpression;
import org.eclipse.core.expressions.WithExpression;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.handlers.HandlerPersistence;
import org.eclipse.ui.services.IEvaluationReference;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.tests.SelectionProviderView;
import org.eclipse.ui.tests.commands.ActiveContextExpression;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Assume;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

/**
 * @since 3.3
 */
@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EvaluationServiceTest extends UITestCase {
	private static final String CHECK_HANDLER_ID = "org.eclipse.ui.tests.services.checkHandler";
	private static final String CONTEXT_ID1 = "org.eclipse.ui.command.contexts.evaluationService1";

	public EvaluationServiceTest() {
		super(EvaluationServiceTest.class.getName());
	}

	private static class MyEval implements IPropertyChangeListener {
		public volatile int count = 0;
		public volatile boolean currentValue;

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			count++;
			if (event.getProperty() == IEvaluationService.RESULT
					&& event.getNewValue() instanceof Boolean bool) {
				currentValue = bool.booleanValue();
			}
		}
	}

	@Test
	public void testBug334524() throws Exception {
		IPerspectiveRegistry registry = getWorkbench().getPerspectiveRegistry();
		IPerspectiveDescriptor resourcePerspective = registry.findPerspectiveWithId("org.eclipse.ui.resourcePerspective");
		IPerspectiveDescriptor javaPerspective = registry.findPerspectiveWithId("org.eclipse.jdt.ui.JavaPerspective");
		String viewId = "org.eclipse.ui.tests.SelectionProviderView";

		IWorkbenchWindow window = openTestWindow();
		IWorkbenchPage activePage = window.getActivePage();

		// show view in resource perspective
		activePage.setPerspective(resourcePerspective);
		SelectionProviderView view = (SelectionProviderView) activePage.showView(viewId);
		processEvents();

		// show view in java perspective
		activePage.setPerspective(javaPerspective);
		activePage.showView(viewId);
		processEvents();

		// now set the selection
		IStructuredSelection selection = new StructuredSelection(new String("testing selection"));
		view.setSelection(selection);
		processEvents();

		// switch perspective & check selection
		activePage.setPerspective(resourcePerspective);
		processEvents();

		IEvaluationService service = window.getService(IEvaluationService.class);
		Object currentSelection = service.getCurrentState().getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
		assertEquals(selection, currentSelection);

	}

	@Test
	public void testBasicService() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		waitForJobs(500, 5000);

		boolean activeShell = forceActive(window.getShell());
		Assume.assumeTrue(activeShell);

		IEvaluationService service = window
				.getService(IEvaluationService.class);
		assertNotNull(service);

		MyEval listener = new MyEval();
		IContextActivation context1 = null;
		IEvaluationReference evalRef = null;
		IContextService contextService = null;
		try {
			contextService = window.getService(IContextService.class);
			assertFalse(contextService.getActiveContextIds().contains(CONTEXT_ID1));

			evalRef = service.addEvaluationListener(
					new ActiveContextExpression(CONTEXT_ID1,
							new String[] { ISources.ACTIVE_CONTEXT_NAME }),
					listener, IEvaluationService.RESULT);
			assertEquals(1, listener.count);
			assertFalse(listener.currentValue);


			context1 = contextService.activateContext(CONTEXT_ID1);
			processEvents();
			waitForJobs(500, 3000);
			assertTrue(contextService.getActiveContextIds().contains(CONTEXT_ID1));

			assertEquals(2, listener.count);
			assertTrue(listener.currentValue);

			contextService.deactivateContext(context1);
			context1 = null;
			assertFalse(contextService.getActiveContextIds().contains(CONTEXT_ID1));

			assertEquals(3, listener.count);
			assertFalse(listener.currentValue);

			service.removeEvaluationListener(evalRef);
			evalRef = null;
			assertEquals(4, listener.count);

			context1 = contextService.activateContext(CONTEXT_ID1);
			processEvents();
			waitForJobs(500, 3000);
			assertTrue(contextService.getActiveContextIds().contains(CONTEXT_ID1));

			assertEquals(4, listener.count);
			assertFalse(listener.currentValue);
			contextService.deactivateContext(context1);
			context1 = null;
			assertFalse(contextService.getActiveContextIds().contains(CONTEXT_ID1));

			assertEquals(4, listener.count);
			assertFalse(listener.currentValue);
		} finally {
			if (context1 != null) {
				contextService.deactivateContext(context1);
			}
			if (evalRef != null) {
				service.removeEvaluationListener(evalRef);
			}
		}
	}

	@Test
	public void testTwoEvaluations() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		boolean activeShell = forceActive(window.getShell());

		waitForJobs(500, 5000);

		final AtomicBoolean shellIsActive = new AtomicBoolean(activeShell);
		Assume.assumeTrue(shellIsActive.get());

		IEvaluationService service = window
				.getService(IEvaluationService.class);
		assertNotNull(service);
		MyEval listener1 = new MyEval();
		MyEval listener2 = new MyEval();
		IContextActivation context1 = null;
		IEvaluationReference evalRef1 = null;
		IEvaluationReference evalRef2 = null;
		IContextService contextService = null;
		try {
			contextService = window.getService(IContextService.class);
			assertFalse(contextService.getActiveContextIds().contains(CONTEXT_ID1));

			evalRef1 = service.addEvaluationListener(
					new ActiveContextExpression(CONTEXT_ID1,
							new String[] { ISources.ACTIVE_CONTEXT_NAME }),
					listener1, IEvaluationService.RESULT);
			assertEquals(1, listener1.count);
			assertFalse(listener1.currentValue);

			evalRef2 = service.addEvaluationListener(
					new ActiveContextExpression(CONTEXT_ID1,
							new String[] { ISources.ACTIVE_CONTEXT_NAME }),
					listener2, IEvaluationService.RESULT);
			assertEquals(1, listener2.count);
			assertFalse(listener2.currentValue);
			evalRef2.setResult(true);

			context1 = contextService.activateContext(CONTEXT_ID1);
			processEvents();
			waitForJobs(500, 3000);
			assertTrue(contextService.getActiveContextIds().contains(CONTEXT_ID1));

			int count = 0;
			while (count < 5 && listener1.count != 2) {
				count++;
				waitForJobs(100 * count, 1000);
			}

			assertEquals(2, listener1.count);
			assertTrue(listener1.currentValue);
			// we already set this guy to true, he should skip
			assertEquals(1, listener2.count);
			assertFalse(listener2.currentValue);

			evalRef1.setResult(false);
			contextService.deactivateContext(context1);
			processEvents();
			waitForJobs(500, 3000);
			assertFalse(contextService.getActiveContextIds().contains(CONTEXT_ID1));

			context1 = null;
			assertEquals(2, listener2.count);
			assertFalse(listener2.currentValue);

			// we already set this guy to false, so he should be the old
			// values
			assertEquals(2, listener1.count);
			assertTrue(listener1.currentValue);

		} finally {
			if (context1 != null) {
				contextService.deactivateContext(context1);
			}
			if (evalRef1 != null) {
				service.removeEvaluationListener(evalRef1);
			}
			if (evalRef2 != null) {
				service.removeEvaluationListener(evalRef2);
			}
		}
	}

	@Test
	@Ignore // TODO fix testRestriction
	public void testRestriction() {
		IWorkbenchWindow window = openTestWindow();
		IEvaluationService evaluationService = window
				.getService(IEvaluationService.class);
		assertNotNull(evaluationService);
		IContextService contextService = window
				.getService(IContextService.class);
		assertNotNull(contextService);

		Expression expression = new ActiveContextExpression(CONTEXT_ID1,
				new String[] { ISources.ACTIVE_CONTEXT_NAME });

		final boolean[] propertyChanged = new boolean[1];
		final boolean[] propertyShouldChange = new boolean[1];

		IPropertyChangeListener propertyChangeListener = event -> {
			if (event.getProperty().equals("foo")) {
				propertyChanged[0] = true;
			}

		};
		IEvaluationReference ref = evaluationService.addEvaluationListener(
				expression, propertyChangeListener, "foo");
		((WorkbenchWindow)window).getMenuRestrictions().add(ref);

		IPropertyChangeListener propertyShouldChangeListener = event -> {
			if (event.getProperty().equals("foo")) {
				propertyShouldChange[0] = true;
			}

		};
		evaluationService.addEvaluationListener(expression,
				propertyShouldChangeListener, "foo");

		propertyChanged[0] = false;
		propertyShouldChange[0] = false;

		assertFalse(contextService.getActiveContextIds().contains(CONTEXT_ID1));
		IContextActivation activation = contextService
				.activateContext(CONTEXT_ID1);

		assertTrue(propertyChanged[0]);
		assertTrue(propertyShouldChange[0]);
		propertyChanged[0] = false;
		propertyShouldChange[0] = false;

		contextService.deactivateContext(activation);
		assertTrue(propertyChanged[0]);
		assertTrue(propertyShouldChange[0]);
		assertFalse(contextService.getActiveContextIds().contains(CONTEXT_ID1));
		activation = contextService.activateContext(CONTEXT_ID1);
		propertyChanged[0] = false;
		propertyShouldChange[0] = false;
		assertTrue(contextService.getActiveContextIds().contains(CONTEXT_ID1));

		// open second window
		IWorkbenchWindow window2 = openTestWindow();
		assertFalse(propertyChanged[0]);
		assertTrue(propertyShouldChange[0]);
		assertFalse(contextService.getActiveContextIds().contains(CONTEXT_ID1));
		propertyChanged[0] = false;
		propertyShouldChange[0] = false;

		window2.close();
		processEvents();

		assertTrue(contextService.getActiveContextIds().contains(CONTEXT_ID1));
		assertFalse(propertyChanged[0]);
		assertTrue(propertyShouldChange[0]);
	}

	@Test
	@Ignore // TODO fix testScopedService
	public void testScopedService() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IEvaluationService service = window
				.getService(IEvaluationService.class);
		assertNotNull(service);
		//assertTrue(service instanceof SlaveEvaluationService);

		MyEval listener = new MyEval();
		IContextActivation context1 = null;
		IContextService contextService = null;
		try {
			service.addEvaluationListener(
					new ActiveContextExpression(CONTEXT_ID1,
							new String[] { ISources.ACTIVE_CONTEXT_NAME }),
					listener, IEvaluationService.RESULT);
			assertEquals(1, listener.count);
			assertFalse(listener.currentValue);

			contextService = window.getWorkbench()
					.getService(IContextService.class);
			context1 = contextService.activateContext(CONTEXT_ID1);
			assertEquals(2, listener.count);
			assertTrue(listener.currentValue);

			window.close();
			processEvents();
			assertEquals(3, listener.count);
			assertTrue(listener.currentValue);

			contextService.deactivateContext(context1);
			context1 = null;
			assertEquals(3, listener.count);
			assertTrue(listener.currentValue);
		} finally {
			if (context1 != null) {
				contextService.deactivateContext(context1);
			}
		}
	}

	private static class UserExpression extends Expression {
		public String lookFor;

		public UserExpression(String lookFor) {
			this.lookFor = lookFor;
		}

		@Override
		public void collectExpressionInfo(ExpressionInfo info) {
			info.addVariableNameAccess("username");
		}

		@Override
		public EvaluationResult evaluate(IEvaluationContext context) {
			Object o = context.getVariable("username");
			if (o instanceof String) {
				String variable = (String) o;
				return lookFor.equals(variable) ? EvaluationResult.TRUE : EvaluationResult.FALSE;
			}
			return EvaluationResult.FALSE;
		}
	}

	@Test
	public void testSourceProvider() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IEvaluationService service = window
				.getService(IEvaluationService.class);
		assertNotNull(service);

		MyEval listener = new MyEval();
		UserExpression expression = new UserExpression("Paul");
		IEvaluationReference ref = service.addEvaluationListener(expression,
				listener, IEvaluationService.RESULT);
		assertEquals(ISources.ACTIVE_CONTEXT << 1, ref.getSourcePriority());
		assertFalse(listener.currentValue);
		assertEquals(1, listener.count);

		ISourceProviderService sps = window
				.getService(ISourceProviderService.class);
		ActiveUserSourceProvider userProvider = (ActiveUserSourceProvider) sps
				.getSourceProvider("username");

		userProvider.setUsername("John");
		assertFalse(listener.currentValue);
		assertEquals(1, listener.count);

		userProvider.setUsername("Paul");
		assertTrue(listener.currentValue);
		assertEquals(2, listener.count);

		userProvider.setUsername("guest");
		assertFalse(listener.currentValue);
		assertEquals(3, listener.count);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSourceProviderPriority() throws Exception {
		IHandlerService hs = getWorkbench().getService(IHandlerService.class);

		Collection<IHandlerActivation> activations = null;
		// fill in a set of activations
		String hsClassName = hs.getClass().getName();
		switch (hsClassName) {
		case "org.eclipse.ui.internal.handlers.HandlerService":
			{
				Field hpField = hs.getClass().getDeclaredField("handlerPersistence");
				hpField.setAccessible(true);
				HandlerPersistence hp = (HandlerPersistence) hpField.get(hs);
				Field activationsField = hp.getClass().getDeclaredField("handlerActivations");
				activationsField.setAccessible(true);
				activations = (Collection<IHandlerActivation>) activationsField.get(hp);
				assertNotNull(activations);
				break;
			}
		case "org.eclipse.ui.internal.handlers.LegacyHandlerService":
			{
				Field hsField = hs.getClass().getDeclaredField("LEGACY_H_ID");
				String LEGACY_H_ID = (String) hsField.get(null);
				Field hpField = hs.getClass().getDeclaredField("eclipseContext");
				hpField.setAccessible(true);
				Object eclipseContext = hpField.get(hs);
				assertNotNull(eclipseContext);
				Method getMethod = eclipseContext.getClass().getDeclaredMethod("get", String.class);
				activations = (Collection<IHandlerActivation>) getMethod.invoke(eclipseContext,
						LEGACY_H_ID + CHECK_HANDLER_ID);
				assertNotNull(activations);
				break;
			}
		default:
			fail("Incorrect handler service: " + hsClassName);
			break;
		}

		IHandlerActivation activation = null;
		for (IHandlerActivation ha : activations) {
			if (CHECK_HANDLER_ID.equals(ha.getCommandId())) {
				activation = ha;
			}
		}


		assertNotNull("Could not find activation for " + CHECK_HANDLER_ID, activation);

		assertEquals(ISources.ACTIVE_CONTEXT<<1, activation.getSourcePriority());
	}

	@Test
	public void testPropertyChange() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IEvaluationService service = window
				.getService(IEvaluationService.class);
		assertNotNull(service);
		MyEval listener = new MyEval();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement element = null;
		IConfigurationElement[] elements = registry
				.getConfigurationElementsFor("org.eclipse.core.expressions.definitions");
		for (int i = 0; i < elements.length && element == null; i++) {
			if (elements[i].getAttribute("id").equals(
					"org.eclipse.ui.tests.defWithPropertyTester")) {
				element = elements[i];
			}
		}

		assertNotNull(element);
		Expression expr = ExpressionConverter.getDefault().perform(element.getChildren()[0]);
		service.addEvaluationListener(expr,
				listener, IEvaluationService.RESULT);
		assertFalse(listener.currentValue);
		assertEquals(1, listener.count);

		StaticVarPropertyTester.result = true;
		assertFalse(listener.currentValue);
		assertEquals(1, listener.count);

		service.requestEvaluation("org.eclipse.ui.tests.class.method");
		assertTrue(listener.currentValue);
		assertEquals(2, listener.count);

		service.requestEvaluation("org.eclipse.ui.tests.class.method");
		assertTrue(listener.currentValue);
		assertEquals(2, listener.count);
	}

	@Test
	public void testPlatformProperty() throws Exception {
		IEvaluationService evaluationService = getWorkbench().getService(IEvaluationService.class);
		TestExpression test = new TestExpression("org.eclipse.core.runtime",
				"bundleState",
				new Object[] { "org.eclipse.core.expressions" }, "ACTIVE", false);
		WithExpression exp = new WithExpression("org.eclipse.core.runtime.Platform");
		exp.add(test);
		EvaluationResult result= exp.evaluate(evaluationService.getCurrentState());
		assertEquals(EvaluationResult.TRUE, result);
	}

	@Test
	@Ignore
	public void testSystemProperty() throws Exception {
		// this is not added, as the ability to test system properties with
		// no '.' seems unhelpful
		System.setProperty("isHere", "true");
		IEvaluationService evaluationService = getWorkbench().getService(IEvaluationService.class);
		TestExpression test = new TestExpression("org.eclipse.core.runtime",
				"isHere",
				new Object[] { "true" }, null, false);
		WithExpression exp = new WithExpression(
				"java.lang.System" );
		exp.add(test);
		EvaluationResult result = exp.evaluate(evaluationService
				.getCurrentState());
		assertEquals(EvaluationResult.TRUE, result);

	}

	static class ActivePartIdExpression extends Expression {
		private final String partId;

		public ActivePartIdExpression(String id) {
			partId = id;
		}

		@Override
		public void collectExpressionInfo(ExpressionInfo info) {
			info.addVariableNameAccess(ISources.ACTIVE_PART_ID_NAME);
			info.addVariableNameAccess(ISources.ACTIVE_CURRENT_SELECTION_NAME);
		}

		@Override
		public EvaluationResult evaluate(IEvaluationContext context){
			Object v = context.getVariable(ISources.ACTIVE_PART_ID_NAME);
			return EvaluationResult.valueOf(partId.equals(v));
		}
	}

	static class PartSelection {
		public ISelection selection;
		public IWorkbenchPart part;

		public PartSelection(ISelection sel, IWorkbenchPart p) {
			selection = sel;
			part = p;
		}
	}

	@Test
	public void testWorkbenchProvider() throws Exception {

		IWorkbenchWindow window = openTestWindow();
		final IEvaluationService service = window
				.getWorkbench().getService(IEvaluationService.class);
		assertNotNull(service);

		// some setup
		IWorkbenchPage page = window.getActivePage();
		SelectionProviderView view1 = (SelectionProviderView) page
				.showView(org.eclipse.ui.tests.SelectionProviderView.ID);
		view1.setSelection(StructuredSelection.EMPTY);
		SelectionProviderView view2 = (SelectionProviderView) page
				.showView(org.eclipse.ui.tests.SelectionProviderView.ID_2);
		TextSelection mySelection = new TextSelection(0, 5);
		view2.setSelection(mySelection);

		processEvents();

		final ArrayList<PartSelection> selection = new ArrayList<>();
		IPropertyChangeListener listener = event -> {
			IEvaluationContext state = service.getCurrentState();
			try {
				ISelection sel = null;
				IWorkbenchPart part = null;
				Object o = state
						.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
				if (o instanceof ISelection s) {
					sel = s;
				}
				o = state.getVariable(ISources.ACTIVE_PART_NAME);
				if (o instanceof IWorkbenchPart iwp) {
					part = iwp;
				}
				selection.add(new PartSelection(sel, part));
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		IEvaluationReference ref = service.addEvaluationListener(
				new ActivePartIdExpression(
						org.eclipse.ui.tests.SelectionProviderView.ID),
				listener, "PROP");
		int callIdx = 0;
		try {

			// initially ID_2 is showed
			assertSelection(selection, callIdx, TextSelection.class, SelectionProviderView.ID_2);

			page.activate(view1);
			processEvents();
			callIdx++;

			assertSelection(selection, callIdx, StructuredSelection.class, SelectionProviderView.ID);

			page.activate(view2);
			processEvents();
			callIdx++;

			assertSelection(selection, callIdx, TextSelection.class, SelectionProviderView.ID_2);
			assertEquals(window.getActivePage().getActivePart().getSite().getId(),
					service.getCurrentState().getVariable(ISources.ACTIVE_PART_ID_NAME));

			IWorkbenchWindow window2 = openTestWindow();
			IWorkbenchPage page2 = window2.getActivePage();
			processEvents();

			// no change
			assertEquals(callIdx + 1, selection.size());

			SelectionProviderView view3 = (SelectionProviderView) page2
					.showView(org.eclipse.ui.tests.SelectionProviderView.ID);
			processEvents();
			// id1 activated with default selection StructuredSelection.EMPTY
			callIdx++;

			assertSelection(selection, callIdx, StructuredSelection.class, SelectionProviderView.ID);
			assertEquals(window2.getActivePage().getActivePart().getSite().getId(),
					service.getCurrentState().getVariable(ISources.ACTIVE_PART_ID_NAME));

			view3.setSelection(new TreeSelection(new TreePath(new Object[] {"nothing"})));
			processEvents();
			// selection changes, but view id remains the same - so no callIdx increments
			assertEquals(callIdx + 1, selection.size());

			window.getShell().forceActive();
			processEvents();
			// the shell activate should have forced another change
			callIdx++;
//			assertEquals(window.getActivePage().getActivePart().getSite().getId(),
//					service.getCurrentState().getVariable(ISources.ACTIVE_PART_ID_NAME));

//			assertSelection(selection, callIdx, TextSelection.class, SelectionProviderView.ID_2);

		} finally {
			service.removeEvaluationListener(ref);
		}
	}

	private void assertSelection(final ArrayList<PartSelection> selection, int callIdx, Class<?> clazz, String viewId) {
		assertEquals(callIdx + 1, selection.size());
		assertEquals(clazz, getSelection(selection, callIdx)
				.getClass());
		assertEquals(viewId, getPart(selection, callIdx).getSite().getId());
	}

	private ISelection getSelection(final ArrayList<PartSelection> selection, int idx) {
		return selection.get(idx).selection;
	}

	private IWorkbenchPart getPart(final ArrayList<PartSelection> selection, int idx) {
		return selection.get(idx).part;
	}
}
