/*******************************************************************************
 * Copyright (c) 2016-2017 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sopot Cela (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests.contributions;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

/**
 * This presentation reconciler is associated to a more "basic" content-type so it
 * shouldn't be used.
 */
public class ThePresentationReconcilerGreen extends PresentationReconciler {

	public ThePresentationReconcilerGreen() {
		RuleBasedScanner scanner= new RuleBasedScanner();
		IRule[] rules = new IRule[1];
		rules[0]= new SingleLineRule("'", "'", new Token(new TextAttribute(new Color(Display.getCurrent(), new RGB(0, 255, 0))))); //$NON-NLS-1$ //$NON-NLS-2$
		scanner.setRules(rules);
		DefaultDamagerRepairer dr= new DefaultDamagerRepairer(scanner);
		this.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		this.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
	}

}
