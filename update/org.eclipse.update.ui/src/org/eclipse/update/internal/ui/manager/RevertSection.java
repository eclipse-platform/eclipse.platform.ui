package org.eclipse.update.internal.ui.manager;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.ui.forms.FormWidgetFactory;
import org.eclipse.update.core.*;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.core.runtime.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.jface.operation.IRunnableWithProgress;
import java.lang.reflect.InvocationTargetException;

public class RevertSection extends UpdateSection {
	private Composite container;
	private FormWidgetFactory factory;
	private IInstallConfiguration config;
	private Label currentTextLabel;
	private Label textLabel;
	private Button revertButton;
	
	public RevertSection(UpdateFormPage page) {
		super(page);
		setAddSeparator(false);
		setHeaderText("Configuration Reversal");
		setDescription("You can revert from the current to any of the previous configurations."+
		" Note that the reversal will be only partial if some of the features that are part of the "+
		"desired configuration have since been deleted.");
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
			currentTextLabel.setText("This is the current configuration.");
			textLabel.setText(" To revert to the previous one, press:");
			revertButton.setText("Revert");
		}
		else {
			currentTextLabel.setText("");
			textLabel.setText("To restore this configuration, press:");
			revertButton.setText("Restore");
		}
	}
	
	public void performRevert() {
		try {
			final ILocalSite localSite = SiteManager.getLocalSite();
			IInstallConfiguration target = config;
			if (config.isCurrent()) {
				// take the previous one
				IInstallConfiguration [] history = localSite.getConfigurationHistory();
				target = history[history.length - 2];
			}
			final IInstallConfiguration ftarget = target;
			IRunnableWithProgress operation = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					try {
						localSite.revertTo(ftarget, monitor);
					} catch (CoreException e) {
						UpdateUIPlugin.logException(e);
					} finally {
						monitor.done();
					}
				}
			};
			try {
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(container.getShell());
				dialog.run(false, true, operation);
			}
			catch (InvocationTargetException e) {
				UpdateUIPlugin.logException(e);
			}
			catch (InterruptedException e) {
			}
		}
		catch (CoreException e) {
			UpdateUIPlugin.logException(e);
		}
	}
}