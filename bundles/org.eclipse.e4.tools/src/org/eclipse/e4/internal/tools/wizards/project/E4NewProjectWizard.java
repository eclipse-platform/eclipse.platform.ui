/*******************************************************************************
 * Copyright (c) 2006, 2013 Soyatec(http://www.soyatec.com) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Soyatec - initial API and implementation
 * IBM Corporation - ongoing enhancements
 * Sopot Cela - ongoing enhancements
 * Lars Vogel - ongoing enhancements
 * Wim Jongman - ongoing enhancements
 * Steven Spungin - ongoing enhancements, Bug 438591
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
import org.eclipse.e4.internal.tools.Messages;
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
@SuppressWarnings("restriction")
public class E4NewProjectWizard extends NewPluginProjectWizard {

	private static final String NAME = "name"; //$NON-NLS-1$
	private static final String APPLICATION = "application"; //$NON-NLS-1$
	private static final String PRODUCT = "product"; //$NON-NLS-1$
	private static final String TRUE = "TRUE"; //$NON-NLS-1$
	private static final String PLUGIN_XML = "plugin.xml"; //$NON-NLS-1$
	private static final String MODEL_EDITOR_ID = "org.eclipse.e4.tools.emf.editor3x.e4wbm"; //$NON-NLS-1$
	private static final String APPLICATION_MODEL = "Application.e4xmi"; //$NON-NLS-1$
	private final PluginFieldData fPluginData;
	private NewApplicationWizardPage fApplicationPage;
	private IProjectProvider fProjectProvider;
	private PluginContentPage fContentPage;
	private boolean isMinimalist;

	public E4NewProjectWizard() {
		fPluginData = new PluginFieldData();
	}

	@Override
	public void addPages() {
		fMainPage = new E4NewProjectWizardPage("main", fPluginData, false, getSelection()); //$NON-NLS-1$
		fMainPage.setTitle(""); //$NON-NLS-1$
		fMainPage.setDescription(""); //$NON-NLS-1$
		final String pname = getDefaultValue(DEF_PROJECT_NAME);
		if (pname != null) {
			fMainPage.setInitialProjectName(pname);
		}
		addPage(fMainPage);

		fProjectProvider = new IProjectProvider() {
			@Override
			public String getProjectName() {
				return fMainPage.getProjectName();
			}

			@Override
			public IProject getProject() {
				return fMainPage.getProjectHandle();
			}

			@Override
			public IPath getLocationPath() {
				return fMainPage.getLocationPath();
			}
		};

		fContentPage = new PluginContentPage("page2", fProjectProvider, fMainPage, fPluginData); //$NON-NLS-1$

		fApplicationPage = new NewApplicationWizardPage(fProjectProvider,
			fPluginData);

		addPage(fContentPage);
		addPage(fApplicationPage);
	}

	@Override
	public boolean performFinish() {
		try {
			fMainPage.updateData();
			fContentPage.updateData();
			final IDialogSettings settings = getDialogSettings();
			if (settings != null) {
				fMainPage.saveSettings(settings);
				fContentPage.saveSettings(settings);
			}

			// Create the project
			getContainer().run(
				false,
				true,
				new NewProjectCreationOperation(fPluginData,
					fProjectProvider, new ContentWizard()));

			// Add Project to working set
			final IWorkingSet[] workingSets = fMainPage.getSelectedWorkingSets();
			if (workingSets.length > 0) {
				getWorkbench().getWorkingSetManager().addToWorkingSets(
					fProjectProvider.getProject(), workingSets);
			}

			createProductsExtension(fProjectProvider.getProject());

			createApplicationResources(fProjectProvider.getProject(),
				new NullProgressMonitor());

			// Add the resources to build.properties
			adjustBuildPropertiesFile(fProjectProvider.getProject());

			// Open the model editor
			openEditorForApplicationModel();

			return true;
		} catch (final InvocationTargetException e) {
			PDEPlugin.logException(e);
		} catch (final InterruptedException e) {
		} catch (final CoreException e) {
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
		final IFile file = fProjectProvider.getProject().getFile(APPLICATION_MODEL);
		if (file != null) {
			final FileEditorInput input = new FileEditorInput(file);
			final IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
			final IWorkbenchPage page = window.getActivePage();
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
		final IFile file = PDEProject.getBuildProperties(project);
		if (file.exists()) {
			final WorkspaceBuildModel model = new WorkspaceBuildModel(file);
			final IBuildEntry e = model.getBuild().getEntry(IBuildEntry.BIN_INCLUDES);

			e.addToken(PLUGIN_XML);
			e.addToken(APPLICATION_MODEL);

			// Event though an icons directory is always created
			// it seems appropriate to only add it if it contains
			// some content
			if (!isMinimalist) {
				e.addToken("icons/"); //$NON-NLS-1$
			}

			final Map<String, String> map = fApplicationPage.getData();
			final String cssEntry = map
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
	public void createProductsExtension(IProject project) {
		final Map<String, String> map = fApplicationPage.getData();
		if (map == null
			|| map.get(NewApplicationWizardPage.PRODUCT_NAME) == null) {
			return;
		}

		final WorkspacePluginModelBase fmodel = new WorkspaceBundlePluginModel(
			project.getFile(ICoreConstants.BUNDLE_FILENAME_DESCRIPTOR),
			project.getFile(ICoreConstants.PLUGIN_FILENAME_DESCRIPTOR));
		final IPluginExtension extension = fmodel.getFactory().createExtension();
		try {
			final String productName = map.get(NewApplicationWizardPage.PRODUCT_NAME);
			final String applicationName = map
				.get(NewApplicationWizardPage.APPLICATION);

			String cssValue = map
				.get(NewApplicationWizardPage.APPLICATION_CSS_PROPERTY);
			if (cssValue != null) {
				cssValue = "platform:/plugin/" + fPluginData.getId() + "/" //$NON-NLS-1$ //$NON-NLS-2$
					+ cssValue;
				map.put(NewApplicationWizardPage.APPLICATION_CSS_PROPERTY,
					cssValue);
			}

			if (TRUE.equals(map.get(NewApplicationWizardPage.generateLifecycle))) {
				String lifeCycleValue = map
					.get(NewApplicationWizardPage.generateLifecycleName);
				if (lifeCycleValue != null && lifeCycleValue.isEmpty() == false) {
					lifeCycleValue = "bundleclass://" + fPluginData.getId() + "/" //$NON-NLS-1$ //$NON-NLS-2$
						+ fPluginData.getId().toLowerCase() + "." + lifeCycleValue; //$NON-NLS-1$
					map.put(NewApplicationWizardPage.LIFECYCLE_URI_PROPERTY,
						lifeCycleValue);
				}
			}

			extension.setPoint("org.eclipse.core.runtime.products"); //$NON-NLS-1$
			extension.setId(PRODUCT);
			final IPluginElement productElement = fmodel.getFactory().createElement(
				extension);

			productElement.setName(PRODUCT);
			if (applicationName != null) {
				productElement.setAttribute(APPLICATION, applicationName);
			} else {
				productElement.setAttribute(APPLICATION,
					NewApplicationWizardPage.E4_APPLICATION);
			}
			productElement.setAttribute(NAME, productName);

			final Set<Entry<String, String>> set = map.entrySet();
			if (set != null) {
				final Iterator<Entry<String, String>> it = set.iterator();
				if (it != null) {
					while (it.hasNext()) {
						final Entry<String, String> entry = it.next();
						final String value = entry.getValue();
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
						final IPluginElement element = fmodel.getFactory()
							.createElement(productElement);
						element.setName("property"); //$NON-NLS-1$
						element.setAttribute(NAME, entry.getKey());
						element.setAttribute("value", value); //$NON-NLS-1$
						productElement.add(element);
					}
				}
			}
			extension.add(productElement);
			fmodel.getPluginBase().add(extension);
			fmodel.save();

		} catch (final CoreException e) {
			PDEPlugin.logException(e);
		}
	}

	/**
	 * create products extension detail
	 *
	 * @param project
	 */
	public void createApplicationResources(IProject project,
		IProgressMonitor monitor) {
		final Map<String, String> map = fApplicationPage.getData();
		if (map == null
			|| map.get(NewApplicationWizardPage.PRODUCT_NAME) == null) {
			return;
		}
		isMinimalist = !map.get(NewApplicationWizardPage.richSample)
			.equalsIgnoreCase(TRUE);

		// If the project has invalid characters, the plug-in name would replace
		// them with underscores, product name does the same
		final String pluginName = fPluginData.getId();

		// BEGIN Generate E4Lifecycle class with annotations
		final boolean lifeCycleCreated = TRUE.equals(map.get(NewApplicationWizardPage.generateLifecycle));
		if (lifeCycleCreated) {
			final String classname = fPluginData.getId() + "." //$NON-NLS-1$
				+ map.get(NewApplicationWizardPage.generateLifecycleName);
			final LifeCycleClassCodeGenerator fGenerator = new LifeCycleClassCodeGenerator(project, classname,
				fPluginData);
			try {
				fGenerator.generate(new NullProgressMonitor());
			} catch (final CoreException e2) {
				e2.printStackTrace();
			}
		}
		// END Generate E4Lifecycle class with annotations

		// If there's no Activator or LifeCycle created we create default package
		if (!fPluginData.doGenerateClass() && !lifeCycleCreated) {
			final String packageName = fPluginData.getId();
			IPath path = new Path(packageName.replace('.', '/'));
			if (fPluginData.getSourceFolderName().trim().length() > 0) {
				path = new Path(fPluginData.getSourceFolderName()).append(path);
			}

			try {
				CoreUtility.createFolder(project.getFolder(path));
			} catch (final CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		final IJavaProject javaProject = JavaCore.create(project);
		IPackageFragment fragment = null;

		try {
			for (final IPackageFragment element : javaProject.getPackageFragments()) {
				if (element.getKind() == IPackageFragmentRoot.K_SOURCE) {
					fragment = element;
				}
			}
		} catch (final JavaModelException e1) {
			e1.printStackTrace();
		}

		createApplicationModel(project, pluginName, fragment);

		final String cssPath = map
			.get(NewApplicationWizardPage.APPLICATION_CSS_PROPERTY);
		if (cssPath != null && cssPath.trim().length() > 0) {
			final IFile file = project.getFile(cssPath);

			try {
				prepareFolder(file.getParent(), monitor);

				final URL corePath = ResourceLocator
					.getProjectTemplateFiles("css/default.css"); //$NON-NLS-1$
				file.create(corePath.openStream(), true, monitor);
			} catch (final Exception e) {
				PDEPlugin.logException(e);
			}
		}

		final String template_id = "common"; //$NON-NLS-1$
		final Set<String> binaryExtentions = new HashSet<String>();
		binaryExtentions.add(".gif"); //$NON-NLS-1$
		binaryExtentions.add(".png"); //$NON-NLS-1$

		final Map<String, String> keys = new HashMap<String, String>();
		keys.put("projectName", pluginName); //$NON-NLS-1$
		keys.put("productFileName", //$NON-NLS-1$
			map.get(NewApplicationWizardPage.PRODUCT_NAME));
		final String elementName = fragment.getElementName();
		keys.put("packageName", (elementName.equals("") ? "" : elementName //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			+ ".") //$NON-NLS-1$
			+ "handlers"); //$NON-NLS-1$
		keys.put("packageName2", (elementName.equals("") ? "" : elementName //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			+ ".") //$NON-NLS-1$
			+ "parts"); //$NON-NLS-1$
		keys.put("programArgs", //$NON-NLS-1$
			"true".equalsIgnoreCase(map //$NON-NLS-1$
				.get(NewApplicationWizardPage.CLEAR_PERSISTED_STATE)) ? "-clearPersistedState" //$NON-NLS-1$
					: ""); //$NON-NLS-1$
		try {
			final URL corePath = ResourceLocator.getProjectTemplateFiles(template_id);
			final IRunnableWithProgress op = new TemplateOperation(corePath, project,
				keys, binaryExtentions, isMinimalist);
			getContainer().run(false, true, op);
		} catch (final Exception e) {
			PDEPlugin.logException(e);
		}
		if (!isMinimalist) {
			try {
				final URL corePath = ResourceLocator.getProjectTemplateFiles("src"); //$NON-NLS-1$
				final IRunnableWithProgress op = new TemplateOperation(corePath,
					(IContainer) fragment.getResource(), keys,
					binaryExtentions, isMinimalist);
				getContainer().run(false, true, op);
			} catch (final Exception e) {
				PDEPlugin.logException(e);
			}
		}
	}

	private void createApplicationModel(IProject project, String pluginName,
		IPackageFragment fragment) {
		final Map<String, String> map = fApplicationPage.getData();
		final boolean isMinimalist = !map.get(NewApplicationWizardPage.richSample)
			.equalsIgnoreCase(TRUE);
		if (APPLICATION_MODEL != null && APPLICATION_MODEL.trim().length() > 0) {

			// Create a resource set
			//
			final ResourceSet resourceSet = new ResourceSetImpl();

			// Get the URI of the model file.
			//
			final URI fileURI = URI.createPlatformResourceURI(project.getName() + "/" //$NON-NLS-1$
				+ APPLICATION_MODEL, true);

			// Create a resource for this file.
			//
			final Resource resource = resourceSet.createResource(fileURI);

			final MApplication application = MApplicationFactory.INSTANCE
				.createApplication();

			application.setElementId("org.eclipse.e4.ide.application"); //$NON-NLS-1$

			MAddon addon = MApplicationFactory.INSTANCE.createAddon();
			addon.setElementId("org.eclipse.e4.core.commands.service"); //$NON-NLS-1$
			addon
			.setContributionURI("bundleclass://org.eclipse.e4.core.commands/org.eclipse.e4.core.commands.CommandServiceAddon"); //$NON-NLS-1$
			application.getAddons().add(addon);

			addon = MApplicationFactory.INSTANCE.createAddon();
			addon.setElementId("org.eclipse.e4.ui.contexts.service"); //$NON-NLS-1$
			addon
			.setContributionURI("bundleclass://org.eclipse.e4.ui.services/org.eclipse.e4.ui.services.ContextServiceAddon"); //$NON-NLS-1$
			application.getAddons().add(addon);

			addon = MApplicationFactory.INSTANCE.createAddon();
			addon.setElementId("org.eclipse.e4.ui.bindings.service"); //$NON-NLS-1$
			addon
			.setContributionURI("bundleclass://org.eclipse.e4.ui.bindings/org.eclipse.e4.ui.bindings.BindingServiceAddon"); //$NON-NLS-1$
			application.getAddons().add(addon);

			addon = MApplicationFactory.INSTANCE.createAddon();
			addon.setElementId("org.eclipse.e4.ui.workbench.commands.model"); //$NON-NLS-1$
			addon
			.setContributionURI("bundleclass://org.eclipse.e4.ui.workbench/org.eclipse.e4.ui.internal.workbench.addons.CommandProcessingAddon"); //$NON-NLS-1$
			application.getAddons().add(addon);

			addon = MApplicationFactory.INSTANCE.createAddon();
			addon.setElementId("org.eclipse.e4.ui.workbench.handler.model"); //$NON-NLS-1$
			addon
			.setContributionURI("bundleclass://org.eclipse.e4.ui.workbench/org.eclipse.e4.ui.internal.workbench.addons.HandlerProcessingAddon"); //$NON-NLS-1$
			application.getAddons().add(addon);

			addon = MApplicationFactory.INSTANCE.createAddon();
			addon.setElementId("org.eclipse.e4.ui.workbench.contexts.model"); //$NON-NLS-1$
			addon
			.setContributionURI("bundleclass://org.eclipse.e4.ui.workbench/org.eclipse.e4.ui.internal.workbench.addons.ContextProcessingAddon"); //$NON-NLS-1$
			application.getAddons().add(addon);

			addon = MApplicationFactory.INSTANCE.createAddon();
			addon.setElementId("org.eclipse.e4.ui.workbench.bindings.model"); //$NON-NLS-1$
			addon
			.setContributionURI("bundleclass://org.eclipse.e4.ui.workbench.swt/org.eclipse.e4.ui.workbench.swt.util.BindingProcessingAddon"); //$NON-NLS-1$
			application.getAddons().add(addon);
			final MTrimmedWindow mainWindow = MBasicFactory.INSTANCE
				.createTrimmedWindow();
			application.getChildren().add(mainWindow);
			mainWindow.setLabel(pluginName);
			mainWindow.setWidth(500);
			mainWindow.setHeight(400);
			resource.getContents().add((EObject) application);
			final MBindingContext rootContext = MCommandsFactory.INSTANCE
				.createBindingContext();
			rootContext.setElementId("org.eclipse.ui.contexts.dialogAndWindow"); //$NON-NLS-1$
			rootContext.setName(Messages.E4NewProjectWizard_InDialogsAndWindows);

			MBindingContext childContext = MCommandsFactory.INSTANCE
				.createBindingContext();
			childContext.setElementId("org.eclipse.ui.contexts.window"); //$NON-NLS-1$
			childContext.setName(Messages.E4NewProjectWizard_InWindows);
			rootContext.getChildren().add(childContext);

			childContext = MCommandsFactory.INSTANCE.createBindingContext();
			childContext.setElementId("org.eclipse.ui.contexts.dialog"); //$NON-NLS-1$
			childContext.setName(Messages.E4NewProjectWizard_InDialogs);
			rootContext.getChildren().add(childContext);

			application.getRootContext().add(rootContext);
			application.getBindingContexts().add(rootContext);
			if (!isMinimalist) {

				// Create Quit command
				final MCommand quitCommand = createCommand("org.eclipse.ui.file.exit", "quitCommand", //$NON-NLS-1$ //$NON-NLS-2$
					"QuitHandler", "M1+Q", pluginName, fragment, //$NON-NLS-1$ //$NON-NLS-2$
					application);

				final MCommand openCommand = createCommand(pluginName + ".open", //$NON-NLS-1$
					"openCommand", "OpenHandler", "M1+O", pluginName, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					fragment, application);

				final MCommand saveCommand = createCommand("org.eclipse.ui.file.save", "saveCommand", //$NON-NLS-1$ //$NON-NLS-2$
					"SaveHandler", "M1+S", pluginName, fragment, //$NON-NLS-1$ //$NON-NLS-2$
					application);

				final MCommand aboutCommand = createCommand("org.eclipse.ui.help.aboutAction", "aboutCommand", //$NON-NLS-1$ //$NON-NLS-2$
					"AboutHandler", "M1+A", pluginName, fragment, //$NON-NLS-1$//$NON-NLS-2$
					application);

				final MMenu menu = MMenuFactory.INSTANCE.createMenu();
				mainWindow.setMainMenu(menu);
				menu.setElementId("menu:org.eclipse.ui.main.menu"); //$NON-NLS-1$

				final MMenu fileMenuItem = MMenuFactory.INSTANCE.createMenu();
				menu.getChildren().add(fileMenuItem);
				fileMenuItem.setLabel(Messages.E4NewProjectWizard_File);
				{
					final MHandledMenuItem menuItemOpen = MMenuFactory.INSTANCE
						.createHandledMenuItem();
					fileMenuItem.getChildren().add(menuItemOpen);
					menuItemOpen.setLabel(Messages.E4NewProjectWizard_Open);
					menuItemOpen.setIconURI("platform:/plugin/" + pluginName //$NON-NLS-1$
						+ "/icons/sample.png"); //$NON-NLS-1$
					menuItemOpen.setCommand(openCommand);

					final MHandledMenuItem menuItemSave = MMenuFactory.INSTANCE
						.createHandledMenuItem();
					fileMenuItem.getChildren().add(menuItemSave);
					menuItemSave.setLabel(Messages.E4NewProjectWizard_Save);
					menuItemSave.setIconURI("platform:/plugin/" + pluginName //$NON-NLS-1$
						+ "/icons/save_edit.png"); //$NON-NLS-1$
					menuItemSave.setCommand(saveCommand);

					final MHandledMenuItem menuItemQuit = MMenuFactory.INSTANCE
						.createHandledMenuItem();
					fileMenuItem.getChildren().add(menuItemQuit);
					menuItemQuit.setLabel(Messages.E4NewProjectWizard_Quit);
					menuItemQuit.setCommand(quitCommand);
				}
				final MMenu helpMenuItem = MMenuFactory.INSTANCE.createMenu();
				menu.getChildren().add(helpMenuItem);
				helpMenuItem.setLabel(Messages.E4NewProjectWizard_Help);
				{
					final MHandledMenuItem menuItemAbout = MMenuFactory.INSTANCE
						.createHandledMenuItem();
					helpMenuItem.getChildren().add(menuItemAbout);
					menuItemAbout.setLabel(Messages.E4NewProjectWizard_About);
					menuItemAbout.setCommand(aboutCommand);
				}

				// PerspectiveStack
				final MPerspectiveStack perspectiveStack = MAdvancedFactory.INSTANCE
					.createPerspectiveStack();
				mainWindow.getChildren().add(perspectiveStack);

				final MPerspective perspective = MAdvancedFactory.INSTANCE
					.createPerspective();
				perspectiveStack.getChildren().add(perspective);
				{
					// Part Container
					final MPartSashContainer partSashContainer = MBasicFactory.INSTANCE
						.createPartSashContainer();
					perspective.getChildren().add(partSashContainer);

					final MPartStack partStack = MBasicFactory.INSTANCE
						.createPartStack();
					partSashContainer.getChildren().add(partStack);

					final MPart part = MBasicFactory.INSTANCE.createPart();
					partStack.getChildren().add(part);
					part.setLabel(Messages.E4NewProjectWizard_SamplePart);
					part.setContributionURI("bundleclass://" + pluginName + "/" //$NON-NLS-1$ //$NON-NLS-2$
						+ fragment.getElementName() + ".parts" //$NON-NLS-1$
						+ ".SamplePart"); //$NON-NLS-1$

				}

				// WindowTrim
				final MTrimBar trimBar = MBasicFactory.INSTANCE.createTrimBar();
				mainWindow.getTrimBars().add(trimBar);

				final MToolBar toolBar = MMenuFactory.INSTANCE.createToolBar();
				toolBar.setElementId("toolbar:org.eclipse.ui.main.toolbar"); //$NON-NLS-1$
				trimBar.getChildren().add(toolBar);

				final MHandledToolItem toolItemOpen = MMenuFactory.INSTANCE
					.createHandledToolItem();
				toolBar.getChildren().add(toolItemOpen);
				toolItemOpen.setIconURI("platform:/plugin/" + pluginName //$NON-NLS-1$
					+ "/icons/sample.png"); //$NON-NLS-1$
				toolItemOpen.setCommand(openCommand);

				final MHandledToolItem toolItemSave = MMenuFactory.INSTANCE
					.createHandledToolItem();
				toolBar.getChildren().add(toolItemSave);
				toolItemSave.setIconURI("platform:/plugin/" + pluginName //$NON-NLS-1$
					+ "/icons/save_edit.png"); //$NON-NLS-1$
				toolItemSave.setCommand(saveCommand);
			}
			final Map<Object, Object> options = new HashMap<Object, Object>();
			options.put(XMLResource.OPTION_ENCODING, "UTF-8"); //$NON-NLS-1$
			try {
				resource.save(options);
			} catch (final IOException e) {
				PDEPlugin.logException(e);

			}
		}
	}

	private MCommand createCommand(String commandId, String name,
		String className, String keyBinding, String projectName,
		IPackageFragment fragment, MApplication application) {
		final MCommand command = MCommandsFactory.INSTANCE.createCommand();
		command.setCommandName(name);
		command.setElementId(commandId);
		application.getCommands().add(command);
		{
			// Create handler for command
			final MHandler handler = MCommandsFactory.INSTANCE.createHandler();
			handler.setCommand(command);
			final String elementName = fragment.getElementName();
			handler.setContributionURI("bundleclass://" + projectName + "/" //$NON-NLS-1$ //$NON-NLS-2$
				+ (elementName.equals("") ? "" : elementName + ".") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ "handlers." + className); //$NON-NLS-1$
			handler.setElementId(projectName + ".handler." + name); //$NON-NLS-1$
			application.getHandlers().add(handler);

			// create binding for the command
			final MKeyBinding binding = MCommandsFactory.INSTANCE.createKeyBinding();
			binding.setKeySequence(keyBinding);
			binding.setCommand(command);
			final List<MBindingTable> tables = application.getBindingTables();
			if (tables.size() == 0) {
				MBindingContext rootContext = null;
				if (application.getRootContext().size() > 0) {
					rootContext = application.getRootContext().get(0);
				} else {
					rootContext = MCommandsFactory.INSTANCE
						.createBindingContext();
					rootContext
					.setElementId("org.eclipse.ui.contexts.dialogAndWindow"); //$NON-NLS-1$
					rootContext.setName(Messages.E4NewProjectWizard_InDialogsAndWindows);
					application.getRootContext().add(rootContext);
				}
				final MBindingTable table = MCommandsFactory.INSTANCE
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
		final IContainer parent = container.getParent();
		if (parent instanceof IFolder) {
			prepareFolder(parent, monitor);
		}
		if (!container.exists() && container instanceof IFolder) {
			final IFolder folder = (IFolder) container;
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

		String[] dependencies = new String[] { "javax.inject", //$NON-NLS-1$
			"org.eclipse.core.runtime", "org.eclipse.swt", //$NON-NLS-1$//$NON-NLS-2$
			"org.eclipse.e4.ui.model.workbench", "org.eclipse.jface", //$NON-NLS-1$ //$NON-NLS-2$
			"org.eclipse.e4.ui.services", "org.eclipse.e4.ui.workbench", //$NON-NLS-1$ //$NON-NLS-2$
			"org.eclipse.e4.core.di", "org.eclipse.e4.ui.di", //$NON-NLS-1$ //$NON-NLS-2$
			"org.eclipse.e4.core.contexts", }; //$NON-NLS-1$

		@Override
		public void init(IFieldData data) {
		}

		@Override
		public IPluginReference[] getDependencies(String schemaVersion) {
			final ArrayList<IPluginReference> result = new ArrayList<IPluginReference>(
				dependencies.length);
			for (final String dependency : dependencies) {
				final Bundle bundle = Platform.getBundle(dependency);
				String versionString = "0.0.0"; //$NON-NLS-1$
				if (dependency != null) {
					final Version version = bundle.getVersion();
					versionString = version.getMajor() + "." //$NON-NLS-1$
						+ version.getMinor() + "." + version.getMicro(); //$NON-NLS-1$
				}
				result.add(new PluginReference(dependency, versionString,
					IMatchRules.GREATER_OR_EQUAL));
			}
			return result.toArray(new IPluginReference[0]);
		}

		@Override
		public String[] getNewFiles() {
			return new String[0];
		}

		@Override
		public boolean performFinish(IProject project, IPluginModelBase model,
			IProgressMonitor monitor) {
			return true;
		}

		@Override
		public String[] getImportPackages() {
			return new String[] {};
		}

		@Override
		public boolean performFinish() {
			return true;
		}

	}
}
