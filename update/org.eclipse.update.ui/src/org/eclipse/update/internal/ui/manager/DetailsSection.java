package org.eclipse.update.internal.ui.manager;
import org.eclipse.swt.widgets.*;
import java.net.*;
import org.eclipse.jface.util.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.SWT;
import org.eclipse.jface.resource.*;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.layout.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.core.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.program.Program;
import org.eclipse.update.internal.ui.UpdateUIPluginImages;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.swt.events.*;
import org.eclipse.update.ui.model.*;
import org.eclipse.update.internal.ui.*;

public class DetailsSection extends UpdateSection {
	private Label nameLabel;
	private Label imageLabel;
	private Label providerLabel;
	private Label sizeLabel;
	private StyledText descriptionText;
	private Composite control;
	private Label infoLinkLabel;
	private URL infoLinkURL;
	private Image providerImage;
	private Button doButton;
	private IFeature currentFeature;
	
public DetailsSection(UpdateFormPage page) {
	super(page);
	setHeaderPainted(false);
	providerImage = UpdateUIPluginImages.DESC_PROVIDER.createImage();
}

public final Composite createClient(
	Composite parent,
	FormWidgetFactory factory) {
	Composite container = factory.createComposite(parent);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	container.setLayout(layout);

	nameLabel = factory.createHeadingLabel(container, "");
	nameLabel.setFont(JFaceResources.getFont(JFaceResources.BANNER_FONT));
	GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
	nameLabel.setLayoutData(gd);
	imageLabel = factory.createLabel(container, null);
	gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
	gd.verticalSpan = 4;
	imageLabel.setLayoutData(gd);

	addSection(container, factory, "Provider", 1);
	providerLabel = factory.createLabel(container, "");
	gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
	providerLabel.setLayoutData(gd);

	addSection(container, factory, "Download Size", 2);
	sizeLabel = factory.createLabel(container, "");
	gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
	gd.horizontalSpan = 2;
	sizeLabel.setLayoutData(gd);

	addSection(container, factory, "Description", 2);
	descriptionText = new StyledText(container, SWT.MULTI | SWT.READ_ONLY);
	descriptionText.setCursor(null);
	descriptionText.getCaret().setVisible(false);
	descriptionText.setBackground(factory.getBackgroundColor());
	gd = new GridData(GridData.FILL_HORIZONTAL);
	gd.horizontalSpan = 2;
	descriptionText.setLayoutData(gd); 
   	infoLinkLabel = factory.createHyperlinkLabel(container,
   						"More info", new IHyperlinkListener() {
	   		public void linkActivated(Control linkLabel) {
	   			openInfoURL();
	   		}
			public void linkEntered(Control linkLabel) {
				showStatus(infoLinkURL.toString());
			}
			public void linkExited(Control linkLabel) {
				showStatus(null);
			}
			private void showStatus(String text) {
				IViewSite site = getPage().getView().getViewSite();
				IStatusLineManager sm = site.getActionBars().getStatusLineManager();
				sm.setMessage(text);
			}
	   });
  	gd = new GridData();
  	infoLinkLabel.setLayoutData(gd);
  	
  	doButton = factory.createButton(container, "", SWT.PUSH);
  	doButton.addSelectionListener(new SelectionAdapter() {
  		public void widgetSelected(SelectionEvent e) {
  			doButtonSelected();
  		}
  	});
  	gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
  	doButton.setLayoutData(gd);
	return container;
}

private void addSection(Composite container, FormWidgetFactory factory, String text, int span) {
	Label l = factory.createLabel(container, null);
	GridData gd = new GridData();
	gd.horizontalSpan = span;
	l.setLayoutData(gd);
	l = factory.createLabel(container, text);
	l.setForeground(factory.getColor(factory.COLOR_COMPOSITE_SEPARATOR));
	l.setFont(JFaceResources.getFont(JFaceResources.BANNER_FONT));
	gd = new GridData();
	gd.horizontalSpan = span;
	l.setLayoutData(gd);
}

public void initialize(Object model) {
	inputChanged(null);
}

public void expandTo(Object obj) {
	if (obj instanceof IFeature) {
		inputChanged((IFeature)obj);
	}
	else if (obj instanceof ChecklistJob) {
		inputChanged(((ChecklistJob)obj).getFeature());
	}
	else inputChanged(null);
}

private void inputChanged(IFeature feature) {
	if (feature==null) {
		nameLabel.setText("");
		providerLabel.setText("");
		sizeLabel.setText("");
		descriptionText.setText("");
		infoLinkLabel.setVisible(false);
		doButton.setVisible(false);
		imageLabel.setImage(null);
		currentFeature = null;
		return;
	}
	
	nameLabel.setText(feature.getLabel());
	providerLabel.setText(feature.getProvider());
	sizeLabel.setText("0 KB");
	descriptionText.setText(feature.getDescription());
	if (imageLabel.getImage()==null ||
		!imageLabel.getImage().equals(providerImage))
	imageLabel.setImage(providerImage);
	infoLinkURL = feature.getInfoURL();
	infoLinkLabel.setVisible(infoLinkURL!=null);
	nameLabel.getParent().layout(true);
	((ScrollableForm)getPage().getForm()).update();
	if (feature.getSite() instanceof ILocalSite) {
		doButton.setText("Uninstall");
	}
	else {
		doButton.setText("Install");
	}
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	doButton.setEnabled(!model.checklistContains(feature));
	doButton.setVisible(true);
	currentFeature = feature;
}

private void openInfoURL() {
	if (infoLinkURL!=null) {
		final String fileName = infoLinkURL.toString();
		BusyIndicator.showWhile(nameLabel.getDisplay(), new Runnable() {
			public void run() {
				DetailsView dv = (DetailsView)getPage().getView();
				dv.showURL(fileName);
			}
		});
	}
}

private void doButtonSelected() {
	if (currentFeature!=null) {
		int mode = ChecklistJob.INSTALL;
		if (currentFeature.getSite() instanceof ILocalSite) {
			mode = ChecklistJob.UNINSTALL;
		}
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		model.addJob(new ChecklistJob(currentFeature, mode));
		doButton.setEnabled(false);
	}
}
}