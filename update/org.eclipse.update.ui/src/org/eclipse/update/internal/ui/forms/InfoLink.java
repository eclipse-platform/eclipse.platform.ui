/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.forms;
import java.net.URL;

import org.eclipse.swt.widgets.*;
import org.eclipse.update.core.IURLEntry;
import org.eclipse.update.internal.ui.views.DetailsView;
import org.eclipse.update.ui.forms.internal.*;

public class InfoLink extends HyperlinkAdapter {
	private IURLEntry info;
	private SelectableFormLabel linkLabel;
	private DetailsView view;
	private boolean visible = true;
	private String text="";

	public InfoLink(DetailsView view) {
		this.view = view;
	}
	
	public void createControl(Composite composite, FormWidgetFactory factory) {
		linkLabel = factory.createSelectableLabel(composite, text);
		factory.turnIntoHyperlink(linkLabel, this);
		linkLabel.setVisible(visible);
	}
	
	public Control getControl() {
		return linkLabel;
	}

	public void linkActivated(Control link) {
		if (info==null) return;
		URL url = info.getURL();
		if (url!=null) {
			DetailsView.showURL(url.toString());
			return;
		}
		String annotation = info.getAnnotation();
		if (annotation !=null && annotation.length()>0) {
			view.showText(annotation);
		}
	}
	
	public void setInfo(IURLEntry info) {
		this.info = info;
		if (info!=null) {
			boolean hasText = info.getAnnotation()!=null && info.getAnnotation().length()>0;
			boolean hasURL = info.getURL()!=null;
			setVisible(hasText || hasURL);
		}
		else setVisible(false);
	}
	
	private void setVisible(boolean visible) {
		this.visible = visible;
		if (linkLabel!=null) {
			linkLabel.setVisible(visible);
		}
	}
	/**
	 * Gets the text.
	 * @return Returns a String
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text.
	 * @param text The text to set
	 */
	public void setText(String text) {
		this.text = text;
		if (linkLabel != null) {
			linkLabel.setText(text);
		}
	}

}
