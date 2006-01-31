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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.editor.HockeyleagueEditor;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * An abstract implementation of a section for measurements. There is a text
 * field for the mesurement value and a radio box for the measurement units. For
 * example, weight in either pounds or kilograms.
 * 
 * @author Anthony Hunter
 */
public abstract class AbstractMeasurementPropertySection
	extends AbstractIntegerPropertySection {

	/**
	 * the left radio button for the section.
	 */
	protected Button radioLeft;

	/**
	 * the right radio button for the section.
	 */
	protected Button radioRight;

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#createControls(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		Composite composite = getWidgetFactory()
			.createFlatFormComposite(parent);

		String[] labels = getEnumerationLabels();

		radioLeft = getWidgetFactory().createButton(composite, labels[0],
			SWT.RADIO);
		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		radioLeft.setLayoutData(data);

		radioRight = getWidgetFactory().createButton(composite, labels[1],
			SWT.RADIO);
		data = new FormData();
		data.left = new FormAttachment(radioLeft,
			ITabbedPropertyConstants.HSPACE);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		radioRight.setLayoutData(data);

		SelectionListener selectionListener = new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				handleRadioModified(((Button) e.getSource()));
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

		};

		radioLeft.addSelectionListener(selectionListener);
		radioRight.addSelectionListener(selectionListener);
	}

	/**
	 * Handle the radio box modified event.
	 */
	protected void handleRadioModified(Button button) {

		int index;
		if (button == radioLeft) {
			index = 0;
		} else {
			index = 1;
		}
		boolean equals = isEnumerationEqual(index);
		if (!equals) {
			EditingDomain editingDomain = ((HockeyleagueEditor) getPart())
				.getEditingDomain();
			Object value = getEnumerationFeatureValue(index);
			if (eObjectList.size() == 1) {
				/* apply the property change to single selected object */
				editingDomain.getCommandStack().execute(
					SetCommand.create(editingDomain, eObject,
						getEnumerationFeature(), value));
			} else {
				CompoundCommand compoundCommand = new CompoundCommand();
				/* apply the property change to all selected elements */
				for (Iterator i = eObjectList.iterator(); i.hasNext();) {
					EObject nextObject = (EObject) i.next();
					compoundCommand.append(SetCommand.create(editingDomain,
						nextObject, getEnumerationFeature(), value));
				}
				editingDomain.getCommandStack().execute(compoundCommand);
			}
		}
	}

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#refresh()
	 */
	public void refresh() {
		super.refresh();
		if (getEnumerationIndex() == 0) {
			radioLeft.setSelection(true);
			radioRight.setSelection(false);
		} else {
			radioLeft.setSelection(false);
			radioRight.setSelection(true);
		}

	}

	/**
	 * Determine if the provided index of the enumeration is equal to the
	 * current setting of the enumeration property.
	 * 
	 * @param index
	 *            the new index in the enumeration.
	 * @return <code>true</code> if the new index value is equal to the
	 *         current property setting.
	 */
	protected abstract boolean isEnumerationEqual(int index);

	/**
	 * Get the feature for the enumeration field for the section.
	 * 
	 * @return the feature for the text.
	 */
	protected abstract EAttribute getEnumerationFeature();

	/**
	 * Get the enumeration values of the feature for the radio field for the
	 * section.
	 * 
	 * @return the list of values of the feature as text.
	 */
	protected abstract String[] getEnumerationLabels();

	/**
	 * Get the index value of the feature for the radio field for the section.
	 * 
	 * @return the index value of the feature.
	 */
	protected abstract int getEnumerationIndex();

	/**
	 * Get the new value of the feature for the text field for the section.
	 * 
	 * @param index
	 *            the new index in the enumeration.
	 * @return the new value of the feature.
	 */
	protected abstract Object getEnumerationFeatureValue(int index);
}