package org.eclipse.ui.tests.api;

import static org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout.EDITOR_ONBOARDING_COMMAND;
import static org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout.EDITOR_ONBOARDING_IMAGE;
import static org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout.EDITOR_ONBOARDING_TEXT;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.e4.core.commands.CommandServiceAddon;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.ModelServiceImpl;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.ContextServiceAddon;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.ui.internal.PerspectiveExtensionReader;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PerspectiveExtensionReaderTest {

	private static final String ID = "org.eclipse.ui.tests.OnboardingPerspective";

	private PerspectiveExtensionReader extensionReader;
	private ModeledPageLayout pageLayout;

	private MPerspective perspective;

	private MApplication application;

	private IEclipseContext context;

	@Before
	public void setUp() {
		extensionReader = new PerspectiveExtensionReader();

		context = createApplicationContext();

		perspective = MAdvancedFactory.INSTANCE.createPerspective();
		MWindow window = MBasicFactory.INSTANCE.createWindow();
		application = MApplicationFactory.INSTANCE.createApplication();
		application.getChildren().add(window);
		application.setContext(context);
		ModelServiceImpl modelService = new ModelServiceImpl(context);

		pageLayout = new ModeledPageLayout(window, modelService, null, perspective, null, null, false);
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	@Test
	public void extendsOnboardingTags() throws Exception {
		MBindingTable bindingTable = MCommandsFactory.INSTANCE.createBindingTable();
		bindingTable.getBindings().add(createBinding("org.eclipse.ui.window.quickAccess", "Find Actions", "M1+3"));

		application.getBindingTables().add(bindingTable);

		extensionReader.setIncludeOnlyTags(new String[] { IWorkbenchRegistryConstants.ATT_EDITOR_ONBOARDING_TEXT,
				IWorkbenchRegistryConstants.ATT_EDITOR_ONBOARDING_IMAGE,
				IWorkbenchRegistryConstants.TAG_EDITOR_ONBOARDING_COMMAND });

		extensionReader.extendLayout(null, ID, pageLayout);

		assertThat(perspective.getTags(), hasItems(//
				EDITOR_ONBOARDING_TEXT + "The onboarding text", //
				EDITOR_ONBOARDING_IMAGE + "platform:/plugin/org.eclipse.ui.tests/icons/anything.gif", //
				EDITOR_ONBOARDING_COMMAND + "Find Actions: " + KeySequence.getInstance("M1+3").format()));
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
		ContextInjectionFactory.make(CommandServiceAddon.class, appContext);
		ContextInjectionFactory.make(ContextServiceAddon.class, appContext);
		ActionSetRegistry actionSetRegistry = mock(ActionSetRegistry.class);
		appContext.set(ActionSetRegistry.class, actionSetRegistry);

		when(actionSetRegistry.getActionSets()).thenReturn(new IActionSetDescriptor[0]);
		return appContext;
	}
}