package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import java.io.StringWriter;

/**
 * The FilterAction is the class that adds the filter views to a ResourceNavigator.
 */
public class FilterSelectionAction extends ResourceNavigatorAction {
	private static final String FILTER_TOOL_TIP = ResourceNavigatorMessages.getString("FilterSelection.toolTip"); //$NON-NLS-1$
	private static final String FILTER_SELECTION_MESSAGE = ResourceNavigatorMessages.getString("FilterSelection.message"); //$NON-NLS-1$
	
	private Shell shell;
/**
 * Create a new filter action
 * @param shell the shell that will be used for the list selection
 * @param navigator the resource navigator
 * @param label the label for the action
 */
public FilterSelectionAction(IResourceNavigatorPart navigator, String label) {
	super(navigator, label);
	setToolTipText(FILTER_TOOL_TIP);
	WorkbenchHelp.setHelp(this, new Object[] {INavigatorHelpContextIds.FILTER_SELECTION_ACTION});
	setEnabled(true);
}
/**
 * Implementation of method defined on <code>IAction</code>.
 */
public void run() {

	ResourcePatternFilter filter = this.getNavigator().getPatternFilter();
	FiltersContentProvider contentProvider = new FiltersContentProvider(filter);

	ListSelectionDialog dialog =
		new ListSelectionDialog(
			this.shell,
			this.getResourceViewer(),
			contentProvider,
			new LabelProvider(),
			FILTER_SELECTION_MESSAGE);

	dialog.setInitialSelections(contentProvider.getInitialSelections());
	dialog.open();
	if (dialog.getReturnCode() == dialog.OK) {
		Object[] results = dialog.getResult();
		String[] selectedPatterns = new String[results.length];
		System.arraycopy(results, 0, selectedPatterns, 0, results.length);
		filter.setPatterns(selectedPatterns);
		saveInPreferences(selectedPatterns);
		TreeViewer viewer = getResourceViewer();
		viewer.getControl().setRedraw(false);
		viewer.refresh();
		viewer.getControl().setRedraw(true);
	}
}
/**
 * Save the supplied patterns in the preferences for the UI plugin.
 * They are saved in the format: pattern,pattern.
 */
private void saveInPreferences(String[] patterns) {

	StringWriter writer = new StringWriter();

	for (int i = 0; i < patterns.length; i++) {
		if (i != 0)
			writer.write(ResourcePatternFilter.COMMA_SEPARATOR);
		writer.write(patterns[i]);
	}

	getNavigator().getPlugin().getPreferenceStore().setValue(
		ResourcePatternFilter.FILTERS_TAG,
		writer.toString());

}
}
