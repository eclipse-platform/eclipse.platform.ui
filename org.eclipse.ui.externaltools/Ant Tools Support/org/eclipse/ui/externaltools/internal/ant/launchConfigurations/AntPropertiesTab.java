package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ant.core.Property;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.externaltools.internal.ant.preferences.AddPropertyDialog;
import org.eclipse.ui.externaltools.internal.ant.preferences.AntPreferencesMessages;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.ui.ExternalToolsContentProvider;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * Tab for setting Ant user properties per launch configuration. All properties
 * specified here will be set as user properties on the project for the
 * specified Ant build
 */
public class AntPropertiesTab extends AbstractLaunchConfigurationTab {
	
	private Button editButton;
	private Button removeButton;
	private Button addButton;
	private Button addFileButton;
	private Button removeFileButton;
	
	private TableViewer propertyTableViewer;
	private TableViewer fileTableViewer;
	
	private final AntPropertiesLabelProvider labelProvider = new AntPropertiesLabelProvider();
	
	private IDialogSettings fDialogSettings;
	
	
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
			addButton= createPushButton(parent, AntLaunchConfigurationMessages.getString("AntPropertiesTab.&Add..._1"), null); //$NON-NLS-1$
			addButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					addProperty();
				}
			});
			
			editButton= createPushButton(parent, AntLaunchConfigurationMessages.getString("AntPropertiesTab.Ed&it..._2"), null);  //$NON-NLS-1$
			editButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					edit();
				}
			});
			
			removeButton= createPushButton(parent, AntLaunchConfigurationMessages.getString("AntPropertiesTab.R&emove_3"), null);  //$NON-NLS-1$
			removeButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					remove(propertyTableViewer);
				}
			});
		} else {
			addFileButton= createPushButton(parent, AntLaunchConfigurationMessages.getString("AntPropertiesTab.A&dd..._4"), null); //$NON-NLS-1$
			addFileButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					addPropertyFile();
				}
			});
			removeFileButton= createPushButton(parent, AntLaunchConfigurationMessages.getString("AntPropertiesTab.Rem&ove_5"), null); //$NON-NLS-1$
			removeFileButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					remove(fileTableViewer);
				}
			});
		}
	}
	
	/**
	 * Creates the table viewer.
	 */
	protected void createTable(Composite parent) {
		if (propertyTableViewer == null) {
			Table table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
			GridData data= new GridData(GridData.FILL_BOTH);
			data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
			table.setLayoutData(data);
			table.setFont(parent.getFont());
			
			propertyTableViewer = new TableViewer(table);
			propertyTableViewer.setContentProvider(new ExternalToolsContentProvider());
			propertyTableViewer.setLabelProvider(getLabelProvider());
			propertyTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					propertyTableSelectionChanged((IStructuredSelection) event.getSelection());
				}
			});
		} else {
			Table table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
			GridData data= new GridData(GridData.FILL_BOTH);
			data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
			table.setLayoutData(data);
			table.setFont(parent.getFont());
			
			fileTableViewer = new TableViewer(table);
			fileTableViewer.setContentProvider(new ExternalToolsContentProvider());
			fileTableViewer.setLabelProvider(getLabelProvider());
			fileTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					fileTableSelectionChanged((IStructuredSelection) event.getSelection());
				}
			});
		}
	}
	
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		
		fDialogSettings= ExternalToolsPlugin.getDefault().getDialogSettings();
		Composite top = new Composite(parent, SWT.NONE);
		top.setFont(font);
		setControl(top);
		
		GridLayout layout = new GridLayout();
		layout.numColumns= 2;
		top.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		top.setLayoutData(gridData);
		
		createVerticalSpacer(top, 2);
		
		Label label = new Label(top, SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan =2;
		label.setLayoutData(gd);
		label.setFont(font);
		label.setText(AntLaunchConfigurationMessages.getString("AntPropertiesTab.&Properties__6")); //$NON-NLS-1$
		
		createTable(top);
		createButtonGroup(top);
		
		label = new Label(top, SWT.NONE);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan =2;
		label.setLayoutData(gd);
		label.setFont(font);
		label.setText(AntLaunchConfigurationMessages.getString("AntPropertiesTab.Property_f&iles__7")); //$NON-NLS-1$
		
		createTable(top);
		createButtonGroup(top);
	}
	
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
	
	protected void remove(TableViewer viewer) {
		ExternalToolsContentProvider antContentProvider= (ExternalToolsContentProvider)viewer.getContentProvider();
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		antContentProvider.remove(sel);
		updateLaunchConfigurationDialog();
	}
	
	/**
	 * Allows the user to enter a global user property
	 */
	private void addProperty() {
		String title = AntPreferencesMessages.getString("AntPropertiesPage.Add_Property_2");  //$NON-NLS-1$
		AddPropertyDialog dialog = new AddPropertyDialog(getShell(), title, new String[]{"", ""});
		if (dialog.open() == Dialog.CANCEL) {
			return;
		}

		Property prop = new Property();
		String[] pair= dialog.getNameValuePair();
		prop.setName(pair[0]);
		prop.setValue(pair[1]);
		((ExternalToolsContentProvider)propertyTableViewer.getContentProvider()).add(prop);
		updateLaunchConfigurationDialog();
	}
	
	protected void edit() {
		IStructuredSelection selection= (IStructuredSelection) propertyTableViewer.getSelection();
		Property prop = (Property) selection.getFirstElement();
		if (prop == null) {
			return;
		}
		String title = AntPreferencesMessages.getString("AntPropertiesPage.Edit_User_Property_5"); //$NON-NLS-1$
		AddPropertyDialog dialog = new AddPropertyDialog(getShell(), title, new String[]{prop.getName(), prop.getValue()});
		
		if (dialog.open() == Dialog.CANCEL) {
			return;
		}

		String[] pair= dialog.getNameValuePair();
		prop.setName(pair[0]);
		prop.setValue(pair[1]);
		propertyTableViewer.update(prop, null);
		updateLaunchConfigurationDialog();
	}

	/**
	 * Label provider for classpath elements
	 */
	private static final class AntPropertiesLabelProvider extends LabelProvider implements ITableLabelProvider {
		private static final String IMG_CLASSPATH = "icons/full/obj16/classpath.gif"; //$NON-NLS-1$;
		private static final String IMG_PROPERTY = "icons/full/obj16/prop_ps.gif"; //$NON-NLS-1$;

		private Image classpathImage;
		private Image fileImage;
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
			// file image is shared, do not dispose.
			fileImage = null;
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
			if (element instanceof Property) {
				return getPropertyImage();
			} else {
				return getFileImage();
			}
		}
		
		/* (non-Javadoc)
		 * Method declared on ITableLabelProvider.
		 */
		public String getColumnText(Object element, int columnIndex) {
			return element.toString();
		}

		private Image getFileImage() {
			if (fileImage == null) {
				fileImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
			}
			return fileImage;
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
	private void fileTableSelectionChanged(IStructuredSelection newSelection) {
		removeFileButton.setEnabled(newSelection.size() > 0);
	}
	
	/**
	 * Handles selection changes in the Property file table viewer.
	 */
	private void propertyTableSelectionChanged(IStructuredSelection newSelection) {
		int size= newSelection.size();
		removeButton.setEnabled(size > 0);
		editButton.setEnabled(size > 0);
	}
	
	/**
	 * Allows the user to enter property files
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
			((ExternalToolsContentProvider)fileTableViewer.getContentProvider()).add(path.toOSString());
		}
		
		fDialogSettings.put(IExternalToolsUIConstants.DIALOGSTORE_LASTEXTFILE, filterPath.toOSString());
		updateLaunchConfigurationDialog();
	}
	
	/**
	 * Returns the specified property files
	 * 
	 * @return String[]
	 */
	protected String[] getPropertyFiles() {
		Object[] elements = ((ExternalToolsContentProvider)fileTableViewer.getContentProvider()).getElements(null);
		String[] files= new String[elements.length];
		for (int i = 0; i < elements.length; i++) {
			files[i] = (String)elements[i];
		}
		return files;
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return labelProvider.getPropertyImage();
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return AntLaunchConfigurationMessages.getString("AntPropertiesTab.P&roperties_8"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		setErrorMessage(null);
		setMessage(null);
		Map properties= null;
		try {
			properties= configuration.getAttribute(IExternalToolConstants.ATTR_ANT_PROPERTIES, (Map)null);
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log(AntLaunchConfigurationMessages.getString("AntPropertiesTab.Error_reading_configuration_9"), ce); //$NON-NLS-1$
		}
		
		String propertyFiles= null;
		try {
			propertyFiles= configuration.getAttribute(IExternalToolConstants.ATTR_ANT_PROPERTY_FILES, (String)null);
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log(AntLaunchConfigurationMessages.getString("AntPropertiesTab.Error_reading_configuration_9"), ce); //$NON-NLS-1$
		}
		
		populatePropertyViewer(properties);
		
		String[] files= AntUtil.parseString(propertyFiles, ","); //$NON-NLS-1$
		fileTableViewer.setInput(files);
		
		propertyTableSelectionChanged((IStructuredSelection) propertyTableViewer.getSelection());
		fileTableSelectionChanged((IStructuredSelection)fileTableViewer.getSelection());
	}
	
	private void populatePropertyViewer(Map properties) {
		if (properties == null) {
			propertyTableViewer.setInput(new Property[0]);
			return;
		} 
		Property[] result = new Property[properties.size()];
		Iterator entries= properties.entrySet().iterator();
		int i= 0;
		while (entries.hasNext()) {
			Map.Entry element = (Map.Entry) entries.next();
			Property property = new Property();
			property.setName((String)element.getKey());
			property.setValue((String)element.getValue());
			result[i]= property;
			i++;
		}
		propertyTableViewer.setInput(result);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		
		Object[] items= ((ExternalToolsContentProvider)propertyTableViewer.getContentProvider()).getElements(null);
		Map properties= null;
		if (items.length > 0) {
			properties= new HashMap(items.length);
			for (int i = 0; i < items.length; i++) {
				Property property = (Property)items[i];
				properties.put(property.getName(), property.getValue());
			}
		}
		
		configuration.setAttribute(IExternalToolConstants.ATTR_ANT_PROPERTIES, properties);
		
		items= ((ExternalToolsContentProvider)fileTableViewer.getContentProvider()).getElements(null);
		String files= null;
		if (items.length > 0) {
			StringBuffer buff= new StringBuffer();
			for (int i = 0; i < items.length; i++) {
				String path = (String)items[i];
				buff.append(path);
				buff.append(',');
			}
			files= buff.toString();
		}
		
		configuration.setAttribute(IExternalToolConstants.ATTR_ANT_PROPERTY_FILES, files);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

}