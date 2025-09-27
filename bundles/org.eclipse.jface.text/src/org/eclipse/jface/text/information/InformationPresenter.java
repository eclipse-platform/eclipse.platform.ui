/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.information;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.util.Geometry;

import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.IWidgetTokenKeeperExtension;
import org.eclipse.jface.text.IWidgetTokenOwner;
import org.eclipse.jface.text.IWidgetTokenOwnerExtension;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;


/**
 * Standard implementation of <code>IInformationPresenter</code>.
 * This implementation extends <code>AbstractInformationControlManager</code>.
 * The information control is made visible on request by calling
 * {@link #showInformationControl(Rectangle)}.
 * <p>
 * Usually, clients instantiate this class and configure it before using it. The configuration
 * must be consistent: This means the used {@link org.eclipse.jface.text.IInformationControlCreator}
 * must create an information control expecting information in the same format the configured
 * {@link org.eclipse.jface.text.information.IInformationProvider}s  use to encode the information they provide.
 * </p>
 *
 * @since 2.0
 */
public class InformationPresenter extends AbstractInformationControlManager implements IInformationPresenter, IInformationPresenterExtension, IWidgetTokenKeeper, IWidgetTokenKeeperExtension {


	/**
	 * Priority of the info controls managed by this information presenter.
	 * Default value: <code>5</code>.
	 *
	 * @since 3.0
	 */
	/*
	 * 5 as value has been chosen in order to beat the hovers of {@link org.eclipse.jface.text.TextViewerHoverManager}
	 */
	public static final int WIDGET_PRIORITY= 5;


	/**
	 * Internal information control closer. Listens to several events issued by its subject control
	 * and closes the information control when necessary.
	 */
	class Closer implements IInformationControlCloser, ControlListener, MouseListener, FocusListener, IViewportListener, KeyListener {

		/** The subject control. */
		private Control fSubjectControl;
		/** The information control. */
		private IInformationControl fInformationControlToClose;
		/** Indicates whether this closer is active. */
		private boolean fIsActive= false;

		@Override
		public void setSubjectControl(Control control) {
			fSubjectControl= control;
		}

		@Override
		public void setInformationControl(IInformationControl control) {
			fInformationControlToClose= control;
		}

		@Override
		public void start(Rectangle informationArea) {

			if (fIsActive)
				return;
			fIsActive= true;

			if (fSubjectControl != null && !fSubjectControl.isDisposed()) {
				fSubjectControl.addControlListener(this);
				fSubjectControl.addMouseListener(this);
				fSubjectControl.addFocusListener(this);
				fSubjectControl.addKeyListener(this);
			}

			if (fInformationControlToClose != null)
				fInformationControlToClose.addFocusListener(this);

			fTextViewer.addViewportListener(this);
		}

		@Override
		public void stop() {

			if (!fIsActive)
				return;
			fIsActive= false;

			fTextViewer.removeViewportListener(this);

			if (fInformationControlToClose != null)
				fInformationControlToClose.removeFocusListener(this);

			if (fSubjectControl != null && !fSubjectControl.isDisposed()) {
				fSubjectControl.removeControlListener(this);
				fSubjectControl.removeMouseListener(this);
				fSubjectControl.removeFocusListener(this);
				fSubjectControl.removeKeyListener(this);
			}
		}

		 @Override
		public void controlResized(ControlEvent e) {
			 hideInformationControl();
		}

		 @Override
		public void controlMoved(ControlEvent e) {
			 hideInformationControl();
		}

		 @Override
		public void mouseDown(MouseEvent e) {
			 hideInformationControl();
		}

		@Override
		public void mouseUp(MouseEvent e) {
		}

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			hideInformationControl();
		}

		@Override
		public void focusGained(FocusEvent e) {
		}

		 @Override
		public void focusLost(FocusEvent e) {
			Display d= fSubjectControl.getDisplay();
			// Without the asyncExec, mouse clicks to the workbench window are swallowed.
			d.asyncExec(() -> {
				if (fInformationControlToClose == null || !fInformationControlToClose.isFocusControl())
					hideInformationControl();
			});
		}

		@Override
		public void viewportChanged(int topIndex) {
			hideInformationControl();
		}

		@Override
		public void keyPressed(KeyEvent e) {
			hideInformationControl();
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}
	}


	/** The text viewer this information presenter works on */
	private ITextViewer fTextViewer;
	/** The map of <code>IInformationProvider</code> objects */
	private Map<String, IInformationProvider> fProviders;
	/** The offset to override selection. */
	private int fOffset= -1;
	/**
	 * The document partitioning for this information presenter.
	 * @since 3.0
	 */
	private String fPartitioning;

	/**
	 * Creates a new information presenter that uses the given information control creator.
	 * The presenter is not installed on any text viewer yet. By default, an information
	 * control closer is set that closes the information control in the event of key strokes,
	 * resizing, moves, focus changes, mouse clicks, and disposal - all of those applied to
	 * the information control's parent control. Also, the setup ensures that the information
	 * control when made visible will request the focus. By default, the default document
	 * partitioning {@link IDocumentExtension3#DEFAULT_PARTITIONING} is used.
	 *
	 * @param creator the information control creator to be used
	 */
	public InformationPresenter(IInformationControlCreator creator) {
		super(creator);
		setCloser(new Closer());
		takesFocusWhenVisible(true);
		fPartitioning= IDocumentExtension3.DEFAULT_PARTITIONING;
	}

	/**
	 * Sets the document partitioning to be used by this information presenter.
	 *
	 * @param partitioning the document partitioning to be used by this information presenter
	 * @since 3.0
	 */
	public void setDocumentPartitioning(String partitioning) {
		Assert.isNotNull(partitioning);
		fPartitioning= partitioning;
	}

	@Override
	public String getDocumentPartitioning() {
		return fPartitioning;
	}

	/**
	 * Registers a given information provider for a particular content type.
	 * If there is already a provider registered for this type, the new provider
	 * is registered instead of the old one.
	 *
	 * @param provider the information provider to register, or <code>null</code> to remove an existing one
	 * @param contentType the content type under which to register
	 */
	 public void setInformationProvider(IInformationProvider provider, String contentType) {

		Assert.isNotNull(contentType);

		if (fProviders == null)
			fProviders= new HashMap<>();

		if (provider == null)
			fProviders.remove(contentType);
		else
			fProviders.put(contentType, provider);
	}

	@Override
	public IInformationProvider getInformationProvider(String contentType) {
		if (fProviders == null)
			return null;

		return fProviders.get(contentType);
	}

	/**
	 * Sets a offset to override the selection. Setting the value to <code>-1</code> will disable
	 * overriding.
	 *
	 * @param offset the offset to override selection or <code>-1</code>
	 */
	public void setOffset(int offset) {
		fOffset= offset;
	}

	@Override
	protected void computeInformation() {

		int offset= fOffset < 0 ? fTextViewer.getSelectedRange().x : fOffset;
		if (offset == -1)
			return;

		fOffset= -1;

		IInformationProvider provider= null;
		try {
			String contentType= TextUtilities.getContentType(fTextViewer.getDocument(), getDocumentPartitioning(), offset, true);
			provider= getInformationProvider(contentType);
		} catch (BadLocationException x) {
		}
		if (provider == null)
			return;

		IRegion subject= provider.getSubject(fTextViewer, offset);
		if (subject == null)
			return;

		Object info;
		if (provider instanceof IInformationProviderExtension extension) {
			info= extension.getInformation2(fTextViewer, subject);
		} else {
			// backward compatibility code
			info= provider.getInformation(fTextViewer, subject);
		}

		if (provider instanceof IInformationProviderExtension2)
			setCustomInformationControlCreator(((IInformationProviderExtension2) provider).getInformationPresenterControlCreator());
		else
			setCustomInformationControlCreator(null);

		setInformation(info, computeArea(subject));
	}

	/**
	 * Determines the graphical area covered by the given text region.
	 *
	 * @param region the region whose graphical extend must be computed
	 * @return the graphical extend of the given region
	 */
	private Rectangle computeArea(IRegion region) {

		int start= 0;
		int end= 0;

		IRegion widgetRegion= modelRange2WidgetRange(region);
		if (widgetRegion != null) {
			start= widgetRegion.getOffset();
			end= widgetRegion.getOffset() + widgetRegion.getLength();
		}

		StyledText styledText= fTextViewer.getTextWidget();
		Rectangle bounds;
		if (end > 0 && start < end)
			bounds= styledText.getTextBounds(start, end - 1);
		else {
			Point loc= styledText.getLocationAtOffset(start);
			bounds= new Rectangle(loc.x, loc.y, 0, styledText.getLineHeight(start));
		}

		Rectangle clientArea= styledText.getClientArea();
		Geometry.moveInside(bounds, clientArea);
		return bounds;
	}

	/**
	 * Translated the given range in the viewer's document into the corresponding
	 * range of the viewer's widget.
	 *
	 * @param region the range in the viewer's document
	 * @return the corresponding widget range
	 * @since 2.1
	 */
	private IRegion modelRange2WidgetRange(IRegion region) {
		if (fTextViewer instanceof ITextViewerExtension5 extension) {
			return extension.modelRange2WidgetRange(region);
		}

		IRegion visibleRegion= fTextViewer.getVisibleRegion();
		int start= region.getOffset() - visibleRegion.getOffset();
		int end= start + region.getLength();
		if (end > visibleRegion.getLength())
			end= visibleRegion.getLength();

		return new Region(start, end - start);
	}

	@Override
	public void install(ITextViewer textViewer) {
		fTextViewer= textViewer;
		install(fTextViewer.getTextWidget());
	}

	@Override
	public void uninstall() {
		dispose();
	}

	@Override
	protected void showInformationControl(Rectangle subjectArea) {
		if (fTextViewer instanceof IWidgetTokenOwnerExtension extension && fTextViewer instanceof IWidgetTokenOwner) {
			if (extension.requestWidgetToken(this, WIDGET_PRIORITY))
				super.showInformationControl(subjectArea);
		} else if (fTextViewer instanceof IWidgetTokenOwner owner) {
			if (owner.requestWidgetToken(this))
				super.showInformationControl(subjectArea);

		} else
			super.showInformationControl(subjectArea);
	}

	@Override
	protected void hideInformationControl() {
		try {
			super.hideInformationControl();
		} finally {
			if (fTextViewer instanceof IWidgetTokenOwner owner) {
				owner.releaseWidgetToken(this);
			}
		}
	}

	@Override
	protected void handleInformationControlDisposed() {
		try {
			super.handleInformationControlDisposed();
		} finally {
			if (fTextViewer instanceof IWidgetTokenOwner owner) {
				owner.releaseWidgetToken(this);
			}
		}
	}

	@Override
	public boolean requestWidgetToken(IWidgetTokenOwner owner) {
		return false;
	}

	@Override
	public boolean requestWidgetToken(IWidgetTokenOwner owner, int priority) {
		return false;
	}

	@Override
	public boolean setFocus(IWidgetTokenOwner owner) {
		return false;
	}
}

