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

import java.util.Properties;
import java.util.regex.Pattern;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;

import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IFile;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import org.eclipse.search.ui.IContextMenuConstants;

import org.eclipse.search.internal.ui.util.SWTUtil;

import org.eclipse.search2.internal.ui.InternalSearchUI;
import org.eclipse.search2.internal.ui.SearchMessages;

/**
 * @author markus.schorn@windriver.com
 */
public class RetrieverReplaceTab implements IRetrieverKeys {
	private RetrieverPage fView;

	private Combo fReplacementCombo;
	private Button fNextButton;
	private Button fReplaceNextButton;
	private Button fRestoreNextButton;
	private Button fReplaceRestoreButton;
	private Button fReplaceAllButton;
	private Button fRestoreAllButton;
	private Text fPreviewLine;
	private Pattern fSearchPattern;
	private boolean fSearchInProgress;

	private RetrieverMatch fMatchOfPreview;


	public RetrieverReplaceTab(RetrieverPage view) {
		fView= view;
	}

	public void createControl(Composite parent) {
		GridLayout gl;
		GridData gd;

		Composite group= new Composite(parent, SWT.NONE);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setLayout(gl= new GridLayout(2, false));
		gl.marginHeight= gl.marginWidth= 0;

		// line 1
		Label l= new Label(group, SWT.NONE);
		l.setText(SearchMessages.RetrieverReplaceTab_ReplaceWith_label);

		fReplacementCombo= new Combo(group, SWT.NONE);
		fReplacementCombo.setVisibleItemCount(VISIBLE_ITEMS_IN_COMBO);
		fReplacementCombo.setLayoutData(gd= new GridData(GridData.FILL_HORIZONTAL));
		gd.widthHint= 20;

		// line 2
		group= new Composite(parent, SWT.NONE);
		group.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		group.setLayout(gl= new GridLayout(1, true));
		gl.marginHeight= gl.marginWidth= 0;

		Composite lg= new Composite(group, SWT.NONE);
		lg.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		lg.setLayout(gl= new GridLayout(3, true));
		gl.marginHeight= gl.marginWidth= 0;
		gl.verticalSpacing= gl.horizontalSpacing= 2;

		fNextButton= new Button(lg, SWT.NONE);
		fNextButton.setText(SearchMessages.RetrieverReplaceTab_Find_text);
		fNextButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		SWTUtil.setButtonDimensionHint(fNextButton);

		fReplaceNextButton= new Button(lg, SWT.NONE);
		fReplaceNextButton.setText(SearchMessages.RetrieverReplaceTab_ReplaceFind_text);
		fReplaceNextButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		SWTUtil.setButtonDimensionHint(fReplaceNextButton);

		fRestoreNextButton= new Button(lg, SWT.NONE);
		fRestoreNextButton.setText(SearchMessages.RetrieverReplaceTab_RestoreFind_text);
		fRestoreNextButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		SWTUtil.setButtonDimensionHint(fRestoreNextButton);

		fReplaceRestoreButton= new Button(lg, SWT.NONE);
		fReplaceRestoreButton.setText(SearchMessages.RetrieverReplaceTab_Replace_text);
		fReplaceRestoreButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		SWTUtil.setButtonDimensionHint(fReplaceRestoreButton);

		fReplaceAllButton= new Button(lg, SWT.NONE);
		fReplaceAllButton.setText(SearchMessages.RetrieverReplaceTab_ReplaceAll_text);
		fReplaceAllButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		SWTUtil.setButtonDimensionHint(fReplaceAllButton);

		fRestoreAllButton= new Button(lg, SWT.NONE);
		fRestoreAllButton.setText(SearchMessages.RetrieverReplaceTab_RestoreAll_text);
		fRestoreAllButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		SWTUtil.setButtonDimensionHint(fRestoreAllButton);

		// line 4
		group= new Composite(parent, SWT.NONE);
		group.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		group.setLayout(gl= new GridLayout(1, true));
		gl.marginHeight= gl.marginWidth= 0;
		gl.verticalSpacing= 2;

		l= new Label(group, SWT.NONE);
		l.setText(SearchMessages.RetrieverReplaceTab_Preview_label);

		fPreviewLine= new Text(group, SWT.READ_ONLY | SWT.BORDER);
		fPreviewLine.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	void createListeners(Viewer resultViewer) {
		resultViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				onSelectionChanged((IStructuredSelection) event.getSelection());
			}
		});

		fReplacementCombo.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {
				onReplacementChange();
			}
		});
		fReplacementCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onReplacementChange();
			}
		});
		fNextButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fView.gotoNextMatch();
			}
		});
		fReplaceRestoreButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (fMatchOfPreview != null) {
					onReplace(!fMatchOfPreview.isReplaced(), false, fReplaceRestoreButton.getText());
				}
			}
		});
		fReplaceNextButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onReplace(true, true, fReplaceNextButton.getText());
			}
		});
		fRestoreNextButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onReplace(false, true, fRestoreNextButton.getText());
			}
		});
		fReplaceAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onReplaceAll(true, fReplaceAllButton.getText());
			}
		});
		fRestoreAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onReplaceAll(false, fRestoreAllButton.getText());
			}
		});
	}

	void createActions() {
	}

	protected void onReplacementChange() {
		updatePreview(fView.getSelection());
	}

	protected void updatePreview(ISelection sel) {
		fMatchOfPreview= (RetrieverMatch) fView.getCurrentMatch();
		if (fMatchOfPreview == null) {
			fMatchOfPreview= getMatchOfSelection(sel);
		}
		String preview= null;
		if (fMatchOfPreview != null) {
			preview= computePreview(fMatchOfPreview);
		}
		if (preview == null) {
			preview= ""; //$NON-NLS-1$
		}
		fPreviewLine.setText(preview);
	}

	private RetrieverMatch getMatchOfSelection(ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			Object element= ((IStructuredSelection) sel).getFirstElement();
			if (element instanceof RetrieverLine) {
				RetrieverMatch[] matches= ((RetrieverLine) element).getDisplayedMatches();
				if (matches.length > 0) {
					return matches[0];
				}
			}
		}
		return null;
	}

	protected void onSelectionChanged(IStructuredSelection sel) {
		// compute preview
		updatePreview(sel);

		if (fSearchInProgress) {
			fReplaceRestoreButton.setEnabled(false);
			fReplaceNextButton.setEnabled(false);
			fRestoreNextButton.setEnabled(false);
			fReplaceAllButton.setEnabled(false);
			fRestoreAllButton.setEnabled(false);
			return;
		}

		if (fMatchOfPreview != null) {
			boolean replaced= fMatchOfPreview.isReplaced();
			fReplaceRestoreButton.setEnabled(true);
			fReplaceRestoreButton.setText(replaced ? SearchMessages.RetrieverReplaceTab_Restore_text : SearchMessages.RetrieverReplaceTab_Replace_text);
			fReplaceNextButton.setEnabled(!replaced);
			fRestoreNextButton.setEnabled(replaced);
		}
		fReplaceAllButton.setEnabled(true);
		fRestoreAllButton.setEnabled(true);

	}

	private String computePreview(RetrieverMatch match) {
		RetrieverLine line= match.getLine();
		IFile file= line.getParent();
		IDocument doc= null;
		if (file != null) {
			IPath path= file.getFullPath();
			ITextFileBuffer txfbuf= FileBuffers.getTextFileBufferManager().getTextFileBuffer(path);
			if (txfbuf != null) {
				doc= txfbuf.getDocument();
			}
		}

		String pre, post;
		if (doc != null) {
			int offset1= match.getOffset();
			int offset2= offset1 + match.getLength();
			Position pos= InternalSearchUI.getInstance().getPositionTracker().getCurrentPosition(match);
			if (pos != null) {
				offset1= pos.offset;
				offset2= offset1 + pos.length;
			}
			if (0 > offset1 || offset1 > offset2 || offset2 > doc.getLength()) {
				return null;
			}
			String matchstr;
			try {
				int offset0= offset1;
				int min= Math.max(0, offset1 - 1024);
				while (offset0 > min) {
					char c= doc.getChar(offset0 - 1);
					if (c == '\n' || c == '\r') {
						break;
					}
					offset0--;
				}
				int offset3= offset2;
				int max= Math.min(doc.getLength(), offset2 + 1024);
				while (offset3 < max) {
					char c= doc.getChar(offset3);
					if (c == '\n' || c == '\r') {
						break;
					}
					offset3++;
				}
				// don't accept trailing \r
				if (offset3 > offset2 && doc.getChar(offset3 - 1) == '\r') {
					offset3--;
				}
				pre= doc.get(offset0, offset1 - offset0);
				matchstr= doc.get(offset1, offset2 - offset1);
				post= doc.get(offset2, offset3 - offset2);
			} catch (BadLocationException e) {
				return null;
			}
			if (!matchstr.equals(match.getCurrentText())) {
				return null;
			}
		} else {
			pre= line.getPreString(match);
			post= line.getPostString(match);
		}

		StringBuffer result= new StringBuffer();
		result.append(pre);
		if (match.isReplaced()) {
			result.append(match.getOriginal());
		} else {
			String unescapedReplacement= getUnescapedReplacementString();
			String replacement= match.computeReplacement(fSearchPattern, unescapedReplacement);
			if (replacement == null) {
				return null;
			}
			result.append(replacement);
		}
		result.append(post);
		return RetrieverLabelProvider.convertChars(result);
	}

	private String getUnescapedReplacementString() {
		return unescapeWhitespace(fReplacementCombo.getText());
	}

	private String unescapeWhitespace(String input) {
		StringBuffer result= new StringBuffer(input.length());
		int i= 0;
		while (i < input.length() - 1) {
			char c= input.charAt(i);
			i++;
			if (c == '\\') {
				c= input.charAt(i);
				i++;
				switch (c) {
					case 't':
						result.append('\t');
						break;
					case 'r':
						result.append('\r');
						break;
					case 'n':
						result.append('\n');
						break;
					case '\\':
						result.append('\\');
						result.append('\\');
						break;
					default:
						result.append('\\');
						result.append(c);
						break;
				}
			} else {
				result.append(c);
			}
		}
		if (i < input.length()) {
			result.append(input.charAt(i));
		}

		return result.toString();
	}

	protected void onReplace(boolean replace, boolean moveOn, String actionLabel) {
		storeValues();
		if (fMatchOfPreview != null) {
			ReplaceAction op= new ReplaceAction(fView, actionLabel, replace, fMatchOfPreview);
			op.setReplacement(fSearchPattern, getUnescapedReplacementString());
			op.run();
			if (fView.getCurrentMatch() == null) {
				fView.gotoNextMatch();
			}
			if (moveOn && op.wasSuccessful()) {
				fView.gotoNextMatch();
			} else {
				onSelectionChanged(fView.getSelection());
			}
		}
	}

	protected void onReplaceAll(boolean replace, String actionLabel) {
		storeValues();
		ReplaceAction op= new ReplaceAction(fView, actionLabel, replace);
		op.setReplacement(fSearchPattern, getUnescapedReplacementString());
		op.run();
	}

	void storeValues() {
		fView.storeComboContent(fReplacementCombo, KEY_REPLACEMENT_STRING, MAX_REPLACEMENT_STRINGS);
	}

	void restoreValues() {
		fView.restoreCombo(fReplacementCombo, KEY_REPLACEMENT_STRING, ""); //$NON-NLS-1$
		updateEnablement(true);
	}

	public void setPattern(Pattern pattern) {
		fSearchPattern= pattern;
	}

	public void updateEnablement(boolean searchInProgress) {
		if (fSearchInProgress != searchInProgress) {
			fSearchInProgress= searchInProgress;
			onSelectionChanged(fView.getSelection());
		}
	}

	public void onSelected() {
		fReplacementCombo.setFocus();
	}

	public void getProperties(Properties props) {
		props.setProperty(KEY_REPLACEMENT_STRING, fReplacementCombo.getText());
	}

	public void setProperties(Properties props) {
		String property= props.getProperty(KEY_REPLACEMENT_STRING);
		if (property != null) {
			fReplacementCombo.setText(property);
			onReplacementChange();
		}
	}

	public void fillContextMenu(IMenuManager mgr) {
		IStructuredSelection sel= (IStructuredSelection) fView.getTreeViewer().getSelection();
		String replacement= getUnescapedReplacementString();
		if (sel != null && !sel.isEmpty()) {
			ReplaceAction replaceAction= new ReplaceAction(fView, SearchMessages.RetrieverReplaceTab_ReplaceSelected_text, true, sel);
			if (replaceAction.isEnabled()) {
				replaceAction.setReplacement(fSearchPattern, replacement);
				mgr.appendToGroup(IContextMenuConstants.GROUP_REORGANIZE, replaceAction);
			}
			ReplaceAction restoreAction= new ReplaceAction(fView, SearchMessages.RetrieverReplaceTab_RestoreSelected_text, false, sel);
			if (restoreAction.isEnabled()) {
				mgr.appendToGroup(IContextMenuConstants.GROUP_REORGANIZE, restoreAction);
			}
		}
		ReplaceAction replaceAction= new ReplaceAction(fView, SearchMessages.RetrieverReplaceTab_ReplaceAll_text, true);
		if (replaceAction.isEnabled()) {
			replaceAction.setReplacement(fSearchPattern, replacement);
			mgr.appendToGroup(IContextMenuConstants.GROUP_REORGANIZE, replaceAction);
		}
		ReplaceAction restoreAction= new ReplaceAction(fView, SearchMessages.RetrieverReplaceTab_RestoreAll_text, false);
		if (restoreAction.isEnabled()) {
			mgr.appendToGroup(IContextMenuConstants.GROUP_REORGANIZE, restoreAction);
		}
	}
}
