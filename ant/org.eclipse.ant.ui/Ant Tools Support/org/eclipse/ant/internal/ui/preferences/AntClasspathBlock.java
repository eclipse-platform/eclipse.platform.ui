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
import org.eclipse.ant.core.IAntClasspathEntry;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.ant.internal.ui.model.IAntUIConstants;
import org.eclipse.ant.internal.ui.model.IAntUIPreferenceConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
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

public class AntClasspathBlock {

	private static final String[] XERCES= new String[] {"xercesImpl.jar", "xml-apis.jar", "xmlParserAPIs.jar"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] TOOLS= new String[] {"tools.jar"}; //$NON-NLS-1$

	private TreeViewer treeViewer;
	private AntClasspathContentProvider antContentProvider;

	private Button upButton;
	private Button downButton;
	private Button removeButton;

	private AntClasspathLabelProvider labelProvider = new AntClasspathLabelProvider();
	private Button addFolderButton;
	private Button addJARButton;
	private Button addExternalJARButton;
	
	private Button restoreButton;
	
	private boolean localBlock= false;
	
	private Text antHome;
	private Button browseAntHomeButton;

	private IDialogSettings dialogSettings = AntUIPlugin.getDefault().getDialogSettings();
	
	private boolean initializing = true;
	
	private IAntBlockContainer container;
	
	private int validated= 3;
	
	private IClasspathEntry currentParent;
	
	public AntClasspathBlock(boolean localClasspathBlock) {
		super();
		this.localBlock= localClasspathBlock; 
	}

	public void setContainer(IAntBlockContainer container) {
		this.container= container; 
	}
	
	private void addButtonsToButtonGroup(Composite parent) {
	
		addJARButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.addJarButtonTitle")); //$NON-NLS-1$;
		addJARButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				addJars(treeViewer);
			}
		});
	
		addExternalJARButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.42")); //$NON-NLS-1$
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
				handleMove(true, treeViewer);
			}
		});
		downButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.downButtonTitle")); //$NON-NLS-1$;
		downButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleMove(false, treeViewer);
			}
		});
		removeButton = container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.removeButtonTitle")); //$NON-NLS-1$;
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				remove(treeViewer);
			}
		});
		if (localBlock) {
			restoreButton= container.createPushButton(parent, AntPreferencesMessages.getString("AntClasspathBlock.54")); //$NON-NLS-1$
			restoreButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					restoreGlobalEntries();
				}
			});
		}
	}
	
	private void restoreGlobalEntries() {
		GlobalClasspathEntriesSelectionDialog dialog= new GlobalClasspathEntriesSelectionDialog(treeViewer.getControl().getShell(), (AntClasspathLabelProvider)treeViewer.getLabelProvider());
		ClasspathModel model= antContentProvider.getModel();
		Object[] removed= model.getRemovedGlobalEntries();
		Object[] elements= new Object[removed.length + 1];
		elements[0]= model.getAntHomeEntry();
		for (int i = 0; i < removed.length; i++) {
			elements[i+1] = removed[i];
		}
		dialog.setElements(elements);
		if (dialog.open() == Window.OK) {
			Object[] result= dialog.getResult();
			for (int i = 0; i < result.length; i++) {
				GlobalClasspathEntries entry = (GlobalClasspathEntries)result[i];
				if (entry.canBeRemoved()) {
					if (dialog.addAsUnit()) {
						model.setGlobalEntries(AntCorePlugin.getPlugin().getPreferences().getAdditionalClasspathEntries());
						treeViewer.refresh();
					} else {
						IAntClasspathEntry[] entries= AntCorePlugin.getPlugin().getPreferences().getAdditionalClasspathEntries();
						addURLs(entries);
					}
				} else {
					if (dialog.addAsUnit()) {
						initializeAntHome(AntCorePlugin.getPlugin().getPreferences().getAntHome(), false);
					} else {
						IAntClasspathEntry[] entries= AntCorePlugin.getPlugin().getPreferences().getAntHomeClasspathEntries();
						addURLs(entries);
					}
				}
			}	
		}
	}

	private void addURLs(IAntClasspathEntry[] entries) {
		antContentProvider.setRefreshEnabled(false);
		for (int j = 0; j < entries.length; j++) {
			antContentProvider.add(ClasspathModel.USER, entries[j]);
		}
		antContentProvider.setRefreshEnabled(true);
	}

	private void handleMove(boolean up, TreeViewer viewer) {
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		Iterator selected= null;
		if (up) {
			List list= sel.toList();
			Collections.reverse(list);
			selected= list.iterator();
		} else {
			selected= sel.toList().iterator();
		}
		while (selected.hasNext()) {
			IClasspathEntry entry =  (IClasspathEntry) selected.next();
			((AntClasspathContentProvider) viewer.getContentProvider()).handleMove(up, entry);
		}
		treeViewer.refresh();
		treeViewer.setSelection(treeViewer.getSelection());
		updateContainer();
	}

	private void remove(TreeViewer viewer) {
		AntClasspathContentProvider viewerContentProvider = (AntClasspathContentProvider) viewer.getContentProvider();
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
				((AntClasspathContentProvider)viewer.getContentProvider()).add(currentParent, url);
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
		AntClasspathContentProvider contentProvider= (AntClasspathContentProvider)viewer.getContentProvider();
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
		if (currentParent != null) {
			allURLs.addAll(Arrays.asList(currentParent.getEntries()));
		} else {
			Object[] entries= antContentProvider.getModel().getURLEntries(ClasspathModel.USER);
			if (entries != null) {
				allURLs.addAll(Arrays.asList(entries));
			}
		}
		
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
			AntClasspathContentProvider contentProvider= (AntClasspathContentProvider)viewer.getContentProvider();
			contentProvider.setRefreshEnabled(false);
			for (int i = 0; i < elements.length; i++) {
				IFile file = (IFile)elements[i];
				String varExpression= VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression("workspace_loc", file.getFullPath().toString()); //$NON-NLS-1$
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

		antContentProvider = new AntClasspathContentProvider();
		treeViewer = new TreeViewer(tree);
		treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		treeViewer.setContentProvider(antContentProvider);
		treeViewer.setLabelProvider(labelProvider);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (!initializing) {
					tableSelectionChanged((IStructuredSelection) event.getSelection(),
						(AntClasspathContentProvider) treeViewer.getContentProvider());
				}
			}
		});
	}
			
	public void createContents(Composite parent) {
		createClasspathTree(parent);
		createButtonGroup(parent);

		createAntHome(parent);
		
		tableSelectionChanged((IStructuredSelection)treeViewer.getSelection(), antContentProvider);
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

		Label antHomeLabel = new Label(antHomeComposite, SWT.NONE);
		antHomeLabel.setFont(top.getFont());
		antHomeLabel.setText(AntPreferencesMessages.getString("AntClasspathBlock.55"));  //$NON-NLS-1$
		
		antHome = new Text(antHomeComposite, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		antHome.setLayoutData(gd);
		antHome.setFont(top.getFont());
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
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	private void tableSelectionChanged(IStructuredSelection selection, AntClasspathContentProvider contentProvider) {
		
		boolean notEmpty = !selection.isEmpty();
		Iterator selected = selection.iterator();
		boolean first = false;
		boolean last = false;
		boolean canRemove= true;
		boolean haveGlobalEntrySelected= false;
		
		while (selected.hasNext()) {
			IClasspathEntry element = (IClasspathEntry) selected.next();
			
			if (element instanceof GlobalClasspathEntries) {
				if (!((GlobalClasspathEntries)element).canBeRemoved() || !localBlock) {
					canRemove= false;
				}
			}
			IClasspathEntry parent= element.getParent();
			if (parent instanceof GlobalClasspathEntries) {
				haveGlobalEntrySelected= ((GlobalClasspathEntries)parent).canBeRemoved();
			}
			Object[] childEntries = contentProvider.getChildren(parent);
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
		if (localBlock) {
			if (!notEmpty) {
				canAdd= true;
				currentParent= antContentProvider.getModel();
			} else {
				resolveCurrentParent(selection);
				if (haveGlobalEntrySelected) {
					canRemove= false;
				}
			}
		} else {
			canAdd= resolveCurrentParent(selection) && notEmpty;	
		}
		if (addJARButton != null) {
			addJARButton.setEnabled(canAdd);
		}
		addExternalJARButton.setEnabled(canAdd);
		addFolderButton.setEnabled(canAdd);
		removeButton.setEnabled(notEmpty && canRemove);
		upButton.setEnabled((canRemove || localBlock) && notEmpty && !first);
		downButton.setEnabled((canRemove || localBlock) && notEmpty && !last);
		
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
		AntClasspathContentProvider contentProvider = (AntClasspathContentProvider) treeViewer.getContentProvider();
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
		
		contentProvider.setRefreshEnabled(true);
		updateContainer();
	}
	
	public String getAntHome() {
		String antHomeText= antHome.getText().trim();
		if (antHomeText.length() == 0) {
			antHomeText= ""; //$NON-NLS-1$
		}
		return antHomeText;
	}
	
	public void initializeAntHome(String antHomeString, boolean setInitializing) {
		this.initializing= setInitializing; //possible turn off the modifytext callback
		antHome.setText(antHomeString);
		this.initializing= false;
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
				antURLs= antContentProvider.getModel().getURLEntries(ClasspathModel.GLOBAL_USER);
				valid= !JARPresent(antURLs, TOOLS).isEmpty();
				if (!valid) {
					antURLs= antContentProvider.getModel().getURLEntries(ClasspathModel.USER);
					valid= !JARPresent(antURLs, TOOLS).isEmpty();
					if (!valid) {
						valid= MessageDialogWithToggle.openQuestion(AntUIPlugin.getActiveWorkbenchWindow().getShell(), AntPreferencesMessages.getString("AntClasspathBlock.31"), AntPreferencesMessages.getString("AntClasspathBlock.32"), IAntUIPreferenceConstants.ANT_TOOLS_JAR_WARNING, AntPreferencesMessages.getString("AntClasspathBlock.33"), AntUIPlugin.getDefault().getPreferenceStore()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
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
				if (suffixes.isEmpty()) {
					userURLs=  antContentProvider.getModel().getURLEntries(ClasspathModel.USER);
					suffixes= JARPresent(userURLs, XERCES);
				}
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
		if (classpathEntries == null) {
			return Collections.EMPTY_LIST;
		}
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