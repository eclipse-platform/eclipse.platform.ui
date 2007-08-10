/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.provisional.views.markers;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * FilterConfigurationArea is the area that the user can configure a filter in.
 * 
 * @since 3.4
 * 
 */
public abstract class FilterConfigurationArea {

	MarkerField field;

	private FontMetrics fontMetrics;

	/**
	 * Apply the current settings to group.
	 * 
	 * @param group
	 */
	public abstract void applyToGroup(MarkerFieldFilterGroup group);

	/**
	 * Create the contents of the configuration area in the parent.
	 * 
	 * @param parent
	 */
	public abstract void createContents(Composite parent);

	/**
	 * Get the MarkerFieldFilter associated with the filter in group.
	 * 
	 * @param group
	 * @return MarkerFieldFilter or <code>null</code>
	 */
	protected MarkerFieldFilter getFilter(MarkerFieldFilterGroup group) {
		return group.getFilter(this.field);
	}

	/**
	 * Return the {@link FontMetrics} for the receiver.
	 * @return {@link FontMetrics} or <code>null</code> if {@link #initializeFontMetrics(Control)}
	 * has not been called.
	 */
	protected FontMetrics getFontMetrics(){
		return fontMetrics;
	}

	/**
	 * Get the title for the receiver.
	 * 
	 * @return
	 */
	public String getTitle() {
		return field.getColumnHeaderText();
	}

	/**
	 * Initialise {@link FontMetrics} for the receiver.
	 * 
	 * @param control
	 */
	protected void initializeFontMetrics(Control control) {
		GC gc = new GC(control);
		gc.setFont(JFaceResources.getDialogFont());
		fontMetrics = gc.getFontMetrics();
		gc.dispose();

	}

	/**
	 * Initialise the receiver using the entries in group.
	 * 
	 * @param group
	 */
	public abstract void initializeFromGroup(MarkerFieldFilterGroup group);
	
	/**
	 * Set the markerField for the receiver
	 * 
	 * @param markerField
	 */
	public void setField(MarkerField markerField) {
		field = markerField;
	}

	/**
	 * Set the standard button data for the button.
	 * @param button
	 */
	protected void setButtonLayoutData(Button button) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int widthHint = Dialog.convertHorizontalDLUsToPixels(getFontMetrics(),IDialogConstants.BUTTON_WIDTH);
		Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = Math.max(widthHint, minSize.x);
		button.setLayoutData(data);
		
	}

}
