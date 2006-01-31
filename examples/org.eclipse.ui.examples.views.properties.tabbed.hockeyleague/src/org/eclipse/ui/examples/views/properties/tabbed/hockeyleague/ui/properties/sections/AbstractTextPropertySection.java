/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections;

import java.util.Iterator;

import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.editor.HockeyleagueEditor;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.TextChangeHelper;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * An abstract implementation of a section with a text field.
 * 
 * @author Anthony Hunter
 */
public abstract class AbstractTextPropertySection
	extends AbstractHockeyleaguePropertySection {

	/**
	 * the text control for the section.
	 */
	protected Text text;

	/**
	 * A helper to listen for events that indicate that a text field has been
	 * changed.
	 */
	protected TextChangeHelper listener;

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ITabbedPropertySection#createControls(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	public void createControls(Composite parent,
			TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);
		Composite composite = getWidgetFactory()
			.createFlatFormComposite(parent);
		FormData data;

		text = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, getStandardLabelWidth(composite,
			new String[] {getLabelText()}));
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		text.setLayoutData(data);

		CLabel nameLabel = getWidgetFactory().createCLabel(composite,
			getLabelText());
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(text, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(text, 0, SWT.CENTER);
		nameLabel.setLayoutData(data);

		listener = new TextChangeHelper() {

			public void textChanged(Control control) {
				handleTextModified();
			}
		};
		listener.startListeningTo(text);
		listener.startListeningForEnter(text);
	}

	/**
	 * Handle the text modified event.
	 */
	protected void handleTextModified() {
		String newText = text.getText();
		boolean equals = isEqual(newText);
		if (!equals) {
			EditingDomain editingDomain = ((HockeyleagueEditor) getPart())
				.getEditingDomain();
			Object value = getFeatureValue(newText);
			if (eObjectList.size() == 1) {
				/* apply the property change to single selected object */
				editingDomain.getCommandStack().execute(
					SetCommand.create(editingDomain, eObject, getFeature(),
						value));
			} else {
				CompoundCommand compoundCommand = new CompoundCommand();
				/* apply the property change to all selected elements */
				for (Iterator i = eObjectList.iterator(); i.hasNext();) {
					EObject nextObject = (EObject) i.next();
					compoundCommand.append(SetCommand.create(editingDomain,
						nextObject, getFeature(), value));
				}
				editingDomain.getCommandStack().execute(compoundCommand);
			}
		}
	}

	/*
	 * @see org.eclipse.ui.views.properties.tabbed.view.ITabbedPropertySection#refresh()
	 */
	public void refresh() {
		text.setText(getFeatureAsText());
	}

	/**
	 * Determine if the provided string value is an equal representation of the
	 * current setting of the text property.
	 * 
	 * @param newText
	 *            the new string value.
	 * @return <code>true</code> if the new string value is equal to the
	 *         current property setting.
	 */
	protected abstract boolean isEqual(String newText);

	/**
	 * Get the feature for the text field for the section.
	 * 
	 * @return the feature for the text.
	 */
	protected abstract EAttribute getFeature();

	/**
	 * Get the value of the feature as text for the text field for the section.
	 * 
	 * @return the value of the feature as text.
	 */
	protected abstract String getFeatureAsText();

	/**
	 * Get the new value of the feature for the text field for the section.
	 * 
	 * @param newText
	 *            the new value of the feature as a string.
	 * @return the new value of the feature.
	 */
	protected abstract Object getFeatureValue(String newText);

	/**
	 * Get the label for the text field for the section.
	 * 
	 * @return the label for the text field.
	 */
	protected abstract String getLabelText();
}