/*******************************************************************************
 * Copyright (c) 2006, 2013 Soyatec(http://www.soyatec.com) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Soyatec - initial API and implementation
 *     IBM Corporation - ongoing enhancements
 *     Sopot Cela - ongoing enhancements
 *     Lars Vogel - ongoing enhancements
 *     Wim Jongman - ongoing enhancements
 *     Steven Spungin - ongoing enhancements, Bug 438591
 *******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.project;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.pde.core.build.IBuildEntry;
import org.eclipse.pde.core.plugin.IMatchRules;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginReference;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.pde.internal.core.build.WorkspaceBuildModel;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundlePluginModel;
import org.eclipse.pde.internal.core.plugin.WorkspacePluginModelBase;
import org.eclipse.pde.internal.core.project.PDEProject;
import org.eclipse.pde.internal.core.util.CoreUtility;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.wizards.IProjectProvider;
import org.eclipse.pde.internal.ui.wizards.plugin.NewPluginProjectWizard;
import org.eclipse.pde.internal.ui.wizards.plugin.NewProjectCreationOperation;
import org.eclipse.pde.internal.ui.wizards.plugin.PluginFieldData;
import org.eclipse.pde.ui.IBundleContentWizard;
import org.eclipse.pde.ui.IFieldData;
import org.eclipse.pde.ui.templates.PluginReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * @author jin.liu (jin.liu@soyatec.com)
 */
public class E4NewProjectWizard extends NewPluginProjectWizard {

	private static final String PLUGIN_XML = "plugin.xml";
	private static final String MODEL_EDITOR_ID = "org.eclipse.e4.tools.emf.editor3x.e4wbm";
	private static final String APPLICATION_MODEL = "Application.e4xmi";
	private PluginFieldData fPluginData;
	private NewApplicationWizardPage fApplicationPage;
	private IProjectProvider fProjectProvider;
	private PluginContentPage fContentPage;
	private boolean isMinimalist;

	public E4NewProjectWizard() {
		fPluginData = new PluginFieldData();
	}

	@Override
	public void addPages() {
		fMainPage = new E4NewProjectWizardPage(
				"main", fPluginData, false, getSelection()); //$NON-NLS-1$
		fMainPage.setTitle(PDEUIMessages.NewProjectWizard_MainPage_title);
		fMainPage.setDescription(PDEUIMessages.NewProjectWizard_MainPage_desc);
		String pname = getDefaultValue(DEF_PROJECT_NAME);
		if (pname != null)
			fMainPage.setInitialProjectName(pname);
		addPage(fMainPage);

		fProjectProvider = new IProjectProvider() {
			public String getProjectName() {
				return fMainPage.getProjectName();
			}

			public IProject getProject() {
				return fMainPage.getProjectHandle();
			}

			public IPath getLocationPath() {
				return fMainPage.getLocationPath();
			}
		};

		fContentPage = new PluginContentPage(
				"page2", fProjectProvider, fMainPage, fPluginData); //$NON-NLS-1$

		fApplicationPage = new NewApplicationWizardPage(fProjectProvider,
				fPluginData);

		addPage(fContentPage);
		addPage(fApplicationPage);
	}

	@Override
	@SuppressWarnings("restriction")
	public boolean performFinish() {
		try {
			fMainPage.updateData();
			fContentPage.updateData();
			IDialogSettings settings = getDialogSettings();
			if (settings != null) {
				fMainPage.saveSettings(settings);
				fContentPage.saveSettings(settings);
			}

			// Create the project
			getContainer().run(
					false,
					true,
					new NewProjectCreationOperation(fPluginData,
							fProjectProvider, new ContentWizard()) {
						private WorkspacePluginModelBase model;

						@Override
						protected void setPluginLibraries(
								WorkspacePluginModelBase model)
								throws CoreException {
							this.model = model;
							super.setPluginLibraries(model);
						}
					});

			// Add Project to working set
			IWorkingSet[] workingSets = fMainPage.getSelectedWorkingSets();
			if (workingSets.length > 0)
				getWorkbench().getWorkingSetManager().addToWorkingSets(
						fProjectProvider.getProject(), workingSets);

			this.createProductsExtension(fProjectProvider.getProject());

			this.createApplicationResources(fProjectProvider.getProject(),
					new NullProgressMonitor());

			// Add the resources to build.properties
			adjustBuildPropertiesFile(fProjectProvider.getProject());

			// Open the model editor
			openEditorForApplicationModel();

			return true;
		} catch (InvocationTargetException e) {
			PDEPlugin.logException(e);
		} catch (InterruptedException e) {
		} catch (CoreException e) {
			PDEPlugin.logException(e);
		}
		return false;
	}

	/**
	 * Opens the model editor after the project was created.
	 * 
	 * @throws PartInitException
	 */
	private void openEditorForApplicationModel() throws PartInitException {
		IFile file = fProjectProvider.getProject().getFile(APPLICATION_MODEL);
		if (file != null) {
			FileEditorInput input = new FileEditorInput(file);
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();
			page.openEditor(input, MODEL_EDITOR_ID);
		}
	}

	/**
	 * Adds other resources to the build.properties file.
	 * 
	 * @param project
	 * @throws CoreException
	 */
	private void adjustBuildPropertiesFile(IProject project)
			throws CoreException {
		IFile file = PDEProject.getBuildProperties(project);
		if (file.exists()) {
			WorkspaceBuildModel model = new WorkspaceBuildModel(file);
			IBuildEntry e = model.getBuild().getEntry(IBuildEntry.BIN_INCLUDES);

			e.addToken(PLUGIN_XML);
			e.addToken(APPLICATION_MODEL);

			// Event though an icons directory is always created
			// it seems appropriate to only add it if it contains
			// some content
			if (!isMinimalist) {
				e.addToken("icons/");
			}

			Map<String, String> map = fApplicationPage.getData();
			String cssEntry = map
					.get(NewApplicationWizardPage.APPLICATION_CSS_PROPERTY);
			if (cssEntry != null) {
				e.addToken(cssEntry);
			}

			model.save();
		}
	}

	/**
	 * create products extension detail
	 * 
	 * @param project
	 */
	@SuppressWarnings("restriction")
	public void createProductsExtension(IProject project) {
		Map<String, String> map = fApplicationPage.getData();
		if (map == null
				|| map.get(NewApplicationWizardPage.PRODUCT_NAME) == null)
			return;

		WorkspacePluginModelBase fmodel = new WorkspaceBundlePluginModel(
				project.getFile(ICoreConstants.BUNDLE_FILENAME_DESCRIPTOR),
				project.getFile(ICoreConstants.PLUGIN_FILENAME_DESCRIPTOR));
		IPluginExtension extension = fmodel.getFactory().createExtension();
		try {
			String productName = map.get(NewApplicationWizardPage.PRODUCT_NAME);
			String applicationName = map
					.get(NewApplicationWizardPage.APPLICATION);

			String cssValue = map
					.get(NewApplicationWizardPage.APPLICATION_CSS_PROPERTY);
			if (cssValue != null) {
				cssValue = "platform:/plugin/" + fPluginData.getId() + "/"
						+ cssValue;
				map.put(NewApplicationWizardPage.APPLICATION_CSS_PROPERTY,
						cssValue);
			}

			if ("TRUE".equals(map.get(NewApplicationWizardPage.generateLifecycle))){
				String lifeCycleValue = map
						.get(NewApplicationWizardPage.generateLifecycleName);
				if (lifeCycleValue != null && lifeCycleValue.isEmpty() == false) {
					lifeCycleValue = "bundleclass://" + fPluginData.getId() + "/"
							+ fPluginData.getId().toLowerCase() + "." + lifeCycleValue;
					map.put(NewApplicationWizardPage.LIFECYCLE_URI_PROPERTY,
							lifeCycleValue);
				}
			}

			extension.setPoint("org.eclipse.core.runtime.products");
			extension.setId("product");
			IPluginElement productElement = fmodel.getFactory().createElement(
					extension);

			productElement.setName("product");
			if (applicationName != null) {
				productElement.setAttribute("application", applicationName);
			} else {
				productElement.setAttribute("application",
						NewApplicationWizardPage.E4_APPLICATION);
			}
			productElement.setAttribute("name", productName);

			Set<Entry<String, String>> set = map.entrySet();
			if (set != null) {
				Iterator<Entry<String, String>> it = set.iterator();
				if (it != null) {
					while (it.hasNext()) {
						Entry<String, String> entry = it.next();
						String value = entry.getValue();
						if (value == null || value.trim().length() == 0) {
							continue;
						}

						if (entry.getKey().equals(
								NewApplicationWizardPage.PRODUCT_NAME)
								|| entry.getKey().equals(
										NewApplicationWizardPage.APPLICATION)
								|| entry.getKey().equals(
										NewApplicationWizardPage.richSample)
								|| entry.getKey().equals(
												NewApplicationWizardPage.generateLifecycle)
								|| entry.getKey().equals(
												NewApplicationWizardPage.generateLifecycleName)
								|| entry.getKey()
										.equals(NewApplicationWizardPage.CLEAR_PERSISTED_STATE)) {
							continue;
						}
						IPluginElement element = fmodel.getFactory()
								.createElement(productElement);
						element.setName("property");
						element.setAttribute("name", entry.getKey());
						element.setAttribute("value", value);
						productElement.add(element);
					}
				}
			}
			extension.add(productElement);
			fmodel.getPluginBase().add(extension);
			fmodel.save();

		} catch (CoreException e) {
			PDEPlugin.logException(e);
		}
	}

	/**
	 * create products extension detail
	 * 
	 * @param project
	 */
	@SuppressWarnings("restriction")
	public void createApplicationResources(IProject project,
			IProgressMonitor monitor) {
		Map<String, String> map = fApplicationPage.getData();
		isMinimalist = !map.get(NewApplicationWizardPage.richSample)
				.equalsIgnoreCase("TRUE");
		if (map == null
				|| map.get(NewApplicationWizardPage.PRODUCT_NAME) == null)
			return;

		// If the project has invalid characters, the plug-in name would replace
		// them with underscores, product name does the same
		String pluginName = fPluginData.getId();
		
		// BEGIN Generate E4Lifecycle class with annotations	
		boolean lifeCycleCreated = "TRUE".equals(map.get(NewApplicationWizardPage.generateLifecycle));
		if (lifeCycleCreated){
			String classname = fPluginData.getId() + "." + map.get(NewApplicationWizardPage.generateLifecycleName);
			LifeCycleClassCodeGenerator fGenerator = new LifeCycleClassCodeGenerator(project, classname, fPluginData, false, getContainer());
			try {
				fGenerator.generate(new NullProgressMonitor());
			} catch (CoreException e2) {
				e2.printStackTrace();
			}
		}
		// END Generate E4Lifecycle class with annotations
		
		// If there's no Activator or LifeCycle created we create default package
		if (!fPluginData.doGenerateClass() && !lifeCycleCreated) {
			String packageName = fPluginData.getId();
			IPath path = new Path(packageName.replace('.', '/'));
			if (fPluginData.getSourceFolderName().trim().length() > 0)
				path = new Path(fPluginData.getSourceFolderName()).append(path);

			try {
				CoreUtility.createFolder(project.getFolder(path));
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		IJavaProject javaProject = JavaCore.create(project);
		IPackageFragment fragment = null;

		try {
			for (IPackageFragment element : javaProject.getPackageFragments()) {
				if (element.getKind() == IPackageFragmentRoot.K_SOURCE) {
					fragment = element;
				}
			}
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}

		createApplicationModel(project, pluginName, fragment);

		String cssPath = map
				.get(NewApplicationWizardPage.APPLICATION_CSS_PROPERTY);
		if (cssPath != null && cssPath.trim().length() > 0) {
			IFile file = project.getFile(cssPath);

			try {
				prepareFolder(file.getParent(), monitor);

				URL corePath = ResourceLocator
						.getProjectTemplateFiles("css/default.css");
				file.create(corePath.openStream(), true, monitor);
			} catch (Exception e) {
				PDEPlugin.logException(e);
			}
		}

		String template_id = "common";
		Set<String> binaryExtentions = new HashSet<String>();
		binaryExtentions.add(".gif");
		binaryExtentions.add(".png");

		Map<String, String> keys = new HashMap<String, String>();
		keys.put("projectName", pluginName);
		keys.put("productFileName",
				map.get(NewApplicationWizardPage.PRODUCT_NAME));
		String elementName = fragment.getElementName();
		keys.put("packageName", (elementName.equals("") ? "" : elementName
				+ ".")
				+ "handlers");
		keys.put("packageName2", (elementName.equals("") ? "" : elementName
				+ ".")
				+ "parts");
		keys.put(
				"programArgs",
				"true".equalsIgnoreCase(map
						.get(NewApplicationWizardPage.CLEAR_PERSISTED_STATE)) ? "-clearPersistedState"
						: "");
		try {
			URL corePath = ResourceLocator.getProjectTemplateFiles(template_id);
			IRunnableWithProgress op = new TemplateOperation(corePath, project,
					keys, binaryExtentions, isMinimalist);
			getContainer().run(false, true, op);
		} catch (Exception e) {
			PDEPlugin.logException(e);
		}
		if (!isMinimalist) {
			try {
				URL corePath = ResourceLocator.getProjectTemplateFiles("src");
				IRunnableWithProgress op = new TemplateOperation(corePath,
						(IContainer) fragment.getResource(), keys,
						binaryExtentions, isMinimalist);
				getContainer().run(false, true, op);
			} catch (Exception e) {
				PDEPlugin.logException(e);
			}
		}
	}

	private void createApplicationModel(IProject project, String pluginName,
			IPackageFragment fragment) {
		Map<String, String> map = fApplicationPage.getData();
		boolean isMinimalist = !map.get(NewApplicationWizardPage.richSample)
				.equalsIgnoreCase("TRUE");
		if (APPLICATION_MODEL != null && APPLICATION_MODEL.trim().length() > 0) {

			// Create a resource set
			//
			ResourceSet resourceSet = new ResourceSetImpl();

			// Get the URI of the model file.
			//
			URI fileURI = URI.createPlatformResourceURI(project.getName() + "/"
					+ APPLICATION_MODEL, true);

			// Create a resource for this file.
			//
			Resource resource = resourceSet.createResource(fileURI);

			MApplication application = MApplicationFactory.INSTANCE
					.createApplication();

			application.setElementId("org.eclipse.e4.ide.application");

			MAddon addon = MApplicationFactory.INSTANCE.createAddon();
			addon.setElementId("org.eclipse.e4.core.commands.service");
			addon.setContributionURI("bundleclass://org.eclipse.e4.core.commands/org.eclipse.e4.core.commands.CommandServiceAddon");
			application.getAddons().add(addon);

			addon = MApplicationFactory.INSTANCE.createAddon();
			addon.setElementId("org.eclipse.e4.ui.contexts.service");
			addon.setContributionURI("bundleclass://org.eclipse.e4.ui.services/org.eclipse.e4.ui.services.ContextServiceAddon");
			application.getAddons().add(addon);

			addon = MApplicationFactory.INSTANCE.createAddon();
			addon.setElementId("org.eclipse.e4.ui.bindings.service");
			addon.setContributionURI("bundleclass://org.eclipse.e4.ui.bindings/org.eclipse.e4.ui.bindings.BindingServiceAddon");
			application.getAddons().add(addon);

			addon = MApplicationFactory.INSTANCE.createAddon();
			addon.setElementId("org.eclipse.e4.ui.workbench.commands.model");
			addon.setContributionURI("bundleclass://org.eclipse.e4.ui.workbench/org.eclipse.e4.ui.internal.workbench.addons.CommandProcessingAddon");
			application.getAddons().add(addon);

			addon = MApplicationFactory.INSTANCE.createAddon();
			addon.setElementId("org.eclipse.e4.ui.workbench.handler.model");
			addon.setContributionURI("bundleclass://org.eclipse.e4.ui.workbench/org.eclipse.e4.ui.internal.workbench.addons.HandlerProcessingAddon");
			application.getAddons().add(addon);

			addon = MApplicationFactory.INSTANCE.createAddon();
			addon.setElementId("org.eclipse.e4.ui.workbench.contexts.model");
			addon.setContributionURI("bundleclass://org.eclipse.e4.ui.workbench/org.eclipse.e4.ui.internal.workbench.addons.ContextProcessingAddon");
			application.getAddons().add(addon);

			addon = MApplicationFactory.INSTANCE.createAddon();
			addon.setElementId("org.eclipse.e4.ui.workbench.bindings.model");
			addon.setContributionURI("bundleclass://org.eclipse.e4.ui.workbench.swt/org.eclipse.e4.ui.workbench.swt.util.BindingProcessingAddon");
			application.getAddons().add(addon);
			MTrimmedWindow mainWindow = MBasicFactory.INSTANCE
					.createTrimmedWindow();
			application.getChildren().add(mainWindow);
			mainWindow.setLabel(pluginName);
			mainWindow.setWidth(500);
			mainWindow.setHeight(400);
			resource.getContents().add((EObject) application);
			MBindingContext rootContext = MCommandsFactory.INSTANCE
					.createBindingContext();
			rootContext.setElementId("org.eclipse.ui.contexts.dialogAndWindow");
			rootContext.setName("In Dialog and Windows");

			MBindingContext childContext = MCommandsFactory.INSTANCE
					.createBindingContext();
			childContext.setElementId("org.eclipse.ui.contexts.window");
			childContext.setName("In Windows");
			rootContext.getChildren().add(childContext);

			childContext = MCommandsFactory.INSTANCE.createBindingContext();
			childContext.setElementId("org.eclipse.ui.contexts.dialog");
			childContext.setName("In Dialogs");
			rootContext.getChildren().add(childContext);

			application.getRootContext().add(rootContext);
			application.getBindingContexts().add(rootContext);
			if (!isMinimalist) {

				// Create Quit command
				MCommand quitCommand = createCommand(
						"org.eclipse.ui.file.exit", "quitCommand",
						"QuitHandler", "M1+Q", pluginName, fragment,
						application);

				MCommand openCommand = createCommand(pluginName + ".open",
						"openCommand", "OpenHandler", "M1+O", pluginName,
						fragment, application);

				MCommand saveCommand = createCommand(
						"org.eclipse.ui.file.save", "saveCommand",
						"SaveHandler", "M1+S", pluginName, fragment,
						application);

				MCommand aboutCommand = createCommand(
						"org.eclipse.ui.help.aboutAction", "aboutCommand",
						"AboutHandler", "M1+A", pluginName, fragment,
						application);

				MMenu menu = MMenuFactory.INSTANCE.createMenu();
				mainWindow.setMainMenu(menu);
				menu.setElementId("menu:org.eclipse.ui.main.menu");

				MMenu fileMenuItem = MMenuFactory.INSTANCE.createMenu();
				menu.getChildren().add(fileMenuItem);
				fileMenuItem.setLabel("File");
				{
					MHandledMenuItem menuItemOpen = MMenuFactory.INSTANCE
							.createHandledMenuItem();
					fileMenuItem.getChildren().add(menuItemOpen);
					menuItemOpen.setLabel("Open");
					menuItemOpen.setIconURI("platform:/plugin/" + pluginName
							+ "/icons/sample.png");
					menuItemOpen.setCommand(openCommand);

					MHandledMenuItem menuItemSave = MMenuFactory.INSTANCE
							.createHandledMenuItem();
					fileMenuItem.getChildren().add(menuItemSave);
					menuItemSave.setLabel("Save");
					menuItemSave.setIconURI("platform:/plugin/" + pluginName
							+ "/icons/save_edit.png");
					menuItemSave.setCommand(saveCommand);

					MHandledMenuItem menuItemQuit = MMenuFactory.INSTANCE
							.createHandledMenuItem();
					fileMenuItem.getChildren().add(menuItemQuit);
					menuItemQuit.setLabel("Quit");
					menuItemQuit.setCommand(quitCommand);
				}
				MMenu helpMenuItem = MMenuFactory.INSTANCE.createMenu();
				menu.getChildren().add(helpMenuItem);
				helpMenuItem.setLabel("Help");
				{
					MHandledMenuItem menuItemAbout = MMenuFactory.INSTANCE
							.createHandledMenuItem();
					helpMenuItem.getChildren().add(menuItemAbout);
					menuItemAbout.setLabel("About");
					menuItemAbout.setCommand(aboutCommand);
				}

				// PerspectiveStack
				MPerspectiveStack perspectiveStack = MAdvancedFactory.INSTANCE
						.createPerspectiveStack();
				mainWindow.getChildren().add(perspectiveStack);

				MPerspective perspective = MAdvancedFactory.INSTANCE
						.createPerspective();
				perspectiveStack.getChildren().add(perspective);
				{
					// Part Container
					MPartSashContainer partSashContainer = MBasicFactory.INSTANCE
							.createPartSashContainer();
					perspective.getChildren().add(partSashContainer);

					MPartStack partStack = MBasicFactory.INSTANCE
							.createPartStack();
					partSashContainer.getChildren().add(partStack);

					MPart part = MBasicFactory.INSTANCE.createPart();
					partStack.getChildren().add(part);
					part.setLabel("Sample Part");
					part.setContributionURI("bundleclass://" + pluginName + "/"
							+ fragment.getElementName() + ".parts"
							+ ".SamplePart");

				}

				// WindowTrim
				MTrimBar trimBar = MBasicFactory.INSTANCE.createTrimBar();
				mainWindow.getTrimBars().add(trimBar);

				MToolBar toolBar = MMenuFactory.INSTANCE.createToolBar();
				toolBar.setElementId("toolbar:org.eclipse.ui.main.toolbar");
				trimBar.getChildren().add(toolBar);

				MHandledToolItem toolItemOpen = MMenuFactory.INSTANCE
						.createHandledToolItem();
				toolBar.getChildren().add(toolItemOpen);
				toolItemOpen.setIconURI("platform:/plugin/" + pluginName
						+ "/icons/sample.png");
				toolItemOpen.setCommand(openCommand);

				MHandledToolItem toolItemSave = MMenuFactory.INSTANCE
						.createHandledToolItem();
				toolBar.getChildren().add(toolItemSave);
				toolItemSave.setIconURI("platform:/plugin/" + pluginName
						+ "/icons/save_edit.png");
				toolItemSave.setCommand(saveCommand);
			}
			Map<Object, Object> options = new HashMap<Object, Object>();
			options.put(XMLResource.OPTION_ENCODING, "UTF-8");
			try {
				resource.save(options);
			} catch (IOException e) {
				PDEPlugin.logException(e);

			}
		}
	}

	private MCommand createCommand(String commandId, String name,
			String className, String keyBinding, String projectName,
			IPackageFragment fragment, MApplication application) {
		MCommand command = MCommandsFactory.INSTANCE.createCommand();
		command.setCommandName(name);
		command.setElementId(commandId);
		application.getCommands().add(command);
		{
			// Create handler for command
			MHandler handler = MCommandsFactory.INSTANCE.createHandler();
			handler.setCommand(command);
			String elementName = fragment.getElementName();
			handler.setContributionURI("bundleclass://" + projectName + "/"
					+ (elementName.equals("") ? "" : elementName + ".")
					+ "handlers." + className);
			handler.setElementId(projectName + ".handler." + name);
			application.getHandlers().add(handler);

			// create binding for the command
			MKeyBinding binding = MCommandsFactory.INSTANCE.createKeyBinding();
			binding.setKeySequence(keyBinding);
			binding.setCommand(command);
			List<MBindingTable> tables = application.getBindingTables();
			if (tables.size() == 0) {
				MBindingContext rootContext = null;
				if (application.getRootContext().size() > 0) {
					rootContext = application.getRootContext().get(0);
				} else {
					rootContext = MCommandsFactory.INSTANCE
							.createBindingContext();
					rootContext
							.setElementId("org.eclipse.ui.contexts.dialogAndWindow");
					rootContext.setName("In Dialog and Windows");
					application.getRootContext().add(rootContext);
				}
				MBindingTable table = MCommandsFactory.INSTANCE
						.createBindingTable();
				table.setBindingContext(rootContext);
				tables.add(table);
			}
			tables.get(0).getBindings().add(binding);
		}
		return command;
	}

	private void prepareFolder(IContainer container, IProgressMonitor monitor)
			throws CoreException {
		IContainer parent = container.getParent();
		if (parent instanceof IFolder) {
			prepareFolder(parent, monitor);
		}
		if (!container.exists() && container instanceof IFolder) {
			IFolder folder = (IFolder) container;
			folder.create(true, true, monitor);
		}
	}

	@Override
	public String getPluginId() {
		return fPluginData.getId();
	}

	@Override
	public String getPluginVersion() {
		return fPluginData.getVersion();
	}

	private class ContentWizard extends Wizard implements IBundleContentWizard {

		String[] dependencies = new String[] { "javax.inject",
				"org.eclipse.core.runtime", "org.eclipse.swt",
				"org.eclipse.e4.ui.model.workbench", "org.eclipse.jface",
				"org.eclipse.e4.ui.services", "org.eclipse.e4.ui.workbench",
				"org.eclipse.e4.core.di", "org.eclipse.e4.ui.di",
				"org.eclipse.e4.core.contexts", };

		public void init(IFieldData data) {
		}

		public IPluginReference[] getDependencies(String schemaVersion) {
			ArrayList<IPluginReference> result = new ArrayList<IPluginReference>(
					dependencies.length);
			for (String dependency : dependencies) {
				Bundle bundle = Platform.getBundle(dependency);
				String versionString = "0.0.0";
				if (dependency != null) {
					Version version = bundle.getVersion();
					versionString = version.getMajor() + "."
							+ version.getMinor() + "." + version.getMicro();
				}
				result.add(new PluginReference(dependency, versionString,
						IMatchRules.GREATER_OR_EQUAL));
			}
			return result.toArray(new IPluginReference[0]);
		}

		public String[] getNewFiles() {
			return new String[0];
		}

		public boolean performFinish(IProject project, IPluginModelBase model,
				IProgressMonitor monitor) {
			return true;
		}

		public String[] getImportPackages() {
			return new String[] { };
		}

		@Override
		public boolean performFinish() {
			return true;
		}

	}
}
