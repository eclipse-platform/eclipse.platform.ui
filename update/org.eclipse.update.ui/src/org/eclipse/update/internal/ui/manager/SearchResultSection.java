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
import org.eclipse.update.ui.internal.model.*;
import org.eclipse.update.core.IFeature;

public class SearchResultSection extends UpdateSection {
	private static final String KEY_TITLE = "UpdatesPage.SearchResultSection.title";
	private static final String KEY_DESC = "UpdatesPage.SearchResultSection.desc";
	private static final String KEY_NODESC = "UpdatesPage.SearchResultSection.nodesc";
	private Composite resultContainer;
	private FormWidgetFactory factory;
	private int counter = 0;
	private boolean fullMode=false;
	
	public SearchResultSection(UpdateFormPage page) {
		super(page);
		setAddSeparator(false);
		setHeaderText(UpdateUIPlugin.getResourceString(KEY_TITLE));
		setDescription(UpdateUIPlugin.getResourceString(KEY_NODESC));
	}

	public Composite createClient(Composite parent, FormWidgetFactory factory) {
		GridLayout layout = new GridLayout();
		this.factory = factory;
		header.setForeground(factory.getColor(factory.COLOR_COMPOSITE_SEPARATOR));
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		Composite container = factory.createComposite(parent);
		container.setLayout(layout);	
		
		createSeparator(container);
		GridData gd = new GridData(GridData.FILL_BOTH);
		resultContainer = factory.createComposite(container);
		resultContainer.setLayoutData(gd);
		layout = new GridLayout();
		resultContainer.setLayout(layout);
		createSeparator(container);
		initialize();
		return container;
	}
	
	private void createSeparator(Composite parent) {
		Composite sep = factory.createCompositeSeparator(parent);
		sep.setBackground(factory.getBorderColor());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 1;
		sep.setLayoutData(gd);
	}
	
	public void setFullMode(boolean value) {
		if (fullMode!=value) {
			this.fullMode = value;
			reflow();
		}
	}
	
	public void reflow() {
		counter = 0;
		Control [] children = resultContainer.getChildren();
		for (int i=0; i<children.length; i++) {
			children[i].dispose();
		}
		initialize();
		GridData gd = (GridData)resultContainer.getLayoutData();
		if (counter==0)
		   gd.heightHint = 5;
		else
			gd.heightHint = SWT.DEFAULT;

		resultContainer.layout(true);
		resultContainer.getParent().layout(true);
		resultContainer.getParent().getParent().layout(true);
	}
	
	private void initialize() {
		// add children
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		AvailableUpdates updates = model.getUpdates();
		Object [] sites = updates.getChildren(null);
		for (int i=0; i<sites.length; i++) {
			UpdateSearchSite site = (UpdateSearchSite)sites[i];
			Object [] features = site.getChildren(null);
			for (int j=0; j<features.length; j++) {
				IFeature feature = (IFeature)features[j];
				addFeature(feature);
			}
		}
		if (counter>0) {
			String pattern = UpdateUIPlugin.getResourceString(KEY_DESC);
			String desc = UpdateUIPlugin.getFormattedMessage(pattern, (""+counter));
			setDescription(desc);
		}
		else {
			setDescription(UpdateUIPlugin.getResourceString(KEY_NODESC));
		}
	}
	
	private void addFeature(IFeature feature) {
		counter++;
		Label featureLabel = factory.createLabel(resultContainer, null);
		String fullLabel = feature.getLabel();
		fullLabel += " "+feature.getIdentifier().getVersion().toString();
		featureLabel.setText(fullLabel);
	}
}