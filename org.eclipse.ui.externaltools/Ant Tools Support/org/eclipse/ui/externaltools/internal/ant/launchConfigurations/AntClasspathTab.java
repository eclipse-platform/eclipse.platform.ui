package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.AntCorePreferences;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.externaltools.internal.ant.preferences.AntClasspathContentProvider;
import org.eclipse.ui.externaltools.internal.ant.preferences.AntClasspathLabelProvider;
import org.eclipse.ui.externaltools.internal.ant.preferences.AntPreferencesMessages;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.ui.ExternalToolsContentProvider;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

public class AntClasspathTab extends AbstractLaunchConfigurationTab {

	private Button useDefaultButton;
	private Button reuseClassLoader;

	private TableViewer antTableViewer;
	private ExternalToolsContentProvider antContentProvider;

	private Button antHomeButton;
	private Text antHome;
	private Button browseAntHomeButton;

	private TableViewer userTableViewer;
	private ExternalToolsContentProvider userContentProvider;

	private Button upButton;
	private Button downButton;
	protected Button removeButton;

	private Button upUserButton;
	private Button downUserButton;
	private Button removeUserButton;

	private final AntClasspathLabelProvider labelProvider = new AntClasspathLabelProvider();
	private Button addUserJarButton;
	private Button addUserFolderButtton;
	private Button addFolderButtton;
	private Button addJarButton;
	
	private boolean initializing = false;

	private IDialogSettings fDialogSettings = ExternalToolsPlugin.getDefault().getDialogSettings();

	/**
	 * Creates the group which will contain the buttons.
	 */
	protected void createButtonGroup(Composite top) {
		Composite buttonGroup = new Composite(top, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonGroup.setLayout(layout);
		buttonGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		buttonGroup.setFont(top.getFont());

		addButtonsToButtonGroup(buttonGroup);
	}

	private void addButtonsToButtonGroup(Composite parent) {
		if (addJarButton == null) {
			addJarButton = createPushButton(parent, AntPreferencesMessages.getString("AntClasspathPage.addJarButtonTitle"), null); //$NON-NLS-1$;
			addJarButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					addJars(antTableViewer);
					
				}
			});
			addFolderButtton = createPushButton(parent, AntPreferencesMessages.getString("AntClasspathPage.addFolderButtonTitle"), null); //$NON-NLS-1$;
			addFolderButtton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					addFolder(antTableViewer, AntPreferencesMessages.getString("AntClasspathPage.&Choose_a_folder_to_add_to_the_classpath__1")); //$NON-NLS-1$
				}
			});

			upButton = createPushButton(parent, AntPreferencesMessages.getString("AntClasspathPage.upButtonTitle"), null); //$NON-NLS-1$;
			upButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					handleMove(-1, antTableViewer);
				}
			});
			downButton = createPushButton(parent, AntPreferencesMessages.getString("AntClasspathPage.downButtonTitle"), null); //$NON-NLS-1$;
			downButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					handleMove(1, antTableViewer);
				}
			});
			removeButton = createPushButton(parent, AntPreferencesMessages.getString("AntClasspathPage.removeButtonTitle"), null); //$NON-NLS-1$;
			removeButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					remove(antTableViewer);
				}
			});

		} else {
			addUserJarButton = createPushButton(parent, AntPreferencesMessages.getString("AntClasspathPage.addJarButtonTitle2"), null); //$NON-NLS-1$;
			addUserJarButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					addJars(userTableViewer);
				}
			});
			addUserFolderButtton = createPushButton(parent, AntPreferencesMessages.getString("AntClasspathPage.addFolderButtonTitle2"), null); //$NON-NLS-1$;
			addUserFolderButtton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					addFolder(userTableViewer, AntPreferencesMessages.getString("AntClasspathPage.&Choose_a_folder_to_add_to_the_classpath__1")); //$NON-NLS-1$
				}
			});
			upUserButton = createPushButton(parent, AntPreferencesMessages.getString("AntClasspathPage.upButtonTitle"), null); //$NON-NLS-1$;
			upUserButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					handleMove(-1, userTableViewer);
				}
			});
			downUserButton = createPushButton(parent, AntPreferencesMessages.getString("AntClasspathPage.downButtonTitle2"), null); //$NON-NLS-1$;
			downUserButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					handleMove(1, userTableViewer);
				}
			});
			removeUserButton = createPushButton(parent, AntPreferencesMessages.getString("AntClasspathPage.removeButtonTitle2"), null); //$NON-NLS-1$;
			removeUserButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					remove(userTableViewer);
				}
			});
		}
	}

	private void handleMove(int direction, TableViewer viewer) {
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		List selList = sel.toList();
		Object[] elements = ((ExternalToolsContentProvider) viewer.getContentProvider()).getElements(viewer.getInput());
		List contents = new ArrayList(elements.length);
		for (int i = 0; i < elements.length; i++) {
			contents.add(elements[i]);
		}
		Object[] moved = new Object[contents.size()];
		int i;
		for (Iterator current = selList.iterator(); current.hasNext();) {
			Object config = current.next();
			i = contents.indexOf(config);
			moved[i + direction] = config;
		}

		contents.removeAll(selList);

		for (int j = 0; j < moved.length; j++) {
			Object config = moved[j];
			if (config != null) {
				contents.add(j, config);
			}
		}
		viewer.setInput(contents);
		viewer.setSelection(viewer.getSelection());
		updateLaunchConfigurationDialog();
	}

	protected void remove(TableViewer viewer) {
		ExternalToolsContentProvider antContentProvider = (ExternalToolsContentProvider) viewer.getContentProvider();
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		Iterator enum = sel.iterator();
		while (enum.hasNext()) {
			antContentProvider.remove(enum.next());
		}
		updateLaunchConfigurationDialog();
	}

	/**
		 * Allows the user to enter a folder as a classpath.
		 */
	private void addFolder(TableViewer viewer, String message) {
		String lastUsedPath = fDialogSettings.get(IExternalToolsUIConstants.DIALOGSTORE_LASTFOLDER);
		if (lastUsedPath == null) {
			lastUsedPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		}
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setMessage(message);
		dialog.setFilterPath(lastUsedPath);
		String result = dialog.open();
		if (result != null) {
			try {
				URL url = new URL("file:" + result + "/"); //$NON-NLS-2$;//$NON-NLS-1$;
				((ExternalToolsContentProvider)viewer.getContentProvider()).add(url);
			} catch (MalformedURLException e) {
			}
		}
		viewer.setSelection(viewer.getSelection());
		fDialogSettings.put(IExternalToolsUIConstants.DIALOGSTORE_LASTFOLDER, result);
		updateLaunchConfigurationDialog();
	}

	private void addJars(TableViewer viewer) {
		String lastUsedPath = fDialogSettings.get(IExternalToolsUIConstants.DIALOGSTORE_LASTEXTJAR);
		if (lastUsedPath == null) {
			lastUsedPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		}
		FileDialog dialog = new FileDialog(getShell(), SWT.MULTI);
		dialog.setFilterExtensions(new String[] { "*.jar" }); //$NON-NLS-1$;
		dialog.setFilterPath(lastUsedPath);

		String result = dialog.open();
		if (result == null) {
			return;
		}
		IPath filterPath = new Path(dialog.getFilterPath());
		String[] results = dialog.getFileNames();
		for (int i = 0; i < results.length; i++) {
			String jarName = results[i];
			try {
				IPath path = filterPath.append(jarName).makeAbsolute();
				URL url = new URL("file:" + path.toOSString()); //$NON-NLS-1$;
				((ExternalToolsContentProvider)viewer.getContentProvider()).add(url);
			} catch (MalformedURLException e) {
			}
		}

		viewer.setSelection(viewer.getSelection());
		fDialogSettings.put(IExternalToolsUIConstants.DIALOGSTORE_LASTEXTJAR, filterPath.toOSString());
		updateLaunchConfigurationDialog();
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();

		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		top.setLayout(layout);
		top.setLayoutData(new GridData(GridData.FILL_BOTH));

		setControl(top);

		createChangeClasspath(top);

		Label label = new Label(top, SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		label.setFont(font);
		label.setText(AntPreferencesMessages.getString("AntClasspathPage.Run&time_classpath__8")); //$NON-NLS-1$

		createAntTable(top);
		createButtonGroup(top);

		createSeparator(top);

		createAntHome(top);

		label = new Label(top, SWT.NONE);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		label.setFont(font);
		label.setText(AntPreferencesMessages.getString("AntClasspathPage.Additional_classpath_entries__11")); //$NON-NLS-1$

		createUserTable(top);
		createButtonGroup(top);
	}

	/**
	 * Creates a space between controls
	 */
	private Label createSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd.heightHint = 4;
		gd.horizontalSpan = 2;
		separator.setLayoutData(gd);
		return separator;
	}

	private void createAntHome(Composite top) {
		Composite antHomeComposite = new Composite(top, SWT.NONE);
		antHomeComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		antHomeComposite.setLayout(layout);
		antHomeComposite.setFont(top.getFont());

		antHomeButton = new Button(antHomeComposite, SWT.CHECK);
		antHomeButton.setFont(top.getFont());
		antHomeButton.setText(AntPreferencesMessages.getString("AntClasspathPage.Set_ANT_HO&ME_9")); //$NON-NLS-1$
		antHomeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				specifyAntHome();
			}
		});

		antHome = new Text(antHomeComposite, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		antHome.setLayoutData(gd);
		antHome.setFont(top.getFont());
		antHome.setEnabled(false);
		antHome.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (initializing) {
					return;
				}
				File rootDir= validateAntHome(antHome.getText());
				if (rootDir != null) {
					setAntHome(rootDir);
				}
			}
		});

		browseAntHomeButton = createPushButton(top, AntPreferencesMessages.getString("AntClasspathPage.&Browse..._10"), null); //$NON-NLS-1$
		browseAntHomeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				browseAntHome();
			}
		});
		browseAntHomeButton.setEnabled(false);
	}

	private void specifyAntHome() {
		antHome.setEnabled(!antHome.getEnabled());
		browseAntHomeButton.setEnabled(!browseAntHomeButton.getEnabled());
		if (antHome.isEnabled()) {
			File rootDir = validateAntHome(antHome.getText());
			if (rootDir != null) {
				setAntHome(rootDir);
			}
		} else {
			setMessage(null);
			setErrorMessage(null);
		}
		updateLaunchConfigurationDialog();
	}

	private void browseAntHome() {
		String lastUsedPath= fDialogSettings.get(IExternalToolsUIConstants.DIALOGSTORE_LASTANTHOME);
		if (lastUsedPath == null) {
			lastUsedPath= ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		}
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setMessage(AntPreferencesMessages.getString("AntClasspathPage.&Choose_a_folder_that_will_be_used_as_the_location_of_ANT_HOME_3")); //$NON-NLS-1$
		dialog.setFilterPath(lastUsedPath);
		String path = dialog.open();
		if (path == null) {
			return;
		}

		antHome.setText(path);
		fDialogSettings.put(IExternalToolsUIConstants.DIALOGSTORE_LASTANTHOME, path);
	}
		
	private void setAntHome(File rootDir) {
		AntClasspathContentProvider contentProvider = (AntClasspathContentProvider) antTableViewer.getContentProvider();
		contentProvider.removeAll();
		String[] names = rootDir.list();
		for (int i = 0; i < names.length; i++) {
			File file = new File(rootDir, names[i]);
			if (file.isFile() && file.getPath().endsWith(".jar")) { //$NON-NLS-1$
				try {
					IPath jarPath = new Path(file.getAbsolutePath());
					URL url = new URL("file:" + jarPath.toOSString()); //$NON-NLS-1$
					contentProvider.add(url);
				} catch (MalformedURLException e) {
				}
			}
		}
		AntCorePreferences prefs = AntCorePlugin.getPlugin().getPreferences();
		URL url = prefs.getToolsJarURL();
		if (url != null) {
			contentProvider.add(url);
		}
		updateLaunchConfigurationDialog();
	}
	private File validateAntHome(String path) {
		File rootDir = null;
		if (path.length() > 0) {
			rootDir = new File(path, "lib"); //$NON-NLS-1$
			if (!rootDir.exists()) {
				setErrorMessage(AntPreferencesMessages.getString("AntClasspathPage.Specified_ANT_HOME_does_not_contain_a___lib___directory_7")); //$NON-NLS-1$
				return null;
			}
		}

		setErrorMessage(null);

		return rootDir;
	}

	private void createChangeClasspath(Composite top) {
		Composite changeClasspath = new Composite(top, SWT.NONE);
		changeClasspath.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		changeClasspath.setLayout(layout);
		changeClasspath.setFont(top.getFont());

		useDefaultButton = new Button(changeClasspath, SWT.CHECK);
		useDefaultButton.setFont(top.getFont());
		useDefaultButton.setText(AntLaunchConfigurationMessages.getString("AntClasspathTab.Use_&global_classpath_as_specified_in_the_Ant_preferences_4")); //$NON-NLS-1$
		useDefaultButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				toggleUseDefaultClasspath();
				updateLaunchConfigurationDialog();
			}

		});
		
		Composite reuse = new Composite(changeClasspath, SWT.NONE);
		reuse.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 20;
		reuse.setLayout(layout);
		reuse.setFont(top.getFont());
		reuseClassLoader = new Button(reuse, SWT.CHECK);
		reuseClassLoader.setFont(reuse.getFont());
		reuseClassLoader.setText(AntLaunchConfigurationMessages.getString("AntClasspathTab.Reuse_the_Ant_classloader_for_each_&build_5")); //$NON-NLS-1$
		reuseClassLoader.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
	}

	private void toggleUseDefaultClasspath() {
		boolean enable = !useDefaultButton.getSelection();
		
		reuseClassLoader.setEnabled(!enable);
		
		antHomeButton.setEnabled(enable);
		
		addFolderButtton.setEnabled(enable);
		addJarButton.setEnabled(enable);
		addUserJarButton.setEnabled(enable);
		addUserFolderButtton.setEnabled(enable);
		if (enable) {
			antTableViewer.setSelection(antTableViewer.getSelection());
			userTableViewer.setSelection(userTableViewer.getSelection());
		} else {
			antHomeButton.setSelection(false);
			antHome.setEnabled(false);
			browseAntHomeButton.setEnabled(false);
			downButton.setEnabled(false);
			downUserButton.setEnabled(false);
			removeButton.setEnabled(false);
			removeUserButton.setEnabled(false);
			upButton.setEnabled(false);
			upUserButton.setEnabled(false);
			AntCorePreferences prefs = AntCorePlugin.getPlugin().getPreferences();
			/*URL[] extensionURLs= prefs.getDefaultURLs();
			List allURLs= new ArrayList();
			allURLs.addAll(Arrays.asList(prefs.getAntURLs()));
			allURLs.addAll(Arrays.asList(extensionURLs));*/
			antTableViewer.setInput(prefs.getAntURLs());
			userTableViewer.setInput(prefs.getCustomURLs());
		}

	}

	private void createAntTable(Composite parent) {
		Table table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.horizontalSpan = 1;
		table.setLayoutData(data);
		table.setFont(parent.getFont());

		antContentProvider = new AntClasspathContentProvider();
		antTableViewer = new TableViewer(table);
		antTableViewer.setContentProvider(antContentProvider);
		antTableViewer.setLabelProvider(labelProvider);
		antTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (!useDefaultButton.getSelection()) {
					tableSelectionChanged((IStructuredSelection) event.getSelection());
				}
			}
		});
	}

	/* (non-Javadoc)
		 * Method declared on AntPage.
		 */
	protected void tableSelectionChanged(IStructuredSelection selection) {
		ExternalToolsContentProvider contentProvider = (ExternalToolsContentProvider) antTableViewer.getContentProvider();
		Object[] elements = contentProvider.getElements(null);
		List urls = Arrays.asList(elements);
		boolean notEmpty = !selection.isEmpty();
		Iterator selected = selection.iterator();
		boolean first = false;
		boolean last = false;
		int lastUrl = urls.size() - 1;
		while (selected.hasNext()) {
			Object element = (Object) selected.next();
			if (!first && urls.indexOf(element) == 0) {
				first = true;
			}
			if (!last && urls.indexOf(element) == lastUrl) {
				last = true;
			}
		}

		removeButton.setEnabled(notEmpty);
		upButton.setEnabled(notEmpty && !first);
		downButton.setEnabled(notEmpty && !last);
	}

	/**
	 * Method createUserTable.
	 * @param top
	 */
	private void createUserTable(Composite top) {
		Table table = new Table(top, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.horizontalSpan = 1;
		table.setLayoutData(data);
		table.setFont(top.getFont());
		userContentProvider = new AntClasspathContentProvider();
		userTableViewer = new TableViewer(table);
		userTableViewer.setContentProvider(userContentProvider);
		userTableViewer.setLabelProvider(labelProvider);
		userTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (!useDefaultButton.getSelection()) {
					userTableSelectionChanged((IStructuredSelection) event.getSelection());
				}
			}
		});
	}

	private void userTableSelectionChanged(IStructuredSelection selection) {
		ExternalToolsContentProvider contentProvider = (ExternalToolsContentProvider) userTableViewer.getContentProvider();
		Object[] elements = contentProvider.getElements(null);
		List urls = Arrays.asList(elements);

		boolean notEmpty = !selection.isEmpty();
		Iterator selected = selection.iterator();
		boolean first = false;
		boolean last = false;
		int lastFile = urls.size() - 1;
		while (selected.hasNext()) {
			Object element = (Object) selected.next();
			if (!first && urls.indexOf(element) == 0) {
				first = true;
			}
			if (!last && urls.indexOf(element) == lastFile) {
				last = true;
			}
		}

		removeUserButton.setEnabled(notEmpty);
		upUserButton.setEnabled(notEmpty && !first);
		downUserButton.setEnabled(notEmpty && !last);
	}
	
	/**
	* Returns the specified user classpath URLs
	*
	* @return List
	*/
	protected List getUserURLs() {
		Object[] elements = userContentProvider.getElements(null);
		return Arrays.asList(elements);
	}
	
	/**
		 * Returns the currently listed objects in the table.  Returns null
		 * if this widget has not yet been created or has been disposed.
		 */
		protected List getAntURLs() {
			Object[] elements = antContentProvider.getElements(null);
			return Arrays.asList(elements);
		}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IExternalToolConstants.ATTR_ANT_REUSE_CLASSLOADER, true);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		initializing= true;
		String urlStrings= null;
		try {
			urlStrings = configuration.getAttribute(IExternalToolConstants.ATTR_ANT_CUSTOM_CLASSPATH, (String) null);
		} catch (CoreException e) {
		}
		if (urlStrings == null) {
			useDefaultButton.setSelection(true);
			AntCorePreferences prefs = AntCorePlugin.getPlugin().getPreferences();
			/*URL[] extensionURLs= prefs.getDefaultURLs();
			List allURLs= new ArrayList();
			allURLs.addAll(Arrays.asList(prefs.getAntURLs()));
			allURLs.addAll(Arrays.asList(extensionURLs));*/
			antTableViewer.setInput(prefs.getAntURLs());
			userTableViewer.setInput(prefs.getCustomURLs());
		} else {
			String antHomeString= null;
			try {
				antHomeString= configuration.getAttribute(IExternalToolConstants.ATTR_ANT_HOME, (String)null);
			} catch (CoreException e) {
			}
			antHomeButton.setSelection(antHomeString != null);
			if (antHomeString != null) {
				antHome.setEnabled(true);
				browseAntHomeButton.setEnabled(true);
				antHome.setText(antHomeString);
			}
			useDefaultButton.setSelection(false);
			reuseClassLoader.setEnabled(false);
			List userURLs= new ArrayList();
			List antURLs= new ArrayList();
			AntUtil.getCustomClasspaths(configuration, antURLs, userURLs);
			userTableViewer.setInput(userURLs);
			antTableViewer.setInput(antURLs);
			
		}
		try {
			reuseClassLoader.setSelection(configuration.getAttribute(IExternalToolConstants.ATTR_ANT_REUSE_CLASSLOADER, true));
		} catch (CoreException ce) {
			reuseClassLoader.setSelection(true);
		}
		toggleUseDefaultClasspath();
		initializing= false;
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (useDefaultButton.getSelection()) {
			configuration.setAttribute(IExternalToolConstants.ATTR_ANT_CUSTOM_CLASSPATH, (String)null);
			configuration.setAttribute(IExternalToolConstants.ATTR_ANT_REUSE_CLASSLOADER, reuseClassLoader.getSelection());
			return;
		}
		List antUrls= getAntURLs();
		List userUrls= getUserURLs();
		StringBuffer urlString= new StringBuffer();
		Iterator antUrlsItr= antUrls.iterator();
		while (antUrlsItr.hasNext()) {
			URL url = (URL) antUrlsItr.next();
			urlString.append(url.getFile());
			urlString.append(',');
		}
		if (userUrls.size() > 0) {
			urlString.append('*');
		}
		Iterator userUrlsItr= userUrls.iterator();
		while (userUrlsItr.hasNext()) {
			URL url = (URL) userUrlsItr.next();
			urlString.append(url.getFile());
			urlString.append(',');
		}
		configuration.setAttribute(IExternalToolConstants.ATTR_ANT_CUSTOM_CLASSPATH, urlString.substring(0, urlString.length() - 1));
		String antHomeText= antHome.getText().trim();
		if (!antHomeButton.getSelection() || antHomeText.length() == 0) {
			antHomeText= null;
		}
		configuration.setAttribute(IExternalToolConstants.ATTR_ANT_HOME, antHomeText);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return AntLaunchConfigurationMessages.getString("AntClasspathTab.Classpath_6"); //$NON-NLS-1$
	}
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return labelProvider.getClasspathImage();
	}
}
