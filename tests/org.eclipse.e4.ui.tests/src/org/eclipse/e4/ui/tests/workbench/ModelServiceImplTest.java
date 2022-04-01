/*******************************************************************************
 * Copyright (c) 2019, 2022 KGU-Consulting GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stefan Nöbauer  - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import java.util.List;
import javax.inject.Inject;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.tests.rules.WorkbenchContextRule;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

public class ModelServiceImplTest {
	private static final String DUMMY_EDITOR_ID = "dummyEditor";

	@Rule
	public WorkbenchContextRule contextRule = new WorkbenchContextRule();

	@Inject
	private EModelService modelService;

	private MPart editor;

	@Before
	public void setUp() throws Exception {
		editor = modelService.createModelElement(MPart.class);
		editor.setElementId(DUMMY_EDITOR_ID);
		editor.getTags().add("Editor");
	}

	@Test
	public void testModelServiceFindElementsWithEditorInSharedElements() {
		// arrange
		MApplication app = createAppWithEditorInSharedElements();

		// act
		List<MPart> findElements = modelService.findElements(app, DUMMY_EDITOR_ID, MPart.class, null,
				EModelService.IN_SHARED_ELEMENTS);

		// assert
		assertTrue(findElements.contains(editor));
	}

	private MApplication createAppWithEditorInSharedElements() {
		MApplication app = modelService.createModelElement(MApplication.class);

		MTrimmedWindow trimmedWindow = modelService.createModelElement(MTrimmedWindow.class);

		MArea editorArea = modelService.createModelElement(MArea.class);
		editorArea.setElementId("org.eclipse.ui.editorss");

		MPartStack editorPartStack = modelService.createModelElement(MPartStack.class);
		editorPartStack.setElementId("org.eclipse.e4.primaryDataStack");

		editorPartStack.getChildren().add(editor);
		editorArea.getChildren().add(editorPartStack);
		trimmedWindow.getSharedElements().add(editorArea);
		app.getChildren().add(trimmedWindow);

		return app;
	}

	@Test
	public void testRemovePerspectiveModelWhenPerspectiveHasNoParent() {
		// given
		var perspective = mock(MPerspective.class);
		doReturn(null).when(perspective).getParent();

		// when
		modelService.removePerspectiveModel(perspective, mock(MWindow.class));

		// then
		Mockito.verify(perspective, never()).setToBeRendered(false);
	}
}
