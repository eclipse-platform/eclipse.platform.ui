package org.eclipse.update.internal.ui.wizards;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import java.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.core.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.ui.*;
import java.net.URL;
import java.io.*;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;

public class NewUpdatesWizardPage extends BannerPage {
	// NL keys
	private static final String KEY_TITLE = "NewUpdatesWizard.MainPage.title";
	private static final String KEY_DESC = "NewUpdatesWizard.MainPage.desc";
	private static final String KEY_SELECT_ALL = "NewUpdatesWizard.MainPage.selectAll";
	private static final String KEY_DESELECT_ALL = "NewUpdatesWizard.MainPage.deselectAll";
	private static final String KEY_C_FEATURE = "NewUpdatesWizard.MainPage.column.feature";
	private static final String KEY_C_PROVIDER = "NewUpdatesWizard.MainPage.column.provider";
	private static final String KEY_C_SIZE = "NewUpdatesWizard.MainPage.column.size";
	private CheckboxTableViewer tableViewer;
	private IInstallConfiguration config;
	private Image featureImage;
	private PendingChange [] pendingChanges;

	class TableContentProvider
		extends DefaultContentProvider
		implements IStructuredContentProvider {

		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object parent) {
			return pendingChanges;		}
	}

	class TableLabelProvider
		extends LabelProvider
		implements ITableLabelProvider {
		/**
		* @see ITableLabelProvider#getColumnImage(Object, int)
		*/
		public Image getColumnImage(Object obj, int col) {
			if (col==0)
				return featureImage;
			else
				return null;
		}

		/**
		 * @see ITableLabelProvider#getColumnText(Object, int)
		 */
		public String getColumnText(Object obj, int col) {
			if (obj instanceof IFeatureAdapter) {
				
				try {
				IFeature feature = ((IFeatureAdapter)obj).getFeature();
				
				switch (col) {
					case 0:
						return feature.getLabel();
					case 1:
						return feature.getProvider();
					case 2:
						return feature.getDownloadSize()+"KB";
				}
				}
				catch (CoreException e) {
					UpdateUIPlugin.logException(e);
					return "??";
				}
			}
			return "";
		}
	}

	/**
	 * Constructor for ReviewPage
	 */
	public NewUpdatesWizardPage(
		PendingChange [] changes,
		IInstallConfiguration config) {
		super("Target");
		setTitle(UpdateUIPlugin.getResourceString(KEY_TITLE));
		setDescription(UpdateUIPlugin.getResourceString(KEY_DESC));
		this.config = config;
		this.pendingChanges = changes;
		featureImage = UpdateUIPluginImages.DESC_FEATURE_OBJ.createImage();
	}

	public void dispose() {
		if (featureImage != null) {
			featureImage.dispose();
			featureImage = null;
		}
		super.dispose();
	}

	/**
	 * @see DialogPage#createControl(Composite)
	 */
	public Control createContents(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = layout.marginHeight = 0;
		client.setLayout(layout);
		createTableViewer(client);
		Composite buttonContainer = new Composite(client, SWT.NULL);
		GridLayout blayout = new GridLayout();
		blayout.marginWidth = blayout.marginHeight = 0;
		buttonContainer.setLayout(blayout);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		
		Button button = new Button(buttonContainer, SWT.PUSH);
		button.setText(UpdateUIPlugin.getResourceString(KEY_SELECT_ALL));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectAll(true);
			}
		});
		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		button.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(button);
		
		button = new Button(buttonContainer, SWT.PUSH);
		button.setText(UpdateUIPlugin.getResourceString(KEY_DESELECT_ALL));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectAll(false);
			}
		});
		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		button.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(button);		
		
		tableViewer.setInput(UpdateUIPlugin.getDefault().getUpdateModel());
		tableViewer.setCheckedElements(pendingChanges);
		pageChanged();
		return client;
	}
	
	private void selectAll(boolean state) {
		tableViewer.setAllChecked(state);
		pageChanged();
	}
	
	private void pageChanged() {
		Object [] checked = tableViewer.getCheckedElements();
		setPageComplete(checked.length>0);
	}
	
	private void createTableViewer(Composite parent) {
		tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		Table table = tableViewer.getTable();
		
		TableColumn column = new TableColumn(table, SWT.NULL);
		column.setText(UpdateUIPlugin.getResourceString(KEY_C_FEATURE));
		
		column = new TableColumn(table, SWT.NULL);
		column.setText(UpdateUIPlugin.getResourceString(KEY_C_PROVIDER));
		
		column = new TableColumn(table, SWT.NULL);
		column.setText(UpdateUIPlugin.getResourceString(KEY_C_SIZE));
		
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(100));
		layout.addColumnData(new ColumnWeightData(100));
		layout.addColumnData(new ColumnWeightData(100));
		table.setLayout(layout);
		
		table.setLayoutData(gd);
		tableViewer.setContentProvider(new TableContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			tableViewer.getTable().setFocus();
		}
	}
}