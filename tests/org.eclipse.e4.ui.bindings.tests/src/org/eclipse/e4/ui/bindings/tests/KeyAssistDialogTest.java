/*******************************************************************************
* Copyright (c) 2023 Ole Osterhagen and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Ole Osterhagen - Issue 654
*******************************************************************************/
package org.eclipse.e4.ui.bindings.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.bindings.internal.KeyAssistDialog;
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.junit.jupiter.api.Test;

@SuppressWarnings("restriction")
public class KeyAssistDialogTest {

	private final Binding binding1 = mockBinding("Command 1");
	private final Binding binding2 = mockBinding("Command 2");
	private final Binding binding3 = mockBinding("Command 3");

	@Test
	public void testIsShowingBindings() throws Exception {
		KeyAssistDialog keyAssistDialog = new KeyAssistDialog(mock(IEclipseContext.class), new KeyBindingDispatcher());
		keyAssistDialog.open(List.of(binding1, binding2));

		// the order is not important
		assertTrue(keyAssistDialog.isShowingBindings(List.of(binding2, binding1)));

		// different bindings
		assertFalse(keyAssistDialog.isShowingBindings(List.of(binding2, binding3)));

		keyAssistDialog.close();
		assertFalse(keyAssistDialog.isShowingBindings(List.of(binding2, binding1)));
	}

	private Binding mockBinding(String commandName) {
		ParameterizedCommand command = mock(ParameterizedCommand.class);
		try {
			when(command.getName()).thenReturn(commandName);
		} catch (NotDefinedException e) {
			throw new RuntimeException(e);
		}

		Binding binding = mock(Binding.class);
		when(binding.getParameterizedCommand()).thenReturn(command);
		when(binding.getTriggerSequence()).thenReturn(KeySequence.getInstance());
		return binding;
	}

}
