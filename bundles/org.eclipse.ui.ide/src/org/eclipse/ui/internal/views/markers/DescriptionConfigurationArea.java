package org.eclipse.ui.internal.views.markers;

/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.markers.FilterConfigurationArea;
import org.eclipse.ui.views.markers.MarkerFieldFilter;
import org.eclipse.ui.views.markers.MarkerSupportConstants;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * DescriptionConfigurationArea is the configuration area for description
 * configuration fields.
 */
public class DescriptionConfigurationArea extends FilterConfigurationArea {

	private Combo descriptionCombo;
	private Text descriptionText;

	/**
	 * Create new instance of the receiver.
	 */
	public DescriptionConfigurationArea() {
		super();
	}

	@Override
	public void apply(MarkerFieldFilter filter) {
		DescriptionFieldFilter desc = (DescriptionFieldFilter) filter;
		if (descriptionCombo.getSelectionIndex() == 0)
			desc.setContainsModifier(MarkerSupportConstants.CONTAINS_KEY);
		else
			desc
					.setContainsModifier(MarkerSupportConstants.DOES_NOT_CONTAIN_KEY);
		desc.setContainsText(descriptionText.getText());

	}

	@Override
	public void createContents(Composite parent) {
		createDescriptionGroup(parent);
	}

	@Override
	public void initialize(MarkerFieldFilter filter) {
		DescriptionFieldFilter desc = (DescriptionFieldFilter) filter;
		if (desc.getContainsModifier().equals(
				MarkerSupportConstants.CONTAINS_KEY))
			descriptionCombo.select(0);
		else
			descriptionCombo.select(1);

		descriptionText.setText(desc.getContainsText());

	}

	/**
	 * Create the group for the description filter.
	 */
	private void createDescriptionGroup(Composite parent) {

		Composite descriptionComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		descriptionComposite.setLayout(layout);
		descriptionComposite.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));

		Label descriptionLabel = new Label(descriptionComposite, SWT.NONE);
		descriptionLabel.setText(MarkerMessages.filtersDialog_descriptionLabel);

		descriptionCombo = new Combo(descriptionComposite, SWT.READ_ONLY);
		descriptionCombo.add(MarkerMessages.filtersDialog_contains);
		descriptionCombo.add(MarkerMessages.filtersDialog_doesNotContain);

		// Prevent Esc and Return from closing the dialog when the combo is
		// active.
		descriptionCombo.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_ESCAPE
					|| e.detail == SWT.TRAVERSE_RETURN) {
				e.doit = false;
			}
		});

		GC gc = new GC(descriptionComposite);
		gc.setFont(JFaceResources.getDialogFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();

		descriptionText = new Text(descriptionComposite, SWT.SINGLE
				| SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL);
		data.widthHint = Dialog.convertWidthInCharsToPixels(fontMetrics, 25);
		descriptionText.setLayoutData(data);
	}


	@Override
	public String getTitle() {
		return MarkerMessages.filtersDialog_description;
	}
}
