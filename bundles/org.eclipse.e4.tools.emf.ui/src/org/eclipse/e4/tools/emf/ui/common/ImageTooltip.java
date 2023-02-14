/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.common;

import java.text.MessageFormat;

import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public class ImageTooltip extends ToolTip {
	private final Messages Messages;
	private final AbstractComponentEditor<?> editor;

	public ImageTooltip(Control control, Messages Messages, AbstractComponentEditor<?> editor) {
		super(control);
		this.Messages = Messages;
		this.editor = editor;
	}

	@Override
	protected boolean shouldCreateToolTip(Event event) {
		if (getImageURI() != null) {
			return super.shouldCreateToolTip(event);
		}
		return false;
	}

	@Override
	protected Composite createToolTipContentArea(Event event, Composite parent) {
		parent = new Composite(parent, SWT.NONE);
		parent.setBackground(event.widget.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		parent.setBackgroundMode(SWT.INHERIT_DEFAULT);
		parent.setLayout(new GridLayout(2, false));

		final String imageUri = getImageURI();
		final Image image = imageUri != null ? getImage() : null;


		// ---------------------------------
		Label l = new Label(parent, SWT.NONE);
		l.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
		l.setText(Messages.ImageTooltip_Icon + ":"); //$NON-NLS-1$

		l = new Label(parent, SWT.NONE);
		if (image == null && imageUri != null)
		{
			final String errorMessage = MessageFormat.format(Messages.ImageTooltip_FileNotFound, imageUri);
			l.setText(errorMessage);
		} else

		{
			l.setImage(image);
		}

		// ---------------------------------
		l = new Label(parent, SWT.NONE);
		l.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
		l.setText(Messages.ImageTooltip_Name + ":"); //$NON-NLS-1$

		l = new Label(parent, SWT.NONE);
		final int pos = imageUri.lastIndexOf('/');
		if (pos != -1) {
			l.setText(imageUri.substring(pos));
		} else {
			l.setText(imageUri);
		}

		// ---------------------------------
		l = new Label(parent, SWT.NONE);
		l.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
		l.setText(Messages.ImageTooltip_Dimension + ":"); //$NON-NLS-1$

		l = new Label(parent, SWT.NONE);
		if (image != null)
		{
			l.setText(image.getBounds().width + "x" + image.getBounds().height + " px"); //$NON-NLS-1$ //$NON-NLS-2$
		} else
		{
			l.setText("0x0 px"); //$NON-NLS-1$
		}


		return parent;}

	private String getImageURI() {
		final MUILabel part = (MUILabel) editor.getMaster().getValue();
		return part.getIconURI();

	}

	protected Image getImage() {
		final MUILabel part = (MUILabel) editor.getMaster().getValue();
		final String iconUri = getImageURI();

		return iconUri != null ? editor.getImageFromIconURI(part) : null;
	}

}
