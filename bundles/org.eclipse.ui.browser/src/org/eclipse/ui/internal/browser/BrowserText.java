package org.eclipse.ui.internal.browser;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

public class BrowserText {
    private String url;
    private FallbackScrolledComposite scomp;
    private Label title;
    private Label text;
    private Link link;
    
    class ReflowScrolledComposite extends FallbackScrolledComposite {
        public ReflowScrolledComposite(Composite parent, int style) {
            super(parent, style);
        }
        public void reflow(boolean flushCache) {
            updateWidth(this);
            super.reflow(flushCache);
        }
    }
        
    public BrowserText(Composite parent, int style) {
        Color bg = parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
        scomp = new ReflowScrolledComposite(parent, SWT.NULL);
        Composite client = new Composite(scomp, SWT.NULL);
        scomp.setContent(client);
        fillContent(client, bg);
        scomp.setBackground(bg);
    }
   
    private void fillContent(Composite parent, Color bg) {
        GridLayout layout = new GridLayout();
        parent.setLayout(layout);
        title = new Label(parent, SWT.WRAP);
        title.setText("Embedded browser not available");
        title.setFont(JFaceResources.getHeaderFont());
        title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        title.setBackground(bg);
        text = new Label(parent, SWT.WRAP);

        text.setText("The embedded browser for this editor cannot be created.");
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.setBackground(bg);
        
        link = new Link(parent, SWT.WRAP);
        link.setText("<a href=\"open\">Open file using the system editor</a>");
        link.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        link.setBackground(bg);
   }
    
    private void updateWidth(Composite parent) {
        GridData gd = (GridData)title.getLayoutData();
        Rectangle area = parent.getClientArea();
        gd.widthHint = area.width-10; 
        gd = (GridData)text.getLayoutData();
        gd.widthHint = area.width-10;         
        gd = (GridData)link.getLayoutData();
        gd.widthHint = area.width-10;         
    }
    
    public Control getControl() {
        return scomp;
    }
    
    public boolean setUrl(String url) {
       this.url = url;
       return true;
    }
    
    public void setFocus() {
        link.setFocus();
    }
 
    public String getUrl() {
        return url;
    }
    
    public void refresh() {
        scomp.reflow(true);
    }
}