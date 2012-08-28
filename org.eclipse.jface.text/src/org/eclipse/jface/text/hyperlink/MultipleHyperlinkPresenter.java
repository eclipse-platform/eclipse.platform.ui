/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.hyperlink;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
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
import org.eclipse.jface.text.ITextHoverExtension2;
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
public class MultipleHyperlinkPresenter extends DefaultHyperlinkPresenter implements IHyperlinkPresenterExtension2 {

	private static final boolean IS_OLD_WINDOWS;
	static {
		int majorVersion= Integer.MAX_VALUE;
		if (Util.isWin32()) {
			String osVersion= System.getProperty("os.version"); //$NON-NLS-1$
			if (osVersion != null) {
				int majorIndex = osVersion.indexOf('.');
				if (majorIndex != -1) {
					osVersion = osVersion.substring(0, majorIndex);
					try {
						majorVersion= Integer.parseInt(osVersion);
					} catch (NumberFormatException exception) {
						// use default
					}
				}
			}
		}
		IS_OLD_WINDOWS= majorVersion < 6; // before Vista (6.0)
	}
	private static final boolean IS_MAC= Util.isMac();
	private static final boolean IS_GTK= Util.isGtk();

	/**
	 * An information control capable of showing a list of hyperlinks. The hyperlinks can be opened.
	 */
	private static class LinkListInformationControl extends AbstractInformationControl implements IInformationControlExtension2 {

		private static final class LinkContentProvider implements IStructuredContentProvider {

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
			/*
			 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				IHyperlink link= (IHyperlink)element;
				String text= link.getHyperlinkText();
				if (text != null)
					return text;
				return HyperlinkMessages.getString("LinkListInformationControl.unknownLink"); //$NON-NLS-1$
			}
		}

		private final MultipleHyperlinkHoverManager fManager;

		private IHyperlink[] fInput;
		private Composite fParent;
		private Table fTable;

		private final Color fForegroundColor;
		private final Color fBackgroundColor;


		/**
		 * Creates a link list information control with the given shell as parent.
		 *
		 * @param parentShell the parent shell
		 * @param manager the hover manager
		 * @param foregroundColor the foreground color, must not be disposed
		 * @param backgroundColor the background color, must not be disposed
		 */
		public LinkListInformationControl(Shell parentShell, MultipleHyperlinkHoverManager manager, Color foregroundColor, Color backgroundColor) {
			super(parentShell, false);
			fManager= manager;
			fForegroundColor= foregroundColor;
			fBackgroundColor= backgroundColor;
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
			fParent= parent;
			GridLayout layout= new GridLayout();
			
			if (IS_OLD_WINDOWS) {
				layout.marginWidth= 0;
				layout.marginHeight= 4;
				layout.marginRight= 4;
			} else if (IS_MAC) {
				layout.marginWidth= 4;
				layout.marginHeight= 0;
				layout.marginTop= 4;
				layout.marginBottom= 4 - 1;
			} else if (IS_GTK) {
				layout.marginWidth= 4;
				layout.marginHeight= 0;
				layout.marginTop= 4;
				layout.marginBottom= 4 - 2;
			} else {
				layout.marginWidth= 4;
				layout.marginHeight= 4;
			}
			
			fParent.setLayout(layout);
			fParent.setForeground(fForegroundColor);
			fParent.setBackground(fBackgroundColor);
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

			if (IS_MAC && fTable.getScrollbarsMode() == SWT.SCROLLBAR_OVERLAY) {
				// workaround for https://bugs.eclipse.org/387732 : [10.8] Table scrollbar width is 16 (not 15) on Mountain Lion
				scrollBarWidth--;
				scrollBarHeight--;
			}
			
			int width;
			if (preferedSize.y - scrollBarHeight <= constraints.y) {
				width= preferedSize.x - scrollBarWidth;
				fTable.getVerticalBar().setVisible(false);
			} else {
				width= Math.min(preferedSize.x, constraints.x);
			}

			int height;
			if (preferedSize.x - scrollBarWidth <= constraints.x) {
				height= preferedSize.y - scrollBarHeight;
				fTable.getHorizontalBar().setVisible(false);
			} else {
				height= Math.min(preferedSize.y, constraints.y);
			}

			return new Point(width, height);
		}

		private void deferredCreateContent(Composite parent) {
			fTable= new Table(parent, SWT.SINGLE | SWT.FULL_SELECTION);
			fTable.setLinesVisible(false);
			fTable.setHeaderVisible(false);
			fTable.setForeground(fForegroundColor);
			fTable.setBackground(fBackgroundColor);
			fTable.setFont(JFaceResources.getDialogFont());

			GridData data= new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
			fTable.setLayoutData(data);

			final TableViewer viewer= new TableViewer(fTable);
			viewer.setContentProvider(new LinkContentProvider());
			viewer.setLabelProvider(new LinkLabelProvider());
			viewer.setInput(fInput);
			fTable.setSelection(0);

			registerTableListeners();

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

		private void registerTableListeners() {

			fTable.addMouseMoveListener(new MouseMoveListener() {
				TableItem fLastItem= null;

				public void mouseMove(MouseEvent e) {
					if (fTable.equals(e.getSource())) {
						Object o= fTable.getItem(new Point(e.x, e.y));
						if (fLastItem == null ^ o == null) {
							fTable.setCursor(o == null ? null : fTable.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
						}
						if (o instanceof TableItem) {
							TableItem item= (TableItem) o;
							if (!o.equals(fLastItem)) {
								fLastItem= (TableItem) o;
								fTable.setSelection(new TableItem[] { fLastItem });
							} else if (e.y < fTable.getItemHeight() / 4) {
								// Scroll up
								int index= fTable.indexOf(item);
								if (index > 0) {
									fLastItem= fTable.getItem(index - 1);
									fTable.setSelection(new TableItem[] { fLastItem });
								}
							} else if (e.y > fTable.getBounds().height - fTable.getItemHeight() / 4) {
								// Scroll down
								int index= fTable.indexOf(item);
								if (index < fTable.getItemCount() - 1) {
									fLastItem= fTable.getItem(index + 1);
									fTable.setSelection(new TableItem[] { fLastItem });
								}
							}
						} else if (o == null) {
							fLastItem= null;
						}
					}
				}
			});

			fTable.addSelectionListener(new SelectionAdapter() {
				public void widgetDefaultSelected(SelectionEvent e) {
					openSelectedLink();
				}
			});

			fTable.addMouseListener(new MouseAdapter() {
				public void mouseUp(MouseEvent e) {
					if (fTable.getSelectionCount() < 1)
						return;

					if (e.button != 1)
						return;

					if (fTable.equals(e.getSource())) {
						Object o= fTable.getItem(new Point(e.x, e.y));
						TableItem selection= fTable.getSelection()[0];
						if (selection.equals(o))
							openSelectedLink();
					}
				}
			});

			fTable.addTraverseListener(new TraverseListener() {
				public void keyTraversed(TraverseEvent e) {
					if (e.keyCode == SWT.ESC) {
						fManager.hideInformationControl();
					}
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
		 * Opens the currently selected link.
		 */
		private void openSelectedLink() {
			if (fTable.getSelectionCount() < 1)
				return;
			
			TableItem selection= fTable.getSelection()[0];
			IHyperlink link= (IHyperlink)selection.getData();
			fManager.hideInformationControl();
			fManager.setCaret();
			link.open();
		}
	}

	private class MultipleHyperlinkHover implements ITextHover, ITextHoverExtension, ITextHoverExtension2 {

		/**
		 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
		 * @deprecated As of 3.4, replaced by
		 *             {@link ITextHoverExtension2#getHoverInfo2(ITextViewer, IRegion)}
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
					Color foregroundColor= fTextViewer.getTextWidget().getForeground();
					Color backgroundColor= fTextViewer.getTextWidget().getBackground();
					return new LinkListInformationControl(parent, fManager, foregroundColor, backgroundColor);
				}
			};
		}
	}

	private static class MultipleHyperlinkHoverManager extends AbstractInformationControlManager implements IWidgetTokenKeeper, IWidgetTokenKeeperExtension {

		private class Closer implements IInformationControlCloser, Listener, KeyListener, MouseListener {

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
					fTextViewer.getTextWidget().addMouseListener(this);
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
					fTextViewer.getTextWidget().removeMouseListener(this);
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

				if (!isTakingFocusWhenVisible())
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
				//  +-----------+
				//  |subjectArea|
				//  +-----------+
				//  |also keepUp|
				// ++-----------+-------+
				// | totalBounds        |
				// +--------------------+
				if (fSubjectArea.contains(x, y))
					return true;

				Rectangle iControlBounds= fSubjectControl.getDisplay().map(null, fSubjectControl, controlBounds);
				Rectangle totalBounds= Geometry.copy(iControlBounds);
				if (totalBounds.contains(x, y))
					return true;

				int keepUpY= fSubjectArea.y + fSubjectArea.height;
				Rectangle alsoKeepUp= new Rectangle(fSubjectArea.x, keepUpY, fSubjectArea.width, totalBounds.y - keepUpY);
				return alsoKeepUp.contains(x, y);
			}

			/*
			 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
			 */
			public void keyPressed(KeyEvent e) {
				hideInformationControl();
			}

			/*
			 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
			 */
			public void keyReleased(KeyEvent e) {
				if (!isTakingFocusWhenVisible())
					hideInformationControl();
			}

			/*
			 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
			 * @since 3.5
			 */
			public void mouseDoubleClick(MouseEvent e) {
			}

			/*
			 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
			 * @since 3.5
			 */
			public void mouseDown(MouseEvent e) {
			}

			/*
			 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
			 * @since 3.5
			 */
			public void mouseUp(MouseEvent e) {
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
		private final MultipleHyperlinkPresenter fHyperlinkPresenter;
		private final Closer fCloser;
		private boolean fIsControlVisible;


		/**
		 * Create a new MultipleHyperlinkHoverManager. The MHHM can show and hide
		 * the given MultipleHyperlinkHover inside the given ITextViewer.
		 *
		 * @param hover the hover to manage
		 * @param viewer the viewer to show the hover in
		 * @param hyperlinkPresenter the hyperlink presenter using this manager to present hyperlinks
		 */
		public MultipleHyperlinkHoverManager(MultipleHyperlinkHover hover, ITextViewer viewer, MultipleHyperlinkPresenter hyperlinkPresenter) {
			super(hover.getHoverControlCreator());

			fHover= hover;
			fTextViewer= viewer;
			fHyperlinkPresenter= hyperlinkPresenter;

			fCloser= new Closer();
			setCloser(fCloser);
			fIsControlVisible= false;
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
		 * @see org.eclipse.jface.text.AbstractInformationControlManager#computeInformationControlLocation(org.eclipse.swt.graphics.Rectangle, org.eclipse.swt.graphics.Point)
		 */
		protected Point computeInformationControlLocation(Rectangle subjectArea, Point controlSize) {
			Point result= super.computeInformationControlLocation(subjectArea, controlSize);

			Point cursorLocation= fTextViewer.getTextWidget().getDisplay().getCursorLocation();
			if (isTakingFocusWhenVisible() || cursorLocation.x <= result.x + controlSize.x)
				return result;

			result.x= cursorLocation.x + 20 - controlSize.x;
			return result;
		}

		/*
		 * @see org.eclipse.jface.text.AbstractInformationControlManager#showInformationControl(org.eclipse.swt.graphics.Rectangle)
		 */
		protected void showInformationControl(Rectangle subjectArea) {
			if (fTextViewer instanceof IWidgetTokenOwnerExtension) {
				if (((IWidgetTokenOwnerExtension)fTextViewer).requestWidgetToken(this, WIDGET_TOKEN_PRIORITY)) {
					super.showInformationControl(subjectArea);
					fIsControlVisible= true;
				}
			} else if (fTextViewer instanceof IWidgetTokenOwner) {
				if (((IWidgetTokenOwner)fTextViewer).requestWidgetToken(this)) {
					super.showInformationControl(subjectArea);
					fIsControlVisible= true;
				}
			} else {
				super.showInformationControl(subjectArea);
				fIsControlVisible= true;
			}
		}

		/**
		 * Sets the caret where hyperlinking got initiated.
		 * 
		 * @since 3.5
		 */
		private void setCaret() {
			fHyperlinkPresenter.setCaret();
		}

		/*
		 * @see org.eclipse.jface.text.AbstractInformationControlManager#hideInformationControl()
		 */
		protected void hideInformationControl() {
			super.hideInformationControl();

			if (fTextViewer instanceof IWidgetTokenOwner) {
				((IWidgetTokenOwner) fTextViewer).releaseWidgetToken(this);
			}

			fIsControlVisible= false;
			fHyperlinkPresenter.hideHyperlinks();
		}

		/*
		 * @see org.eclipse.jface.text.AbstractInformationControlManager#disposeInformationControl()
		 */
		public void disposeInformationControl() {
			super.disposeInformationControl();

			if (fTextViewer instanceof IWidgetTokenOwner) {
				((IWidgetTokenOwner) fTextViewer).releaseWidgetToken(this);
			}

			fIsControlVisible= false;
			fHyperlinkPresenter.hideHyperlinks();
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
			return isTakingFocusWhenVisible();
		}

		/**
		 * Returns <code>true</code> if the information control managed by
		 * this manager is visible, <code>false</code> otherwise.
		 *
		 * @return <code>true</code> if information control is visible
		 */
		public boolean isInformationControlVisible() {
			return fIsControlVisible;
		}
	}

	private ITextViewer fTextViewer;

	private IHyperlink[] fHyperlinks;
	private Region fSubjectRegion;
	private MultipleHyperlinkHoverManager fManager;

	/**
	 * The offset in the text viewer where hyperlinking got initiated.
	 * @since 3.5
	 */
	private int fCursorOffset;

	/**
	 * Creates a new multiple hyperlink presenter which uses {@link #HYPERLINK_COLOR} to read the
	 * color from the given preference store.
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

		fManager= new MultipleHyperlinkHoverManager(new MultipleHyperlinkHover(), fTextViewer, this);
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
	 * @see org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter#canHideHyperlinks()
	 */
	public boolean canHideHyperlinks() {
		return !fManager.isInformationControlVisible();
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
		showHyperlinks(hyperlinks, false);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @since 3.7
	 */
	public void showHyperlinks(IHyperlink[] activeHyperlinks, boolean takesFocusWhenVisible) {
		fManager.takesFocusWhenVisible(takesFocusWhenVisible);
		super.showHyperlinks(new IHyperlink[] { activeHyperlinks[0] });

		fSubjectRegion= null;
		fHyperlinks= activeHyperlinks;

		if (activeHyperlinks.length == 1)
			return;

		int start= activeHyperlinks[0].getHyperlinkRegion().getOffset();
		int end= start + activeHyperlinks[0].getHyperlinkRegion().getLength();

		for (int i= 1; i < activeHyperlinks.length; i++) {
			int hstart= activeHyperlinks[i].getHyperlinkRegion().getOffset();
			int hend= hstart + activeHyperlinks[i].getHyperlinkRegion().getLength();

			start= Math.min(start, hstart);
			end= Math.max(end, hend);
		}

		fSubjectRegion= new Region(start, end - start);
		fCursorOffset= JFaceTextUtil.getOffsetForCursorLocation(fTextViewer);

		fManager.showInformation();
	}

	/**
	 * Sets the caret where hyperlinking got initiated.
	 * 
	 * @since 3.5
	 */
	private void setCaret() {
		Point selectedRange= fTextViewer.getSelectedRange();
		if (fCursorOffset != -1 && !(fSubjectRegion.getOffset() <= selectedRange.x && selectedRange.x + selectedRange.y <= fSubjectRegion.getOffset() + fSubjectRegion.getLength()))
			fTextViewer.setSelectedRange(fCursorOffset, 0);
	}

}
