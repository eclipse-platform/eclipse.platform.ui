package org.eclipse.team.internal.ccvs.ui.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Persists the size of the wizard dialog.
 */
public class WizardSizeSaver {
	
	private final int DEFAULT_WIDTH;
	private final int DEFAULT_HEIGHT;
    
    private static final String BOUNDS_HEIGHT_KEY = "width"; //$NON-NLS-1$
    private static final String BOUNDS_WIDTH_KEY = "height"; //$NON-NLS-1$
    
    final IWizard fWizard;
    final String fSectionName;
    
    public WizardSizeSaver(IWizard wizard, String sectionName) {
    	this(wizard, sectionName, 300, 400);
    }
    
    public WizardSizeSaver(IWizard wizard, String sectionName, int defaultWidth, int defaultHeight) {
        DEFAULT_WIDTH= defaultWidth;
        DEFAULT_HEIGHT= defaultHeight;
        fWizard= wizard;
        fSectionName= sectionName;
    }
    
    public void saveSize() {
        
        final Rectangle bounds= fWizard.getContainer().getCurrentPage().getControl().getParent().getClientArea();
    	final IDialogSettings settings= fWizard.getDialogSettings();
    	if (settings == null)
    		return;
    	
    	IDialogSettings section= settings.getSection(fSectionName); 
    	if (section == null)
    		section= settings.addNewSection(fSectionName);
    	
        section.put(BOUNDS_WIDTH_KEY, bounds.width);
        section.put(BOUNDS_HEIGHT_KEY, bounds.height);
    }
    
    public Point getSize() {
        final Point size= new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        
    	final IDialogSettings settings= fWizard.getDialogSettings();
    	if (settings == null)
    		return size;
    	
    	final IDialogSettings section= settings.getSection(fSectionName);
    	if (section == null)
    		return size;

        try {
            size.x= section.getInt(BOUNDS_WIDTH_KEY);
            size.y= section.getInt(BOUNDS_HEIGHT_KEY);
        } catch (NumberFormatException e) {
        }
        return size;
    }
}
