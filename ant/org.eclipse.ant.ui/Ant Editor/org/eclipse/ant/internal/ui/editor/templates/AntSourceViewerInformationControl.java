/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.templates;

import org.eclipse.ant.internal.ui.editor.formatter.FormattingPreferences;
import org.eclipse.ant.internal.ui.editor.formatter.XmlFormatter;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.ant.internal.ui.preferences.AntEditorPreferenceConstants;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.swt.widgets.Shell;

public class AntSourceViewerInformationControl extends DefaultInformationControl {
	
	public AntSourceViewerInformationControl(Shell parent, int shellStyle, int style, IInformationPresenter presenter) {
		super(parent, shellStyle, style, presenter);
	}
	
	public AntSourceViewerInformationControl(Shell parent, int shellStyle, int style, IInformationPresenter presenter, String statusFieldText) {
		super(parent, shellStyle, style, presenter, statusFieldText);
	}
	
	public AntSourceViewerInformationControl(Shell parent, int style, IInformationPresenter presenter) {
		super(parent, style, presenter);
	}
	
	public AntSourceViewerInformationControl(Shell parent, int style, IInformationPresenter presenter, String statusFieldText) {
		super(parent, style, presenter, statusFieldText);
	}
	
	public AntSourceViewerInformationControl(Shell parent) {
		super(parent);
	}
	
	public AntSourceViewerInformationControl(Shell parent, IInformationPresenter presenter) {
		super(parent, presenter);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#setInformation(java.lang.String)
	 */
	public void setInformation(String content) {
		if (content != null 
			&& AntUIPlugin.getDefault().getPreferenceStore().getBoolean(AntEditorPreferenceConstants.TEMPLATES_USE_CODEFORMATTER)) {
			content= XmlFormatter.format(content, new FormattingPreferences());
		}
		super.setInformation(content);
	}
}
