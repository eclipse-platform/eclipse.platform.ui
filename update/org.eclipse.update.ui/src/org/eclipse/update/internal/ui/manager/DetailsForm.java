package org.eclipse.update.internal.ui.manager;

import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;
import java.net.URL;
import org.eclipse.update.core.*;
import org.eclipse.update.ui.internal.model.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.custom.*;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.update.internal.ui.wizards.*;
import org.eclipse.jface.wizard.*;
public class DetailsForm extends UpdateWebForm {
private Label imageLabel;
private Label providerLabel;
private Label versionLabel;
private Label sizeLabel;
private Label descriptionText;
private Composite control;
private Label infoLinkLabel;
private Label licenseLinkLabel;
private Label copyrightLinkLabel;
private URL infoLinkURL;
private URL licenseLinkURL;
private URL copyrightLinkURL;
private Image providerImage;
private Button doButton;
private IFeature currentFeature;
private ModelListener modelListener;


class ModelListener implements IUpdateModelChangedListener {
	/**
	 * @see IUpdateModelChangedListener#objectAdded(Object, Object)
	 */
	public void objectAdded(Object parent, Object child) {
		if (child instanceof ChecklistJob) {
			ChecklistJob job = (ChecklistJob)child;
			if (job.getFeature().equals(currentFeature)) {
				doButton.setEnabled(false);
			}
		}
	}

	/**
	 * @see IUpdateModelChangedListener#objectRemoved(Object, Object)
	 */
	public void objectRemoved(Object parent, Object child) {
		if (child instanceof ChecklistJob) {
			ChecklistJob job = (ChecklistJob)child;
			if (job.getFeature().equals(currentFeature)) {
				doButton.setEnabled(true);
			}
		}
	}

	/**
	 * @see IUpdateModelChangedListener#objectChanged(Object, String)
	 */
	public void objectChanged(Object object, String property) {
	}
}


abstract class LinkListener implements IHyperlinkListener {
public abstract URL getURL();
public void linkActivated(Control linkLabel) {
	URL url = getURL();
	if (url!=null) openURL(url.toString());
}
public void linkEntered(Control linkLabel) {
	URL url = getURL();
	if (url!=null)
	   showStatus(url.toString());
}
public void linkExited(Control linkLabel) {
	showStatus(null);
}

private void showStatus(String text) {
	IViewSite site = getPage().getView().getViewSite();
	IStatusLineManager sm = site.getActionBars().getStatusLineManager();
	sm.setMessage(text);
}
}

public DetailsForm(UpdateFormPage page) {
	super(page);
	providerImage = UpdateUIPluginImages.DESC_PROVIDER.createImage();
	modelListener = new ModelListener();
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	model.addUpdateModelChangedListener(modelListener);
}

public void dispose() {
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	model.removeUpdateModelChangedListener(modelListener);
	providerImage.dispose();
	super.dispose();
}
	
public void initialize(Object modelObject) {
	setHeadingText("Feature Details");
	setHeadingImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_BANNER));
	setHeadingUnderlineImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_UNDERLINE));
	super.initialize(modelObject);
}

public void createContents(Composite container) {
	HTMLTableLayout layout = new HTMLTableLayout();
	layout.numColumns = 2;
	container.setLayout(layout);
	layout.rightMargin = 0;
	
	GridLayout glayout = new GridLayout();
	Composite properties = factory.createComposite(container);
	properties.setLayout(glayout);
	glayout.marginWidth = glayout.marginHeight = 0;
	glayout.verticalSpacing = 0;

	createHeading(properties, "Provider");
	providerLabel = factory.createLabel(properties, null, SWT.WRAP);
	createHeading(properties, "\nVersion");
	versionLabel = factory.createLabel(properties, null, SWT.WRAP);
	createHeading(properties, "\nDownload Size");
	sizeLabel = factory.createLabel(properties, null, SWT.WRAP);

	imageLabel = factory.createLabel(container, null);
	TableData td = new TableData();
	td.align = TableData.CENTER;
	td.valign = TableData.MIDDLE;
	imageLabel.setLayoutData(td);
	
	Label label = createHeading(container, "\nDescription");
	td = new TableData();
	td.colspan = 2;
	label.setLayoutData(td);
	descriptionText = factory.createLabel(container, null, SWT.WRAP);
	td = new TableData();
	td.colspan = 2;
	descriptionText.setLayoutData(td);
	
	glayout = new GridLayout();
	glayout.numColumns = 4;
	glayout.horizontalSpacing = 20;
	Composite footer = factory.createComposite(container);
	td = new TableData();
	td.colspan = 2;
	td.align = TableData.FILL;
	td.valign = TableData.FILL;
	footer.setLayoutData(td);
	footer.setLayout(glayout);

	LinkListener listener = new LinkListener() {
		public URL getURL() { return infoLinkURL; }
	};
   	infoLinkLabel = factory.createHyperlinkLabel(footer,
   						"More Info", listener);
	listener = new LinkListener() {
		public URL getURL() { return licenseLinkURL; }
	};
   	licenseLinkLabel = factory.createHyperlinkLabel(footer,
   						"License", listener);
   	listener = new LinkListener() {
   		public URL getURL() { return copyrightLinkURL; }
   	};
   	copyrightLinkLabel = factory.createHyperlinkLabel(footer,
   						"Copyright", listener);
	
  	doButton = factory.createButton(footer, "", SWT.PUSH);
  	doButton.addSelectionListener(new SelectionAdapter() {
  		public void widgetSelected(SelectionEvent e) {
  			doButtonSelected();
  		}
  	});
  	GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
  	gd.grabExcessHorizontalSpace = true;
  	doButton.setLayoutData(gd);
}

Label createHeading(Composite parent, String text) {
	Color hc = factory.getColor(factory.COLOR_COMPOSITE_SEPARATOR);	
	Label l = factory.createHeadingLabel(parent, text);
	l.setForeground(hc);
	return l;
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
		providerLabel.setText("");
		versionLabel.setText("");
		sizeLabel.setText("");
		descriptionText.setText("");
		infoLinkLabel.setVisible(false);
		doButton.setVisible(false);
		imageLabel.setImage(null);
		currentFeature = null;
		return;
	}
	
	setHeadingText(feature.getLabel());
	providerLabel.setText(feature.getProvider());
	versionLabel.setText(feature.getIdentifier().getVersion().toString());
	sizeLabel.setText("0 KB");
	descriptionText.setText(feature.getDescription().getText());
	if (imageLabel.getImage()==null ||
		!imageLabel.getImage().equals(providerImage))
	imageLabel.setImage(providerImage);
	infoLinkURL = feature.getDescription().getURL();
	infoLinkLabel.setVisible(infoLinkURL!=null);
	if (feature.getSite() instanceof ILocalSite) {
		doButton.setText("Uninstall");
	}
	else {
		doButton.setText("Install");
	}
	doButton.getParent().layout(true);
	((Composite)getControl()).layout(true);
	((Composite)getControl()).redraw();

	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	doButton.setEnabled(!model.checklistContains(feature));
	doButton.setVisible(true);
	currentFeature = feature;
}

private void openURL(final String url) {
	BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
		public void run() {
			DetailsView dv = (DetailsView)getPage().getView();
			dv.showURL(url);
		}
	});
}

private void doButtonSelected() {
	if (currentFeature!=null) {
		int mode = ChecklistJob.INSTALL;
		if (currentFeature.getSite() instanceof ILocalSite) {
			mode = ChecklistJob.UNINSTALL;
		}
		final ChecklistJob job = new ChecklistJob(currentFeature, mode);
		//UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		//model.addJob(job);
		BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
			public void run() {
				InstallWizard wizard = new InstallWizard(job);
				WizardDialog dialog = new WizardDialog(UpdateUIPlugin.getActiveWorkbenchShell(), wizard);
				dialog.create();
				dialog.getShell().setSize(500, 500);
				dialog.open();
			}
		});
	}
}
}