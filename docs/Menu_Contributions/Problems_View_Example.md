Menu Contributions/Problems View Example
========================================

Contents
--------

*   [1 Add ProblemView menus](#Add-ProblemView-menus)
    *   [1.1 Commands](#Commands)
    *   [1.2 Handlers](#Handlers)
    *   [1.3 Menus](#Menus)
    *   [1.4 Menus API](#Menus-API)

Add ProblemView menus
=====================

Add the Problems view menus. 
The Problems view has one toolbar action and in the view menu, 3 actions and 2 dynamic submenus. 
It also has a dynamic menu and another bunch of actions in its context menu.

Commands
--------

First define commands that are specific to the view. 
Since these are view commands, we can specify a default handler ... we're unlikely to replace it.

        <extension point="org.eclipse.ui.commands">
            <category id="org.eclipse.ui.views.problems"
        name="%ProblemView.category.name">
            </category>
            <command categoryId="org.eclipse.ui.views.problems"
        defaultHandler="org.eclipse.ui.views.markers.internal.TableSortHandler"
        description="%ProblemView.Sorting.description"
        id="org.eclipse.ui.views.problems.sorting"
        name="%ProblemView.Sorting.name">
            </command>
            <!\-\- the view preference command would probably be defined once
        with the other preference contributions -->
            <command categoryId="org.eclipse.ui.views.problems"
        defaultHandler="org.eclipse.ui.preferences.ViewPreferencesHandler"
        description="%ViewPreferences.description"
        id="org.eclipse.ui.preferences.viewPreferences"
        name="%ViewPreferences.name">
                <commandParameter id="markerEnablementName"
        name="%ViewPreferences.markerEnablementName.name"
        optional="false" />
                <commandParameter id="markerLimitName"
        name="%ViewPreferences.markerLimitName.name"
        optional="false" />
            </command>
            <command categoryId="org.eclipse.ui.views.problems"
        defaultHandler="org.eclipse.ui.views.markers.internal.FiltersHandler"
        description="%ProblemView.ConfigureFilters.description"
        id="org.eclipse.ui.views.problems.configureFilters"
        name="%ProblemView.ConfigureFilters.name">
            </command>
            <command categoryId="org.eclipse.ui.views.problems"
        defaultHandler="org.eclipse.ui.views.markers.internal.OpenMarkerHandler"
        description="%ProblemView.GoTo.description"
        id="org.eclipse.ui.views.problems.goTo"
        name="%ProblemView.GoTo.name" />
        </extension>

Handlers
--------

We can also use a number of global commands, like copy, paste, delete, quick fix, and properties. 
For these, we just need to define our handlers. 
We need to add them with `<activeWhen/>` clauses to restrict them to being active when the view is active.

        <extension point="org.eclipse.ui.handlers">
            <handler commandId="org.eclipse.ui.edit.copy"
        class="org.eclipse.ui.views.markers.internal.CopyMarkerHandler">
                <enabledWhen>
                    <not>
                        <count value="0" />
                    </not>
                </enabledWhen>
                <activeWhen>
                    <with variable="activePartId">
                        <equals value="org.eclipse.ui.views.ProblemView" />
                    </with>
                </activeWhen>
            </handler>
            <handler commandId="org.eclipse.ui.edit.paste"
        class="org.eclipse.ui.views.markers.internal.PasteMarkerHandler">
                <enabledWhen>
                    <not>
                        <count value="0" />
                    </not>
                </enabledWhen>
                <activeWhen>
                    <with variable="activePartId">
                        <equals value="org.eclipse.ui.views.ProblemView" />
                    </with>
                </activeWhen>
            </handler>
            <handler commandId="org.eclipse.ui.edit.delete"
        class="org.eclipse.ui.views.markers.internal.RemoveMarkerHandler">
                <enabledWhen>
                    <not>
                        <count value="0" />
                    </not>
                </enabledWhen>
                <activeWhen>
                    <with variable="activePartId">
                        <equals value="org.eclipse.ui.views.ProblemView" />
                    </with>
                </activeWhen>
            </handler>
            <handler commandId="org.eclipse.ui.edit.selectAll"
        class="org.eclipse.ui.views.markers.internal.SelectAllMarkersHandler">
                <enabledWhen>
                    <not>
                        <count value="0" />
                    </not>
                </enabledWhen>
                <activeWhen>
                    <with variable="activePartId">
                        <equals value="org.eclipse.ui.views.ProblemView" />
                    </with>
                </activeWhen>
            </handler>
            <handler commandId="org.eclipse.jdt.ui.edit.text.java.correction.assist.proposals"
        class="org.eclipse.ui.views.markers.internal.ResolveMarkerHandler">
                <enabledWhen>
                    <not>
                        <count value="0" />
                    </not>
                </enabledWhen>
                <activeWhen>
                    <with variable="activePartId">
                        <equals value="org.eclipse.ui.views.ProblemView" />
                    </with>
                </activeWhen>
            </handler>
            <handler commandId="org.eclipse.ui.file.properties"
        class="org.eclipse.ui.views.markers.internal.ProblemPropertiesHandler">
                <enabledWhen>
                    <not>
                        <count value="0" />
                    </not>
                </enabledWhen>
                <activeWhen>
                    <with variable="activePartId">
                        <equals value="org.eclipse.ui.views.ProblemView" />
                    </with>
                </activeWhen>
            </handler>
        </extension>

Or we can programmatically activate them through the IHandlerService which we would retrieve from the ProblemView site.

		IHandlerService handlerServ = (IHandlerService)getSite().getService(IHandlerService.class);
		CopyMarkerHandler copy = new CopyMarkerHandler();
		handlerServ.activateHandler("org.eclipse.ui.edit.copy", copy);

Using the ProblemView site to access the IHandlerService handles the `<activeWhen/>` clause for us, and our programmatic handler would manage its own enablement state.

Menus
-----

Then we would define the ProblemView menu structures. 
We are using 3 **roots**: the view menu, the view toolbar, and the view context menu. 
This is an example of an "in-place" menu definition. The `<menuContribution/>` location attribute is a URI that defines the starting point for inserting the menu elements. 
The XML hierarchy mirrors the menu hierarchy, in that you can define items and menus within the body of other menus.

        <extension point="org.eclipse.ui.menus">
            <menuContribution locationURI="menu:org.eclipse.ui.views.ProblemView">
                <command commandId="org.eclipse.ui.views.problems.sorting"
        label="%ProblemView.Sorting.name"
        mnemonic="%ProblemView.Sorting.mnemonic"
        style="push"
        tooltip="%ProblemView.Sorting.tooltip">
                </command>
                <menu id="org.eclipse.ui.views.problems.groupBy.menu"
        label="%ProblemView.GroupBy.label"
        mnemonic="%ProblemView.GroupBy.mnemonic">
                    <dynamic class="org.eclipse.ui.views.markers.internal.GroupByItems"
        id="org.eclipse.ui.views.problems.groupBy.items">
                    </dynamic>
                </menu>
                <separator name="group.filter" visible="true" />
                <menu id="org.eclipse.ui.views.problems.filters.menu"
        label="%ProblemView.Filters.label"
        mnemonic="%ProblemView.Filters.mnemonic">
                    <dynamic class="org.eclipse.ui.views.markers.internal.FilterItems"
        id="org.eclipse.ui.views.problems.filters.items" />
                </menu>
                <command commandId="org.eclipse.ui.views.problems.configureFilters"
        icon="$nl$/elcl16/filter_ps.gif"
        label="%ProblemView.ConfigureFilters.name"
        mnemonic="%ProblemView.ConfigureFilters.mnemonic"
        tooltip="%ProblemView.ConfigureFilters.tooltip" />
                <command commandId="org.eclipse.ui.preferences.viewPreferences"
        label="%ViewPreferences.name"
        mnemonic="%ViewPreferences.mnemonic">
                    <parameter name="markerEnablementName"
        value="LIMIT_PROBLEMS" />
                    <parameter name="markerLimitName"
        value="PROBLEMS_LIMIT" />
                </command>
            </menuContribution>
            <menuContribution locationURI="toolbar:org.eclipse.ui.views.ProblemView">
                <command commandId="org.eclipse.ui.views.problems.configureFilters"
        icon="$nl$/elcl16/filter_ps.gif"
        tooltip="%ProblemView.ConfigureFilters.tooltip" />
            </menuContribution>
            <menuContribution locationURI="popup:org.eclipse.ui.views.ProblemView">
                <command commandId="org.eclipse.ui.views.problems.goTo"
        mnemonic="%ProblemView.GoTo.mnemonic"
        icon="$nl$/elcl16/gotoobj_tsk.gif"
        disabledIcon="$nl$/dlcl16/gotoobj_tsk.gif"
        tooltip="%ProblemView.GoTo.tooltip" />
                <separator name="group.showIn"
        visible="true" />
                <menu id="org.eclipse.ui.views.problems.showIn.menu"
        label="%ProblemView.ShowIn.label"
        mnemonic="%ProblemView.ShowIn.mnemonic">
                    <dynamic class="org.eclipse.ui.actions.ShowInContributions"
        id="org.eclipse.ui.views.problems.showIn.items" />
                </menu>
                <separator name="group.edit"
        visible="true" />
                <command commandId="org.eclipse.ui.edit.copy"
        mnemonic="%ProblemView.copy.mnemonic"
        icon="$nl$/icons/full/etool16/copy_edit.gif"
        disabledIcon="$nl$/icons/full/dtool16/copy_edit.gif" />
                <command commandId="org.eclipse.ui.edit.paste"
        mnemonic="%ProblemView.paste.mnemonic"
        icon="$nl$/icons/full/etool16/paste_edit.gif"
        disabledIcon="$nl$/icons/full/dtool16/paste_edit.gif" />
                <command commandId="org.eclipse.ui.edit.delete"
        mnemonic="%ProblemView.delete.mnemonic"
        icon="$nl$/icons/full/etool16/delete_edit.gif"
        disabledIcon="$nl$/icons/full/dtool16/delete_edit.gif">
                    <visibleWhen>
                        <not>
                            <with variable="activePartId">
                                <equals value="org.eclipse.ui.views.ProblemView" />
                            </with>
                        </not>
                    </visibleWhen>
                </command>
                <command commandId="org.eclipse.ui.edit.selectAll"
        mnemonic="%ProblemView.selectAll.mnemonic" />
                <separator name="group.resolve"
        visible="true" />
                <command commandId="org.eclipse.jdt.ui.edit.text.java.correction.assist.proposals"
        mnemonic="%ProblemView.Resolve.mnemonic"
        icon="$nl$/icons/full/elcl16/smartmode_co.gif"
        disabledIcon="$nl$/icons/full/dlcl16/smartmode_co.gif" />
                <separator name="additions"
        visible="false" />
                <separator name="group.properties"
        visible="true" />
                <command commandId="org.eclipse.ui.file.properties"
        mnemonic="%ProblemView.Properties.mnemonic" />
            </menuContribution>
        </extension>

Menus API
---------

We can contribute menu definitions through the IMenuService API.

The above example can be done for the view menus:

        public void addProblemsViewMenuContribution() {
            IMenuService menuService = (IMenuService) PlatformUI.getWorkbench()
                    .getService(IMenuService.class);
         
            AbstractContributionFactory viewMenuAddition = new AbstractContributionFactory(
                    "menu:org.eclipse.ui.views.ProblemView?after=additions") {
                public void createContributionItems(IMenuService menuService,
                        List additions) {
                    CommandContributionItem item = new CommandContributionItem(
                            null, "org.eclipse.ui.views.problems.sorting", null,
                            null, null, null, "Sorting...", "S",
                            "Change the Sort order",
                            CommandContributionItem.STYLE_PUSH);
                    additions.add(item);
         
                    MenuManager submenu = new MenuManager("Group &By",
                            "org.eclipse.ui.views.problems.groupBy.menu");
                    IContributionItem dynamicItem = new CompoundContributionItem(
                            "org.eclipse.ui.views.problems.groupBy.items") {
                        protected IContributionItem\[\] getContributionItems() {
                            // Here's where you would dynamically generate your list
                            IContributionItem\[\] list = new IContributionItem\[2\];
                            Map parms = new HashMap();
                            parms.put("groupBy", "Severity");
                            list\[0\] = new CommandContributionItem(null,
                                    "org.eclipse.ui.views.problems.grouping",
                                    parms, null, null, null, "Severity", null,
                                    null, CommandContributionItem.STYLE_PUSH);
         
                            parms = new HashMap();
                            parms.put("groupBy", "None");
                            list\[1\] = new CommandContributionItem(null,
                                    "org.eclipse.ui.views.problems.grouping",
                                    parms, null, null, null, "None", null, null,
                                    CommandContributionItem.STYLE_PUSH);
                            return list;
                        }
                    };
                    submenu.add(dynamicItem);
         
                    additions.add(submenu);
                    additions.add(new Separator("group.filter"));
         
                    submenu = new MenuManager("&Filters",
                            "org.eclipse.ui.views.problems.filters.menu");
                    dynamicItem = new CompoundContributionItem(
                            "org.eclipse.ui.views.problems.filters.items") {
                        protected IContributionItem\[\] getContributionItems() {
                            // Here's where you would dynamically generate your list
                            IContributionItem\[\] list = new IContributionItem\[1\];
                            Map parms = new HashMap();
                            parms.put("filter", "Default");
                            list\[0\] = new CommandContributionItem(null,
                                    "org.eclipse.ui.views.problems.filters", parms,
                                    null, null, null, "Default", null, null,
                                    CommandContributionItem.STYLE_PUSH);
                            return list;
                        }
                    };
                    submenu.add(dynamicItem);
         
                    additions.add(submenu);
         
                    ImageDescriptor filterIcon = PlatformUI.getWorkbench()
                            .getSharedImages().getImageDescriptor(
                                    "elcl16/filter_ps.gif");
                    item = new CommandContributionItem(null,
                            "org.eclipse.ui.views.problems.configureFilters", null,
                            filterIcon, null, null, "Configure Filters...", "C",
                            "Configure the filters to be applied to this view",
                            CommandContributionItem.STYLE_PUSH);
                    additions.add(item);
         
                    Map parms = new HashMap();
                    parms.put("markerEnablementName", "LIMIT_PROBLEMS");
                    parms.put("markerLimitName", "PROBLEMS_LIMIT");
                    item = new CommandContributionItem(null,
                            "org.eclipse.ui.preferences.viewPreferences", parms,
                            null, null, null, "Preference", "P",
                            "Open the preference dialog",
                            CommandContributionItem.STYLE_PUSH);
                    additions.add(item);
                }
         
                public void releaseContributionItems(IMenuService menuService,
                        List items) {
                    // for us this is a no-op
                }
            };
            menuService.addContributionFactory(viewMenuAddition);
        }

The `AbstractContributionFactory` creates new contribution items every time `createContributionItems(List)` is called. 
The factory location tells the framework where to insert the contributions when populating `ContributionManager`s.

