/*
 * Created on Dec 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.help.ui.internal.search.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.search.ui.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.*;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SearchPart extends SectionPart implements IHelpPart {
	private ReusableHelpPart parent;
	private Text phraseText;
	private String id;
	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public SearchPart(Composite parent, FormToolkit toolkit) {
		super(parent, toolkit, Section.EXPANDED|Section.TWISTIE|Section.TITLE_BAR);
		Section section = getSection();
		section.setText("Search");
		Composite helpContainer = toolkit.createComposite(section);
		section.setClient(helpContainer);

		GridLayout glayout = new GridLayout();
		glayout.numColumns = 3;
		glayout.marginWidth = glayout.marginHeight = 1;
		helpContainer.setLayout(glayout);
		helpContainer.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		toolkit.paintBordersFor(helpContainer);
		Label label = toolkit.createLabel(helpContainer, "Search");
		label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		phraseText = toolkit.createText(helpContainer, ""); //$NON-NLS-1$
		phraseText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		final Button button = toolkit.createButton(helpContainer,
				"Go", SWT.PUSH); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doSearch(phraseText.getText());
			}
		});
		button.setEnabled(false);
		phraseText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = phraseText.getText();
				button.setEnabled(text.length() > 0);
			}
		});
		phraseText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.character == '\r') {
					if (button.isEnabled())
						doSearch(phraseText.getText());
				}
			}
		});
		toolkit.paintBordersFor(helpContainer);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#getControl()
	 */
	public Control getControl() {
		return getSection();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#init(org.eclipse.help.ui.internal.views.NewReusableHelpPart)
	 */
	public void init(ReusableHelpPart parent, String id) {
		this.parent = parent;
		this.id = id;
	}
	public String getId() {
		return id;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		getSection().setVisible(visible);
	}
	
	private void doSearch(String phrase) {
		SearchQueryData data = new SearchQueryData();
		//if (!parent.isInWorkbenchWindow()) 
			data.setMaxHits(5);
		data.setSearchWord(phrase);
		HelpSearchQuery query = new HelpSearchQuery(data);
		startInPlaceSearch(phrase, query);
	}
	private void startWorkbenchSearch(ISearchQuery query) {
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInForeground(parent.getRunnableContext(), query);
	}
	private void startInPlaceSearch(final String phrase, final ISearchQuery query) {
		final ISearchResult result = query.getSearchResult();
		final StringBuffer resultBuffer = new StringBuffer();
		result.addListener(new ISearchResultListener() {
			public void searchResultChanged(SearchResultEvent e) {
				final HelpSearchResult hresult = (HelpSearchResult) result;
				getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						updateResults(phrase, resultBuffer, hresult);
					}
				});
			}
		});
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				query.run(monitor);
			}
		};
		try {
			parent.getRunnableContext().run(true, true, op);
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		}
	}
	private void updateResults(String phrase, StringBuffer buffer, HelpSearchResult hresult) {
		SearchResultsPart part = (SearchResultsPart)parent.findPart(IHelpViewConstants.SEARCH_RESULT);
		if (part!=null)
			part.updateResults(phrase, buffer, hresult);
	}
}