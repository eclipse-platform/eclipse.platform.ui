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
package org.eclipse.ui.forms.parts;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.FormColors;

/**
 *
 */

public abstract class Form extends ScrolledComposite implements IPropertyChangeListener {
	protected FormToolkit toolkit;
	protected Color titleColor;
	protected Image titleImage;
	protected String title;
	protected Font titleFont;
	private IPropertyChangeListener hyperlinkColorListener;
	
	public Form(Composite parent) {
		this(parent, new FormColors(parent.getDisplay()));
	}

	public Form(Composite parent, FormColors colors) {
		super(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		toolkit = new FormToolkit(colors);
		titleFont = JFaceResources.getHeaderFont();
		JFaceResources.getFontRegistry().addListener(this);
		IPreferenceStore pstore = JFacePreferences.getPreferenceStore();
		hyperlinkColorListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if (e.getProperty().equals(JFacePreferences.HYPERLINK_COLOR)
					|| e.getProperty().equals(
						JFacePreferences.ACTIVE_HYPERLINK_COLOR)) {
					updateHyperlinkColors();
				}
			}
		};
		pstore.addPropertyChangeListener(hyperlinkColorListener);
		addListener(SWT.Dispose, new Listener() {
			public void handleEvent(Event e) {
				onDispose();
			}
		});
	}
	
	protected void updateHyperlinkColors() {
		toolkit.updateHyperlinkColors();
	}
	/**
	 * @see IForm#dispose()
	 */
	private void onDispose() {
		toolkit.dispose();
		JFaceResources.getFontRegistry().removeListener(this);
		IPreferenceStore pstore = JFacePreferences.getPreferenceStore();
		pstore.removePropertyChangeListener(this);
		pstore.removePropertyChangeListener(hyperlinkColorListener);
	}
	/**
	 * @see IForm#getFactory()
	 */
	public FormToolkit getToolkit() {
		return toolkit;
	}

	/**
	 * @see IForm#getHeadingForeground()
	 */
	public Color getHeadingForeground() {
		return titleColor;
	}

	/**
	 * @see IForm#getHeadingImage()
	 */
	public Image getTitleImage() {
		return titleImage;
	}

	/**
	 * @see IForm#getHeading()
	 */
	public String getTitle() {
		if (title == null)
			return "";
		return title;
	}
	/**
	 * @see IForm#setHeadingForeground(Color)
	 */
	public void setTitleColor(Color color) {
		this.titleColor = color;
	}

	/**
	 * @see IForm#setHeadingImage(Image)
	 */
	public void setTitleImage(Image titleImage) {
		this.titleImage = titleImage;
	}

	/**
	 * @see IForm#setHeading(String)
	 */
	public void setTitle(String title) {
		this.title = title;
	}
}
