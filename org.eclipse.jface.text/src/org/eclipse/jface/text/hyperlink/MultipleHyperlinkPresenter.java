/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.hyperlink;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.IInformationControlExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.IWidgetTokenKeeperExtension;
import org.eclipse.jface.text.IWidgetTokenOwner;
import org.eclipse.jface.text.IWidgetTokenOwnerExtension;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.Region;


/**
 * A hyperlink presenter capable of showing multiple hyperlinks in a hover.
 * 
 * @since 3.4
 */
public class MultipleHyperlinkPresenter extends DefaultHyperlinkPresenter {
	
	/**
	 * An information control capable of showing a list of hyperlinks. The hyperlinks can be opened.
	 */
	private static class LinkListInformationControl extends AbstractInformationControl implements IInformationControlExtension2 {
		
		private static final class LinkConentenProvider implements IStructuredContentProvider {
			
			/*
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}
			
			/*
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {
			}
			
			/*
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		}
		
		private static final class LinkLabelProvider extends ColumnLabelProvider {
			
			private final IHyperlink fDefaultLink;
			
			public LinkLabelProvider(IHyperlink defaultLink) {
				fDefaultLink= defaultLink;
			}
			
			/*
			 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				IHyperlink link= (IHyperlink) element;
				
				String text= link.getHyperlinkText();
				if (text == null)
					text= HyperlinkMessages.getString("LinkListInformationControl.unknownLink"); //$NON-NLS-1$
					
				if (link == fDefaultLink) {
					text= MessageFormat.format(HyperlinkMessages.getString("LinkListInformationControl.defaultLinkPattern"), new Object[] { text }); //$NON-NLS-1$
				}
				
				return text;
			}
			

//			/*
//			 * @see org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider#getStyledText(java.lang.Object)
//			 */
//			public StyledStringBuilder getStyledText(Object element) {
//				String text= getText(element);
//				StyledStringBuilder result= new StyledStringBuilder(text);
//				if (element == fDefaultLink) {
//					result.setStyle(text.length() - 10, 10, new Styler() {
//						public void applyStyles(TextStyle textStyle) {
//							textStyle.foreground= Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
//						}
//					});
//				}
//
//				return result;
//			}
		}
		
		private final MultipleHyperlinkHoverManager fManager;
		
		private IHyperlink[] fInput;
		private Composite fParent;
		private Table fTable;
		
		/**
		 * Creates a link list information control with the given shell as parent.
		 *
		 * @param parentShell the parent shell
		 * @param manager
		 */
		public LinkListInformationControl(Shell parentShell, MultipleHyperlinkHoverManager manager) {
			super(parentShell, true);
			fManager= manager;
			
			create();
		}
		
		/*
		 * @see org.eclipse.jface.text.IInformationControl#setInformation(java.lang.String)
		 */
		public void setInformation(String information) {
			//replaced by IInformationControlExtension2#setInput(java.lang.Object)
		}
		
		/*
		 * @see org.eclipse.jface.text.IInformationControlExtension2#setInput(java.lang.Object)
		 */
		public void setInput(Object input) {
			fInput= (IHyperlink[]) input;
			deferredCreateContent(fParent);
		}
		
		/*
		 * @see org.eclipse.jface.text.AbstractInformationControl#createContent(org.eclipse.swt.widgets.Composite)
		 */
		protected void createContent(Composite parent) {
			fParent= new Composite(parent, SWT.NONE);
			
			GridLayout layout= new GridLayout(1, false);
			layout.marginWidth= 0;
			fParent.setLayout(layout);
			fParent.setBackground(fParent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		}
		
		/*
		 * @see org.eclipse.jface.text.AbstractInformationControl#computeSizeHint()
		 */
		public Point computeSizeHint() {
			Point preferedSize= getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			
			Point constraints= getSizeConstraints();
			if (constraints == null)
				return preferedSize;
		
			if (fTable.getVerticalBar() == null || fTable.getHorizontalBar() == null)
				return Geometry.min(constraints, preferedSize);
			
			int scrollBarWidth= fTable.getVerticalBar().getSize().x;
			int scrollBarHeight= fTable.getHorizontalBar().getSize().y;
			
			int width;
			if (preferedSize.y - scrollBarHeight < constraints.y) {
				width= preferedSize.x - scrollBarWidth;
			} else {
				width= Math.min(preferedSize.x, constraints.x);
			}
			
			int height;
			if (preferedSize.x - scrollBarWidth < constraints.x) {
				height= preferedSize.y - scrollBarHeight;
			} else {
				height= Math.min(preferedSize.y, constraints.y);
			}
			
			return new Point(width, height);
		}
		
		private void deferredCreateContent(Composite parent) {
			fTable= new Table(parent, SWT.SINGLE);
			GridData data= new GridData(SWT.FILL, SWT.FILL, true, true);
			fTable.setLayoutData(data);
			fTable.setLinesVisible(false);
			fTable.setHeaderVisible(false);
			fTable.setBackground(parent.getBackground());
			
			final TableViewer viewer= new TableViewer(fTable);
			viewer.setContentProvider(new LinkConentenProvider());
			viewer.setLabelProvider(new LinkLabelProvider(fInput[0]));
			viewer.setInput(fInput);
			
			registerQuickViewTableListeners(viewer);
			
			getShell().addShellListener(new ShellAdapter() {
				
				/*
				 * @see org.eclipse.swt.events.ShellAdapter#shellActivated(org.eclipse.swt.events.ShellEvent)
				 */
				public void shellActivated(ShellEvent e) {
					if (viewer.getTable().getSelectionCount() == 0) {
						viewer.getTable().setSelection(0);
					}
					
					viewer.getTable().setFocus();
				}
			});
		}
		
		private void registerQuickViewTableListeners(final TableViewer viewer) {
			final Table table= viewer.getTable();
			
			table.addMouseMoveListener(new MouseMoveListener() {
				TableItem fLastItem= null;
				
				public void mouseMove(MouseEvent e) {
					if (table.equals(e.getSource())) {
						Object o= table.getItem(new Point(e.x, e.y));
						if (o instanceof TableItem) {
							TableItem item= (TableItem) o;
							if (!o.equals(fLastItem)) {
								fLastItem= (TableItem) o;
								table.setSelection(new TableItem[] { fLastItem });
							} else if (e.y < table.getItemHeight() / 4) {
								// Scroll up
								int index= table.indexOf(item);
								if (index > 0) {
									fLastItem= table.getItem(index - 1);
									table.setSelection(new TableItem[] { fLastItem });
								}
							} else if (e.y > table.getBounds().height - table.getItemHeight() / 4) {
								// Scroll down
								int index= table.indexOf(item);
								if (index < table.getItemCount() - 1) {
									fLastItem= table.getItem(index + 1);
									table.setSelection(new TableItem[] { fLastItem });
								}
							}
						}
					}
				}
			});
			
			table.addMouseListener(new MouseAdapter() {
				public void mouseUp(MouseEvent e) {
					
					if (table.getSelectionCount() < 1)
						return;
					
					if (e.button != 1)
						return;
					
					if (table.equals(e.getSource())) {
						Object o= table.getItem(new Point(e.x, e.y));
						TableItem selection= table.getSelection()[0];
						if (selection.equals(o)) {
							openLink((IHyperlink) selection.getData());
						}
					}
				}
			});
			
			viewer.addOpenListener(new IOpenListener() {
				public void open(OpenEvent event) {
					StructuredSelection selection= (StructuredSelection) event.getSelection();
					openLink((IHyperlink) selection.getFirstElement());
				}
			});
		}
		
		/*
		 * @see org.eclipse.jface.text.IInformationControlExtension#hasContents()
		 */
		public boolean hasContents() {
			return true;
		}

		/**
		 * Opens the given link.
		 * 
		 * @param link the link to open
		 */
		private void openLink(IHyperlink link) {
			fManager.hideInformationControl();
			link.open();
		}
	}
	
	private class MultipleHyperlinkHover implements ITextHover, ITextHoverExtension {
		
		/**
		 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
		 * @deprecated
		 */
		public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
			return null;
		}
		
		/*
		 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer, int)
		 */
		public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
			return fSubjectRegion;
		}
		
		/*
		 * @see org.eclipse.jface.text.ITextHoverExtension2#getHoverInfo2(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
		 */
		public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
			return fHyperlinks;
		}
		
		/*
		 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
		 */
		public IInformationControlCreator getHoverControlCreator() {
			return new IInformationControlCreator() {
				public IInformationControl createInformationControl(Shell parent) {
					return new LinkListInformationControl(parent, fManager);
				}
			};
		}
	}
	
	private static class MultipleHyperlinkHoverManager extends AbstractInformationControlManager implements IWidgetTokenKeeper, IWidgetTokenKeeperExtension {
		
		private class Closer implements IInformationControlCloser, Listener, KeyListener {
			
			private Control fSubjectControl;
			private Display fDisplay;
			private IInformationControl fControl;
			private Rectangle fSubjectArea;
			
			/*
			 * @see org.eclipse.jface.text.AbstractInformationControlManager.IInformationControlCloser#setInformationControl(org.eclipse.jface.text.IInformationControl)
			 */
			public void setInformationControl(IInformationControl control) {
				fControl= control;
			}
			
			/*
			 * @see org.eclipse.jface.text.AbstractInformationControlManager.IInformationControlCloser#setSubjectControl(org.eclipse.swt.widgets.Control)
			 */
			public void setSubjectControl(Control subject) {
				fSubjectControl= subject;
			}
			
			/*
			 * @see org.eclipse.jface.text.AbstractInformationControlManager.IInformationControlCloser#start(org.eclipse.swt.graphics.Rectangle)
			 */
			public void start(Rectangle subjectArea) {
				fSubjectArea= subjectArea;
				
				fDisplay= fSubjectControl.getDisplay();
				if (!fDisplay.isDisposed()) {
					fDisplay.addFilter(SWT.FocusOut, this);
					fDisplay.addFilter(SWT.MouseMove, this);
					fTextViewer.getTextWidget().addKeyListener(this);
				}
			}
			
			/*
			 * @see org.eclipse.jface.text.AbstractInformationControlManager.IInformationControlCloser#stop()
			 */
			public void stop() {
				if (fDisplay != null && !fDisplay.isDisposed()) {
					fDisplay.removeFilter(SWT.FocusOut, this);
					fDisplay.removeFilter(SWT.MouseMove, this);
					fTextViewer.getTextWidget().removeKeyListener(this);
				}
				
				fSubjectArea= null;
			}
			
			/*
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				switch (event.type) {
					case SWT.FocusOut:
						if (!fControl.isFocusControl())
							disposeInformationControl();
						break;
					case SWT.MouseMove:
						handleMouseMove(event);
						break;
				}
			}
			
			/**
			 * Handle mouse movement events.
			 * 
			 * @param event the event
			 */
			private void handleMouseMove(Event event) {
				if (!(event.widget instanceof Control))
					return;
				
				if (fControl.isFocusControl())
					return;
				
				Control eventControl= (Control) event.widget;
				
				//transform coordinates to subject control:
				Point mouseLoc= event.display.map(eventControl, fSubjectControl, event.x, event.y);
				
				if (fSubjectArea.contains(mouseLoc))
					return;
				
				if (inKeepUpZone(mouseLoc.x, mouseLoc.y, ((IInformationControlExtension3) fControl).getBounds()))
					return;
				
				hideInformationControl();
			}
			
			/**
			 * Tests whether a given mouse location is within the keep-up zone.
			 * The hover should not be hidden as long as the mouse stays inside this zone.
			 * 
			 * @param x the x coordinate, relative to the <em>subject control</em>
			 * @param y the y coordinate, relative to the <em>subject control</em>
			 * @param controlBounds the bounds of the current control
			 * 
			 * @return <code>true</code> iff the mouse event occurred in the keep-up zone
			 */
			private boolean inKeepUpZone(int x, int y, Rectangle controlBounds) {
				Rectangle iControlBounds= fSubjectControl.getDisplay().map(null, fSubjectControl, controlBounds);
				Rectangle totalBounds= Geometry.copy(iControlBounds);
				
				// FIXME: should maybe use convex hull, not bounding box
				totalBounds.add(fSubjectArea);
				return totalBounds.contains(x, y);
			}
			
			/*
			 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
			 */
			public void keyPressed(KeyEvent e) {
			}
			
			/*
			 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
			 */
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN) {
					fControl.setFocus();
					return;
				}
				
				hideInformationControl();
			}
		}
		
		/**
		 * Priority of the hover managed by this manager.
		 * Default value: One higher then for the hovers
		 * managed by TextViewerHoverManager.
		 */
		private static final int WIDGET_TOKEN_PRIORITY= 1;
		
		private final MultipleHyperlinkHover fHover;
		private final ITextViewer fTextViewer;
		private Closer fCloser;
		
		/**
		 * Create a new MultipleHyperlinkHoverManager. The MHHM can show and hide
		 * the given MultipleHyperlinkHover inside the given ITextViewer.
		 * 
		 * @param hover the hover to manage
		 * @param viewer the viewer to show the hover in
		 */
		public MultipleHyperlinkHoverManager(MultipleHyperlinkHover hover, ITextViewer viewer) {
			super(hover.getHoverControlCreator());
			
			fHover= hover;
			fTextViewer= viewer;
			
			fCloser= new Closer();
			setCloser(fCloser);
		}
		
		/*
		 * @see org.eclipse.jface.text.AbstractInformationControlManager#computeInformation()
		 */
		protected void computeInformation() {
			IRegion region= fHover.getHoverRegion(fTextViewer, -1);
			if (region == null) {
				setInformation(null, null);
				return;
			}
			
			Rectangle area= JFaceTextUtil.computeArea(region, fTextViewer);
			if (area == null || area.isEmpty()) {
				setInformation(null, null);
				return;
			}
			
			Object information= fHover.getHoverInfo2(fTextViewer, region);
			setCustomInformationControlCreator(fHover.getHoverControlCreator());
			setInformation(information, area);
		}
		
		/*
		 * @see org.eclipse.jface.text.AbstractInformationControlManager#showInformationControl(org.eclipse.swt.graphics.Rectangle)
		 */
		protected void showInformationControl(Rectangle subjectArea) {
			if (fTextViewer instanceof IWidgetTokenOwnerExtension) {
				if (((IWidgetTokenOwnerExtension) fTextViewer).requestWidgetToken(this, WIDGET_TOKEN_PRIORITY))
					super.showInformationControl(subjectArea);
			} else if (fTextViewer instanceof IWidgetTokenOwner) {
				if (((IWidgetTokenOwner) fTextViewer).requestWidgetToken(this))
					super.showInformationControl(subjectArea);
			} else {
				super.showInformationControl(subjectArea);
			}
		}
		
		/*
		 * @see org.eclipse.jface.text.AbstractInformationControlManager#hideInformationControl()
		 */
		protected void hideInformationControl() {
			super.hideInformationControl();
			
			if (fTextViewer instanceof IWidgetTokenOwner) {
				((IWidgetTokenOwner) fTextViewer).releaseWidgetToken(this);
			}
		}
		
		/*
		 * @see org.eclipse.jface.text.AbstractInformationControlManager#disposeInformationControl()
		 */
		public void disposeInformationControl() {
			super.disposeInformationControl();
			
			if (fTextViewer instanceof IWidgetTokenOwner) {
				((IWidgetTokenOwner) fTextViewer).releaseWidgetToken(this);
			}
		}
		
		/*
		 * @see org.eclipse.jface.text.IWidgetTokenKeeper#requestWidgetToken(org.eclipse.jface.text.IWidgetTokenOwner)
		 */
		public boolean requestWidgetToken(IWidgetTokenOwner owner) {
			hideInformationControl();
			return true;
		}
		
		/*
		 * @see org.eclipse.jface.text.IWidgetTokenKeeperExtension#requestWidgetToken(org.eclipse.jface.text.IWidgetTokenOwner, int)
		 */
		public boolean requestWidgetToken(IWidgetTokenOwner owner, int priority) {
			if (priority < WIDGET_TOKEN_PRIORITY)
				return false;
			
			hideInformationControl();
			return true;
		}
		
		/*
		 * @see org.eclipse.jface.text.IWidgetTokenKeeperExtension#setFocus(org.eclipse.jface.text.IWidgetTokenOwner)
		 */
		public boolean setFocus(IWidgetTokenOwner owner) {
			return false;
		}
	}
	
	private ITextViewer fTextViewer;
	
	private IHyperlink[] fHyperlinks;
	private Region fSubjectRegion;
	private MultipleHyperlinkHoverManager fManager;
	
	/**
	 * Creates a new multiple hyperlink presenter which uses
	 * {@link #HYPERLINK_COLOR} to read the color from the given preference store.
	 *
	 * @param store the preference store
	 */
	public MultipleHyperlinkPresenter(IPreferenceStore store) {
		super(store);
	}
	
	/**
	 * Creates a new multiple hyperlink presenter.
	 *
	 * @param color the hyperlink color, to be disposed by the caller
	 */
	public MultipleHyperlinkPresenter(RGB color) {
		super(color);
	}
	
	/*
	 * @see org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter#install(org.eclipse.jface.text.ITextViewer)
	 */
	public void install(ITextViewer viewer) {
		super.install(viewer);
		fTextViewer= viewer;
		
		fManager= new MultipleHyperlinkHoverManager(new MultipleHyperlinkHover(), fTextViewer);
		fManager.install(viewer.getTextWidget());
		fManager.setSizeConstraints(100, 12, false, true);
	}
	
	/*
	 * @see org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter#uninstall()
	 */
	public void uninstall() {
		super.uninstall();
		
		if (fTextViewer != null) {
			fManager.dispose();
			
			fTextViewer= null;
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter#canShowMultipleHyperlinks()
	 */
	public boolean canShowMultipleHyperlinks() {
		return true;
	}
	
	/*
	 * @see org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter#hideHyperlinks()
	 */
	public void hideHyperlinks() {
		super.hideHyperlinks();
		
		fHyperlinks= null;
	}
	
	/*
	 * @see org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter#showHyperlinks(org.eclipse.jface.text.hyperlink.IHyperlink[])
	 */
	public void showHyperlinks(IHyperlink[] hyperlinks) {
		super.showHyperlinks(new IHyperlink[] { hyperlinks[0] });
		
		if (equals(fHyperlinks, hyperlinks))
			return;
		
		fManager.disposeInformationControl();
		fSubjectRegion= null;
		fHyperlinks= hyperlinks;
		
		if (hyperlinks.length == 1)
			return;
		
		int start= hyperlinks[0].getHyperlinkRegion().getOffset();
		int end= start + hyperlinks[0].getHyperlinkRegion().getLength();
		
		for (int i= 1; i < hyperlinks.length; i++) {
			int hstart= hyperlinks[i].getHyperlinkRegion().getOffset();
			int hend= hstart + hyperlinks[i].getHyperlinkRegion().getLength();
			
			start= Math.min(start, hstart);
			end= Math.max(end, hend);
		}
		
		fSubjectRegion= new Region(start, end - start);
		
		fManager.showInformation();
	}
	
	private boolean equals(IHyperlink[] oldLinks, IHyperlink[] newLinks) {
		if (oldLinks == null)
			return false;
		
		if (oldLinks.length != newLinks.length)
			return false;
		
		for (int i= 0; i < newLinks.length; i++) {
			if (!oldLinks[i].getHyperlinkRegion().equals(newLinks[i].getHyperlinkRegion()))
				return false;
		}
		
		return true;
	}
}
