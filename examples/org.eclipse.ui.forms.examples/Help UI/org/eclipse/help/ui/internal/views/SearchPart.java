/*
 * Created on Dec 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.help.ui.internal.search.HelpSearchQuery;
import org.eclipse.help.ui.internal.search.HelpSearchResult;
import org.eclipse.help.ui.internal.search.SearchQueryData;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.examples.internal.ExamplesPlugin;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SearchPart extends SectionPart implements IHelpPart {
	private ReusableHelpPart parent;
	private Text phraseText;
	private Button goButton;
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
		section.marginWidth = 5;
		section.addExpansionListener(new IExpansionListener() {
			public void expansionStateChanging(ExpansionEvent e) {
				toggleSearchResults(e.getState());
			}
			public void expansionStateChanged(ExpansionEvent e) {
			}
		});
		Composite helpContainer = toolkit.createComposite(section);
		section.setClient(helpContainer);
		ImageHyperlink clearLink = new ImageHyperlink(section, SWT.NULL);
		toolkit.adapt(clearLink, true, true);
		clearLink.setToolTipText("Clear results");
		clearLink.setImage(ExamplesPlugin.getDefault().getImage(ExamplesPlugin.IMG_CLEAR));
		clearLink.setBackground(section.getTitleBarGradientBackground());		
		clearLink.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				doClear();
			}
		});
		section.setTextClient(clearLink);

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
		goButton = toolkit.createButton(helpContainer,
				"Go", SWT.PUSH); //$NON-NLS-1$
		goButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doSearch(phraseText.getText());
			}
		});
		goButton.setEnabled(false);
		phraseText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = phraseText.getText();
				goButton.setEnabled(text.length() > 0);
			}
		});
		phraseText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.character == '\r') {
					if (goButton.isEnabled())
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
	
	public void startWorkbenchSearch(String phrase) {
		SearchQueryData data = new SearchQueryData();
		data.setSearchWord(phrase);
		HelpSearchQuery query = new HelpSearchQuery(data);
		startWorkbenchSearch(query);
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
	private void doClear() {
		SearchResultsPart part = (SearchResultsPart)parent.findPart(IHelpViewConstants.SEARCH_RESULT);
		if (part!=null)
			part.clearResults();
	}
	private void toggleSearchResults(boolean visible) {
		SearchResultsPart part = (SearchResultsPart)parent.findPart(IHelpViewConstants.SEARCH_RESULT);
		if (part!=null)
			part.setVisible(visible);		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public boolean fillContextMenu(IMenuManager manager) {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#hasFocusControl(org.eclipse.swt.widgets.Control)
	 */
	public boolean hasFocusControl(Control control) {
		return phraseText.equals(control) || goButton.equals(control);
	}
}