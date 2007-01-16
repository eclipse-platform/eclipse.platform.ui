/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.readmetool;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * This page will be added to the property page dialog
 * when "Properties..." popup menu item is selected
 * for Readme files. 
 *
 * This page demonstrates conditional property pages which look
 * different depending on the state of the element. In this example,
 * the arbitrary condition chosen is whether the Readme file is
 * greater than 256 bytes in length. If it is smaller than 256 bytes
 * in length, this will be a placeholder page containing 
 * a simple message. If it is 256 bytes or larger, additional 
 * information will be provided. This information is determined at
 * runtime.
 *
 * This class may be reused to implement a conditional property page.
 * The getPageIndex() method tests the condition and returns the
 * index of the page to create. The createPage*() methods are called
 * upon to create the actual pages.
 */
public class ReadmeFilePropertyPage2 extends PropertyPage {

    /**
     * Utility method that creates a new composite and
     * sets up its layout data.
     *
     * @param parent  the parent of the composite
     * @param numColumns  the number of columns in the new composite
     * @return the newly-created composite
     */
    protected Composite createComposite(Composite parent, int numColumns) {
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = numColumns;
        composite.setLayout(layout);
        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        composite.setLayoutData(data);
        return composite;
    }

    /** (non-Javadoc)
     * Method declared on PreferencePage
     */
    public Control createContents(Composite parent) {
        // ensure the page has no special buttons
        noDefaultAndApplyButton();
        Composite panel = createComposite(parent, 2);

        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				IReadmeConstants.PROPERTY_PAGE2_CONTEXT);

        // layout the page
        int page = getPageIndex();
        switch (page) {
        case 1:
            createPageOne(panel);
            break;
        case 2:
            createPageTwo(panel);
            break;
        default:
        }
        return new Canvas(panel, 0);
    }

    /**
     * Utility method that creates a new label and sets up
     * its layout data.
     *
     * @param parent  the parent of the label
     * @param text  the text of the label
     * @return the newly-created label
     */
    protected Label createLabel(Composite parent, String text) {
        Label label = new Label(parent, SWT.LEFT);
        label.setText(text);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        label.setLayoutData(data);
        return label;
    }

    /**
     * Creates the first version of the page. This is a placeholder page which
     * notified the user that the page is not available.
     *
     * @param panel  the panel in which to create the page
     */
    protected void createPageOne(Composite panel) {
        Label l = createLabel(panel, MessageUtil
                .getString("Additional_Readme_properties_not_available.")); //$NON-NLS-1$
        GridData gd = (GridData) l.getLayoutData();
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        l = createLabel(
                panel,
                MessageUtil
                        .getString("This_illustrates_a_property_page_that_is_dynamically_determined")); //$NON-NLS-1$
        gd = (GridData) l.getLayoutData();
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        l = createLabel(
                panel,
                MessageUtil
                        .getString("not_to_be_available_based_on_the_state_of_the_object.")); //$NON-NLS-1$
        gd = (GridData) l.getLayoutData();
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
    }

    /**
     * Creates the second version of the page. This page might contain more information
     * about the file or other information.
     *
     * @param panel  the panel in which to create the page
     */
    protected void createPageTwo(Composite panel) {
        Label l = createLabel(
                panel,
                MessageUtil
                        .getString("The_size_of_the_Readme_file_is_at_least_256_bytes.")); //$NON-NLS-1$
        GridData gd = (GridData) l.getLayoutData();
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        l = createLabel(
                panel,
                MessageUtil
                        .getString("Had_it_been_less_than_256_bytes_this_page_would_be_a_placeholder_page.")); //$NON-NLS-1$
        gd = (GridData) l.getLayoutData();
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        l = createLabel(panel, MessageUtil.getString("Additional_information")); //$NON-NLS-1$
        gd = (GridData) l.getLayoutData();
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        l = createLabel(
                panel,
                MessageUtil
                        .getString("This_illustrates_a_property_page_that_is_dynamically_determined")); //$NON-NLS-1$
        gd = (GridData) l.getLayoutData();
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        l = createLabel(panel, MessageUtil
                .getString("to_be_available_based_on_the_state_of_the_object.")); //$NON-NLS-1$
        gd = (GridData) l.getLayoutData();
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
    }

    /**
     * Returns which page to display. This implementation
     * answers 1 if the size of the Readme file is less than 256 bytes
     * and 2 otherwise.
     *
     * @return the index of the page to display
     */
    protected int getPageIndex() {
        IResource resource = (IResource) getElement();

        if (resource.getType() == IResource.FILE) {
            InputStream contentStream = null;
            int length = 0;
            try {
                IFile file = (IFile) resource;
                contentStream = file.getContents();
                Reader in = new InputStreamReader(contentStream);
                int chunkSize = contentStream.available();
                StringBuffer buffer = new StringBuffer(chunkSize);
                char[] readBuffer = new char[chunkSize];
                int n = in.read(readBuffer);

                while (n > 0) {
                    buffer.append(readBuffer);
                    n = in.read(readBuffer);
                }

                contentStream.close();
                length = buffer.length();
            } catch (CoreException e) {
                length = 0;
            } catch (IOException e) {
                // do nothing
            } finally {
                if (contentStream != null) {
                    try {
                        contentStream.close();
                    } catch (IOException e) {
                        // do nothing
                    }
                }
            }

            if (length < 256)
                return 1;
            return 2;
        }

        return 0;
    }

    /** (non-Javadoc)
     * Method declared on PreferencePage
     */
    public boolean performOk() {
        // nothing to do - read-only page
        return true;
    }
}
