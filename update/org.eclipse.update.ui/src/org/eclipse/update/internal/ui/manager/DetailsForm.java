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
import java.util.*;
import org.eclipse.core.runtime.CoreException;
public class DetailsForm extends UpdateWebForm {
private Label imageLabel;
private Label providerLabel;
private Label versionLabel;
private Label sizeLabel;
private Label osLabel;
private Label wsLabel;
private Label nlLabel;
private Label descriptionText;
private Composite control;
private URL infoLinkURL;
private Label infoLinkLabel;
private InfoGroup licenseGroup;
private InfoGroup copyrightGroup;
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

class ReflowInfoGroup extends InfoGroup {
	public ReflowInfoGroup(DetailsView view) {
		super(view);
	}
	
	protected URL resolveURL(URL inputURL) {
		try {
			return org.eclipse.update.internal.core.UpdateManagerUtils.resolveAsLocal(inputURL);
		}
		catch (Exception e) {
			return inputURL;
		}
	}
	public void expanded() {
		reflow();
		updateSize();
	}
	public void collapsed() {
		reflow();
		updateSize();
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

	providerLabel = createProperty(properties, "Provider");
	versionLabel = createProperty(properties,"\nVersion" );
	sizeLabel = createProperty(properties, "\nDownload Size");
	osLabel = createProperty(properties, "\nOperating System");
	wsLabel = createProperty(properties, "\nWindowing System");
	nlLabel = createProperty(properties, "\nSupported Languages");

	imageLabel = factory.createLabel(container, null);
	TableData td = new TableData();
	td.align = TableData.CENTER;
	//td.valign = TableData.MIDDLE;
	imageLabel.setLayoutData(td);
	
	Label label = createHeading(container, "\nDescription");
	td = new TableData();
	td.colspan = 2;
	label.setLayoutData(td);
	descriptionText = factory.createLabel(container, null, SWT.WRAP);
	td = new TableData();
	td.colspan = 2;
	td.grabHorizontal = true;
	descriptionText.setLayoutData(td);
	
	glayout = new GridLayout();
	glayout.numColumns = 4;
	glayout.horizontalSpacing = 20;
	glayout.marginWidth = 10;
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
   	GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
   	infoLinkLabel.setLayoutData(gd);
   	licenseGroup = new ReflowInfoGroup((DetailsView)getPage().getView());
   	licenseGroup.setText("License");
   	licenseGroup.createControl(footer, factory);
    gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
   	licenseGroup.getControl().setLayoutData(gd);
   	copyrightGroup = new ReflowInfoGroup((DetailsView)getPage().getView());
   	copyrightGroup.setText("Copyright");
   	copyrightGroup.createControl(footer, factory);
   	gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
   	copyrightGroup.getControl().setLayoutData(gd);

  	doButton = factory.createButton(footer, "", SWT.PUSH);
  	doButton.addSelectionListener(new SelectionAdapter() {
  		public void widgetSelected(SelectionEvent e) {
  			doButtonSelected();
  		}
  	});
  	gd = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING);
  	gd.grabExcessHorizontalSpace = true;
  	doButton.setLayoutData(gd);
}


private Label createProperty(Composite parent, String name) {
	createHeading(parent, name);
	Label label = factory.createLabel(parent, null);
	GridData gd = new GridData();
	gd.horizontalIndent = 10;
	label.setLayoutData(gd);
	return label;
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
	else if (obj instanceof CategorizedFeature) {
		try {
			IFeature feature = ((CategorizedFeature)obj).getFeature();
			inputChanged(feature);
		}
		catch (CoreException e) {
			UpdateUIPlugin.logException(e);
		}

	}
	else if (obj instanceof ChecklistJob) {
		inputChanged(((ChecklistJob)obj).getFeature());
	}
	else inputChanged(null);
}

private void inputChanged(IFeature feature) {
/*
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
*/
	if (feature==null) feature = currentFeature;
	if (feature==null) return;
	
	setHeadingText(feature.getLabel());
	providerLabel.setText(feature.getProvider());
	versionLabel.setText(feature.getIdentifier().getVersion().toString());
	sizeLabel.setText("0 KB");
	descriptionText.setText(feature.getDescription().getText());
	/* if (imageLabel.getImage()==null ||
		!imageLabel.getImage().equals(providerImage))
		*/
	imageLabel.setImage(providerImage);
	infoLinkURL = feature.getDescription().getURL();
	//Temp. - should not use internal classes
	try {
		infoLinkURL = org.eclipse.update.internal.core.UpdateManagerUtils.resolveAsLocal(infoLinkURL);
	}
	catch (Exception e) {
	}
	infoLinkLabel.setVisible(infoLinkURL!=null);
	if (feature.getSite() instanceof ILocalSite) {
		doButton.setText("Uninstall");
	}
	else {
		doButton.setText("Install");
	}
	setOS(feature.getOS());
	setWS(feature.getWS());
	setNL(feature.getNL());
	
	licenseGroup.setInfo(feature.getLicense());
	copyrightGroup.setInfo(feature.getCopyright());
	reflow();
	updateSize();
	((Composite)getControl()).redraw();

	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	doButton.setEnabled(!model.checklistContains(feature));
	doButton.setVisible(true);

	currentFeature = feature;
}

private void reflow() {
	doButton.getParent().layout(true);
	imageLabel.getParent().layout(true);
	((Composite)getControl()).layout(true);
}

private void setOS(String os) {
	if (os==null) osLabel.setText("");
	else {
		String [] array = getTokens(os);
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<array.length; i++) {
			if (i>0) buf.append("\n");
			buf.append(mapOS(array[i]));
		}
		osLabel.setText(buf.toString());
	}
}

private String mapOS(String key) {
	if (key.equals("OS_WIN32"))
	   return "Windows";
	if (key.equals("OS_LINUX"))
	   return "Linux";
	return key;
}

private String mapWS(String key) {
	if (key.equals("WS_WIN32"))
	   return "Windows";
	if (key.equals("WS_MOTIF"))
	   return "Motif";
	if (key.equals("WS_GTK"))
	   return "GTK";
	return key;
}

private String mapNL(String nl) {
	String language, country;
	
	int loc = nl.indexOf('_');
	if (loc != -1) {
		language = nl.substring(0, loc);
		country = nl.substring(loc+1);
	}
	else {
		language = nl;
		country = "";
	}
	Locale locale = new Locale(language, country);
	return locale.getDisplayName();
}

private void setWS(String ws) {
	if (ws==null) wsLabel.setText("");
	else {
		String [] array = getTokens(ws);
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<array.length; i++) {
			buf.append(mapWS(array[i])+"\n");
		}
		wsLabel.setText(buf.toString());
	}
}

private void setNL(String nl) {
	if (nl==null) nlLabel.setText("");
	else {
		String [] array = getTokens(nl);
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<array.length; i++) {
			buf.append(mapNL(array[i])+"\n");
		}
		nlLabel.setText(buf.toString());
	}
}

private String [] getTokens(String source) {
	Vector result = new Vector();
	StringTokenizer stok = new StringTokenizer(source, ",");
	while (stok.hasMoreTokens()) {
		String tok = stok.nextToken();
		result.add(tok);
	}
	return (String [])result.toArray(new String[result.size()]);
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