package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * Page to select keyword substitution mode.
 */
public class KSubstWizardSelectionPage extends CVSWizardPage {
	private KSubstOption ksubst;
	private List ksubstOptions;
	private Button automaticRadioButton;
	private Button binaryRadioButton;
	private Button textRadioButton;
	private Button ksubstRadioButton;
	private Combo ksubstOptionCombo;

	public KSubstWizardSelectionPage(String pageName, String title, ImageDescriptor image, KSubstOption defaultKSubst) {
		super(pageName, title, image);
		this.ksubst = defaultKSubst;

		// sort the options by display text
		KSubstOption[] options = KSubstOption.getAllKSubstOptions();
		this.ksubstOptions = new ArrayList();
		for (int i = 0; i < options.length; i++) {
			KSubstOption option = options[i];
			if (! (Command.KSUBST_BINARY.equals(option) ||
				Command.KSUBST_TEXT.equals(option))) {
				ksubstOptions.add(option);
			}
		}
		Collections.sort(ksubstOptions, new Comparator() {
			public int compare(Object a, Object b) {
				String aKey = ((KSubstOption) a).getLongDisplayText();
				String bKey = ((KSubstOption) b).getLongDisplayText();
				return aKey.compareTo(bKey);
			}
		});
	}
	
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout());
		setControl(top);

		Listener selectionListener = new Listener() {
			public void handleEvent(Event event) {
				updateEnablements();
			}
		};
		
		// Automatic
		automaticRadioButton = createRadioButton(top, Policy.bind("KSubstWizardSelectionPage.automaticButton"), 1); //$NON-NLS-1$
		automaticRadioButton.addListener(SWT.Selection, selectionListener);
		automaticRadioButton.setSelection(ksubst == null);
		createWrappingLabel(top, Policy.bind("KSubstWizardSelectionPage.automaticLabel", //$NON-NLS-1$
			Command.KSUBST_BINARY.getLongDisplayText(),
			CVSProviderPlugin.getPlugin().getDefaultTextKSubstOption().getLongDisplayText()),
			LABEL_INDENT_WIDTH, LABEL_WIDTH_HINT);

		// Binary
		binaryRadioButton = createRadioButton(top, Policy.bind("KSubstWizardSelectionPage.binaryButton"), 1); //$NON-NLS-1$
		binaryRadioButton.addListener(SWT.Selection, selectionListener);
		binaryRadioButton.setSelection(Command.KSUBST_BINARY.equals(ksubst));
		createIndentedLabel(top, Policy.bind("KSubstWizardSelectionPage.binaryLabel"), LABEL_INDENT_WIDTH); //$NON-NLS-1$
		
		// Text without keyword substitution
		textRadioButton = createRadioButton(top, Policy.bind("KSubstWizardSelectionPage.textButton"), 1); //$NON-NLS-1$
		textRadioButton.addListener(SWT.Selection, selectionListener);
		textRadioButton.setSelection(Command.KSUBST_TEXT.equals(ksubst));
		createIndentedLabel(top, Policy.bind("KSubstWizardSelectionPage.textLabel"), LABEL_INDENT_WIDTH); //$NON-NLS-1$
		
		// Text with keyword substitution
		ksubstRadioButton = createRadioButton(top, Policy.bind("KSubstWizardSelectionPage.textWithSubstitutionsButton"), 1); //$NON-NLS-1$
		ksubstRadioButton.addListener(SWT.Selection, selectionListener);
		ksubstRadioButton.setSelection(false);
		createIndentedLabel(top, Policy.bind("KSubstWizardSelectionPage.textWithSubstitutionsLabel"), LABEL_INDENT_WIDTH); //$NON-NLS-1$
		
		ksubstOptionCombo = new Combo(top, SWT.READ_ONLY);
		ksubstOptionCombo.addListener(SWT.Selection, selectionListener);
		GridData data = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_BEGINNING);
		data.horizontalIndent = LABEL_INDENT_WIDTH;
		ksubstOptionCombo.setLayoutData(data);

		// populate the combo box and select the default option
		for (int i = 0; i < ksubstOptions.size(); ++i) {
			KSubstOption option = (KSubstOption) ksubstOptions.get(i);
			ksubstOptionCombo.add(option.getLongDisplayText());
			if (option.equals(ksubst)) {
				ksubstOptionCombo.select(i);
				ksubstRadioButton.setSelection(true);
			} else if (option.equals(Command.KSUBST_TEXT_EXPAND)) {
				// if no expansion mode selected, show KSUBST_TEXT_EXPAND
				// since it is the server default
				if (! ksubstRadioButton.getSelection()) ksubstOptionCombo.select(i);
			}
		}
		updateEnablements();
	}
	
	/**
	 * Enable and disable controls based on the selected radio button.
	 */
	protected void updateEnablements() {
		if (ksubstRadioButton.getSelection()) {
			ksubstOptionCombo.setEnabled(true);
			ksubst = (KSubstOption) ksubstOptions.get(ksubstOptionCombo.getSelectionIndex());
		} else {
			ksubstOptionCombo.setEnabled(false);
			if (automaticRadioButton.getSelection()) {
				ksubst = null;
			} else if (binaryRadioButton.getSelection()) {
				ksubst = Command.KSUBST_BINARY;
			} else if (textRadioButton.getSelection()) {
				ksubst = Command.KSUBST_TEXT;
			}
		}
	}
	
	public KSubstOption getKSubstOption() {
		return ksubst;
	}
}
