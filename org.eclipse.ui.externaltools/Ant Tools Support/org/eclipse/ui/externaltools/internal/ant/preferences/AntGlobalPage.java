package org.eclipse.ui.externaltools.internal.ant.preferences;

import java.net.URL;
import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;

/**
 * Preference page for setting global Ant user properties.
 * All properties specified here will be set as user properties on the 
 * project for any Ant build
 */
public class AntGlobalPage extends AntPage {
	
	private static final int ADD_BUTTON = IDialogConstants.CLIENT_ID + 1;
	private static final int EDIT_BUTTON = IDialogConstants.CLIENT_ID + 2;
	private static final int REMOVE_BUTTON = IDialogConstants.CLIENT_ID + 3;
	
	private static final int ADD_PROPERTY_FILE_BUTTON = IDialogConstants.CLIENT_ID + 4;
	private static final int EDIT_PROPERTY_FILE_BUTTON = IDialogConstants.CLIENT_ID + 5;
	private static final int REMOVE_PROPERTY_FILE_BUTTON = IDialogConstants.CLIENT_ID + 6;
	
	private Button addButton;
	private Button addFileButton;
	private Button editFileButton;
	private Button removeFileButton;
	
	private TableViewer fileTableViewer;
	private AntPageContentProvider fileContentProvider;
	
	private final AntPropertiesLabelProvider labelProvider = new AntPropertiesLabelProvider();
	
	/**
	 * Creates an instance.
	 */
	public AntGlobalPage(AntPreferencePage preferencePage) {
		super(preferencePage);
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
			addButton= createButton(parent, "AntGlobalPage.addButton", ADD_BUTTON); //$NON-NLS-1$;
			editButton= createButton(parent, "AntGlobalPage.editButton", EDIT_BUTTON); //$NON-NLS-1$;
			removeButton= createButton(parent, "AntGlobalPage.removeButton", REMOVE_BUTTON); //$NON-NLS-1$;
		} else {
			addFileButton= createButton(parent, "AntGlobalPage.addFileButton", ADD_PROPERTY_FILE_BUTTON); //$NON-NLS-1$;
			editFileButton= createButton(parent, "AntGlobalPage.editFileButton", EDIT_PROPERTY_FILE_BUTTON); //$NON-NLS-1$;
			removeFileButton= createButton(parent, "AntGlobalPage.removeButton", REMOVE_PROPERTY_FILE_BUTTON); //$NON-NLS-1$;
		}
	}
	
	/**
	 * Creates the tab item that contains this sub-page.
	 */
	public TabItem createTabItem(TabFolder folder) {
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText("Global");
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
				//addProperty();
				break;
			case EDIT_BUTTON :
				//addProperty();
				break;
			case REMOVE_BUTTON :
				removeButtonPressed();
				break;
			case ADD_PROPERTY_FILE_BUTTON :
				//addFile();
				break;
			case EDIT_PROPERTY_FILE_BUTTON :
				//editFile(getSelection());
				break;
			case REMOVE_PROPERTY_FILE_BUTTON :
				//removeFile();
				break;
		}
	}
	
	protected void performDefaults() {
		/*fVariablesList.removeAllElements();
		Map properties= AntCorePlugin.getPlugin().getPreferences().getAntProperties();
		Set entries= properties.entrySet();
		for (Iterator iter = entries.iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			AntPropertyElement elem= new AntPropertyElement((String)entry.getKey(), (String)entry.getValue());
			fVariablesList.addElement(elem);
		}*/
	}

	/**
	 * Label provider for classpath elements
	 */
	private static final class AntPropertiesLabelProvider extends LabelProvider implements ITableLabelProvider {
		private static final String IMG_JAR_FILE = "icons/full/obj16/jar_l_obj.gif"; //$NON-NLS-1$;
		private static final String IMG_CLASSPATH = "icons/full/obj16/classpath.gif"; //$NON-NLS-1$;
		private static final String IMG_PROPERTY = "icons/full/obj16/prop_ps.gif"; //$NON-NLS-1$;

		private Image classpathImage;
		private Image folderImage;
		private Image jarImage;
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
			if (jarImage != null) {
				jarImage.dispose();
				jarImage = null;
			}
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
		
		private Image getPropertyImage() {
			if (propertyImage == null) {
				ImageDescriptor desc= ExternalToolsPlugin.getDefault().getImageDescriptor(IMG_PROPERTY);
				propertyImage = desc.createImage();
			} 
			return propertyImage;
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
	 * Handles selection changes in the Property file table viewer.
	 */
	protected void fileTableSelectionChanged(IStructuredSelection newSelection) {
		int size = newSelection.size();
		editFileButton.setEnabled(size == 1);
		removeFileButton.setEnabled(size > 0);
	}
	
	/**
	 * Sets the contents of the table on this page.  Has no effect
	 * if this widget has not yet been created or has been disposed.
	 */
	public void initialize() {
		
		getTableViewer().setInput(new ArrayList());
		fileTableViewer.setInput(new ArrayList());
		tableSelectionChanged((IStructuredSelection) getTableViewer().getSelection());
		fileTableSelectionChanged((IStructuredSelection)fileTableViewer.getSelection());
	}
}