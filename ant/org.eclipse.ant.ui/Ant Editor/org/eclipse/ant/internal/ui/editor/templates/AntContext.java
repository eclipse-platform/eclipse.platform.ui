/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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
import org.eclipse.ant.internal.ui.editor.formatter.XmlDocumentFormatter;
import org.eclipse.ant.internal.ui.editor.outline.AntModel;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.ContextType;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateException;

public class AntContext extends DocumentTemplateContext {
	
	private AntModel fAntModel;
	
	public AntContext(ContextType type, IDocument document, AntModel model, int completionOffset, int completionLength) {
		super(type, document, completionOffset, completionLength);
		fAntModel= model;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.templates.TemplateContext#evaluate(org.eclipse.jface.text.templates.Template)
	 */
	public TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException {

		TemplateBuffer templateBuffer= super.evaluate(template);
		
		if (template == null) {
			return null;
		}
	
		FormattingPreferences prefs = new FormattingPreferences();
		XmlDocumentFormatter formatter= new XmlDocumentFormatter();
		
		formatter.format(templateBuffer, this, prefs);
		return templateBuffer;
	}
	
	/**
	 * @return Returns the AntModel.
	 */
	public AntModel getAntModel() {
		return fAntModel;
	}
}
