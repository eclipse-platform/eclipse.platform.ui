/*******************************************************************************
 * Copyright (c) 2016, 2026 EclipseSource Muenchen GmbH and others.
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Alexandra Buzila - initial API and implementation
 * Gerhard Kreuzer  - Bug 561324
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.runtime.ContributorFactorySimple;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.internal.workbench.ExtensionsSort;
import org.eclipse.e4.ui.internal.workbench.ModelAssembler;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.fragment.MFragmentFactory;
import org.eclipse.e4.ui.model.fragment.MModelFragment;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.swt.DisplayUISynchronize;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.equinox.log.ExtendedLogReaderService;
import org.eclipse.equinox.log.LogFilter;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;

public class ModelAssemblerTests {
	private static final String EXTENSION_POINT_ID = "org.eclipse.e4.workbench.model";
	private static final String BUNDLE_SYMBOLIC_NAME = "org.eclipse.e4.ui.tests";
	private static final String APPLICATION_ID = "org.eclipse.e4.ui.tests.modelassembler.app";
	private IEclipseContext appContext;
	private MApplication application;
	private E4XMIResourceFactory factory;
	private ResourceSetImpl resourceSet;
	private E4XMIResource appResource;
	private ModelAssembler assembler;
	private EModelService modelService;

	private static final int COUNTDOWN_TIMEOUT = 10_000;
	private ArrayDeque<String> logMessages;
	private ModelAssemblerTestLogListener logListener;

	class ModelAssemblerTestLogListener implements LogListener {

		CountDownLatch countDownLatch;

		@Override
		public void logged(LogEntry entry) {
			logMessages.add(entry.getMessage());
			if (countDownLatch != null) {
				countDownLatch.countDown();
			}
		}

	}

	@BeforeEach
	public void setup() {
		appContext = E4Application.createDefaultContext();
		application = ApplicationFactoryImpl.eINSTANCE.createApplication();
		application.setElementId(APPLICATION_ID);
		application.setContext(appContext);

		appContext.set(MApplication.class, application);
		appContext.set(UISynchronize.class, new DisplayUISynchronize(Display.getDefault()));

		factory = new E4XMIResourceFactory();
		appResource = (E4XMIResource) factory.createResource(URI.createURI("virtualuri"));
		resourceSet = new ResourceSetImpl();
		resourceSet.getResources().add(appResource);
		appResource.getContents().add((EObject) application);

		assembler = appContext.get(ModelAssembler.class);
		assembler.init(application, appContext, new DisplayUISynchronize(Display.getDefault()));
		ContextInjectionFactory.invoke(assembler, PostConstruct.class, appContext);

		logMessages = new ArrayDeque<>();
		logListener = new ModelAssemblerTestLogListener();

		ExtendedLogReaderService log = appContext.get(ExtendedLogReaderService.class);
		LogFilter logFilter = (_, loggerName, _) -> {
			return "org.eclipse.e4.ui.internal.workbench.ModelAssembler".equals(loggerName);
		};

		log.addLogListener(logListener, logFilter);

		modelService = application.getContext().get(EModelService.class);
	}

	@AfterEach
	public void tearDown() {
		ExtendedLogReaderService log = appContext.get(ExtendedLogReaderService.class);
		log.removeLogListener(logListener);
	}

	/**
	 * Test the handling of a fragment contribution with no elements to merge.
	 */
	@Test
	public void testFragments_emptyFragment() throws Exception {
		MModelFragment fragment = MFragmentFactory.INSTANCE.createStringModelFragment();
		final String contributorURI = "testFragments_emptyFragment_contribURI";

		List<MApplicationElement> elements = assembler.processModelFragment(fragment, contributorURI, true);
		assertTrue(elements.isEmpty());

		List<MApplicationElement> modelElements = modelService.findElements(application, MApplicationElement.class,
				EModelService.ANYWHERE, element -> element.getContributorURI() != null
						&& element.getContributorURI().equals(contributorURI));
		assertTrue(modelElements.isEmpty());

		assertEquals(0, logMessages.size());
	}

	/**
	 * Tests that fragments are correctly contributed to the application model.
	 */
	@Test
	public void testFragments_workingFragment() throws Exception {
		// the contributed element
		MWindow window = modelService.createModelElement(MWindow.class);
		final String contributedElementId = "testFragments_workingFragment-contributedWindow";
		window.setElementId(contributedElementId);

		// create fragment
		MStringModelFragment fragment = MFragmentFactory.INSTANCE.createStringModelFragment();
		fragment.setFeaturename("children");
		final String fragmentParentId = "org.eclipse.e4.ui.tests.modelassembler.app";
		fragment.setParentElementId(fragmentParentId);
		fragment.getElements().add(window);
		// add fragment to resource
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		fragmentResource.getContents().add((EObject) fragment);

		assertEquals(null, modelService.find(contributedElementId, application));

		final String contributorURI = "testFragments_emptyFragment_contribURI";
		List<MApplicationElement> elements = assembler.processModelFragment(fragment, contributorURI, false);

		assertEquals(window, modelService.find(contributedElementId, application));
		assertEquals(1, elements.size());
		assertEquals(contributorURI, elements.get(0).getContributorURI());
		assertTrue(elements.contains(window));
		MUIElement found = modelService.find(contributedElementId, application);
		assertEquals(window, found);
		assertEquals(fragmentParentId, found.getParent().getElementId());

		assertEquals(0, logMessages.size());
	}

	/**
	 * Tests that fragments configured to be merged only if their elements don't
	 * exist yet are not merged if the model already contains the contributed
	 * element.
	 */
	@Test
	public void testFragments_existingXMIID_checkExists() throws Exception {
		// create fragment
		MStringModelFragment fragment = MFragmentFactory.INSTANCE.createStringModelFragment();
		fragment.setFeaturename("children");
		fragment.setParentElementId("org.eclipse.e4.ui.tests.modelassembler.app");
		// create fragment resource
		E4XMIResource fragmentResource = (E4XMIResource) factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		fragmentResource.getContents().add((EObject) fragment);

		final String contributedElementId = "testFragments_existingElementID-contributedWindow";
		MWindow window1 = modelService.createModelElement(MWindow.class);
		window1.setElementId(contributedElementId);
		MWindow window2 = modelService.createModelElement(MWindow.class);
		window2.setElementId(contributedElementId);

		// add window1 to app and window2 to fragment
		application.getChildren().add(window1);
		fragment.getElements().add(window2);

		// set the same resource xmi id to window1 and window2
		final String xmiId = "testFragments_existingXMIID_XMIID";
		appResource.setID((EObject) window1, xmiId);
		fragmentResource.setID((EObject) window2, xmiId);
		final String contributorURI = "testFragments_existingElementID_contribURI";
		window1.setContributorURI(contributorURI);
		window2.setContributorURI(contributorURI);
		List<MApplicationElement> elements = assembler.processModelFragment(fragment,
				contributorURI, true);

		// fragment wasn't merged as the contributed element was already part of
		// the application model
		assertEquals(0, elements.size());

		MUIElement found = modelService.find(contributedElementId, application);
		assertEquals(window1, found);

		assertEquals(0, logMessages.size());
	}

	/**
	 * Tests that an element which exists in the model but differs from the
	 * contributed one, e.g. because it was changed at runtime and restored from
	 * the persisted state, is not replaced by the contributed element.
	 */
	@Test
	public void testFragments_existingXMIID_checkExists_modifiedElement() throws Exception {
		// create fragment
		MStringModelFragment fragment = MFragmentFactory.INSTANCE.createStringModelFragment();
		fragment.setFeaturename("children");
		fragment.setParentElementId("org.eclipse.e4.ui.tests.modelassembler.app");
		// create fragment resource
		E4XMIResource fragmentResource = (E4XMIResource) factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		fragmentResource.getContents().add((EObject) fragment);

		final String contributedElementId = "testFragments_existingXMIID_modifiedElement-contributedWindow";
		MWindow window1 = modelService.createModelElement(MWindow.class);
		window1.setElementId(contributedElementId);
		window1.setLabel("persisted");
		MWindow window2 = modelService.createModelElement(MWindow.class);
		window2.setElementId(contributedElementId);
		window2.setLabel("contributed");

		// add window1 to app and window2 to fragment
		application.getChildren().add(window1);
		fragment.getElements().add(window2);

		// set the same resource xmi id to window1 and window2
		final String xmiId = "testFragments_existingXMIID_modifiedElement_XMIID";
		appResource.setID((EObject) window1, xmiId);
		fragmentResource.setID((EObject) window2, xmiId);
		List<MApplicationElement> elements = assembler.processModelFragment(fragment,
				"testFragments_existingXMIID_modifiedElement_contribURI", true);

		assertEquals(0, elements.size());
		assertEquals(1, application.getChildren().size());
		MUIElement found = modelService.find(contributedElementId, application);
		assertEquals(window1, found);
		assertEquals("persisted", window1.getLabel());
		assertEquals(xmiId, appResource.getID((EObject) window1));

		assertEquals(0, logMessages.size());
	}

	/**
	 * Tests that fragments configured to be merged only if their elements don't
	 * exist yet still merge the elements which are missing in the model.
	 */
	@Test
	public void testFragments_checkExists_mergesMissingElements() throws Exception {
		// create fragment
		MStringModelFragment fragment = MFragmentFactory.INSTANCE.createStringModelFragment();
		fragment.setFeaturename("children");
		fragment.setParentElementId("org.eclipse.e4.ui.tests.modelassembler.app");
		// create fragment resource
		E4XMIResource fragmentResource = (E4XMIResource) factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		fragmentResource.getContents().add((EObject) fragment);

		final String existingElementId = "testFragments_checkExists_mergesMissingElements-existingWindow";
		MWindow existingWindow = modelService.createModelElement(MWindow.class);
		existingWindow.setElementId(existingElementId);
		MWindow contributedExistingWindow = modelService.createModelElement(MWindow.class);
		contributedExistingWindow.setElementId(existingElementId);
		final String missingElementId = "testFragments_checkExists_mergesMissingElements-missingWindow";
		MWindow missingWindow = modelService.createModelElement(MWindow.class);
		missingWindow.setElementId(missingElementId);

		application.getChildren().add(existingWindow);
		fragment.getElements().add(contributedExistingWindow);
		fragment.getElements().add(missingWindow);

		final String existingXmiId = "testFragments_checkExists_mergesMissingElements_existingXMIID";
		appResource.setID((EObject) existingWindow, existingXmiId);
		fragmentResource.setID((EObject) contributedExistingWindow, existingXmiId);
		final String missingXmiId = "testFragments_checkExists_mergesMissingElements_missingXMIID";
		fragmentResource.setID((EObject) missingWindow, missingXmiId);
		final String contributorURI = "testFragments_checkExists_mergesMissingElements_contribURI";
		List<MApplicationElement> elements = assembler.processModelFragment(fragment, contributorURI, true);

		assertEquals(List.of(missingWindow), elements);
		assertEquals(2, application.getChildren().size());
		assertEquals(existingWindow, modelService.find(existingElementId, application));
		assertEquals(missingWindow, modelService.find(missingElementId, application));
		assertEquals(contributorURI, missingWindow.getContributorURI());
		assertEquals(missingXmiId, appResource.getID((EObject) missingWindow));

		assertEquals(0, logMessages.size());
	}

	/**
	 * Tests that an element merged by a fragment references the existing element
	 * of the model, if the element it references is contributed by another
	 * fragment and is not merged because it already exists.
	 */
	@Test
	public void testFragments_checkExists_referenceToExistingElement() throws Exception {
		checkReferenceToExistingElement(false);
	}

	/**
	 * Tests that an element merged by a fragment references the existing element
	 * of the model, if the fragment contributing the referenced element is
	 * processed afterwards and not merged because its element already exists.
	 */
	@Test
	public void testFragments_checkExists_referenceToExistingElement_processedLater() throws Exception {
		checkReferenceToExistingElement(true);
	}

	private void checkReferenceToExistingElement(boolean handlerFragmentFirst) {
		// create the fragments contributing a command and a handler referencing it
		MStringModelFragment commandFragment = MFragmentFactory.INSTANCE.createStringModelFragment();
		commandFragment.setFeaturename("commands");
		commandFragment.setParentElementId(APPLICATION_ID);
		MStringModelFragment handlerFragment = MFragmentFactory.INSTANCE.createStringModelFragment();
		handlerFragment.setFeaturename("handlers");
		handlerFragment.setParentElementId(APPLICATION_ID);
		MModelFragments fragments = MFragmentFactory.INSTANCE.createModelFragments();
		fragments.getFragments().add(commandFragment);
		fragments.getFragments().add(handlerFragment);
		// create fragment resource
		E4XMIResource fragmentResource = (E4XMIResource) factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		fragmentResource.getContents().add((EObject) fragments);

		final String commandElementId = "testFragments_checkExists_referenceToExistingElement-command";
		MCommand existingCommand = modelService.createModelElement(MCommand.class);
		existingCommand.setElementId(commandElementId);
		MCommand contributedCommand = modelService.createModelElement(MCommand.class);
		contributedCommand.setElementId(commandElementId);
		MHandler handler = modelService.createModelElement(MHandler.class);
		handler.setElementId("testFragments_checkExists_referenceToExistingElement-handler");
		handler.setCommand(contributedCommand);

		// add the existing command to app, the contributed one and the handler to the fragments
		application.getCommands().add(existingCommand);
		commandFragment.getElements().add(contributedCommand);
		handlerFragment.getElements().add(handler);

		// set the same resource xmi id to both commands
		final String commandXmiId = "testFragments_checkExists_referenceToExistingElement_commandXMIID";
		appResource.setID((EObject) existingCommand, commandXmiId);
		fragmentResource.setID((EObject) contributedCommand, commandXmiId);
		fragmentResource.setID((EObject) handler, "testFragments_checkExists_referenceToExistingElement_handlerXMIID");

		final String contributorURI = "testFragments_checkExists_referenceToExistingElement_contribURI";
		if (handlerFragmentFirst) {
			assertEquals(List.of(handler), assembler.processModelFragment(handlerFragment, contributorURI, true));
			assertEquals(0, assembler.processModelFragment(commandFragment, contributorURI, true).size());
		} else {
			assertEquals(0, assembler.processModelFragment(commandFragment, contributorURI, true).size());
			assertEquals(List.of(handler), assembler.processModelFragment(handlerFragment, contributorURI, true));
		}

		assertEquals(List.of(existingCommand), application.getCommands());
		assertEquals(List.of(handler), application.getHandlers());
		assertEquals(existingCommand, handler.getCommand());

		assertEquals(0, logMessages.size());
	}

	/**
	 * Tests that an element which exists in the model is not merged if its
	 * contents reference each other, e.g. a container its selected element.
	 */
	@Test
	public void testFragments_checkExists_referencesWithinExistingElement() throws Exception {
		// create fragment
		MStringModelFragment fragment = MFragmentFactory.INSTANCE.createStringModelFragment();
		fragment.setFeaturename("children");
		fragment.setParentElementId(APPLICATION_ID);
		// create fragment resource
		E4XMIResource fragmentResource = (E4XMIResource) factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		fragmentResource.getContents().add((EObject) fragment);

		final String windowElementId = "testFragments_checkExists_referencesWithinExistingElement-window";
		final String stackElementId = "testFragments_checkExists_referencesWithinExistingElement-stack";
		MWindow existingWindow = modelService.createModelElement(MWindow.class);
		existingWindow.setElementId(windowElementId);
		MPartStack existingStack = modelService.createModelElement(MPartStack.class);
		existingStack.setElementId(stackElementId);
		existingWindow.getChildren().add(existingStack);
		existingWindow.setSelectedElement(existingStack);
		MWindow contributedWindow = modelService.createModelElement(MWindow.class);
		contributedWindow.setElementId(windowElementId);
		MPartStack contributedStack = modelService.createModelElement(MPartStack.class);
		contributedStack.setElementId(stackElementId);
		contributedWindow.getChildren().add(contributedStack);
		contributedWindow.setSelectedElement(contributedStack);

		// add the existing window to app and the contributed one to the fragment
		application.getChildren().add(existingWindow);
		fragment.getElements().add(contributedWindow);

		// set the same resource xmi ids to the windows and their stacks
		final String windowXmiId = "testFragments_checkExists_referencesWithinExistingElement_windowXMIID";
		final String stackXmiId = "testFragments_checkExists_referencesWithinExistingElement_stackXMIID";
		appResource.setID((EObject) existingWindow, windowXmiId);
		appResource.setID((EObject) existingStack, stackXmiId);
		fragmentResource.setID((EObject) contributedWindow, windowXmiId);
		fragmentResource.setID((EObject) contributedStack, stackXmiId);
		List<MApplicationElement> elements = assembler.processModelFragment(fragment,
				"testFragments_checkExists_referencesWithinExistingElement_contribURI", true);

		assertEquals(0, elements.size());
		assertEquals(List.of(existingWindow), application.getChildren());
		assertEquals(List.of(existingStack), existingWindow.getChildren());
		assertEquals(existingStack, existingWindow.getSelectedElement());

		assertEquals(0, logMessages.size());
	}

	/**
	 * Tests that fragments configured to be always merged are correctly
	 * contributed to the application model, even if the model already contains
	 * the contributed element.
	 */
	@Test
	public void testFragments_existingXMIID_ignoreExists() throws Exception {
		// create fragment
		MStringModelFragment fragment = MFragmentFactory.INSTANCE.createStringModelFragment();
		fragment.setFeaturename("children");
		fragment.setParentElementId("org.eclipse.e4.ui.tests.modelassembler.app");
		// create fragment resource
		E4XMIResource fragmentResource = (E4XMIResource) factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		fragmentResource.getContents().add((EObject) fragment);

		final String contributedElementId = "testFragments_existingElementID-contributedWindow";
		MWindow window1 = modelService.createModelElement(MWindow.class);
		window1.setElementId(contributedElementId);
		MWindow window2 = modelService.createModelElement(MWindow.class);
		window2.setElementId(contributedElementId);

		// add window1 to app and window2 to fragment
		application.getChildren().add(window1);
		fragment.getElements().add(window2);

		// set the same resource xmi id to window1 and window2
		final String xmiId = "testFragments_existingXMIID_XMIID";
		appResource.setID((EObject) window1, xmiId);
		fragmentResource.setID((EObject) window2, xmiId);

		final String contributorID = "testFragments_existingElementID_contribURI";
		List<MApplicationElement> elements = assembler.processModelFragment(fragment, contributorID, false);

		assertEquals(elements.size(), 1);
		MUIElement found = modelService.find(contributedElementId, application);
		assertEquals(found, window2);
		assertEquals(contributorID, found.getContributorURI());

		assertEquals(0, logMessages.size());
	}

	/** Tests that correctly configured imports are correctly handled. */
	@Test
	public void testImports() {
		List<MApplicationElement> imports = new ArrayList<>();
		List<MApplicationElement> addedElements = new ArrayList<>();

		final String windowElementId = "testImports_emptyList_window1";
		MTrimmedWindow importWindow1 = modelService.createModelElement(MTrimmedWindow.class);
		importWindow1.setElementId(windowElementId);
		MModelFragments fragment = MFragmentFactory.INSTANCE.createModelFragments();
		fragment.getImports().add(importWindow1);
		imports.add(importWindow1);

		MTrimmedWindow realWindow1 = modelService.createModelElement(MTrimmedWindow.class);
		realWindow1.setElementId(windowElementId);
		application.getChildren().add(realWindow1);

		MPlaceholder placeholder = modelService.createModelElement(MPlaceholder.class);
		placeholder.setRef(importWindow1);
		addedElements.add(placeholder);

		assembler.resolveImports(imports, addedElements);
		assertEquals(realWindow1, placeholder.getRef());

		assertEquals(0, logMessages.size());
	}

	/** Tests the processing of an import with a null/incorrect element id. */
	@Test
	public void testImports_noImportElementId() throws Exception {
		List<MApplicationElement> imports = new ArrayList<>();
		List<MApplicationElement> addedElements = new ArrayList<>();

		MTrimmedWindow importWindow1 = modelService.createModelElement(MTrimmedWindow.class);
		importWindow1.setElementId(null);
		MModelFragments fragment = MFragmentFactory.INSTANCE.createModelFragments();
		fragment.getImports().add(importWindow1);
		imports.add(importWindow1);
		MTrimmedWindow realWindow1 = modelService.createModelElement(MTrimmedWindow.class);
		realWindow1.setElementId("testImports_emptyList_window1");
		application.getChildren().add(realWindow1);

		MPlaceholder placeholder = modelService.createModelElement(MPlaceholder.class);
		placeholder.setRef(importWindow1);
		addedElements.add(placeholder);

		CountDownLatch countDownLatch = new CountDownLatch(1);
		this.logListener.countDownLatch = countDownLatch;

		assembler.resolveImports(imports, addedElements);
		assertEquals(null, placeholder.getRef());

		boolean completed = countDownLatch.await(COUNTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
		assertTrue(completed, "Timeout - no event received");

		assertEquals(1, logMessages.size());
		assertEquals("Could not resolve import for null", logMessages.poll());
	}

	/**
	 * Make sure that all fragments and imports are resolved before the
	 * post-processors are run. For reference, see
	 * <a href="https://bugs.eclipse.org/475934">bug 475934</a>.
	 *
	 * @throws Exception
	 *             if anything went wrong during the test
	 */
	@Test
	public void testModelProcessingOrder() throws Exception {
		/* setup application model */
		/* this creates a window, containing a part and an area */
		MTrimmedWindow trimmedWindow = modelService.createModelElement(MTrimmedWindow.class);
		trimmedWindow.setElementId("testModelProcessingOrder-trimmedWindow");
		application.getChildren().add(trimmedWindow);
		MPart part = modelService.createModelElement(MPart.class);
		part.setElementId("testModelProcessingOrder-part");
		trimmedWindow.getChildren().add(part);
		MArea area = modelService.createModelElement(MArea.class);
		area.setElementId("testModelProcessingOrder-area");
		trimmedWindow.getChildren().add(area);

		/* contribute fragment with imports and post-processor */
		IContributor contributor = ContributorFactorySimple.createContributor(BUNDLE_SYMBOLIC_NAME);
		IExtensionRegistry registry = createTestExtensionRegistry();
		assembler.setExtensionRegistry(registry);

		assertEquals(0, registry.getConfigurationElementsFor(EXTENSION_POINT_ID).length);
		// The fragment contributes a Placeholder to the application's Area. The
		// Placeholder references the Part that we created above.
		// Besides the Placeholder, the xml also contributes a
		// post-processor(org.eclipse.e4.ui.tests.workbench.ModelAssemblerProcessingOrderPostProcessor).
		// It will iterate over the elements of the application model and will
		// make sure that no imports are left unresolved. The post-processor
		// will throw an error if such elements are found and this test will
		// fail.
		String dataFilePath = "org.eclipse.e4.ui.tests/data/ModelAssembler/modelProcessingOrder.xml";
		registry.addContribution(getContentsAsInputStream(dataFilePath), contributor, false, null, null, null);

		assembler.processModel(true);

		// the testing was done in the post-processor; if we didn't fail there,
		// everything went fine.
		assertEquals(0, logMessages.size());
	}

	/**
	 * Tests that pre-processors running from a non-persisted state that are
	 * marked as "always" are executed.
	 */
	@Test
	public void testPreProcessor_nonPersistedState_always() throws Exception {
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processors_always.xml", true, false);
		assertEquals(1, application.getDescriptors().size());
		assertEquals("simpleprocessor.pre", application.getDescriptors().get(0).getElementId());
		assertEquals(0, logMessages.size());
	}

	/**
	 * Tests that pre-processors running from a persisted state that are marked
	 * as "always" are executed.
	 */
	@Test
	public void testPreProcessor_persistedState_always() throws Exception {
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processors_always.xml", false, false);
		assertEquals(1, application.getDescriptors().size());
		assertEquals("simpleprocessor.pre", application.getDescriptors().get(0).getElementId());
		assertEquals(0, logMessages.size());
	}

	/**
	 * Tests that pre-processors running from a non-persisted state and marked
	 * as "initial" are executed.
	 */
	@Test
	public void testPreProcessor_nonPersistedState_initial() throws Exception {
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processors_initial.xml", true, false);
		assertEquals(1, application.getDescriptors().size());
		assertEquals("simpleprocessor.pre", application.getDescriptors().get(0).getElementId());
		assertEquals(0, logMessages.size());
	}

	/**
	 * Tests that pre-processors running from a persisted state and marked as
	 * "initial" are not executed.
	 */
	@Test
	public void testPreProcessor_persistedState_initial() throws Exception {
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processors_initial.xml", false, false);
		assertEquals(0, application.getDescriptors().size());
		assertEquals(0, logMessages.size());
	}

	/**
	 * Tests the execution of post-processors that should always be applied,
	 * running from a persisted state.
	 */
	@Test
	public void testPostProcessor_persistedState_always() throws Exception {
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processors_always.xml", false, true);
		assertEquals(1, application.getDescriptors().size());
		assertEquals("simpleprocessor.post", application.getDescriptors().get(0).getElementId());
		assertEquals(0, logMessages.size());
	}

	/**
	 * Tests the execution of post-processors that should always be applied,
	 * running from a non-persisted state.
	 */
	@Test
	public void testPostProcessor_nonPersistedState_always() throws Exception {
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processors_always.xml", true, true);
		assertEquals(1, application.getDescriptors().size());
		assertEquals("simpleprocessor.post", application.getDescriptors().get(0).getElementId());
		assertEquals(0, logMessages.size());
	}

	/**
	 * Tests the execution of post-processors running from a non-persisted state
	 * declared to be applied as "initial".
	 */
	@Test
	public void testPostProcessor_NonPersistedState_initial() throws Exception {
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processors_initial.xml", true, true);
		assertEquals(1, application.getDescriptors().size());
		assertEquals("simpleprocessor.post", application.getDescriptors().get(0).getElementId());
		assertEquals(0, logMessages.size());
	}

	/**
	 * Processors running from a persisted state declared to be applied as
	 * "initial" should not be run.
	 */
	@Test
	public void testPostProcessor_persistedState_initial() throws Exception {
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processors_initial.xml", false, true);
		assertEquals(0, application.getDescriptors().size());
		assertEquals(0, logMessages.size());
	}

	/**
	 * Test handling of processor contribution without any processor class. A
	 * warning should be logged in such cases.
	 */
	@Test
	public void testProcessor_noProcessor() throws Exception {
		CountDownLatch countDownLatch = new CountDownLatch(1);
		this.logListener.countDownLatch = countDownLatch;

		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processor_null.xml", true, false);
		boolean completed = countDownLatch.await(COUNTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
		assertTrue(completed, "Timeout - no event received");

		assertEquals(1, logMessages.size());
		assertEquals("Unable to create processor null from org.eclipse.e4.ui.tests", logMessages.poll());

		assertEquals(0, application.getDescriptors().size());
		assertEquals(0, logMessages.size());
	}

	/**
	 * Tests a contribution containing an nonexistent processor class. A warning
	 * should be logged in such cases.
	 */
	@Test
	public void testProcessor_processorNotFound() throws Exception {
		CountDownLatch countDownLatch = new CountDownLatch(1);
		this.logListener.countDownLatch = countDownLatch;

		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processor_wrongProcessorClass.xml", true, false);
		boolean completed = countDownLatch.await(COUNTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
		assertTrue(completed, "Timeout - no event received");

		assertEquals(1, logMessages.size());
		assertEquals(
				"Unable to create processor org.eclipse.e4.ui.tests.workbench.SimplePreProcessor_NotFound from org.eclipse.e4.ui.tests",
				logMessages.poll());

		assertEquals(0, application.getDescriptors().size());
		assertEquals(0, logMessages.size());
	}

	/**
	 * Tests a processor contribution that adds to the context an element with
	 * an id that does not exist in the application model. A warning should be
	 * logged, but the processors should still be executed.
	 */
	@Test
	public void testProcessor_wrongAppId() throws Exception {
		CountDownLatch countDownLatch = new CountDownLatch(1);
		this.logListener.countDownLatch = countDownLatch;

		application.setElementId("newID");
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processors_initial.xml", true, true);
		boolean completed = countDownLatch.await(COUNTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
		assertTrue(completed, "Timeout - no event received");

		assertEquals(1, logMessages.size());
		assertEquals("Could not find element with id org.eclipse.e4.ui.tests.modelassembler.app", logMessages.poll());

		assertEquals(0, logMessages.size());
		assertEquals(1, application.getDescriptors().size());
		assertEquals("simpleprocessor.post", application.getDescriptors().get(0).getElementId());
	}

	private void testProcessor(String filePath, boolean initial, boolean afterFragments) throws Exception {
		IContributor contributor = ContributorFactorySimple.createContributor(BUNDLE_SYMBOLIC_NAME);
		IExtensionRegistry registry = createTestExtensionRegistry();
		assertEquals(0, registry.getConfigurationElementsFor(EXTENSION_POINT_ID).length);
		registry.addContribution(getContentsAsInputStream(filePath), contributor, false, null, null, null);
		IExtensionPoint extPoint = registry.getExtensionPoint(EXTENSION_POINT_ID);
		IExtension[] extensions = new ExtensionsSort().sort(extPoint.getExtensions());
		assertEquals(0, application.getDescriptors().size());
		assembler.runProcessors(extensions, initial, afterFragments);
	}

	private IExtensionRegistry createTestExtensionRegistry() {
		IExtensionRegistry defaultRegistry = RegistryFactory.getRegistry();
		IExtensionPoint extensionPoint = defaultRegistry.getExtensionPoint(EXTENSION_POINT_ID);
		ExtensionRegistry registry = (ExtensionRegistry) RegistryFactory.createRegistry(null, null, null);
		registry.addExtensionPoint(extensionPoint.getUniqueIdentifier(), extensionPoint.getContributor(), false,
				extensionPoint.getLabel(), extensionPoint.getSchemaReference(), null);
		appContext.set(IExtensionRegistry.class, registry);
		return registry;
	}

	private InputStream getContentsAsInputStream(String filePath) throws IOException {
		URI uri = URI.createPlatformPluginURI(filePath, true);
		return URIConverter.INSTANCE.createInputStream(uri);
	}
}
