package org.eclipse.ui.views.bookmarkexplorer;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.actions.SelectionProviderAction;

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
