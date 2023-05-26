/*******************************************************************************
 * Copyright (c) 2005, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems) -  bug 284447
 *     Christian Georgi (SAP)             -  bug 432480
 *     Denis Zygann <d.zygann@web.de>      - bug 457390
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 502050
 *******************************************************************************/
package org.eclipse.ui.internal.ide.application;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringJoiner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.internal.ide.AboutInfo;
import org.eclipse.ui.internal.ide.EditorAreaDropAdapter;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.WorkbenchActionBuilder;
import org.eclipse.ui.internal.ide.dialogs.WelcomeEditorInput;
import org.eclipse.ui.internal.tweaklets.TitlePathUpdater;
import org.eclipse.ui.internal.tweaklets.Tweaklets;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.MarkerTransfer;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * Window-level advisor for the IDE.
 */
public class IDEWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	private static final String WELCOME_EDITOR_ID = "org.eclipse.ui.internal.ide.dialogs.WelcomeEditor"; //$NON-NLS-1$

	private IDEWorkbenchAdvisor wbAdvisor;
	private boolean editorsAndIntrosOpened = false;
	private IEditorPart lastActiveEditor = null;
	private IPerspectiveDescriptor lastPerspective = null;

	private IWorkbenchPage lastActivePage;
	private String lastEditorTitleTooltip = ""; //$NON-NLS-1$

	private IPropertyListener editorPropertyListener = (source, propId) -> {
		if (propId == IWorkbenchPartConstants.PROP_TITLE) {
			if (lastActiveEditor != null) {
				String newTitle = lastActiveEditor.getTitleToolTip();
				if (!lastEditorTitleTooltip.equals(newTitle)) {
					recomputeTitle();
				}
			}
		}
	};

	private IAdaptable lastInput;
	private IWorkbenchAction openPerspectiveAction;

	/**
	 * The property change listener.
	 * @since 3.6.1
	 */
	private IPropertyChangeListener propertyChangeListener;

	private TitlePathUpdater titlePathUpdater;

	/**
	 * Crates a new IDE workbench window advisor.
	 *
	 * @param wbAdvisor
	 *            the workbench advisor
	 * @param configurer
	 *            the window configurer
	 */
	public IDEWorkbenchWindowAdvisor(IDEWorkbenchAdvisor wbAdvisor,
			IWorkbenchWindowConfigurer configurer) {
		super(configurer);
		this.wbAdvisor = wbAdvisor;
		titlePathUpdater = (TitlePathUpdater) Tweaklets.get(TitlePathUpdater.KEY);
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new WorkbenchActionBuilder(configurer);
	}

	/**
	 * Returns the workbench.
	 *
	 * @return the workbench
	 */
	private IWorkbench getWorkbench() {
		return getWindowConfigurer().getWorkbenchConfigurer().getWorkbench();
	}

	@Override
	public boolean preWindowShellClose() {
		if (getWorkbench().getWorkbenchWindowCount() > 1) {
			return true;
		}
		// the user has asked to close the last window, while will cause the
		// workbench to close in due course - prompt the user for confirmation
		return promptOnExit(getWindowConfigurer().getWindow().getShell());
	}

	/**
	 * Asks the user whether the workbench should really be closed. Only asks if
	 * the preference is enabled.
	 *
	 * @param parentShell
	 *            the parent shell to use for the confirmation dialog
	 * @return <code>true</code> if OK to exit, <code>false</code> if the user
	 *         canceled
	 * @since 3.6
	 */
	static boolean promptOnExit(Shell parentShell) {
		IPreferenceStore store = IDEWorkbenchPlugin.getDefault()
				.getPreferenceStore();
		boolean promptOnExit = store
				.getBoolean(IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW);

		if (promptOnExit) {
			if (parentShell == null) {
				IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (workbenchWindow != null) {
					parentShell = workbenchWindow.getShell();
				}
			}
			if (parentShell != null) {
				parentShell.setMinimized(false);
				parentShell.forceActive();
			}

			String message;

			String productName = null;
			IProduct product = Platform.getProduct();
			if (product != null) {
				productName = product.getName();
			}
			if (productName == null) {
				message = IDEWorkbenchMessages.PromptOnExitDialog_message0;
			} else {
				message = NLS.bind(
						IDEWorkbenchMessages.PromptOnExitDialog_message1,
						productName);
			}

			// use of LinkedHashMap to preserve insertion order
			LinkedHashMap<String, Integer> buttonLabelToIdMap = new LinkedHashMap<>();
			buttonLabelToIdMap.put(IDEWorkbenchMessages.PromptOnExitDialog_button_label_exit, IDialogConstants.OK_ID);
			buttonLabelToIdMap.put(IDialogConstants.CANCEL_LABEL, IDialogConstants.CANCEL_ID);
			MessageDialogWithToggle dlg =
					new MessageDialogWithToggle(
							parentShell,
							IDEWorkbenchMessages.PromptOnExitDialog_shellTitle,
							null,
							message, MessageDialog.CONFIRM, buttonLabelToIdMap, 0,
							IDEWorkbenchMessages.PromptOnExitDialog_choice, false);
			dlg.open();
			if (dlg.getReturnCode() != IDialogConstants.OK_ID) {
				return false;
			}
			if (dlg.getToggleState()) {
				store
						.setValue(
								IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW,
								false);
				IDEWorkbenchPlugin.getDefault().savePluginPreferences();
			}
		}

		return true;
	}

	@Override
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();

		// show the shortcut bar and progress indicator, which are hidden by
		// default
		configurer.setShowPerspectiveBar(true);
		configurer.setShowProgressIndicator(true);

		// add the drag and drop support for the editor area
		configurer.addEditorAreaTransfer(EditorInputTransfer.getInstance());
		configurer.addEditorAreaTransfer(ResourceTransfer.getInstance());
		configurer.addEditorAreaTransfer(FileTransfer.getInstance());
		configurer.addEditorAreaTransfer(MarkerTransfer.getInstance());
		configurer.configureEditorAreaDropListener(new EditorAreaDropAdapter(
				configurer.getWindow()));

		hookTitleUpdateListeners(configurer);
	}

	/**
	 * Hooks the listeners needed on the window
	 *
	 * @param configurer
	 */
	private void hookTitleUpdateListeners(IWorkbenchWindowConfigurer configurer) {
		// hook up the listeners to update the window title
		configurer.getWindow().addPageListener(new IPageListener() {
			@Override
			public void pageActivated(IWorkbenchPage page) {
				updateTitle(false);
			}

			@Override
			public void pageClosed(IWorkbenchPage page) {
				updateTitle(false);
			}

			@Override
			public void pageOpened(IWorkbenchPage page) {
				// do nothing
			}
		});
		configurer.getWindow().addPerspectiveListener(new PerspectiveAdapter() {
			@Override
			public void perspectiveActivated(IWorkbenchPage page,
					IPerspectiveDescriptor perspective) {
				updateTitle(false);
			}

			@Override
			public void perspectiveSavedAs(IWorkbenchPage page,
					IPerspectiveDescriptor oldPerspective,
					IPerspectiveDescriptor newPerspective) {
				updateTitle(false);
			}

			@Override
			public void perspectiveDeactivated(IWorkbenchPage page,
					IPerspectiveDescriptor perspective) {
				updateTitle(false);
			}
		});
		configurer.getWindow().getPartService().addPartListener(
				new IPartListener2() {
					@Override
					public void partActivated(IWorkbenchPartReference ref) {
						if (ref instanceof IEditorReference) {
							updateTitle(false);
						}
					}

					@Override
					public void partBroughtToTop(IWorkbenchPartReference ref) {
						if (ref instanceof IEditorReference) {
							updateTitle(false);
						}
					}

					@Override
					public void partClosed(IWorkbenchPartReference ref) {
						updateTitle(false);
					}

					@Override
					public void partDeactivated(IWorkbenchPartReference ref) {
						// do nothing
					}

					@Override
					public void partOpened(IWorkbenchPartReference ref) {
						// do nothing
					}

					@Override
					public void partHidden(IWorkbenchPartReference ref) {
						if (ref.getPart(false) == lastActiveEditor
								&& lastActiveEditor != null) {
							updateTitle(true);
						}
					}

					@Override
					public void partVisible(IWorkbenchPartReference ref) {
						if (ref.getPart(false) == lastActiveEditor
								&& lastActiveEditor != null) {
							updateTitle(false);
						}
					}

					@Override
					public void partInputChanged(IWorkbenchPartReference ref) {
						// do nothing
					}
				});

		// Listen for changes of the workspace name.
		propertyChangeListener = event -> {
			String property = event.getProperty();
			if (IDEInternalPreferences.WORKSPACE_NAME.equals(property)
					|| IDEInternalPreferences.SHOW_LOCATION.equals(property)
					|| IDEInternalPreferences.SHOW_LOCATION_NAME.equals(property)
					|| IDEInternalPreferences.SHOW_PERSPECTIVE_IN_TITLE.equals(property)
					|| IDEInternalPreferences.SHOW_PRODUCT_IN_TITLE.equals(property)) {
				// Make sure the title is actually updated by
				// setting last active page.
				lastActivePage = null;
				updateTitle(false);
			}
		};
		IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.addPropertyChangeListener(propertyChangeListener);
	}

	private String computeTitle() {
		StringJoiner sj = new StringJoiner(" - "); //$NON-NLS-1$

		IPreferenceStore ps = IDEWorkbenchPlugin.getDefault().getPreferenceStore();

		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		IWorkbenchPage currentPage = configurer.getWindow().getActivePage();
		IEditorPart activeEditor = null;
		if (currentPage != null) {
			activeEditor = lastActiveEditor;
		}

		// show workspace name
		if (ps.getBoolean(IDEInternalPreferences.SHOW_LOCATION_NAME)) {
			String workspaceName = ps.getString(IDEInternalPreferences.WORKSPACE_NAME);
			if (workspaceName != null && workspaceName.length() > 0) {
				sj.add(workspaceName);
			}
		}

		// perspective name
		if (ps.getBoolean(IDEInternalPreferences.SHOW_PERSPECTIVE_IN_TITLE)) {
			IPerspectiveDescriptor persp = currentPage.getPerspective();
			if (persp != null) {
				sj.add(persp.getLabel());
			}
		}

		// active editor
		if (currentPage != null) {
			if (activeEditor != null) {
				sj.add(activeEditor.getTitleToolTip());
			}
		}

		// workspace location is non null either when SHOW_LOCATION is true or
		// when forcing -showlocation via command line
		String workspaceLocation = wbAdvisor.getWorkspaceLocation();
		if (workspaceLocation != null) {
			sj.add(workspaceLocation);
		}

		// Application (product) name
		if (ps.getBoolean(IDEInternalPreferences.SHOW_PRODUCT_IN_TITLE)) {
			IProduct product = Platform.getProduct();
			if (product != null) {
				sj.add(product.getName());
			}
		}

		return sj.toString();
	}

	private void recomputeTitle() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		String oldTitle = configurer.getTitle();
		String newTitle = computeTitle();
		if (!newTitle.equals(oldTitle)) {
			configurer.setTitle(newTitle);
		}
		setTitlePath();
	}

	private void setTitlePath() {

		String titlePath = null;
		if (lastActiveEditor != null) {
			IEditorInput editorInput = lastActiveEditor.getEditorInput();
			if (editorInput instanceof IFileEditorInput) {
				titlePath = computeTitlePath((IFileEditorInput) editorInput);
			} else if (editorInput instanceof FileStoreEditorInput) {
				titlePath = computeTitlePath((FileStoreEditorInput) editorInput);
			}
		}
		titlePathUpdater.updateTitlePath(getWindowConfigurer().getWindow().getShell(), titlePath);
	}

	private String computeTitlePath(FileStoreEditorInput editorInput) {
		return editorInput.getURI().getPath();
	}

	private String computeTitlePath(IFileEditorInput editorInput) {
		IFile file = editorInput.getFile();
		if (file == null) {
			return null;
		}
		IPath location = file.getLocation();
		if (location != null) {
			return location.toFile().toString();
		}
		return null;
	}

	/**
	 * Updates the window title. Format will be: [pageInput -]
	 * [currentPerspective -] [editorInput -] [workspaceLocation -] productName
	 * @param editorHidden TODO
	 */
	private void updateTitle(boolean editorHidden) {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		IWorkbenchWindow window = configurer.getWindow();
		IEditorPart activeEditor = null;
		IWorkbenchPage currentPage = window.getActivePage();
		IPerspectiveDescriptor persp = null;
		IAdaptable input = null;

		if (currentPage != null) {
			activeEditor = currentPage.getActiveEditor();
			persp = currentPage.getPerspective();
			input = currentPage.getInput();
		}

		if (editorHidden) {
			activeEditor = null;
		}

		// Nothing to do if the editor hasn't changed
		if (activeEditor == lastActiveEditor && currentPage == lastActivePage
				&& persp == lastPerspective && input == lastInput) {
			return;
		}

		if (lastActiveEditor != null) {
			lastActiveEditor.removePropertyListener(editorPropertyListener);
		}

		if (window.isClosing()) {
			return;
		}

		lastActiveEditor = activeEditor;
		lastActivePage = currentPage;
		lastPerspective = persp;
		lastInput = input;

		if (activeEditor != null) {
			activeEditor.addPropertyListener(editorPropertyListener);
		}

		recomputeTitle();
	}

	@Override
	public void postWindowRestore() throws WorkbenchException {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		IWorkbenchWindow window = configurer.getWindow();

		int index = getWorkbench().getWorkbenchWindowCount() - 1;

		AboutInfo[] welcomePerspectiveInfos = wbAdvisor
				.getWelcomePerspectiveInfos();
		if (index >= 0 && welcomePerspectiveInfos != null
				&& index < welcomePerspectiveInfos.length) {
			// find a page that exist in the window
			IWorkbenchPage page = window.getActivePage();
			if (page == null) {
				IWorkbenchPage[] pages = window.getPages();
				if (pages != null && pages.length > 0) {
					page = pages[0];
				}
			}

			// if the window does not contain a page, create one
			String perspectiveId = welcomePerspectiveInfos[index]
					.getWelcomePerspectiveId();
			if (page == null) {
				IAdaptable root = wbAdvisor.getDefaultPageInput();
				page = window.openPage(perspectiveId, root);
			} else {
				IPerspectiveRegistry reg = getWorkbench()
						.getPerspectiveRegistry();
				IPerspectiveDescriptor desc = reg
						.findPerspectiveWithId(perspectiveId);
				if (desc != null) {
					page.setPerspective(desc);
				}
			}

			// set the active page and open the welcome editor
			window.setActivePage(page);
			page.openEditor(new WelcomeEditorInput(
					welcomePerspectiveInfos[index]), WELCOME_EDITOR_ID, true);
		}
		cleanUpEditorArea();
	}

	/**
	 * Tries to open the intro, if one exists and otherwise will open the legacy
	 * Welcome pages.
	 *
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#openIntro()
	 */
	@Override
	public void openIntro() {
		if (editorsAndIntrosOpened) {
			return;
		}

		editorsAndIntrosOpened = true;

		// don't try to open the welcome editors if there is an intro
		if (wbAdvisor.hasIntro()) {
			super.openIntro();
		} else {
			openWelcomeEditors(getWindowConfigurer().getWindow());
			// save any preferences changes caused by the above actions
			IDEWorkbenchPlugin.getDefault().savePluginPreferences();
		}
	}

	/*
	 * Open the welcome editor for the primary feature and for any newly
	 * installed features.
	 */
	private void openWelcomeEditors(IWorkbenchWindow window) {
		if (IDEWorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(
				IDEInternalPreferences.WELCOME_DIALOG)) {
			// show the welcome page for the product the first time the
			// workbench opens
			IProduct product = Platform.getProduct();
			if (product == null) {
				return;
			}

			AboutInfo productInfo = new AboutInfo(product);
			URL url = productInfo.getWelcomePageURL();
			if (url == null) {
				return;
			}

			IDEWorkbenchPlugin.getDefault().getPreferenceStore().setValue(
					IDEInternalPreferences.WELCOME_DIALOG, false);
			openWelcomeEditor(window, new WelcomeEditorInput(productInfo), null);
		} else {
			// Show the welcome page for any newly installed features
			List<AboutInfo> welcomeFeatures = new ArrayList<>();
			for (AboutInfo info : wbAdvisor.getNewlyAddedBundleGroups().values()) {
				if (info != null && info.getWelcomePageURL() != null) {
					welcomeFeatures.add(info);
					// activate the feature plug-in so it can run some install
					// code
					String pi = info.getBrandingBundleId();
					if (pi != null) {
						// Start the bundle if there is one
						Bundle bundle = Platform.getBundle(pi);
						if (bundle != null) {
							try {
								bundle.start(Bundle.START_TRANSIENT);
							} catch (BundleException exception) {
								StatusManager.getManager().handle(new Status(IStatus.ERROR, IDEApplication.PLUGIN_ID,
										"Failed to load feature", exception));//$NON-NLS-1$
							}
						}
					}
				}
			}

			int wCount = getWorkbench().getWorkbenchWindowCount();
			for (int i = 0; i < welcomeFeatures.size(); i++) {
				AboutInfo newInfo = welcomeFeatures.get(i);
				String id = newInfo.getWelcomePerspectiveId();
				// Other editors were already opened in postWindowRestore(..)
				if (id == null || i >= wCount) {
					openWelcomeEditor(window, new WelcomeEditorInput(newInfo),
							id);
				}
			}
		}
	}

	/**
	 * Open a welcome editor for the given input
	 */
	private void openWelcomeEditor(IWorkbenchWindow window,
			WelcomeEditorInput input, String perspectiveId) {
		if (getWorkbench().getWorkbenchWindowCount() == 0) {
			// Something is wrong, there should be at least
			// one workbench window open by now.
			return;
		}

		IWorkbenchWindow win = window;
		if (perspectiveId != null) {
			try {
				win = getWorkbench().openWorkbenchWindow(perspectiveId,
						wbAdvisor.getDefaultPageInput());
				if (win == null) {
					win = window;
				}
			} catch (WorkbenchException e) {
				IDEWorkbenchPlugin
						.log(
								"Error opening window with welcome perspective.", e.getStatus()); //$NON-NLS-1$
				return;
			}
		}

		if (win == null) {
			win = getWorkbench().getWorkbenchWindows()[0];
		}

		IWorkbenchPage page = win.getActivePage();
		String id = perspectiveId;
		if (id == null) {
			id = getWorkbench().getPerspectiveRegistry()
					.getDefaultPerspective();
		}

		if (page == null) {
			try {
				page = win.openPage(id, wbAdvisor.getDefaultPageInput());
			} catch (WorkbenchException e) {
				ErrorDialog.openError(win.getShell(),
						IDEWorkbenchMessages.Problems_Opening_Page, e
								.getMessage(), e.getStatus());
			}
		}
		if (page == null) {
			return;
		}

		if (page.getPerspective() == null) {
			try {
				page = getWorkbench().showPerspective(id, win);
			} catch (WorkbenchException e) {
				ErrorDialog
						.openError(
								win.getShell(),
								IDEWorkbenchMessages.Workbench_openEditorErrorDialogTitle,
								IDEWorkbenchMessages.Workbench_openEditorErrorDialogMessage,
								e.getStatus());
				return;
			}
		}

		page.setEditorAreaVisible(true);

		// see if we already have an editor
		IEditorPart editor = page.findEditor(input);
		if (editor != null) {
			page.activate(editor);
			return;
		}

		try {
			page.openEditor(input, WELCOME_EDITOR_ID);
		} catch (PartInitException e) {
			ErrorDialog
					.openError(
							win.getShell(),
							IDEWorkbenchMessages.Workbench_openEditorErrorDialogTitle,
							IDEWorkbenchMessages.Workbench_openEditorErrorDialogMessage,
							e.getStatus());
		}
		return;
	}

	@Override
	public Control createEmptyWindowContents(Composite parent) {
		final IWorkbenchWindow window = getWindowConfigurer().getWindow();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		Display display = composite.getDisplay();
		Color bgCol = display
				.getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
		composite.setBackground(bgCol);
		Label label = new Label(composite, SWT.WRAP);
		label.setForeground(display
				.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		label.setBackground(bgCol);
		label.setFont(JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT));
		String msg = IDEWorkbenchMessages.IDEWorkbenchAdvisor_noPerspective;
		label.setText(msg);
		ToolBarManager toolBarManager = new ToolBarManager();
		// TODO: should obtain the open perspective action from ActionFactory
		openPerspectiveAction = ActionFactory.OPEN_PERSPECTIVE_DIALOG
				.create(window);
		toolBarManager.add(openPerspectiveAction);
		ToolBar toolBar = toolBarManager.createControl(composite);
		toolBar.setBackground(bgCol);
		return composite;
	}
	@Override
	public void dispose() {
		if (propertyChangeListener != null) {
			IDEWorkbenchPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
			propertyChangeListener = null;
		}

		if (openPerspectiveAction!=null) {
			openPerspectiveAction.dispose();
			openPerspectiveAction = null;
		}
		super.dispose();
	}

}
