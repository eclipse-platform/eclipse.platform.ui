package org.eclipse.ui.tutorials.rcp.part3.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.tutorials.rcp.part3.Messages;

public class SampleView extends ViewPart {
    public static final String ID_VIEW = "org.eclipse.ui.tutorials.rcp.part3.views.SampleView"; //$NON-NLS-1$

    private TableViewer viewer;

    class ViewContentProvider implements IStructuredContentProvider {
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }
        public void dispose() {
        }
        public Object[] getElements(Object parent) {
            return new String[] { Messages.getString("One"), //$NON-NLS-1$
                Messages.getString("Two"), //$NON-NLS-1$
                Messages.getString("Three")}; //$NON-NLS-1$
        }
    }
    class ViewLabelProvider
        extends LabelProvider
        implements ITableLabelProvider {
        public String getColumnText(Object obj, int index) {
            return getText(obj);
        }
        public Image getColumnImage(Object obj, int index) {
            return getImage(obj);
        }
        public Image getImage(Object obj) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(
                ISharedImages.IMG_OBJ_ELEMENT);
        }
    }

    public SampleView() {
    }

    public void createPartControl(Composite parent) {
        viewer =
            new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
        viewer.setInput(this);
    }

    public void setFocus() {
        viewer.getControl().setFocus();
    }
}