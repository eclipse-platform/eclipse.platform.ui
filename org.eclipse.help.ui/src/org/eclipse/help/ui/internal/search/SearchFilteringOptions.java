package org.eclipse.help.ui.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;
import org.eclipse.help.IToc;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
/**
 * Displays Search Filtering Options dialog.
 */
public class SearchFilteringOptions {
	private CheckboxTreeViewer checkboxTreeViewer;
	private SearchQueryData queryData;
	private Collection excludedCategories; // = query.getExcludedCategories()
	public SearchFilteringOptions(Composite parent, SearchQueryData queryData) {
		this.queryData = queryData;
		excludedCategories = queryData.getExcludedCategories();
		if (excludedCategories == null)
			excludedCategories = new ArrayList();
		createControl(parent);
	}
	/**
	 * Fills in the dialog area with text and checkboxes
	 * @param the parent composite to contain the dialog area
	 * @return the dialog area control
	 */
	protected void createControl(Composite parent) {
		checkboxTreeViewer =
			new CheckboxTreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData();
		gd.horizontalAlignment = gd.END;
		gd.verticalAlignment = gd.VERTICAL_ALIGN_BEGINNING;
		checkboxTreeViewer.getControl().setLayoutData(gd);
		/** TO DO ..
		// Listen to check state changes
		checkboxTreeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getElement() instanceof IToc)
					checkboxTreeViewer.setSubtreeChecked(event.getElement(), event.getChecked());
				else if (event.getElement() instanceof ITopic) {
					if (((ITopic) event.getElement()).getParent() instanceof InfoView)
						updateTocSelection((IToc) ((ITopic) event.getElement()).getParent());
				}
			}
		});
		
		checkboxTreeViewer.setContentProvider(CheckboxTreeContentProvider.getDefault());
		checkboxTreeViewer.setLabelProvider(CheckboxLabelProvider.getDefault());
		checkboxTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		IToc toc =
			HelpSystem.getTocManager().getToc(queryData.getInfoset());
		checkboxTreeViewer.setInput(infoset);
		checkboxTreeViewer.setExpandedElements(infoset.getChildrenList().toArray());
		setExcludedCategories(queryData.getExcludedCategories());
		//checkboxTreeViewer.refresh();
		 */
	}
	/**
	 * Returns a list of Topics (categories) that are to be excluded from search
	 */
	public List getExcludedCategories() {
		ArrayList categories = new ArrayList();
		/* TO DO
		for (Iterator viewIterator = infoset.getChildren(); viewIterator.hasNext();) {
			InfoView view = (InfoView) viewIterator.next();
			for (Iterator topicIterator = view.getChildren(); topicIterator.hasNext();) {
				categories.add(topicIterator.next());
			}
		}
		Object[] checkedElements = checkboxTreeViewer.getCheckedElements();
		for (int i = 0; i < checkedElements.length; i++) {
			Contribution c = (Contribution) checkedElements[i];
			categories.remove(c);
		}
		*/
		return categories;
	}
	/**
	 * Selects checkboxes based on (categories) that are to be excluded from search
	 */
	public void setExcludedCategories(List categories) {
		/* TO DO
		InfoSet infoset = HelpSystem.getNavigationManager().getCurrentInfoSet();
		for (Iterator viewIterator = infoset.getChildren(); viewIterator.hasNext();) {
			InfoView view = (InfoView) viewIterator.next();
			// First set all, then un-check
			checkboxTreeViewer.setSubtreeChecked(view, true);
			for (Iterator topicIterator = view.getChildren(); topicIterator.hasNext();) {
				Contribution topic = (Contribution) topicIterator.next();
				if (categories != null && categories.contains(topic))
					checkboxTreeViewer.setChecked(topic, false);
			}
			updateInfoViewSelection(view);
		}
		*/
	}
	/**
	 * Selects InfoView if at least one of children topics is selected
	 * Deselects InfoView if none of children topics is selected
	 */
	private void updateTocSelection(IToc toc) {
		/*
		boolean viewSelected = false;
		for (Iterator topicIterator = view.getChildren(); topicIterator.hasNext();) {
			Contribution topic = (Contribution) topicIterator.next();
			if (checkboxTreeViewer.getChecked(topic)) {
				viewSelected = true;
				break;
			}
		}
		if (checkboxTreeViewer.getChecked(view) != viewSelected)
			checkboxTreeViewer.setChecked(view, viewSelected);
		*/
	}
}