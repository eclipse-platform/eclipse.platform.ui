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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
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
	private AntPageContentProvider userContentProvider;

	private IDialogSettings fDialogSettings;
	private final AntClasspathLabelProvider labelProvider = new AntClasspathLabelProvider();

	/**
	 * Creates an instance.
	 */
	public AntClasspathPage(AntPreferencePage preferencePage) {
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
			createButton(parent, "AntClasspathPage.addJarButtonTitle", ADD_USER_JARS_BUTTON); //$NON-NLS-1$;
			createButton(parent, "AntClasspathPage.addFolderButtonTitle", ADD_USER_FOLDER_BUTTON); //$NON-NLS-1$;
			upUserButton= createButton(parent, "AntClasspathPage.upButtonTitle", UP_USER_BUTTON); //$NON-NLS-1$;
			downUserButton= createButton(parent, "AntClasspathPage.downButtonTitle", DOWN_USER_BUTTON); //$NON-NLS-1$;
			removeUserButton= createButton(parent, "AntClasspathPage.removeButtonTitle", REMOVE_USER_BUTTON); //$NON-NLS-1$;
		}
	}
	
	/**
	 * Allows the user to enter a folder as a classpath.
	 */
	private void addFolder(AntPageContentProvider contentProvider, String message) {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setMessage(message);
		
		String result = dialog.open();
		if (result != null) {
			try {
				URL url = new URL("file:" + result + "/"); //$NON-NLS-2$;//$NON-NLS-1$;
				contentProvider.add(url);
			} catch (MalformedURLException e) {
			}
		}
	}
	
	private void addJars(AntPageContentProvider contentProvider) {
		String lastUsedPath= fDialogSettings.get(IExternalToolsUIConstants.DIALOGSTORE_LASTEXTJAR);
		if (lastUsedPath == null) {
			lastUsedPath= ""; //$NON-NLS-1$
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
				contentProvider.add(url);
			} catch (MalformedURLException e) {
			}
		}
		
		fDialogSettings.put(IExternalToolsUIConstants.DIALOGSTORE_LASTEXTJAR, filterPath.toOSString());
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
			case ADD_JARS_BUTTON :
				addJars((AntPageContentProvider)getTableViewer().getContentProvider());
				break;
			case ADD_FOLDER_BUTTON :
				addFolder((AntPageContentProvider)getTableViewer().getContentProvider(), AntPreferencesMessages.getString("AntClasspathPage.&Choose_a_folder_to_add_to_the_classpath__1")); //$NON-NLS-1$
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
				addJars(userContentProvider);
				break;
			case ADD_USER_FOLDER_BUTTON :
				addFolder(userContentProvider,AntPreferencesMessages.getString("AntClasspathPage.&Choose_a_folder_to_add_to_the_classpath__1")); //$NON-NLS-1$
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
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setMessage(AntPreferencesMessages.getString("AntClasspathPage.&Choose_a_folder_that_will_be_used_as_the_location_of_ANT_HOME_3")); //$NON-NLS-1$
		
		String path = dialog.open();
		if (path == null) {
			return;
		}
		
		antHome.setText(path);
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
		contentProvider.add(prefs.getToolsJarURL());
	}
	
	private File validateAntHome(String path) {
		if (path.length() == 0) {
			return null;
		}
		File rootDir = new File(path, "lib"); //$NON-NLS-1$
		if (!rootDir.exists()) {
			getPreferencePage().setErrorMessage(AntPreferencesMessages.getString("AntClasspathPage.Specified_ANT_HOME_does_not_contain_a___lib___directory_7")); //$NON-NLS-1$
			getPreferencePage().setValid(false);
			return null;
		} else {
			getPreferencePage().setErrorMessage(null);
			getPreferencePage().setValid(true);
		}
		return rootDir;
	}
	
	/**
	 * Returns the specified user classpath URLs
	 * 
	 * @return List
	 */
	protected List getUserURLs() {
		Object[] elements = userContentProvider.getElements(null);
		List contents= new ArrayList(elements.length);
		for (int i = 0; i < elements.length; i++) {
			contents.add(elements[i]);
		}
		return contents;
	}
	
	/**
	 * Sets the contents of the tables on this page.
	 */
	protected void initialize() {
		AntCorePreferences prefs= AntCorePlugin.getPlugin().getPreferences();
		getTableViewer().setInput(Arrays.asList(prefs.getAntURLs()));
		userTableViewer.setInput(Arrays.asList(prefs.getCustomURLs()));
		String antHomePath= prefs.getAntHome();
		boolean enabled= antHomePath.length() > 0;
		antHome.setEnabled(enabled);
		browseAntHomeButton.setEnabled(enabled);
		antHomeButton.setSelection(enabled);
		antHome.setText(antHomePath);
		tableSelectionChanged((IStructuredSelection) getTableViewer().getSelection());
		userTableSelectionChanged((IStructuredSelection)userTableViewer.getSelection());
		getPreferencePage().setErrorMessage(null);
		getPreferencePage().setValid(true);
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
		
		Composite top = new Composite(parent, SWT.NONE);
		
		Label label = new Label(top, SWT.NONE);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan =2;
		label.setLayoutData(gd);
		label.setFont(parent.getFont());
		label.setText(AntPreferencesMessages.getString("AntClasspathPage.Run&time_classpath__8")); //$NON-NLS-1$
		
		super.createContents(top);
		
		Composite antHomeComposite = new Composite(top, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=1;
		antHomeComposite.setLayoutData(gd);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		antHomeComposite.setLayout(layout);
		
		antHomeButton = new Button(antHomeComposite, SWT.CHECK);
		antHomeButton.setText(AntPreferencesMessages.getString("AntClasspathPage.Set_ANT_HO&ME_9")); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 1;
		antHomeButton.setLayoutData(gd);
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
		antHome.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				File rootDir= validateAntHome(antHome.getText());
				if (rootDir != null) {
					setAntHome(rootDir);
				}
			}
		});
		antHome.setEnabled(false);
		
		browseAntHomeButton= new Button(top, SWT.PUSH);
		browseAntHomeButton.setText(AntPreferencesMessages.getString("AntClasspathPage.&Browse..._10")); //$NON-NLS-1$
		browseAntHomeButton.setData(new Integer(BROWSE_ANT_HOME));
		browseAntHomeButton.addSelectionListener(selectionAdapter);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		browseAntHomeButton.setLayoutData(gd);
		browseAntHomeButton.setEnabled(false);
		//getPreferencePage().setButtonGridData(browseAntHomeButton);
		
		label = new Label(top, SWT.NONE);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan =2;
		label.setLayoutData(gd);
		label.setFont(parent.getFont());
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
			
	private void userTableSelectionChanged(IStructuredSelection newSelection) {
		int size = newSelection.size();
		removeUserButton.setEnabled(size > 0);
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
	 * @return AntPageContentProvider
	 */
	protected AntPageContentProvider getContentProvider() {
		return new AntClasspathContentProvider();
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected void tableSelectionChanged(IStructuredSelection newSelection) {
		IStructuredSelection selection = (IStructuredSelection)getTableViewer().getSelection();
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
		Object[] elements = ((AntPageContentProvider)viewer.getContentProvider()).getElements(viewer.getInput());
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

	/**
	 * Label provider for classpath elements
	 */
	private static final class AntClasspathLabelProvider extends LabelProvider implements ITableLabelProvider {
		private static final String IMG_JAR_FILE = "icons/full/obj16/jar_l_obj.gif"; //$NON-NLS-1$;
		private static final String IMG_CLASSPATH = "icons/full/obj16/classpath.gif"; //$NON-NLS-1$;

		private Image classpathImage;
		private Image folderImage;
		private Image jarImage;
	
		/**
		 * Creates an instance.
		 */
		public AntClasspathLabelProvider() {
		}
		
		/* (non-Javadoc)
		 * Method declared on IBaseLabelProvider.
		 */
		public void dispose() {
			// Folder image is shared, do not dispose.
			folderImage = null;
			if (jarImage != null) {
				jarImage.dispose();
				jarImage = null;
			}
			if (classpathImage != null) {
				classpathImage.dispose();
				classpathImage = null;
			}
		}
		
		/* (non-Javadoc)
		 * Method declared on ITableLabelProvider.
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			URL url = (URL) element;
			if (url.getFile().endsWith("/")) //$NON-NLS-1$
				return getFolderImage();
			else
				return getJarImage();
		}
		
		/* (non-Javadoc)
		 * Method declared on ITableLabelProvider.
		 */
		public String getColumnText(Object element, int columnIndex) {
			return ((URL) element).getFile();
		}

		private Image getFolderImage() {
			if (folderImage == null)
				folderImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			return folderImage;
		}
		
		private Image getJarImage() {
			if (jarImage == null) {
				ImageDescriptor desc = ExternalToolsPlugin.getDefault().getImageDescriptor(IMG_JAR_FILE);
				jarImage = desc.createImage();
			}
			return jarImage;
		}
		
		private Image getClasspathImage() {
			if (classpathImage == null) {
				ImageDescriptor desc = ExternalToolsPlugin.getDefault().getImageDescriptor(IMG_CLASSPATH);
				classpathImage = desc.createImage();
			}
			return classpathImage;
		}
	}
	/**
	 * Content provider that maintains a generic list of objects which
	 * are shown in a table viewer.
	 */
	private static class AntClasspathContentProvider extends AntPageContentProvider {
		public void add(Object o) {
			URL newURL= (URL)o;
			Iterator itr= elements.iterator();
			while (itr.hasNext()) {
				URL url = (URL) itr.next();
				if (url.sameFile(newURL)) {
					return;
				}
			}
			elements.add(o);
			viewer.add(o);
		}
		
		public void removeAll() {
			viewer.remove(elements.toArray());
			elements= new ArrayList(5);
		}
	}
	
	protected String getAntHome() {
		if (antHomeButton.getSelection()) {
			return antHome.getText();
		} else {
			return ""; //$NON-NLS-1$
		}
	}
}