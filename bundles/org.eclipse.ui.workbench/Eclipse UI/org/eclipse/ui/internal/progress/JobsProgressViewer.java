/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.util.ImageSupport;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * The NewProgressViewer is the viewer for progress using progress monitors.
 */
public class JobsProgressViewer extends StructuredViewer implements
		FinishedJobs.KeptJobsListener {

	class ListLayout extends Layout {
		static final int VERTICAL_SPACING = 1;

		boolean refreshBackgrounds;

		protected Point computeSize(Composite composite, int wHint, int hHint,
				boolean flushCache) {
			int w = 0, h = -VERTICAL_SPACING;
			Control[] cs = composite.getChildren();
			for (int i = 0; i < cs.length; i++) {
				Control c = cs[i];
				Point e = c.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
				w = Math.max(w, e.x);
				h += e.y + VERTICAL_SPACING;
			}
			return new Point(w, h);
		}

		protected void layout(Composite composite, boolean flushCache) {
			int x = 0, y = 0;
			Point e = composite.getSize();
			Control[] cs = getSortedChildren();
			boolean dark = (cs.length % 2) == 1;
			for (int i = 0; i < cs.length; i++) {
				Control c = cs[i];
				Point s = c.computeSize(e.x, SWT.DEFAULT, flushCache);
				c.setBounds(x, y, s.x, s.y);
				y += s.y + VERTICAL_SPACING;
				if (refreshBackgrounds && c instanceof JobItem) {
					((JobItem) c).updateBackground(dark);
					dark = !dark;
				}
			}
		}
	}

	static final boolean DEBUG = false;

	private static String ELLIPSIS = ProgressMessages
			.getString("ProgressFloatingWindow.EllipsisValue"); //$NON-NLS-1$

	static final QualifiedName ICON_PROPERTY = IProgressConstants.ICON_PROPERTY;

	static final boolean isCarbon = "carbon".equals(SWT.getPlatform()); //$NON-NLS-1$

	static final QualifiedName KEEP_PROPERTY = IProgressConstants.KEEP_PROPERTY;

	static final QualifiedName KEEPONE_PROPERTY = IProgressConstants.KEEPONE_PROPERTY;

	/**
	 * Shorten the given text <code>t</code> so that its length doesn't exceed
	 * the width of the given control. This implementation replaces characters
	 * in the center of the original string with an ellipsis ("...").
	 */
	static String shortenText(Control control, String textValue) {
		if (textValue != null) {
			Display display = control.getDisplay();
			GC gc = new GC(display);
			int maxWidth = control.getBounds().width;
			textValue = shortenText(gc, maxWidth, textValue);
			gc.dispose();
		}
		return textValue;
	}

	/**
	 * Shorten the given text <code>t</code> so that its length doesn't exceed
	 * the given width. This implementation replaces characters in the center of
	 * the original string with an ellipsis ("...").
	 */
	static String shortenText(GC gc, int maxWidth, String textValue) {
		if (gc.textExtent(textValue).x < maxWidth) {
			return textValue;
		}
		int length = textValue.length();
		int ellipsisWidth = gc.textExtent(ELLIPSIS).x;
		int pivot = length / 2;
		int start = pivot;
		int end = pivot + 1;
		while (start >= 0 && end < length) {
			String s1 = textValue.substring(0, start);
			String s2 = textValue.substring(end, length);
			int l1 = gc.textExtent(s1).x;
			int l2 = gc.textExtent(s2).x;
			if (l1 + ellipsisWidth + l2 < maxWidth) {
				return s1 + ELLIPSIS + s2;
			}
			start--;
			end++;
		}
		return textValue;
	}

	Font boldFont;

	Image cancelJobIcon;

	Image cancelJobDIcon;

	Image clearJobIcon;

	Image clearJobDIcon;

	Color darkColor;

	Image defaultJobIcon;

	boolean dialogContext; // viewer runs in dialog: filter accordingly

	FinishedJobs finishedJobs;

	// to be disposed
	private Cursor handCursor;

	JobItem highlightItem;

	Job highlightJob;

	Color lightColor;

	private Composite list;

	HashMap map = new HashMap();

	Cursor normalCursor;

	ScrolledComposite scroller;

	Color selectedColor;

	Color selectedTextColor;

	private Font smallerFont;

	Color textColor;

	/**
	 * Create a new ProgressViewer
	 */
	public JobsProgressViewer(Composite parent, int flags) {
		super();

		dialogContext = (flags & SWT.BORDER) != 0; // hack to determine context

		finishedJobs = FinishedJobs.getInstance();

		finishedJobs.addListener(this);

		Display display = parent.getDisplay();
		handCursor = new Cursor(display, SWT.CURSOR_HAND);
		normalCursor = new Cursor(display, SWT.CURSOR_ARROW);

		defaultJobIcon = ImageSupport.getImageDescriptor(
				"icons/full/progress/progress_task.gif").createImage(display); //$NON-NLS-1$
		cancelJobIcon = ImageSupport.getImageDescriptor(
				"icons/full/elcl16/progress_stop.gif").createImage(display); //$NON-NLS-1$
		cancelJobDIcon = ImageSupport.getImageDescriptor(
				"icons/full/dlcl16/progress_stop.gif").createImage(display); //$NON-NLS-1$
		clearJobIcon = ImageSupport.getImageDescriptor(
				"icons/full/elcl16/progress_rem.gif").createImage(display); //$NON-NLS-1$
		clearJobDIcon = ImageSupport.getImageDescriptor(
				"icons/full/dlcl16/progress_rem.gif").createImage(display); //$NON-NLS-1$

		boldFont = getDefaultFont();
		FontData fds[] = getDefaultFontData();
		if (fds.length > 0) {
			FontData fd = fds[0];
			int h = fd.getHeight();
			boldFont = new Font(display, fd.getName(), h, fd.getStyle()
					| SWT.BOLD);
			smallerFont = new Font(display, fd.getName(), h, fd.getStyle());
		}

		int shift = isCarbon ? -25 : -10; // Mac has different Gamma value
		lightColor = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		darkColor = new Color(display,
				Math.max(0, lightColor.getRed() + shift), Math.max(0,
						lightColor.getGreen() + shift), Math.max(0, lightColor
						.getBlue()
						+ shift));
		textColor = display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		selectedTextColor = display
				.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
		selectedColor = display.getSystemColor(SWT.COLOR_LIST_SELECTION);

		scroller = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL
				| flags);
		int height = getDefaultFontData()[0].getHeight();
		scroller.getVerticalBar().setIncrement(height * 2);
		scroller.setExpandHorizontal(true);
		scroller.setExpandVertical(true);

		list = new Composite(scroller, SWT.NONE);
		list.setFont(getDefaultFont());
		list.setBackground(lightColor);
		list.setLayout(new ListLayout());

		list.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				select(null, event); // clear selection
			}
		});

		scroller.addListener(SWT.Dispose, new Listener() {
			public void handleEvent(Event event) {
				finishedJobs.removeListener(JobsProgressViewer.this);

				defaultJobIcon.dispose();
				cancelJobIcon.dispose();
				cancelJobDIcon.dispose();
				clearJobIcon.dispose();
				clearJobDIcon.dispose();

				handCursor.dispose();
				normalCursor.dispose();

				Font defaultFont = getDefaultFont();
				if (boldFont != defaultFont)
					boldFont.dispose();
				if (smallerFont != defaultFont)
					smallerFont.dispose();

				darkColor.dispose();

				JobsProgressViewer.this.handleDispose(null);
			}
		});

		scroller.setContent(list);

		// refresh UI
		refresh(true);
	}

	public void add(Object parentElement, Object[] elements) {
		if (DEBUG)
			System.err.println("add"); //$NON-NLS-1$
		if (list.isDisposed())
			return;
		JobItem lastAdded = null;
		for (int i = 0; i < elements.length; i++)
			if (!filtered(elements[i]))
				lastAdded = findJobItem(elements[i], true);
		relayout(true, true);
		if (lastAdded != null)
			reveal(lastAdded);
	}

	// /////////////////////////////////

	protected void addTreeListener(Control c, TreeListener listener) {
	}

	public void cancelSelection() {
		boolean changed = false;
		Control[] cs = list.getChildren();
		for (int i = 0; i < cs.length; i++) {
			JobItem ji = (JobItem) cs[i];
			if (ji.selected)
				changed |= ji.cancelOrRemove();
		}
		relayout(changed, changed);
	}

	void clearAll() {

		finishedJobs.clearAll();

		if (DEBUG) {
			JobTreeElement[] elements = finishedJobs.getJobInfos();
			System.out.println("jobs: " + elements.length); //$NON-NLS-1$
			for (int i = 0; i < elements.length; i++)
				System.out.println("  " + elements[i]); //$NON-NLS-1$
		}
	}

	Object[] contentProviderGetChildren(Object parent) {
		IContentProvider provider = getContentProvider();
		if (provider instanceof ITreeContentProvider)
			return ((ITreeContentProvider) provider).getChildren(parent);
		return new Object[0];
	}

	Object[] contentProviderGetRoots(Object parent) {
		IContentProvider provider = getContentProvider();
		if (provider instanceof ITreeContentProvider)
			return ((ITreeContentProvider) provider).getElements(parent);
		return new Object[0];
	}

	protected void createChildren(Widget widget) {
		refresh(true);
	}

	JobItem createItem(JobTreeElement element) {
		return new JobItem(this, list, element);
	}

	void doSelection() {
		boolean changed = false;
		Control[] cs = list.getChildren();
		for (int i = 0; i < cs.length; i++) {
			JobItem ji = (JobItem) cs[i];
			if (ji.selected) {
				Control[] children = ji.getChildren();
				for (int j = 0; j < children.length; j++) {
					if (children[j] instanceof HyperLinkItem) {
						HyperLinkItem hl = (HyperLinkItem) children[j];
						if (hl.handleActivate())
							return;
					}
				}
			}
		}
	}

	protected void doUpdateItem(final Item item, Object element) {
	}

	/**
	 * Returns true if given element is filtered (i.e. not shown).
	 */
	private boolean filtered(Object element) {

		if (element == null)
			return true;

		if (!dialogContext && ProgressViewUpdater.getSingleton().debug) // display
																		// all
																		// in
																		// debug
																		// mode
			return false;

		if (element instanceof JobInfo)
			return jobFiltered((JobInfo) element);

		if (element instanceof GroupInfo) {
			Object[] children = ((GroupInfo) element).getChildren();
			for (int i = 0; i < children.length; i++)
				if (jobFiltered((JobInfo) children[i]))
					return true;
		}

		if (element instanceof TaskInfo) {
			Object parent = ((TaskInfo) element).getParent();
			if (parent instanceof JobInfo)
				return jobFiltered((JobInfo) parent);
		}

		return false;
	}

	JobItem findJobItem(Object element, boolean create) {
		JobItem ji = (JobItem) map.get(element);
		if (ji == null && create) {
			JobTreeElement jte = (JobTreeElement) element;
			Object parent = jte.getParent();
			if (parent != null) {
				JobItem parentji = findJobItem(parent, true);
				if (parentji instanceof JobItem && !(jte instanceof TaskInfo)) {
					if (findJobItem(jte, false) == null) {
						JobItem p = (JobItem) parentji;
						p.createChild(jte);
					}
				}
			} else {
				ji = createItem(jte);
			}
		}
		return ji;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.NewKeptJobs.KeptJobsListener#finished(org.eclipse.ui.internal.progress.JobInfo)
	 */
	public void finished(JobTreeElement jte) {
	}

	private void forcedRemove(final JobTreeElement jte) {
		if (list != null && !list.isDisposed()) {
			list.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (DEBUG)
						System.err.println("  forced remove"); //$NON-NLS-1$
					JobItem ji = findJobItem(jte, false);
					if (ji != null && !ji.isDisposed() && ji.remove())
						relayout(true, true);
				}
			});
		}
	}

	/**
	 * Get the color for active hyperlinks.
	 * 
	 * @return Color
	 */
	Color getActiveHyperlinkColor() {
		return JFaceColors.getActiveHyperlinkText(getControl().getDisplay());
	}

	protected Item[] getChildren(Widget o) {
		return new Item[0];
	}

	public Control getControl() {
		return scroller;
	}

	/**
	 * Get the default font to use.
	 * 
	 * @return Font
	 */
	private Font getDefaultFont() {
		return JFaceResources.getDefaultFont();
	}

	/**
	 * Get the default font data for the workbench
	 * 
	 * @return FontData[]
	 */
	private FontData[] getDefaultFontData() {
		return JFaceResources.getDefaultFont().getFontData();
	}

	/**
	 * Get the color for errors.
	 * 
	 * @return Color
	 */
	Color getErrorColor() {
		return JFaceColors.getErrorText(getControl().getDisplay());
	}

	protected boolean getExpanded(Item item) {
		return true;
	}

	String getFinishedString(JobTreeElement jte, String name, boolean withTime) {
		String time = null;
		if (withTime)
			time = getTimeString(jte);
		if (time != null)
			return ProgressMessages.format(
					"JobInfo.FinishedAt", new Object[] { name, time }); //$NON-NLS-1$
		return ProgressMessages.format(
				"JobInfo.Finished", new Object[] { name }); //$NON-NLS-1$
	}

	/**
	 * Get the color for highlight.
	 * 
	 * @return Color
	 */
	Color getHighlightColor() {
		return JFaceColors.getErrorText(getControl().getDisplay());
	}

	/**
	 * Get the color for hyperlinks.
	 * 
	 * @return Color
	 */
	Color getHyperlinkColor() {
		return JFaceColors.getHyperlinkText(getControl().getDisplay());
	}

	protected Item getItem(int x, int y) {
		return null;
	}

	protected int getItemCount(Control widget) {
		return 1;
	}

	protected int getItemCount(Item item) {
		return 0;
	}

	protected Item[] getItems(Item item) {
		return new Item[0];
	}

	String getJobNameAndStatus(JobInfo ji, Job job, boolean terminated,
			boolean withTime) {
		String name = job.getName();

		if (job.isSystem())
			name = ProgressMessages.format(
					"JobInfo.System", new Object[] { name }); //$NON-NLS-1$

		if (ji.isCanceled())
			return ProgressMessages.format(
					"JobInfo.Cancelled", new Object[] { name }); //$NON-NLS-1$

		if (terminated)
			return getFinishedString(ji, name, withTime);

		if (ji.isBlocked()) {
			IStatus blockedStatus = ji.getBlockedStatus();
			return ProgressMessages.format("JobInfo.Blocked", //$NON-NLS-1$
					new Object[] { name, blockedStatus.getMessage() });
		}

		switch (job.getState()) {
		case Job.RUNNING:
			return name;
		case Job.SLEEPING:
			return ProgressMessages.format(
					"JobInfo.Sleeping", new Object[] { name }); //$NON-NLS-1$
		default:
			return ProgressMessages.format(
					"JobInfo.Waiting", new Object[] { name }); //$NON-NLS-1$
		}
	}

	protected Item getParentItem(Item item) {
		return null;
	}

	// //// SelectionProvider

	public ISelection getSelection() {
		if (list.isDisposed())
			return new StructuredSelection();
		ArrayList l = new ArrayList();
		Control[] cs = list.getChildren();
		for (int i = 0; i < cs.length; i++) {
			JobItem ji = (JobItem) cs[i];
			l.add(ji.jobTreeElement);
		}
		return new StructuredSelection(l);
	}

	protected Item[] getSelection(Control widget) {
		return new Item[0];
	}

	Control[] getSortedChildren() {
		Control[] cs = list.getChildren();
		ViewerSorter vs = getSorter();
		if (vs != null) {
			HashMap map2 = new HashMap(); // temp remember items for sorting
			JobTreeElement[] elements = new JobTreeElement[cs.length];
			for (int i = 0; i < cs.length; i++) {
				JobItem ji = (JobItem) cs[i];
				elements[i] = ji.jobTreeElement;
				map2.put(elements[i], ji);
			}
			vs.sort(JobsProgressViewer.this, elements);
			for (int i = 0; i < cs.length; i++)
				cs[i] = (JobItem) map2.get(elements[i]);
		}
		return cs;
	}

	private String getTimeString(JobTreeElement jte) {
		Date date = finishedJobs.getFinishDate(jte);
		if (date != null)
			return DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
		return null;
	}

	protected void internalRefresh(Object element, boolean updateLabels) {
	}

	private boolean jobFiltered(JobInfo ji) {
		Job job = ji.getJob();
		if (job != null && job == highlightJob)
			return false;

		if (job == null || job.getState() == Job.SLEEPING)
			return true;

		if (job.isSystem()) {
			if (getContentProvider() instanceof ProgressContentProvider)
				return ((ProgressContentProvider) getContentProvider()).filterDebug;
			return false;
		}
		return false;
	}

	protected Item newItem(Widget parent, int flags, int ix) {
		return null;
	}

	public void refresh(boolean updateLabels) {
		if (list.isDisposed())
			return;
		if (DEBUG)
			System.err.println("refreshAll"); //$NON-NLS-1$
		boolean changed = false;
		boolean countChanged = false;
		JobItem lastAdded = null;

		Object[] roots = contentProviderGetRoots(getInput());
		HashSet modelJobs = new HashSet();
		for (int z = 0; z < roots.length; z++)
			modelJobs.add(roots[z]);

		// find all removed
		Control[] children = list.getChildren();
		for (int i = 0; i < children.length; i++) {
			JobItem ji = (JobItem) children[i];
			if (modelJobs.contains(ji.jobTreeElement)) {
				if (DEBUG)
					System.err.println("  refresh"); //$NON-NLS-1$
				changed |= ji.refresh();
			} else {
				if (DEBUG)
					System.err.println("  remove: " + ji.jobTreeElement); //$NON-NLS-1$
				if (ji.remove())
					countChanged = changed = true;
			}
		}

		// find all added
		for (int i = 0; i < roots.length; i++) {
			Object element = roots[i];
			if (filtered(element))
				continue;
			if (findJobItem(element, false) == null) {
				if (DEBUG)
					System.err.println("  added"); //$NON-NLS-1$
				lastAdded = createItem((JobTreeElement) element);
				changed = countChanged = true;
			}
		}
		// now add kept finished jobs
		if (!dialogContext) {
			JobTreeElement[] infos = finishedJobs.getJobInfos();
			for (int i = 0; i < infos.length; i++) {
				Object element = infos[i];
				if (filtered(element))
					continue;
				JobItem jte = findJobItem(element, true);
				if (jte != null) {
					jte.setKept();
					lastAdded = jte;

					if (jte instanceof HyperLinkItem) {
						JobItem p = (JobItem) jte.getParent();
						p.setKept();
						lastAdded = p;
					}

					changed = countChanged = true;
				}
			}
		}

		relayout(changed, countChanged);
		if (lastAdded != null)
			reveal(lastAdded);
	}

	public void refresh(Object element, boolean updateLabels) {
		if (list.isDisposed())
			return;
		if (filtered(element))
			return;
		JobItem ji = findJobItem(element, false);
		if (ji == null) { // not found -> add it (workaround for #68151)
			ji = findJobItem(element, true);
			relayout(true, true);
		} else { // just a refresh
			if (ji.refresh())
				relayout(true, true);
		}
	}

	/*
	 * Needs to be called after items have been added or removed, or after the
	 * size of an item has changed. Optionally updates the background of all
	 * items. Ensures that the background following the last item is always
	 * white.
	 */
	void relayout(boolean layout, boolean refreshBackgrounds) {
		if (layout) {
			ListLayout l = (ListLayout) list.getLayout();
			l.refreshBackgrounds = refreshBackgrounds;
			Point size = list.computeSize(list.getClientArea().x, SWT.DEFAULT);
			list.setSize(size);
			scroller.setMinSize(size);
		}
	}

	public void remove(Object[] elements) {
		if (list.isDisposed())
			return;
		if (DEBUG)
			System.err.println("remove"); //$NON-NLS-1$
		boolean changed = false;
		for (int i = 0; i < elements.length; i++) {
			JobItem ji = findJobItem(elements[i], false);
			if (ji != null)
				changed |= ji.remove();
		}
		relayout(changed, changed);
	}

	protected void removeAll(Control widget) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.NewKeptJobs.KeptJobsListener#removed(org.eclipse.ui.internal.progress.JobInfo)
	 */
	public void removed(final JobTreeElement info) {
		if (list != null && !list.isDisposed()) {
			list.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (info != null) { // we got a specific item to remove
						JobItem ji = findJobItem(info, false);
						if (ji != null && ji.jobTerminated) {
							ji.dispose();
							relayout(true, true);
						}
					} else {
						// remove all terminated
						Control[] children = list.getChildren();
						for (int i = 0; i < children.length; i++) {
							JobItem ji = (JobItem) children[i];
							if (ji.jobTerminated)
								ji.dispose();
						}
						relayout(true, true);
					}
				}
			});
		}
	}

	public void reveal(JobItem jti) {
		if (jti != null && !jti.isDisposed()) {

			Rectangle bounds = jti.getBounds();

			int s = bounds.y;
			int e = bounds.y + bounds.height;

			int as = scroller.getOrigin().y;
			int ae = as + scroller.getClientArea().height;

			if (s < as)
				scroller.setOrigin(0, s);
			else if (e > ae)
				scroller.setOrigin(0, as + (e - ae));
		}
	}

	void select(JobItem newSelection, Event e) {

		boolean clearAll = false;
		JobItem newSel = null;
		Control[] cs = getSortedChildren();

		JobTreeElement element = null;
		if (newSelection != null)
			element = newSelection.jobTreeElement;

		if (clearAll) {
			for (int i = 0; i < cs.length; i++) {
				JobItem ji = (JobItem) cs[i];
				ji.selected = ji == newSel;
			}
		}

		boolean dark = (cs.length % 2) == 1;
		for (int i = 0; i < cs.length; i++) {
			JobItem ji = (JobItem) cs[i];
			ji.updateBackground(dark);
			dark = !dark;
		}

		if (newSel != null)
			reveal(newSel);
	}

	protected void setExpanded(Item node, boolean expand) {
	}

	public void setFocus() {
		if (list != null) {
			Control[] cs = list.getChildren();
			for (int i = 0; i < cs.length; i++) {
				JobItem ji = (JobItem) cs[i];
				if (ji.selected) {
					ji.forceFocus();
					return;
				}
			}
			if (cs.length > 0)
				cs[0].forceFocus();
		}
	}

	public void setHighlightJob(Job job) {
		highlightJob = job;
		relayout(true, true);
	}

	public void setInput(IContentProvider provider) {
		refresh(true);
	}

	public void setSelection(ISelection selection) {
	}

	protected void setSelection(List items) {
	}

	public void setUseHashlookup(boolean b) {
	}

	protected void showItem(Item item) {
	}

	String stripPercent(String s) {
		int l = s.length();
		if (l > 0) {
			if (s.charAt(0) == '(') {
				int pos = s.indexOf("%) "); //$NON-NLS-1$
				if (pos >= 0)
					s = s.substring(pos + 3);
			} else if (s.charAt(l - 1) == ')') {
				int pos = s.lastIndexOf(": ("); //$NON-NLS-1$
				if (pos >= 0)
					s = s.substring(0, pos);
			}
		}
		return s;
	}

	protected Widget doFindInputItem(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	protected Widget doFindItem(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
		// TODO Auto-generated method stub

	}

	protected List getSelectionFromWidget() {
		// TODO Auto-generated method stub
		return null;
	}

	protected void internalRefresh(Object element) {
		// TODO Auto-generated method stub

	}

	public void reveal(Object element) {
		// TODO Auto-generated method stub

	}

	protected void setSelectionToWidget(List l, boolean reveal) {
		// TODO Auto-generated method stub

	}

	public void select(JobItem newSelection, KeyEvent e) {
		boolean clearAll = false;
		JobItem newSel = null;
		Control[] cs = getSortedChildren();

		JobTreeElement element = null;
		if (newSelection != null)
			element = newSelection.jobTreeElement;

		// key
		if (e.keyCode == SWT.ARROW_UP) {
			for (int i = 0; i < cs.length; i++) {
				JobItem ji = (JobItem) cs[i];
				if (ji.selected) {
					if (i - 1 >= 0) {
						newSel = (JobItem) cs[i - 1];
						if ((e.stateMask & SWT.MOD2) != 0) {
							newSel.selected = true;
						} else {
							clearAll = true;
						}
						break;
					}
					return;
				}
			}
			if (newSel == null && cs.length > 0) {
				newSel = (JobItem) cs[cs.length - 1];
				newSel.selected = true;
			}
		} else if (e.keyCode == SWT.ARROW_DOWN) {
			for (int i = cs.length - 1; i >= 0; i--) {
				JobItem ji = (JobItem) cs[i];
				if (ji.selected) {
					if (i + 1 < cs.length) {
						newSel = (JobItem) cs[i + 1];
						if ((e.stateMask & SWT.MOD2) != 0) {
							newSel.selected = true;
						} else {
							clearAll = true;
						}
						break;
					}
					return;
				}
			}
			if (newSel == null && cs.length > 0) {
				newSel = (JobItem) cs[0];
				newSel.selected = true;
			}
		}

		if (clearAll) {
			clearAllItems(cs, newSel);
		}
	}

	public void select(JobItem newSelection, MouseEvent event) {
	
		boolean clearAll = false;
		Control[] cs = getSortedChildren();
		JobItem newSel = null;
		
		if (newSelection == null) {
			clearAll = true;
		} else {
			if ((event.stateMask & SWT.MOD1) != 0) {
				newSelection.selected = !newSelection.selected;
			} else if ((event.stateMask & SWT.MOD2) != 0) {
				// not yet implemented
			} else {
				if (newSelection.selected)
					return;
				clearAll = true;
				newSel = newSelection;
			}
		}
		
		if (clearAll) {
			clearAllItems(cs, newSel);
		}
	
	}

	/**
	 * Clear all of the items in the receiver.
	 * @param cs
	 * @param newSel
	 */
	private void clearAllItems(Control[] cs, JobItem newSel) {
		for (int i = 0; i < cs.length; i++) {
			JobItem ji = (JobItem) cs[i];
			ji.selected = ji == newSel;
		}
	}

	public Font getSmallerFont() {
		return smallerFont;
	}

	public Color getLightColor() {
		return lightColor;
	}
	

	public Color getSelectedColor() {
		return selectedColor;
	}
	

	public Color getSelectedTextColor() {
		return selectedTextColor;
	}
	

	public Color getTextColor() {
		return textColor;
	}

	public Cursor getHandCursor() {
		return handCursor;
	}
	
	
	
}