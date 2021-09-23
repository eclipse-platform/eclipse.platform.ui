/*******************************************************************************
 * Copyright (c) 2021 EclipseSource GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     EclipseSource GmbH - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.workbench.compatibiliy.migration;

import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_ACTIVE_PAGE_ID;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_ACTIVE_PART;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_ACTIVE_PERSPECTIVE;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_ALWAYS_ON_ACTION_SET;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_AREA;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_AREA_TRIM_STATE;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_AREA_VISIBLE;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_CONTENT;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_DESCRIPTOR;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_DETACHED_WINDOW;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_EDITOR;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_EDITORS;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_EXPANDED;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_FAST_VIEWS;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_FAST_VIEW_BAR;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_FAST_VIEW_BARS;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_FILE;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_FOCUS;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_FOLDER;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_HEIGHT;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_HIDDEN_WINDOW;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_HIDE_MENU;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_HIDE_TOOLBAR;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_ID;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_INFO;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_LABEL;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_LAYOUT;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_MAIN_WINDOW;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_MAXIMIZED;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_MINIMIZED;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_MRU_LIST;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_NEW_WIZARD_ACTION;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_PAGE;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_PART;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_PART_NAME;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_PERSPECTIVE;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_PERSPECTIVES;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_PERSPECTIVE_ACTION;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_PERSPECTIVE_BAR;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_PRESENTATION;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_PROPERTIES;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_PROPERTY;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_RATIO;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_RATIO_LEFT;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_RATIO_RIGHT;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_RELATIONSHIP;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_RELATIVE;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_SHOW_IN_TIME;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_SHOW_VIEW_ACTION;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_TITLE;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_TOOLTIP;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_VIEW;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_VIEWS;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_VIEW_STATE;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_WIDTH;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_WINDOW;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_WORKBOOK;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_WORKING_SET;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_WORKING_SETS;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_X;
import static org.eclipse.ui.internal.IWorkbenchConstants.TAG_Y;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.persistence.common.CommonUtil;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayoutUtils;

/**
 * The Implementation of the Converter interface.
 *
 * @since 3.3
 *
 */
@SuppressWarnings("restriction")
public class Converter {

	/** Element id for the primary editor stack in an e4 application. */
	private static final String PRIMARY_EDITOR_PART_STACK_ID = "org.eclipse.e4.primaryDataStack"; //$NON-NLS-1$

	private static final String EDITOR_AREA_ID = "org.eclipse.ui.editorss"; //$NON-NLS-1$

	private final Map<Object, Set<String>> visited = new LinkedHashMap<>();

	/**
	 * Constructor.
	 */
	public Converter() {
	}

	/**
	 * Allows to convert an 3.x perspective to a serialized e4 applciation.
	 *
	 * @param sourcePath The path to the 3.x serialized perspective.
	 * @param targetPath The path to the file to serialize the e4 application to.
	 * @throws IOException           When reading/writing the files fails.
	 * @throws FileNotFoundException When the files do not exist.
	 * @throws WorkbenchException    When reading the 3.x perspective fails
	 */
	public static void convert(final String sourcePath, final String targetPath)
			throws FileNotFoundException, IOException, WorkbenchException {
		Converter converter = new Converter();
		IMemento memento;
		try (FileInputStream input = new FileInputStream(sourcePath)) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, "utf-8")); //$NON-NLS-1$
			memento = XMLMemento.createReadRoot(reader);
		}

		ResourceSet resourceSet = new ResourceSetImpl();

		Resource resource = resourceSet.createResource(URI.createURI("sample.e4xmi")); //$NON-NLS-1$
		resource.getContents().add((EObject) converter.convert(memento));

		try (FileOutputStream output = new FileOutputStream(targetPath)) {
			resource.save(output, null);
		}

	}

	/**
	 * Convert the IMemento of the workbench state of an 3.x application to a MApplication.
	 *
	 * @param memento The IMemento to convert
	 * @return The resulting MApplication
	 * @throws WorkbenchException Thrown when the xml cannot be parsed
	 */
	public MApplication convert(final IMemento memento) {
		MApplication application = MApplicationFactory.INSTANCE.createApplication();

		// Initialize the add ons.
		{
			List<MAddon> addons = application.getAddons();
			String[][] addonValues = new String[][] { new String[] { "org.eclipse.e4.core.commands.service", //$NON-NLS-1$
					"platform:/plugin/org.eclipse.platform", //$NON-NLS-1$
					"bundleclass://org.eclipse.e4.core.commands/org.eclipse.e4.core.commands.CommandServiceAddon" }, //$NON-NLS-1$
					new String[] { "org.eclipse.e4.ui.contexts.service", "platform:/plugin/org.eclipse.platform", //$NON-NLS-1$ //$NON-NLS-2$
							"bundleclass://org.eclipse.e4.ui.services/org.eclipse.e4.ui.services.ContextServiceAddon" }, //$NON-NLS-1$
					new String[] { "org.eclipse.e4.ui.bindings.service", "platform:/plugin/org.eclipse.platform", //$NON-NLS-1$ //$NON-NLS-2$
							"bundleclass://org.eclipse.e4.ui.bindings/org.eclipse.e4.ui.bindings.BindingServiceAddon" }, //$NON-NLS-1$
					new String[] { "org.eclipse.e4.ui.workbench.commands.model", //$NON-NLS-1$
							"platform:/plugin/org.eclipse.platform", //$NON-NLS-1$
							"bundleclass://org.eclipse.e4.ui.workbench/org.eclipse.e4.ui.internal.workbench.addons.CommandProcessingAddon" }, //$NON-NLS-1$
					new String[] { "org.eclipse.e4.ui.workbench.contexts.model", //$NON-NLS-1$
							"platform:/plugin/org.eclipse.platform", //$NON-NLS-1$
							"bundleclass://org.eclipse.e4.ui.workbench/org.eclipse.e4.ui.internal.workbench.addons.ContextProcessingAddon" }, //$NON-NLS-1$
					new String[] { "org.eclipse.e4.ui.workbench.bindings.model", //$NON-NLS-1$
							"platform:/plugin/org.eclipse.platform", //$NON-NLS-1$
							"bundleclass://org.eclipse.e4.ui.workbench.swt/org.eclipse.e4.ui.workbench.swt.util.BindingProcessingAddon" }, //$NON-NLS-1$
					new String[] { "Cleanup Addon", "platform:/plugin/org.eclipse.platform", //$NON-NLS-1$ //$NON-NLS-2$
							"bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.cleanupaddon.CleanupAddon" }, //$NON-NLS-1$
					new String[] { "DnD Addon", "platform:/plugin/org.eclipse.platform", //$NON-NLS-1$ //$NON-NLS-2$
							"bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.dndaddon.DnDAddon" }, //$NON-NLS-1$
					new String[] { "MinMax Addon", "platform:/plugin/org.eclipse.platform", //$NON-NLS-1$ //$NON-NLS-2$
							"bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.minmax.MinMaxAddon" }, //$NON-NLS-1$
					new String[] { "org.eclipse.ui.workbench.addon.0", "platform:/plugin/org.eclipse.platform", //$NON-NLS-1$ //$NON-NLS-2$
							"bundleclass://org.eclipse.e4.ui.workbench/org.eclipse.e4.ui.internal.workbench.addons.HandlerProcessingAddon" }, //$NON-NLS-1$
					new String[] { "SplitterAddon", null, //$NON-NLS-1$
							"bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.splitteraddon.SplitterAddon" }, //$NON-NLS-1$
					new String[] { "org.eclipse.ui.ide.addon.0", null, //$NON-NLS-1$
							"bundleclass://org.eclipse.ui.ide/org.eclipse.ui.internal.ide.addons.SaveAllDirtyPartsAddon" }, //$NON-NLS-1$
					new String[] { "org.eclipse.ui.ide.application.addon.0", //$NON-NLS-1$
							"platform:/plugin/org.eclipse.ui.ide.application", //$NON-NLS-1$
							"bundleclass://org.eclipse.ui.ide.application/org.eclipse.ui.internal.ide.application.addons.ModelCleanupAddon" }, }; //$NON-NLS-1$
			for (String[] addonValue : addonValues) {
				MAddon addon = MApplicationFactory.INSTANCE.createAddon();
				addon.setElementId(addonValue[0]);
				addon.setContributorURI(addonValue[1]);
				addon.setContributionURI(addonValue[2]);
				addons.add(addon);
			}
		}

		IMemento mruMemento = memento.getChild(TAG_MRU_LIST);
		if (mruMemento != null) {
			for (IMemento fileMemento : mruMemento.getChildren(TAG_FILE)) {
				// Ignored.
				fileMemento.toString();
			}
		}

		final IMemento[] windowMementos = memento.getChildren(TAG_WINDOW);
		if (windowMementos == null || windowMementos.length == 0) {
			// no <window> memento available - try memento instead
			visitWindow(application, memento);
		} else {
			for (IMemento windowMemento : windowMementos) {
				visitWindow(application, windowMemento);
			}
		}

		return application;
	}

	private void visitWindow(final MApplication application, final IMemento memento) {
		MTrimmedWindow window = MBasicFactory.INSTANCE.createTrimmedWindow();
		window.setContributorURI("platform:/plugin/org.eclipse.platform"); //$NON-NLS-1$
		application.getChildren().add(window);

		Rectangle shellBounds = new Rectangle(getInteger(memento, TAG_X), getInteger(memento, TAG_Y),
				getInteger(memento, TAG_WIDTH), getInteger(memento, TAG_HEIGHT));
		if (!shellBounds.isEmpty()) {
			window.setX(shellBounds.x);
			window.setY(shellBounds.y);
			window.setWidth(shellBounds.width);
			window.setHeight(shellBounds.height);
		}

		if (getBoolean(memento, TAG_MAXIMIZED)) {
			// Ignored.
		}

		if (getBoolean(memento, TAG_MINIMIZED)) {
			// Ignored.
		}

		IMemento perspectiveBarMemento = memento.getChild(TAG_PERSPECTIVE_BAR);
		visitPerspectiveBar(perspectiveBarMemento);

		for (IMemento pageMemento : memento.getChildren(TAG_PAGE)) {
			visitPage(window, pageMemento);
		}

	}

	private void visitPage(final MWindow window, final IMemento memento) {
		String focus = getString(memento, TAG_FOCUS);
		if (focus != null && !focus.isEmpty()) {
			String pageName = getString(memento, TAG_LABEL);
			if (pageName != null) {
				// Ignore.
			}

			String workingSetName = getString(memento, TAG_WORKING_SET);
			if (workingSetName != null) {
				// Ignore.
			}

			IMemento workingSetsMemento = memento.getChild(TAG_WORKING_SETS);
			if (workingSetsMemento != null) {
				for (IMemento workingSetMemento : workingSetsMemento.getChildren(TAG_WORKING_SET)) {
					String id = getID(workingSetMemento);
					if (id != null) {
						// Ignore.
					}
				}
			}

			// org.eclipse.ui.internal.WorkbenchPage.ATT_AGGREGATE_WORKING_SET_ID
			String aggregateWorkingSetId = getString(memento, "aggregateWorkingSetId"); //$NON-NLS-1$
			if (aggregateWorkingSetId != null) {
				// Ignore.
			}

			IMemento editorsMemento = memento.getChild(TAG_EDITORS);
			if (editorsMemento != null) {
				visitEditors(window, editorsMemento);
			}

			IMemento viewsMemento = memento.getChild(TAG_VIEWS);
			if (viewsMemento != null) {
				visitViews(window, viewsMemento);
			}

			IMemento perspectivesMemento = memento.getChild(TAG_PERSPECTIVES);
			if (perspectivesMemento != null) {
				visitPerspectives(window, perspectivesMemento);
			}
		}
	}

	private void visitPerspectives(final MWindow window, final IMemento memento) {
		String activePartID = getString(memento, TAG_ACTIVE_PART);
		String activePerspectiveID = getString(memento, TAG_ACTIVE_PERSPECTIVE);

		if (activePartID != null) {
			// Ignore.
		}

		MPartSashContainer partSashContainer = MBasicFactory.INSTANCE.createPartSashContainer();
		partSashContainer.setHorizontal(true);
		window.getChildren().add(partSashContainer);

		MPerspectiveStack perspectiveStack = MAdvancedFactory.INSTANCE.createPerspectiveStack();
		partSashContainer.getChildren().add(perspectiveStack);

		List<MPerspective> perspectiveStackChildren = perspectiveStack.getChildren();
		for (IMemento perspectiveMemento : memento.getChildren(TAG_PERSPECTIVE)) {
			MPerspective perspective = MAdvancedFactory.INSTANCE.createPerspective();
			perspectiveStackChildren.add(0, perspective);

			int areaVisible = getInteger(perspectiveMemento, TAG_AREA_VISIBLE);

			int trimAreaState = getInteger(perspectiveMemento, TAG_AREA_TRIM_STATE);
			if (trimAreaState != 0) {
				// Ignore.
			}

			Set<String> visibleViews = new LinkedHashSet<>();
			for (IMemento viewMemento : perspectiveMemento.getChildren(TAG_VIEW)) {
				String id = getID(viewMemento);
				visibleViews.add(id);
			}

			Set<String> minimizedViews = getMinimizedViews(perspectiveMemento);

			IMemento fastViewsMemento = perspectiveMemento.getChild(TAG_FAST_VIEWS);
			if (fastViewsMemento != null) {
				for (IMemento viewMemento : fastViewsMemento.getChildren(TAG_VIEW)) {
					String id = getID(viewMemento);
					if (id != null) {
						// Ignore.
					}
				}
			}

			IMemento descriptorMemento = perspectiveMemento.getChild(TAG_DESCRIPTOR);
			if (descriptorMemento != null) {
				String id = getString(descriptorMemento, TAG_ID);
				String label = getString(descriptorMemento, TAG_LABEL);

				perspective.setElementId(id);
				perspective.setLabel(label);

				if (id.equals(activePerspectiveID)) {
					perspectiveStack.setSelectedElement(perspective);
				}
			}

			IMemento boundsMemento = perspectiveMemento.getChild(TAG_WINDOW);
			if (boundsMemento != null) {
				Rectangle bounds = new Rectangle(getInteger(boundsMemento, TAG_X), getInteger(boundsMemento, TAG_Y),
						getInteger(boundsMemento, TAG_WIDTH), getInteger(boundsMemento, TAG_HEIGHT));
				if (!bounds.isEmpty()) {
					// Ignore.
				}
			}

			IMemento layoutMemento = perspectiveMemento.getChild(TAG_LAYOUT);
			if (layoutMemento != null) {
				IMemento mainWindowMemento = layoutMemento.getChild(TAG_MAIN_WINDOW);
				if (mainWindowMemento != null) {
					MPartSashContainer perspectivePartSashContainer = MBasicFactory.INSTANCE.createPartSashContainer();
					perspectivePartSashContainer.setHorizontal(true);
					perspective.getChildren().add(perspectivePartSashContainer);

					if (areaVisible != 0) {
						// Ignore.
					}

					Map<String, Info> infos = new LinkedHashMap<>();
					for (IMemento infoMemento : mainWindowMemento.getChildren(TAG_INFO)) {
						Info info = new Info();
						info.partID = getString(infoMemento, TAG_PART);
						info.relativeID = getString(infoMemento, TAG_RELATIVE);
						if (info.relativeID != null) {
							info.relationship = getInteger(infoMemento, TAG_RELATIONSHIP);
							info.ratio = getFloat(infoMemento, TAG_RATIO, 0.5f);
							info.left = getInteger(infoMemento, TAG_RATIO_LEFT);
							info.right = getInteger(infoMemento, TAG_RATIO_RIGHT);
						}

						String folder = getString(infoMemento, TAG_FOLDER);
						if (folder != null) {
							IMemento folderMemento = infoMemento.getChild(TAG_FOLDER);
							if (folderMemento != null) {
								MPartStack partStack = MBasicFactory.INSTANCE.createPartStack();
								partStack.setElementId(info.partID);
								perspectivePartSashContainer.getChildren().add(partStack);

								info.part = partStack;

								String activePageID = getString(folderMemento, TAG_ACTIVE_PAGE_ID);

								List<MStackElement> partStackChildren = partStack.getChildren();
								for (IMemento pageMemento : folderMemento.getChildren(TAG_PAGE)) {
									String pagePartID = getString(pageMemento, TAG_CONTENT);
									if (pagePartID != null) {
										MPlaceholder placeHolder = MAdvancedFactory.INSTANCE.createPlaceholder();
										partStackChildren.add(placeHolder);
										placeHolder.setElementId(pagePartID);
										// A placeholder is rendered if it's visible or minimized.
										// In case of minimization it is minimized through its parent part stack
										boolean visibleOrMinimized = visibleViews.contains(pagePartID)
												|| minimizedViews.contains(pagePartID);
										placeHolder.setToBeRendered(visibleOrMinimized);

										for (MUIElement element : window.getSharedElements()) {
											if (pagePartID.equals(element.getElementId())) {
												placeHolder.setRef(element);
												break;
											}
										}

										if (placeHolder.getRef() == null) {
											MPart part = MBasicFactory.INSTANCE.createPart();
											part.setElementId(pagePartID);
											part.setLabel("fake" + pagePartID); //$NON-NLS-1$
											part.setContributionURI(
													"bundleclass://org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityView"); //$NON-NLS-1$
											window.getSharedElements().add(part);
											placeHolder.setRef(part);
										}

										if (pagePartID.equals(activePageID)) {
											partStack.setSelectedElement(placeHolder);
											if (placeHolder.getRef() == null) {
												System.err.print(""); //$NON-NLS-1$

											}
										}
									}
								}

								int state = getInteger(folderMemento, TAG_EXPANDED);
								if (state != 0) {
									// Ignore.
								}

								for (IMemento presentationMemento : folderMemento.getChildren(TAG_PRESENTATION)) {
									String id = getString(presentationMemento, TAG_ID);
									if (id != null) {
										// Ignore.
									}
								}

								Map<String, String> properties = new LinkedHashMap<>();
								IMemento propertiesMemento = folderMemento.getChild(TAG_PROPERTIES);
								if (propertiesMemento != null) {
									for (IMemento propertyMemento : propertiesMemento.getChildren(TAG_PROPERTY)) {
										properties.put(getID(propertiesMemento), propertyMemento.getTextData());
									}
								}
							}
						} else if (EDITOR_AREA_ID.equals(info.partID)) {
							// Assume that the editor area is at the root level of the main window
							MPlaceholder areaPlaceholder = MAdvancedFactory.INSTANCE.createPlaceholder();
							perspectivePartSashContainer.getChildren().add(areaPlaceholder);
							areaPlaceholder.setElementId(EDITOR_AREA_ID);
							info.part = areaPlaceholder;

							// Find editor area and reference it in the placeholder
							window.getSharedElements().stream().filter(e -> EDITOR_AREA_ID.equals(e.getElementId()))
									.findFirst().ifPresent(e -> areaPlaceholder.setRef(e));
						} else {
							continue;
						}

						infos.put(info.partID, info);
					}

					ModeledPageLayoutUtils modeledPageLayoutUtils = new org.eclipse.ui.internal.e4.compatibility.ModeledPageLayoutUtils(
							CommonUtil.getEModelService());
					System.out.println("Perspective: " + perspective.getLabel()); //$NON-NLS-1$
					for (Info info : infos.values()) {
						System.out.println("  " + info); //$NON-NLS-1$

						Info relativePart = infos.get(info.relativeID);
						if (relativePart != null) {
							modeledPageLayoutUtils.insert(info.part, relativePart.part,
									modeledPageLayoutUtils.plRelToSwt(info.relationship), info.ratio);
						}
					}

					for (Iterator<EObject> i = ((EObject) perspectivePartSashContainer).eAllContents(); i.hasNext();) {
						EObject eObject = i.next();
						if (eObject instanceof MPartStack) {
							MPartStack partStack = (MPartStack) eObject;
							boolean toBeRendered = false;
							boolean minimized = false;
							// If non of the stack's children are rendered, do not render the stack either
							for (MStackElement stackElement : partStack.getChildren()) {
								if (stackElement.isToBeRendered()) {
									toBeRendered = true;
								}
								if (minimizedViews.contains(stackElement.getElementId())) {
									minimized = true;
									toBeRendered = true;
									break;
								}
							}

							// If the stack's children are minimized, minimize the part stack
							if (minimized) {
								// If the part stack is minimized, select the first child that is rendered.
								// Otherwise no child is selected on restore.
								for (MStackElement stackElement : partStack.getChildren()) {
									if (stackElement.isToBeRendered()) {
										partStack.setSelectedElement(stackElement);
										break;
									}
								}
								partStack.setToBeRendered(true);
								partStack.setVisible(false);
								partStack.getTags().add(IPresentationEngine.MINIMIZED);
							} else {
								partStack.setToBeRendered(toBeRendered);
								partStack.setVisible(toBeRendered);
							}
						}
					}

					for (Iterator<EObject> i = ((EObject) perspectivePartSashContainer).eAllContents(); i.hasNext();) {
						EObject eObject = i.next();
						if (eObject instanceof MPartSashContainer) {
							MPartSashContainer otherPartSashContainer = (MPartSashContainer) eObject;
							boolean toBeRendered = false;
							boolean visible = false;
							for (MPartSashContainerElement sashContainerElement : otherPartSashContainer
									.getChildren()) {
								if (sashContainerElement.isVisible()) {
									visible = true;
								}
								if (sashContainerElement.isToBeRendered()) {
									toBeRendered = true;
								}
								if (visible && toBeRendered) {
									break;
								}
							}

							otherPartSashContainer.setToBeRendered(toBeRendered);
							// A part sash container can only be visible if it is rendered.
							// A container can be rendered but not visible if all child part stacks are
							// minimized.
							otherPartSashContainer.setVisible(visible && toBeRendered);
						}
					}

					for (IMemento detachedWindowMemento : mainWindowMemento.getChildren(TAG_DETACHED_WINDOW)) {
						if (detachedWindowMemento != null) {
							// Ignore.
						}
					}

					for (IMemento hiddenWindowMemento : mainWindowMemento.getChildren(TAG_HIDDEN_WINDOW)) {
						if (hiddenWindowMemento != null) {
							// Ignore.
						}
					}

					String maximizedStackID = getString(mainWindowMemento, TAG_MAXIMIZED);
					if (maximizedStackID != null) {
						// Ignore.
					}
				}
			}

			for (IMemento viewMemento : perspectiveMemento.getChildren(TAG_VIEW)) {
				String id = getString(viewMemento, TAG_ID);
				if (id != null) {
					// Ignore.
				}
			}

			for (IMemento alwaysOnActionMemento : perspectiveMemento.getChildren(TAG_ALWAYS_ON_ACTION_SET)) {
				String id = getString(alwaysOnActionMemento, TAG_ID);
				if (id != null) {
					// Ignore.
				}
			}

			for (IMemento toggleViewActionMemento : perspectiveMemento.getChildren(TAG_SHOW_VIEW_ACTION)) {
				String id = getString(toggleViewActionMemento, TAG_ID);
				if (id != null) {
					// Ignore.
				}
			}

			for (IMemento showInTimeMemento : perspectiveMemento.getChildren(TAG_SHOW_IN_TIME)) {
				String id = getString(showInTimeMemento, TAG_ID);
				if (id != null) {
					// Ignore.
				}
			}

			for (IMemento newWizardActionMemento : perspectiveMemento.getChildren(TAG_NEW_WIZARD_ACTION)) {
				String id = getString(newWizardActionMemento, TAG_ID);
				if (id != null) {
					// Ignore.
				}
			}

			for (IMemento perspectiveActionMemento : perspectiveMemento.getChildren(TAG_PERSPECTIVE_ACTION)) {
				String id = getString(perspectiveActionMemento, TAG_ID);
				if (id != null) {
					// Ignore.
				}
			}

			for (IMemento hideMenuMemento : perspectiveMemento.getChildren(TAG_HIDE_MENU)) {
				String id = getString(hideMenuMemento, TAG_ID);
				if (id != null) {
					// Ignore.
				}
			}

			for (IMemento hideToolbarMemento : perspectiveMemento.getChildren(TAG_HIDE_TOOLBAR)) {
				String id = getString(hideToolbarMemento, TAG_ID);
				if (id != null) {
					// Ignore.
				}
			}
		}
	}

	/** Get minimized view ids from a perspective's fast view bars. */
	private Set<String> getMinimizedViews(IMemento perspectiveMemento) {
		IMemento fastViewBarsMemento = perspectiveMemento.getChild(TAG_FAST_VIEW_BARS);
		if (fastViewBarsMemento != null) {
			// 1. Get all fast views in fast view bars
			// 2. get all views in fast views
			// 3. collect view ids
			Set<String> result = Arrays.stream(fastViewBarsMemento.getChildren(TAG_FAST_VIEW_BAR))
					.map(fVB -> fVB.getChild(TAG_FAST_VIEWS)).filter(Objects::nonNull)
					.flatMap(fV -> Arrays.stream(fV.getChildren(TAG_VIEW))).map(v -> v.getString(TAG_ID))
					.filter(Objects::nonNull).collect(Collectors.toSet());
			return result;
		}
		return new HashSet<String>();
	}

	private void visitViews(final MWindow window, final IMemento memento) {
		List<MUIElement> sharedElements = window.getSharedElements();
		for (IMemento viewMemento : memento.getChildren(TAG_VIEW)) {
			String id = getString(viewMemento, TAG_ID);
			IMemento viewStateMemento = viewMemento.getChild(TAG_VIEW_STATE);

			// org.eclipse.ui.internal.ViewReference.ViewReference(ViewFactory,
			// String, String, IMemento)
			String name = getString(viewMemento, TAG_PART_NAME);

			MPart part = MBasicFactory.INSTANCE.createPart();
			part.setElementId(id);
			part.setLabel(name);
			part.setContributionURI(
					"bundleclass://org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityView"); //$NON-NLS-1$
			sharedElements.add(part);
			if (viewStateMemento instanceof XMLMemento) {
				part.getPersistedState().put("memento", toXML(viewStateMemento)); //$NON-NLS-1$
			}

			Map<String, String> properties = part.getProperties();
			IMemento propertiesMemento = viewMemento.getChild(TAG_PROPERTIES);
			if (propertiesMemento != null) {
				for (IMemento propertyMemento : propertiesMemento.getChildren(TAG_PROPERTY)) {
					properties.put(getID(propertiesMemento), propertyMemento.getTextData());
				}
			}
		}
	}

	private String toXML(final IMemento memento) {
		try {
			StringWriter writer = new StringWriter();
			((XMLMemento) memento).save(writer);
			writer.flush();
			return writer.toString();
		} catch (Throwable ex) {
			return null;
		}
	}

	private void visitEditors(final MWindow window, final IMemento memento) {
		IMemento areaMemento = memento.getChild(TAG_AREA);
		if (areaMemento != null) {
			MArea area = MAdvancedFactory.INSTANCE.createArea();
			area.setElementId(EDITOR_AREA_ID);
			window.getSharedElements().add(area);

			// Iterate over info elements in the editor area and create a part stack for
			// each one.
			// Afterwards, map editors to the part stacks and create a part for each editor
			Map<String, Info> partStackInfos = new LinkedHashMap<>();
			IMemento[] infoMementos = areaMemento.getChildren(TAG_INFO);
			for (int infoIndex = 0; infoIndex < infoMementos.length; infoIndex++) {
				IMemento infoMemento = infoMementos[infoIndex];

				Info info = new Info();
				info.partID = getString(infoMemento, TAG_PART);
				info.relativeID = getString(infoMemento, TAG_RELATIVE);
				if (info.relativeID != null) {
					info.relationship = getInteger(infoMemento, TAG_RELATIONSHIP);
					info.ratio = getFloat(infoMemento, TAG_RATIO, 0.5f);
					info.left = getInteger(infoMemento, TAG_RATIO_LEFT);
					info.right = getInteger(infoMemento, TAG_RATIO_RIGHT);
				}
				// Use original part attribute as key (instead of part stack id) because this
				// will be needed later to find relative positioned part stacks.
				partStackInfos.put(info.partID, info);

				// Create part stack. First one has the primary id. Further ids are extended
				// with the index.
				String partStackId = PRIMARY_EDITOR_PART_STACK_ID;
				if (infoIndex > 0) {
					partStackId += "." + infoIndex; //$NON-NLS-1$
				}
				MPartStack partStack = MBasicFactory.INSTANCE.createPartStack();
				partStack.setElementId(partStackId);
				area.getChildren().add(partStack);
				info.part = partStack;
				if (infoIndex == 0) {
					// The primary editor part stack has the following tags.
					partStack.getTags().add(PRIMARY_EDITOR_PART_STACK_ID);
					partStack.getTags().add("EditorStack"); //$NON-NLS-1$
				}

				IMemento folderMemento = infoMemento.getChild(TAG_FOLDER);
				if (folderMemento != null) {
					int state = getInteger(folderMemento, TAG_EXPANDED);
					if (state != 0) {
						// Ignore.
					}

					for (IMemento presentationMemento : folderMemento.getChildren(TAG_PRESENTATION)) {
						String id = getString(presentationMemento, TAG_ID);
						if (id != null) {
							// Ignore.
						}
					}
				}
				// End of info sub elements

				// Add all matching editors as parts to the part stack
				// An e3 editor belongs to a stack if the editor's workbook attribute equals the
				// stack info's "part" attribute
				List<IMemento> relevantEditorMementos = Stream.of(memento.getChildren(TAG_EDITOR))
						.filter(e -> info.partID.equals(e.getString(TAG_WORKBOOK))).collect(Collectors.toList());
				for (IMemento editorMemento : relevantEditorMementos) {
					// Create part using the compatibility editor to render legacy editor
					MPart editorPart = MBasicFactory.INSTANCE.createPart();
					partStack.getChildren().add(editorPart);
					editorPart.setElementId("org.eclipse.e4.ui.compatibility.editor"); //$NON-NLS-1$
					editorPart.setContributionURI(
							"bundleclass://org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor"); //$NON-NLS-1$
					editorPart.setCloseable(true);
					// Add editor memento to the parts persisted state. The compatibility editor
					// uses this to create the correct editor.
					editorPart.getPersistedState().put("memento", editorMemento.toString()); //$NON-NLS-1$
					// Add tags marking as an editor, remove on hide, and the editor id
					editorPart.getTags().add("Editor"); //$NON-NLS-1$
					editorPart.getTags().add(EPartService.REMOVE_ON_HIDE_TAG);
					editorPart.getTags().add(editorMemento.getString(TAG_ID));

					// Set name and tooltip (both are allowed to be null)
					String partName = getString(editorMemento, TAG_PART_NAME);
					if (partName != null) {
						editorPart.setLabel(partName);
					} else {
						editorPart.setLabel(getString(editorMemento, TAG_TITLE));
					}
					editorPart.setTooltip(getString(editorMemento, TAG_TOOLTIP));

					if (getBoolean(editorMemento, TAG_ACTIVE_PART)) {
						editorPart.getTags().add(IPresentationEngine.ACTIVE);
					}

					// Focused in e3 means the editor is the selected element in its parent stack
					if (getBoolean(editorMemento, TAG_FOCUS)) {
						partStack.setSelectedElement(editorPart);
					}
				}
			}

			// (Re-)insert part stacks into editor area to layout them properly
			ModeledPageLayoutUtils modeledPageLayoutUtils = new org.eclipse.ui.internal.e4.compatibility.ModeledPageLayoutUtils(
					CommonUtil.getEModelService());
			for (Info info : partStackInfos.values()) {
				Info relativePart = partStackInfos.get(info.relativeID);
				if (relativePart != null) {
					modeledPageLayoutUtils.insert(info.part, relativePart.part,
							modeledPageLayoutUtils.plRelToSwt(info.relationship), info.ratio);
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void visitPerspectiveBar(final IMemento memento) {
		// Ignore.
	}

	private void visited(final IMemento memento, final String tag) {
		Object key = key(memento);
		Set<String> set = visited.get(key);
		if (set == null) {
			set = new LinkedHashSet<>();
			visited.put(key, set);
		}

		if (tag != null) {
			set.add(tag);
		}
	}

	private static Object key(final IMemento memento) {
		try {
			Field field = memento.getClass().getDeclaredField("element"); //$NON-NLS-1$
			field.setAccessible(true);
			return field.get(memento);
		} catch (Exception ex) {
			return null;
		}
	}

	private float getFloat(final IMemento memento, final String tag, final float defaultValue) {
		visited(memento, tag);
		Float value = memento.getFloat(tag);
		return value == null ? defaultValue : value.floatValue();
	}

	private int getInteger(final IMemento memento, final String tag) {
		return getInteger(memento, tag, 0);
	}

	private int getInteger(final IMemento memento, final String tag, final int defaultValue) {
		visited(memento, tag);
		Integer value = memento.getInteger(tag);
		return value == null ? defaultValue : value.intValue();
	}

	private boolean getBoolean(final IMemento memento, final String tag) {
		visited(memento, tag);
		Boolean value = memento.getBoolean(tag);
		return value != null && value.booleanValue();
	}

	private String getString(final IMemento memento, final String tag) {
		return getString(memento, tag, null);
	}

	private String getString(final IMemento memento, final String tag, final String defaultValue) {
		visited(memento, tag);
		String value = memento.getString(tag);
		return value == null ? defaultValue : value;
	}

	private String getID(final IMemento memento) {
		return getString(memento, TAG_ID);
	}

	private static class Info {

		public String partID;

		public String relativeID;

		public int relationship;

		public int left;

		public int right;

		public float ratio = 0.5f;

		public MPartSashContainerElement part;

		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("Info [partID=").append(partID); //$NON-NLS-1$
			if (relativeID != null) {
				result.append(", relativeID=").append(relativeID); //$NON-NLS-1$
				result.append(", relationship=").append(relationship); //$NON-NLS-1$
				result.append(", left=").append(left); //$NON-NLS-1$
				result.append(", right=").append(right); //$NON-NLS-1$
				result.append(", ratio=").append(ratio); //$NON-NLS-1$
			}
			result.append(']');

			return result.toString();
		}
	}
}
