/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.reconciler;

import java.util.Collection;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerPlaceholderTest extends
		ModelReconcilerTest {

	public void testPlaceholder_Ref_Set() {
		MApplication application = createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		MPerspective perspective = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		MPlaceholder placeholder = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();

		application.getChildren().add(window);
		application.setSelectedElement(window);

		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		perspective.getChildren().add(placeholder);
		perspective.setSelectedElement(placeholder);

		window.getSharedElements().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		placeholder.setRef(part);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		perspectiveStack = (MPerspectiveStack) window.getChildren().get(0);
		perspective = perspectiveStack.getChildren().get(0);
		placeholder = (MPlaceholder) perspective.getChildren().get(0);
		part = (MPart) window.getSharedElements().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(1, window.getChildren().size());
		assertEquals(perspectiveStack, window.getChildren().get(0));
		assertEquals(1, perspectiveStack.getChildren().size());
		assertEquals(perspective, perspectiveStack.getChildren().get(0));
		assertEquals(1, perspective.getChildren().size());
		assertEquals(placeholder, perspective.getChildren().get(0));
		assertEquals(1, window.getSharedElements().size());
		assertEquals(part, window.getSharedElements().get(0));
		assertNull(placeholder.getRef());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(1, window.getChildren().size());
		assertEquals(perspectiveStack, window.getChildren().get(0));
		assertEquals(1, perspectiveStack.getChildren().size());
		assertEquals(perspective, perspectiveStack.getChildren().get(0));
		assertEquals(1, perspective.getChildren().size());
		assertEquals(placeholder, perspective.getChildren().get(0));
		assertEquals(1, window.getSharedElements().size());
		assertEquals(part, window.getSharedElements().get(0));
		assertEquals(part, placeholder.getRef());
	}

	public void testPlaceholder_Ref_Unset() {
		MApplication application = createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		MPerspective perspective = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		MPlaceholder placeholder = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();

		application.getChildren().add(window);
		application.setSelectedElement(window);

		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		perspective.getChildren().add(placeholder);
		perspective.setSelectedElement(placeholder);

		window.getSharedElements().add(part);

		placeholder.setRef(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		placeholder.setRef(null);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		perspectiveStack = (MPerspectiveStack) window.getChildren().get(0);
		perspective = perspectiveStack.getChildren().get(0);
		placeholder = (MPlaceholder) perspective.getChildren().get(0);
		part = (MPart) window.getSharedElements().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(1, window.getChildren().size());
		assertEquals(perspectiveStack, window.getChildren().get(0));
		assertEquals(1, perspectiveStack.getChildren().size());
		assertEquals(perspective, perspectiveStack.getChildren().get(0));
		assertEquals(1, perspective.getChildren().size());
		assertEquals(placeholder, perspective.getChildren().get(0));
		assertEquals(1, window.getSharedElements().size());
		assertEquals(part, window.getSharedElements().get(0));
		assertEquals(part, placeholder.getRef());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(1, window.getChildren().size());
		assertEquals(perspectiveStack, window.getChildren().get(0));
		assertEquals(1, perspectiveStack.getChildren().size());
		assertEquals(perspective, perspectiveStack.getChildren().get(0));
		assertEquals(1, perspective.getChildren().size());
		assertEquals(placeholder, perspective.getChildren().get(0));
		assertEquals(1, window.getSharedElements().size());
		assertEquals(part, window.getSharedElements().get(0));
		assertNull(placeholder.getRef());
	}
}
