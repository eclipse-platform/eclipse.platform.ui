package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.*;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.ResourceWorkingSetFilter;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.FilterDialog;

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
	IResourceNavigatorPart navigator = getNavigator();
	ResourcePatternFilter filter = navigator.getPatternFilter();
	FiltersContentProvider contentProvider = new FiltersContentProvider(filter);
	IWorkingSet workingSet = navigator.getWorkingSet();

	FilterDialog dialog =
		new FilterDialog(
			getShell(),
			getResourceViewer(),
			contentProvider,
			new LabelProvider(),
			FILTER_SELECTION_MESSAGE);

	dialog.setTitle(FILTER_TITLE_MESSAGE);
	dialog.setInitialSelections(contentProvider.getInitialSelections());
	dialog.setWorkingSet(workingSet);
	dialog.open();
	if (dialog.getReturnCode() == dialog.OK) {
		Object[] results = dialog.getResult();
		String[] selectedPatterns = new String[results.length];
		IWorkingSetManager workingSetManager = WorkbenchPlugin.getDefault().getWorkingSetManager();

		System.arraycopy(results, 0, selectedPatterns, 0, results.length);
		filter.setPatterns(selectedPatterns);
		workingSet = dialog.getWorkingSet();			
		navigator.setWorkingSet(workingSet);
		if (workingSet != null) {
			workingSetManager.addRecentWorkingSet(workingSet);
		}

		navigator.setFiltersPreference(selectedPatterns);
		Viewer viewer = getResourceViewer();
		viewer.getControl().setRedraw(false);
		viewer.refresh();
		viewer.getControl().setRedraw(true);
	}
	else
	if (navigator.getWorkingSet() != workingSet) {
		navigator.setWorkingSet(workingSet);
	}
}

}
