package org.eclipse.ui.internal;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IViewReference;

public class FastViewBarContextMenuContribution extends ContributionItem {
    private MenuItem orientationItem;
    private MenuItem restoreItem;
    private MenuItem closeItem;
    private MenuItem showOn;
    private FastViewBar bar;
    private RadioMenu radioButtons;
    private Menu sidesMenu;
    private IViewReference selectedView;
    private IntModel currentOrientation = new IntModel(SWT.VERTICAL);
    
    private IChangeListener orientationChangeListener = new IChangeListener() {
        public void update(boolean changed) {
            if (changed && selectedView != null) {
                bar.setOrientation(selectedView, currentOrientation.get());
            }
        }
    };
    
    public FastViewBarContextMenuContribution(FastViewBar bar) {
        this.bar = bar;
        currentOrientation.addChangeListener(orientationChangeListener);
    }

    public void fill(Menu menu, int index) {
        // TODO Auto-generated method stub
        super.fill(menu, index);
        

        orientationItem = new MenuItem(menu, SWT.CASCADE, index++);
        {
            orientationItem.setText(WorkbenchMessages.FastViewBar_view_orientation);

            Menu orientationSwtMenu = new Menu(orientationItem);
            RadioMenu orientationMenu = new RadioMenu(orientationSwtMenu,
                    currentOrientation);
            orientationMenu
                    .addMenuItem(
                            WorkbenchMessages.FastViewBar_horizontal, new Integer(SWT.HORIZONTAL)); 
            orientationMenu
                    .addMenuItem(
                            WorkbenchMessages.FastViewBar_vertical, new Integer(SWT.VERTICAL)); 

            orientationItem.setMenu(orientationSwtMenu);
        }

        restoreItem = new MenuItem(menu, SWT.CHECK, index++);
        restoreItem.setText(WorkbenchMessages.ViewPane_fastView);
        restoreItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                bar.restoreView(selectedView);
            }
        });

        closeItem = new MenuItem(menu, SWT.NONE, index++);
        closeItem.setText(WorkbenchMessages.WorkbenchWindow_close); 
        closeItem.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                if (selectedView != null) {
                    WorkbenchPage page = bar.getWindow().getActiveWorkbenchPage();
                    if (page != null) {
                        page.hideView(selectedView);
                    }
                }
            }
        });

        new MenuItem(menu, SWT.SEPARATOR, index++);

        showOn = new MenuItem(menu, SWT.CASCADE, index++);
        {
            showOn.setText(WorkbenchMessages.FastViewBar_dock_on); 

            sidesMenu = new Menu(showOn);
            radioButtons = new RadioMenu(sidesMenu, bar.side);

            radioButtons.addMenuItem(WorkbenchMessages.FastViewBar_Left, new Integer(SWT.LEFT)); 
            radioButtons
                    .addMenuItem(
                            WorkbenchMessages.FastViewBar_Right, new Integer(SWT.RIGHT)); 
            radioButtons
                    .addMenuItem(
                            WorkbenchMessages.FastViewBar_Bottom, new Integer(SWT.BOTTOM));

            showOn.setMenu(sidesMenu);
        }
        
        boolean selectingView = (selectedView != null);
        WorkbenchPage page = bar.getWindow().getActiveWorkbenchPage();
        
        if (selectingView) {
        	restoreItem.setEnabled(page!=null && page.isMoveable(selectedView));
        } else {
        	restoreItem.setEnabled(false);
        }
        restoreItem.setSelection(true);
        
        if (selectingView) {
			closeItem
					.setEnabled(page != null && page.isCloseable(selectedView));
		} else {
			closeItem.setEnabled(false);
		}
        
        orientationItem.setEnabled(selectingView);
        if (selectingView) {
            // Set the new orientation, but avoid re-sending the event to our own
            // listener
            currentOrientation.set(bar.getOrientation(selectedView),
                    orientationChangeListener);
        }
    }
    
    public void setTarget(IViewReference selectedView) {
        this.selectedView = selectedView;
    }

    public boolean isDynamic() {
        return true;
    }
    
    public void dispose() {
        if (radioButtons != null) {
            radioButtons.dispose();
        }
        super.dispose();
    }
}
