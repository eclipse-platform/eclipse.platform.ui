package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.AboutInfo;
import org.eclipse.ui.internal.AboutItem;
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
	private static final String ATT_HTTP = "http://"; //$NON-NLS-1$
	private	Image 			image;	//image to display on dialog
	private  	AboutInfo     	aboutInfo;
	private	PlatformInfo 	platformInfo;	//the platform info
	private	ProductInfo 	productInfo;	//the product info
	private 	ArrayList images = new ArrayList();
	private 	AboutItem item;
	private	int MAX_IMAGE_WIDTH_FOR_TEXT = 250;
	private    int ABOUT_TEXT_WIDTH = 70; // chars
	private    int ABOUT_TEXT_HEIGHT = 15; // chars
	private 	Cursor handCursor;
	private 	Cursor busyCursor;
	private 	boolean webBrowserOpened;
	
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
/**
 * Adds listeners to the given styled text
 */
private void addListeners(StyledText styledText) {
	styledText.addMouseListener(new MouseAdapter() {
		public void mouseUp(MouseEvent e) {
			StyledText text = (StyledText)e.widget;
			int offset = text.getCaretOffset();
			if (item != null && item.isLinkAt(offset)) {	
				text.setCursor(busyCursor);
				openLink(item.getLinkAt(offset));
				text.setCursor(null);
			}
		}
	});
	styledText.addMouseMoveListener(new MouseMoveListener() {
		public void mouseMove(MouseEvent e) {
			StyledText text = (StyledText)e.widget;
			int offset = -1;
			try {
				offset = text.getOffsetAtLocation(new Point(e.x, e.y));
			} catch (IllegalArgumentException ex) {
				// leave value as -1
			}
			if (offset == -1)
				text.setCursor(null);
			else if (item != null && item.isLinkAt(offset)) 
				text.setCursor(handCursor);
			else 
				text.setCursor(null);
		}
	});
	
	styledText.addTraverseListener(new TraverseListener() {
		public void keyTraversed(TraverseEvent e) {
			if (e.detail == SWT.TRAVERSE_ESCAPE || e.detail == SWT.TRAVERSE_RETURN ||
				e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
					e.doit = true;
			}
		}
	});
	
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
	Button b = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	b.setFocus();
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
	handCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
	busyCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT);
	getShell().addDisposeListener(new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			handCursor.dispose();
			busyCursor.dispose();
		}
	});
	
	ImageDescriptor imageDescriptor =  aboutInfo.getAboutImage();	// may be null
	if (imageDescriptor != null) 
		image = imageDescriptor.createImage();
	if (image == null) {
		// backward compatibility
		image =  productInfo.getAboutImage();	// may be null
	}
	if (image == null || image.getBounds().width <= MAX_IMAGE_WIDTH_FOR_TEXT) {
		// show text
		String aboutText = aboutInfo.getAboutText();
		if (aboutText != null) {
			// get an about item
			item = scan(aboutText);
		}
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
	layout.numColumns = (image == null || item == null ? 1 : 2);
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
		data.grabExcessHorizontalSpace = false;
		imageLabel.setLayoutData(data);
		imageLabel.setImage(image);
	}
	
	if (item != null) {
		// text on the right
		StyledText styledText = new StyledText(topContainer, SWT.MULTI | SWT.READ_ONLY);
		styledText.setCaret(null);
		styledText.setFont(parent.getFont());
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		data.grabExcessHorizontalSpace = true;
		data.widthHint = convertWidthInCharsToPixels(ABOUT_TEXT_WIDTH);
		data.heightHint = convertHeightInCharsToPixels(ABOUT_TEXT_HEIGHT);
		styledText.setText(item.getText());
		styledText.setLayoutData(data);
		styledText.setCursor(null);
		styledText.setBackground(topContainer.getBackground());
		setLinkRanges(styledText, item.getLinkRanges());
		addListeners(styledText);
	}

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
	final AboutInfo[] infoArray = getFeaturesInfo();
	for (int i = 0; i < infoArray.length; i++) {
		ImageDescriptor desc = infoArray[i].getFeatureImage();
		Image image = null;
		if (desc != null) {
			Button button = new Button(featureContainer, SWT.FLAT | SWT.PUSH);
			button.setData(infoArray[i]);
			image = desc.createImage();
			images.add(image);
			button.setImage(image);
			String name = infoArray[i].getFeatureLabel();
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
 * Scan the contents of the about text
 */
private AboutItem scan(String s) {
	int max = s.length();
	int i = s.indexOf(ATT_HTTP);
	ArrayList linkRanges = new ArrayList();
	ArrayList links = new ArrayList();
	while (i != -1) {
		int start = i;
		// look for the first whitespace character
		boolean found = false;
		i += ATT_HTTP.length();
		while (!found && i < max) {
			found = Character.isWhitespace(s.charAt(i++));
		}
		linkRanges.add(new int[] {start, i - start});
		links.add(s.substring(start, i));
		i = s.indexOf(ATT_HTTP, i);
	}
	return new AboutItem(
			s,
			(int[][])linkRanges.toArray(new int[linkRanges.size()][2]),
			(String[])links.toArray(new String[links.size()]));
}


/**
 * Returns the feature infos ensuring that none have duplicate icons and
 * excluding the primary feature.
 */
private AboutInfo[] getFeaturesInfo() {
	AboutInfo[] rawArray = ((Workbench)PlatformUI.getWorkbench()).getFeaturesInfo();
	// quickly exclude any that do not have an image
	ArrayList infoList = new ArrayList();
	for (int i = 0; i < rawArray.length; i++) {
		if (rawArray[i].getFeatureImageName() != null)
			infoList.add(rawArray[i]); 
	}
	AboutInfo[] infoArray = (AboutInfo[])infoList.toArray(new AboutInfo[infoList.size()]);
	
	// now exclude those with duplicate images
	infoList = new ArrayList();
	if (aboutInfo != null 
		&& aboutInfo.getFeatureImageName() != null)
		// add the primary feature now, we will remove it later
		infoList.add(aboutInfo);
	for (int i = 0; i < infoArray.length; i++) {
		if (infoArray[i].getFeatureImageName() == null) 
			break;
		// check for identical name
		boolean add = true;
		for (int j = 0; j < infoList.size(); j++) {
			AboutInfo current = (AboutInfo)infoList.get(j);
			if (current.getFeatureImageName().equals(infoArray[i].getFeatureImageName())) {
				// same name
				// we have to check if the CRC's are identical
				Long crc1 = current.getFeatureImageCRC();
				Long crc2 = infoArray[i].getFeatureImageCRC();
				if (crc1 == null ? false : crc1.equals(crc2)) {
					// duplicate
					add = false;
					break;
				}
			}
		}
		if (add)
			infoList.add(infoArray[i]);
	}	
	infoList.remove(aboutInfo);
	return (AboutInfo[])infoList.toArray(new AboutInfo[infoList.size()]);
}


/**
 * Open a link
 */
private void openLink(final String href) {
	if (SWT.getPlatform().equals("win32")) { //$NON-NLS-1$
		Program.launch(href);
	} else {
			Thread launcher = new Thread("About Link Launcher") {//$NON-NLS-1$
	public void run() {
				try {
					if (webBrowserOpened) {
						Runtime.getRuntime().exec("netscape -remote openURL(" + href + ")"); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						Process p = Runtime.getRuntime().exec("netscape " + href); //$NON-NLS-1$
						webBrowserOpened = true;
						try {
							if (p != null)
								p.waitFor();
						} catch (InterruptedException e) {
							MessageDialog.openError(AboutDialog.this.getShell(), WorkbenchMessages.getString("AboutDialog.errorTitle"), //$NON-NLS-1$
							e.getMessage());
						} finally {
							webBrowserOpened = false;
						}
					}
				} catch (IOException e) {
					MessageDialog.openError(AboutDialog.this.getShell(), WorkbenchMessages.getString("AboutDialog.errorTitle"), //$NON-NLS-1$
					e.getMessage());

				}
			}
		};
		launcher.start();
	}
}

/**
 * Answer the product text to show on the right side of the dialog.
 */ 
private String getAboutText() {
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
 * Sets the styled text's bold ranges
 */
private void setBoldRanges(StyledText styledText, int[][] boldRanges) {
	for (int i = 0; i < boldRanges.length; i++) {
		StyleRange r = new StyleRange(boldRanges[i][0], boldRanges[i][1], null, null, SWT.BOLD);
		styledText.setStyleRange(r);
	}
}
/**
 * Sets the styled text's link (blue) ranges
 */
private void setLinkRanges(StyledText styledText, int[][] linkRanges) {
	Color fg = JFaceColors.getHyperlinkText(styledText.getShell().getDisplay());
	for (int i = 0; i < linkRanges.length; i++) {
		StyleRange r = new StyleRange(linkRanges[i][0], linkRanges[i][1], fg, null);
		styledText.setStyleRange(r);
	}
}


}
