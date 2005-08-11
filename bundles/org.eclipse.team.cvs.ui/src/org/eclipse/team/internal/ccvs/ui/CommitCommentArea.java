/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids - bug 57208
 *     Maik Schreiber - bug 102461
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ui.SWTUtils;
import org.eclipse.team.internal.ui.dialogs.DialogArea;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * This area provides the widgets for providing the CVS commit comment
 */
public class CommitCommentArea extends DialogArea {
    
    private class TextBox implements ModifyListener, TraverseListener, FocusListener, Observer {
        
        private final Text fTextField; // updated only by modify events
        private final String fMessage;
        
        private String fText;
        
        public TextBox(Composite composite, String message, String initialText) {
            
            fMessage= message;
            fText= initialText;
            
            fTextField = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
            fTextField.setLayoutData(SWTUtils.createHVFillGridData());
            
            setText(initialText);
            
            fTextField.addTraverseListener(this);
            fTextField.addModifyListener(this);
            fTextField.addFocusListener(this);
        }
        
        public void modifyText(ModifyEvent e) {
            final String old = fText;
            fText = fTextField.getText();
            firePropertyChangeChange(COMMENT_MODIFIED, old, fText);
        }
        
        public void keyTraversed(TraverseEvent e) {
            if (e.detail == SWT.TRAVERSE_RETURN && (e.stateMask & SWT.CTRL) != 0) {
                e.doit = false;
                firePropertyChangeChange(OK_REQUESTED, null, null);
            }
        }
        
        public void focusGained(FocusEvent e) {

            if (fText.length() > 0) 
                return;
            
            fTextField.removeModifyListener(this);
            try {
                fTextField.setText(fText);
            } finally {
                fTextField.addModifyListener(this);
            }
        }
        
        public void focusLost(FocusEvent e) {
            
            if (fText.length() > 0) 
                return;
            
            fTextField.removeModifyListener(this);
            try {
                fTextField.setText(fMessage);
                fTextField.selectAll();
            } finally {
                fTextField.addModifyListener(this);
            }
        }
        
        public void setEnabled(boolean enabled) {
            fTextField.setEnabled(enabled);
        }
        
        public void update(Observable o, Object arg) {
            if (arg instanceof String) {
                setText((String)arg); // triggers a modify event
            }
        }
        
        public String getText() {
            return fText;
        }
        
        private void setText(String text) {
            if (text.length() == 0) {
                fTextField.setText(fMessage);
                fTextField.selectAll();
            } else
                fTextField.setText(text);
        }

        public void setFocus() {
            fTextField.setFocus();
        }
    }
    
    private static class ComboBox extends Observable implements SelectionListener, FocusListener {
        
        private final String fMessage;
        private final String [] fComments;
        private String[] fCommentTemplates;
        private final Combo fCombo;
        
        
        public ComboBox(Composite composite, String message, String [] options,
                String[] commentTemplates) {
            
            fMessage= message;
            fComments= options;
            fCommentTemplates = commentTemplates;
            
            fCombo = new Combo(composite, SWT.READ_ONLY);
            fCombo.setLayoutData(SWTUtils.createHFillGridData());
            
            // populate the previous comment list
            populateList();
            
            // We don't want to have an initial selection
            // (see bug 32078: http://bugs.eclipse.org/bugs/show_bug.cgi?id=32078)
            fCombo.addFocusListener(this);
            fCombo.addSelectionListener(this);
        }

		private void populateList() {
			fCombo.removeAll();
			
			fCombo.add(fMessage);
            for (int i = 0; i < fCommentTemplates.length; i++) {
                fCombo.add(CVSUIMessages.CommitCommentArea_6 + ": " + //$NON-NLS-1$
                		HistoryView.flattenText(fCommentTemplates[i]));
            }
            for (int i = 0; i < fComments.length; i++) {
                fCombo.add(HistoryView.flattenText(fComments[i]));
            }
            fCombo.setText(fMessage);
		}
        
        public void widgetSelected(SelectionEvent e) {
            int index = fCombo.getSelectionIndex();
            if (index > 0) {
                index--;
                setChanged();
                
                // map from combo box index to array index
                String message;
                if (index < fCommentTemplates.length) {
                	message = fCommentTemplates[index];
                } else {
                	message = fComments[index - fCommentTemplates.length];
                }
                notifyObservers(message);
            }
        }
        
        public void widgetDefaultSelected(SelectionEvent e) {
        }
        
        public void focusGained(FocusEvent e) {
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
         */
        public void focusLost(FocusEvent e) {
            fCombo.removeSelectionListener(this);
            try {
                fCombo.setText(fMessage);
            } finally {
                fCombo.addSelectionListener(this);
            }
        }
        
        public void setEnabled(boolean enabled) {
            fCombo.setEnabled(enabled);
        }
        
        void setCommentTemplates(String[] templates) {
			fCommentTemplates = templates;
			populateList();
		}
    }
    
    private static final String EMPTY_MESSAGE= CVSUIMessages.CommitCommentArea_0; 
    private static final String COMBO_MESSAGE= CVSUIMessages.CommitCommentArea_1;
    private static final String CONFIGURE_TEMPLATES_MESSAGE= CVSUIMessages.CommitCommentArea_5;
    
    public static final String OK_REQUESTED = "OkRequested";//$NON-NLS-1$
    public static final String COMMENT_MODIFIED = "CommentModified";//$NON-NLS-1$
    
    private TextBox fTextBox;
    private ComboBox fComboBox;
    
    private IProject fMainProject;
    private String fProposedComment;
    private Composite fComposite;
    
    /**
     * @see org.eclipse.team.internal.ccvs.ui.DialogArea#createArea(org.eclipse.swt.widgets.Composite)
     */
    public void createArea(Composite parent) {
        Dialog.applyDialogFont(parent);
        initializeDialogUnits(parent);
        
        fComposite = createGrabbingComposite(parent, 1);
        initializeDialogUnits(fComposite);
        
        fTextBox= new TextBox(fComposite, EMPTY_MESSAGE, getInitialComment());
        
        final String [] comments = CVSUIPlugin.getPlugin().getRepositoryManager().getPreviousComments();
        final String[] commentTemplates = CVSUIPlugin.getPlugin().getRepositoryManager().getCommentTemplates();
        fComboBox= new ComboBox(fComposite, COMBO_MESSAGE, comments, commentTemplates);
        
        Link templatesPrefsLink = new Link(fComposite, 0);
        templatesPrefsLink.setText("<a href=\"configureTemplates\">" + //$NON-NLS-1$
        		CONFIGURE_TEMPLATES_MESSAGE + "</a>"); //$NON-NLS-1$
        templatesPrefsLink.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				openCommentTemplatesPreferencePage();
			}
		
			public void widgetSelected(SelectionEvent e) {
				openCommentTemplatesPreferencePage();
			}
		});
        
        fComboBox.addObserver(fTextBox);
    }
    
    void openCommentTemplatesPreferencePage() {
		PreferencesUtil.createPreferenceDialogOn(
				null,
				"org.eclipse.team.cvs.ui.CommentTemplatesPreferences", //$NON-NLS-1$
				new String[] { "org.eclipse.team.cvs.ui.CommentTemplatesPreferences" }, //$NON-NLS-1$
				null).open();
		fComboBox.setCommentTemplates(
				CVSUIPlugin.getPlugin().getRepositoryManager().getCommentTemplates());
	}

	public String getComment(boolean save) {
        final String comment= fTextBox.getText();
        if (comment == null)
            return ""; //$NON-NLS-1$
        
        final String stripped= strip(comment);
        if (save && comment.length() > 0)
            CVSUIPlugin.getPlugin().getRepositoryManager().addComment(comment);

        return stripped;
    }
    
    public String getCommentWithPrompt(Shell shell) {
        final String comment= getComment(false);
        if (comment.length() == 0) {
            final IPreferenceStore store= CVSUIPlugin.getPlugin().getPreferenceStore();
            final String value= store.getString(ICVSUIConstants.PREF_ALLOW_EMPTY_COMMIT_COMMENTS);
            
            if (MessageDialogWithToggle.NEVER.equals(value))
                return null;
            
            if (MessageDialogWithToggle.PROMPT.equals(value)) {
                
                final String title= CVSUIMessages.CommitCommentArea_2; 
                final String message= CVSUIMessages.CommitCommentArea_3; 
                final String toggleMessage= CVSUIMessages.CommitCommentArea_4; 
                
                final MessageDialogWithToggle dialog= MessageDialogWithToggle.openYesNoQuestion(shell, title, message, toggleMessage, false, store, ICVSUIConstants.PREF_ALLOW_EMPTY_COMMIT_COMMENTS);
                if (dialog.getReturnCode() != IDialogConstants.YES_ID) {
                    fTextBox.setFocus();
                    return null;
                }
            }
        }
        return getComment(true);
    }

    
    public void setProject(IProject iProject) {
        this.fMainProject = iProject;
    }
    
    public void setFocus() {
        if (fTextBox != null) {
            fTextBox.setFocus();
        }
    }
    
    public void setProposedComment(String proposedComment) {
    	if (proposedComment == null || proposedComment.length() == 0) {
    		this.fProposedComment = null;
    	} else {
    		this.fProposedComment = proposedComment;
    	}
    }
    
    public boolean hasCommitTemplate() {
        try {
            String commitTemplate = getCommitTemplate();
            return commitTemplate != null && commitTemplate.length() > 0;
        } catch (CVSException e) {
            CVSUIPlugin.log(e);
            return false;
        }
    }
    
    public void setEnabled(boolean enabled) {
        fTextBox.setEnabled(enabled);
        fComboBox.setEnabled(enabled);
    }
    
    public Composite getComposite() {
        return fComposite;
    }
    
    protected void firePropertyChangeChange(String property, Object oldValue, Object newValue) {
        super.firePropertyChangeChange(property, oldValue, newValue);
    }
    
    private String getInitialComment() {
        if (fProposedComment != null)
            return fProposedComment;
        try {
            return getCommitTemplate();
        } catch (CVSException e) {
            CVSUIPlugin.log(e);
            return ""; //$NON-NLS-1$
        }
    }

    private String strip(String comment) {
        // strip template from the comment entered
        try {
            final String commitTemplate = getCommitTemplate();
            if (comment.startsWith(commitTemplate)) {
                return comment.substring(commitTemplate.length());
            } else if (comment.endsWith(commitTemplate)) {
                return comment.substring(0, comment.length() - commitTemplate.length());
            }
        } catch (CVSException e) {
            // we couldn't get the commit template. Log the error and continue
            CVSUIPlugin.log(e);
        }
        return comment;
    }

    private CVSTeamProvider getProvider() {
        if (fMainProject == null) return null;
        return (CVSTeamProvider) RepositoryProvider.getProvider(fMainProject, CVSProviderPlugin.getTypeId());
    }

    private String getCommitTemplate() throws CVSException {
        CVSTeamProvider provider = getProvider();
        if (provider == null) 
            return ""; //$NON-NLS-1$
        final String template = provider.getCommitTemplate();
        return template != null ? template : ""; //$NON-NLS-1$
    }
}
