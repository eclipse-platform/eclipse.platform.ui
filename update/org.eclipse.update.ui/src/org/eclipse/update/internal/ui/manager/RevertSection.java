package org.eclipse.update.internal.ui.manager;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.ui.forms.FormWidgetFactory;
import org.eclipse.update.core.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;


public class RevertSection extends UpdateSection {
	private Composite container;
	private FormWidgetFactory factory;
	private IInstallConfiguration config;
	private Label textLabel;
	private Button revertButton;
	
	public RevertSection(UpdateFormPage page) {
		super(page);
		setAddSeparator(false);
		setHeaderText("Configuration Reversal");
		setDescription("You can revert from the current to any of the previous configurations."+
		" Note that the reversal may not be complete if some of the features that is part of the "+
		"desired configuration has since been deleted.");
	}

	public Composite createClient(Composite parent, FormWidgetFactory factory) {
		HTMLTableLayout layout = new HTMLTableLayout();
		this.factory = factory;
		header.setForeground(factory.getColor(factory.COLOR_COMPOSITE_SEPARATOR));
		layout.leftMargin = layout.rightMargin = 0;
		layout.horizontalSpacing = 10;
		container = factory.createComposite(parent);
		container.setLayout(layout);
		layout.numColumns = 2;
		textLabel = factory.createLabel(container, "", SWT.WRAP);
		revertButton = factory.createButton(container, "", SWT.PUSH);
		revertButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performRevert();
			}
		});
		return container;
	}
	
	public void configurationChanged(IInstallConfiguration config) {
		this.config = config;
		if (config.isCurrent()) {
			textLabel.setText("This is the current configuration. To revert to the previous one press:");
			revertButton.setText("Revert");
		}
		else {
			textLabel.setText("To restore this configuration, press:");
			revertButton.setText("Restore");
		}
	}
	
	public void performRevert() {
	}
}