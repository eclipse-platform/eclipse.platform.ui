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
package org.eclipse.ant.internal.ui.preferences;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.AntCorePreferences;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.ant.internal.ui.model.IAntUIConstants;
import org.eclipse.ant.internal.ui.model.IAntUIPreferenceConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ArchiveFilter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;

/** 
 * This class is a work in progress
 *
 */
public class AntClasspathBlock2 {

	private static final String[] XERCES= new String[] {"xercesImpl.jar", "xml-apis.jar", "xmlParserAPIs.jar"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] TOOLS= new String[] {"tools.jar"}; //$NON-NLS-1$

	private TreeViewer treeViewer;
	private AntClasspathContentProvider2 antContentProvider;

	private Button upButton;
	private Button downButton;
	private Button removeButton;

	private final AntClasspathLabelProvider2 labelProvider = new AntClasspathLabelProvider2();
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
	
	private int validated= 3;
	
	private IClasspathEntry currentParent;
	
	public AntClasspathBlock2(boolean showExternalJARButton) {
		super();
		this.showExternalJARButton= showExternalJARButton; 
	}

	public void setContainer(IAntBlockContainer container) {
		this.container= container; 
	}
	
	private void addButtonsToButtonGroup(Composite parent) {
		
		if (showExternalJARButton) {
			addJARButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.addJarButtonTitle")); //$NON-NLS-1$;
			addJARButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					addJars(treeViewer);
				}
			});
		}
	
		String label;
		if (showExternalJARButton) {
			label= AntPreferencesMessages.getString("AntClasspathBlock.42"); //$NON-NLS-1$
		} else {
			label= AntPreferencesMessages.getString("AntClasspathBlock.addJarButtonTitle");	 //$NON-NLS-1$
		}
		addExternalJARButton = container.createPushButton(parent, label);
		addExternalJARButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				addExternalJars(treeViewer);
			
			}
		});
		addFolderButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.addFolderButtonTitle")); //$NON-NLS-1$;
		addFolderButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				addFolder(treeViewer, AntPreferencesMessages.getString("AntClasspathBlock.1")); //$NON-NLS-1$
			}
		});

		upButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.upButtonTitle")); //$NON-NLS-1$;
		upButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleMove(-1, treeViewer);
			}
		});
		downButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.downButtonTitle")); //$NON-NLS-1$;
		downButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleMove(1, treeViewer);
			}
		});
		removeButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.removeButtonTitle")); //$NON-NLS-1$;
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				remove(treeViewer);
			}
		});

	}
	
	private void handleMove(int direction, TreeViewer viewer) {
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		Iterator selected= null;
		if (direction > 0) {
			List list= sel.toList();
			Collections.reverse(list);
			selected= list.iterator();
		} else {
			selected= sel.toList().iterator();
		}
		while (selected.hasNext()) {
			IClasspathEntry entry =  (IClasspathEntry) selected.next();
			((AntClasspathContentProvider2) viewer.getContentProvider()).handleMove(direction, entry);
		}
		treeViewer.refresh();
		treeViewer.setSelection(treeViewer.getSelection());
		updateContainer();
	}

	private void remove(TreeViewer viewer) {
		AntClasspathContentProvider2 viewerContentProvider = (AntClasspathContentProvider2) viewer.getContentProvider();
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		viewerContentProvider.remove(sel);
		updateContainer();
	}

	/**
	 * Allows the user to enter a folder as a classpath.
	 */
	private void addFolder(TreeViewer viewer, String message) {
		String lastUsedPath = dialogSettings.get(IAntUIConstants.DIALOGSTORE_LASTFOLDER);
		if (lastUsedPath == null) {
			lastUsedPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		}
		DirectoryDialog dialog = new DirectoryDialog(treeViewer.getControl().getShell());
		dialog.setMessage(message);
		dialog.setFilterPath(lastUsedPath);
		String result = dialog.open();
		if (result != null) {
			try {
				URL url = new URL("file:" + result + "/"); //$NON-NLS-2$;//$NON-NLS-1$;
				((AntClasspathContentProvider2)viewer.getContentProvider()).add(currentParent, url);
			} catch (MalformedURLException e) {
			}
		}
		viewer.setSelection(viewer.getSelection());
		dialogSettings.put(IAntUIConstants.DIALOGSTORE_LASTFOLDER, result);
		updateContainer();
	}

	private void addExternalJars(TreeViewer viewer) {
		String lastUsedPath = dialogSettings.get(IAntUIConstants.DIALOGSTORE_LASTEXTJAR);
		if (lastUsedPath == null) {
			lastUsedPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		}
		FileDialog dialog = new FileDialog(treeViewer.getControl().getShell(), SWT.MULTI);
		dialog.setFilterExtensions(new String[] { "*.jar;*.zip" }); //$NON-NLS-1$
		dialog.setFilterPath(lastUsedPath);

		String result = dialog.open();
		if (result == null) {
			return;
		}
		IPath filterPath = new Path(dialog.getFilterPath());
		String[] results = dialog.getFileNames();
		AntClasspathContentProvider2 contentProvider= (AntClasspathContentProvider2)viewer.getContentProvider();
		contentProvider.setRefreshEnabled(false);
		for (int i = 0; i < results.length; i++) {
			String jarName = results[i];
			try {
				IPath path = filterPath.append(jarName).makeAbsolute();
				URL url = new URL("file:" + path.toOSString()); //$NON-NLS-1$;
				contentProvider.add(currentParent, url);
			} catch (MalformedURLException e) {
			}
		}
		contentProvider.setRefreshEnabled(true);

		viewer.setSelection(viewer.getSelection());
		dialogSettings.put(IAntUIConstants.DIALOGSTORE_LASTEXTJAR, filterPath.toOSString());
		updateContainer();
	}
	
	private void addJars(TreeViewer viewer) {
		List allURLs= new ArrayList();
		//TODO not sure this is currect
		allURLs.addAll(Arrays.asList(currentParent.getEntries()));
		
		ViewerFilter filter= new ArchiveFilter(allURLs);
		
		ILabelProvider lp= new WorkbenchLabelProvider();
		ITreeContentProvider cp= new WorkbenchContentProvider();

		ElementTreeSelectionDialog dialog= new ElementTreeSelectionDialog(viewer.getControl().getShell(), lp, cp);
		dialog.setTitle(AntPreferencesMessages.getString("AntClasspathBlock.44"));  //$NON-NLS-1$
		dialog.setMessage(AntPreferencesMessages.getString("AntClasspathBlock.45")); //$NON-NLS-1$
		dialog.addFilter(filter);
		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());	
		dialog.setSorter(new ResourceSorter(ResourceSorter.NAME));
		
		ISelectionStatusValidator validator= new ISelectionStatusValidator() {
			public IStatus validate(Object[] selection) {
				if (selection.length == 0) {
					return new Status(IStatus.ERROR, AntUIPlugin.getUniqueIdentifier(), 0, "", null); //$NON-NLS-1$
				}
				for (int i= 0; i < selection.length; i++) {
					if (!(selection[i] instanceof IFile)) {
						return new Status(IStatus.ERROR, AntUIPlugin.getUniqueIdentifier(), 0, "", null); //$NON-NLS-1$
					}					
				}
				return new Status(IStatus.OK, AntUIPlugin.getUniqueIdentifier(), 0, "", null); //$NON-NLS-1$
			}			
		};
		dialog.setValidator(validator);

		if (dialog.open() == Window.OK) {
			Object[] elements= dialog.getResult();
			AntClasspathContentProvider2 contentProvider= (AntClasspathContentProvider2)viewer.getContentProvider();
			contentProvider.setRefreshEnabled(false);
			for (int i = 0; i < elements.length; i++) {
				IFile file = (IFile)elements[i];
				String varExpression= DebugPlugin.getDefault().getStringVariableManager().generateVariableExpression("workspace_loc", file.getFullPath().toString()); //$NON-NLS-1$
				contentProvider.add(currentParent, varExpression);
			}
			contentProvider.setRefreshEnabled(true);
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
	
	private void createClasspathTree(Composite parent) {
		Tree tree = new Tree(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.heightHint = tree.getItemHeight();
		data.horizontalSpan = 1;
		tree.setLayoutData(data);
		tree.setFont(parent.getFont());
		
		tree.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					remove(treeViewer);
				}
			}
		});	

		antContentProvider = new AntClasspathContentProvider2();
		treeViewer = new TreeViewer(tree);
		treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		treeViewer.setContentProvider(antContentProvider);
		treeViewer.setLabelProvider(labelProvider);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (!initializing) {
					tableSelectionChanged((IStructuredSelection) event.getSelection(),
						(AntClasspathContentProvider2) treeViewer.getContentProvider());
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

		createClasspathTree(parent);
		createButtonGroup(parent);

		createSeparator(parent);

		createAntHome(parent);
		
		tableSelectionChanged((IStructuredSelection)treeViewer.getSelection(), antContentProvider);
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
	private void tableSelectionChanged(IStructuredSelection selection, AntClasspathContentProvider2 contentProvider) {
		
		
		boolean notEmpty = !selection.isEmpty();
		Iterator selected = selection.iterator();
		boolean first = false;
		boolean last = false;
		boolean canRemove= true;
		
		while (selected.hasNext()) {
			IClasspathEntry element = (IClasspathEntry) selected.next();
			if (element instanceof GlobalClasspathEntries) {
				canRemove= false;
			}
			Object[] childEntries = contentProvider.getChildren(element.getParent());
			List entries = Arrays.asList(childEntries);
			int lastEntryIndex = entries.size() - 1;
			if (!first && entries.indexOf(element) == 0) {
				first = true;
			}
			if (!last && entries.indexOf(element) == lastEntryIndex) {
				last = true;
			}
		}

		boolean canAdd= false;
		if (notEmpty) {
			canAdd= resolveCurrentParent(selection);
		}
		if (addJARButton != null) {
			addJARButton.setEnabled(canAdd);
		}
		addExternalJARButton.setEnabled(canAdd);
		addFolderButton.setEnabled(canAdd);
		removeButton.setEnabled(notEmpty && canRemove);
		upButton.setEnabled(canRemove && notEmpty && !first);
		downButton.setEnabled(canRemove && notEmpty && !last);
		
	}
	
	private boolean resolveCurrentParent(IStructuredSelection selection) {
		currentParent= null;
		Iterator selected= selection.iterator();
		
		while (selected.hasNext()) {
			Object element = selected.next();
			if (element instanceof ClasspathEntry) {
				IClasspathEntry parent= ((IClasspathEntry)element).getParent();
				if (currentParent != null) {
					if (!currentParent.equals(parent)) {
						return false;
					}
				} else {
					currentParent= parent;
				}
			} else {
				if (currentParent != null) {
					if (!currentParent.equals(element)) {
						return false;
					}
				} else {
					currentParent= (IClasspathEntry)element;
				}
			}
		}
		return true;
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
		DirectoryDialog dialog = new DirectoryDialog(treeViewer.getControl().getShell());
		dialog.setMessage(AntPreferencesMessages.getString("AntClasspathBlock.3")); //$NON-NLS-1$
		dialog.setFilterPath(lastUsedPath);
		String path = dialog.open();
		if (path == null) {
			return;
		}
	
		antHome.setText(path); //the container will be updated as a side effect of this call
		dialogSettings.put(IAntUIConstants.DIALOGSTORE_LASTANTHOME, path);
	}
		
	private void setAntHome(File rootDir) {
		AntClasspathContentProvider2 contentProvider = (AntClasspathContentProvider2) treeViewer.getContentProvider();
		contentProvider.setRefreshEnabled(false);
		contentProvider.removeAllGlobalAntClasspathEntries();
		String[] names = rootDir.list();
		for (int i = 0; i < names.length; i++) {
			File file = new File(rootDir, names[i]);
			if (file.isFile() && file.getPath().endsWith(".jar")) { //$NON-NLS-1$
				try {
					String name= file.getAbsolutePath();
					IPath jarPath = new Path(name);
					URL url = new URL("file:" + jarPath.toOSString()); //$NON-NLS-1$
					contentProvider.add(ClasspathModel.GLOBAL, url);
				} catch (MalformedURLException e) {
				}
			}
		}
		AntCorePreferences prefs = AntCorePlugin.getPlugin().getPreferences();
		URL url = prefs.getToolsJarURL();
		if (url != null) {
			contentProvider.add(ClasspathModel.GLOBAL, url);
		}
		contentProvider.setRefreshEnabled(true);
		updateContainer();
	}
	
	public String getAntHome() {
		String antHomeText= antHome.getText().trim();
		if (!antHomeButton.getSelection() || antHomeText.length() == 0) {
			antHomeText= null;
		}
		return antHomeText;
	}
	
	public void initializeAntHome(String antHomeString) {
		initializing= true;
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
	
	public void setInput(ClasspathModel model) {
		treeViewer.setInput(model);
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
	
	public boolean validateToolsJAR() {
		validated++;
		boolean check= AntUIPlugin.getDefault().getPreferenceStore().getBoolean(IAntUIPreferenceConstants.ANT_TOOLS_JAR_WARNING);
		if (check && !AntUIPlugin.isMacOS()) {
			Object[] antURLs= antContentProvider.getModel().getURLEntries(ClasspathModel.GLOBAL);
			boolean valid= !JARPresent(antURLs, TOOLS).isEmpty();
			if (!valid) {
				valid= MessageDialogWithToggle.openQuestion(AntUIPlugin.getActiveWorkbenchWindow().getShell(), AntPreferencesMessages.getString("AntClasspathBlock.31"), AntPreferencesMessages.getString("AntClasspathBlock.32"), IAntUIPreferenceConstants.ANT_TOOLS_JAR_WARNING, AntPreferencesMessages.getString("AntClasspathBlock.33"), AntUIPlugin.getDefault().getPreferenceStore()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			if (!valid) {
				container.setErrorMessage(AntPreferencesMessages.getString("AntClasspathBlock.34")); //$NON-NLS-1$
				validated= 3;
			}
			return valid;
			}
		return true;
	}

	public boolean validateXerces(boolean sameVM) {
		boolean valid= true;
		validated++;
		boolean check= AntUIPlugin.getDefault().getPreferenceStore().getBoolean(IAntUIPreferenceConstants.ANT_XERCES_JARS_WARNING);
		if (check) {
			Object[] antURLs= antContentProvider.getModel().getURLEntries(ClasspathModel.GLOBAL);
			List suffixes= JARPresent(antURLs, XERCES);
			if (suffixes.isEmpty()) {
				Object[] userURLs=  antContentProvider.getModel().getURLEntries(ClasspathModel.GLOBAL_USER);
				suffixes= JARPresent(userURLs, XERCES);
			}
			if (sameVM && !suffixes.isEmpty()) {
				valid= MessageDialogWithToggle.openQuestion(treeViewer.getControl().getShell(), AntPreferencesMessages.getString("AntClasspathBlock.35"), MessageFormat.format(AntPreferencesMessages.getString("AntClasspathBlock.36"), new Object[]{suffixes.get(0)}), IAntUIPreferenceConstants.ANT_XERCES_JARS_WARNING, AntPreferencesMessages.getString("AntClasspathBlock.33"), AntUIPlugin.getDefault().getPreferenceStore()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else if (!sameVM && suffixes.size() < 2) {
				valid= MessageDialogWithToggle.openQuestion(treeViewer.getControl().getShell(), AntPreferencesMessages.getString("AntClasspathBlock.35"), AntPreferencesMessages.getString("AntClasspathBlock.52"), IAntUIPreferenceConstants.ANT_XERCES_JARS_WARNING, AntPreferencesMessages.getString("AntClasspathBlock.33"), AntUIPlugin.getDefault().getPreferenceStore()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else {
				valid= true;
			}
			if (!valid) {
				String message;
				if (sameVM) {
					message= MessageFormat.format(AntPreferencesMessages.getString("AntClasspathBlock.38"), new Object[]{suffixes.get(0)}); //$NON-NLS-1$
				} else {
					message= AntPreferencesMessages.getString("AntClasspathBlock.53"); //$NON-NLS-1$
				}
				container.setErrorMessage(message);
			}
		}
		return valid;
	}
	
	private List JARPresent(Object[] classpathEntries, String[] suffixes) {
		List found= new ArrayList(2);
		for (int i = 0; i < classpathEntries.length; i++) {
			String file;
			Object entry = classpathEntries[i];
			if (entry instanceof URL) {
				file= ((URL)entry).getFile();
			} else {
				file= entry.toString();
			}
			for (int j = 0; j < suffixes.length; j++) {
				String suffix = suffixes[j];
				if (file.endsWith(suffix)) {
					found.add(suffix);
				}
			}
		}
		return found;
	}
	
	public boolean isValidated() {
		return validated >= 3;
	}
	
	public void setValidated() {
		validated= 3;
	}
}