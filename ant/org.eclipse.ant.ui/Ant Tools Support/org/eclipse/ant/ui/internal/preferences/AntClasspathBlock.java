/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.ui.internal.preferences;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.AntCorePreferences;
import org.eclipse.ant.ui.internal.model.AntUIPlugin;
import org.eclipse.ant.ui.internal.model.IAntUIConstants;
import org.eclipse.ant.ui.internal.model.IAntUIPreferenceConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.variables.ILaunchVariableManager;
import org.eclipse.debug.core.variables.LaunchVariableUtil;
import org.eclipse.jdt.internal.debug.ui.actions.ArchiveFilter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
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
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.externaltools.internal.ui.ExternalToolsContentProvider;
import org.eclipse.ui.externaltools.internal.ui.MessageDialogWithToggle;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;

public class AntClasspathBlock {

	private static final String[] XERCES= new String[] {"xercesImpl.jar", "xml-apis.jar", "xmlParserAPIs.jar"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] TOOLS= new String[] {"tools.jar"}; //$NON-NLS-1$

	private TableViewer antTableViewer;
	private ExternalToolsContentProvider antContentProvider;
	private TableViewer userTableViewer;
	private ExternalToolsContentProvider userContentProvider;

	private Button upButton;
	private Button downButton;
	private Button removeButton;

	private Button upUserButton;
	private Button downUserButton;
	private Button removeUserButton;

	private final AntClasspathLabelProvider labelProvider = new AntClasspathLabelProvider();
	private Button addUserExternalJarButton;
	private Button addUserJarButton;
	private Button addUserFolderButton;
	private Button addFolderButton;
	private Button addJARButton;
	private Button addExternalJARButton;
	
	private boolean showExternalJARButton= false;
	
	private Button antHomeButton;
	private Text antHome;
	private Button browseAntHomeButton;

	private final IDialogSettings dialogSettings = AntUIPlugin.getDefault().getDialogSettings();
	
	private boolean initializing = true;
	
	private IAntBlockContainer container;
	
	private boolean tablesEnabled= true;
	
	private int validated= 3;
	
	public AntClasspathBlock() {
		super();
	}
	
	public AntClasspathBlock(boolean showExternalJARButton) {
		super();
		this.showExternalJARButton= showExternalJARButton; 
	}

	public void setContainer(IAntBlockContainer container) {
		this.container= container; 
	}
	
	private void addButtonsToButtonGroup(Composite parent) {
		if (addExternalJARButton == null) {
			if (showExternalJARButton) {
				addJARButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.addJarButtonTitle")); //$NON-NLS-1$;
				addJARButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent evt) {
						addJars(antTableViewer);
					}
				});
			}
		
			String label;
			if (showExternalJARButton) {
				label= "Add E&xternal JARs...";
			} else {
				label= AntPreferencesMessages.getString("AntClasspathBlock.addJarButtonTitle");	 //$NON-NLS-1$
			}
			addExternalJARButton = container.createPushButton(parent, label);
			addExternalJARButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					addExternalJars(antTableViewer);
				
				}
			});
			addFolderButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.addFolderButtonTitle")); //$NON-NLS-1$;
			addFolderButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					addFolder(antTableViewer, AntPreferencesMessages.getString("AntClasspathBlock.&Choose_a_folder_to_add_to_the_classpath__1")); //$NON-NLS-1$
				}
			});

			upButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.upButtonTitle")); //$NON-NLS-1$;
			upButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					handleMove(-1, antTableViewer);
				}
			});
			downButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.downButtonTitle")); //$NON-NLS-1$;
			downButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					handleMove(1, antTableViewer);
				}
			});
			removeButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.removeButtonTitle")); //$NON-NLS-1$;
			removeButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					remove(antTableViewer);
				}
			});

		} else {
			if (showExternalJARButton) {
				addUserJarButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.addJarButtonTitle2")); //$NON-NLS-1$;
				addUserJarButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent evt) {
						addJars(userTableViewer);
					}
				});
			} 
			String label;
			if (showExternalJARButton) {
				label= "Add Externa&l JARs...";
			} else {
				label= AntPreferencesMessages.getString("AntClasspathBlock.addJarButtonTitle2");	 //$NON-NLS-1$
			}
			addUserExternalJarButton = container.createPushButton(parent, label);
			addUserExternalJarButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					addExternalJars(userTableViewer);
				}
			});
			
			addUserFolderButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.addFolderButtonTitle2")); //$NON-NLS-1$;
			addUserFolderButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					addFolder(userTableViewer, AntPreferencesMessages.getString("AntClasspathBlock.&Choose_a_folder_to_add_to_the_classpath__1")); //$NON-NLS-1$
				}
			});
			upUserButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.upButtonTitle2")); //$NON-NLS-1$;
			upUserButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					handleMove(-1, userTableViewer);
				}
			});
			downUserButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.downButtonTitle2")); //$NON-NLS-1$;
			downUserButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					handleMove(1, userTableViewer);
				}
			});
			removeUserButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.removeButtonTitle2")); //$NON-NLS-1$;
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
		updateContainer();
	}

	private void remove(TableViewer viewer) {
		ExternalToolsContentProvider viewerContentProvider = (ExternalToolsContentProvider) viewer.getContentProvider();
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		Iterator enum = sel.iterator();
		while (enum.hasNext()) {
			viewerContentProvider.remove(enum.next());
		}
		updateContainer();
	}

	/**
	 * Allows the user to enter a folder as a classpath.
	 */
	private void addFolder(TableViewer viewer, String message) {
		String lastUsedPath = dialogSettings.get(IAntUIConstants.DIALOGSTORE_LASTFOLDER);
		if (lastUsedPath == null) {
			lastUsedPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		}
		DirectoryDialog dialog = new DirectoryDialog(antTableViewer.getControl().getShell());
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
		dialogSettings.put(IAntUIConstants.DIALOGSTORE_LASTFOLDER, result);
		updateContainer();
	}

	private void addExternalJars(TableViewer viewer) {
		String lastUsedPath = dialogSettings.get(IAntUIConstants.DIALOGSTORE_LASTEXTJAR);
		if (lastUsedPath == null) {
			lastUsedPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		}
		FileDialog dialog = new FileDialog(antTableViewer.getControl().getShell(), SWT.MULTI);
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
		dialogSettings.put(IAntUIConstants.DIALOGSTORE_LASTEXTJAR, filterPath.toOSString());
		updateContainer();
	}
	
	private void addJars(TableViewer viewer) {
		List allURLs= new ArrayList();
		allURLs.addAll(getAntURLs());
		allURLs.addAll(getUserURLs());
		ViewerFilter filter= new ArchiveFilter(allURLs);
		
		ILabelProvider lp= new WorkbenchLabelProvider();
		ITreeContentProvider cp= new WorkbenchContentProvider();

		ElementTreeSelectionDialog dialog= new ElementTreeSelectionDialog(viewer.getControl().getShell(), lp, cp);
		dialog.setTitle("JAR Selection"); 
		dialog.setMessage("&Choose JARs and ZIPs to add:");
		dialog.addFilter(filter);
		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());	
		dialog.setSorter(new ResourceSorter(ResourceSorter.NAME));

		if (dialog.open() == Window.OK) {
			Object[] elements= dialog.getResult();
			for (int i = 0; i < elements.length; i++) {
				IFile file = (IFile)elements[i];
				String varExpression= LaunchVariableUtil.newVariableExpression(ILaunchVariableManager.VAR_WORKSPACE_LOC, file.getFullPath().toString());
				((ExternalToolsContentProvider)viewer.getContentProvider()).add(varExpression);
			}
			updateContainer();
		}
	}
		
	private void updateContainer() {
		validated= 0;
		container.update();
	}

	/**
	 * Creates the group which will contain the buttons.
	 */
	private void createButtonGroup(Composite top) {
		Composite buttonGroup = new Composite(top, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonGroup.setLayout(layout);
		buttonGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		buttonGroup.setFont(top.getFont());

		addButtonsToButtonGroup(buttonGroup);
	}
	
	private void createAntTable(Composite parent) {
		Table table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.heightHint = table.getItemHeight();
		data.horizontalSpan = 1;
		table.setLayoutData(data);
		table.setFont(parent.getFont());

		antContentProvider = new AntClasspathContentProvider();
		antTableViewer = new TableViewer(table);
		antTableViewer.setContentProvider(antContentProvider);
		antTableViewer.setLabelProvider(labelProvider);
		antTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (tablesEnabled) {
					tableSelectionChanged((IStructuredSelection) event.getSelection(),
						(ExternalToolsContentProvider) antTableViewer.getContentProvider(), false);
				}
			}
		});
	}
			
	public void createContents(Composite parent) {
		Font font = parent.getFont();
		Label label = new Label(parent, SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		label.setFont(font);
		label.setText(AntPreferencesMessages.getString("AntClasspathBlock.Run&time_classpath__8")); //$NON-NLS-1$

		createAntTable(parent);
		createButtonGroup(parent);

		createSeparator(parent);

		createAntHome(parent);

		label = new Label(parent, SWT.NONE);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		label.setFont(font);
		label.setText(AntPreferencesMessages.getString("AntClasspathBlock.Additional_classpath_entries__11")); //$NON-NLS-1$

		createUserTable(parent);
		createButtonGroup(parent);
	}
	
	private void createUserTable(Composite top) {
		Table table = new Table(top, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.heightHint = table.getItemHeight();
		data.horizontalSpan = 1;
		table.setLayoutData(data);
		table.setFont(top.getFont());
		userContentProvider = new AntClasspathContentProvider();
		userTableViewer = new TableViewer(table);
		userTableViewer.setContentProvider(userContentProvider);
		userTableViewer.setLabelProvider(labelProvider);
		userTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (tablesEnabled) {
					tableSelectionChanged((IStructuredSelection) event.getSelection(),
						(ExternalToolsContentProvider) userTableViewer.getContentProvider(), true);
				}
			}
		});
	}

	/**
	 * Creates a space between controls
	 */
	private Label createSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.NONE);
		GridData gd =
			new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd.heightHint = 4;
		gd.horizontalSpan = 2;
		separator.setLayoutData(gd);
		return separator;
	}

	private void createAntHome(Composite top) {
		Composite antHomeComposite = new Composite(top, SWT.NONE);
		antHomeComposite.setLayoutData(
			new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		antHomeComposite.setLayout(layout);
		antHomeComposite.setFont(top.getFont());

		antHomeButton = new Button(antHomeComposite, SWT.CHECK);
		antHomeButton.setFont(top.getFont());
		antHomeButton.setText(AntPreferencesMessages.getString("AntClasspathBlock.Set_ANT_HO&ME_9")); //$NON-NLS-1$
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
				String path= antHome.getText();
				if (path.length() > 0) {
					File rootDir = new File(path, "lib"); //$NON-NLS-1$
					if (rootDir.exists()) {
						setAntHome(rootDir);
					} else {
						updateContainer();
					}
				} else {
					updateContainer();
				}
			}
		});

		browseAntHomeButton = container.createPushButton(top, AntPreferencesMessages.getString("AntClasspathBlock.&Browse..._10")); //$NON-NLS-1$
		browseAntHomeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				browseAntHome();
			}
		});
		browseAntHomeButton.setEnabled(false);
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	private void tableSelectionChanged(IStructuredSelection selection, ExternalToolsContentProvider contentProvider, boolean user) {
		Object[] elements = contentProvider.getElements(null);
		List urls = Arrays.asList(elements);
		boolean notEmpty = !selection.isEmpty();
		Iterator selected = selection.iterator();
		boolean first = false;
		boolean last = false;
		int lastUrl = urls.size() - 1;
		while (selected.hasNext()) {
			Object element = selected.next();
			if (!first && urls.indexOf(element) == 0) {
				first = true;
			}
			if (!last && urls.indexOf(element) == lastUrl) {
				last = true;
			}
		}

		if (user) {
			removeUserButton.setEnabled(notEmpty);
			upUserButton.setEnabled(notEmpty && !first);
			downUserButton.setEnabled(notEmpty && !last);
		} else {
			removeButton.setEnabled(notEmpty);
			upButton.setEnabled(notEmpty && !first);
			downButton.setEnabled(notEmpty && !last);
		}
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
			container.setMessage(null);
			container.setErrorMessage(null);
		}
		updateContainer();
	}
	
	private File validateAntHome(String path) {
		File rootDir = null;
		if (path.length() > 0) {
			rootDir = new File(path, "lib"); //$NON-NLS-1$
			if (!rootDir.exists()) {
				container.setErrorMessage(AntPreferencesMessages.getString("AntClasspathBlock.Specified_ANT_HOME_does_not_contain_a___lib___directory_7")); //$NON-NLS-1$
				validated= 3;
				return null;
			}
		} else {
			validated= 3;
			container.setErrorMessage(AntPreferencesMessages.getString("AntClasspathBlock.Specified_ANT_HOME_does_not_contain_a___lib___directory_7")); //$NON-NLS-1$
			return null;
		}

		container.setErrorMessage(null);
		return rootDir;
	}
	
	private void browseAntHome() {
		String lastUsedPath= dialogSettings.get(IAntUIConstants.DIALOGSTORE_LASTANTHOME);
		if (lastUsedPath == null) {
			lastUsedPath= ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		}
		DirectoryDialog dialog = new DirectoryDialog(antTableViewer.getControl().getShell());
		dialog.setMessage(AntPreferencesMessages.getString("AntClasspathBlock.&Choose_a_folder_that_will_be_used_as_the_location_of_ANT_HOME_3")); //$NON-NLS-1$
		dialog.setFilterPath(lastUsedPath);
		String path = dialog.open();
		if (path == null) {
			return;
		}
	
		antHome.setText(path);
		dialogSettings.put(IAntUIConstants.DIALOGSTORE_LASTANTHOME, path);
		container.update();
	}
		
	private void setAntHome(File rootDir) {
		AntClasspathContentProvider contentProvider = (AntClasspathContentProvider) antTableViewer.getContentProvider();
		contentProvider.removeAll();
		String[] names = rootDir.list();
		for (int i = 0; i < names.length; i++) {
			File file = new File(rootDir, names[i]);
			if (file.isFile() && file.getPath().endsWith(".jar")) { //$NON-NLS-1$
				try {
					String name= file.getAbsolutePath();
					IPath jarPath = new Path(name);
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
		updateContainer();
	}
	
	public List getAntURLs() { 
		Object[] elements = antContentProvider.getElements(null);
		return Arrays.asList(elements);
	}
	
	public List getUserURLs() { 
		Object[] elements = userContentProvider.getElements(null);
		return Arrays.asList(elements);
	}
	
	public String getAntHome() {
		String antHomeText= antHome.getText().trim();
		if (!antHomeButton.getSelection() || antHomeText.length() == 0) {
			antHomeText= null;
		}
		return antHomeText;
	}
	
	public void setEnabled(boolean enable) {
		validated= 0;
		setTablesEnabled(enable);
		antHomeButton.setEnabled(enable);
		addFolderButton.setEnabled(enable);
		if (addJARButton != null) {
			addJARButton.setEnabled(enable);
		}
		addExternalJARButton.setEnabled(enable);
		if (addUserJarButton != null) {
			addUserJarButton.setEnabled(enable);
		}
		addUserExternalJarButton.setEnabled(enable);
		addUserFolderButton.setEnabled(enable);
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
			antTableViewer.setInput(prefs.getAntURLs());
			userTableViewer.setInput(prefs.getCustomURLs());
		}
	}
	
	public void initializeAntHome(String antHomeString) {
		antHomeButton.setSelection(antHomeString != null);
		antHome.setEnabled(antHomeString != null);
		browseAntHomeButton.setEnabled(antHomeString != null);
		if (antHomeString != null) {
			antHome.setText(antHomeString);
		} else {
			antHome.setText(""); //$NON-NLS-1$
		}
		initializing= false;
	}
	
	public void setUserTableInput(Object input) {
		userTableViewer.setInput(input);
	}
	
	public void setAntTableInput(Object input) {
		antTableViewer.setInput(input);
	}
	
	public boolean isAntHomeEnabled() {
		return antHome.isEnabled();
	}
	
	public boolean validateAntHome() {
		validated++;
		return validateAntHome(antHome.getText()) != null;
	}
	
	public Image getClasspathImage() {
		return labelProvider.getClasspathImage();
	}
	
	public void setTablesEnabled(boolean tablesEnabled) {
		this.tablesEnabled= tablesEnabled;
	}
	
	public boolean validateToolsJAR() {
		validated++;
		boolean check= AntUIPlugin.getDefault().getPreferenceStore().getBoolean(IAntUIPreferenceConstants.ANT_TOOLS_JAR_WARNING);
		if (check && !AntUIPlugin.isMacOS()) {
			List antURLs= getAntURLs();
			boolean valid= JARPresent(antURLs, TOOLS) != null;
			if (!valid) {
				List userURLs= getUserURLs();
				if (JARPresent(userURLs, TOOLS) == null) {
					valid= MessageDialogWithToggle.openQuestion(AntUIPlugin.getActiveWorkbenchWindow().getShell(), AntPreferencesMessages.getString("AntClasspathBlock.31"), AntPreferencesMessages.getString("AntClasspathBlock.32"), IAntUIPreferenceConstants.ANT_TOOLS_JAR_WARNING, AntPreferencesMessages.getString("AntClasspathBlock.33"), AntUIPlugin.getDefault().getPreferenceStore()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} else {
					valid= true;
				}
			}
			if (!valid) {
				container.setErrorMessage(AntPreferencesMessages.getString("AntClasspathBlock.34")); //$NON-NLS-1$
				validated= 3;
			}
			return valid;
			}
		return true;
	}

	public boolean validateXerces() {
		boolean valid= true;
		validated++;
		boolean check= AntUIPlugin.getDefault().getPreferenceStore().getBoolean(IAntUIPreferenceConstants.ANT_XERCES_JARS_WARNING);
		if (check) {
			List antURLs= getAntURLs();
			String suffix= JARPresent(antURLs, XERCES);
			if (suffix == null) {
				List userURLs= getUserURLs();
				suffix= JARPresent(userURLs, XERCES);
			}
			if (suffix != null) {
				valid= MessageDialogWithToggle.openQuestion(antTableViewer.getControl().getShell(), AntPreferencesMessages.getString("AntClasspathBlock.35"), MessageFormat.format(AntPreferencesMessages.getString("AntClasspathBlock.36"), new String[]{suffix}), IAntUIPreferenceConstants.ANT_XERCES_JARS_WARNING, AntPreferencesMessages.getString("AntClasspathBlock.37"), AntUIPlugin.getDefault().getPreferenceStore()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else {
				valid= true;
			}
			if (!valid) {
				container.setErrorMessage(MessageFormat.format(AntPreferencesMessages.getString("AntClasspathBlock.38"), new String[]{suffix})); //$NON-NLS-1$
			}
		}
		return valid;
	}
	
	private String JARPresent(List URLs, String[] suffixes) {
		
		for (Iterator iter = URLs.iterator(); iter.hasNext();) {
			String file;
			Object entry = iter.next();
			if (entry instanceof URL) {
				file= ((URL)entry).getFile();
			} else {
				file= entry.toString();
			}
			for (int i = 0; i < suffixes.length; i++) {
				String suffix = suffixes[i];
				if (file.endsWith(suffix)) {
					return suffix;
				}
			}
		}
		return null;
	}
	
	public boolean isValidated() {
		return validated >= 3;
	}
	
	public void setValidated() {
		validated= 3;
	}
}
