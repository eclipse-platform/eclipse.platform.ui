/*
 * Created on Apr 17, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.ui.wizards;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.search.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.search.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ModeSelectionPage extends BannerPage implements ISearchProvider {
	private boolean updateMode=true;
	private Button updatesButton;
	private Button newFeaturesButton;
	private UpdateSearchRequest searchRequest;
	private SearchRunner searchRunner;
	private static final String SECTION_ID = "ModeSelectionPage"; //$NON-NLS-1$
	private static final String P_NEW_FEATURES_MODE = "new-features-mode"; //$NON-NLS-1$
	
	public ModeSelectionPage(SearchRunner searchRunner) {
		super("modeSelection"); //$NON-NLS-1$
		setTitle(UpdateUI.getString("ModeSelectionPage.title")); //$NON-NLS-1$
		setDescription(UpdateUI.getString("ModeSelectionPage.desc")); //$NON-NLS-1$
		this.searchRunner = searchRunner;
	}
	
	public UpdateSearchRequest getSearchRequest() {
		initializeSearch();
		return searchRequest;
	}
	
	private IDialogSettings getSettings() {
		IDialogSettings master = UpdateUI.getDefault().getDialogSettings();
		IDialogSettings section = master.getSection(SECTION_ID);
		if (section==null)
			section = master.addNewSection(SECTION_ID);
		return section;
	}

	private void initializeSearch() {
		if (searchRequest!=null) return;
		UpdateSearchScope scope = new UpdateSearchScope();
		scope.setUpdateMapURL(UpdateUtils.getUpdateMapURL());
		UpdatesSearchCategory category = new UpdatesSearchCategory();
		searchRequest = new UpdateSearchRequest(category, scope);
		searchRequest.addFilter(new EnvironmentFilter());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.internal.ui.wizards.BannerPage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		updatesButton = new Button(composite, SWT.RADIO);
		updatesButton.setText(UpdateUI.getString("ModeSelectionPage.updates")); //$NON-NLS-1$
		boolean newFeaturesMode = getSettings().getBoolean(P_NEW_FEATURES_MODE);;
		updatesButton.setSelection(!newFeaturesMode);
		updatesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				switchMode();
			}
		});
		newFeaturesButton = new Button(composite, SWT.RADIO);
		newFeaturesButton.setSelection(newFeaturesMode);
		newFeaturesButton.setText(UpdateUI.getString("ModeSelectionPage.newFeatures")); //$NON-NLS-1$
		newFeaturesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				switchMode();
			}
		});		
		switchMode();
		
		Dialog.applyDialogFont(parent);
		
		return composite;
	}
	
	public void saveSettings() {
		boolean updateMode = updatesButton.getSelection();
		getSettings().put(P_NEW_FEATURES_MODE, !updateMode);
	}
	
	private void switchMode() {
		updateMode = updatesButton.getSelection();
		if (updateMode)
			searchRunner.setSearchProvider(this);
	}
	
	public boolean isUpdateMode() {
		return updateMode;
	}
}
