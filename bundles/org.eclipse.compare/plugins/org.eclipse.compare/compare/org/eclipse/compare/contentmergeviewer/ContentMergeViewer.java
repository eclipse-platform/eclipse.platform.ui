/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000, 2001
 */
package org.eclipse.compare.contentmergeviewer;

import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.CLabel;

import org.eclipse.jface.util.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.*;
import org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.*;

/**
 * An abstract compare and merge viewer with two side-by-side content areas
 * and an optional content area for the ancestor. The implementation makes no
 * assumptions about the content type.
 * <p>
 * <code>ContentMergeViewer</code>
 * <ul>
 * <li>implements the overall layout and defines hooks so that subclasses
 *	can easily provide an implementation for a specific content type,
 * <li>implements the UI for making the areas resizable,
 * <li>has an action for controlling whether the ancestor area is visible or not,
 * <li>has actions for copying one side of the input to the other side,
 * <li>tracks the dirty state of the left and right sides and send out notification
 *	on state changes.
 * </ul>
 * A <code>ContentMergeViewer</code> accesses its
 * model by means of a content provider which must implement the
 * <code>IMergeViewerContentProvider</code> interface.
 * </p>
 * <p>
 * Clients may wish to use the standard concrete subclass <code>TextMergeViewer</code>,
 * or define their own subclass.
 * 
 * @see IMergeViewerContentProvider
 * @see TextMergeViewer
 */
public abstract class ContentMergeViewer extends ContentViewer implements IPropertyChangeNotifier {
	
	/**
	 * Property names.
	 */
	private static final String ANCESTOR_ENABLED= ComparePreferencePage.INITIALLY_SHOW_ANCESTOR_PANE;	
	
	/* package */ static final int HORIZONTAL= 1;
	/* package */ static final int VERTICAL= 2;
	
	static final double HSPLIT= 0.5;
	static final double VSPLIT= 0.3;
	
	private class ContentMergeViewerLayout extends Layout {
		
		public Point computeSize(Composite c, int w, int h, boolean force) {
			return new Point(100, 100);
		}
		
		public void layout(Composite composite, boolean force) {
			
			// determine some derived sizes
			int headerHeight= fLeftLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y;
			Rectangle r= composite.getClientArea();
			
			int centerWidth= getCenterWidth();	
			int width1= (int)((r.width-centerWidth)*fHSplit);
			int width2= r.width-width1-centerWidth;
			
			int height1= 0;
			int height2= 0;
			if (fAncestorEnabled && fShowAncestor) {
				height1= (int)((r.height-(2*headerHeight))*fVSplit);
				height2= r.height-(2*headerHeight)-height1;
			} else {
				height1= 0;
				height2= r.height-headerHeight;
			}		
							
			int y= 0;
			
			if (fAncestorEnabled && fShowAncestor) {
				fAncestorLabel.setBounds(0, y, r.width, headerHeight);
				fAncestorLabel.setVisible(true);
				y+= headerHeight;
				handleResizeAncestor(0, y, r.width, height1);
				y+= height1;
			} else {
				fAncestorLabel.setVisible(false);
				handleResizeAncestor(0, 0, 0, 0);
			}
			
			fLeftLabel.getSize();	// WORKAROUND FOR PR
			
			if (centerWidth > 3) {
				fLeftLabel.setBounds(0, y, width1+1, headerHeight);
				fDirectionLabel.setVisible(true);
				fDirectionLabel.setBounds(width1+1, y, centerWidth-1, headerHeight);
				fRightLabel.setBounds(width1+centerWidth, y, width2, headerHeight);
			} else {
				fLeftLabel.setBounds(0, y, width1, headerHeight);
				fDirectionLabel.setVisible(false);
				fRightLabel.setBounds(width1, y, r.width-width1, headerHeight);
			}
			
			y+= headerHeight;
			
			if (fCenter != null && !fCenter.isDisposed())
				fCenter.setBounds(width1, y, centerWidth, height2);
					
			handleResizeLeftRight(0, y, width1, centerWidth, width2, height2);
		}
	}

	class Resizer extends MouseAdapter implements MouseMoveListener {
				
		Control fControl;
		int fX, fY;
		int fWidth1, fWidth2;
		int fHeight1, fHeight2;
		int fDirection;
		boolean fLiveResize;
		
		public Resizer(Control c, int dir) {
			fDirection= dir;
			fControl= c;
			fControl.addMouseListener(this);
			fLiveResize= !(fControl instanceof Sash);
			
			fControl.addDisposeListener(
				new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						fControl= null;
					}
				}
			);
		}
		
		public void mouseDoubleClick(MouseEvent e) {
			if ((fDirection & HORIZONTAL) != 0)
				fHSplit= HSPLIT;
			if ((fDirection & VERTICAL) != 0)
				fVSplit= VSPLIT;
			fComposite.layout(true);
		}
		
		public void mouseDown(MouseEvent e) {
			Composite parent= fControl.getParent();
			
			Point s= parent.getSize();
			Point as= fAncestorLabel.getSize();
			Point ys= fLeftLabel.getSize();
			Point ms= fRightLabel.getSize();
			
			fWidth1= ys.x;
			fWidth2= ms.x;
			fHeight1= fLeftLabel.getLocation().y-as.y;
			fHeight2= s.y-(fLeftLabel.getLocation().y+ys.y);
			
			fX= e.x;
			fY= e.y;
			fControl.addMouseMoveListener(this);
		}
		
		public void mouseUp(MouseEvent e) {
			fControl.removeMouseMoveListener(this);
			if (!fLiveResize)
				resize(e);
		}
		
		public void mouseMove(MouseEvent e) {
			if (fLiveResize)
				resize(e);
		}
		
		private void resize(MouseEvent e) {
			int dx= e.x-fX;
			int dy= e.y-fY;
		
			int centerWidth= fCenter.getSize().x;

			if (fWidth1 + dx > centerWidth && fWidth2 - dx > centerWidth) {
				fWidth1+= dx;
				fWidth2-= dx;
				if ((fDirection & HORIZONTAL) != 0)
					fHSplit= (double)fWidth1/(double)(fWidth1+fWidth2);
			}
			if (fHeight1 + dy > centerWidth && fHeight2 - dy > centerWidth) {
				fHeight1+= dy;
				fHeight2-= dy;
				if ((fDirection & VERTICAL) != 0)
					fVSplit= (double)fHeight1/(double)(fHeight1+fHeight2);
			}

			fComposite.layout(true);
			fControl.getDisplay().update();
		}
	};

	/** Style bits for top level composite */
	private int fStyles;
	private ResourceBundle fBundle;
	private CompareConfiguration fCompareConfiguration;
	private IPropertyChangeListener fPropertyChangeListener;
	private ICompareInputChangeListener fCompareInputChangeListener;
	private ListenerList fListenerList;

	private boolean fLeftDirty;		// left side is dirty
	private boolean fRightDirty;		// right side is dirty
	
	private double fHSplit= HSPLIT;		// width ratio of left and right panes
	private double fVSplit= VSPLIT;		// height ratio of ancestor and bottom panes
	
	private boolean fAncestorEnabled= true;	// show ancestor in case of conflicts
	/* package */ boolean fShowAncestor= false;	// if current input has conflicts
	private boolean fIsThreeWay= false;
	private ActionContributionItem fAncestorItem;
	
	private Action fCopyLeftToRightAction;	// copy from left to right
	private Action fCopyRightToLeftAction;	// copy from right to left

	// SWT widgets
	/* package */ Composite fComposite;
	private CLabel fAncestorLabel;
	private CLabel fLeftLabel;
	private CLabel fRightLabel;
	private CLabel fDirectionLabel;
	/* package */ Control fCenter;
		
	//---- SWT resources to be disposed
	private Image fRightArrow;
	private Image fLeftArrow;
	private Image fBothArrow;
	//---- end
	
	/**
	 * Creates a new content merge viewer and initializes with a resource bundle and a
	 * configuration.
	 *
	 * @param bundle the resource bundle
	 * @param cc the configuration object
	 */
	protected ContentMergeViewer(int style, ResourceBundle bundle, CompareConfiguration cc) {
		fStyles= style;
		fBundle= bundle;
		
		
		fAncestorEnabled= Utilities.getBoolean(cc, ANCESTOR_ENABLED, fAncestorEnabled);

		setContentProvider(new MergeViewerContentProvider(cc));
		
		fCompareInputChangeListener= new ICompareInputChangeListener() {
			public void compareInputChanged(ICompareInput input) {
				ContentMergeViewer.this.compareInputChanged(input);
			}
		};
		
		fPropertyChangeListener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				ContentMergeViewer.this.propertyChange(event);
			}
		};
		
		fCompareConfiguration= cc;
		if (fCompareConfiguration != null)
			fCompareConfiguration.addPropertyChangeListener(fPropertyChangeListener);
	}
	
	//---- hooks ---------------------
	
	/**
	 * Returns the viewer's name.
	 *
	 * @return the viewer's name
	 */
	public String getTitle() {
		return "Content Compare";
	}
	
	/**
	 * Creates the SWT controls for the ancestor, left, and right
	 * content areas of this compare viewer.
	 * Implementations typically hold onto the controls
	 * so that they can be initialized with the input objects in method
	 * <code>updateContent</code>.
	 *
	 * @param composite the container for the three areas
	 */
	abstract protected void createControls(Composite composite);

	/**
	 * Lays out the ancestor area of the compare viewer.
	 * It is called whenever the viewer is resized or when the sashes between
	 * the areas are moved to adjust the size of the areas.
	 *
	 * @param x the horizontal position of the ancestor area within its container
	 * @param y the vertical position of the ancestor area within its container
	 * @param width the width of the ancestor area
	 * @param height the height of the ancestor area
	 */
	abstract protected void handleResizeAncestor(int x, int y, int width, int height);
	
	/**
	 * Lays out the left and right areas of the compare viewer.
	 * It is called whenever the viewer is resized or when the sashes between
	 * the areas are moved to adjust the size of the areas.
	 *
	 * @param x the horizontal position of the left area within its container
	 * @param y the vertical position of the left and right area within its container
	 * @param leftWidth the width of the left area
	 * @param centerWidth the width of the gap between the left and right areas
	 * @param rightWidth the width of the right area
	 * @param height the height of the left and right areas
	 */
	abstract protected void handleResizeLeftRight(int x, int y, int leftWidth, int centerWidth,
			int rightWidth, int height);

	/**
	 * Contributes items to the given <code>ToolBarManager</code>.
	 * It is called when this viewer is installed in its container and if the container
	 * has a <code>ToolBarManager</code>.
	 * The <code>ContentMergeViewer</code> implementation of this method does nothing.
	 * Subclasses may reimplement.
	 *
	 * @param toolBarManager the toolbar manager to contribute to
	 */
	protected void createToolItems(ToolBarManager toolBarManager) {
	}

	/**
	 * Initializes the controls of the three content areas with the given input objects.
	 *
	 * @param ancestor the input for the ancestor area
	 * @param left the input for the left area
	 * @param right the input for the right area
	 */
	abstract protected void updateContent(Object ancestor, Object left, Object right);
		
	/**
	 * Copies the content of one side to the other side.
	 * Called from the (internal) actions for copying the sides of the viewer's input object.
	 * 
	 * @param leftToRight if <code>true</code>, the left side is copied to the right side;
	 * if <code>false</code>, the right side is copied to the left side
	 */
	abstract protected void copy(boolean leftToRight);

	/**
	 * Returns the byte contents of the left or right side. If the viewer
	 * has no editable content <code>null</code> can be returned.
	 *
	 * @param left if <code>true</code>, the byte contents of the left area is returned;
	 * 	if <code>false</code>, the byte contents of the right area
	 * @return the content as an array of bytes, or <code>null</code>
	 */
	abstract protected byte[] getContents(boolean left);

	//----------------------------
	
	/**
	 * Returns the resource bundle of this viewer.
	 *
	 * @return the resource bundle
	 */
	protected ResourceBundle getResourceBundle() {
		return fBundle;
	}
	
	/**
	 * Returns the compare configuration of this viewer,
	 * or <code>null</code> if this viewer does not yet have a configuration.
	 *
	 * @return the compare configuration, or <code>null</code> if none
	 */
	protected CompareConfiguration getCompareConfiguration() {
		return fCompareConfiguration;
	}
	
	/**
	 * The <code>ContentMergeViewer</code> implementation of this 
	 * <code>ContentViewer</code> method
	 * checks to ensure that the content provider is an <code>IMergeViewerContentProvider</code>.
	 */
	public void setContentProvider(IContentProvider contentProvider) {
		Assert.isTrue(contentProvider instanceof IMergeViewerContentProvider);
		super.setContentProvider(contentProvider);
	}

	/* package */ IMergeViewerContentProvider getMergeContentProvider() {
		return (IMergeViewerContentProvider) getContentProvider();
	}

	public void refresh() {
	}
	
	/**
	 * The <code>ContentMergeViewer</code> implementation of this 
	 * <code>Viewer</code> method returns the empty selection. Subclasses may override.
	 */
	public ISelection getSelection() {
		return new ISelection() {
			public boolean isEmpty() {
				return true;
			}
		};
	}
	
	/**
	 * The <code>ContentMergeViewer</code> implementation of this 
	 * <code>Viewer</code> method does nothing. Subclasses may reimplement.
	 */
	public void setSelection(ISelection s, boolean reveal) {
	}

	/* package */ void propertyChange(PropertyChangeEvent event) {
		
		String key= event.getProperty();
//		if (key.equals(ICompareConfiguration.MERGE_DIRECTION)) {
//			if (isDirty() && !saveContents(true, true))
//				return;
//			ToolBarManager tbm= null;
//			IVisualContainer vc= getContainer();
//			if (vc instanceof Pane)
//				tbm= ((Pane)vc).getToolBarManager();
//			if (tbm != null) {
//				updateToolItems();
//				tbm.update(true);
//			}
//	
//			updateDirectionLabel();
//		} else
		if (key.equals(ANCESTOR_ENABLED)) {
			fAncestorEnabled= Utilities.getBoolean(getCompareConfiguration(), ANCESTOR_ENABLED, fAncestorEnabled);
			fComposite.layout(true);
		}
	}
		
	//---- input
			 
	/* package */ boolean isThreeWay() {
		return fIsThreeWay;
	}
	
	/**
	 * Internal hook method called when the input to this viewer is
	 * initially set or subsequently changed.
	 * <p>
	 * The <code>ContentMergeViewer</code> implementation of this <code>Viewer</code>
	 * method retrieves the content from the three sides by calling the methods
	 * <code>getAncestorContent</code>, <code>getLeftContent</code>,
	 * and <code>getRightContent</code> on the content provider.
	 * The values returned from these calls are passed to the hook method <code>updateContent</code>.
	 * </p>
	 *
	 * @param input the new input of this viewer, or <code>null</code> if none
	 * @param oldInput the old input element, or <code>null</code> if there
	 *   was previously no input
	 */
	protected final void inputChanged(Object input, Object oldInput) {
		
		if (oldInput instanceof ICompareInput)
			((ICompareInput)oldInput).removeCompareInputChangeListener(fCompareInputChangeListener);
		
		// before setting the new input we have to save the old
		if (fLeftDirty || fRightDirty)
			saveContent(oldInput);
			
		if (input instanceof ICompareInput)
			((ICompareInput)input).addCompareInputChangeListener(fCompareInputChangeListener);
		
		setLeftDirty(false);
		setRightDirty(false);

		// determine the merge direction
		//boolean rightEditable= fMergeViewerContentProvider.isRightEditable(input);
		//boolean leftEditable= fMergeViewerContentProvider.isLeftEditable(input);
				
//		if (fInput instanceof ICompareInput)	
//			fIsThreeWay= (((ICompareInput)fInput).getChangeType() & Differencer.DIRECTION_MASK) != 0;
//		else
//			fIsThreeWay= true;

		IMergeViewerContentProvider content= getMergeContentProvider();
		
		Object ancestor= content.getAncestorContent(input);
		fIsThreeWay= ancestor != null;
			
		if (fAncestorItem != null)
			fAncestorItem.setVisible(fIsThreeWay);
			
		boolean oldFlag= fShowAncestor;
		fShowAncestor= fIsThreeWay && content.showAncestor(input);
		
		if (fAncestorEnabled && oldFlag != fShowAncestor)
			fComposite.layout(true);
		
		ToolBarManager tbm= CompareViewerSwitchingPane.getToolBarManager(fComposite.getParent());
		if (tbm != null) {
			updateToolItems();
			tbm.update(true);
			tbm.getControl().getParent().layout(true);
		}
		
		updateHeader();
								
		Object left= content.getLeftContent(input);
		Object right= content.getRightContent(input);
		updateContent(ancestor, left, right);
	}

	private void compareInputChanged(ICompareInput input) {
				
		if (input != null) {
			
			IMergeViewerContentProvider content= getMergeContentProvider();

			Object ancestor= content.getAncestorContent(input);
			Object left= content.getLeftContent(input);
			Object right= content.getRightContent(input);
					
			updateContent(ancestor, left, right);
		}
	}
	
	//---- layout & SWT control creation
		
	/**
	 * Builds the SWT controls for the three areas of a compare/merge viewer.
	 * <p>
	 * Calls the hooks <code>createControls</code> and <code>createToolItems</code>
	 * to let subclasses build the specific content areas and to add items to
	 * an enclosing toolbar.
	 * <p>
	 * This method must only be called in the constructor of subclasses.
	 *
	 * @param parent the parent control
	 * @return the new control
	 */
	protected final Control buildControl(Composite parent) {
									
		fComposite= new Composite(parent, fStyles) {
			public boolean setFocus() {
				return internalSetFocus();
			}
		};
		fComposite.setData(CompareUI.COMPARE_VIEWER_TITLE, getTitle());

		hookControl(fComposite);	// hook help & dispose listener
		
		fComposite.setLayout(new ContentMergeViewerLayout());
		
		int style= SWT.SHADOW_OUT;
		fAncestorLabel= new CLabel(fComposite, style);
		
		fLeftLabel= new CLabel(fComposite, style);
		new Resizer(fLeftLabel, VERTICAL);
		
		fDirectionLabel= new CLabel(fComposite, style);
		fDirectionLabel.setAlignment(SWT.CENTER);
		new Resizer(fDirectionLabel, HORIZONTAL | VERTICAL);
		
		fRightLabel= new CLabel(fComposite, style);
		new Resizer(fRightLabel, VERTICAL);
		
		if (fCenter == null || fCenter.isDisposed())
			fCenter= createCenter(fComposite);
				
		createControls(fComposite);
						
		ToolBarManager tbm= CompareViewerSwitchingPane.getToolBarManager(parent);
		if (tbm != null) {
			tbm.removeAll();
			
			// define groups
			tbm.add(new Separator("merge"));			
			tbm.add(new Separator("modes"));
			tbm.add(new Separator("navigation"));
			
			CompareConfiguration cc= getCompareConfiguration();
		
			if (cc.isRightEditable()) {
				fCopyLeftToRightAction=
					new Action() {
						public void run() {
							copy(true);
						}
					};
				Utilities.initAction(fCopyLeftToRightAction, getResourceBundle(), "action.CopyLeftToRight.");
				tbm.appendToGroup("merge", fCopyLeftToRightAction);
			}
			
			if (cc.isLeftEditable()) {
				fCopyRightToLeftAction=
					new Action() {
						public void run() {
							copy(false);
						}
					};
				Utilities.initAction(fCopyRightToLeftAction, getResourceBundle(), "action.CopyRightToLeft.");
				tbm.appendToGroup("merge", fCopyRightToLeftAction);
			}
			
			Action a= new ChangePropertyAction(fBundle, fCompareConfiguration, "action.EnableAncestor.", ANCESTOR_ENABLED);
			a.setChecked(fAncestorEnabled);
			fAncestorItem= new ActionContributionItem(a);
			//fAncestorItem.setVisible(false);
			tbm.appendToGroup("modes", fAncestorItem);
			
			createToolItems(tbm);
			updateToolItems();
			
			tbm.update(true);
		}
	
		return fComposite;
	}
	
	/* package */ boolean internalSetFocus() {
		return false;
	}
	
	/* package */ int getCenterWidth() {
		return 3;
	}
	
	/* package */ Control createCenter(Composite parent) {
		Sash sash= new Sash(parent, SWT.VERTICAL);
		new Resizer(sash, HORIZONTAL);
		return sash;
	}
	
	/* package */ Control getCenter() {
		return fCenter;
	}
		
	/* 
	 * @see Viewer.getControl()
	 */
	public Control getControl() {
		return fComposite;
	}
	
	/**
	 * Called on the viewer disposal.
	 * Unregisters from the compare configuration.
	 * Clients may extend if they have to do additional cleanup.
	 */
	protected void handleDispose(DisposeEvent event) {
		
		Object input= getInput();	
		if (input instanceof ICompareInput)
			((ICompareInput)input).removeCompareInputChangeListener(fCompareInputChangeListener);
		
		if (fCompareConfiguration != null)
			fCompareConfiguration.removePropertyChangeListener(fPropertyChangeListener);

		fAncestorLabel= null;
		fLeftLabel= null;
		fDirectionLabel= null;
		fRightLabel= null;
		fCenter= null;
				
		if (fRightArrow != null) {
			fRightArrow.dispose();
			fRightArrow= null;
		}
		if (fLeftArrow != null) {
			fLeftArrow.dispose();
			fLeftArrow= null;
		}
		if (fBothArrow != null) {
			fBothArrow.dispose();
			fBothArrow= null;
		}
		
		super.handleDispose(event);
  	}
  	
	/**
	 * Updates the enabled state of the toolbar items.
	 * <p>
	 * This method is called whenever the state of the items needs updating.
	 * <p>
	 * Subclasses may extend this method, although this is generally not required.
	 */
	protected void updateToolItems() {
										
		IMergeViewerContentProvider content= getMergeContentProvider();
		Object input= getInput();
		
		if (fCopyLeftToRightAction != null)
			fCopyLeftToRightAction.setEnabled(content.isRightEditable(input));
		
		if (fCopyRightToLeftAction != null)
			fCopyRightToLeftAction.setEnabled(content.isLeftEditable(input));
	}
	
//	protected void createToolItems(ToolBarManager tbm) {
//		
//		if (USE_MORE_CONTROLS) {
//			fBothItem= new ActionContributionItem(
//				new Action(fBundle, "action.AcceptBoth.") {
//					public void actionPerformed(Window w) {
//						accept(fCurrentDiff, true, false);
//					}
//				}
//			);
//			tbm.appendToGroup("merge", fBothItem);
//	
//			fAutoItem= new ActionContributionItem(
//				new Action(fBundle, "action.AcceptAll.") {
//					public void actionPerformed(Window w) {
//						autoResolve();
//					}
//				}
//			);
//			tbm.appendToGroup("merge", fAutoItem);
//		}
//		fRejectItem= new ActionContributionItem(
//			new Action(fBundle, "action.AcceptIgnoreNow.") {
//				public void actionPerformed(Window w) {
//					reject(fCurrentDiff, true);
//				}
//			}
//		);
//		tbm.appendToGroup("merge", fRejectItem);
//		
//		Action a= new ChangePropertyAction(getBundle(), TextMergeViewer.class, getCompareConfiguration(), "action.SynchMode.", SYNC_SCROLLING);
//		a.setChecked(fSynchronizedScrolling);
////		tbm.appendToGroup("modes", a);
//		tbm.add(a);
//		
//		tbm.add(new Separator());
//					
//		a= new Action() {
//			public void actionPerformed() {
//				navigate(true);
//			}
//		};
//		CompareUIPlugin.init(a, TextMergeViewer.class, getBundle(), "action.NextDiff.");
//		fNextItem= new ActionContributionItem(a);
//		//tbm.appendToGroup("navigation", fNextItem);
//		tbm.add(fNextItem);
//		
//		a= new Action() {
//			public void actionPerformed() {
//				navigate(false);
//			}
//		};
//		CompareUIPlugin.init(a, TextMergeViewer.class, getBundle(), "action.PrevDiff.");
//		fPreviousItem= new ActionContributionItem(a);
//		//tbm.appendToGroup("navigation", fPreviousItem);
//		tbm.add(fPreviousItem);
//	}

	/**
	 * Updates the headers of the three areas
	 * by querying the content provider for a name and image for
	 * the three sides of the input object.
	 * <p>
	 * This method is called whenever the header must be updated.
	 * <p>
	 * Subclasses may extend this method, although this is generally not required.
	 */
	protected void updateHeader() {
						
		IMergeViewerContentProvider content= getMergeContentProvider();
		Object input= getInput();

		if (fAncestorLabel != null) {
			fAncestorLabel.setImage(content.getAncestorImage(input));
			fAncestorLabel.setText(content.getAncestorLabel(input));
		}
		if (fLeftLabel != null) {
			fLeftLabel.setImage(content.getLeftImage(input));
			fLeftLabel.setText(content.getLeftLabel(input));
		}
		if (fRightLabel != null) {
			fRightLabel.setImage(content.getRightImage(input));
			fRightLabel.setText(content.getRightLabel(input));
		}
		
		updateDirectionLabel();
	}
	
	private Image loadImage(String name) {
		ImageDescriptor id= ImageDescriptor.createFromFile(ContentMergeViewer.class, name);
		if (id != null)
			return id.createImage();
		return null;
	}
	
	private void updateDirectionLabel() {
//		if (fDirectionLabel != null) {
//			Image image= null;
//			
//			//if (fMergePolicy.hasMergeDirection()) {
//				boolean y= fCompareConfiguration.isLeftEditable();
//				boolean m= fCompareConfiguration.isRightEditable();
//				
//				if (y && m) {
//					if (fBothArrow == null)
//						fBothArrow= loadImage("images/both.gif"); 
//					image= fBothArrow;
//				} else if (y) {
//					if (fLeftArrow == null)
//						fLeftArrow= loadImage("images/yours.gif");
//					image= fLeftArrow;
//				} else if (m) {
//					if (fRightArrow == null)
//						fRightArrow= loadImage("images/mine.gif"); 
//					image= fRightArrow;
//				}
//			//}
//	
//			if (image != null)
//				fDirectionLabel.setImage(image);
//			else
//				fDirectionLabel.setText("");
//		}
	}
	
	/**
	 * Calculates the height of the header.
	 */
	/* package */ int getHeaderHeight() {
		int headerHeight= fLeftLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y;
		headerHeight= Math.max(headerHeight, fDirectionLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y);		
		return headerHeight;
	}

	//---- merge direction
	
	/**
	 * Returns true if both sides are editable.
	 */
	/* package */ boolean canToggleMergeDirection() {
		IMergeViewerContentProvider content= getMergeContentProvider();
		Object input= getInput();
		return content.isLeftEditable(input) && content.isRightEditable(input);
	}
	
	//---- dirty state & saving state
	
	/* (non Javadoc)
	 * see IPropertyChangeNotifier.addPropertyChangeListener
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		if (fListenerList == null)
			fListenerList= new ListenerList();
		fListenerList.add(listener);
	}
	
	/* (non Javadoc)
	 * see IPropertyChangeNotifier.removePropertyChangeListener
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		if (fListenerList != null) {
			fListenerList.remove(listener);
			if (fListenerList.isEmpty())
				fListenerList= null;
		}
	}
	
	/* package */ void fireDirtyState(boolean state) {
		Utilities.firePropertyChange(fListenerList, this, CompareEditorInput.DIRTY_STATE, null, new Boolean(state));
	}
	
	/**
	 * Sets the dirty state of the left side of this viewer.
	 * If the new value differs from the old
	 * all registered listener are notified with
	 * a <code>PropertyChangeEvent</code> with the
	 * property name <code>CompareEditorInput.DIRTY_STATE</code>.
	 *
	 * @param dirty the state of the left side dirty flag
	 */
	protected void setLeftDirty(boolean dirty) {
		if (fLeftDirty != dirty) {
			fLeftDirty= dirty;
			fireDirtyState(dirty);
//			if (fActions != null) {
//				Action saveAction= (Action) fActions.get("Save");
//				if (saveAction != null)
//					saveAction.setEnabled(dirty);
//			}
		}
	}
	
	/**
	 * Sets the dirty state of the right side of this viewer.
	 * If the new value differs from the old
	 * all registered listener are notified with
	 * a <code>PropertyChangeEvent</code> with the
	 * property name <code>CompareEditorInput.DIRTY_STATE</code>.
	 *
	 * @param dirty the state of the right side dirty flag
	 */
	protected void setRightDirty(boolean dirty) {
		if (fRightDirty != dirty) {
			fRightDirty= dirty;
			fireDirtyState(dirty);
//			if (fActions != null) {
//				Action saveAction= (Action) fActions.get("Save");
//				if (saveAction != null)
//					saveAction.setEnabled(dirty);
//			}
		}
	}
			
	/**
	 * Save modified content back to input elements via the content provider.
	 */
	/* package */ void saveContent(Object oldInput) {
		
		// write back modified contents
		IMergeViewerContentProvider content= (IMergeViewerContentProvider) getContentProvider();
		
		if (fCompareConfiguration.isLeftEditable() && fLeftDirty) {
			
			byte[] bytes= getContents(true);
			content.saveLeftContent(oldInput, bytes);	
			setLeftDirty(false);
		}
		
		if (fCompareConfiguration.isRightEditable() && fRightDirty) {
			
			byte[] bytes= getContents(false);
			content.saveRightContent(oldInput, bytes);				
			setRightDirty(false);
		}
	}
}

