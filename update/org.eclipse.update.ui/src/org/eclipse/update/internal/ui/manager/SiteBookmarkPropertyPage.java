package org.eclipse.update.internal.ui.manager;

import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.update.ui.internal.model.*;
import org.eclipse.swt.events.*;
import java.net.*;

/**
 * Insert the type's description here.
 * @see PropertyPage
 */
public class SiteBookmarkPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
	private Text siteName;
	private Text siteURL;
	private boolean changed;
	/**
	 * The constructor.
	 */
	public SiteBookmarkPropertyPage() {
	}

	/**
	 * Insert the method's description here.
	 * @see PropertyPage#createContents
	 */
	protected Control createContents(Composite parent)  {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		
		Label label = new Label(container, SWT.NULL);
		label.setText("Name:");
		siteName = new Text(container, SWT.SINGLE|SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		siteName.setLayoutData(gd);
		label = new Label(container, SWT.NULL);
		label.setText("Address:");
		siteURL = new Text(container, SWT.SINGLE|SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		siteURL.setLayoutData(gd);
		initializeFields();
		return container;
	}
	
	public boolean performOk() {
		if (changed) {
			SiteBookmark site = (SiteBookmark)getElement();
			site.setName(siteName.getText());
		
			try {
				URL url = new URL(siteURL.getText());
				site.setURL(url);
			}
			catch (MalformedURLException e) {
			}
		}
		return true;
	}
	
	private void initializeFields() {
		SiteBookmark site = (SiteBookmark)getElement();
		siteName.setText(site.getName());
		siteURL.setText(site.getURL().toString());
		siteName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				checkFields();
			}
		});
		siteURL.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				checkFields();
			}
		});
	}
	private void checkFields() {
		boolean valid = true;
		if (siteName.getText().length()==0) valid = false;
		try {
			new URL(siteURL.getText());
		}
		catch (MalformedURLException e) {
			valid = false;
		}
		setValid(valid);
		changed=true;
	}
}
