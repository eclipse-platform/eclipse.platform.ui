package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.AboutInfo;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.PlatformInfo;
import org.eclipse.ui.internal.ProductInfo;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Displays information about the product.
 *
 * @private
 *		This class is internal to the workbench and must not be called outside the workbench
 */
public class AboutDialog extends Dialog {
	private	Image 			image;	//image to display on dialog
	private  	AboutInfo     	aboutInfo;
	private	PlatformInfo 	platformInfo;	//the platform info
	private	ProductInfo 	productInfo;	//the product info
	private ArrayList images = new ArrayList();
/**
 * Create an instance of the AboutDialog
 */
public AboutDialog(Shell parentShell) {
	super(parentShell);
	Workbench workbench = (Workbench)PlatformUI.getWorkbench();
	aboutInfo = workbench.getAboutInfo();
	platformInfo = workbench.getPlatformInfo();
	productInfo = workbench.getProductInfo();
}

public boolean close() {
	//get rid of the image that was displayed on the left-hand side of the Welcome dialog
	if (image != null)
		image.dispose();
	for (int i = 0; i < images.size(); i++) {
		((Image)images.get(i)).dispose();
	}
	return super.close();
}
/* (non-Javadoc)
 * Method declared on Window.
 */
protected void configureShell(Shell newShell) {
	super.configureShell(newShell);
	String name = aboutInfo.getProductName();
	if (name == null) {
		// backward compatibility
		name = productInfo.getName();
	}
	if (name != null)
		newShell.setText(WorkbenchMessages.format("AboutDialog.shellTitle", new Object[] {productInfo.getName()})); //$NON-NLS-1$
	WorkbenchHelp.setHelp(newShell, IHelpContextIds.ABOUT_DIALOG);
}
/**
 * Add buttons to the dialog's button bar.
 *
 * Subclasses should override.
 *
 * @param parent the button bar composite
 */
protected void createButtonsForButtonBar(Composite parent) {
	createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
}
/**
 * Creates and returns the contents of the upper part 
 * of the dialog (above the button bar).
 *
 * Subclasses should overide.
 *
 * @param the parent composite to contain the dialog area
 * @return the dialog area control
 */
protected Control createDialogArea(Composite parent) {

	ImageDescriptor imageDescriptor =  aboutInfo.getAboutImage();	// may be null
	if (imageDescriptor != null) 
		image = imageDescriptor.createImage();
	if (image == null) {
		// backward compatibility
		image =  productInfo.getAboutImage();	// may be null
	}
		
	// page group
	Composite outer = (Composite)super.createDialogArea(parent);
	outer.setSize(outer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	GridLayout layout = new GridLayout();
	outer.setLayout(layout);
	outer.setLayoutData(new GridData(GridData.FILL_BOTH));

	// the image & text	
	Composite topContainer = new Composite(outer, SWT.NONE);
	layout = new GridLayout();
	layout.numColumns = (image == null ? 1: 2);
	layout.marginWidth = 0;
	topContainer.setLayout(layout);
	GridData data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.grabExcessHorizontalSpace = true;
	topContainer.setLayoutData(data);

	//image on left side of dialog
	if (image != null) {
		Label imageLabel = new Label(topContainer, SWT.NONE);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		data.grabExcessHorizontalSpace = true;
		imageLabel.setLayoutData(data);
		imageLabel.setImage(image);
	}
	
	// text on the right
	Label label = new Label(topContainer, SWT.LEFT | SWT.WRAP );
	data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.verticalAlignment = GridData.BEGINNING;
	label.setText(productText());
	label.setLayoutData(data);
	label.setFont(parent.getFont());

	// horizontal bar
	Label bar =  new Label(outer, SWT.HORIZONTAL | SWT.SEPARATOR);
	data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	bar.setLayoutData(data);
	
	// feature images
	Composite featureContainer = new Composite(outer, SWT.NONE);
	RowLayout rowLayout = new RowLayout();
	rowLayout.wrap = true;
	featureContainer.setLayout(rowLayout);
	data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	featureContainer.setLayoutData(data);
	
	Workbench workbench = (Workbench)PlatformUI.getWorkbench();
	final AboutInfo[] infoArray = workbench.getFeaturesInfo();
	for (int i = 0; i < infoArray.length; i++) {
		ImageDescriptor desc = infoArray[i].getFeatureImage();
		Image image = null;
		if (desc != null) {
			Button button = new Button(featureContainer, SWT.FLAT | SWT.PUSH);
			button.setData(infoArray[i]);
			image = desc.createImage();
			images.add(image);
			button.setImage(image);
			String name = infoArray[i].getProductName();
			if (name == null)
				name = "";
			button.setToolTipText(name);
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					AboutFeaturesDialog d = new AboutFeaturesDialog(getShell());
					d.setInitialSelection((AboutInfo)event.widget.getData());
					d.open();
				}
			});
		}
	}
	
	// horizontal bar
	bar =  new Label(outer, SWT.HORIZONTAL | SWT.SEPARATOR);
	data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	bar.setLayoutData(data);
	
	// button composite
	Composite buttonComposite = new Composite(outer, SWT.NONE);

	// create a layout with spacing and margins appropriate for the font size.
	layout = new GridLayout();
	layout.numColumns = 2; // this is incremented by createButton
	layout.makeColumnsEqualWidth = true;
	layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
	layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
	layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
	layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
	buttonComposite.setLayout(layout);
	data = new GridData();
	data.horizontalAlignment = GridData.BEGINNING;
	bar.setLayoutData(data);
	buttonComposite.setLayoutData(data);

	Button button = new Button(buttonComposite, SWT.PUSH);
	button.setText(WorkbenchMessages.getString("AboutDialog.featureInfo")); //$NON-NLS-1$
	data = new GridData();
	data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
	int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
	data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	button.setLayoutData(data);
	button.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			new AboutFeaturesDialog(getShell()).open();
		}
	});
	
	button = new Button(buttonComposite, SWT.PUSH);
	button.setText(WorkbenchMessages.getString("AboutDialog.pluginInfo")); //$NON-NLS-1$
	data = new GridData();
	data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
	widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
	data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	button.setLayoutData(data);
	button.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			new AboutPluginsDialog(getShell()).open();
		}
	});

	return outer;
}
/**
 * Answer the product text to show on the right side of the dialog.
 */ 
protected String productText() {
	String text = aboutInfo.getAboutText();
	if (text != null)
		return text;
	// backward compatibility	
	if (productInfo.getBuildID().length() == 0) {
		return WorkbenchMessages.format("AboutText.withoutBuildNumber", new Object[] {productInfo.getDetailedName(),productInfo.getVersion(),productInfo.getCopyright()}); //$NON-NLS-1$
	} else {
		return WorkbenchMessages.format("AboutText.withBuildNumber", new Object[] {productInfo.getDetailedName(),productInfo.getVersion(),productInfo.getBuildID(),productInfo.getCopyright()}); //$NON-NLS-1$
	}
}
	
/**
 * Answer the platform text to show on the right side of the dialog.
 */ 
protected String platformText() {
	if (platformInfo.getBuildID().length() == 0) {
		return WorkbenchMessages.format("AboutText.withoutBuildNumber", new Object[] {platformInfo.getDetailedName(),platformInfo.getVersion(),platformInfo.getCopyright()}); //$NON-NLS-1$
	} else {
		return WorkbenchMessages.format("AboutText.withBuildNumber", new Object[] {platformInfo.getDetailedName(),platformInfo.getVersion(),platformInfo.getBuildID(),platformInfo.getCopyright()}); //$NON-NLS-1$
	}
}

}
