package org.eclipse.ui.views.contentoutline;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.help.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.widgets.Composite;

/**
 * Main class for the Content Outline View.
 * <p>
 * This standard view has id <code>"org.eclipse.ui.views.ContentOutline"</code>.
 * </p>
 * When a <b>content outline view</b> notices an editor being activated, it 
 * asks the editor whether it has a <b>content outline page</b> to include
 * in the outline view. This is done using <code>getAdapter</code>:
 * <pre>
 * IEditorPart editor = ...;
 * IContentOutlinePage outlinePage = (IContentOutlinePage) editor.getAdapter(IContentOutlinePage.class);
 * if (outlinePage != null) {
 *    // editor wishes to contribute outlinePage to content outline view
 * }
 * </pre>
 * If the editor supports a content outline page, the editor instantiates
 * and configures the page, and returns it. This page is then added to the 
 * content outline view (a pagebook which presents one page at a time) and 
 * immediately made the current page (the content outline view need not be
 * visible). If the editor does not support a content outline page, the content
 * outline view shows a special default page which makes it clear to the user
 * that the content outline view is disengaged. A content outline page is free
 * to report selection events; the content outline view forwards these events 
 * along to interested parties. When the content outline view notices a
 * different editor being activated, it flips to the editor's corresponding
 * content outline page. When the content outline view notices an editor being
 * closed, it destroys the editor's corresponding content outline page.
 * </p>
 * <p>
 * The workbench will automatically instantiate this class when a Content
 * Outline view is needed for a workbench window. This class was not intended
 * to be instantiated or subclassed by clients.
 * </p>
 */
public class ContentOutline extends PageBookView implements ISelectionProvider, ISelectionChangedListener {

	/**
	 * Help context id used for the content outline view
	 * (value <code>"org.eclipse.ui.general_help_context"</code>).
	 */
	private static java.util.ResourceBundle resoutline_nls = java.util.ResourceBundle.getBundle("org.eclipse.ui.views.contentoutline.messages");  //$NON-NLS-1$
	public static final String CONTENT_OUTLINE_VIEW_HELP_CONTEXT_ID = "org.eclipse.ui.general_help_context";//$NON-NLS-1$

	/**
	 * Message to show on the default page.
	 */
	private String defaultText = resoutline_nls.getString("ContentOutline.noOutline"); //$NON-NLS-1$

	/**
	 * Selection change listeners.
	 */
	private ListenerList selectionChangedListeners = new ListenerList();
/**
 * Creates a content outline view with no content outline pages.
 */
public ContentOutline() {
	super();
}
/* (non-Javadoc)
 * Method declared on ISelectionProvider.
 */
public void addSelectionChangedListener(ISelectionChangedListener listener) {
	selectionChangedListeners.add(listener);	
}
/* (non-Javadoc)
 * Method declared on PageBookView.
 */
protected IPage createDefaultPage(PageBook book) {
	MessagePage page = new MessagePage();
	page.createControl(book);
	page.setMessage(defaultText);
	return page;
}
/**
 * The <code>PageBookView</code> implementation of this <code>IWorkbenchPart</code>
 * method creates a <code>PageBook</code> control with its default page showing.
 */
public void createPartControl(Composite parent) {
	super.createPartControl(parent);
	WorkbenchHelp.setHelp(getPageBook(), new Object[] {CONTENT_OUTLINE_VIEW_HELP_CONTEXT_ID});
}
/* (non-Javadoc)
 * Method declared on PageBookView.
 */
protected PageRec doCreatePage(IWorkbenchPart part) 
{
	// Try to get an outline page.
	Object obj = part.getAdapter(IContentOutlinePage.class);
	if (obj instanceof IContentOutlinePage) {
		IContentOutlinePage page = (IContentOutlinePage)obj;
		page.createControl(getPageBook());
		page.addSelectionChangedListener(this);
		return new PageRec(part, page);
	}
	// There is no content outline
	return null;
}
/* (non-Javadoc)
 * Method declared on PageBookView.
 */
protected void doDestroyPage(IWorkbenchPart part, PageRec rec) {
	IContentOutlinePage page = (IContentOutlinePage) rec.page;
	page.removeSelectionChangedListener(this);
	page.dispose();
	rec.dispose();
}
/* (non-Javadoc)
 * Method declared on IAdaptable.
 */
public Object getAdapter(Class key) {
	if (key == IContributedContentsView.class)
		return new IContributedContentsView () {
			public IWorkbenchPart getContributingPart() {
				return getContributingEditor();
			}
		};
	return null;
}
/* (non-Javadoc)
 * Method declared on PageBookView.
 */
protected IWorkbenchPart getBootstrapPart() {
	IWorkbenchPage page = getSite().getPage();
	if (page != null)
		return page.getActiveEditor();
	else
		return null;
}
/**
 * Returns the editor which contributed the current 
 * page to this view.
 *
 * @return the editor which contributed the current page
 * or <code>null</code> if no editor contributed the current page
 */
private IWorkbenchPart getContributingEditor() {
	return getCurrentContributingPart();
}
/* (non-Javadoc)
 * Method declared on ISelectionProvider.
 */
public ISelection getSelection() {
	// get the selection from the current page
	if (getCurrentPage() instanceof IContentOutlinePage) {
		return ((IContentOutlinePage)getCurrentPage()).getSelection();
	} else {
		return StructuredSelection.EMPTY;
	}
}
/* (non-Javadoc)
 * Method declared on IViewPart.
 */
public void init(IViewSite site) throws PartInitException {
	site.setSelectionProvider(this);
	super.init(site);
}
/* (non-Javadoc)
 * Method declared on PageBookView.
 * We only want to track editors.
 */
protected boolean isImportant(IWorkbenchPart part) {
	//We only care about editors
	return (part instanceof IEditorPart);
}
/* (non-Javadoc)
 * Method declared on IViewPart.
 * Treat this the same as part activation.
 */
public void partBroughtToTop(IWorkbenchPart part) {
	partActivated(part);
}
/* (non-Javadoc)
 * Method declared on ISelectionProvider.
 */
public void removeSelectionChangedListener(ISelectionChangedListener listener) {
	selectionChangedListeners.remove(listener);
}
/* (non-Javadoc)
 * Method declared on ISelectionChangedListener.
 */
public void selectionChanged(SelectionChangedEvent event) {
	// fire the event
	Object[] listeners = selectionChangedListeners.getListeners();
	for (int i = 0; i < listeners.length; ++i) {
		((ISelectionChangedListener) listeners[i]).selectionChanged(event);
	}
}
/* (non-Javadoc)
 * Method declared on ISelectionProvider.
 */
public void setSelection(ISelection selection) {
	if (getCurrentPage() instanceof IContentOutlinePage)
		((IContentOutlinePage)getCurrentPage()).setSelection(selection);
}
}
