package org.eclipse.ui.internal.presentations.defaultpresentation;


public class DetachedViewTabItem extends DefaultTabItem {

    private DetachedViewTabFolder folder;
    
    public DetachedViewTabItem(DetachedViewTabFolder folder, int index, int flags) {
        super(folder.getTabFolder(), index, flags);
        
        this.folder = folder;
    }

    public void dispose() {
        super.dispose();
        
        folder.itemRemoved();
    }
}
