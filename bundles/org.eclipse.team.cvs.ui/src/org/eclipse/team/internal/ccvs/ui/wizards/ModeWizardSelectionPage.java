/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.wizards;


import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.util.StringMatcher;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.wizards.ModeWizard.FileModeChange;
import org.eclipse.team.internal.ui.PixelConverter;
import org.eclipse.team.internal.ui.SWTUtils;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class ModeWizardSelectionPage extends WizardPage {
    
    private final static class ModeChangeCellModifier implements ICellModifier {
        
        private final ModeChangeTable fTable;
        
        public ModeChangeCellModifier(ModeChangeTable table) {
            fTable= table;
        }
        
        public boolean canModify(Object element, String property) {
            return PROPERTY_MODE.equals(property);
        }
        
        
        public Object getValue(Object element, String property) {
            if (PROPERTY_MODE.equals(property)) {
                final KSubstOption mode= ((FileModeChange)element).getNewMode();
                for (int i = 0; i < MODES.length; i++) {
                    if (MODES[i].equals(mode)) {
                        return new Integer(i);
                    }
                }
            }
            return null;
        }
        
        
        public void modify(Object element, String property, Object value) {
            if (element instanceof Item)
                element= ((Item)element).getData();
            if (PROPERTY_MODE.equals(property)) {
                ((FileModeChange)element).setNewMode(MODES[((Integer)value).intValue()]);
                fTable.modelChanged(true);
            }
        }
    }
    
    private final static class ModeChangeLabelProvider implements ITableLabelProvider {
        
        private final DecoratingLabelProvider fDecoratingLP;
        private final ModeChangeTable fTable;
        
        public ModeChangeLabelProvider(ModeChangeTable table) {
            fTable= table;
            fDecoratingLP= new DecoratingLabelProvider(new WorkbenchLabelProvider(), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator());
            fDecoratingLP.addListener(fTable);
        }
        
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 0) {
                return fDecoratingLP.getImage(((FileModeChange)element).getFile());
            }
            return null;
        }
        
        public String getColumnText(Object element, int columnIndex) {
            final FileModeChange change= (FileModeChange)element;
            switch (columnIndex) {
            case 0: return fDecoratingLP.getText(change.getFile());
            case 1: return change.getNewMode().getLongDisplayText();
            }
            throw new IllegalArgumentException();
        }
        
        public void addListener(ILabelProviderListener listener) {
        }
        
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }
        
        public void dispose() {
            fDecoratingLP.removeListener(fTable);
        }
        
        public void removeListener(ILabelProviderListener listener) {        
        }
    }
    
    private static final class ModeChangeTable extends Observable implements ISelectionChangedListener, ILabelProviderListener {
        
        private final List fChanges;
        private final TableViewer fViewer;
        private final Filter fFilter;
        
        public ModeChangeTable(Composite composite, PixelConverter converter, List changes) {
            
            fChanges= changes;
            
            /**
             * Create a table.
             */
            final Table table = new Table(composite, SWT.V_SCROLL | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
            table.setLayoutData(SWTUtils.createHVFillGridData());
            table.setLinesVisible(false);
            table.setHeaderVisible(true);
            
            /**
             * The 'File' column
             */
            final TableColumn fileColumn = new TableColumn(table, SWT.NONE, 0);
            fileColumn.setWidth(converter.convertWidthInCharsToPixels(LARGE_COLUMN));
            fileColumn.setText("File");
            
            /**
             * The 'Mode' column
             */
            final TableColumn newModeColumn = new TableColumn(table, SWT.NONE, 1);
            newModeColumn.setWidth(converter.convertWidthInCharsToPixels(COLUMN_MIN_WIDTH_IN_CHARS + 6));
            newModeColumn.setText("Mode");
            
            fViewer= new TableViewer(table);
            fViewer.setContentProvider(new ModeChangeContentProvider());
            fViewer.setLabelProvider(new ModeChangeLabelProvider(this));
            
            fViewer.getControl().setLayoutData(SWTUtils.createHVFillGridData());
            
            final CellEditor newModeEditor = new ComboBoxCellEditor(table, COMBO_TEXT, SWT.READ_ONLY);
            
            fViewer.setCellEditors(new CellEditor [] { null, newModeEditor });
            fViewer.setColumnProperties(new String [] { PROPERTY_FILE, PROPERTY_MODE });
            fViewer.setCellModifier(new ModeChangeCellModifier(this));
            fViewer.setInput(fChanges);
            
            fFilter= new Filter();
            fViewer.addFilter(fFilter);
            
            //TODO: CVSLightweightDecorator.decorate() is lighter than normal decs.
            fViewer.addSelectionChangedListener(this);
        }
        
        public TableViewer getViewer() {
            return fViewer;
        }
        
        public void selectionChanged(SelectionChangedEvent event) {
            setChanged();
            notifyObservers(fViewer.getSelection());
        }
        
        public void modelChanged(boolean updateLabels) {
            fViewer.refresh(updateLabels);
            setChanged();
            notifyObservers();
        }
        
        public Filter getFilter() {
            return fFilter;
        }
        
        public IStructuredSelection getSelection() {
            return (IStructuredSelection)fViewer.getSelection();
        }
        
        
        public void labelProviderChanged(LabelProviderChangedEvent event) {
            fViewer.refresh();
        }
    }
    
    private static final class ModeChangeContentProvider implements IStructuredContentProvider {
        
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
        
        public Object[] getElements(Object inputElement) {
            return ((List)inputElement).toArray();
        }
        
        public void dispose() {
        }
    }
    
    private static final class ModeCombo extends SelectionAdapter implements Observer {
        
        private final Combo fCombo;
        private final ModeChangeTable fTable;
        
        public ModeCombo(ModeChangeTable table, Composite parent) {
            fTable= table;
            fCombo= new Combo(parent, SWT.READ_ONLY);
            fCombo.setLayoutData(SWTUtils.createHFillGridData());
            fCombo.setItems(COMBO_TEXT);
            fCombo.addSelectionListener(this);
            fTable.addObserver(this);
        }
        
        public void widgetSelected(SelectionEvent e) {
            final KSubstOption mode= MODES[fCombo.getSelectionIndex()];
            final IStructuredSelection selection= fTable.getSelection();
            for (final Iterator iter = selection.iterator(); iter.hasNext();) {
                final FileModeChange change = (FileModeChange) iter.next();
                change.setNewMode(mode);                
            }
            fTable.modelChanged(true);
        }
        
        public void update(Observable o, Object arg) {
            final IStructuredSelection selection= (IStructuredSelection)fTable.getViewer().getSelection();
            
            if (selection.isEmpty()) {
                fCombo.deselectAll();
                fCombo.setEnabled(false);
            } else {
                fCombo.setEnabled(true);
                final KSubstOption option= ((FileModeChange)selection.getFirstElement()).getNewMode();
                for (Iterator iter = selection.iterator(); iter.hasNext();) {
                    if (option != ((FileModeChange)iter.next()).getNewMode()) {
                        fCombo.deselectAll();
                        return;
                    }
                }
                fCombo.setText(option.getLongDisplayText());
            }
        }
    }
    
    private static final class FileExtensionSorter extends ViewerSorter {
        
        public int compare(Viewer viewer, Object e1, Object e2) {
            final IFile f1= ((FileModeChange)e1).getFile();
            final IFile f2= ((FileModeChange)e2).getFile();
            if (f1.getFileExtension() == null) {
                return f2.getFileExtension() == null ? ((Comparable)e1).compareTo(e2) : -1;
            } else {
                return f2.getFileExtension() == null ? 1 : f1.getFileExtension().compareTo(f2.getFileExtension());
            }
        }
    }
    
    private static final class Filter extends ViewerFilter {
        private boolean fFilterUnchanged;
//      private boolean fFilterShared;
        private StringMatcher fMatcher;
        
        public Filter() {
            fFilterUnchanged= false;
//          fFilterShared= false;
        }
        
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            final FileModeChange change= (FileModeChange)element;
//          if (fFilterShared && change.isShared()) 
//          return false;
            if (fFilterUnchanged && !change.hasChanged())
                return false;
            if (fMatcher != null && !fMatcher.match(change.getFile().getName()))
                return false;
            return true;
        }
        
        public void filterUnchanged(boolean filter) {
            fFilterUnchanged= filter;
        }
        
//      public void filterShared(boolean filter) {
//      fFilterShared= filter;
//      }
        
        public void setPattern(String pattern) {
            fMatcher= new StringMatcher(pattern, true, false);
        }
    }
    
    private static final class ResetButton extends SelectionAdapter implements Observer {
        
        private final ModeChangeTable fTable;
        private final Button fButton;
        
        public ResetButton(ModeChangeTable table, Composite parent, PixelConverter converter) {
            fTable= table;
            final int buttonWidth= converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
            fButton= new Button(parent, SWT.NONE);
            fButton.setLayoutData(SWTUtils.createGridData(buttonWidth, SWT.DEFAULT, SWT.FILL, SWT.CENTER, false, false));
            fButton.setText("R&eset");
            fButton.addSelectionListener(this);
            fTable.addObserver(this);
        }
        
        public void widgetSelected(SelectionEvent e) {
            fButton.setEnabled(false);
            final IStructuredSelection selection= fTable.getSelection();
            for (Iterator iter = selection.iterator(); iter.hasNext();) {
                final FileModeChange change = (FileModeChange) iter.next();
                change.setNewMode(change.getMode());
            }
            fTable.modelChanged(true);
        }
        
        public void update(Observable o, Object arg) {
            final IStructuredSelection selection= fTable.getSelection();
            for (final Iterator iter = selection.iterator(); iter.hasNext();) {
                if (((FileModeChange)iter.next()).hasChanged()) {
                    fButton.setEnabled(true);
                    return;
                }
            }
            fButton.setEnabled(false);
        }
    }
    
    private static final class GuessButton extends SelectionAdapter implements Observer {
        
        private final ModeChangeTable fTable;
        private final Button fButton;
        
        public GuessButton(ModeChangeTable table, Composite parent, PixelConverter converter) {
            fTable= table;
            final int buttonWidth= converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
            fButton= new Button(parent, SWT.NONE);
            fButton.setLayoutData(SWTUtils.createGridData(buttonWidth, SWT.DEFAULT, SWT.FILL, SWT.CENTER, false, false));
            fButton.setText("Gue&ss");
            fButton.addSelectionListener(this);
            fTable.addObserver(this);
            
        }
        
        public void widgetSelected(SelectionEvent e) {
            final IStructuredSelection selection= fTable.getSelection();
            for (Iterator iter = selection.iterator(); iter.hasNext();) {
                final FileModeChange change = (FileModeChange) iter.next();
                change.setNewMode(KSubstOption.fromFile(change.getFile()));
            }
            fTable.modelChanged(true);
        }
        
        public void update(Observable o, Object arg) {
            fButton.setEnabled(!fTable.getSelection().isEmpty());
        }
    }     
    
//  private static final class IncludeSharedCheckbox extends SelectionAdapter {
//  private final ModeChangeTable fTable;
//  private final Button fCheckbox;
//  
//  public IncludeSharedCheckbox(ModeChangeTable table, Composite parent) {
//  fTable= table;
//  fCheckbox= new Button(parent, SWT.CHECK);
//  fCheckbox.setLayoutData(SWTUtils.createHFillGridData());
//  fCheckbox.setText("Inc&lude files that are already shared in the repository.");
//  
//  fCheckbox.setSelection(true);
//  fTable.getFilter().filterShared(false);
//  
//  SWTUtils.createLabel(parent, "Note that a mode change on a shared file will affect all versions and revisions of that file in the repository. All developers will need to delete and check the project from the repository again.");
//  fCheckbox.addSelectionListener(this);
//  }
//  
//  public void widgetSelected(SelectionEvent e) {
//  fTable.getFilter().filterShared(!fCheckbox.getSelection());
//  fTable.modelChanged(true);
//  }
//  }
    
    private static final class ShowChangesOnlyButton extends SelectionAdapter {
        private final ModeWizardSelectionPage fPage;
        private final Button fToggle;
        
        public ShowChangesOnlyButton(ModeWizardSelectionPage page, Composite parent, PixelConverter converter) {
            fPage= page;
            fToggle= new Button(parent, SWT.TOGGLE);
            fToggle.setText("Show changes only");
            final int buttonWidth= SWTUtils.calculateButtonSize(converter, new Button [] { fToggle });
            fToggle.setLayoutData(SWTUtils.createGridData(buttonWidth, SWT.DEFAULT, SWT.BEGINNING, SWT.CENTER, false, false));
            fToggle.setSelection(false);
            fToggle.addSelectionListener(this);
        }
        
        public void widgetSelected(SelectionEvent e) {
            final ModeChangeTable table= fPage.getTable();
            table.getFilter().filterUnchanged(fToggle.getSelection());
            table.modelChanged(true);
        }
    }
    
    private static final class FilterTextBox extends SelectionAdapter implements ModifyListener {
        private final ModeWizardSelectionPage fPage;
        private final Text fText;
        
        public FilterTextBox(ModeWizardSelectionPage page, Composite parent, PixelConverter converter) {
            fPage= page;
            fText= new Text(parent, SWT.SINGLE | SWT.BORDER);
            fText.setLayoutData(SWTUtils.createHFillGridData());
            fText.addModifyListener(this);

            final int buttonWidth= converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
            final Button button= new Button(parent, SWT.PUSH);
            button.setText("C&lear");
            button.setLayoutData(SWTUtils.createGridData(buttonWidth, SWT.DEFAULT, SWT.FILL, SWT.CENTER, false, false));
            button.addSelectionListener(this);
            
        }
    
        public void widgetSelected(SelectionEvent e) {
            fText.setText("");
        }
        
        public void modifyText(ModifyEvent e) {
            String pattern= fText.getText();
            
            if (!pattern.endsWith("*"))
                pattern += "*";
            
            final ModeChangeTable table= fPage.getTable();
            table.getFilter().setPattern(pattern);
            table.modelChanged(false);
        }
    }
    
    
    private final static int LARGE_COLUMN= 30;
    
    protected static final String PROPERTY_FILE= "file"; //$NON-NLS-1$
    protected static final String PROPERTY_MODE= "mode"; //$NON-NLS-1$
    
    protected static final KSubstOption [] MODES;
    protected static final String [] COMBO_TEXT;
    
    static final int COLUMN_MIN_WIDTH_IN_CHARS;
    
    static {
        MODES= KSubstOption.getAllKSubstOptions();
        Arrays.sort(MODES, new Comparator() {
            public int compare(Object a, Object b) {
                String aKey = ((KSubstOption)a).getLongDisplayText();
                String bKey = ((KSubstOption) b).getLongDisplayText();
                return aKey.compareTo(bKey);
            }
        });
        COMBO_TEXT= new String[MODES.length];
        int maxLength= 0;
        for (int i = 0; i < MODES.length; i++) {
            COMBO_TEXT[i]= MODES[i].getLongDisplayText();
            if (COMBO_TEXT[i].length() > maxLength) maxLength= COMBO_TEXT[i].length();
        }
        COLUMN_MIN_WIDTH_IN_CHARS= maxLength;
    }
    
    private final List fChanges;
    
    private ModeChangeTable fPage;
    
    public ModeWizardSelectionPage(List modeChanges) {
        super("SelectionPage", "CVS Transfer Mode", CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_KEYWORD));
        setDescription("Configure the CVS transfer mode for each of the following files.");
        fChanges= modeChanges;
    }
    
    public void createControl(Composite parent) {
        
        final PixelConverter converter= SWTUtils.createDialogPixelConverter(parent);
        
        final Composite composite= new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtils.createGridLayout(1, converter, SWTUtils.MARGINS_DEFAULT));
        
        SWTUtils.createLabel(composite, "Select one or more files from the table and configure their transfer mode. For more information " +
        "about the different modes please refer to your CVS documentation.");

        SWTUtils.createLabel(composite, "Filter files by name (? = any character, * = any string):");

        final Composite filterComposite= new Composite(composite, SWT.NONE);
        filterComposite.setLayoutData(SWTUtils.createHFillGridData());
        filterComposite.setLayout(SWTUtils.createGridLayout(3, converter, SWTUtils.MARGINS_NONE));
        
        new FilterTextBox(this, filterComposite, converter);
        new ShowChangesOnlyButton(this, filterComposite, converter);
        
        fPage= new ModeChangeTable(composite, converter, fChanges);
        
        final Composite buttonComposite= new Composite(composite, SWT.NONE);
        buttonComposite.setLayoutData(SWTUtils.createHFillGridData());
        buttonComposite.setLayout(SWTUtils.createGridLayout(3, converter, SWTUtils.MARGINS_NONE));
        
        new ModeCombo(fPage, buttonComposite);
        new GuessButton(fPage, buttonComposite, converter);
        new ResetButton(fPage, buttonComposite, converter);
        
        
//      new IncludeSharedCheckbox(fPage, composite);
        
        fPage.modelChanged(true);
        
        setControl(composite);
    }
    
    protected ModeChangeTable getTable() {
        return fPage;
    }
}
