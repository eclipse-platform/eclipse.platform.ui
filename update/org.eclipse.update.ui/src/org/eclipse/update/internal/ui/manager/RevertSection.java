package org.eclipse.update.internal.ui.manager;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.core.*;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.core.runtime.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.jface.operation.IRunnableWithProgress;
import java.lang.reflect.InvocationTargetException;

public class RevertSection extends UpdateSection {
// NL keys
private static final String KEY_TITLE = "SnapshotPage.RevertSection.title";
private static final String KEY_DESC = "SnapshotPage.RevertSection.desc";
private static final String KEY_CURRENT_TEXT = "SnapshotPage.RevertSection.currentText";
private static final String KEY_REVERT_TEXT = "SnapshotPage.RevertSection.revertText";
private static final String KEY_REVERT_BUTTON = "SnapshotPage.RevertSection.revertButton";
private static final String KEY_RESTORE_TEXT = "SnapshotPage.RevertSection.restoreText";
private static final String KEY_RESTORE_BUTTON = "SnapshotPage.RevertSection.restoreButton";
	
	private Composite container;
	private FormWidgetFactory factory;
	private IInstallConfiguration config;
	private Label currentTextLabel;
	private Label textLabel;
	private Button revertButton;
	
	public RevertSection(UpdateFormPage page) {
		super(page);
		setAddSeparator(false);
		setHeaderText(UpdateUIPlugin.getResourceString(KEY_TITLE));
		setDescription(UpdateUIPlugin.getResourceString(KEY_DESC));
	}

	public Composite createClient(Composite parent, FormWidgetFactory factory) {
		HTMLTableLayout layout = new HTMLTableLayout();
		this.factory = factory;
		header.setForeground(factory.getColor(factory.COLOR_COMPOSITE_SEPARATOR));
		layout.leftMargin = layout.rightMargin = 0;
		layout.horizontalSpacing = 0;
		container = factory.createComposite(parent);
		container.setLayout(layout);
		layout.numColumns = 3;
		currentTextLabel = factory.createLabel(container, "");
		currentTextLabel.setFont(JFaceResources.getBannerFont());
		TableData td = new TableData();
		td.valign = TableData.MIDDLE;
		currentTextLabel.setLayoutData(td);
		textLabel = factory.createLabel(container, "", SWT.WRAP);
		td = new TableData();
		td.valign = TableData.MIDDLE;
		textLabel.setLayoutData(td);
		revertButton = factory.createButton(container, "", SWT.PUSH);
		revertButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performRevert();
			}
		});
		td = new TableData();
		td.indent = 5;
		td.valign = TableData.MIDDLE;
		revertButton.setLayoutData(td);
		return container;
	}
	
	public void configurationChanged(IInstallConfiguration config) {
		this.config = config;
		boolean canRevert = false;
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			IInstallConfiguration [] history = localSite.getConfigurationHistory();
			int length = history.length;
			canRevert = length > 1;
		}
		catch (CoreException e) {
			UpdateUIPlugin.logException(e);
		}
		container.getParent().setVisible(canRevert);
		if (!canRevert) return;
		if (config.isCurrent()) {
			currentTextLabel.setText(UpdateUIPlugin.getResourceString(KEY_CURRENT_TEXT));
			textLabel.setText(UpdateUIPlugin.getResourceString(KEY_REVERT_TEXT));
			revertButton.setText(UpdateUIPlugin.getResourceString(KEY_REVERT_BUTTON));
		}
		else {
			currentTextLabel.setText("");
			textLabel.setText(UpdateUIPlugin.getResourceString(KEY_RESTORE_TEXT));
			revertButton.setText(UpdateUIPlugin.getResourceString(KEY_RESTORE_BUTTON));
		}
	}
	
	private void performRevert() {
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			IInstallConfiguration target = config;
			if (config.isCurrent()) {
				// take the previous one
				IInstallConfiguration [] history = localSite.getConfigurationHistory();
				target = history[history.length - 2];
			}
			performRevert(target);
		}
		catch (CoreException e) {
			UpdateUIPlugin.logException(e);
		}
	}
	
	public static void performRevert(final IInstallConfiguration target) {
		IRunnableWithProgress operation = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				try {
					ILocalSite localSite = SiteManager.getLocalSite();
					localSite.revertTo(target, monitor, new UIProblemHandler());
				} catch (CoreException e) {
					UpdateUIPlugin.logException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(UpdateUIPlugin.getActiveWorkbenchShell().getShell());
			dialog.run(false, true, operation);
		}
		catch (InvocationTargetException e) {
			UpdateUIPlugin.logException(e);
		}
		catch (InterruptedException e) {
		}
	}
}