/*******************************************************************************
 * Copyright (c) 2009, 2023 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.commands.Category;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.ModelServiceImpl;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class HeadlessApplicationTest extends HeadlessApplicationElementTest {

	protected MApplication application;

	protected IPresentationEngine renderer;

	@Before
	@Override

	public void setUp() throws Exception {
		super.setUp();

		application = (MApplication) applicationElement;

		for (MWindow wbw : application.getChildren()) {
			createGUI(wbw);
		}

		if (needsActiveChildEventHandling()) {
			addActiveChildEventHandling();
		}
	}

	@After
	public void tearDown() throws Exception {
		for (MWindow window : application.getChildren()) {
			renderer.removeGui(window);
		}

	}

	protected boolean needsActiveChildEventHandling() {
		return true;
	}

	private void addActiveChildEventHandling() {
	}

	@Test
	public void testGet_ActiveContexts() throws Exception {
		IEclipseContext context = application.getContext();

		assertNotNull(context.get(IServiceConstants.ACTIVE_CONTEXTS));
	}

	@Test
	public void testGet_Selection() throws Exception {
		IEclipseContext context = application.getContext();

		assertNull(context.get(IServiceConstants.ACTIVE_SELECTION));
	}

	@Test
	public void testGet_ActiveChild() throws Exception {
		IEclipseContext context = application.getContext();

		assertNull(context.getActiveChild());
	}

	@Test
	public void testGet_ActivePart() throws Exception {
		IEclipseContext context = application.getContext();

		assertNull(context.get(IServiceConstants.ACTIVE_PART));
	}

	@Test
	public void test_SwitchActivePartsInContext() throws Exception {
		IEclipseContext context = application.getContext();

		MPart[] parts = getTwoParts();

		context.set(IServiceConstants.ACTIVE_PART, parts[0]);

		// the OSGi context should not have been affected by the recursion
		assertNull(getRoot(context).get(IServiceConstants.ACTIVE_PART));

		context.set(IServiceConstants.ACTIVE_PART, parts[1]);

		// the OSGi context should not have been affected by the recursion
		assertNull(getRoot(context).get(IServiceConstants.ACTIVE_PART));
	}

	private IEclipseContext getRoot(IEclipseContext context) {
		IEclipseContext root = context;
		while (true) {
			context = context.getParent();
			if (context == null)
				return root;
			root = context;
		}
	}

	private void test_GetContext(MContext context) {
		assertNotNull(context.getContext());
	}

	@Test
	public void testGetFirstPart_GetContext() {
		// set the active part to ensure that it's actually been rendered
		getFirstPart().getParent().setSelectedElement(getFirstPart());
		test_GetContext(getFirstPart());
	}

	@Test
	public void testGetSecondPart_GetContext() {
		// set the active part to ensure that it's actually been rendered
		getSecondPart().getParent().setSelectedElement(getSecondPart());
		test_GetContext(getSecondPart());
	}

	private void testModify(MContext mcontext) {
		Set<String> variables = getVariables(mcontext, new HashSet<>());
		IEclipseContext context = mcontext.getContext();

		for (String variable : variables) {
			Object newValue = new Object();
			context.modify(variable, newValue);
			assertEquals(newValue, context.get(variable));
		}
	}

	@Test
	public void testModify() {
		testGetFirstPart_GetContext();
		testModify(getFirstPart());
	}

	@Test
	public void testModify2() {
		testGetSecondPart_GetContext();
		testModify(getSecondPart());
	}

	private static Set<String> getVariables(MContext context,
			Set<String> variables) {
		variables.addAll(context.getVariables());

		if (context instanceof MUIElement c) {
			MElementContainer<?> parent = c.getParent();
			while (parent != null) {
				if (parent instanceof MContext mc) {
					getVariables(mc, variables);
				}
				parent = parent.getParent();
			}
		}

		return variables;
	}

	protected MPart[] getTwoParts() {
		MPart firstPart = getFirstPart();
		assertNotNull(firstPart);

		MPart secondPart = getSecondPart();
		assertNotNull(secondPart);

		assertFalse(firstPart.equals(secondPart));

		return new MPart[] { firstPart, secondPart };
	}

	protected abstract MPart getFirstPart();

	protected abstract MPart getSecondPart();

	protected void createGUI(MUIElement uiRoot) {
		renderer.createGui(uiRoot);
	}

	@Override
	protected MApplicationElement createApplicationElement(
			IEclipseContext appContext) throws Exception {
		return createApplication(appContext, getURI());
	}

	protected abstract String getURI();

	protected IPresentationEngine createPresentationEngine(String renderingEngineURI) throws Exception {
		IContributionFactory contributionFactory = rule.getApplicationContext()
				.get(IContributionFactory.class);
		Object newEngine = contributionFactory.create(renderingEngineURI,
				rule.getApplicationContext());
		return (IPresentationEngine) newEngine;
	}

	private MApplication createApplication(IEclipseContext appContext,
			String appURI) throws Exception {
		URI initialWorkbenchDefinitionInstance = URI.createPlatformPluginURI(
				appURI, true);

		ResourceSet set = new ResourceSetImpl();
		set.getPackageRegistry().put("http://MApplicationPackage/",
				ApplicationPackageImpl.eINSTANCE);

		Resource resource = set.getResource(initialWorkbenchDefinitionInstance,
				true);

		MApplication application = (MApplication) resource.getContents().get(0);
		application.setContext(appContext);
		appContext.set(MApplication.class, application); // XXX
		appContext.set(EModelService.class, new ModelServiceImpl(appContext));

		ECommandService cs = appContext.get(ECommandService.class);
		Category cat = cs.defineCategory(MApplication.class.getName(),
				"Application Category", null); //$NON-NLS-1$
		List<MCommand> commands = application.getCommands();
		for (MCommand cmd : commands) {
			String id = cmd.getElementId();
			String name = cmd.getCommandName();
			cs.defineCommand(id, name, null, cat, null);
		}

		// take care of generating the contexts.
		List<MWindow> windows = application.getChildren();
		for (MWindow window : windows) {
			E4Workbench.initializeContext(appContext, window);
		}

		processPartContributions(application.getContext(), resource);

		renderer = createPresentationEngine(getEngineURI());

		return application;
	}

	protected String getEngineURI() {
		return "bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.HeadlessContextPresentationEngine"; //$NON-NLS-1$
	}

	private void processPartContributions(IEclipseContext context,
			Resource resource) {
		IExtensionRegistry registry = context.get(IExtensionRegistry.class);
		String extId = "org.eclipse.e4.workbench.parts"; //$NON-NLS-1$
		IConfigurationElement[] parts = registry
				.getConfigurationElementsFor(extId);

		for (IConfigurationElement p : parts) {
			MPart part = ems.createModelElement(MPart.class);
			part.setLabel(p.getAttribute("label")); //$NON-NLS-1$
			part.setIconURI("platform:/plugin/" //$NON-NLS-1$
					+ p.getContributor().getName() + "/" //$NON-NLS-1$
					+ p.getAttribute("icon")); //$NON-NLS-1$
			part.setContributionURI("bundleclass://" //$NON-NLS-1$
					+ p.getContributor().getName() + "/" //$NON-NLS-1$
					+ p.getAttribute("class")); //$NON-NLS-1$
			String parentId = p.getAttribute("parentId"); //$NON-NLS-1$
			Object parent = findObject(resource.getAllContents(), parentId);
			if (parent instanceof MElementContainer<?>) {
				((MElementContainer<MPartSashContainerElement>) parent).getChildren().add(part);
			}
		}

	}

	private EObject findObject(TreeIterator<EObject> it, String id) {
		while (it.hasNext()) {
			EObject el = it.next();
			if (el instanceof MApplicationElement) {
				if (el.eResource().getURIFragment(el).equals(id)) {
					return el;
				}
			}
		}

		return null;
	}

	protected MApplicationElement findElement(String id) {
		return findElement(application, id);
	}

	private MApplicationElement findElement(MElementContainer<?> container,
			String id) {
		if (id.equals(container.getElementId())) {
			return container;
		}

		List<?> children = container.getChildren();
		for (Object child : children) {
			MApplicationElement element = (MApplicationElement) child;
			if (element instanceof MElementContainer<?> c) {
				MApplicationElement found = findElement(c, id);
				if (found != null) {
					return found;
				}
			} else if (id.equals(element.getElementId())) {
				return element;
			}
		}
		return null;
	}

}
