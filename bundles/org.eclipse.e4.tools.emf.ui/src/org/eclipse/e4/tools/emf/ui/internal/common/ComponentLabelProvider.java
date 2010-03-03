/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common;

import org.eclipse.e4.ui.model.application.MModelComponent;
import org.eclipse.e4.ui.model.application.MModelComponents;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartDescriptor;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;

public class ComponentLabelProvider extends StyledCellLabelProvider {
	private Image modelComponentsImage;
	private Image modelComonentImage;
	private Image partsImage;
	private Image menusImage;
	private Image partImage;
	private Image partDescriptorImage;

	@Override
    public void update(final ViewerCell cell) {
		if( cell.getElement() instanceof MModelComponents ) {
			cell.setText("Model Components");
			if( modelComponentsImage == null ) {
				modelComponentsImage = new Image(cell.getControl().getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/application_view_icons.png"));
			}
			cell.setImage(modelComponentsImage);
		} else if( cell.getElement() instanceof MModelComponent ) {
			MModelComponent m = (MModelComponent) cell.getElement();
			StyledString styledString = new StyledString("Model Component", null);
			String decoration = " - " + m.getParentID();
			Styler styler = new Styler() {

				@Override
				public void applyStyles(TextStyle textStyle) {
					textStyle.foreground = cell.getControl().getDisplay().getSystemColor(SWT.COLOR_GRAY);
				}
			};

	        styledString.append(decoration, styler);
			cell.setText(styledString.getString());
			cell.setStyleRanges(styledString.getStyleRanges());
			if( modelComonentImage == null ) {
				modelComonentImage = new Image(cell.getControl().getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/package_go.png"));
			}
			cell.setImage(modelComonentImage);
		} else if( cell.getElement() instanceof VirtualEntry<?> ) {
			String s = cell.getElement().toString();
			cell.setText(s);
			if( "Parts".equals(s) ) {
				if( partsImage == null ) {
					partsImage = new Image(cell.getControl().getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/application_double.png"));
				}
				cell.setImage(partsImage);
			} else if( "Menus".equals(s) ) {
				if( menusImage == null ) {
					menusImage = new Image(cell.getControl().getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/cog.png"));
				}
				cell.setImage(menusImage);
			}
		} else if( cell.getElement() instanceof MPart ) {
			MPart part = (MPart) cell.getElement();
			String label;
			if( cell.getElement() instanceof MPartDescriptor ) {
				label = "Part Descriptor";
				if( partDescriptorImage == null ) {
					partDescriptorImage = new Image(cell.getControl().getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/application_form_edit.png"));
				}
				cell.setImage(partImage);
			} else {
				label = "Part";
				if( partImage == null ) {
					partImage = new Image(cell.getControl().getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/application_form.png"));
				}
				cell.setImage(partImage);
			}
			StyledString styledString = new StyledString(label, null);
			String decoration = " - " + part.getLabel();
			Styler styler = new Styler() {

				@Override
				public void applyStyles(TextStyle textStyle) {
					textStyle.foreground = cell.getControl().getDisplay().getSystemColor(SWT.COLOR_GRAY);
				}
			};

	        styledString.append(decoration, styler);
	        cell.setText(styledString.getString());
			cell.setStyleRanges(styledString.getStyleRanges());
		} else {
			cell.setText(cell.getElement()+"");
		}
	}

	@Override
	public void dispose() {
		if( modelComponentsImage != null ) {
			modelComponentsImage.dispose();
			modelComponentsImage = null;
		}

		if( modelComonentImage != null ) {
			modelComonentImage.dispose();
			modelComonentImage = null;
		}

		if( partsImage != null ) {
			partsImage.dispose();
			partsImage = null;
		}

		if( menusImage != null ) {
			menusImage.dispose();
			menusImage = null;
		}

		if( partImage != null ) {
			partImage.dispose();
			partImage = null;
		}

		if( partDescriptorImage != null ) {
			partDescriptorImage.dispose();
			partDescriptorImage = null;
		}
		super.dispose();
	}
}
