/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers;

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
	 * Apply the current settings to the filter.
	 * 
	 * @param filter
	 */
	public abstract void apply(MarkerFieldFilter filter);

	/**
	 * Create the contents of the configuration area in the parent.
	 * 
	 * @param parent
	 */
	public abstract void createContents(Composite parent);


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
	 * @return String
	 */
	public String getTitle() {
		return field.getName();
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
	 * Initialise the receiver using the filter.
	 * 
	 * @param filter
	 */
	public abstract void initialize(MarkerFieldFilter filter);
	
	/**
	 * Set the markerField for the receiver. This method intended to
	 * be called by the marker support framework directly after
	 * instantiation of the receiver.
	 * 
	 * @param markerField
	 */
	public final void setField(MarkerField markerField) {
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

	/**
	 * Return the field for the receiver.
	 * @return MarkerField
	 */
	public MarkerField getField() {
		return field;
	}

	/**
	 * Returns whether the configuration area should grab remaining vertical space.
	 * 
	 * The default implementation returns <code>false</code>. Subclasses can override.
	 * 
	 * @return <code>true</code> iff the configuration area should grab remaining vertical space
	 * @since 3.5
	 */
	public boolean grabExcessVerticalSpace() {
		return false;
	}

}
