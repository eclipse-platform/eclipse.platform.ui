/*******************************************************************************
 * Copyright (c) 2016, 2017 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria, Sopot Cela (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.examples.dotproject;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

public class BlueTagsPresentationReconciler extends PresentationReconciler {

    private final TextAttribute tagAttribute = new TextAttribute(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
    private final TextAttribute headerAttribute = new TextAttribute(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));

    public BlueTagsPresentationReconciler() {
        RuleBasedScanner scanner= new RuleBasedScanner();
        IRule[] rules = new IRule[2];
        rules[1]= new SingleLineRule("<", ">", new Token(tagAttribute));
        rules[0]= new SingleLineRule("<?", "?>", new Token(headerAttribute));
        scanner.setRules(rules);
        DefaultDamagerRepairer dr= new DefaultDamagerRepairer(scanner);
        this.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        this.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
    }
}