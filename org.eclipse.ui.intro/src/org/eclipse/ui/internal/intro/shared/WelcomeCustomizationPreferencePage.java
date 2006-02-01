package org.eclipse.ui.internal.intro.shared;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.intro.impl.Messages;


public class WelcomeCustomizationPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IExecutableExtension {
	private TabFolder tabFolder;
	private TreeViewer extensionsTree;
	
	public WelcomeCustomizationPreferencePage() {
		super();
	}

	public WelcomeCustomizationPreferencePage(String title) {
		super(title);
	}

	public WelcomeCustomizationPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		container.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);
		tabFolder = new TabFolder(container, SWT.TOP);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		addPages();
		org.eclipse.jface.dialogs.Dialog.applyDialogFont(container);
		return container;
	}

	public void init(IWorkbench workbench) {
	}

	private void addPages() {
		addRootPage();
		createExtensionsTree();
		addPage("Overview"); //$NON-NLS-1$
		addPage("First Steps"); //$NON-NLS-1$
		addPage("Tutorials"); //$NON-NLS-1$
		addPage("Samples"); //$NON-NLS-1$
		addPage("What's New"); //$NON-NLS-1$
		addPage("Web Resources"); //$NON-NLS-1$
		addPage("Migrate"); //$NON-NLS-1$
	}

	private void createExtensionsTree() {
		extensionsTree = new TreeViewer(tabFolder);
	}

	private void addRootPage() {
		TabItem item = new TabItem(tabFolder, SWT.NULL);
		item.setText("Home"); //$NON-NLS-1$
		Composite container = new Composite(tabFolder, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		Label label = new Label(container, SWT.NULL);
		label.setText(Messages.WelcomeCustomizationPreferencePage_background);
		label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		label = new Label(container, SWT.NULL);
		label.setText(Messages.WelcomeCustomizationPreferencePage_preview);
		Combo combo = new Combo(container, SWT.DROP_DOWN|SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		combo.add("item1"); //$NON-NLS-1$
		combo.add("item2"); //$NON-NLS-1$
		Label preview = new Label(container, SWT.NULL);
		preview.setBackground(preview.getDisplay().getSystemColor(SWT.COLOR_CYAN));
		GridData gd = new GridData();
		gd.widthHint = 160;
		gd.heightHint = 120;
		preview.setLayoutData(gd);
		label = new Label(container, SWT.NULL);
		label.setText(Messages.WelcomeCustomizationPreferencePage_home_links);
		gd = new GridData();
		gd.horizontalSpan  = 2;
		label.setLayoutData(gd);
		CheckboxTableViewer ctable = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL|GridData.FILL_VERTICAL);
		gd.horizontalSpan = 2;
		ctable.getControl().setLayoutData(gd);
		item.setControl(container);
	}

	private void addPage(String label) {
		TabItem item = new TabItem(tabFolder, SWT.NULL);
		item.setText(label);
		item.setControl(extensionsTree.getControl());
	}

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
	}
}