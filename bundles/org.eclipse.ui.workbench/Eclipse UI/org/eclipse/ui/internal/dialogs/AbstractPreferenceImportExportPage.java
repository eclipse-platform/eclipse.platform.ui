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

package org.eclipse.ui.internal.dialogs;

import java.net.URL;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.util.BundleUtility;

/**
 * An abstract class with some utility methods and common values to share
 * between preference import/export wizard pages.
 * 
 * @since 3.0
 */
abstract class AbstractPreferenceImportExportPage extends WizardPage {

    /**
     * The title for all pages involved in the export operation.
     */
    protected static final String EXPORT_TITLE = WorkbenchMessages
            .getString("ImportExportPages.exportTitle"); //$NON-NLS-1$

    /**
     * The title for all pages involved in the export operation.
     */
    protected static final String IMPORT_TITLE = WorkbenchMessages
            .getString("ImportExportPages.importTitle"); //$NON-NLS-1$

    /**
     * The default extension for preferences files.
     */
    protected static final String PREFERENCE_EXT = ".epf"; //$NON-NLS-1$

    /**
     * Retrieves the image descriptor from the given relative path.  The path is
     * relative from the "icons/full" directory.
     * @param relativePath The relative path; should not be <code>null</code>.
     * @return The image descriptor if it can be found; otherwise, the "missing
     * image" descriptor.
     */
    protected static ImageDescriptor getImageDescriptor(String relativePath) {
        String path = "icons/full/" + relativePath; //$NON-NLS-1$
        URL url = BundleUtility.find(PlatformUI.PLUGIN_ID, path);

        return url != null ? ImageDescriptor.createFromURL(url)
                : ImageDescriptor.getMissingImageDescriptor();
    }

    /**
     * Whether this page was opened in export or import mode.  Since there is a
     * significant amount of overlap, the import and export pages are not 
     * separate classes.
     */
    protected final boolean export;

    /**
     * Constructs a new instance of a preference import/export page with the
     * given name.
     * @param name The name of the page to be constructed; must not be
     * <code>null</code>.
     * @param exportWizard Whether this page should be opened in export mode.
     */
    protected AbstractPreferenceImportExportPage(final String name,
            final boolean exportWizard) {
        super(name);

        export = exportWizard;
    }

    /**
     * Computes the width hint for the given button.  The width hint is the 
     * maximum of the default width and the minimum width to display the button
     * text.
     * @param pushButton The push button for which to compute the width hint.
     * @return The width hint, which should be a positive integer capable of
     * displaying all the text in the button.
     */
    protected int computePushButtonWidthHint(Button pushButton) {
        final int defaultWidth = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        final int minimumWidth = pushButton.computeSize(SWT.DEFAULT,
                SWT.DEFAULT, true).x;
        return Math.max(defaultWidth, minimumWidth) + 5;
    }
}