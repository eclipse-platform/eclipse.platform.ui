package org.eclipse.ui.externaltools.internal.ant.preferences;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.ui.ExternalToolsContentProvider;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;

/**
 * Sub-page that allows the user to enter custom classpaths
 * to be used when running Ant build files.
 */
public class AntClasspathPage extends AntPage {
	private static final int ADD_JARS_BUTTON = IDialogConstants.CLIENT_ID + 1;
	private static final int ADD_FOLDER_BUTTON = IDialogConstants.CLIENT_ID + 2;
	private static final int REMOVE_BUTTON = IDialogConstants.CLIENT_ID + 3;
	private static final int UP_BUTTON = IDialogConstants.CLIENT_ID + 4;
	private static final int DOWN_BUTTON = IDialogConstants.CLIENT_ID + 5;
	
	private static final int ADD_USER_JARS_BUTTON = IDialogConstants.CLIENT_ID + 6;
	private static final int ADD_USER_FOLDER_BUTTON = IDialogConstants.CLIENT_ID + 7;
	private static final int REMOVE_USER_BUTTON = IDialogConstants.CLIENT_ID + 8;
	private static final int UP_USER_BUTTON = IDialogConstants.CLIENT_ID + 9;
	private static final int DOWN_USER_BUTTON = IDialogConstants.CLIENT_ID + 10;
	
	private static final int BROWSE_ANT_HOME = IDialogConstants.CLIENT_ID + 11;
	
	private Button upButton;
	private Button downButton;
	
	private Button upUserButton;
	private Button downUserButton;
	private Button removeUserButton;
	
	private Button antHomeButton;
	
	private Text antHome;
	private Button browseAntHomeButton;
	
	private TableViewer userTableViewer;
	private ExternalToolsContentProvider userContentProvider;

	private IDialogSettings fDialogSettings;
	private final AntClasspathLabelProvider labelProvider = new AntClasspathLabelProvider();

	/**
	 * Creates an instance.
	 */
	public AntClasspathPage(AntRuntimePreferencePage preferencePage) {
		super(preferencePage);
		fDialogSettings= ExternalToolsPlugin.getDefault().getDialogSettings();
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected void addButtonsToButtonGroup(Composite parent) {
		if (upButton == null) {
			createButton(parent, "AntClasspathPage.addJarButtonTitle", ADD_JARS_BUTTON); //$NON-NLS-1$;
			createButton(parent, "AntClasspathPage.addFolderButtonTitle", ADD_FOLDER_BUTTON); //$NON-NLS-1$;
			upButton= createButton(parent, "AntClasspathPage.upButtonTitle", UP_BUTTON); //$NON-NLS-1$;
			downButton= createButton(parent, "AntClasspathPage.downButtonTitle", DOWN_BUTTON); //$NON-NLS-1$;
			removeButton= createButton(parent, "AntClasspathPage.removeButtonTitle", REMOVE_BUTTON); //$NON-NLS-1$;
		} else {
			createButton(parent, "AntClasspathPage.addJarButtonTitle2", ADD_USER_JARS_BUTTON); //$NON-NLS-1$;
			createButton(parent, "AntClasspathPage.addFolderButtonTitle2", ADD_USER_FOLDER_BUTTON); //$NON-NLS-1$;
			upUserButton= createButton(parent, "AntClasspathPage.upButtonTitle2", UP_USER_BUTTON); //$NON-NLS-1$;
			downUserButton= createButton(parent, "AntClasspathPage.downButtonTitle2", DOWN_USER_BUTTON); //$NON-NLS-1$;
			removeUserButton= createButton(parent, "AntClasspathPage.removeButtonTitle2", REMOVE_USER_BUTTON); //$NON-NLS-1$;
		}
	}
	
	/**
	 * Allows the user to enter a folder as a classpath.
	 */
	private void addFolder(TableViewer viewer, String message) {
		String lastUsedPath= fDialogSettings.get(IExternalToolsUIConstants.DIALOGSTORE_LASTFOLDER);
		if (lastUsedPath == null) {
			lastUsedPath= ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
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
	}
	
	private void addJars(TableViewer viewer) {
		String lastUsedPath= fDialogSettings.get(IExternalToolsUIConstants.DIALOGSTORE_LASTEXTJAR);
		if (lastUsedPath == null) {
			lastUsedPath= ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		}
		FileDialog dialog = new FileDialog(getShell(), SWT.MULTI);
		dialog.setFilterExtensions(new String[] { "*.jar" }); //$NON-NLS-1$;
		dialog.setFilterPath(lastUsedPath);

		String result = dialog.open();
		if (result == null) {
			return;
		}
		IPath filterPath= new Path(dialog.getFilterPath());
		String[] results= dialog.getFileNames();
		for (int i = 0; i < results.length; i++) {
			String jarName = results[i];
			try {
				IPath path= filterPath.append(jarName).makeAbsolute();	
				URL url = new URL("file:" + path.toOSString()); //$NON-NLS-1$;
				((ExternalToolsContentProvider)viewer.getContentProvider()).add(url);
			} catch (MalformedURLException e) {
			}
		}
		viewer.setSelection(viewer.getSelection());
		fDialogSettings.put(IExternalToolsUIConstants.DIALOGSTORE_LASTEXTJAR, filterPath.toOSString());
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
			case ADD_JARS_BUTTON :
				addJars(getTableViewer());
				break;
			case ADD_FOLDER_BUTTON :
				addFolder(getTableViewer(), AntPreferencesMessages.getString("AntClasspathPage.&Choose_a_folder_to_add_to_the_classpath__1")); //$NON-NLS-1$
				break;
			case UP_BUTTON :
				handleMove(-1, getTableViewer());
				break;
			case DOWN_BUTTON :
				handleMove(1, getTableViewer());
				break;
			case REMOVE_BUTTON :
				remove();
				break;
			case ADD_USER_JARS_BUTTON :
				addJars(userTableViewer);
				break;
			case ADD_USER_FOLDER_BUTTON :
				addFolder(userTableViewer, AntPreferencesMessages.getString("AntClasspathPage.&Choose_a_folder_to_add_to_the_classpath__1")); //$NON-NLS-1$
				break;
			case UP_USER_BUTTON :
				handleMove(-1, userTableViewer);
				break;
			case DOWN_USER_BUTTON :
				handleMove(1, userTableViewer);
				break;
			case REMOVE_USER_BUTTON :
				remove(userTableViewer);
				break;
			case BROWSE_ANT_HOME :
				browseAntHome();
				break;
		}
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
		AntClasspathContentProvider contentProvider= (AntClasspathContentProvider)getTableViewer().getContentProvider();
		contentProvider.removeAll();
		String[] names = rootDir.list();
		for (int i = 0; i < names.length; i++) {
			File file = new File(rootDir, names[i]);		
			if (file.isFile() && file.getPath().endsWith(".jar")) { //$NON-NLS-1$
				try {
					IPath jarPath= new Path(file.getAbsolutePath());	
					URL url = new URL("file:" + jarPath.toOSString()); //$NON-NLS-1$
					contentProvider.add(url);
				} catch (MalformedURLException e) {
				}
			}
		}
		AntCorePreferences prefs= AntCorePlugin.getPlugin().getPreferences();
		URL url = prefs.getToolsJarURL();
		if (url != null) {
			contentProvider.add(url);
		}
	}
	
	private File validateAntHome(String path) {
		File rootDir= null;
		if (path.length() > 0) {
			rootDir = new File(path, "lib"); //$NON-NLS-1$
			if (!rootDir.exists()) {
				getPreferencePage().setErrorMessage(AntPreferencesMessages.getString("AntClasspathPage.Specified_ANT_HOME_does_not_contain_a___lib___directory_7")); //$NON-NLS-1$
				getPreferencePage().setValid(false);
				return null;
			}
		}
		
		getPreferencePage().setErrorMessage(null);
		getPreferencePage().setValid(true);
		
		return rootDir;
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
	 * Sets the contents of the tables on this page.
	 */
	protected void initialize() {
		AntCorePreferences prefs= AntCorePlugin.getPlugin().getPreferences();
		/*URL[] extensionURLs= prefs.getDefaultURLs();
		List allURLs= new ArrayList();
		allURLs.addAll(Arrays.asList(prefs.getAntURLs()));
		allURLs.addAll(Arrays.asList(extensionURLs));*/
		getTableViewer().setInput(prefs.getAntURLs());
		userTableViewer.setInput(Arrays.asList(prefs.getCustomURLs()));
		String antHomePath= prefs.getAntHome();
		boolean enabled= antHomePath.length() > 0;
		antHome.setEnabled(enabled);
		browseAntHomeButton.setEnabled(enabled);
		antHomeButton.setSelection(enabled);
		antHome.setText(antHomePath);
		antHome.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				File rootDir= validateAntHome(antHome.getText());
				if (rootDir != null) {
					setAntHome(rootDir);
				}
			}
		});
		tableSelectionChanged((IStructuredSelection) getTableViewer().getSelection());
		userTableSelectionChanged((IStructuredSelection)userTableViewer.getSelection());
		getPreferencePage().setErrorMessage(null);
		getPreferencePage().setValid(true);
	}
	
	protected void performDefaults() {
		AntCorePreferences prefs= AntCorePlugin.getPlugin().getPreferences();
		getTableViewer().setInput(Arrays.asList(prefs.getDefaultAntURLs()));
		userTableViewer.setInput(new ArrayList(0));
		antHome.setEnabled(false);
		browseAntHomeButton.setEnabled(false);
		antHomeButton.setSelection(false);
		antHome.setText(""); //$NON-NLS-1$
		tableSelectionChanged((IStructuredSelection) getTableViewer().getSelection());
		userTableSelectionChanged((IStructuredSelection)userTableViewer.getSelection());
	}
	
	/**
	 * Creates the tab item that contains this sub-page.
	 */
	protected TabItem createTabItem(TabFolder folder) {
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(AntPreferencesMessages.getString("AntClasspathPage.title")); //$NON-NLS-1$;
		item.setImage(labelProvider.getClasspathImage());
		item.setData(this);
		item.setControl(createContents(folder));
		return item;
	}
	
	/**
	 * Creates this page's controls
	 */
	protected Composite createContents(Composite parent) {
		Font font = parent.getFont();
		
		Composite top = new Composite(parent, SWT.NONE);
		top.setFont(font);
		
		Label label = new Label(top, SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan =2;
		label.setLayoutData(gd);
		label.setFont(font);
		label.setText(AntPreferencesMessages.getString("AntClasspathPage.Run&time_classpath__8")); //$NON-NLS-1$
		
		super.createContents(top);
		
		Label sep= createSeparator(top);
		gd= (GridData)sep.getLayoutData();
		gd.horizontalSpan= 2;
		
		Composite antHomeComposite = new Composite(top, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=1;
		antHomeComposite.setLayoutData(gd);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		antHomeComposite.setLayout(layout);
		
		antHomeButton = new Button(antHomeComposite, SWT.CHECK);
		antHomeButton.setFont(font);
		antHomeButton.setText(AntPreferencesMessages.getString("AntClasspathPage.Set_ANT_HO&ME_9")); //$NON-NLS-1$
		antHomeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				specifyAntHome();
			}
		});
		
		antHome = new Text(antHomeComposite, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		gd.horizontalSpan = 1;
		antHome.setLayoutData(gd);
		antHome.setFont(font);
		antHome.setEnabled(false);
		
		browseAntHomeButton= new Button(top, SWT.PUSH);
		browseAntHomeButton.setFont(font);
		browseAntHomeButton.setText(AntPreferencesMessages.getString("AntClasspathPage.&Browse..._10")); //$NON-NLS-1$
		browseAntHomeButton.setData(new Integer(BROWSE_ANT_HOME));
		browseAntHomeButton.addSelectionListener(selectionAdapter);
		getPreferencePage().setButtonGridData(browseAntHomeButton, GridData.HORIZONTAL_ALIGN_BEGINNING);
		browseAntHomeButton.setEnabled(false);
		
		label = new Label(top, SWT.NONE);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan =2;
		label.setLayoutData(gd);
		label.setFont(font);
		label.setText(AntPreferencesMessages.getString("AntClasspathPage.Additional_classpath_entries__11")); //$NON-NLS-1$
		
		createTable(top);
		createButtonGroup(top);
		
		return top;
	}
	
	/**
	 * Creates the table viewer.
	 */
	protected void createTable(Composite parent) {
		if (getTableViewer() == null) {
			super.createTable(parent);
		} else {
			Table table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
			GridData data= new GridData(GridData.FILL_BOTH);
			data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
			data.horizontalSpan= 1;
			table.setLayoutData(data);
			table.setFont(parent.getFont());
			userContentProvider = getContentProvider();
			userTableViewer = new TableViewer(table);
			userTableViewer.setContentProvider(userContentProvider);
			userTableViewer.setLabelProvider(getLabelProvider());
			userTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					userTableSelectionChanged((IStructuredSelection) event.getSelection());
				}
			});
		}
	}
	
	private void specifyAntHome() {
		antHome.setEnabled(!antHome.getEnabled());
		browseAntHomeButton.setEnabled(!browseAntHomeButton.getEnabled());
		if (antHome.isEnabled()) {
			File rootDir= validateAntHome(antHome.getText());
			if (rootDir != null) {
				setAntHome(rootDir);
			}
		} else {
			getPreferencePage().setMessage(null);
			getPreferencePage().setErrorMessage(null);
		}
	}
			
	private void userTableSelectionChanged(IStructuredSelection selection) {
		ExternalToolsContentProvider contentProvider= (ExternalToolsContentProvider)userTableViewer.getContentProvider();
		Object[] elements = contentProvider.getElements(null);
		List files = Arrays.asList(elements);

		boolean notEmpty = !selection.isEmpty();
		Iterator selected= selection.iterator();
		boolean first= false;
		boolean last= false;
		int lastFile= files.size() - 1;
		while (selected.hasNext()) {
			Object element = (Object) selected.next();
			if(!first && files.indexOf(element) == 0) {
				first= true;
			}
			if (!last && files.indexOf(element) == lastFile) {
				last= true;
			}
		}
		
		removeUserButton.setEnabled(notEmpty);
		upUserButton.setEnabled(notEmpty && !first);
		downUserButton.setEnabled(notEmpty && !last);
		
	}
	

	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected ITableLabelProvider getLabelProvider() {
		return labelProvider;
	}
	
	/**
	 * Returns the content provider to use for the table viewer
	 * 
	 * @return ExternalToolsContentProvider
	 */
	protected ExternalToolsContentProvider getContentProvider() {
		return new AntClasspathContentProvider();
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected void tableSelectionChanged(IStructuredSelection selection) {
		List urls = getContents();
		boolean notEmpty = !selection.isEmpty();
		Iterator elements= selection.iterator();
		boolean first= false;
		boolean last= false;
		int lastUrl= urls.size() - 1;
		while (elements.hasNext()) {
			Object element = (Object) elements.next();
			if(!first && urls.indexOf(element) == 0) {
				first= true;
			}
			if (!last && urls.indexOf(element) == lastUrl) {
				last= true;
			}
		}
		
		removeButton.setEnabled(notEmpty);
		upButton.setEnabled(notEmpty && !first);
		downButton.setEnabled(notEmpty && !last);
	}

	private void handleMove(int direction, TableViewer viewer) {
		IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		List selList= sel.toList();
		Object[] elements = ((ExternalToolsContentProvider)viewer.getContentProvider()).getElements(viewer.getInput());
		List contents= new ArrayList(elements.length);
		for (int i = 0; i < elements.length; i++) {
			contents.add(elements[i]);
		}
		Object[] moved= new Object[contents.size()];
		int i;
		for (Iterator current = selList.iterator(); current.hasNext();) {
			Object config = current.next();
			i= contents.indexOf(config);
			moved[i + direction]= config;
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
	}

	protected String getAntHome() {
		if (antHomeButton.getSelection()) {
			return antHome.getText();
		} else {
			return ""; //$NON-NLS-1$
		}
	}
}