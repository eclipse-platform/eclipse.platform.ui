/*******************************************************************************
 * Copyright (c) 2004 John-Mason P. Shackelford and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     John-Mason P. Shackelford - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.editor.formatter;

import org.eclipse.ant.internal.ui.editor.text.AntDocumentSetupParticipant;
import org.eclipse.ant.internal.ui.editor.text.AntEditorPartitionScanner;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.formatter.MultiPassContentFormatter;

/**
 * Utility class for using the ant code formatter in contexts where an IDocument
 * containing the text to format is not readily available.
 */
public class XmlFormatter {

    /**
     * Format the text using the ant code formatter.
     * 
     * @param text
     *            The text to format. Must be a non-null value.
     * @param prefs
     *            Preferences to use for this format operation. If null, the
     *            preferences currently set in the plug-in's preferences store
     *            are used.
     * @return The formatted text.
     */
    public static String format(String text, FormattingPreferences prefs) {
        
        Assert.isNotNull(text);
        
        FormattingPreferences applyPrefs;
        if(prefs == null) {
            applyPrefs = new FormattingPreferences();
        } else {
            applyPrefs = prefs;
        }
        
        IDocument doc = new Document();
        doc.set(text);
        new AntDocumentSetupParticipant().setup(doc);

        MultiPassContentFormatter formatter = new MultiPassContentFormatter(
                IDocumentExtension3.DEFAULT_PARTITIONING,
                IDocument.DEFAULT_CONTENT_TYPE);

        formatter.setMasterStrategy(new XmlDocumentFormattingStrategy(applyPrefs));
        formatter.setSlaveStrategy(new XmlElementFormattingStrategy(applyPrefs),
                AntEditorPartitionScanner.XML_TAG);
        formatter.format(doc, new Region(0, doc.getLength()));

        return doc.get();
    }

    /**
     * Format the text using the ant code formatter using the preferences
     * settings in the plug-in preference store.
     * 
     * @param text
     *            The text to format. Must be a non-null value.
     * @return The formatted text.
     */
    public static String format(String text) {
        return format(text,null);
    }
}
