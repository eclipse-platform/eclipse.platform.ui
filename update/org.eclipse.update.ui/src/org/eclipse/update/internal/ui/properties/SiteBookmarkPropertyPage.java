package org.eclipse.update.internal.ui.properties;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.swt.events.*;
import java.net.*;
import org.eclipse.update.internal.ui.UpdateUI;

/**
 * Insert the type's description here.
 * @see PropertyPage
 */
public class SiteBookmarkPropertyPage
	extends PropertyPage
	implements IWorkbenchPropertyPage {
	private static final String KEY_NAME = "SiteBookmarkPropertyPage.name";
	private static final String KEY_ADDRESS = "SiteBookmarkPropertyPage.address";
	private static final String KEY_UPDATE_SITE = "SiteBookmarkPropertyPage.updateSite";
	private static final String KEY_WEB_SITE = "SiteBookmarkPropertyPage.webSite";
	private Text siteName;
	private Text siteURL;
	private Button updateButton;
	private Button webButton;
	private boolean changed;
	private boolean urlChanged;
	private boolean typeChanged;
	/**
	 * The constructor.
	 */
	public SiteBookmarkPropertyPage() {
	}

	/**
	 * Insert the method's description here.
	 * @see PropertyPage#createContents
	 */
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);

		Label label = new Label(container, SWT.NULL);
		label.setText(UpdateUI.getString(KEY_NAME));
		siteName = new Text(container, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		siteName.setLayoutData(gd);
		label = new Label(container, SWT.NULL);
		label.setText(UpdateUI.getString(KEY_ADDRESS));
		siteURL = new Text(container, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		siteURL.setLayoutData(gd);
		
		updateButton = new Button(container, SWT.RADIO);
		updateButton.setText(UpdateUI.getString(KEY_UPDATE_SITE));
		gd = new GridData();
		gd.horizontalSpan = 2;
		updateButton.setLayoutData(gd);
		
		webButton = new Button(container, SWT.RADIO);
		webButton.setText(UpdateUI.getString(KEY_WEB_SITE));
		gd = new GridData();
		gd.horizontalSpan = 2;
		webButton.setLayoutData(gd);
		
		initializeFields();
		WorkbenchHelp.setHelp(container, "org.eclipse.update.ui.SiteBookmarkPropertyPage");
		return container;
	}

	public boolean performOk() {
		if (changed) {
			SiteBookmark site = (SiteBookmark) getElement();
			site.setName(siteName.getText());

			if (urlChanged) {

				try {
					URL url = new URL(siteURL.getText());
					site.setURL(url);
				} catch (MalformedURLException e) {
				}
			}
			if (typeChanged) {
				site.setWebBookmark(webButton.getSelection());
			}
		}
		return true;
	}
	
	public void performDefaults() {
		doInitialize();
		super.performDefaults();
	}
	
	private void doInitialize() {
		SiteBookmark site = (SiteBookmark) getElement();
		siteName.setText(site.getName());
		siteURL.setText(site.getURL().toString());
		siteName.setEnabled(site.getType() != SiteBookmark.LOCAL);
		siteURL.setEnabled(site.getType() == SiteBookmark.USER);
		updateButton.setSelection(site.isWebBookmark()==false);
		updateButton.setEnabled(siteURL.isEnabled());
		webButton.setSelection(site.isWebBookmark());
		webButton.setEnabled(updateButton.isEnabled());
	}

	private void initializeFields() {
		doInitialize();

		siteName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				checkFields();
			}
		});
		siteURL.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				urlChanged = true;
				checkFields();
			}
		});
		updateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				typeChanged = true;
				checkFields();
			}
		});
		webButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				typeChanged = true;
				checkFields();
			}
		});
	}
	
	private void checkFields() {
		boolean valid = true;
		if (siteName.getText().length() == 0)
			valid = false;
		try {
			new URL(siteURL.getText());
		} catch (MalformedURLException e) {
			valid = false;
		}
		setValid(valid);
		changed = true;
	}
}