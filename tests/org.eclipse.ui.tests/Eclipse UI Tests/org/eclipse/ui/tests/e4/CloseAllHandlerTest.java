/*******************************************************************************
* Copyright (c) 2024 Feilim Breatnach and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which accompanies this distribution,
* and is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors: Feilim Breatnach, Pilz Ireland - PR #2360
*******************************************************************************/

package org.eclipse.ui.tests.e4;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.CloseAllHandler;
import org.eclipse.ui.internal.Workbench;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * Tests the enabled when and execution logic within the
 * {@link CloseAllHandler}.
 */
public class CloseAllHandlerTest {

	private IEclipseContext applicationContext;
	private MApplication application;
	private EModelService modelService;
	private EPartService partService;

	private static final String TEST_COMPATIBILITY_LAYER_EDITOR_ID = "org.eclipse.ui.tests.TitleTestEditor"; //$NON-NLS-1$
	private static final String CLOSE_ALL_EDITORS_COMMAND_ID = "org.eclipse.ui.file.closeAll"; //$NON-NLS-1$
	private static final String DUMMY_E4_PART_ID = "e4_dummy_part_editor"; //$NON-NLS-1$

	@Before
	public void setUp() throws Exception {
		application = getApplicationModel();
		applicationContext = application.getContext();
		modelService = applicationContext.get(EModelService.class);
		partService = application.getContext().get(EPartService.class);
	}

	private MApplication getApplicationModel() {
		BundleContext bundleContext = FrameworkUtil.getBundle(IWorkbench.class).getBundleContext();
		ServiceReference<IWorkbench> reference = bundleContext.getServiceReference(IWorkbench.class);
		return bundleContext.getService(reference).getApplication();
	}

	/**
	 * Tests the enabled when and execution logic within the
	 * {@link CloseAllHandler}.
	 *
	 * Scenario 1: compatibility layer type editor is closed via the handler (and
	 * the enablement of handler is checked).
	 *
	 * Scenario 2: E4 style part contribution which is tagged as representing an
	 * 'editor' is closed via the handler (and the enablement of handler is
	 * checked).
	 *
	 * Scenario 3: a mix of an open compatibility layer type editor *and* an E4
	 * style part contribution which is tagged as representing an 'editor' are both
	 * closed via the handler (and the enablement of handler is checked).
	 *
	 * @throws PartInitException
	 */
	@Test
	public void testCloseMixedEditorTypes() throws PartInitException {
		EHandlerService handlerService = application.getContext().get(EHandlerService.class);
		ECommandService commandService = application.getContext().get(ECommandService.class);

		Command closeAllCommand = commandService.getCommand(CLOSE_ALL_EDITORS_COMMAND_ID);
		final ParameterizedCommand parameterizedCommand = ParameterizedCommand.generateCommand(closeAllCommand,
				Collections.emptyMap());

		// verify the close all editors handler enabledment is false (no editors are
		// open yet!)
		boolean canExecute = handlerService.canExecute(parameterizedCommand);
		assertFalse(canExecute);

		// scenario 1: open a compatibility layer editor
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		assertNotNull("Active workbench window not found.", window);

		IFileEditorInput input = new DummyFileEditorInput();
		window.getActivePage().openEditor(input, TEST_COMPATIBILITY_LAYER_EDITOR_ID);

		// verify the close all handler is enabled now (since a dummy compatibility
		// layer editor has been opened)
		canExecute = handlerService.canExecute(parameterizedCommand);
		assertTrue(canExecute);

		IEditorPart compatEditor = window.getActivePage().findEditor(input);
		assertNotNull(compatEditor);
		handlerService.executeHandler(parameterizedCommand);
		compatEditor = window.getActivePage().findEditor(input);
		assertNull(compatEditor);

		// verify the close all handler is *not* enabled now (since compatibility layer
		// editor has been closed)
		canExecute = handlerService.canExecute(parameterizedCommand);
		assertFalse(canExecute);

		// scenario 2: e4 part descriptor contribution
		MPartDescriptor partDescriptor = createDummyPartDescriptor();
		application.getDescriptors().add(partDescriptor);

		// open our e4 part which represents an editor
		MPart dummyPart = createAndOpenE4Part(partDescriptor);

		// verify the close all handler is enabled now (since dummy editor has been
		// opened)
		canExecute = handlerService.canExecute(parameterizedCommand);
		assertTrue(canExecute);

		// close all editors (dummy editor should close!)
		dummyPart = partService.findPart(DUMMY_E4_PART_ID);
		assertNotNull(dummyPart);
		handlerService.executeHandler(parameterizedCommand);
		dummyPart = partService.findPart(DUMMY_E4_PART_ID);
		assertNull(dummyPart);

		// verify the close all handler is *not* enabled now (since dummy editor has
		// been closed)
		canExecute = handlerService.canExecute(parameterizedCommand);
		assertFalse(canExecute);

		// scenario 3:
		// finally: re-open both the compatibility layer editor *and* the dummy e4 part
		// which represents an editor, and verify they are *both* closed when we invoked
		// the close all editors handler
		dummyPart = createAndOpenE4Part(partDescriptor);
		window.getActivePage().openEditor(input, TEST_COMPATIBILITY_LAYER_EDITOR_ID);

		compatEditor = window.getActivePage().findEditor(input);
		assertNotNull(compatEditor);
		dummyPart = partService.findPart(DUMMY_E4_PART_ID);
		assertNotNull(dummyPart);

		canExecute = handlerService.canExecute(parameterizedCommand);
		assertTrue(canExecute);

		// close all editors
		handlerService.executeHandler(parameterizedCommand);
		canExecute = handlerService.canExecute(parameterizedCommand);
		assertFalse(canExecute);

		// verify they are all closed
		compatEditor = window.getActivePage().findEditor(input);
		assertNull(compatEditor);
		dummyPart = partService.findPart(DUMMY_E4_PART_ID);
		assertNull(dummyPart);
	}

	private MPart createAndOpenE4Part(MPartDescriptor partDescriptor) {
		Optional<MPartStack> primaryPartStack = findPrimaryConfiguationAreaPartStack(application, modelService);

		if (primaryPartStack.isEmpty()) {
			fail("Test cannot proceed as the primary part stack could not be found in the application.");
		}

		MPart dummyPart = partService.createPart(partDescriptor.getElementId());
		primaryPartStack.get().getChildren().add(dummyPart);
		partService.showPart(dummyPart.getElementId(), PartState.ACTIVATE);
		partService.bringToTop(dummyPart);

		return dummyPart;
	}

	private MPartDescriptor createDummyPartDescriptor() {
		MPartDescriptor partDescriptor = modelService.createModelElement(MPartDescriptor.class);
		partDescriptor.setAllowMultiple(true);
		partDescriptor.setElementId(DUMMY_E4_PART_ID);
		partDescriptor.setCloseable(true);
		partDescriptor.setLabel(DUMMY_E4_PART_ID);
		partDescriptor.getTags().add(Workbench.EDITOR_TAG);
		partDescriptor.getTags().add(EPartService.REMOVE_ON_HIDE_TAG);
		partDescriptor.setContributionURI("bundleclass://org.eclipse.ui.tests/org.eclipse.ui.tests.e4.DummyEditor");

		return partDescriptor;
	}

	private Optional<MPartStack> findPrimaryConfiguationAreaPartStack(MApplication application,
			EModelService modelService) {
		List<MArea> areaCandidates = modelService.findElements(application,
				IPageLayout.ID_EDITOR_AREA, MArea.class, null,
				EModelService.IN_SHARED_ELEMENTS);
		if (areaCandidates.size() == 1) {
			MArea primaryArea = areaCandidates.get(0);
			for (MPartSashContainerElement element : primaryArea.getChildren()) {
				if (element instanceof MPartStack partStack) {
					return Optional.of(partStack);
				} else if (element instanceof MPartSashContainer sash) {
					return sash.getChildren().stream().filter(c -> c instanceof MPartStack)
							.map(c -> (MPartStack) c).findFirst();
				}
			}
		}

		return Optional.empty();
	}

	private class DummyFileEditorInput implements IFileEditorInput {
		@Override
		public boolean exists() {
			return true;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public String getName() {
			return "MyInputFile";
		}

		@Override
		public IPersistableElement getPersistable() {
			return null;
		}

		@Override
		public String getToolTipText() {
			return "My Input File";
		}

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}

		@Override
		public IFile getFile() {
			return null;
		}

		@Override
		public IStorage getStorage() {
			return null;
		}
	}
}