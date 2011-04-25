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
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextStyle;

public class ComponentLabelProvider extends StyledCellLabelProvider {

	private ModelEditor editor;

	public static final String NOT_RENDERED_KEY = "NOT_RENDERED_STYLER";//$NON-NLS-1$

	public static final String NOT_VISIBLE_KEY = "NOT_VISIBLE_KEY";//$NON-NLS-1$

	public static final String NOT_VISIBLE_AND_RENDERED_KEY = "NOT_VISIBLE_AND_RENDERED_KEY";//$NON-NLS-1$

	private Font font;

	private Messages Messages;

	private static Styler BOTH_STYLER = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = JFaceResources.getColorRegistry().get(NOT_VISIBLE_AND_RENDERED_KEY);
			textStyle.strikeout = true;
		}
	};

	private static Styler NOT_RENDERED_STYLER = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = JFaceResources.getColorRegistry().get(NOT_RENDERED_KEY);
			textStyle.strikeout = true;
		}
	};

	private static Styler NOT_VISIBLE_STYLER = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = JFaceResources.getColorRegistry().get(NOT_VISIBLE_KEY);
		}
	};

	public ComponentLabelProvider(ModelEditor editor, Messages Messages) {
		this.editor = editor;
		this.Messages = Messages;
	}

	@Override
	public void update(final ViewerCell cell) {
		if (cell.getElement() instanceof EObject) {

			EObject o = (EObject) cell.getElement();
			AbstractComponentEditor elementEditor = editor.getEditor(o.eClass());
			if (elementEditor != null) {
				String label = elementEditor.getLabel(o);
				String detailText = elementEditor.getDetailLabel(o);
				Styler styler = null;

				if (o instanceof MUIElement) {

					if (!((MUIElement) o).isVisible() && !((MUIElement) o).isToBeRendered()) {
						label += "<" + Messages.ComponentLabelProvider_invisible + "/" + Messages.ComponentLabelProvider_notrendered + ">"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
						styler = BOTH_STYLER;
					} else if (!((MUIElement) o).isVisible()) {
						label += "<" + Messages.ComponentLabelProvider_invisible + ">"; //$NON-NLS-1$//$NON-NLS-2$
						styler = NOT_VISIBLE_STYLER;
					} else if (!((MUIElement) o).isToBeRendered()) {
						label += "<" + Messages.ComponentLabelProvider_notrendered + ">"; //$NON-NLS-1$ //$NON-NLS-2$
						styler = NOT_RENDERED_STYLER;
					}
				}

				if (detailText == null) {
					StyledString styledString = new StyledString(label, styler);
					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());
				} else {
					StyledString styledString = new StyledString(label, styler);
					styledString.append(" - " + detailText, StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());
				}
				cell.setImage(elementEditor.getImage(o, cell.getControl().getDisplay()));
			} else {
				cell.setText(cell.getElement().toString());
			}
		} else if (cell.getElement() instanceof VirtualEntry<?>) {
			String s = cell.getElement().toString();
			if (font == null) {
				FontData[] data = cell.getControl().getFont().getFontData();
				font = new Font(cell.getControl().getDisplay(), new FontData(data[0].getName(), data[0].getHeight(), SWT.ITALIC));
			}
			cell.setFont(font);
			cell.setText(s);
		} else {
			cell.setText(cell.getElement().toString());
		}
	}

	@Override
	public void dispose() {
		if (font != null) {
			font.dispose();
			font = null;
		}
		super.dispose();
	}
}