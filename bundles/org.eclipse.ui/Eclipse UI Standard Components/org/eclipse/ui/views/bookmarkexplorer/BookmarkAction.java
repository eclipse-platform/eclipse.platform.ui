package org.eclipse.ui.views.bookmarkexplorer;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.jface.viewers.*;

/**
 * An abstract class for all bookmark view actions.
 */
/* package */ abstract class BookmarkAction extends SelectionProviderAction {
	private BookmarkNavigator view;
/**
 * Creates a bookmark action.
 */
protected BookmarkAction(BookmarkNavigator view, String label) {
	super(view.getViewer(), label);
	this.view = view;
}
/**
 * Returns the bookmarks view.
 */
public BookmarkNavigator getView() {
	return view;
}
}
