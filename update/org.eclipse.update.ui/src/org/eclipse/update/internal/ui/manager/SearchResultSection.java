package org.eclipse.update.internal.ui.manager;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
import org.eclipse.update.internal.ui.*;
import org.eclipse.jface.operation.IRunnableWithProgress;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.update.ui.internal.model.*;
import org.eclipse.update.core.IFeature;
import org.eclipse.swt.graphics.Image;

public class SearchResultSection extends UpdateSection 
		implements IHyperlinkListener {
	private static final String KEY_TITLE = "UpdatesPage.SearchResultSection.title";
	private static final String KEY_DESC = "UpdatesPage.SearchResultSection.desc";
	private static final String KEY_NODESC = "UpdatesPage.SearchResultSection.nodesc";
	private Composite container;
	private FormWidgetFactory factory;
	private int counter = 0;
	private boolean fullMode=false;
	private Image featureImage;
	
	public SearchResultSection(UpdateFormPage page) {
		super(page);
		setAddSeparator(false);
		setHeaderText(UpdateUIPlugin.getResourceString(KEY_TITLE));
		setDescription(UpdateUIPlugin.getResourceString(KEY_NODESC));
		featureImage = UpdateUIPluginImages.DESC_FEATURE_OBJ.createImage();
	}

	public Composite createClient(Composite parent, FormWidgetFactory factory) {
		HTMLTableLayout layout = new HTMLTableLayout();
		this.factory = factory;
		header.setForeground(factory.getColor(factory.COLOR_COMPOSITE_SEPARATOR));
		layout.leftMargin = 10;
		layout.rightMargin = 10;
		layout.horizontalSpacing = 0;
		container = factory.createComposite(parent);
		container.setLayout(layout);	
		layout.numColumns = 2;
		initialize();
		return container;
	}
	
	public void dispose() {
		featureImage.dispose();
		super.dispose();
	}
	
	public void setFullMode(boolean value) {
		if (fullMode!=value) {
			this.fullMode = value;
			if (container!=null) reflow();
		}
	}
	
	public void reflow() {
		counter = 0;
		Control [] children = container.getChildren();
		for (int i=0; i<children.length; i++) {
			children[i].dispose();
		}
		initialize();
		container.layout(true);
		container.getParent().layout(true);
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
		Label imageLabel = factory.createLabel(container, null);
		imageLabel.setImage(featureImage);
		SelectableFormLabel featureLabel = new SelectableFormLabel(container, SWT.WRAP);
		featureLabel.setText(getFeatureLabel(feature));
		featureLabel.setData(feature);
		factory.turnIntoHyperlink(featureLabel, this);
		if (fullMode) {
			factory.createLabel(container, null);
			Label label = factory.createLabel(container, null);
			label.setText("by "+feature.getProvider());
			factory.createLabel(container, null);
			IURLEntry desc = feature.getDescription();
			if (desc != null) {
				String text = desc.getAnnotation();
				if (text!=null)
					factory.createLabel(container, text, SWT.WRAP);
			}
		}
	}
	
	private String getFeatureLabel(IFeature feature) {
		String fullLabel = feature.getLabel();
		return feature.getLabel()+" "+
			feature.getVersionIdentifier().getVersion().toString();
	}
	/*
	 * @see IHyperlinkListener#linkActivated(Control)
	 */
	public void linkActivated(Control linkLabel) {
		Object data = linkLabel.getData();
		if (data instanceof IFeature) {
			DetailsView view = (DetailsView)getPage().getView();
			view.showPageWithInput(DetailsView.DETAILS_PAGE, data);
		}
	}

	/*
	 * @see IHyperlinkListener#linkEntered(Control)
	 */
	public void linkEntered(Control linkLabel) {
	}

	/*
	 * @see IHyperlinkListener#linkExited(Control)
	 */
	public void linkExited(Control linkLabel) {
	}

}