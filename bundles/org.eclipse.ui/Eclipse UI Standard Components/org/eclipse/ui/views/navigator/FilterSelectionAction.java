package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The FilterSelectionAction opens the filters dialog.
 */
public class FilterSelectionAction extends ResourceNavigatorAction {
	private static final String FILTER_TOOL_TIP = ResourceNavigatorMessages.getString("FilterSelection.toolTip"); //$NON-NLS-1$
	private static final String FILTER_SELECTION_MESSAGE = ResourceNavigatorMessages.getString("FilterSelection.message"); //$NON-NLS-1$
	private static final String FILTER_TITLE_MESSAGE = ResourceNavigatorMessages.getString("FilterSelection.title"); //$NON-NLS-1$
	
/**
 * Creates the action.
 * 
 * @param navigator the resource navigator
 * @param label the label for the action
 */
public FilterSelectionAction(IResourceNavigatorPart navigator, String label) {
	super(navigator, label);
	setToolTipText(FILTER_TOOL_TIP);
	WorkbenchHelp.setHelp(this, INavigatorHelpContextIds.FILTER_SELECTION_ACTION);
	setEnabled(true);
}

/*
 * Implementation of method defined on <code>IAction</code>.
 */
public void run() {

	ResourcePatternFilter filter = getNavigator().getPatternFilter();
	FiltersContentProvider contentProvider = new FiltersContentProvider(filter);

	ListSelectionDialog dialog =
		new ListSelectionDialog(
			getShell(),
			getResourceViewer(),
			contentProvider,
			new LabelProvider(),
			FILTER_SELECTION_MESSAGE);

	dialog.setTitle(FILTER_TITLE_MESSAGE);
	dialog.setInitialSelections(contentProvider.getInitialSelections());
	dialog.open();
	if (dialog.getReturnCode() == dialog.OK) {
		Object[] results = dialog.getResult();
		String[] selectedPatterns = new String[results.length];
		System.arraycopy(results, 0, selectedPatterns, 0, results.length);
		filter.setPatterns(selectedPatterns);
		getNavigator().setFiltersPreference(selectedPatterns);
		Viewer viewer = getResourceViewer();
		viewer.getControl().setRedraw(false);
		viewer.refresh();
		viewer.getControl().setRedraw(true);
	}
}

}
