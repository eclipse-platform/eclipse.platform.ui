package org.eclipse.ui.externaltools.internal.ant.preferences;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.Property;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;

/**
 * Preference page for setting global Ant user properties.
 * All properties specified here will be set as user properties on the 
 * project for any Ant build
 */
public class AntPropertiesPage extends AntPage {
	
	private static final int ADD_BUTTON = IDialogConstants.CLIENT_ID + 1;
	private static final int EDIT_BUTTON = IDialogConstants.CLIENT_ID + 2;
	private static final int REMOVE_BUTTON = IDialogConstants.CLIENT_ID + 3;
	
	private static final int ADD_PROPERTY_FILE_BUTTON = IDialogConstants.CLIENT_ID + 4;
	private static final int EDIT_PROPERTY_FILE_BUTTON = IDialogConstants.CLIENT_ID + 5;
	private static final int REMOVE_PROPERTY_FILE_BUTTON = IDialogConstants.CLIENT_ID + 6;
	
	private Button addButton;
	private Button addFileButton;
	private Button removeFileButton;
	
	private TableViewer fileTableViewer;
	private AntPageContentProvider fileContentProvider;
	
	private final AntPropertiesLabelProvider labelProvider = new AntPropertiesLabelProvider();
	
	private IDialogSettings fDialogSettings;
	
	/**
	 * Creates an instance.
	 */
	public AntPropertiesPage(AntPreferencePage preferencePage) {
		super(preferencePage);
		fDialogSettings= ExternalToolsPlugin.getDefault().getDialogSettings();
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected ITableLabelProvider getLabelProvider() {
		return labelProvider;
	}

	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected void addButtonsToButtonGroup(Composite parent) {
		if (editButton == null) {
			addButton= createButton(parent, "AntPropertiesPage.addButton", ADD_BUTTON); //$NON-NLS-1$;
			editButton= createButton(parent, "AntPropertiesPage.editButton", EDIT_BUTTON); //$NON-NLS-1$;
			removeButton= createButton(parent, "AntPropertiesPage.removeButton", REMOVE_BUTTON); //$NON-NLS-1$;
		} else {
			addFileButton= createButton(parent, "AntPropertiesPage.addFileButton", ADD_PROPERTY_FILE_BUTTON); //$NON-NLS-1$;
			removeFileButton= createButton(parent, "AntPropertiesPage.removeButton", REMOVE_PROPERTY_FILE_BUTTON); //$NON-NLS-1$;
		}
	}
	
	/**
	 * Creates the tab item that contains this sub-page.
	 */
	public TabItem createTabItem(TabFolder folder) {
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(AntPreferencesMessages.getString("AntPropertiesPage.title"));
		item.setImage(labelProvider.getPropertyImage());
		item.setData(this);
		item.setControl(createControl(folder));
		return item;
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
			table.setLayoutData(data);
			fileContentProvider = getContentProvider();
			fileTableViewer = new TableViewer(table);
			fileTableViewer.setContentProvider(fileContentProvider);
			fileTableViewer.setLabelProvider(getLabelProvider());
			fileTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					fileTableSelectionChanged((IStructuredSelection) event.getSelection());
				}
			});
			
			fileTableViewer.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					//editFile((IStructuredSelection)event.getSelection());
				}
			});
		}
	}
	
	/**
	 * Creates this page's controls
	 */
	public Control createControl(Composite parent) {
		Composite top = (Composite)super.createControl(parent);
		
		Label separator = new Label(parent, SWT.NONE);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.heightHint = 4;
		separator.setLayoutData(gd);
		
		createTable(top);
		createButtonGroup(top);
		
		return top;
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
			case ADD_BUTTON :
				addProperty();
				break;
			case EDIT_BUTTON :
				edit(getSelection());
				break;
			case REMOVE_BUTTON :
				removeButtonPressed();
				break;
			case ADD_PROPERTY_FILE_BUTTON :
				addPropertyFile();
				break;
			case REMOVE_PROPERTY_FILE_BUTTON :
				removePropertyFile();
				break;
		}
	}
	
	private void removePropertyFile() {
		IStructuredSelection selection= ((IStructuredSelection)fileTableViewer.getSelection());
		Iterator itr = selection.iterator();
		while (itr.hasNext()) {
			fileContentProvider.remove(itr.next());
		}
	}
	
	/**
	 * Allows the user to enter a global user property
	 */
	private void addProperty() {
		String title = "Add Property"; 
		String msg = "Enter a name and value for the user property:"; 
		AddCustomDialog dialog = new AddCustomDialog(getShell(), null, title, msg, "&Value:");
		if (dialog.open() == Dialog.CANCEL) {
			return;
		}

		Property prop = new Property();
		prop.setName(dialog.getName());
		prop.setValue(dialog.getClassName());
		addContent(prop);
	}
	
	protected void edit(IStructuredSelection selection) {
		Property prop = (Property) selection.getFirstElement();
		if (prop == null) {
			return;
		}
		String title = "Edit User Property";
		String msg = "Modify the name or value of a user property:";
		AddCustomDialog dialog = new AddCustomDialog(getShell(), null, title, msg, "Value:");
		
		dialog.setClassName(prop.getValue());
		dialog.setName(prop.getName());
		if (dialog.open() == Dialog.CANCEL) {
			return;
		}

		prop.setName(dialog.getName());
		prop.setValue(dialog.getClassName());
		updateContent(prop);
	}
	
	protected void performDefaults() {
		setInput(Arrays.asList(AntCorePlugin.getPlugin().getPreferences().getCustomProperties()));
	}

	/**
	 * Label provider for classpath elements
	 */
	private static final class AntPropertiesLabelProvider extends LabelProvider implements ITableLabelProvider {
		private static final String IMG_CLASSPATH = "icons/full/obj16/classpath.gif"; //$NON-NLS-1$;
		private static final String IMG_PROPERTY = "icons/full/obj16/prop_ps.gif"; //$NON-NLS-1$;

		private Image classpathImage;
		private Image folderImage;
		private Image propertyImage;
	
		/**
		 * Creates an instance.
		 */
		public AntPropertiesLabelProvider() {
		}
		
		/* (non-Javadoc)
		 * Method declared on IBaseLabelProvider.
		 */
		public void dispose() {
			// Folder image is shared, do not dispose.
			folderImage = null;
			if (classpathImage != null) {
				classpathImage.dispose();
				classpathImage = null;
			}
			if (propertyImage != null) {
				propertyImage.dispose();
				propertyImage = null;
			}
		}
		
		/* (non-Javadoc)
		 * Method declared on ITableLabelProvider.
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return getPropertyImage();
		}
		
		/* (non-Javadoc)
		 * Method declared on ITableLabelProvider.
		 */
		public String getColumnText(Object element, int columnIndex) {
			return element.toString();
		}

		private Image getFolderImage() {
			if (folderImage == null)
				folderImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			return folderImage;
		}
		
		private Image getPropertyImage() {
			if (propertyImage == null) {
				ImageDescriptor desc= ExternalToolsPlugin.getDefault().getImageDescriptor(IMG_PROPERTY);
				propertyImage = desc.createImage();
			} 
			return propertyImage;
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
	 * Handles selection changes in the Property file table viewer.
	 */
	protected void fileTableSelectionChanged(IStructuredSelection newSelection) {
		int size = newSelection.size();
		removeFileButton.setEnabled(size > 0);
	}
	
	/**
	 * Sets the contents of the table on this page.  Has no effect
	 * if this widget has not yet been created or has been disposed.
	 */
	public void initialize() {
		getTableViewer().setInput(Arrays.asList(AntCorePlugin.getPlugin().getPreferences().getCustomProperties()));
		fileTableViewer.setInput(Arrays.asList(AntCorePlugin.getPlugin().getPreferences().getCustomPropertyFiles()));
		tableSelectionChanged((IStructuredSelection) getTableViewer().getSelection());
		fileTableSelectionChanged((IStructuredSelection)fileTableViewer.getSelection());
	}
	
	/**
	 * Allows the user to enter JARs to the classpath.
	 */
	private void addPropertyFile() {
		String lastUsedPath;
		lastUsedPath= fDialogSettings.get(IExternalToolsUIConstants.DIALOGSTORE_LASTEXTFILE);
		if (lastUsedPath == null) {
			lastUsedPath= ""; //$NON-NLS-1$
		}
		FileDialog dialog = new FileDialog(getShell(), SWT.MULTI);
		dialog.setFilterExtensions(new String[] { "*.properties" }); //$NON-NLS-1$;
		dialog.setFilterPath(lastUsedPath);

		String result = dialog.open();
		if (result == null) {
			return;
		}
		IPath filterPath= new Path(dialog.getFilterPath());
		String[] results= dialog.getFileNames();
		for (int i = 0; i < results.length; i++) {
			String fileName = results[i];	
			IPath path= filterPath.append(fileName).makeAbsolute();	
			addContent(path.toOSString());
		}
		
		fDialogSettings.put(IExternalToolsUIConstants.DIALOGSTORE_LASTEXTFILE, filterPath.toOSString());
	}
	/**
	 * @see org.eclipse.ui.externaltools.internal.ant.preferences.AntPage#addContent(java.lang.Object)
	 */
	protected void addContent(Object o) {
		if (o instanceof String) {
			fileContentProvider.add(o);
		} else {
			super.addContent(o);
		}
	}
	
	/**
	 * Returns the specified property files
	 * 
	 * @return String[]
	 */
	public String[] getPropertyFiles() {
		Object[] elements = fileContentProvider.getElements(null);
		String[] files= new String[elements.length];
		for (int i = 0; i < elements.length; i++) {
			files[i] = (String)elements[i];
		}
		return files;
	}
}