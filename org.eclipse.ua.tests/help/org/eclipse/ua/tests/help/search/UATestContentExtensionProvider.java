/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.search;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.help.AbstractContentExtensionProvider;
import org.eclipse.help.IContentExtension;
import org.eclipse.help.IUAElement;

public class UATestContentExtensionProvider extends
		AbstractContentExtensionProvider {
	
	private IContentExtension extension;

	public UATestContentExtensionProvider() {
		// TODO Auto-generated constructor stub
		extension = new IContentExtension() {
			
			public boolean isEnabled(IEvaluationContext context) {
				// TODO Auto-generated method stub
				return true;
			}
			
			public IUAElement[] getChildren() {
				// TODO Auto-generated method stub
				return null;
			}
			
			public int getType() {
				return REPLACEMENT;
			}
			
			public String getPath() {
				return "org.eclipse.ua.tests/data/help/search/test6.xhtml#test6_paragraph_to_replace2";
			}
			
			public String getContent() {
				return "/org.eclipse.ua.tests/data/help/search/testProvider.xhtml";
			}
		};
	}

	@Override
	public IContentExtension[] getContentExtensions(String locale) {
		return new IContentExtension[] { extension };
	}

}
