/*
 * Created on Apr 17, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.ui.wizards;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ModeSelectionPage extends BannerPage {
	private boolean updateMode=true;
	private Button updatesButton;
	private Button newFeaturesButton;
	
	public ModeSelectionPage() {
		super("modeSelection");
		setTitle("Feature Updates");
		setDescription("Choose the way you want to search for features to install");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.internal.ui.wizards.BannerPage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		updatesButton = new Button(composite, SWT.RADIO);
		updatesButton.setText("Search for updates of the currently installed features");
		updatesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				switchMode();
			}
		});
		newFeaturesButton = new Button(composite, SWT.RADIO);
		newFeaturesButton.setText("Search for new features");
		newFeaturesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				switchMode();
			}
		});		
		return composite;
	}
	
	private void switchMode() {
		updateMode = updatesButton.getSelection();
	}
	
	public boolean isUpdateMode() {
		return updateMode;
	}

}
