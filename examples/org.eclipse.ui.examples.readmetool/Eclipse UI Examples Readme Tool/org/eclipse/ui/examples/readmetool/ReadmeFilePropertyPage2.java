package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.help.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import java.io.*;

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

	WorkbenchHelp.setHelp(panel, new DialogPageContextComputer(this, IReadmeConstants.PROPERTY_PAGE2_CONTEXT));

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
	Label l = createLabel(panel, "Additional Readme properties not available.");
	GridData gd = (GridData) l.getLayoutData();
	gd.horizontalSpan = 2;
	gd.grabExcessHorizontalSpace = true;
	l = createLabel(panel, "This illustrates a property page that is dynamically determined");
	gd = (GridData) l.getLayoutData();
	gd.horizontalSpan = 2;
	gd.grabExcessHorizontalSpace = true;
	l = createLabel(panel, "not to be available based on the state of the object.");
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
	Label l = createLabel(panel, "The size of the Readme file is at least 256 bytes.");
	GridData gd = (GridData) l.getLayoutData();
	gd.horizontalSpan = 2;
	gd.grabExcessHorizontalSpace = true;
	l = createLabel(panel, "Had it been less than 256 bytes, this page would be a placeholder page.");
	gd = (GridData) l.getLayoutData();
	gd.horizontalSpan = 2;
	gd.grabExcessHorizontalSpace = true;
	l = createLabel(panel, "Additional information about the Readme file can go here.");
	gd = (GridData) l.getLayoutData();
	gd.horizontalSpan = 2;
	gd.grabExcessHorizontalSpace = true;
	l = createLabel(panel, "This illustrates a property page that is dynamically determined");
	gd = (GridData) l.getLayoutData();
	gd.horizontalSpan = 2;
	gd.grabExcessHorizontalSpace = true;
	l = createLabel(panel, "to be available based on the state of the object.");
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
			
			if (file.isLocal(IResource.DEPTH_ZERO)) {
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
			}
		} catch (CoreException e) {
			length = 0;
		} catch (IOException e) {
		} finally {
			if (contentStream != null) {
				try {
					contentStream.close();
				} catch (IOException e) {
				}
			}
		}
		
		if (length < 256)
			return 1;
		else
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
