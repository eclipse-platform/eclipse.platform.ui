package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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
import org.eclipse.jface.resource.*;
import org.eclipse.jface.dialogs.MessageDialog;
public class DetailsForm extends UpdateWebForm {
private Label imageLabel;
private Label providerLabel;
private Label versionLabel;
private Label installedVersionLabel;
private Label sizeLabel;
private Label osLabel;
private Label wsLabel;
private Label nlLabel;
private Label descriptionText;
private URL infoLinkURL;
private Label infoLinkLabel;
private InfoGroup licenseGroup;
private InfoGroup copyrightGroup;
private ReflowGroup supportedPlatformsGroup;
private Image providerImage;
private Button doButton;
private IFeature currentFeature;
private ModelListener modelListener;
private Hashtable imageCache = new Hashtable();
private HyperlinkHandler sectionHandler;
private boolean alreadyInstalled;

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
	public void expanded() {
		reflow();
		updateSize();
	}
	public void collapsed() {
		reflow();
		updateSize();
	}
}

abstract class ReflowGroup extends ExpandableGroup {
	public void expanded() {
		reflow();
		updateSize();
	}
	public void collapsed() {
		reflow();
		updateSize();
	}
	protected Label createTextLabel(Composite parent, FormWidgetFactory factory) {
		Label label = super.createTextLabel(parent, factory);
		label.setFont(JFaceResources.getBannerFont());
		return label;
	}
	protected HyperlinkHandler getHyperlinkHandler(FormWidgetFactory factory) {
		return sectionHandler;
	}
}

class PageSettings {
	public static final int LICENSE_EXPANDED = 0x2;
	public static final int COPYRIGHT_EXPANDED = 0x4;
	public int flags;
}

public DetailsForm(UpdateFormPage page) {
	super(page);
	providerImage = UpdateUIPluginImages.DESC_PROVIDER.createImage();
	modelListener = new ModelListener();
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	model.addUpdateModelChangedListener(modelListener);
	sectionHandler = new HyperlinkHandler();
}

public void dispose() {
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	model.removeUpdateModelChangedListener(modelListener);
	providerImage.dispose();
	for (Enumeration enum=imageCache.elements(); enum.hasMoreElements();) {
		Image image = (Image)enum.nextElement();
		image.dispose();
	}
	imageCache.clear();
	sectionHandler.dispose();
	super.dispose();
}
	
public void initialize(Object modelObject) {
	setHeadingText("Feature Details");
	setHeadingImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_BANNER));
	setHeadingUnderlineImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_UNDERLINE));
	super.initialize(modelObject);
}

private void configureSectionHandler(FormWidgetFactory factory) {
	sectionHandler.setHyperlinkUnderlineMode(HyperlinkHandler.UNDERLINE_NEVER);
	sectionHandler.setBackground(factory.getBackgroundColor());
	sectionHandler.setForeground(factory.getColor(factory.COLOR_COMPOSITE_SEPARATOR));
}

public void createContents(Composite container) {
	HTMLTableLayout layout = new HTMLTableLayout();
	layout.numColumns = 2;
	container.setLayout(layout);
	layout.rightMargin = 0;
	GridData gd;
	
	configureSectionHandler(factory);
	
	GridLayout glayout = new GridLayout();
	Composite properties = factory.createComposite(container);
	properties.setLayout(glayout);
	glayout.marginWidth = glayout.marginHeight = 0;
	glayout.verticalSpacing = 0;

	providerLabel = createProperty(properties, "Provider");
	versionLabel = createProperty(properties,"\nVersion" );
	installedVersionLabel = createProperty(properties, "\nInstalled Version");
	sizeLabel = createProperty(properties, "\nDownload Size");
	supportedPlatformsGroup = new ReflowGroup () {
		public void fillExpansion(Composite expansion, FormWidgetFactory factory) {
			GridLayout layout = new GridLayout();
  			expansion.setLayout(layout);
   			layout.marginWidth = 0;
		   	osLabel = createProperty(expansion, "Operating System", true);
			wsLabel = createProperty(expansion, "\nWindowing System", true);
			nlLabel = createProperty(expansion, "\nSupported Languages", true);
			
		}
	};
	supportedPlatformsGroup.setText("Supported Platforms");
	new Label(properties, SWT.NULL);
	supportedPlatformsGroup.createControl(properties, factory);
	/*
	Composite sep = factory.createCompositeSeparator(properties);
	sep.setBackground(factory.getBorderColor());
	gd = new GridData(GridData.FILL_HORIZONTAL);
	gd.heightHint = 1;
	sep.setLayoutData(gd);
	*/
	
	imageLabel = factory.createLabel(container, null);
	TableData td = new TableData();
	td.align = TableData.CENTER;
	//td.valign = TableData.MIDDLE;
	imageLabel.setLayoutData(td);
	
	Label label = createHeading(container, "\nDescription", false);
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
	
	Composite l = factory.createCompositeSeparator(container);
	l.setBackground(factory.getBorderColor());
	td = new TableData();
	td.colspan = 2;
	td.heightHint = 1;
	td.align = TableData.FILL;
	l.setLayoutData(td);
		
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
   	gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
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
	return createProperty(parent, name, false);
}

private Label createProperty(Composite parent, String name, boolean subHeading) {
	createHeading(parent, name, subHeading);
	Label label = factory.createLabel(parent, null);
	label.setText("");
	GridData gd = new GridData();
	gd.horizontalIndent = 10;
	label.setLayoutData(gd);
	return label;
}

private Label createHeading(Composite parent, String text, boolean subHeading) {
	Label l = factory.createHeadingLabel(parent, text);
	Color hc;
	/*
	if (subHeading)
	   hc = factory.getBorderColor();
	else
	*/
	   hc = factory.getColor(factory.COLOR_COMPOSITE_SEPARATOR);	
  	l.setForeground(hc);
	return l;
}

public void expandTo(final Object obj) {
	BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
		public void run() {
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
	});
}

private String getInstalledVersion(IFeature feature) {
	alreadyInstalled = false;
	try {
		ILocalSite localSite = SiteManager.getLocalSite();
	   	IInstallConfiguration config = localSite.getCurrentConfiguration();
	   	ISite [] isites = config.getInstallSites();
	   	String id = feature.getIdentifier().getIdentifier();
	   	StringBuffer buf = new StringBuffer();
	   	for (int i=0; i<isites.length; i++) {
			ISite isite = isites[i];
			IFeature[] result = UpdateUIPlugin.searchSite(id, isite);
			for (int j=0; j<result.length; j++) {
				IFeature installedFeature = result[j];
				if (buf.length()>0) 
			   		buf.append(", ");
				buf.append(result[j].getIdentifier().getVersion().toString());
				if (installedFeature.equals(feature)) {
					alreadyInstalled=true;
				}
			}
		}
		if (buf.length()>0)
	   		return buf.toString();
		else
	   		return null;
	}
	catch (CoreException e) {
		return null;
	}
}

private void inputChanged(IFeature feature) {
	boolean newerVersion=false;
	if (currentFeature!=null) {
		saveSettings(currentFeature);
	}
	if (feature==null) feature = currentFeature;
	if (feature==null) return;
	
	setHeadingText(feature.getLabel());
	providerLabel.setText(feature.getProvider());
	versionLabel.setText(feature.getIdentifier().getVersion().toString());
	String installedVersion = getInstalledVersion(feature);
	if (installedVersion==null)
	   installedVersion = "Not installed";
	else
	   newerVersion = true;
	installedVersionLabel.setText(installedVersion);
	sizeLabel.setText("0KB");
	descriptionText.setText(feature.getDescription().getText());
	Image logoImage = loadProviderImage(feature);
	if (logoImage==null)
	   logoImage = providerImage;
	imageLabel.setImage(logoImage);
	infoLinkURL = feature.getDescription().getURL();
	infoLinkLabel.setVisible(infoLinkURL!=null);
	updateButtonText(feature, newerVersion);
	setOS(feature.getOS());
	setWS(feature.getWS());
	setNL(feature.getNL());
	
	licenseGroup.setInfo(feature.getLicense());
	copyrightGroup.setInfo(feature.getCopyright());
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	doButton.setEnabled(!model.checklistContains(feature));
	
	restoreSettings(feature);
	reflow();
	updateSize();
	((Composite)getControl()).redraw();

	currentFeature = feature;
}

private void updateButtonText(IFeature feature, boolean update) {
	if (alreadyInstalled) {
		doButton.setText("&Uninstall");
	}
	else if (update) {
		doButton.setText("&Update");
	}
	else
	  	doButton.setText("&Install");
}

private void restoreSettings(IFeature feature) {
	PageSettings settings = (PageSettings)getSettings(feature);
	if (settings==null) return;
	if ((settings.flags & PageSettings.LICENSE_EXPANDED)!=0)
	   licenseGroup.setExpanded(true);
	if ((settings.flags & PageSettings.COPYRIGHT_EXPANDED)!=0)
	   copyrightGroup.setExpanded(true);
}

private void saveSettings(IFeature feature) {
	PageSettings settings = (PageSettings)getSettings(feature);
	if (settings==null) settings = new PageSettings();
	settings.flags =0;
	if (licenseGroup.isExpanded())
	   settings.flags |= PageSettings.LICENSE_EXPANDED;
	if (copyrightGroup.isExpanded())
	   settings.flags |= PageSettings.COPYRIGHT_EXPANDED;
	setSettings(feature, settings);
}

private Image loadProviderImage(IFeature feature) {
	Image image = null;
	URL imageURL = feature.getImage();
	if (imageURL==null) return null;
	// check table
	image = (Image)imageCache.get(imageURL);
	if (image==null) {
		ImageDescriptor id = ImageDescriptor.createFromURL(imageURL);
		image = id.createImage();
		if (image!=null)
		   imageCache.put(imageURL, image);
	}
	return image;
}

private void reflow() {
	versionLabel.getParent().layout(true);
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
			if (i>0) buf.append("\n");
			buf.append(mapWS(array[i]));
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
			if (i>0) buf.append("\n");
			buf.append(mapNL(array[i]));
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
		if (alreadyInstalled) {
			mode = ChecklistJob.UNINSTALL;
		}
		final ChecklistJob job = new ChecklistJob(currentFeature, mode);
		//UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		//model.addJob(job);
		BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
			public void run() {
				InstallWizard wizard = new InstallWizard(job);
				WizardDialog dialog = new InstallWizardDialog(UpdateUIPlugin.getActiveWorkbenchShell(), wizard);
				dialog.create();
				dialog.getShell().setSize(500, 500);
				dialog.open();
				if (wizard.isSuccessfulInstall()) {
					String title = alreadyInstalled?"Uninstall":"Install";
					String message=alreadyInstalled?
					"The feature has been successfully uninstalled. You will need to restart the workbench to see the effects of the action.":
					"The feature has been successfully installed. You will need to restart the workbench to be able to use it.";
					MessageDialog.openInformation(UpdateUIPlugin.getActiveWorkbenchShell(),
							title,
							message);
				}
			}
		});
	}
}
}