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

import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.emf.ecore.EObject;
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

	private ModelEditor editor;

	public ComponentLabelProvider(ModelEditor editor) {
		this.editor = editor;
	}

	@Override
    public void update(final ViewerCell cell) {
		if( cell.getElement() instanceof EObject ) {

			EObject o = (EObject) cell.getElement();
			AbstractComponentEditor elementEditor = editor.getEditor(o.eClass());
			if( elementEditor != null ) {
				String label = elementEditor.getLabel(o);
				String detailText = elementEditor.getDetailLabel(o);
				if( detailText == null ) {
					cell.setText(label);
				} else {
					StyledString styledString = new StyledString(label, null);
					Styler styler = new Styler() {

						@Override
						public void applyStyles(TextStyle textStyle) {
							textStyle.foreground = cell.getControl().getDisplay().getSystemColor(SWT.COLOR_GRAY);
						}
					};

			        styledString.append(" - " + detailText, styler);
					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());
				}
				cell.setImage(elementEditor.getImage(o, cell.getControl().getDisplay()));
			} else {
				cell.setText(cell.getElement().toString());
			}
		} else if( cell.getElement() instanceof VirtualEntry<?> ) {
			String s = cell.getElement().toString();
			cell.setText(s);
			if( "Parts".equals(s) ) {
				if( partsImage == null ) {
					partsImage = new Image(cell.getControl().getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/application_double.png"));
				}
				cell.setImage(partsImage);
			}
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
