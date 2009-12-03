/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MKeyBinding;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerKeyBindingTest extends ModelReconcilerTest {

	private void testKeySequence_KeySequence(String before, String after) {
		String applicationId = createId();
		String commandId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);
		MCommand command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		MKeyBinding keyBinding = MApplicationFactory.eINSTANCE
				.createKeyBinding();
		keyBinding.setKeySequence(before);
		keyBinding.setCommand(command);
		application.getBindings().add(keyBinding);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		keyBinding.setKeySequence(after);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);
		command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		keyBinding = MApplicationFactory.eINSTANCE.createKeyBinding();
		keyBinding.setKeySequence(before);
		keyBinding.setCommand(command);
		application.getBindings().add(keyBinding);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(before, keyBinding.getKeySequence());

		applyAll(deltas);

		assertEquals(after, keyBinding.getKeySequence());
	}

	public void testKeySequence_KeySequence_NullNull() {
		testKeySequence_KeySequence(null, null);
	}

	public void testKeySequence_KeySequence_NullEmpty() {
		testKeySequence_KeySequence(null, "");
	}

	public void testKeySequence_KeySequence_NullString() {
		testKeySequence_KeySequence(null, "Ctrl+S");
	}

	public void testKeySequence_KeySequence_EmptyNull() {
		testKeySequence_KeySequence("", null);
	}

	public void testKeySequence_KeySequence_EmptyEmpty() {
		testKeySequence_KeySequence("", "");
	}

	public void testKeySequence_KeySequence_EmptyString() {
		testKeySequence_KeySequence("", "Ctrl+S");
	}

	public void testKeySequence_KeySequence_StringNull() {
		testKeySequence_KeySequence("Ctrl+S", null);
	}

	public void testKeySequence_KeySequence_StringEmpty() {
		testKeySequence_KeySequence("Ctrl+S", "");
	}

	public void testKeySequence_KeySequence_StringString() {
		testKeySequence_KeySequence("Ctrl+S", "Ctrl+D");
	}

}
