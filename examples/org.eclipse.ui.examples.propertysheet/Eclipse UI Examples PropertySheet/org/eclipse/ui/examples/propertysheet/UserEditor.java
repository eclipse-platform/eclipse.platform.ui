package org.eclipse.ui.examples.propertysheet;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.text.Document;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

/**
 * This class implements the User editor.
 */
public class UserEditor extends TextEditor {
	private ContentOutlinePage userContentOutline;
/**
 * UserEditor default Constructor
 */
public UserEditor() {
	super();
}
/* (non-Javadoc)
 * Method declared on WorkbenchPart
 */
public void createPartControl(Composite parent) {
	super.createPartControl(parent);
	getSourceViewer().setDocument(new Document("Click on the items in the content outline\nto see their properties"));
}
/* (non-Javadoc)
 * Method declared on IAdaptable
 */
public Object getAdapter(Class adapter) {
	if (adapter.equals(IContentOutlinePage.class)) {
			return getContentOutline();
	}
	if (adapter.equals(IPropertySheetPage.class)) {
			return getPropertySheet();
	}
	return super.getAdapter(adapter);
}
/**
 * Returns the content outline.
 */
protected ContentOutlinePage getContentOutline() {
	if (userContentOutline == null) {
		//Create a property outline page using the parsed result of passing in the document provider.
		userContentOutline =
			new PropertySheetContentOutlinePage(
				new UserFileParser().parse(getDocumentProvider()));
	}
	return userContentOutline;
}
/**
 * Returns the property sheet.
 */
protected IPropertySheetPage getPropertySheet() {
	return new PropertySheetPage();
}
}
