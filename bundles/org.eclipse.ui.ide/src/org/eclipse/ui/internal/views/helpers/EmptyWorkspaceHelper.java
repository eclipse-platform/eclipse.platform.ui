/*******************************************************************************
 * Copyright (c) 2019 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthias Becker <ma.becker@sap.com> - Bug 543746
 ******************************************************************************/
package org.eclipse.ui.internal.views.helpers;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.NewProjectAction;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.navigator.wizards.WizardShortcutAction;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.eclipse.ui.wizards.IWizardRegistry;

/**
 * This class can be re-used by views that show the workspace's projects (like
 * e.g. the "Project Explorer" does). With the help of this class these views
 * can show explanatory text in cases where no project is in the workspace. The
 * focus here is on "on-boarding" occasional or new users. They might be lost
 * when you first open their IDE and see a window with an empty editor area and
 * some (mainly) empty views. Their questions are: "How do I start? How do I get
 * content into here?"
 *
 * This class uses a stack layout to switch between the "original" composite of
 * the view and an additional composite given the user the explanatory text.
 * This text is displayed when no projects are in the workspace. Once projects
 * are created this class switches back to the "original" composite of the view.
 *
 * The explanatory text explains the current situation that no projects are
 * available and provides a list of options to create projects. This list
 * contains links to:
 * <ol>
 * <li>Project creation wizards specific to the current perspective</li>
 * <li>The "New Project Wizard" to allow creation of project of any type</li>
 * </ol>
 * If no perspective specific project creation wizards are found then a simple
 * text with a link to the "New Project Wizard" is shown.
 *
 * This class also takes care of refreshing these links when the user switches
 * the perspective.
 *
 */
public final class EmptyWorkspaceHelper {

	private Composite parent;
	private Composite emptyArea;
	private StackLayout layout;
	private Tree control;
	private boolean controlIsEmpty;
	private Composite displayArea;
	private ArrayList<IAction> projectWizardActions;
	private IAction newProjectAction;
	private IAction importAction;
	private LocalResourceManager resourceManager;

	/**
	 * This method should be called at the point in time when the view's controls
	 * are created.
	 *
	 * @param aParent The composite where the explanatory text should be put into.
	 * @return A new composite (a child of "parent") that has to be used by
	 *         consumers as parent for their UI elements
	 */
	public Composite getComposite(Composite aParent) {
		this.parent = aParent;
		displayArea = new Composite(parent, SWT.NONE);

		layout = new StackLayout();
		displayArea.setLayout(layout);
		createEmptyArea(displayArea);

		return displayArea;
	}

	/**
	 * This method should be used to hand over the "original" control that is
	 * "normally" visible in the view.
	 *
	 * @param control The "original" tree control of the view.
	 */
	public void setNonEmptyControl(Control control) {
		this.control = (Tree) control;
		controlIsEmpty = this.control.getItemCount() == 0;
		emptyArea.setBackground(control.getBackground());
		switchTopControl();
		registerListeners();
	}

	private void dispose(Listener listener) {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			activeWorkbenchWindow.removePerspectiveListener(listener);
		}
		JFaceResources.getColorRegistry().removeListener(listener);
		parent.removeDisposeListener(listener);
		control.removeListener(SWT.EmptinessChanged, listener);

		// paranoia
		parent = null;
		emptyArea = null;
		layout = null;
		control = null;
		displayArea = null;
		projectWizardActions = null;
		newProjectAction = null;
		importAction = null;
	}

	private void registerListeners() {
		Listener listener = new Listener();
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			activeWorkbenchWindow.addPerspectiveListener(listener);
		}
		control.addListener(SWT.EmptinessChanged, listener);
		JFaceResources.getColorRegistry().addListener(listener);
		parent.addDisposeListener(listener);
	}

	private void createEmptyArea(Composite displayAreas) {
		if (newProjectAction == null) {
			newProjectAction = new NewProjectAction();
		}
		if (importAction == null) {
			importAction = new ImportAction();
		}
		if (projectWizardActions == null) {
			projectWizardActions = new ArrayList<>();
			readProjectWizardActions();
		}

		emptyArea = new Composite(displayAreas, SWT.NONE);
		resourceManager = new LocalResourceManager(JFaceResources.getResources(), emptyArea);

		emptyArea.setBackgroundMode(SWT.INHERIT_FORCE);
		GridLayoutFactory.fillDefaults().applyTo(emptyArea);
		Composite infoArea = new Composite(emptyArea, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.TOP).grab(true, true).indent(5, 5).applyTo(infoArea);
		GridLayoutFactory.swtDefaults().applyTo(infoArea);
		Link messageLabel = new Link(infoArea, SWT.WRAP);

		messageLabel.setText(ResourceNavigatorMessages.EmptyWorkspaceHelper_noProjectsAvailable);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(messageLabel);

		Composite optionsArea = new Composite(infoArea, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(optionsArea);
		GridDataFactory.swtDefaults().indent(5, 0).grab(true, true).applyTo(optionsArea);

		final FormToolkit toolkit = new FormToolkit(emptyArea.getDisplay());
		emptyArea.addDisposeListener(e -> toolkit.dispose());
		final Color linkColor = JFaceColors.getHyperlinkText(emptyArea.getDisplay());

		for (IAction action : projectWizardActions) {
			String description = action.getDescription();
			if (description == null || description.isEmpty()) {
				description = action.getText();
			}
			createOption(optionsArea, toolkit, linkColor, action, action.getImageDescriptor(), description);
		}
		createOption(optionsArea, toolkit, linkColor, newProjectAction, newProjectAction.getImageDescriptor(),
				ResourceNavigatorMessages.EmptyWorkspaceHelper_createProject);
		createOption(optionsArea, toolkit, linkColor, importAction,
				WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_IMPORT_WIZ),
				ResourceNavigatorMessages.EmptyWorkspaceHelper_importProjects);
	}

	private void recreateEmptyArea() {
		disposeEmptyArea();

		// forces to re-create wizard actions, fixes Bug 552374
		projectWizardActions = null;

		// re-read the project wizards and re-create the empty area
		createEmptyArea(displayArea);
		if (control != null && !control.isDisposed()) {
			emptyArea.setBackground(control.getBackground());
		}
	}

	private void disposeEmptyArea() {
		if (emptyArea != null) {
			// throw away already existing empty area
			emptyArea.dispose();
			emptyArea = null;
		}
	}

	private void readProjectWizardActions() {
		if (projectWizardActions == null) {
			return;
		}
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		if (win == null) {
			return;
		}
		IWorkbenchPage page = win.getActivePage();
		String[] wizardIds = page.getNewWizardShortcuts();
		projectWizardActions.clear();
		for (String wizardId : wizardIds) {
			IWizardRegistry newWizardRegistry = WorkbenchPlugin.getDefault().getNewWizardRegistry();
			IWizardDescriptor wizardDesc = newWizardRegistry.findWizard(wizardId);
			if (wizardDesc == null) {
				continue;
			}
			String[] tags = wizardDesc.getTags();
			for (String tag : tags) {
				if (WorkbenchWizardElement.TAG_PROJECT.equals(tag)) {
					IAction action = getAction(newWizardRegistry, wizardId);
					if (action != null) {
						projectWizardActions.add(action);
					}
				}
			}
		}
	}

	private void createOption(Composite optionsArea, final FormToolkit toolkit, final Color linkColor, IAction action,
			ImageDescriptor imageDesc, String text) {
		Label addLabel = new Label(optionsArea, SWT.NONE);

		if (imageDesc == null) {
			ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
			imageDesc = images.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD);
		}
		addLabel.setImage(resourceManager.createImage(imageDesc));

		Hyperlink addLink = toolkit.createHyperlink(optionsArea, text, SWT.WRAP);
		addLink.setForeground(linkColor);
		addLink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				action.run();
			}
		});
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(addLink);
	}

	private IAction getAction(IWizardRegistry registry, String id) {
		IWizardDescriptor wizardDesc = registry.findWizard(id);
		WizardShortcutAction action = null;
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		if (wizardDesc != null && win != null) {
			action = new WizardShortcutAction(win, wizardDesc);
		}
		return action;
	}

	private Runnable switchTopControlRunnable = () -> {
		if (switchTopControl()) {
			displayArea.requestLayout();
		}
	};

	private boolean switchTopControl() {
		if (control == null || control.isDisposed()) {
			return false;
		}
		Control oldTop = layout.topControl;
		if (!controlIsEmpty) {
			layout.topControl = control;
			disposeEmptyArea();
		} else {
			if (emptyArea == null || emptyArea.isDisposed()) {
				recreateEmptyArea();
			}
			layout.topControl = emptyArea;
		}
		return oldTop != layout.topControl;
	}

	private class Listener
			implements org.eclipse.swt.widgets.Listener, //
			IPerspectiveListener, IPropertyChangeListener, DisposeListener {

		@Override
		public void handleEvent(Event e) {
			if (e.type == SWT.EmptinessChanged) {
				controlIsEmpty = e.detail == 1;
				switchTopControlRunnable.run();
			}
		}

		/**
		 * update the list of available project wizards and refresh empty area
		 *
		 * @noreference This method is not intended to be referenced by clients.
		 */
		@Override
		public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			if (emptyArea != null) {
				recreateEmptyArea();
				switchTopControlRunnable.run();
			}
		}

		/**
		 * @noreference This method is not intended to be referenced by clients.
		 */
		@Override
		public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
			// do nothing
		}

		/**
		 * React on changes to the hyperlink color
		 *
		 * @noreference This method is not intended to be referenced by clients.
		 */
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (emptyArea != null && JFacePreferences.HYPERLINK_COLOR.equals(event.getProperty())) {
				recreateEmptyArea();
				switchTopControlRunnable.run();
			}
		}

		@Override
		public void widgetDisposed(DisposeEvent e) {
			dispose(this);
		}
	}

	private static class ImportAction extends Action {
		@Override
		public void run() {
			IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (activeWorkbenchWindow == null) {
				return;
			}
			IHandlerService handlerService = activeWorkbenchWindow
					.getService(IHandlerService.class);
			try {
				handlerService.executeCommand("org.eclipse.ui.file.import", null); //$NON-NLS-1$
			} catch (Exception ex) {
				IDEWorkbenchPlugin.log(this.getClass(), "run", ex); //$NON-NLS-1$
			}
		}
	}

}
