/*******************************************************************************
 * Copyright (c) 2023 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http:www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import static org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout.EDITOR_ONBOARDING_COMMAND;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ModeledPageLayoutTest {

	private ModeledPageLayout pageLayout;
	private MPerspective perspective;

	private MApplication application;
	private IEclipseContext context;

	@Before
	public void setUp() {
		context = createApplicationContext();

		EModelService modelService = context.get(EModelService.class);
		application = modelService.createModelElement(MApplication.class);
		application.setContext(context);
		MWindow window = modelService.createModelElement(MWindow.class);

		application.getChildren().add(window);
		application.setSelectedElement(window);

		perspective = modelService.createModelElement(MPerspective.class);

		pageLayout = new ModeledPageLayout(window, modelService, null, perspective, null, null, false);
	}

	@After
	public void tearDown() {
		context.remove(ActionSetRegistry.class);
		context.dispose();
	}

	@Test
	public void setsEditorOnboardingText() {
		pageLayout.setEditorOnboardingText("Onboarding Text");
		assertThat(perspective.getTags(),
				CoreMatchers.hasItem(ModeledPageLayout.EDITOR_ONBOARDING_TEXT + "Onboarding Text"));
	}

	@Test
	public void setsEditorOnboardingImageUri() {
		pageLayout.setEditorOnboardingImageUri("/image/uri");
		assertThat(perspective.getTags(), hasItem(ModeledPageLayout.EDITOR_ONBOARDING_IMAGE + "/image/uri"));
	}

	@Test
	public void addsEditorOnboardingCommandIds() throws Exception {
		MBindingTable bindingTable = MCommandsFactory.INSTANCE.createBindingTable();
		bindingTable.getBindings().add(createBinding("org.eclipse.ui.window.quickAccess", "Find Actions", "M1+3"));
		bindingTable.getBindings()
				.add(createBinding("org.eclipse.ui.window.showKeyAssist", "Show Key Assist", "M2+M1+L"));

		application.getBindingTables().add(bindingTable);

		pageLayout.addEditorOnboardingCommandId("org.eclipse.ui.window.quickAccess");
		pageLayout.addEditorOnboardingCommandId("org.eclipse.ui.window.showKeyAssist");

		assertThat(perspective.getTags(),
				hasItems(
						ModeledPageLayout.EDITOR_ONBOARDING_COMMAND + "Find Actions$$$"
								+ KeySequence.getInstance("M1+3").format(),
						ModeledPageLayout.EDITOR_ONBOARDING_COMMAND + "Show Key Assist$$$"
								+ KeySequence.getInstance("M2+M1+L").format()));
	}

	@Test
	public void doesNotAddUnexistingOnboardingCommandIds() throws Exception {
		application.getBindingTables().add(MCommandsFactory.INSTANCE.createBindingTable());

		pageLayout.addEditorOnboardingCommandId("org.eclipse.ui.doesnt.Exist");

		assertThat(getNumberOfOnboardingCommands(perspective.getTags()), is(0));
	}

	@Test
	public void addsUpToFiveEditorOnboardingCommandIds() throws Exception {
		MBindingTable bindingTable = MCommandsFactory.INSTANCE.createBindingTable();
		for (int i = 1; i <= 6; i++) {
			bindingTable.getBindings().add(createBinding("command" + i, "Command " + i, "M1+" + i));
		}
		application.getBindingTables().add(bindingTable);
		for (int i = 0; i <= 6; i++) {
			pageLayout.addEditorOnboardingCommandId("command" + i);
		}

		assertThat(getNumberOfOnboardingCommands(perspective.getTags()), is(5));
	}

	@Test
	public void addEditorOnboardingCommandWhenBrokenKeyBindingExsists() throws Exception {
		MBindingTable bindingTable = MCommandsFactory.INSTANCE.createBindingTable();
		MKeyBinding binding = createBinding("unknownCommand", "Unknown", "M1+1+5");
		binding.setCommand(null);
		bindingTable.getBindings().add(binding);
		bindingTable.getBindings().add(createBinding("org.eclipse.ui.window.quickAccess", "Find Actions", "M1+3"));

		application.getBindingTables().add(bindingTable);

		pageLayout.addEditorOnboardingCommandId("org.eclipse.ui.window.quickAccess");

		assertThat(perspective.getTags(), hasItem(ModeledPageLayout.EDITOR_ONBOARDING_COMMAND + "Find Actions$$$"
				+ KeySequence.getInstance("M1+3").format()));
	}

	private int getNumberOfOnboardingCommands(List<String> commands) {
		return commands.stream().filter(t -> t.startsWith(EDITOR_ONBOARDING_COMMAND)).mapToInt(i -> 1).sum();
	}

	private MKeyBinding createBinding(String elementId, String name, String keys) {
		MKeyBinding binding = MCommandsFactory.INSTANCE.createKeyBinding();
		MCommand command = MCommandsFactory.INSTANCE.createCommand();
		binding.setCommand(command);
		command.setElementId(elementId);
		command.setCommandName(name);
		binding.setKeySequence(keys);
		return binding;
	}

	private IEclipseContext createApplicationContext() {
		final IEclipseContext appContext = E4Application.createDefaultContext();
		appContext.set(ActionSetRegistry.class, WorkbenchPlugin.getDefault().getActionSetRegistry());
		return appContext;
	}
}