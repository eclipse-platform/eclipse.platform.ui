package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class LaunchConfigurationTypesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	// UI widgets
	private Label fFileTypeLabel;
	private TableViewer fFileTypeTableViewer;
	private Label fConfigTypeLabel;
	private TableViewer fConfigTypeTableViewer;
	private Button fDefaultButton;
	
	// Local mapping of file extensions to default launch configs.  It is necessary to keep a local
	// copy of this in this class so that the user can 'Cancel' out of any changes they make in this 
	// preference page.
	private Map fDefaultConfigsMap;

	/**
	 * Content provider for the file type TableViewer
	 */
	protected class FileTypeContentProvider implements IStructuredContentProvider {
		
		private String[] fContent;
		
		public FileTypeContentProvider(String[] content) {
			fContent = content;
		}
		
		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			return fContent;
		}

		/**
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/**
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	/**
	 * Label provider for the FileType table viewer
	 */
	protected class FileTypeLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		/**
		 * @see ITableLabelProvider#getColumnText(Object, int)
		 */
		public String getColumnText(Object object, int column) {
			if (column == 0) {
				return generateElementText(object);
			}
			return ""; //$NON-NLS-1$
		}
		
		/**
		 * @see ILabelProvider#getText(Object)
		 */
		public String getText(Object element) {
			return generateElementText(element);
		}
		
		/**
		 * @see ITableLabelProvider#getColumnImage(Object, int)
		 */
		public Image getColumnImage(Object object, int column) {
			// Implement later
			return null;
		}
		
		/**
		 * Convenience method that generates the displayable form of a file type
		 */
		private String generateElementText(Object obj) {
			StringBuffer buffer = new StringBuffer((String)obj);
			buffer.insert(0, "*.");			//$NON-NLS-1$
			return buffer.toString();
		}
	}

	/**
	 * Content provider for the configuration type TableViewer
	 */
	protected class ConfigTypeContentProvider implements IStructuredContentProvider {
		
		private String fFileTypeInput;
		
		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			return getLaunchManager().getAllLaunchConfigurationTypesFor(fFileTypeInput);
		}

		/**
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/**
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			fFileTypeInput = (String) newInput;
		}

	}

	/**
	 * Label provider for the configuration table viewer
	 */
	protected class ConfigTypeLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		/**
		 * @see ITableLabelProvider#getColumnText(Object, int)
		 */
		public String getColumnText(Object object, int column) {
			if (column == 0) {
				return generateElementText(object);
			}
			return ""; //$NON-NLS-1$
		}
		
		/**
		 * @see ILabelProvider#getText(Object)
		 */
		public String getText(Object element) {
			return generateElementText(element);
		}
		
		/**
		 * @see ITableLabelProvider#getColumnImage(Object, int)
		 */
		public Image getColumnImage(Object object, int column) {
			// Implement later
			return null;
		}
		
		/**
		 * Convenience method that generates the displayable form of a file type
		 */
		private String generateElementText(Object obj) {
			ILaunchConfigurationType configType = (ILaunchConfigurationType) obj;
			StringBuffer buffer = new StringBuffer(configType.getName());
			String selectedFileType = getFileTypeSelection();
			if (getDefaultConfigsMap().get(selectedFileType).equals(configType)) {
				buffer.append(" (default)");
			}
			return buffer.toString();
		}
	}

	/**
	 * Creates the page's UI content.
	 */
	protected Control createContents(Composite parent) {
	
		Composite topComp = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		topComp.setLayout(layout);
		GridData gd;
	
		setFileTypeLabel(new Label(topComp, SWT.LEFT));
		getFileTypeLabel().setText("File types");
		gd = new GridData();
		gd.horizontalSpan = 2;
		getFileTypeLabel().setLayoutData(gd);
	
		setFileTypeTableViewer(new TableViewer(topComp, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION));
		String[] fileTypes = getLaunchManager().getAllRegisteredFileExtensions();
		getFileTypeTableViewer().setContentProvider(new FileTypeContentProvider(fileTypes));
		getFileTypeTableViewer().setLabelProvider(new FileTypeLabelProvider());
		getFileTypeTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent evt) {
				handleFileTypeSelectionChanged();
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		getFileTypeTableViewer().getTable().setLayoutData(gd);
		getFileTypeTableViewer().setInput(fileTypes);
	
		createSpacer(topComp, 1);
		createSpacer(topComp, 2);
	
		setConfigTypeLabel(new Label(topComp, SWT.LEFT));
		getConfigTypeLabel().setText("Configuration types");
		gd = new GridData();
		gd.horizontalSpan = 2;
		getConfigTypeLabel().setLayoutData(gd);
	
		setConfigTypeTableViewer(new TableViewer(topComp, SWT.SINGLE | SWT.BORDER));
		getConfigTypeTableViewer().setContentProvider(new ConfigTypeContentProvider());
		getConfigTypeTableViewer().setLabelProvider(new ConfigTypeLabelProvider());
		getConfigTypeTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent evt) {
				handleConfigTypeSelectionChanged();
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		getConfigTypeTableViewer().getTable().setLayoutData(gd);
		
		setDefaultButton(new Button(topComp, SWT.PUSH));
		getDefaultButton().setText("Set as default");
		getDefaultButton().addSelectionListener(new SelectionAdapter() {
			public void widgetSelection(SelectionEvent evt) {
				handleDefaultButtonSelected();
			}
		});
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		getDefaultButton().setLayoutData(gd);
		
		//WorkbenchHelp.setHelp(parent, new DialogPageContextComputer(this, IHelpContextIds.FILE_EDITORS_PREFERENCE_PAGE));
		
		return topComp;
	}

	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}
	
	private void setDefaultConfigsMap(Map defaultConfigsMap) {
		fDefaultConfigsMap = defaultConfigsMap;
	}

	private Map getDefaultConfigsMap() {
		return fDefaultConfigsMap;
	}

	/*****************************************************************************
	 * 
	 * Accessors for the UI widgets
	 * 
	 *****************************************************************************/
	
	private void setDefaultButton(Button defaultButton) {
		fDefaultButton = defaultButton;
	}

	private Button getDefaultButton() {
		return fDefaultButton;
	}

	private void setConfigTypeTableViewer(TableViewer tableViewer) {
		fConfigTypeTableViewer = tableViewer;
	}

	private TableViewer getConfigTypeTableViewer() {
		return fConfigTypeTableViewer;
	}

	private void setConfigTypeLabel(Label configTypeLabel) {
		fConfigTypeLabel = configTypeLabel;
	}

	private Label getConfigTypeLabel() {
		return fConfigTypeLabel;
	}

	private void setFileTypeLabel(Label fileTypeLabel) {
		fFileTypeLabel = fileTypeLabel;
	}

	private Label getFileTypeLabel() {
		return fFileTypeLabel;
	}
	
	private void setFileTypeTableViewer(TableViewer tableViewer) {
		fFileTypeTableViewer = tableViewer;
	}
	
	private TableViewer getFileTypeTableViewer() {
		return fFileTypeTableViewer;
	}
	
	/*****************************************************************************
	 * 
	 * Event handling methods
	 * 
	 *****************************************************************************/
	
	/**
	 * Set the input for the config type viewer based on the current selection in the file type viewer.
	 * If there is no selection in the viewer, then the config type viewer's input is set to null.  
	 */
	protected void handleFileTypeSelectionChanged() {
		String newInput = getFileTypeSelection();
		getConfigTypeTableViewer().setInput(newInput);
	}
	
	/**
	 * Set the enabled state of the 'default' button based on the current selection in the 
	 * config type table viewer.
	 */
	protected void handleConfigTypeSelectionChanged() {
		ILaunchConfigurationType configType = getConfigTypeSelection();
		if (configType == null) {
			getDefaultButton().setEnabled(false);
		} else {
			getDefaultButton().setEnabled(true);			
		}
	}
	
	/**
	 * Set the currently selected config type in the config type viewer to be the default.
	 */
	protected void handleDefaultButtonSelected() {
		String fileType = getFileTypeSelection();
		ILaunchConfigurationType configType = getConfigTypeSelection();
		getDefaultConfigsMap().put(fileType, configType);
		getConfigTypeTableViewer().refresh();
	}
	
	/**
	 * Convenience method to return the first selected element in the file type viewer
	 */
	protected String getFileTypeSelection() {
		IStructuredSelection selection = (IStructuredSelection) getFileTypeTableViewer().getSelection();
		return (String) selection.getFirstElement();		
	}
	
	/**
	 * Convenience method to return the first selected element in the config type viewer
	 */
	protected ILaunchConfigurationType getConfigTypeSelection() {
		IStructuredSelection selection = (IStructuredSelection) getConfigTypeTableViewer().getSelection();
		return (ILaunchConfigurationType) selection.getFirstElement();		
	}
	
	/**
	 * @see IWorkbenchPreferencePage
	 */
	public void init(IWorkbench aWorkbench){
		readDefaultLaunchConfigs();
		noDefaultAndApplyButton();
	}

	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		saveDefaultLaunchConfigs();
		return true;
	}
	
	/**
	 * Populate a local copy of the file extension-->default launch configs mapping from the launch manager.
	 */
	protected void readDefaultLaunchConfigs() {
		ILaunchManager launchManager = getLaunchManager();
		String[] allFileExtensions = launchManager.getAllRegisteredFileExtensions();
		setDefaultConfigsMap(new HashMap(allFileExtensions.length));
		for (int i = 0; i < allFileExtensions.length; i++) {
			String fileExtension = allFileExtensions[i];
			ILaunchConfigurationType configType = launchManager.getDefaultLaunchConfigurationType(fileExtension);
			getDefaultConfigsMap().put(fileExtension, configType);
		}
	}
	
	/**
	 * Save the current mapping of file extensions to default launch configs into the launch manager.
	 */
	protected void saveDefaultLaunchConfigs() {
		ILaunchManager launchManager = getLaunchManager();
		Iterator iterator = getDefaultConfigsMap().keySet().iterator();
		while (iterator.hasNext()) {
			String fileExtension = (String) iterator.next();
			ILaunchConfigurationType configType = (ILaunchConfigurationType) getDefaultConfigsMap().get(fileExtension);
			launchManager.setDefaultLaunchConfigurationType(fileExtension, configType);
		}
	}
	
	/**
	 * Convenience method to retrieve the launch mananger
	 */	
	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

}

