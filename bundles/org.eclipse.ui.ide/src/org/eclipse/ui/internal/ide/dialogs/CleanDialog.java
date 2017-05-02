/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - [IDE] Project>Clean dialog should not use a question-mark icon - http://bugs.eclipse.org/155436
 *     Mark Melvin <mark_melvin@amis.com>
 *     Christian Georgi <christian.georgi@sap.com> -  [IDE] Clean dialog should scroll to reveal selected projects - http://bugs.eclipse.org/415522
 *     Andrey Loskutov <loskutov@gmx.de> - generified interface, bug 462760
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472784
 *     David Weiser <David.Weiser@vogella.com> - Bug 500598
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.dialogs.SearchPattern;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.IProgressConstants2;

/**
 * Dialog that asks the user to confirm a clean operation, and to configure
 * settings in relation to the clean. Clicking ok in the dialog will perform the
 * clean operation.
 *
 * @since 3.0
 */
public class CleanDialog extends MessageDialog {

    private class ProjectSubsetBuildAction extends BuildAction {

        private IProject[] projectsToBuild = new IProject[0];

        public ProjectSubsetBuildAction(IShellProvider shellProvider, int type, IProject[] projects) {
            super(shellProvider, type);
            this.projectsToBuild = projects;
        }

        @Override
		protected List<? extends IResource> getSelectedResources() {
            return Arrays.asList(this.projectsToBuild);
        }
	}

    private static final String DIALOG_SETTINGS_SECTION = "CleanDialogSettings"; //$NON-NLS-1$
    private static final String DIALOG_ORIGIN_X = "DIALOG_X_ORIGIN"; //$NON-NLS-1$
    private static final String DIALOG_ORIGIN_Y = "DIALOG_Y_ORIGIN"; //$NON-NLS-1$
    private static final String DIALOG_WIDTH = "DIALOG_WIDTH"; //$NON-NLS-1$
    private static final String DIALOG_HEIGHT = "DIALOG_HEIGHT"; //$NON-NLS-1$
	private static final String TOGGLE_SELECTED = "TOGGLE_SELECTED"; //$NON-NLS-1$
    private static final String BUILD_NOW = "BUILD_NOW"; //$NON-NLS-1$
    private static final String BUILD_ALL = "BUILD_ALL"; //$NON-NLS-1$

	private Button alwaysCleanButton, buildNowButton, globalBuildButton,
			projectBuildButton;

    private CheckboxTableViewer projectNames;

    private Object[] selection;

    private IWorkbenchWindow window;

	private Text filterText;
	private SearchPattern searchPattern = new SearchPattern();
	private Label clearLabel;

	/**
	 * Image descriptor for enabled clear button.
	 */
	private static final String CLEAR_ICON = "org.eclipse.ui.internal.dialogs.CLEANDIALOG_CLEAR_ICON"; //$NON-NLS-1$

	/**
	 * Image descriptor for disabled clear button.
	 */
	private static final String DISABLED_CLEAR_ICON = "org.eclipse.ui.internal.dialogs.CLEANDIALOG_DCLEAR_ICON"; //$NON-NLS-1$

	/**
	 * Get image descriptors for the clear button.
	 */
	static {
		ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(PlatformUI.PLUGIN_ID,
				"$nl$/icons/full/etool16/clear_co.png"); //$NON-NLS-1$
		if (descriptor != null) {
			JFaceResources.getImageRegistry().put(CLEAR_ICON, descriptor);
		}
		descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(PlatformUI.PLUGIN_ID,
				"$nl$/icons/full/dtool16/clear_co.png"); //$NON-NLS-1$
		if (descriptor != null) {
			JFaceResources.getImageRegistry().put(DISABLED_CLEAR_ICON, descriptor);
		}
	}

    /**
     * Gets the text of the clean dialog, depending on whether the
     * workspace is currently in autobuild mode.
     * @return String the question the user will be asked.
     */
    private static String getQuestion() {
        boolean autoBuilding = ResourcesPlugin.getWorkspace().isAutoBuilding();
        if (autoBuilding) {
            return IDEWorkbenchMessages.CleanDialog_buildCleanAuto;
        }
        return IDEWorkbenchMessages.CleanDialog_buildCleanManual;
    }

    /**
     * Creates a new clean dialog.
     *
     * @param window the window to create it in
     * @param selection the currently selected projects (may be empty)
     */
    public CleanDialog(IWorkbenchWindow window, IProject[] selection) {
		super(window.getShell(), IDEWorkbenchMessages.CleanDialog_title, null, getQuestion(), NONE, 0,
				IDEWorkbenchMessages.CleanDialog_clean_button_label, IDialogConstants.CANCEL_LABEL);
        this.window = window;
        this.selection = selection;
        if (this.selection == null) {
            this.selection = new Object[0];
        }
		searchPattern.setPattern(""); //$NON-NLS-1$
    }

    @Override
	protected void buttonPressed(int buttonId) {
		final boolean cleanAll = alwaysCleanButton.getSelection();
		final boolean buildAll = buildNowButton != null && buildNowButton.getSelection();
		final boolean globalBuild = globalBuildButton != null && globalBuildButton.getSelection();
        super.buttonPressed(buttonId);
        if (buttonId != IDialogConstants.OK_ID) {
            return;
        }
        //save all dirty editors
        BuildUtilities.saveEditors(null);
        //batching changes ensures that autobuild runs after cleaning
		WorkspaceJob cleanJob = new WorkspaceJob(
				cleanAll ? IDEWorkbenchMessages.CleanDialog_cleanAllTaskName
						: IDEWorkbenchMessages.CleanDialog_cleanSelectedTaskName) {
            @Override
			public boolean belongsTo(Object family) {
                return ResourcesPlugin.FAMILY_MANUAL_BUILD.equals(family);
            }
            @Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                doClean(cleanAll, monitor);
                //see if a build was requested
                if (buildAll) {
                    // Only build what was requested
                    if (globalBuild) {
                        //start an immediate workspace build
						GlobalBuildAction build = new GlobalBuildAction(window,
								IncrementalProjectBuilder.INCREMENTAL_BUILD);
                        build.doBuild();
                    } else {
                        // Only build what was cleaned
                        IProject[] projects = new IProject[selection.length];
                        for (int i = 0; i < selection.length; i++) {
                            projects[i] = (IProject) selection[i];
                        }

                        ProjectSubsetBuildAction projectBuild =
                            new ProjectSubsetBuildAction(window,
                                IncrementalProjectBuilder.INCREMENTAL_BUILD,
                                projects);
                        projectBuild.runInBackground(
                                ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
                    }
                }
                return Status.OK_STATUS;
            }
        };
		cleanJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
        cleanJob.setUser(true);
        cleanJob.setProperty(IProgressConstants2.SHOW_IN_TASKBAR_ICON_PROPERTY, Boolean.TRUE);
        cleanJob.schedule();
    }

    @Override
	protected Control createCustomArea(Composite parent) {
        Composite area = new Composite(parent, SWT.NONE);
        GridLayout areaLayout = new GridLayout();
        areaLayout.marginWidth = areaLayout.marginHeight = 0;
		areaLayout.numColumns = 1;
		areaLayout.makeColumnsEqualWidth = false;
        area.setLayout(areaLayout);
        area.setLayoutData(new GridData(GridData.FILL_BOTH));

		IDialogSettings settings = getDialogSettings(DIALOG_SETTINGS_SECTION);

		alwaysCleanButton = new Button(area, SWT.CHECK);
		alwaysCleanButton.setText(IDEWorkbenchMessages.CleanDialog_alwaysCleanAllButton);
		alwaysCleanButton.setSelection(!settings.getBoolean(TOGGLE_SELECTED));
		alwaysCleanButton.addSelectionListener(widgetSelectedAdapter(e -> {
			updateEnablement();
			if (!alwaysCleanButton.getSelection()) {
				setInitialFilterText();
			} else {
				filterText.setText(""); //$NON-NLS-1$
			}
		}));

		Composite filterTextArea = null;
		if (useNativeSearchField(area)) {
			filterTextArea = new Composite(area, SWT.NONE);
			filterText = new Text(filterTextArea, SWT.BORDER | SWT.SINGLE | SWT.SEARCH | SWT.ICON_CANCEL);
		} else {
			filterTextArea = new Composite(area, SWT.BORDER);
			filterText = new Text(filterTextArea, SWT.SINGLE);
		}

		GridLayout filterTextLayout = new GridLayout();
		filterTextLayout.marginWidth = 0;
		filterTextLayout.marginHeight = 0;
		filterTextLayout.numColumns = 1;
		filterTextLayout.horizontalSpacing = 0;
		filterTextLayout.makeColumnsEqualWidth = false;
		filterTextArea.setLayout(filterTextLayout);
		filterTextArea.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		filterText.setMessage(IDEWorkbenchMessages.CleanDialog_typeFilterText);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		filterText.setLayoutData(gd);
		filterText.addModifyListener(e -> {
			String filter = filterText.getText();
			if (filter.startsWith("*") || filter.startsWith("?")) { //$NON-NLS-1$ //$NON-NLS-2$
				searchPattern.setPattern(filter);
			} else {
				searchPattern.setPattern("*" + filter); //$NON-NLS-1$
			}

			if (filter.isEmpty()) {
				filterText.setMessage(IDEWorkbenchMessages.CleanDialog_typeFilterText);
			}

			showClearButton(!filter.isEmpty() && !filter.equals(IDEWorkbenchMessages.CleanDialog_typeFilterText));


			projectNames.refresh();
		});

		filterText.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (filterText.getText().equals(IDEWorkbenchMessages.CleanDialog_typeFilterText)) {
					filterText.setText(""); //$NON-NLS-1$
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});

		createClearTextNew(filterTextArea);
		showClearButton(false);

		createProjectSelectionTable(area);
		if (!alwaysCleanButton.getSelection()) {
			setInitialFilterText();
		}

        //only prompt for immediate build if autobuild is off
        if (!ResourcesPlugin.getWorkspace().isAutoBuilding()) {
			SelectionListener updateEnablement = widgetSelectedAdapter(e -> updateEnablement());

            buildNowButton = new Button(parent, SWT.CHECK);
            buildNowButton.setText(IDEWorkbenchMessages.CleanDialog_buildNowButton);
            String buildNow = settings.get(BUILD_NOW);
            buildNowButton.setSelection(buildNow == null || Boolean.valueOf(buildNow).booleanValue());
			buildNowButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
            buildNowButton.addSelectionListener(updateEnablement);

            globalBuildButton = new Button(parent, SWT.RADIO);
            globalBuildButton.setText(IDEWorkbenchMessages.CleanDialog_globalBuildButton);
            String buildAll = settings.get(BUILD_ALL);
            globalBuildButton.setSelection(buildAll == null || Boolean.valueOf(buildAll).booleanValue());
            GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
            data.horizontalIndent = 10;
            globalBuildButton.setLayoutData(data);
            globalBuildButton.setEnabled(buildNowButton.getSelection());

            projectBuildButton = new Button(parent, SWT.RADIO);
            projectBuildButton.setText(IDEWorkbenchMessages.CleanDialog_buildSelectedProjectsButton);
            projectBuildButton.setSelection(!globalBuildButton.getSelection());
            data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
            data.horizontalIndent = 10;
            projectBuildButton.setLayoutData(data);
            projectBuildButton.setEnabled(buildNowButton.getSelection());

			SelectionListener buildRadioSelected = widgetSelectedAdapter(e -> updateBuildRadioEnablement());

            globalBuildButton.addSelectionListener(buildRadioSelected);
            projectBuildButton.addSelectionListener(buildRadioSelected);
        }
        return area;
    }

	private static boolean useNativeSearchField(Composite composite) {
		boolean useNativeSearchField = true;
		Text testText = null;
		try {
			testText = new Text(composite, SWT.SEARCH | SWT.ICON_CANCEL);
			useNativeSearchField = Boolean.valueOf((testText.getStyle() & SWT.ICON_CANCEL) != 0);
		} finally {
			if (testText != null) {
				testText.dispose();
			}
		}
		return useNativeSearchField;
	}

	private void setInitialFilterText() {
		filterText.setText(IDEWorkbenchMessages.CleanDialog_typeFilterText);
		filterText.selectAll();
		filterText.setFocus();
	}

	protected void showClearButton(boolean visible) {
		if (clearLabel != null) {
			clearLabel.setVisible(visible);
			GridData layoutData = (GridData) clearLabel.getLayoutData();
			layoutData.exclude = !visible;
			clearLabel.getParent().requestLayout();
		}
	}

    @Override
	protected Control createContents(Composite parent) {
    	Control contents= super.createContents(parent);
    	updateEnablement();
    	return contents;
    }

	private void createProjectSelectionTable(Composite parent) {
		projectNames = CheckboxTableViewer.newCheckList(parent, SWT.BORDER);
        projectNames.setContentProvider(new WorkbenchContentProvider());
        projectNames.setLabelProvider(new WorkbenchLabelProvider());
        projectNames.setComparator(new ResourceComparator(ResourceComparator.NAME));
        projectNames.addFilter(new ViewerFilter() {
            private final IProject[] projectHolder = new IProject[1];
            @Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (!(element instanceof IProject)) {
                    return false;
                }
                IProject project = (IProject) element;
				boolean isProjectNameMatchingPattern = searchPattern.matches(project.getName());
				if (!project.isAccessible() || !isProjectNameMatchingPattern) {
					if (!filterText.getText().equals(IDEWorkbenchMessages.CleanDialog_typeFilterText)) {
						return false;
					}
                }
                projectHolder[0] = project;
                return BuildUtilities.isEnabled(projectHolder, IncrementalProjectBuilder.CLEAN_BUILD);
            }
        });
        projectNames.setInput(ResourcesPlugin.getWorkspace().getRoot());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.heightHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        projectNames.getTable().setLayoutData(data);
        projectNames.setCheckedElements(selection);
        Object[] checked = projectNames.getCheckedElements();
		// reveal first checked project
		if (checked.length > 0) {
            projectNames.reveal(checked[0]);
        }
        projectNames.addCheckStateListener(event -> {
		    selection = projectNames.getCheckedElements();
		    updateEnablement();
		});
    }

    /**
     * Performs the actual clean operation.
     * @param cleanAll if <code>true</true> clean all projects
     * @param monitor The monitor that the build will report to
     * @throws CoreException thrown if there is a problem from the
     * core builder.
     */
    protected void doClean(boolean cleanAll, IProgressMonitor monitor)
            throws CoreException {
        if (cleanAll) {
			ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
        } else {
			SubMonitor subMonitor = SubMonitor.convert(monitor, IDEWorkbenchMessages.CleanDialog_cleanSelectedTaskName,
					selection.length);
			for (Object currentSelection : selection) {
				((IProject) currentSelection).build(IncrementalProjectBuilder.CLEAN_BUILD, subMonitor.split(1));
            }
        }
    }

    /**
	 * Updates the enablement of the dialog elements based on the current
	 * choices in the dialog.
	 */
    protected void updateEnablement() {
		projectNames.getTable().setEnabled(!alwaysCleanButton.getSelection());
		filterText.setEnabled(!alwaysCleanButton.getSelection());

		boolean enabled = selection.length > 0 || alwaysCleanButton.getSelection();
        getButton(OK).setEnabled(enabled);
        if (globalBuildButton != null) {
            globalBuildButton.setEnabled(buildNowButton.getSelection());
        }
        if (projectBuildButton != null) {
            projectBuildButton.setEnabled(buildNowButton.getSelection());
        }
    }

    /**
     * Updates the enablement of the dialog's build selection radio
     * buttons.
     */
    protected void updateBuildRadioEnablement() {
        projectBuildButton.setSelection(!globalBuildButton.getSelection());
    }

    @Override
	public boolean close() {
        persistDialogSettings(getShell(), DIALOG_SETTINGS_SECTION);
        return super.close();
    }

    @Override
	protected Point getInitialLocation(Point initialSize) {
        Point p = getInitialLocation(DIALOG_SETTINGS_SECTION);
        return p != null ? p : super.getInitialLocation(initialSize);
    }

    @Override
	protected Point getInitialSize() {
        Point p = super.getInitialSize();
        return getInitialSize(DIALOG_SETTINGS_SECTION, p);
    }

    /**
     * Returns the initial location which is persisted in the IDE Plugin dialog settings
     * under the provided dialog setttings section name.
     * If location is not persisted in the settings, the <code>null</code> is returned.
     *
     * @param dialogSettingsSectionName The name of the dialog settings section
     * @return The initial location or <code>null</code>
     */
    public Point getInitialLocation(String dialogSettingsSectionName) {
        IDialogSettings settings = getDialogSettings(dialogSettingsSectionName);
        try {
            int x= settings.getInt(DIALOG_ORIGIN_X);
            int y= settings.getInt(DIALOG_ORIGIN_Y);
            return new Point(x,y);
        } catch (NumberFormatException e) {
        }
        return null;
    }

    private IDialogSettings getDialogSettings(String dialogSettingsSectionName) {
        IDialogSettings settings = IDEWorkbenchPlugin.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(dialogSettingsSectionName);
        if (section == null) {
            section = settings.addNewSection(dialogSettingsSectionName);
        }
        return section;
    }

    /**
     * Persists the location and dimensions of the shell and other user settings in the
     * plugin's dialog settings under the provided dialog settings section name
     *
     * @param shell The shell whose geometry is to be stored
     * @param dialogSettingsSectionName The name of the dialog settings section
     */
    private void persistDialogSettings(Shell shell, String dialogSettingsSectionName) {
        Point shellLocation = shell.getLocation();
        Point shellSize = shell.getSize();
        IDialogSettings settings = getDialogSettings(dialogSettingsSectionName);
        settings.put(DIALOG_ORIGIN_X, shellLocation.x);
        settings.put(DIALOG_ORIGIN_Y, shellLocation.y);
        settings.put(DIALOG_WIDTH, shellSize.x);
        settings.put(DIALOG_HEIGHT, shellSize.y);

        if (buildNowButton != null) {
            settings.put(BUILD_NOW, buildNowButton.getSelection());
        }
        if (globalBuildButton != null) {
            settings.put(BUILD_ALL, globalBuildButton.getSelection());
        }

		settings.put(TOGGLE_SELECTED, !alwaysCleanButton.getSelection());
    }

    /**
     * Returns the initial size which is the larger of the <code>initialSize</code> or
     * the size persisted in the IDE UI Plugin dialog settings under the provided dialog setttings section name.
     * If no size is persisted in the settings, the <code>initialSize</code> is returned.
     *
     * @param initialSize The initialSize to compare against
     * @param dialogSettingsSectionName The name of the dialog settings section
     * @return the initial size
     */
    private Point getInitialSize(String dialogSettingsSectionName, Point initialSize) {
        IDialogSettings settings = getDialogSettings(dialogSettingsSectionName);
        try {
            int x, y;
            x = settings.getInt(DIALOG_WIDTH);
            y = settings.getInt(DIALOG_HEIGHT);
            return new Point(Math.max(x, initialSize.x), Math.max(y, initialSize.y));
        } catch (NumberFormatException e) {
        }
        return initialSize;
    }

    @Override
	protected boolean isResizable() {
        return true;
    }

	/**
	 * Create the button that clears the text.
	 *
	 * @param parent
	 *            parent <code>Composite</code> of button
	 */
	private void createClearTextNew(Composite parent) {
		// only create the button if the text widget doesn't support one
		// natively
		if ((filterText.getStyle() & SWT.ICON_CANCEL) == 0) {
			// add one additional column to the parent view to add space for the clear
			// button
			((GridLayout) parent.getLayout()).numColumns = 2;

			final Image inactiveImage = JFaceResources.getImageRegistry()
					.getDescriptor(DISABLED_CLEAR_ICON).createImage();
			final Image activeImage = JFaceResources.getImageRegistry()
					.getDescriptor(CLEAR_ICON).createImage();
			final Image pressedImage = new Image(parent.getDisplay(), activeImage, SWT.IMAGE_GRAY);

			final Label clearButton = new Label(parent, SWT.NONE);
			clearButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			clearButton.setImage(inactiveImage);
			clearButton.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
			clearButton.setToolTipText(IDEWorkbenchMessages.CleanDialog_clearToolTip);
			clearButton.addMouseListener(new MouseAdapter() {
				private MouseMoveListener fMoveListener;

				@Override
				public void mouseDown(MouseEvent e) {
					clearButton.setImage(pressedImage);
					fMoveListener = new MouseMoveListener() {
						private boolean fMouseInButton = true;

						@Override
						public void mouseMove(MouseEvent event) {
							boolean mouseInButton = isMouseInButton(event);
							if (mouseInButton != fMouseInButton) {
								fMouseInButton = mouseInButton;
								clearButton.setImage(mouseInButton ? pressedImage : inactiveImage);
							}
						}
					};
					clearButton.addMouseMoveListener(fMoveListener);
				}

				@Override
				public void mouseUp(MouseEvent e) {
					if (fMoveListener != null) {
						clearButton.removeMouseMoveListener(fMoveListener);
						fMoveListener = null;
						boolean mouseInButton = isMouseInButton(e);
						clearButton.setImage(mouseInButton ? activeImage : inactiveImage);
						if (mouseInButton) {
							filterText.setText(""); //$NON-NLS-1$
							filterText.selectAll();
							filterText.setFocus();
						}
					}
				}

				private boolean isMouseInButton(MouseEvent e) {
					Point buttonSize = clearButton.getSize();
					return 0 <= e.x && e.x < buttonSize.x && 0 <= e.y && e.y < buttonSize.y;
				}
			});
			clearButton.addMouseTrackListener(new MouseTrackListener() {
				@Override
				public void mouseEnter(MouseEvent e) {
					clearButton.setImage(activeImage);
				}

				@Override
				public void mouseExit(MouseEvent e) {
					clearButton.setImage(inactiveImage);
				}

				@Override
				public void mouseHover(MouseEvent e) {
				}
			});
			clearButton.addDisposeListener(e -> {
				inactiveImage.dispose();
				activeImage.dispose();
				pressedImage.dispose();
			});
			clearButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {
				@Override
				public void getName(AccessibleEvent e) {
					e.result = IDEWorkbenchMessages.CleanDialog_AccessibleListenerClearButton;
				}
			});
			clearButton.getAccessible().addAccessibleControlListener(new AccessibleControlAdapter() {
				@Override
				public void getRole(AccessibleControlEvent e) {
					e.detail = ACC.ROLE_PUSHBUTTON;
				}
			});
			this.clearLabel = clearButton;
		}
	}
}
