/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.css2;

import org.eclipse.e4.ui.css.core.dom.properties.css2.AbstractCSSPropertyTextHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyTextHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyTextSWTHandler extends AbstractCSSPropertyTextHandler {

	public final static ICSSPropertyTextHandler INSTANCE = new CSSPropertyTextSWTHandler();

	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		Control control = SWTElementHelpers.getControl(element);
		if (control != null) {
			super.applyCSSProperty(control, property, value, pseudo, engine);
			return true;
		}
		return false;

	}

	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		Control control = SWTElementHelpers.getControl(element);
		if (control != null) {
			return super.retrieveCSSProperty(control, property, pseudo, engine);
		}
		return null;
	}

	public void applyCSSPropertyColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		Control control = (Control) element;
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			Color newColor = (Color) engine.convert(value, Color.class, control
					.getDisplay());
			if (control instanceof CTabFolder && "selected".equals(pseudo)) {
				((CTabFolder) control).setSelectionForeground(newColor);
			} else {
				control.setForeground(newColor);
			}
		}
	}

	public void applyCSSPropertyTextTransform(Object element,
			final CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		Control control = (Control) element;
		String defaultText = (String) control.getData(CSSSWTConstants.TEXT_KEY);
		if (element instanceof Text) {
			final Text text = (Text) control;
			VerifyListener listener = (VerifyListener) text
					.getData("CSSModifyTextListener");
			if (hasTextTransform(value)) {
				if (listener == null) {
					// Add ModifyListener
					listener = new VerifyListener() {
						public void verifyText(VerifyEvent e) {
							// System.out.println(e);
							// int start = e.start;
							// int end = e.end;
							// e.text = getTextTransform(e.text, text.getText(),
							// value);
						}
					};
					text.addVerifyListener(listener);
					text.setData("CSSModifyTextListener", listener);
				}
			} else {
				if (listener != null) {
					text.removeVerifyListener(listener);
					text.setData("CSSModifyTextListener", null);
				}
			}
			String oldText = text.getText();
			String newText = getTextTransform(text.getText(), value,
					defaultText);
			if (!oldText.equals(newText))
				text.setText(newText);
		}
		if (element instanceof Label) {
			Label label = (Label) element;
			label
					.setText(getTextTransform(label.getText(), value,
							defaultText));
			return;
		}
		if (element instanceof Button) {
			Button button = (Button) element;
			button.setText(getTextTransform(button.getText(), value,
					defaultText));
			return;
		}
	}

	public String retrieveCSSPropertyColor(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		Control control = (Control) element;
		Color color = control.getForeground();
		return engine.convert(color, Color.class, null);
	}

	public String retrieveCSSPropertyTextTransform(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		String text = null;
		Control control = (Control) element;
		// if (control instanceof Text) {
		// final Text controlText = ((Text) element);
		// text = controlText.getText();
		// } else {
		if (control instanceof Label) {
			text = ((Label) element).getText();
			if (text != null)
				control.setData(CSSSWTConstants.TEXT_KEY, text);
		} else {
			if (control instanceof Button) {
				text = ((Button) element).getText();
				if (text != null)
					control.setData(CSSSWTConstants.TEXT_KEY, text);
			}
		}
		// }
		return "none";
	}

}
