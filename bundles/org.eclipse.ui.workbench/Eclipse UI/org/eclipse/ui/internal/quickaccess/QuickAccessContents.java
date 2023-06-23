/*******************************************************************************
 * Copyright (c) 2005, 2020 IBM Corporation and others.
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
 *     Tom Hochstein (Freescale) - Bug 393703 - NotHandledException selecting inactive command under 'Previous Choices' in Quick access
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654, 491272, 491398
 *     Leung Wang Hei <gemaspecial@yahoo.com.hk> - Bug 483343
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 491291, 491529, 491293, 492434, 492452, 459989, 507322
 *******************************************************************************/
package org.eclipse.ui.internal.quickaccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.quickaccess.QuickAccessElement;
import org.eclipse.ui.themes.ColorUtil;

/**
 * Provides the contents for the quick access shell created by
 * {@link SearchField}. This was also used by {@link QuickAccessDialog} prior to
 * e4. The SearchField is responsible for handling opening and closing the shell
 * as well as setting {@link #setShowAllMatches(boolean)}.
 */
public abstract class QuickAccessContents {
	/**
	 * When opened in a popup we were given the command used to open it. Now that we
	 * have a shell, we are just using a hard coded command id.
	 */
	private static final String QUICK_ACCESS_COMMAND_ID = "org.eclipse.ui.window.quickAccess"; //$NON-NLS-1$

	protected Text filterText;

	private QuickAccessProvider[] providers;
	private Map<String, QuickAccessProvider> providerMap = new HashMap<>();
	private Map<QuickAccessElement, QuickAccessProvider> elementsToProviders = new HashMap<>();

	protected Table table;
	protected Label infoLabel;

	private LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

	protected String rememberedText;

	/**
	 * A color for dulled out items created by mixing the table foreground. Will be
	 * disposed when the {@link #resourceManager} is disposed.
	 */
	private Color grayColor;
	private TextLayout textLayout;
	private boolean showAllMatches = false;
	protected boolean resized = false;
	private TriggerSequence keySequence;
	private Job computeProposalsJob;

	public QuickAccessContents(QuickAccessProvider[] providers) {
		this.providers = providers;
	}

	/**
	 * Returns the number of items the table can fit in its current layout
	 */
	private int computeNumberOfItems() {
		Rectangle rect = table.getClientArea();
		int itemHeight = table.getItemHeight();
		int headerHeight = table.getHeaderHeight();
		return (rect.height - headerHeight + itemHeight - 1) / (itemHeight + table.getGridLineWidth());
	}

	/**
	 * Refreshes the contents of the quick access shell
	 *
	 * @param filter The filter text to apply to results
	 *
	 */
	public void updateProposals(String filter) {
		if (computeProposalsJob != null) {
			computeProposalsJob.cancel();
			computeProposalsJob = null;
		}
		if (table == null || table.isDisposed()) {
			return;
		}
		final Display display = table.getDisplay();

		// perfect match, to be selected in the table if not null
		QuickAccessElement perfectMatch = getPerfectMatch(filter);

		String computingMessage = NLS.bind(QuickAccessMessages.QuickaAcessContents_computeMatchingEntries, filter);
		int maxNumberOfItemsInTable = computeNumberOfItems();
		AtomicReference<List<QuickAccessEntry>[]> entries = new AtomicReference<>();
		final Job currentComputeEntriesJob = Job.create(computingMessage, theMonitor -> {
			entries.set(
					computeMatchingEntries(filter, perfectMatch, maxNumberOfItemsInTable, theMonitor));
			return theMonitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
		});
		currentComputeEntriesJob.setPriority(Job.INTERACTIVE);
		// feedback is delayed in a job as we don't want to show it on every keystroke
		// but only when user seems to be waiting
		UIJob computingFeedbackJob = new UIJob(table.getDisplay(), QuickAccessMessages.QuickAccessContents_computeMatchingEntries_displayFeedback_jobName) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (currentComputeEntriesJob.getResult() == null && !monitor.isCanceled() && !table.isDisposed()) {
					showHintText(computingMessage, grayColor);
					return Status.OK_STATUS;
				}
				return Status.CANCEL_STATUS;
			}
		};
		currentComputeEntriesJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				computingFeedbackJob.cancel();
				if (computeProposalsJob == currentComputeEntriesJob && event.getResult().isOK()
						&& !table.isDisposed()) {
					display.asyncExec(() -> {
						computingFeedbackJob.cancel();
						refreshTable(perfectMatch, entries.get(), filter);
					});
				}
			}
		});
		this.computeProposalsJob = currentComputeEntriesJob;
		currentComputeEntriesJob.schedule();
		computingFeedbackJob.schedule(200); // delay a bit so if proposals compute fast enough, we don't show feedback
	}

	/**
	 * Allows the quick access content owner to mark a quick access element as being
	 * a perfect match, putting it at the start of the table.
	 *
	 * @param filter the filter text used to find a match
	 * @return an element to be put at the top of the table or <code>null</code>
	 */
	protected abstract QuickAccessElement getPerfectMatch(String filter);

	/**
	 * Notifies the quick access content owner that the contents of the table have
	 * been changed.
	 *
	 * @param filterTextEmpty whether the filter text used to calculate matches was
	 *                        empty
	 * @param showAllMatches  whether the results were constrained by the size of
	 *                        the dialog
	 *
	 */
	protected abstract void updateFeedback(boolean filterTextEmpty, boolean showAllMatches);

	/**
	 * Sets whether to display all matches to the current filter or limit the
	 * results. Will refresh the table contents and update the info label.
	 *
	 * @param showAll whether to display all matches
	 */
	public void setShowAllMatches(boolean showAll) {
		if (showAllMatches != showAll) {
			showAllMatches = showAll;
			updateInfoLabel();
			updateProposals(filterText.getText().toLowerCase());
		}
	}

	private void updateInfoLabel() {
		if (infoLabel != null) {
			TriggerSequence sequence = getTriggerSequence();
			boolean forceHide = (getNumberOfFilteredResults() == 0)
					|| (showAllMatches && (table.getItemCount() <= computeNumberOfItems()));
			if (sequence == null || forceHide) {
				infoLabel.setText(""); //$NON-NLS-1$
			} else if (showAllMatches) {
				infoLabel.setText(
						NLS.bind(QuickAccessMessages.QuickAccessContents_PressKeyToLimitResults, sequence.format()));
			} else {
				infoLabel
						.setText(NLS.bind(QuickAccessMessages.QuickAccess_PressKeyToShowAllMatches, sequence.format()));
			}
			infoLabel.getParent().layout(true);
		}
	}

	/**
	 * Returns the trigger sequence that can be used to open the quick access dialog
	 * as well as toggle the show all results feature. Can return <code>null</code>
	 * if no trigger sequence is known.
	 *
	 * @return the trigger sequence used to open the quick access or
	 *         <code>null</code>
	 */
	public TriggerSequence getTriggerSequence() {
		if (keySequence == null) {
			IBindingService bindingService = Adapters.adapt(PlatformUI.getWorkbench(), IBindingService.class);
			keySequence = bindingService.getBestActiveBindingFor(QUICK_ACCESS_COMMAND_ID);
		}
		return keySequence;
	}

	/**
	 * Return whether the shell is currently set to display all matches or limit the
	 * results.
	 *
	 * @return whether all matches will be displayed
	 */
	public boolean getShowAllMatches() {
		return showAllMatches;
	}

	private void refreshTable(QuickAccessElement perfectMatch, List<QuickAccessEntry>[] entries, String filter) {
		if (table.isDisposed()) {
			return;
		}
		if (table.getItemCount() > entries.length && table.getItemCount() - entries.length > 20) {
			table.removeAll();
		}
		TableItem[] items = table.getItems();
		int selectionIndex = -1;
		int index = 0;
		for (List<QuickAccessEntry> entriesForCurrentCategory : entries) {
			if (entriesForCurrentCategory != null) {
				boolean firstEntry = true;
				for (Iterator<QuickAccessEntry> it = entriesForCurrentCategory.iterator(); it.hasNext();) {
					QuickAccessEntry entry = it.next();
					entry.firstInCategory = firstEntry;
					firstEntry = false;
					if (!it.hasNext()) {
						entry.lastInCategory = true;
					}
					TableItem item;
					if (index < items.length) {
						item = items[index];
						table.clear(index);
					} else {
						item = new TableItem(table, SWT.NONE);
					}
					if (perfectMatch == entry.element && selectionIndex == -1) {
						selectionIndex = index;
					}
					item.setData(entry);
					item.setText(0, entry.provider.getName());
					item.setText(1, entry.element.getLabel());
					if (Util.isWpf()) {
						item.setImage(1, entry.getImage(entry.element, resourceManager));
					}
					index++;
				}
			}
		}
		if (index < items.length) {
			table.remove(index, items.length - 1);
		}
		if (selectionIndex == -1) {
			selectionIndex = 0;
		}

		if (table.getItemCount() > 0) {
			table.setSelection(selectionIndex);
			hideHintText();
		} else if (filter.isEmpty()) {
			showHintText(QuickAccessMessages.QuickAccess_StartTypingToFindMatches, grayColor);
		} else {
			showHintText(QuickAccessMessages.QuickAccessContents_NoMatchingResults, grayColor);
		}
		updateInfoLabel();
		updateFeedback(filter.isEmpty(), showAllMatches);
	}

	int numberOfFilteredResults;

	/**
	 * Compute how many items are effectively filtered at a specific point in time.
	 * So doing, the quick access content can perform operations that depends on
	 * this number, i.e. hide the info label.
	 *
	 * @return number number of elements filtered
	 */
	protected int getNumberOfFilteredResults() {
		return numberOfFilteredResults;
	}

	/**
	 * Returns a list per provider containing matching {@link QuickAccessEntry} that
	 * should be displayed in the table given a text filter and a perfect match
	 * entry that should be given priority. The number of items returned is affected
	 * by {@link #getShowAllMatches()} and the size of the table's composite.
	 *
	 * @param filter       the string text filter to apply, possibly empty
	 * @param perfectMatch a quick access element that should be given priority or
	 *                     <code>null</code>
	 *
	 * @param aMonitor
	 * @return the array of lists (one per provider) contains the quick access
	 *         entries that should be added to the table, possibly empty
	 */
	private List<QuickAccessEntry>[] computeMatchingEntries(String filter, QuickAccessElement perfectMatch,
			int maxNumberOfItemsInTable, IProgressMonitor aMonitor) {
		if (aMonitor == null) {
			aMonitor = new NullProgressMonitor();
		}
		// check for a category filter, like "Views: "
		Matcher categoryMatcher = getCategoryPattern().matcher(filter);
		String category = null;
		if (categoryMatcher.matches()) {
			category = categoryMatcher.group(1);
			filter = category + " " + categoryMatcher.group(2); //$NON-NLS-1$
		}
		final String finalFilter = filter;

		// collect matching elements
		LinkedHashMap<QuickAccessProvider, List<QuickAccessElement>> elementsForProviders = new LinkedHashMap<>(
				providers.length);
		for (QuickAccessProvider provider : providers) {
			if (aMonitor.isCanceled()) {
				break;
			}
			boolean isPreviousPickProvider = provider instanceof PreviousPicksProvider;
			// skip if filter contains a category, and current provider isn't this category
			if (category != null && !category.equalsIgnoreCase(provider.getName()) && !isPreviousPickProvider) {
				continue;
			}
			if (!filter.isEmpty() || isPreviousPickProvider || showAllMatches) {
				AtomicReference<List<QuickAccessElement>> sortedElementRef = new AtomicReference<>();
				if (provider.requiresUiAccess()) {
					UIJob job = new UIJob(
							NLS.bind(QuickAccessMessages.QuickAccessContents_processingProviderInUI,
									provider.getName())) {
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							sortedElementRef.set(Arrays.asList(provider.getElementsSorted(finalFilter, monitor)));
							return Status.OK_STATUS;
						}
					};
					job.setPriority(Job.INTERACTIVE);
					job.schedule();
					try {
						job.join(0, new NullProgressMonitor());
					} catch (Exception e) {
						WorkbenchPlugin.log(e);
					}
				} else {
					sortedElementRef.set(Arrays.asList(provider.getElementsSorted(filter, aMonitor)));
				}
				List<QuickAccessElement> sortedElements = sortedElementRef.get();
				if (sortedElements == null) {
					sortedElements = Collections.emptyList();
				}
				if (!(provider instanceof PreviousPicksProvider)) {
					for (QuickAccessElement element : sortedElements) {
						elementsToProviders.put(element, provider);
					}
				}
				if (!filter.isEmpty() && !sortedElements.isEmpty()) {
					sortedElements = putPrefixMatchFirst(sortedElements, filter);
				}
				elementsForProviders.put(provider, new ArrayList<>(sortedElements));
			}
		}

		// Sort out the Previous Pick
		List<String> prevPickIds = new ArrayList<>();
		for (Entry<QuickAccessProvider, List<QuickAccessElement>> entry : elementsForProviders.entrySet()) {
			if (entry.getKey() instanceof PreviousPicksProvider) {
				prevPickIds
						.addAll(entry.getValue().stream().map(QuickAccessElement::getId).collect(Collectors.toList()));
			}
		}
		for (Entry<QuickAccessProvider, List<QuickAccessElement>> entry : elementsForProviders.entrySet()) {
			if (!(entry.getKey() instanceof PreviousPicksProvider)) {
				List<QuickAccessElement> filteredElements = new ArrayList<>(entry.getValue());
				filteredElements.removeIf(element -> prevPickIds.contains(element.getId()));
				entry.setValue(filteredElements);
			}
		}
		// remove perfect match (will be added on top later)
		QuickAccessProvider perfectMatchProvider = null;
		if (perfectMatch != null) {
			for (Entry<QuickAccessProvider, List<QuickAccessElement>> entry : elementsForProviders.entrySet()) {
				if (perfectMatchProvider != null) {
					List<QuickAccessElement> filteredElements = new ArrayList<>(entry.getValue());
					if (filteredElements.removeIf(element -> prevPickIds.contains(element.getId()))) {
						entry.setValue(filteredElements);
						perfectMatchProvider = entry.getKey();
					}
				}
			}
		}
		LinkedHashMap<QuickAccessProvider, List<QuickAccessEntry>> entriesPerProvider = new LinkedHashMap<>(
				elementsForProviders.size());
		if (showAllMatches) {
			// Map elements to entries
			for (Entry<QuickAccessProvider, List<QuickAccessElement>> elementsPerProvider : elementsForProviders
					.entrySet()) {
				QuickAccessProvider provider = elementsPerProvider.getKey();
				List<QuickAccessEntry> entries = elementsPerProvider.getValue().stream() //
						.map(QuickAccessMatcher::new) //
						.map(matcher -> matcher.match(finalFilter, provider)) //
						.filter(Objects::nonNull) //
						.collect(Collectors.toList());
				if (!entries.isEmpty()) {
					entriesPerProvider.put(provider, entries);
				}
			}
		} else {
			int numberOfSlotsLeft = perfectMatch != null ? maxNumberOfItemsInTable -1 : maxNumberOfItemsInTable;
			while (!elementsForProviders.isEmpty() && numberOfSlotsLeft > 0) {
				int nbEntriesPerProvider = numberOfSlotsLeft / elementsForProviders.size();
				if (nbEntriesPerProvider > 0) {
					for (Entry<QuickAccessProvider, List<QuickAccessElement>> elementsPerProvider : elementsForProviders
							.entrySet()) {
						QuickAccessProvider provider = elementsPerProvider.getKey();
						List<QuickAccessElement> elements = elementsPerProvider.getValue();
						int toPickEntries = nbEntriesPerProvider;
						while (toPickEntries > 0 && !elements.isEmpty()) {
							QuickAccessElement element = elements.remove(0);
							QuickAccessEntry entry = new QuickAccessMatcher(element).match(filter, provider);
							if (entry != null) {
								numberOfSlotsLeft--;
								toPickEntries--;
								if (!entriesPerProvider.containsKey(provider)) {
									entriesPerProvider.put(provider, new LinkedList<>());
								}
								entriesPerProvider.get(provider).add(entry);
							}
						}
					}
				} else {
					for (Entry<QuickAccessProvider, List<QuickAccessElement>> elementsForProvider : elementsForProviders
							.entrySet()) {
						if (numberOfSlotsLeft > 0) {
							QuickAccessProvider provider = elementsForProvider.getKey();
							List<QuickAccessElement> elements = elementsForProvider.getValue();
							boolean entryPicked = false;
							while (!entryPicked && !elements.isEmpty()) {
								QuickAccessElement element = elements.remove(0);
								QuickAccessEntry entry = new QuickAccessMatcher(element).match(filter, provider);
								if (entry != null) {
									numberOfSlotsLeft--;
									entryPicked = true;
									if (!entriesPerProvider.containsKey(provider)) {
										entriesPerProvider.put(provider, new LinkedList<>());
									}
									entriesPerProvider.get(provider).add(entry);
								}
							}
						}
					}
				}
				Set<QuickAccessProvider> exhaustedProviders = new HashSet<>();
				elementsForProviders.forEach((provider, elements) -> {
					if (elements.isEmpty()) {
						exhaustedProviders.add(provider);
					}
				});
				exhaustedProviders.forEach(elementsForProviders::remove);
			}
		}
		//
		List<List<QuickAccessEntry>> res = new ArrayList<>();
		if (perfectMatch != null) {
			res.add(Collections.singletonList(new QuickAccessEntry(perfectMatch,
					perfectMatchProvider != null ? perfectMatchProvider : providers[0], new int[0][0], new int[0][0],
					QuickAccessEntry.MATCH_PERFECT)));
		}
		res.addAll(entriesPerProvider.values());
		return (List<QuickAccessEntry>[]) res.toArray(new List<?>[res.size()]);
	}

	/*
	 * Consider whether we could directly check the "matchQuality" here, but it
	 * seems to be a more expensive operation
	 */
	private static List<QuickAccessElement> putPrefixMatchFirst(List<QuickAccessElement> elements, String prefix) {
		List<QuickAccessElement> res = new ArrayList<>(elements);
		List<Integer> matchingIndexes = new ArrayList<>();
		for (int i = 0; i < elements.size(); i++) {
			if (elements.get(i).getLabel().toLowerCase().startsWith(prefix.toLowerCase())) {
				matchingIndexes.add(Integer.valueOf(i));
			}
		}
		int currentMatchIndex = 0;
		int currentNonMatchIndex = matchingIndexes.size();
		for (int i = 0; i < res.size(); i++) {
			boolean isMatch = !matchingIndexes.isEmpty() && matchingIndexes.iterator().next().intValue() == i;
			if (isMatch) {
				matchingIndexes.remove(0);
				res.set(currentMatchIndex, elements.get(i));
				currentMatchIndex++;
			} else {
				res.set(currentNonMatchIndex, elements.get(i));
				currentNonMatchIndex++;
			}
		}
		return res;
	}

	Pattern categoryPattern;

	/**
	 * Return a pattern like {@code "^(:?Views|Perspective):\\s?(.*)"}, with all the
	 * provider names separated by semicolon.
	 *
	 * @return Returns the patternProvider.
	 */
	protected Pattern getCategoryPattern() {
		if (categoryPattern == null) {
			// build regex like "^(:?Views|Perspective):\\s?(.*)"
			StringBuilder sb = new StringBuilder();
			sb.append("^(:?"); //$NON-NLS-1$
			for (int i = 0; i < providers.length; i++) {
				if (i != 0)
					sb.append("|"); //$NON-NLS-1$
				sb.append(providers[i].getName());
			}
			sb.append("):\\s?(.*)"); //$NON-NLS-1$
			String regex = sb.toString();
			categoryPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		}
		return categoryPattern;
	}

	private void doDispose() {
		if (textLayout != null && !textLayout.isDisposed()) {
			textLayout.dispose();
		}
		if (resourceManager != null) {
			// Disposing the resource manager will dispose the color
			resourceManager.dispose();
			resourceManager = null;
		}
	}

	protected String getId() {
		return "org.eclipse.ui.internal.QuickAccess"; //$NON-NLS-1$
	}

	protected abstract void handleElementSelected(String text, Object selectedElement);

	private void handleSelection() {
		QuickAccessElement selectedElement = null;
		String text = filterText.getText().toLowerCase();
		if (table.getSelectionCount() == 1) {
			QuickAccessEntry entry = (QuickAccessEntry) table.getSelection()[0].getData();
			selectedElement = entry == null ? null : entry.element;
		}
		if (selectedElement != null) {
			doClose();
			handleElementSelected(text, selectedElement);
		}
	}

	/**
	 * Should be called by the owner of the parent composite when the shell is being
	 * activated (made visible). This allows the show all keybinding to be updated.
	 */
	public void preOpen() {
		// Make sure we always start filtering
		setShowAllMatches(false);
		// In case the key binding has changed, update the label
		keySequence = null;
		updateInfoLabel();
	}

	/**
	 * Informs the owner of the parent composite that the quick access dialog should
	 * be closed
	 */
	protected abstract void doClose();

	/**
	 * Allows the dialog contents to interact correctly with the text box used to
	 * open it
	 *
	 * @param filterText text box to hook up
	 */
	public void hookFilterText(Text filterText) {
		this.filterText = filterText;
		filterText.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
				case SWT.CR:
				case SWT.KEYPAD_CR:
					handleSelection();
					break;
				case SWT.ARROW_DOWN:
					int index = table.getSelectionIndex();
					if (index != -1 && table.getItemCount() > index + 1) {
						table.setSelection(index + 1);
					}
					break;
				case SWT.ARROW_UP:
					index = table.getSelectionIndex();
					if (index != -1 && index >= 1) {
						table.setSelection(index - 1);
					}
					break;
				case SWT.ESC:
					doClose();
					break;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// do nothing
			}
		});
		filterText.addModifyListener(e -> {
			String text = ((Text) e.widget).getText();
			updateProposals(text);
		});
	}

	Label hintText;
	private boolean displayHintText;

	/** Create HintText as child of the given parent composite */
	Label createHintText(Composite composite, int defaultOrientation) {
		hintText = new Label(composite, SWT.FILL);
		hintText.setOrientation(defaultOrientation);
		displayHintText = true;
		return hintText;
	}

	/** Hide the hint text */
	private void hideHintText() {
		if (displayHintText) {
			setHintTextToDisplay(false);
		}
	}

	/** Show the hint text with the given color */
	private void showHintText(String text, Color color) {
		if (hintText == null || hintText.isDisposed()) {
			// toolbar hidden
			return;
		}
		hintText.setText(text);
		if (color != null) {
			hintText.setForeground(color);
		}
		if (!displayHintText) {
			setHintTextToDisplay(true);
		}
	}

	/**
	 * Sets hint text to be displayed and requests the layout
	 *
	 * @param toDisplay
	 */
	private void setHintTextToDisplay(boolean toDisplay) {
		GridData data = (GridData) hintText.getLayoutData();
		data.exclude = !toDisplay;
		hintText.setVisible(toDisplay);
		hintText.requestLayout();
		this.displayHintText = toDisplay;
	}

	/**
	 * Creates the table providing the contents for the quick access dialog
	 *
	 * @param composite          parent composite with {@link GridLayout}
	 * @param defaultOrientation the window orientation to use for the table
	 *                           {@link SWT#RIGHT_TO_LEFT} or
	 *                           {@link SWT#LEFT_TO_RIGHT}
	 * @return the created table
	 */
	public Table createTable(Composite composite, int defaultOrientation) {
		composite.addDisposeListener(e -> doDispose());
		Composite tableComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableComposite);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		table = new Table(tableComposite, SWT.SINGLE | SWT.FULL_SELECTION);
		textLayout = new TextLayout(table.getDisplay());
		textLayout.setOrientation(defaultOrientation);
		Font boldFont = resourceManager.create(FontDescriptor.createFrom(table.getFont()).setStyle(SWT.BOLD));
		textLayout.setFont(table.getFont());
		textLayout.setText(QuickAccessMessages.QuickAccess_AvailableCategories);
		int maxProviderWidth = (textLayout.getBounds().width);
		textLayout.setFont(boldFont);
		for (QuickAccessProvider provider : providers) {
			textLayout.setText(provider.getName());
			int width = (textLayout.getBounds().width);
			if (width > maxProviderWidth) {
				maxProviderWidth = width;
			}
		}
		tableColumnLayout.setColumnData(new TableColumn(table, SWT.NONE), new ColumnWeightData(0, maxProviderWidth));
		tableColumnLayout.setColumnData(new TableColumn(table, SWT.NONE), new ColumnWeightData(100, 100));
		table.getShell().addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				if (!showAllMatches) {
					if (!resized) {
						resized = true;
						e.display.timerExec(100, () -> {
							if (table != null && !table.isDisposed() && filterText != null
									&& !filterText.isDisposed()) {
								updateProposals(filterText.getText().toLowerCase());
							}
							resized = false;
						});
					}
				}
			}
		});

		table.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_UP && table.getSelectionIndex() == 0) {
					filterText.setFocus();
				} else if (e.character == SWT.ESC) {
					doClose();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// do nothing
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {

				if (table.getSelectionCount() < 1)
					return;

				if (e.button != 1)
					return;

				if (table.equals(e.getSource())) {
					Object o = table.getItem(new Point(e.x, e.y));
					TableItem selection = table.getSelection()[0];
					if (selection.equals(o))
						handleSelection();
				}
			}
		});

		table.addMouseMoveListener(new MouseMoveListener() {
			TableItem lastItem = null;

			@Override
			public void mouseMove(MouseEvent e) {
				if (table.equals(e.getSource())) {
					TableItem tableItem = table.getItem(new Point(e.x, e.y));
					if (lastItem == null ^ tableItem == null) {
						table.setCursor(tableItem == null ? null : table.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
					}
					if (tableItem != null) {
						if (!tableItem.equals(lastItem)) {
							lastItem = tableItem;
							table.setSelection(new TableItem[] { lastItem });
						}
					} else {
						lastItem = null;
					}
				}
			}
		});

		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				handleSelection();
			}
		});

		final TextStyle boldStyle;
		if (PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.USE_COLORED_LABELS)) {
			boldStyle = new TextStyle(boldFont, null, null);
			grayColor = resourceManager
					.createColor(ColorUtil.blend(table.getBackground().getRGB(), table.getForeground().getRGB()));
		} else {
			boldStyle = null;
		}
		Listener listener = event -> {
			QuickAccessEntry entry = (QuickAccessEntry) event.item.getData();
			if (entry != null) {
				switch (event.type) {
				case SWT.MeasureItem:
					entry.measure(event, textLayout, resourceManager, boldStyle);
					break;
				case SWT.PaintItem:
					entry.paint(event, textLayout, resourceManager, boldStyle, grayColor);
					break;
				case SWT.EraseItem:
					entry.erase(event);
					break;
				}
			}
		};
		table.addListener(SWT.MeasureItem, listener);
		table.addListener(SWT.EraseItem, listener);
		table.addListener(SWT.PaintItem, listener);

		return table;
	}

	/**
	 * Creates a label which will display the key binding to expand the search
	 * results.
	 *
	 * @param parent parent composite with {@link GridLayout}
	 * @return the created label
	 */
	public Label createInfoLabel(Composite parent) {
		infoLabel = new Label(parent, SWT.NONE);
		infoLabel.setFont(parent.getFont());
		infoLabel.setForeground(grayColor);
		infoLabel.setBackground(table.getBackground());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = SWT.RIGHT;
		gd.grabExcessHorizontalSpace = false;
		infoLabel.setLayoutData(gd);
		updateInfoLabel();
		return infoLabel;
	}

	QuickAccessProvider getProvider(String providerId) {
		if (providers == null || providers.length == 0) {
			return null;
		}
		if (providerMap == null || providerMap.size() != providers.length) {
			providerMap = Arrays.stream(providers)
					.collect(Collectors.toMap(QuickAccessProvider::getId, Function.identity()));
		}
		return providerMap.get(providerId);
	}

	QuickAccessProvider getProviderFor(QuickAccessElement quickAccessElement) {
		return elementsToProviders.get(quickAccessElement);
	}

	void registerProviderFor(QuickAccessElement quickAccessElement, QuickAccessProvider quickAccessProvider) {
		if (quickAccessElement == null || quickAccessProvider == null) {
			return;
		}
		elementsToProviders.put(quickAccessElement, quickAccessProvider);
	}

	public Text getFilterText() {
		return filterText;
	}

	public Table getTable() {
		return table;
	}
}