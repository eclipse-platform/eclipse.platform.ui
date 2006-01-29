/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.window.Window;

import org.eclipse.search.ui.NewSearchUI;

import org.eclipse.search.internal.ui.util.SWTUtil;

import org.eclipse.search2.internal.ui.SearchMessages;

/**
 * @author markus.schorn@windriver.com
 */
public class RetrieverFindTab implements IRetrieverKeys {
	private RetrieverPage fView;
	private Combo fWorkingSetCombo;
	private Button fChooseWorkingSetButton;
	private Combo fSearchString;
	private Button fCaseSensitive;
	private Button fRegularExpression;
	private Button fWholeWord;
	private Button fSearchButton;
	private Combo fFilePatterns;
	private Button fFilePatternButton;
	private Control[] fEnableDisable;
	private Map fScopeMap= new HashMap();
	private int fPermanentScopeCount;

	RetrieverFindTab(RetrieverPage view) {
		fView= view;
	}

	public void createControl(Composite group) {
		GridData gd;
		GridLayout gl;
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setLayout(new GridLayout(3, false));

		fSearchString= new Combo(group, SWT.NONE);
		fSearchString.setLayoutData(gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		fSearchString.setVisibleItemCount(VISIBLE_ITEMS_IN_COMBO);
		gd.horizontalSpan= 2;
		gd.widthHint= 20; // spr 112201

		fSearchButton= new Button(group, SWT.PUSH);
		fSearchButton.setText(SearchMessages.RetrieverFindTab_search);
		fSearchButton.setLayoutData(gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		gd.horizontalSpan= 1;
		SWTUtil.setButtonDimensionHint(fSearchButton);

		Composite comp= new Composite(group, SWT.NONE);
		comp.setLayoutData(gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		gd.horizontalSpan= 3;
		comp.setLayout(gl= new GridLayout(3, false));
		gl.marginHeight= gl.marginWidth= 0;

		fCaseSensitive= new Button(comp, SWT.CHECK);
		fCaseSensitive.setText(SearchMessages.RetrieverFindTab_caseSensitive);

		fRegularExpression= new Button(comp, SWT.CHECK);
		fRegularExpression.setText(SearchMessages.RetrieverFindTab_regularExpression);

		fWholeWord= new Button(comp, SWT.CHECK);
		fWholeWord.setText(SearchMessages.RetrieverFindTab_wholeWord);

		Label separator= new Label(group, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		gd.horizontalSpan= 3;
		new Label(group, SWT.NONE).setLayoutData(gd= new GridData());
		gd.horizontalSpan= 3;
		gd.heightHint= 0;

		Label scopeLabel= new Label(group, SWT.NONE);
		scopeLabel.setText(SearchMessages.RetrieverFindTab_searchScope);

		fWorkingSetCombo= new Combo(group, SWT.BORDER | SWT.READ_ONLY);
		fWorkingSetCombo.setVisibleItemCount(VISIBLE_ITEMS_IN_COMBO);
		fWorkingSetCombo.setLayoutData(gd= new GridData(GridData.FILL_HORIZONTAL));
		gd.widthHint= 20;

		fChooseWorkingSetButton= new Button(group, SWT.PUSH);
		fChooseWorkingSetButton.setText(SearchMessages.RetrieverFindTab_choose);
		fChooseWorkingSetButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		SWTUtil.setButtonDimensionHint(fChooseWorkingSetButton);

		Label filePatternLabel= new Label(group, SWT.NONE);
		filePatternLabel.setText(SearchMessages.RetrieverFindTab_filePatterns);

		fFilePatterns= new Combo(group, SWT.NONE);
		fFilePatterns.setVisibleItemCount(VISIBLE_ITEMS_IN_COMBO);
		fFilePatterns.setLayoutData(gd= new GridData(GridData.FILL_HORIZONTAL));
		gd.widthHint= 20;

		fFilePatternButton= new Button(group, SWT.PUSH);
		fFilePatternButton.setText(SearchMessages.RetrieverFindTab_choose);
		fFilePatternButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		SWTUtil.setButtonDimensionHint(fFilePatternButton);

		fEnableDisable= new Control[] {fSearchString, fSearchButton, fCaseSensitive, fRegularExpression, fWholeWord, scopeLabel, fWorkingSetCombo, fChooseWorkingSetButton, filePatternLabel, fFilePatterns, fFilePatternButton};

		setupScopeCombo();
	}

	// overrider
	public void setFocus() {
		fSearchString.setSelection(new Point(0, fSearchString.getText().length()));
		fSearchString.setFocus();
	}

	public void dispose() {
	}

	void storeValues() {
		IScopeDescription scope= getSearchScope();
		fView.storeComboContent(fFilePatterns, KEY_FILE_PATTERNS, MAX_FILE_PATTERNS);
		fView.storeComboContent(fSearchString, KEY_SEARCH_STRINGS, MAX_SEARCH_STRINGS);
		fView.storeButton(fCaseSensitive, KEY_CASE_SENSITIVE_SEARCH);
		fView.storeButton(fRegularExpression, KEY_REGULAR_EXPRESSION_SEARCH);
		fView.storeButton(fWholeWord, KEY_WHOLE_WORD);
		storeScopeCombo();

		RetrieverPage.sLastFilePatterns= fFilePatterns.getText();
		RetrieverPage.sLastSearchPattern= fSearchString.getText();
		RetrieverPage.sLastIsCaseSensitive= fCaseSensitive.getSelection();
		RetrieverPage.sLastIsRegularExpression= fRegularExpression.getSelection();
		RetrieverPage.sLastIsWholeWord= fWholeWord.getSelection();
		RetrieverPage.sLastScope= scope;
	}

	private void storeScopeCombo() {
		String[] names= fWorkingSetCombo.getItems();
		fView.storeValue(names, KEY_SCOPE_HISTORY);
		for (int i= fPermanentScopeCount; i < names.length; i++) {
			String name= names[i];
			IScopeDescription scope= (IScopeDescription) fScopeMap.get(name);
			if (scope != null) {
				fView.storeScope(scope, KEY_SCOPE_HISTORY + "-" + name); //$NON-NLS-1$
			}
		}
		IScopeDescription scope= getSearchScope();
		fView.storeScope(scope, KEY_SEARCH_SCOPE);
	}

	void restoreValues() {
		fView.restoreCombo(fFilePatterns, KEY_FILE_PATTERNS, "*.c,*.ci,*.h,*.java"); //$NON-NLS-1$
		fView.restoreCombo(fSearchString, KEY_SEARCH_STRINGS, null);
		fView.restoreButton(fCaseSensitive, KEY_CASE_SENSITIVE_SEARCH, true);
		fView.restoreButton(fRegularExpression, KEY_REGULAR_EXPRESSION_SEARCH, false);
		fView.restoreButton(fWholeWord, KEY_WHOLE_WORD, true);
		fFilePatterns.clearSelection();
		setupScopeCombo();
	}

	private void setupScopeCombo() {
		fWorkingSetCombo.removeAll();
		fPermanentScopeCount= 4;
		addScopeToCombo(new WorkspaceScopeDescription(), false);
		addScopeToCombo(new CurrentProjectScopeDescription(), false);
		addScopeToCombo(new CurrentFileScopeDescription(), false);
		addScopeToCombo(new WindowWorkingSetScopeDescription(), false);

		String[] names= fView.restoreValue(KEY_SCOPE_HISTORY, null);
		if (names != null) {
			for (int i= names.length - 1; i >= 0; i--) {
				String name= names[i];
				IScopeDescription scope= fView.restoreScope(KEY_SCOPE_HISTORY + "-" + name); //$NON-NLS-1$
				if (scope != null) {
					addScopeToCombo(scope, false);
				}
			}
		}
		IScopeDescription scope= fView.restoreScope(KEY_SEARCH_SCOPE);
		addScopeToCombo(scope, false);
	}

	void createListeners() {
		fSearchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onSearch();
			}
		});
		fSearchString.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				String text= fSearchString.getText();
				if ((e.character == SWT.LF || e.character == SWT.CR) && text.length() > 0) {
					onSearch();
				}
				RetrieverPage.sLastSearchPattern= text;
			}
		});
		fChooseWorkingSetButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onChooseButtonSelectWorkingSet();
			}
		});
		fFilePatternButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onChooseButtonSelectFilePatterns();
			}
		});
		fSearchString.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RetrieverPage.sLastSearchPattern= fSearchString.getText();
			}
		});
		fCaseSensitive.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RetrieverPage.sLastIsCaseSensitive= fCaseSensitive.getSelection();
			}
		});
		fRegularExpression.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RetrieverPage.sLastIsRegularExpression= fRegularExpression.getSelection();
			}
		});
		fWholeWord.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RetrieverPage.sLastIsWholeWord= fWholeWord.getSelection();
			}
		});
		fFilePatterns.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RetrieverPage.sLastFilePatterns= fFilePatterns.getText();
			}
		});
		fWorkingSetCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RetrieverPage.sLastScope= getSearchScope();
			}
		});
	}

	protected void onSearch() {
		// first do a few checks!
		String searchText= fSearchString.getText();
		if (searchText.length() == 0) {
			fView.showError(SearchMessages.RetrieverFindTab_Error_emptySearchString);
			return;
		}
		if (fRegularExpression.getSelection()) {
			Pattern pattern= null;
			try {
				pattern= Pattern.compile(searchText);
			} catch (Exception e) {
				fView.showError(SearchMessages.RetrieverFindTab_Error_invalidRegex + e.getMessage());
				return;
			}

			if (pattern.matcher("").find()) { //$NON-NLS-1$
				boolean cont= fView.showQuestion(SearchMessages.RetrieverFindTab_Question_regexMatchesEmptyString);
				if (!cont) {
					return;
				}
			}
		}

		fView.storeValues();
		RetrieverQuery query= new RetrieverQuery(fView.getSite().getPage());
		query.setSearchString(RetrieverPage.sLastSearchPattern);
		query.setIsCaseSensitive(RetrieverPage.sLastIsCaseSensitive);
		query.setIsRegularExpression(RetrieverPage.sLastIsRegularExpression);
		query.setIsWholeWord(RetrieverPage.sLastIsWholeWord);
		query.setSearchScope(RetrieverPage.sLastScope, RetrieverPage.sLastConsiderDerivedResources);
		query.setFilePatterns(RetrieverPage.sLastFilePatterns, RetrieverPage.sLastUseCaseSensitiveFilePatterns);
		query.setSearchOrder(fView.getPreferredSearchOrder());

		RetrieverResult result= (RetrieverResult) query.getSearchResult();
		fView.initFilter(result);
		fView.storeValues();
		NewSearchUI.runQueryInBackground(query, fView.getViewPart());
	}

	private IScopeDescription getSearchScope() {
		String scopeLabel= fWorkingSetCombo.getText();
		IScopeDescription result= (IScopeDescription) fScopeMap.get(scopeLabel);
		return result == null ? new WorkspaceScopeDescription() : result;
	}

	protected void onChooseButtonSelectWorkingSet() {
		IScopeDescription scope= getSearchScope();
		IScopeDescription use= WorkingSetScopeDescription.createWithDialog(fView.getSite().getPage(), scope);
		if (use != null) {
			addScopeToCombo(use, false);
		}
	}

	protected void onChooseButtonSelectFilePatterns() {
		FilePatternSelectionDialog dlg= new FilePatternSelectionDialog(fView.getSite().getShell(), fFilePatterns.getText());
		if (dlg.open() == Window.OK) {
			setCombo(fFilePatterns, dlg.getFilePatterns(), 0, MAX_FILE_PATTERNS, false);
		}
	}

	void updateEnablement(boolean searchInProgress) {
		for (int i= 0; i < fEnableDisable.length; i++) {
			fEnableDisable[i].setEnabled(!searchInProgress);
		}
	}

	public void onSelected() {
		setFocus();
	}

	public void storeValues(Properties props) {
		props.setProperty(KEY_FILE_PATTERNS, fFilePatterns.getText());
		props.setProperty(KEY_SEARCH_STRINGS, fSearchString.getText());
		storeButton(props, KEY_CASE_SENSITIVE_SEARCH, fCaseSensitive);
		storeButton(props, KEY_REGULAR_EXPRESSION_SEARCH, fRegularExpression);
		storeButton(props, KEY_WHOLE_WORD, fWholeWord);
		storeScope(props, getSearchScope());
	}

	private void storeScope(Properties props, IScopeDescription searchScope) {
		props.setProperty(KEY_SCOPE_DESCRIPTOR_CLASS, searchScope.getClass().getName());
		searchScope.store(props, KEY_SEARCH_SCOPE);
	}

	private void storeButton(Properties props, String key, Button button) {
		props.setProperty(key, String.valueOf(button.getSelection()));
	}

	public void restoreValues(Properties props) {
		restoreComboText(props, KEY_FILE_PATTERNS, fFilePatterns);
		restoreComboText(props, KEY_SEARCH_STRINGS, fSearchString);
		restoreButton(props, KEY_CASE_SENSITIVE_SEARCH, fCaseSensitive);
		restoreButton(props, KEY_REGULAR_EXPRESSION_SEARCH, fRegularExpression);
		restoreButton(props, KEY_WHOLE_WORD, fWholeWord);

		IScopeDescription scope= restoreScope(props);
		addScopeToCombo(scope, false);
	}

	private IScopeDescription restoreScope(Properties props) {
		IScopeDescription scope= null;
		try {
			String scopeDescClass= props.getProperty(KEY_SCOPE_DESCRIPTOR_CLASS);
			scope= (IScopeDescription) Class.forName(scopeDescClass).newInstance();
			scope.restore(props, KEY_SEARCH_SCOPE);
		} catch (Exception e) {
			scope= new WorkspaceScopeDescription();
		}
		return scope;
	}

	private void restoreComboText(Properties props, String key, Combo combo) {
		String property= props.getProperty(key);
		if (property != null) {
			combo.setText(property);
		}
	}

	private void restoreButton(Properties props, String key, Button button) {
		String property= props.getProperty(key);
		if (property != null) {
			button.setSelection(Boolean.valueOf(property).booleanValue());
		}
	}

	public void restoreFromQuery(RetrieverQuery rq) {
		fCaseSensitive.setSelection(rq.isCaseSensitive());
		fRegularExpression.setSelection(rq.isRegularExpression());
		fWholeWord.setSelection(rq.isWholeWord());

		String searchFor= rq.getSearchText();
		if (searchFor != null && searchFor.length() > 0) {
			setCombo(fSearchString, rq.getSearchText(), 0, MAX_SEARCH_STRINGS, true);
		}
		setCombo(fFilePatterns, rq.getFilePatterns(), 0, MAX_FILE_PATTERNS, true);
		addScopeToCombo(rq.getScopeDescription(), true);
	}

	private void addScopeToCombo(IScopeDescription scope, boolean moveExisting) {
		String scopeLabel= scope.getLabel();
		fScopeMap.put(scopeLabel, scope);
		setCombo(fWorkingSetCombo, scope.getLabel(), fPermanentScopeCount, MAX_SCOPES, moveExisting);
		RetrieverPage.sLastScope= scope;
	}

	private void setCombo(Combo combo, String value, int insertPos, int maxItems, boolean moveExisting) {
		int idx= combo.indexOf(value);
		if (idx < 0) {
			insertPos= Math.min(combo.getItemCount(), insertPos);
			combo.add(value, insertPos);
			int itemCount= combo.getItemCount();
			if (itemCount > maxItems) {
				combo.remove(maxItems, itemCount - 1);
			}
			idx= insertPos;
		} else
			if (moveExisting && idx > insertPos) {
				combo.remove(idx);
				combo.add(value, insertPos);
			}
		combo.setText(value);
	}
}
