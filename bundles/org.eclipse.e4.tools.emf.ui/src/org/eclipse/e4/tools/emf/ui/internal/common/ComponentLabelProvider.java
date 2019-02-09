/*******************************************************************************
 * Copyright (c) 2010, 2015 BestSolution.at and others.
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
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 475365
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common;

import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;

public class ComponentLabelProvider extends BaseLabelProvider implements IStyledLabelProvider, IFontProvider {

	private final ModelEditor editor;

	public static final String NOT_RENDERED_KEY = "NOT_RENDERED_STYLER";//$NON-NLS-1$

	public static final String NOT_VISIBLE_KEY = "NOT_VISIBLE_KEY";//$NON-NLS-1$

	public static final String NOT_VISIBLE_AND_RENDERED_KEY = "NOT_VISIBLE_AND_RENDERED_KEY";//$NON-NLS-1$

	private final FontDescriptor italicFontDescriptor;

	private final Messages Messages;

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

	private final ResourceManager resourceManager;

	public ComponentLabelProvider(ModelEditor editor, Messages Messages, FontDescriptor italicFontDescriptor) {
		this.editor = editor;
		this.Messages = Messages;
		this.italicFontDescriptor = italicFontDescriptor;
		resourceManager = new LocalResourceManager(JFaceResources.getResources());
	}

	@Override
	public void dispose() {
		if (resourceManager != null) {
			resourceManager.dispose();
		}
		super.dispose();
	}

	@Override
	public StyledString getStyledText(Object element) {
		if (element instanceof EObject) {

			final EObject o = (EObject) element;
			final AbstractComponentEditor<?> elementEditor = editor.getEditor(o.eClass());
			if (elementEditor != null) {
				String label = elementEditor.getLabel(o);
				final String detailText = elementEditor.getDetailLabel(o);
				Styler styler = null;

				if (o instanceof MUIElement) {

					if (!((MUIElement) o).isVisible() && !((MUIElement) o).isToBeRendered()) {
						label += "<" + Messages.ComponentLabelProvider_invisible + "/" //$NON-NLS-1$//$NON-NLS-2$
								+ Messages.ComponentLabelProvider_notrendered + ">"; //$NON-NLS-1$
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
					return new StyledString(label, styler);
				}
				final StyledString styledString = new StyledString(label, styler);
				styledString.append(" - " + detailText, StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
				return styledString;
			}
		}
		return new StyledString(element.toString());
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof EObject) {
			final EObject o = (EObject) element;
			final AbstractComponentEditor<?> elementEditor = editor.getEditor(o.eClass());
			if (elementEditor != null) {
				return elementEditor.getImage(element);
			}
		}
		return null;
	}

	@Override
	public Font getFont(Object element) {
		if (element instanceof VirtualEntry) {
			return resourceManager.createFont(italicFontDescriptor);
		}
		return null;
	}
}