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
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;
import java.net.URL;
import java.io.*;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;

public class TargetPage extends WizardPage {
	// NL keys
	private static final String KEY_TITLE = "InstallWizard.TargetPage.title";
	private static final String KEY_DESC = "InstallWizard.TargetPage.desc";
	private static final String KEY_NEW = "InstallWizard.TargetPage.new";
	private static final String KEY_LOCATION = "InstallWizard.TargetPage.location";
	private static final String KEY_LOCATION_MESSAGE = "InstallWizard.TargetPage.location.message";
	private static final String KEY_LOCATION_EMPTY = "InstallWizard.TargetPage.location.empty";
	private static final String KEY_LOCATION_ERROR_TITLE = "InstallWizard.TargetPage.location.error.title";
	private static final String KEY_LOCATION_ERROR_MESSAGE = "InstallWizard.TargetPage.location.error.message";
	private TableViewer tableViewer;
	private IInstallConfiguration config;
	private Image siteImage;
	private ConfigListener configListener;

	class TableContentProvider
		extends DefaultContentProvider
		implements IStructuredContentProvider {

		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object parent) {
			return config.getConfigurationSites();
		}
	}

	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		/**
		* @see ITableLabelProvider#getColumnImage(Object, int)
		*/
		public Image getColumnImage(Object obj, int col) {
			return siteImage;
		}

		/**
		 * @see ITableLabelProvider#getColumnText(Object, int)
		 */
		public String getColumnText(Object obj, int col) {
			if (obj instanceof IConfigurationSite && col == 0) {
				IConfigurationSite csite = (IConfigurationSite) obj;
				ISite site = csite.getSite();
				URL url = site.getURL();
				return url.toString();
			}
			return null;
		}

	}

	class ConfigListener implements IInstallConfigurationChangedListener {
		public void installSiteAdded(IConfigurationSite csite) {
			tableViewer.add(csite);
			tableViewer.setSelection(new StructuredSelection(csite));
		}

		public void installSiteRemoved(IConfigurationSite csite) {
			tableViewer.remove(csite);
		}

		public void linkedSiteAdded(IConfigurationSite site) {
		}

		public void linkedSiteRemoved(IConfigurationSite site) {
		}
	}

	/**
	 * Constructor for ReviewPage
	 */
	public TargetPage(IInstallConfiguration config) {
		super("Target");
		setTitle(UpdateUIPlugin.getResourceString(KEY_TITLE));
		setDescription(UpdateUIPlugin.getResourceString(KEY_DESC));
		this.config = config;
		siteImage = UpdateUIPluginImages.DESC_SITE_OBJ.createImage();
		configListener = new ConfigListener();
	}

	public void dispose() {
		if (siteImage != null) {
			siteImage.dispose();
			siteImage = null;
		}
		config.removeInstallConfigurationChangedListener(configListener);
		super.dispose();
	}

	/**
	 * @see DialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		client.setLayout(layout);
		createTableViewer(client);
		Composite buttonContainer = new Composite(client, SWT.NULL);
		GridLayout blayout = new GridLayout();
		blayout.marginWidth = blayout.marginHeight = 0;
		buttonContainer.setLayout(blayout);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		final Button button = new Button(buttonContainer, SWT.PUSH);
		button.setText(UpdateUIPlugin.getResourceString(KEY_NEW));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addTargetLocation();
			}
		});
		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		button.setLayoutData(gd);
		setControl(client);
	}
	private void createTableViewer(Composite parent) {
		tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		Table table = tableViewer.getTable();
		table.setLayoutData(gd);
		table.setHeaderVisible(true);

		TableColumn tc = new TableColumn(table, SWT.NULL);
		tc.setText(UpdateUIPlugin.getResourceString(KEY_LOCATION));

		TableLayout layout = new TableLayout();
		ColumnLayoutData ld = new ColumnWeightData(100);
		layout.addColumnData(ld);
		table.setLayout(layout);
		tableViewer.setContentProvider(new TableContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		tableViewer.setInput(tableViewer);
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener () {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				boolean empty = selection.isEmpty();
				verifyNotEmpty(empty);
			}
		});
		selectFirstTarget();
		table.setFocus();
		config.addInstallConfigurationChangedListener(configListener);
	}
	
	private void verifyNotEmpty(boolean empty) {
		String errorMessage = null;
		if (empty) 
		  errorMessage = UpdateUIPlugin.getResourceString(KEY_LOCATION_EMPTY);
		setErrorMessage(errorMessage);
		setPageComplete(!empty);
	}

	private void selectFirstTarget() {
		IConfigurationSite[] sites = config.getConfigurationSites();
		IConfigurationSite firstSite = null;
		for (int i = 0; i < sites.length; i++) {
			IConfigurationSite csite = sites[i];
			if (csite.isInstallSite()) {
				firstSite = csite;
				break;
			}

		}
		if (firstSite != null) {
			tableViewer.setSelection(new StructuredSelection(firstSite));
		}
	}

	private void addTargetLocation() {
		DirectoryDialog dd = new DirectoryDialog(getContainer().getShell());
		dd.setMessage(UpdateUIPlugin.getResourceString(KEY_LOCATION_MESSAGE));
		String path = dd.open();
		if (path != null) {
			try {
				File file = new File(path);
				ISite site = SiteManager.createSite(file);
				IConfigurationSite csite =
					SiteManager.createConfigurationSite(
						site,
						IPlatformConfiguration.ISitePolicy.USER_EXCLUDE);
				if (csite.isInstallSite())
					config.addConfigurationSite(csite);
				else {
					String title = UpdateUIPlugin.getResourceString(KEY_LOCATION_ERROR_TITLE);
					String message = UpdateUIPlugin.getFormattedMessage(KEY_LOCATION_ERROR_MESSAGE,
								path);
					MessageDialog.openError(getContainer().getShell(), title, message);
				}
			} catch (CoreException e) {
				UpdateUIPlugin.logException(e);
			}
		}
	}

	public IConfigurationSite getTargetSite() {
		IStructuredSelection sel = (IStructuredSelection) tableViewer.getSelection();
		if (sel.isEmpty())
			return null;
		return (IConfigurationSite) sel.getFirstElement();
	}
}