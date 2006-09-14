/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.presentations;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultTabItem;
import org.eclipse.ui.internal.presentations.util.AbstractTabItem;
import org.eclipse.ui.internal.presentations.util.PresentablePartFolder;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

public class BasicPartList extends AbstractTableInformationControl {

    private boolean hiddenTabsBold = true;
    private PresentablePartFolder folder;
    private IStackPresentationSite site;
    
    private class BasicStackListContentProvider implements
            IStructuredContentProvider {
    	
    	/**
    	 * Constructor for stack list content provider.
    	 *
    	 */
        public BasicStackListContentProvider() {
            //no-op
        }

        public void dispose() {
            //no-op 
        }

        public Object[] getElements(Object inputElement) {
            if (folder == null) {
                return new IPresentablePart[0];
            }
            
            return folder.getPartList();
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            folder = (PresentablePartFolder) newInput;
        }
    }

    private class BasicStackListLabelProvider extends LabelProvider implements
            IFontProvider {

        private Font boldFont = null;

        /**
         * Constructor for stack list label provider.
         *
         */
        public BasicStackListLabelProvider() {
            //no-op
        }

        public String getText(Object element) {
            IPresentablePart presentablePart = (IPresentablePart) element;
            if (presentablePart.isDirty()) {
                return DefaultTabItem.DIRTY_PREFIX + presentablePart.getName();
            }
            
            return presentablePart.getName();
        }

        public Image getImage(Object element) {
            IPresentablePart presentablePart = (IPresentablePart) element;
            return presentablePart.getTitleImage();
        }

        public Font getFont(Object element) {
            IPresentablePart presentablePart = (IPresentablePart)element;

            AbstractTabItem item = folder.getTab(presentablePart);
            // if in single tab mode, do not use the bold font for non-visible tabs
            // if in multiple tab mode, use the bold for non-visible tabs only
            if ((item!=null && item.isShowing()) || !hiddenTabsBold) {
				return folder.getTabFolder().getControl().getFont();
			}

            if (boldFont == null) {
                Control control = folder.getTabFolder().getControl();
                Font originalFont = control.getFont();
                FontData fontData[] = originalFont.getFontData();
                // Adding the bold attribute
                for (int i = 0; i < fontData.length; i++) {
					fontData[i].setStyle(fontData[i].getStyle() | SWT.BOLD);
				}
                boldFont = new Font(control.getDisplay(), fontData);
            }
            return boldFont;
        }

        public void dispose() {
            super.dispose();
            if (boldFont != null) {
				boldFont.dispose();
			}
        }
    }

    private class BasicStackListViewerComparator extends ViewerComparator {
    	/**
    	 * Constructor for stack list viewer's comparator.
    	 *
    	 */
        public BasicStackListViewerComparator() {
            //no-op
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        public int compare(Viewer viewer, Object e1, Object e2) {
            int cat1 = category(e1);
            int cat2 = category(e2);

            if (cat1 != cat2) {
				return cat1 - cat2;
			}

            String name1;
            String name2;

            if (viewer == null || !(viewer instanceof ContentViewer)) {
                name1 = e1.toString();
                name2 = e2.toString();
            } else {
                IBaseLabelProvider prov = ((ContentViewer) viewer)
                        .getLabelProvider();
                if (prov instanceof ILabelProvider) {
                    ILabelProvider lprov = (ILabelProvider) prov;
                    name1 = lprov.getText(e1);
                    name2 = lprov.getText(e2);
                    // ILabelProvider's implementation in BasicStackList calls 
                    // DefaultEditorPresentation.getLabelText which returns the name of dirty 
                    // files begining with dirty prefix, sorting should not take dirty prefix in consideration
                    String prefix = DefaultTabItem.DIRTY_PREFIX;
                    if (name1.startsWith(prefix)) {
						name1 = name1.substring(prefix.length());
					}
                    if (name2.startsWith(prefix)) {
						name2 = name2.substring(prefix.length());
					}
                } else {
                    name1 = e1.toString();
                    name2 = e2.toString();
                }
            }
            if (name1 == null) {
				name1 = "";//$NON-NLS-1$
			}
            if (name2 == null) {
				name2 = "";//$NON-NLS-1$
			}
            return getComparator().compare(name1, name2);
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
         */
        public int category(Object element) {

            IPresentablePart part = (IPresentablePart)element;
            AbstractTabItem tabItem = folder.getTab(part);
            
            if (tabItem!=null && tabItem.isShowing()) {
				return 1; // visible
			}
            return 0; // not visible
        }
    }

    /**
     * Constructor for BasicPartList.
     * 
     * @param parent the parent shell
     * @param shellStyle the SWT style bits used to create the shell
     * @param treeStyle	the SWT style bits used to create the tree
     * @param site 
     * @param folder 
     */
    public BasicPartList(Shell parent, int shellStyle, int treeStyle, 
            IStackPresentationSite site, PresentablePartFolder folder) {
        super(parent, shellStyle, treeStyle);
        
        this.site = site;
        this.folder = folder;
    }

    protected TableViewer createTableViewer(Composite parent, int style) {
        Table table = new Table(parent, SWT.SINGLE | (style & ~SWT.MULTI));
        table.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        TableViewer tableViewer = new TableViewer(table) {
            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.TableViewer#internalRefresh(java.lang.Object)
             */
            protected void internalRefresh(Object element) {
                boolean usingMotif = "motif".equals(SWT.getPlatform()); //$NON-NLS-1$
                try {
                    // This avoids a "graphic is disposed" error on Motif by not letting
                    // it redraw while we remove entries.  Some items in this table are
                    // being removed and may have icons which may have already been
                    // disposed elsewhere.
                    if (usingMotif) {
						getTable().setRedraw(false);
					}
                    super.internalRefresh(element);
                } finally {
                    if (usingMotif) {
						getTable().setRedraw(true);
					}
                }
            }
        };
        tableViewer.addFilter(new NamePatternFilter());
        tableViewer.setContentProvider(new BasicStackListContentProvider());
        tableViewer.setComparator(new BasicStackListViewerComparator());
        tableViewer.setLabelProvider(new BasicStackListLabelProvider());
        return tableViewer;
    }

    public void setInput(Object information) {
        PresentablePartFolder newFolder = (PresentablePartFolder) information;
        inputChanged(newFolder, newFolder.getCurrent());
    }

    protected void gotoSelectedElement() {
        Object selectedElement = getSelectedElement();

        //close the shell
        dispose();

        if (selectedElement instanceof IPresentablePart) {
        	site.selectPart((IPresentablePart)selectedElement);
        }
    }

    protected boolean deleteSelectedElements() {

        IStructuredSelection structuredSelection = getSelectedElements();

        if (structuredSelection != null) {

            ArrayList list = new ArrayList(structuredSelection.size());

            for (Iterator iterator = structuredSelection.iterator(); iterator
                    .hasNext();) {
                IPresentablePart presentablePart = (IPresentablePart) iterator.next();
                list.add(presentablePart);
            }

            site.close((IPresentablePart[]) list
                    .toArray(new IPresentablePart[list.size()]));
        }

        if (folder.isDisposed()) {
            fComposite.dispose();
            return true;
        }
        return false;

    }
}
