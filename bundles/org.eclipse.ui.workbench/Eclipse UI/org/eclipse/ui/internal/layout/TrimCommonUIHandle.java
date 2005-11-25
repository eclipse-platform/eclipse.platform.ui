package org.eclipse.ui.internal.layout;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.internal.IChangeListener;
import org.eclipse.ui.internal.IWindowTrim;
import org.eclipse.ui.internal.IntModel;
import org.eclipse.ui.internal.RadioMenu;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.presentations.PresentationUtil;

/**
 * This control provides common UI functionality for trim elements. Its
 * lifecycle is managed by the <code>TrimLayout</code> which automatically
 * adds a UI handle to all added trim elements. It uses an instance of a
 * CoolBar to provide the platform-specific drag affordance.
 * <p>
 * It provides the following features:
 * <p>
 * Drag affordance and handling:
 * <ol>
 * <li>Drag affordance is provided in the <code>paintControl</code> method</li>
 * <li>Drag handling is provided to allow rearrangement within a trim side or
 * to other sides, depending on the values returned by <code>IWindowTrim.getValidSides</code></li>
 * </ol>
 * </p>
 * <p>
 * Context Menu:
 * <ol>
 * <li>A "Dock on" menu item is provided to allow changing the side, depending on the values returned by 
 * <code>IWindowTrim.getValidSides</code></li>
 * <li>A "Close" menu item is provided to allow the User to close (hide) the trim element,
 * based on the value returned by <code>IWindowTrim.isCloseable</code>
 * </ol>
 * </p>
 * <p>
 * @since 3.2
 * </p>
 */
public class TrimCommonUIHandle extends Composite /*implements PaintListener*/ {

	/*
	 * Constants
	 */
	private static final int handleSize = 5;
	
	/*
	 * Fields
	 */
	private TrimLayout  layout;
    private IWindowTrim trim;
	private Control     toDrag;
	private int         curSide;
	private int orientation;

	// CoolBar handling
	private CoolBar cb;
	private CoolItem ci;
	private Composite dummy;
	
    /*
     * Context Menu
     */
	private MenuManager dockMenuManager;
	private ContributionItem dockContributionItem = null;
    private Menu sidesMenu;
	private MenuItem dockCascade;
    private RadioMenu radioButtons;
    private IntModel radioVal = new IntModel(0);
	private Menu showMenu;
	private MenuItem showCascade;
	
	/*
	 * Listeners...
	 */
    
    /**
     * This listener starts a drag operation when
     * the Drag and Drop manager tells it to
     */
    private Listener dragListener = new Listener() {
        public void handleEvent(Event event) {
            Point position = DragUtil.getEventLoc(event);
            startDraggingTrim(position);
        }
    };

    /**
     * This listener brings up the context menu
     */
    private Listener menuListener = new Listener() {
        public void handleEvent(Event event) {
            Point loc = new Point(event.x, event.y);
            if (event.type == SWT.MenuDetect) {
                showDockTrimPopup(loc);
            }
        }
    };

    /**
     * Listen to size changes in the control so we can adjust the
     * Coolbar, CoolItem and dummy composite to match.
     */
    private ControlListener controlListener = new ControlListener() {
		public void controlMoved(ControlEvent e) {
		}

		public void controlResized(ControlEvent e) {
			if (e.widget instanceof TrimCommonUIHandle) {
				TrimCommonUIHandle ctrl = (TrimCommonUIHandle) e.widget;
		        Point size = ctrl.getSize();
		        
		        cb.setSize(size);
		        ci.setSize(size);
		        dummy.setSize(size);
			}
		}
    };

    /**
     * Create a new trim UI handle for a particular IWindowTrim item
     * 
     * @param layout the TrimLayout we're being used in
     * @param trim the IWindowTrim we're acting on behalf of
     * @param curSide  the SWT side that the trim is currently on
     */
    public TrimCommonUIHandle(TrimLayout layout, IWindowTrim trim, int curSide) {
    	super(trim.getControl().getParent(), SWT.NONE);
    	
    	this.layout = layout;
    	this.trim = trim;
    	this.toDrag = trim.getControl();
    	this.curSide = curSide;
    	this.radioVal.set(curSide);
    	
    	// remember the orientation to use
    	orientation = (curSide == SWT.LEFT || curSide == SWT.RIGHT) ? SWT.VERTICAL  : SWT.HORIZONTAL;
    	
    	// Set the control up with all its various hooks, cursor...
    	setup();
    }

    /**
     * Handle the event generated when a User selects a new side to
     * dock this trim on using the context menu
     */
    private void handleShowOnChange() {
    	layout.removeTrim(trim);
    	trim.dock(radioVal.get());
    	layout.addTrim(trim, radioVal.get());
    	
    	// perform an optimized layout to show the trim in its new location
    	LayoutUtil.resize(trim.getControl());
	}

	/**
	 * Set up the trim with its cursor, drag listener, context menu and menu listener
	 */
	private void setup() {    	
        TrimLayoutData td = new TrimLayoutData(false, handleSize, handleSize);
        setLayoutData(td);
    	
        // Listen to size changes to keep the CoolBar synched
        addControlListener(controlListener);
       	
        // Insert a CoolBar and extras in order to provide the
        insertCoolBar(orientation);
        
    	// Set the cursor affordance
    	setDragCursor();
    	
        // Set up the dragging behaviour
        PresentationUtil.addDragListener(cb, dragListener);
    	
    	// Create the docking context menu
    	dockMenuManager = new MenuManager();
    	dockContributionItem = getDockingContribution();
        dockMenuManager.add(dockContributionItem);

        cb.addListener(SWT.MenuDetect, menuListener);
    }

	/**
	 * Place a CoolBar / CoolItem / Control inside the current
	 * UI handle. These elements will maintain thier size based on
	 * the size of their 'parent' (this).
	 * 
	 * @param parent 
	 * @param orientation
	 */
	public void insertCoolBar(int orientation) {
		// Create the necessary parts...
		cb = new CoolBar(this, orientation);
		ci = new CoolItem(cb, SWT.NONE);
		dummy = new Composite(cb, SWT.NONE);

		// Compute an initial value for the 'length' of the affordance
		int length;
        Point ctrlSize = toDrag.getSize();
        if (orientation == SWT.HORIZONTAL)
        	length = ctrlSize.y;
        else
        	length = ctrlSize.x;
		
        // Set the initial sizes to match the given 'length'
		if (orientation == SWT.HORIZONTAL)
			dummy.setSize(1, length);
		else
			dummy.setSize(length, 1);
		
		// Set the CoolItem
		Point size = dummy.getSize();
		ci.setControl(dummy);
		ci.setSize(ci.computeSize(size.x, size.y));
		
		// Finally, set the handle's size
		setSize(ci.getSize());
	}
	
	/**
	 * Determine and set the appropriate drag cursor. If a trim item can
	 * only live on a single side the a two-way arrow is chosen based on
	 * the orientation. If the item has more than one valid side then a 
	 * four-way arrow is supplied.
	 */
	private void setDragCursor() {
   		int validSides = trim.getValidSides();
   		
    	// Can this trim change sides ??
    	int sideCount = 0;
    	if ((validSides & SWT.TOP) != 0)    sideCount++;
    	if ((validSides & SWT.BOTTOM) != 0) sideCount++;
    	if ((validSides & SWT.LEFT) != 0)   sideCount++;
    	if ((validSides & SWT.RIGHT) != 0)  sideCount++;
    	
    	Cursor dragCursor;
    	if (sideCount == 1) {
    		// If there's only one valid side then 'curSide' must be it...
        	if (curSide == SWT.TOP || curSide == SWT.BOTTOM)
    			dragCursor = new Cursor(toDrag.getDisplay(), SWT.CURSOR_SIZEWE);
    		else
    			dragCursor = new Cursor(toDrag.getDisplay(), SWT.CURSOR_SIZENS);
    	}
    	else {
    		dragCursor = new Cursor(toDrag.getDisplay(), SWT.CURSOR_SIZEALL);
    	}
    	
    	setCursor(dragCursor);
	}

	/**
	 * Construct (if necessary) a context menu contribution item and return it. This
	 * is explicitly <code>public</code> so that trim elements can retrieve the item
	 * and add it into their own context menus if desired.
	 * 
	 * @return The contribution item for the handle's context menu. 
	 */
	public ContributionItem getDockingContribution() {
    	if (dockContributionItem == null) {
    		dockContributionItem = new ContributionItem() {
    			public void fill(Menu menu, int index) {
    				// populate from superclass
    				super.fill(menu, index);
    				
    				// Add an 'immediate' 'pref'
    				MenuItem dragStyle = new MenuItem(menu, SWT.CHECK, index++);
    				dragStyle.setText(WorkbenchMessages.TrimCommon_Immediate);
    				dragStyle.setSelection(layout.isImmediate());
    				
    				dragStyle.addSelectionListener(new SelectionListener() {
						public void widgetSelected(SelectionEvent e) {
							layout.setImmediate(!layout.isImmediate());
						}

						public void widgetDefaultSelected(SelectionEvent e) {
						}
    				});

    				new MenuItem(menu, SWT.SEPARATOR, index++);
    				
    				if (trim.isCloseable()) {
	    				MenuItem closeItem = new MenuItem(menu, SWT.PUSH, index++);
	    				closeItem.setText(WorkbenchMessages.TrimCommon_Close);
	    				
	    				closeItem.addSelectionListener(new SelectionListener() {
							public void widgetSelected(SelectionEvent e) {
								handleCloseTrim();
							}

							public void widgetDefaultSelected(SelectionEvent e) {
							}
	    				});

	    				new MenuItem(menu, SWT.SEPARATOR, index++);
    				}
    				
    				dockCascade = new MenuItem(menu, SWT.CASCADE, index++);
    				{
    					dockCascade.setText(WorkbenchMessages.TrimCommon_DockOn); 
    					
    					sidesMenu = new Menu(dockCascade);
    					radioButtons = new RadioMenu(sidesMenu, radioVal);
    					
    					int validSides = trim.getValidSides();
    					if ((validSides & SWT.TOP) != 0)
    						radioButtons.addMenuItem(WorkbenchMessages.TrimCommon_Top, new Integer(SWT.TOP)); 
    					if ((validSides & SWT.BOTTOM) != 0)
    						radioButtons.addMenuItem(WorkbenchMessages.TrimCommon_Bottom, new Integer(SWT.BOTTOM)); 
    					if ((validSides & SWT.LEFT) != 0)
    						radioButtons.addMenuItem(WorkbenchMessages.TrimCommon_Left, new Integer(SWT.LEFT)); 
    					if ((validSides & SWT.RIGHT) != 0)
    						radioButtons.addMenuItem(WorkbenchMessages.TrimCommon_Right, new Integer(SWT.RIGHT)); 
    					
    					dockCascade.setMenu(sidesMenu);
    				}
    				
    				// Provide Show / Hide trim capabilities
    				showCascade = new MenuItem(menu, SWT.CASCADE, index++);
    				{
    					showCascade.setText(WorkbenchMessages.TrimCommon_ShowTrim); 
    					
    					showMenu = new Menu(dockCascade);
    					
    					// Construct a 'hide/show' cascade from -all- the existing trim...
    					IWindowTrim[] trimItems = layout.getAllTrim();
    					for (int i = 0; i < trimItems.length; i++) {
							MenuItem item = new MenuItem(showMenu, SWT.CHECK);
							item.setText(trimItems[i].getDisplayName());
							item.setSelection(trimItems[i].getControl().getVisible());
							item.setData(trimItems[i]);
							
							// TODO: Make this work...wire it off for now
							item.setEnabled(false);
							
							item.addSelectionListener(new SelectionListener() {

								public void widgetSelected(SelectionEvent e) {
									IWindowTrim trim = (IWindowTrim) e.widget.getData();
									layout.setTrimVisible(trim, !trim.getControl().getVisible());
								}

								public void widgetDefaultSelected(SelectionEvent e) {
								}
								
							});
						}
    					
    					showCascade.setMenu(showMenu);
    				}

    		    	// if the radioVal changes it means that the User wants to change the docking location
    		    	radioVal.addChangeListener(new IChangeListener() {
    					public void update(boolean changed) {
    						if (changed)
    							handleShowOnChange();
    					}
    		    	});
    			}
    		};
    	}
    	return dockContributionItem;
    }
    
	/**
	 * Handle the event generated when the "Close" item is
	 * selected on the context menu. This removes the associated
	 * trim and calls back to the IWidnowTrim to inform it that
	 * the User has closed the trim.
	 */
	private void handleCloseTrim() {
		layout.removeTrim(trim);
		trim.handleClose();
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    public void dispose() {
        if (radioButtons != null) {
            radioButtons.dispose();
        }

        // tidy up...
        removeControlListener(controlListener);
        removeListener(SWT.MenuDetect, menuListener);
        
        super.dispose();
    }

    /**
     * Begins dragging the trim
     * 
     * @param position initial mouse position
     */
    protected void startDraggingTrim(Point position) {
        Rectangle dragRect = DragUtil.getDisplayBounds(toDrag);
        
        // If we're doing an 'immediate' drag then avoid showing the
        // track rect at all to avoid cheese...
        if (layout.isImmediate())
        	dragRect = new Rectangle(0,0,0,0);

        DragUtil.performDrag(trim, dragRect, position, true);
    }

    /**
     * Shows the popup menu for an item in the fast view bar.
     */
    private void showDockTrimPopup(Point pt) {
        Menu menu = dockMenuManager.createContextMenu(toDrag);
        menu.setLocation(pt.x, pt.y);
        menu.setVisible(true);
    }	    
}
