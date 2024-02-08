Menu Contributions/Search Menu
==============================


Contents
--------

*   [1 Possible Java Search Menu Example](#Possible-Java-Search-Menu-Example)
    *   [1.1 ActionSet context](#ActionSet-context)
    *   [1.2 Commands](#Commands)
    *   [1.3 Menus](#Menus)
    *   [1.4 Menus API](#Menus-API)

Possible Java Search Menu Example
=================================

**NOTE: This is only an example and does not work as is. The example assumes that Search switched to the new menu contribution story which it didn't so far (see [bug 213385](https://bugs.eclipse.org/bugs/show_bug.cgi?id=213385)).**

The java search menu items are added through a Java Search action set. They have code that enables/disables the action set depending on the active editor.

ActionSet context
-----------------

For something to go in an actionSet, then we would define the actionSet context. ActionSet contexts are only partially supported in **3.3M5**.

 

     <extension point="org.eclipse.ui.contexts">
       <context description="%JavaSearchActionSet.description"
                id="org.eclipse.jdt.ui.SearchActionSet"
                name="%JavaSearchActionSet.label"
                parentId="org.eclipse.ui.contexts.actionSet">
       </context>
     </extension>
    

 

Commands
--------

Also, a number of the items were retargetable actions that allow label updates. The active handler can update their appearance with an ICommandService@refreshElements(*) call.

 

     <extension point="org.eclipse.ui.commands">
       <command name="%ActionDefinition.readAccessInworkspace.name"
                description="%ActionDefinition.readAccessInWorkspace.description"
                categoryId="org.eclipse.search.ui.category.search"
                id="org.eclipse.jdt.ui.edit.text.java.search.read.access.in.workspace">
       </command>
       <command name="%ActionDefinition.readAccessInProject.name"
                description="%ActionDefinition.readAccessInProject.description"
                categoryId="org.eclipse.search.ui.category.search"
                id="org.eclipse.jdt.ui.edit.text.java.search.read.access.in.project">
       </command>
       <command name="%ActionDefinition.readAccessInHierarchy.name"
                description="%ActionDefinition.readAccessInHierarchy.description"
                categoryId="org.eclipse.search.ui.category.search"
                id="org.eclipse.jdt.ui.edit.text.java.search.read.access.in.hierarchy">
       </command>
       <command name="%ActionDefinition.readAccessInWorkingSet.name"
                description="%ActionDefinition.readAccessInWorkingSet.description"
                categoryId="org.eclipse.search.ui.category.search"
                id="org.eclipse.jdt.ui.edit.text.java.search.read.access.in.working.set">
       </command>
       <command name="%ActionDefinition.writeAccessInWorkspace.name"
                description="%ActionDefinition.writeAccessInWorkspace.description"
                categoryId="org.eclipse.search.ui.category.search"
                id="org.eclipse.jdt.ui.edit.text.java.search.write.access.in.workspace">
       </command>
       <command name="%ActionDefinition.writeAccessInProject.name"
                description="%ActionDefinition.writeAccessInProject.description"
                categoryId="org.eclipse.search.ui.category.search"
                id="org.eclipse.jdt.ui.edit.text.java.search.write.access.in.project">
       </command>
       <command name="%ActionDefinition.writeAccessInHierarchy.name"
                description="%ActionDefinition.writeAccessInHierarchy.description"
                categoryId="org.eclipse.search.ui.category.search"
                id="org.eclipse.jdt.ui.edit.text.java.search.write.access.in.hierarchy">
       </command>
       <command name="%ActionDefinition.writeAccessInWorkingSet.name"
                description="%ActionDefinition.writeAccessInWorkingSet.description"
                categoryId="org.eclipse.search.ui.category.search"
                id="org.eclipse.jdt.ui.edit.text.java.search.write.access.in.working.set">
       </command>
     </extension>
    

 

Menus
-----

We'll assume that the Search menu is globally defined elsewhere by the org.eclipse.search plugin.

 

      <extension point="org.eclipse.ui.menus">
         <menuContribution locationURI="menu:org.eclipse.ui.main.menu?after=navigate">
            <menu label="%searchMenu.label"
                  mnemonic="%searchMenu.mnemonic"
                  id="org.eclipse.search.menu">
               <separator name="internalDialogGroup" visible="false" />
               <separator name="dialogGroup" visible="false" />
               <separator name="fileSearchContextMenuActionsGroup"
                          visible="true" />
               <separator name="contextMenuActionsGroup" visible="true" />
               <separator name="occurencesActionsGroup" visible="true" />
               <separator name="extraSearchGroup" visible="true" />
            </menu>
         </menuContribution>
      </extension>
    

 

Then the JDT plugin would contribute the menu items to search, where the menuContribution location specifies the starting point for adding the menus. For groups of actions like the Write Access or Read Access shown here, they can just be specified in order. The <visibleWhen/> clauses must be specified on the items contributed if they want to belong to the actionSet, but if the contribute items are contain in a contributed menu, it can just be specified on the <menu/> element.

  

 

      <extension point="org.eclipse.ui.menus">
         <menuContribution locationURI="menu:org.eclipse.search.menu?after=dialogGroup">
            <command commandId="org.eclipse.jdt.internal.ui.search.openJavaSearchPage"
                     label="%openJavaSearchPageAction.label"
                     mnemonic="%openJavaSearchPageAction.mnemonic"
                     icon="$nl$/icons/full/obj16/jsearch_obj.gif"
                     helpContextId="java_search_action_context">
               <visibleWhen>
                  <with variable="activeContexts">
                     <iterate operator="or">
                        <equals value="org.eclipse.jdt.ui.SearchActionSet">
                        </equals>
                     </iterate>
                  </with>
               </visibleWhen>
            </command>
         </menuContribution>
         <menuContribution locationURI="menu:org.eclipse.search.menu?after=contextMenuActionsGroup">
            <menu id="readAccessSubMenu"
                  label="%readAccessSubMenu.label"
                  mnemonic="%readAccessSubMenu.mnemonic">
               <separator name="group1" visible="false" />
               <command commandId="org.eclipse.jdt.ui.edit.text.java.search.read.access.in.workspace"
                        label="%InWorkspace.label"
                        mnemonic="%InWorkspace.mnemonic">
               </command>
               <command commandId="org.eclipse.jdt.ui.edit.text.java.search.read.access.in.project"
                        label="%InProject.label"
                        mnemonic="%InProject.mnemonic">
               </command>
               <command commandId="org.eclipse.jdt.ui.edit.text.java.search.read.access.in.hierarchy"
                        label="%InHierarchy.label"
                        mnemonic="%InHierarchy.mnemonic">
               </command>
               <command commandId="org.eclipse.jdt.ui.edit.text.java.search.read.access.in.working.set"
                        label="%InWorkingSet.label"
                        mnemonic="%InWorkingSet.mnemonic">
               </command>
               <visibleWhen>
                  <with variable="activeContexts">
                     <iterate operator="or">
                        <equals value="org.eclipse.jdt.ui.SearchActionSet">
                        </equals>
                     </iterate>
                  </with>
               </visibleWhen>
            </menu>
            <menu id="writeAccessSubMenu"
                  label="%writeAccessSubMenu.label"
                  mnemonic="%writeAccessSubMenu.mnemonic">
               <separator name="group1" visible="false" />
               <command commandId="org.eclipse.jdt.ui.edit.text.java.search.write.access.in.workspace"
                        label="%InWorkspace.label"
                        mnemonic="%InWorkspace.mnemonic">
               </command>
               <command commandId="org.eclipse.jdt.ui.edit.text.java.search.write.access.in.project"
                        label="%InProject.label"
                        mnemonic="%InProject.mnemonic">
               </command>
               <command commandId="org.eclipse.jdt.ui.edit.text.java.search.write.access.in.hierarchy"
                        label="%InHierarchy.label"
                        mnemonic="%InHierarchy.mnemonic">
               </command>
               <command commandId="org.eclipse.jdt.ui.edit.text.java.search.write.access.in.working.set"
                        label="%InWorkingSet.label"
                        mnemonic="%InWorkingSet.mnemonic">
               </command>
               <visibleWhen>
                  <with variable="activeContexts">
                     <iterate operator="or">
                        <equals value="org.eclipse.jdt.ui.SearchActionSet">
                        </equals>
                     </iterate>
                  </with>
               </visibleWhen>
            </menu>
         </menuContribution>
         <menuContribution locationURI="menu:org.eclipse.search.menu?after=occurencesActionsGroup">
            <command commandId="org.eclipse.jdt.ui.edit.text.java.search.occurrences.in.file.quickMenu"
                     label="%occurrencesSubMenu.label">
               <visibleWhen>
                  <with variable="activeContexts">
                     <iterate operator="or">
                        <equals value="org.eclipse.jdt.ui.SearchActionSet">
                        </equals>
                     </iterate>
                  </with>
               </visibleWhen>
            </command>
         </menuContribution>
      </extension>
    

 

  
Currently, the java search menus are in the Java Search actionSet, that is dynamically enabled/disabled. This could also be done by specifying a visibleWhen like:

    <visibleWhen>
      <with variable="activeEditorId">
        <or>
          <equals value="org.eclipse.jdt.ui.CompilationUnitEditor" />
          <equals value="org.eclipse.jdt.ui.ClassFileEditor" />
        </or>
      </with>
    </visibleWhen>
    

 

This would make the visible if either the Java or Class File editor was the active editor, and they would disappear otherwise.

Menus API
---------

The API can be used to contribute to the main menu bar:

 

       public static void addSearchMenu() {
           IMenuService menuService = (IMenuService) PlatformUI.getWorkbench()
                   .getService(IMenuService.class);
    
           AbstractContributionFactory searchContribution = new AbstractContributionFactory(
                   "menu:org.eclipse.ui.main.menu?after=navigate") {
               public void createContributionItems(IMenuService menuService,
                       List additions) {
                   MenuManager search = new MenuManager("Se&arch",
                           "org.eclipse.search.menu");
    
                   search.add(new GroupMarker("internalDialogGroup"));
                   search.add(new GroupMarker("dialogGroup"));
                   search.add(new Separator("fileSearchContextMenuActionsGroup"));
                   search.add(new Separator("contextMenuActionsGroup"));
                   search.add(new Separator("occurencesActionsGroup"));
                   search.add(new Separator("extraSearchGroup"));
    
                   additions.add(search);
               }
    
               public void releaseContributionItems(IMenuService menuService,
                       List items) {
                   // nothing to do here
               }
           };
    
           menuService.addContributionFactory(searchContribution);
       }
    

 

  
It's just a menu inserted at the menu root location.

  
Then another plugin can contribute to the search menu:

 

       public static void addToSearchMenu() {
           final IMenuService menuService = (IMenuService) PlatformUI
                   .getWorkbench().getService(IMenuService.class);
           final ActiveActionSetExpression activeSearchActionSet = new ActiveActionSetExpression(
                   "org.eclipse.jdt.ui.SearchActionSet");
    
           final ImageDescriptor searchIcon = AbstractUIPlugin
                   .imageDescriptorFromPlugin("org.eclise.ui.tests",
                           "icons/full/obj16/jsearch_obj.gif");
           AbstractContributionFactory factory = new AbstractContributionFactory(
                   "menu:org.eclipse.search.menu?after=dialogGroup") {
               public void createContributionItems(IMenuService menuService,
                       List additions) {
                   CommandContributionItem item = new CommandContributionItem(
                           "org.eclipse.jdt.internal.ui.search.openJavaSearchPage",
                           "org.eclipse.jdt.internal.ui.search.openJavaSearchPage",
                           null, searchIcon, null, null, null, null, null,
                           CommandContributionItem.STYLE_PUSH);
                   menuService.registerVisibleWhen(item, activeSearchActionSet);
                   additions.add(item);
               }
    
               public void releaseContributionItems(IMenuService menuService,
                       List items) {
               }
           };
           menuService.addContributionFactory(factory);
    
           factory = new AbstractContributionFactory(
                   "menu:org.eclipse.search.menu?after=contextMenuActionsGroup") {
               public void createContributionItems(IMenuService menuService,
                       List additions) {
                   MenuManager readMenu = new MenuManager("&Read Access",
                           "readAccessSubMenu");
                   menuService
                           .registerVisibleWhen(readMenu, activeSearchActionSet);
                   additions.add(readMenu);
    
                   readMenu.add(new GroupMarker("group1"));
    
                   CommandContributionItem item = new CommandContributionItem(
                           "org.eclipse.jdt.ui.edit.text.java.search.read.access.in.workspace",
                           "org.eclipse.jdt.ui.edit.text.java.search.read.access.in.workspace",
                           null, null, null, null, null, "W", null,
                           CommandContributionItem.STYLE_PUSH);
                   readMenu.add(item);
                   item = new CommandContributionItem(
                           "org.eclipse.jdt.ui.edit.text.java.search.read.access.in.project",
                           "org.eclipse.jdt.ui.edit.text.java.search.read.access.in.project",
                           null, null, null, null, null, "P", null,
                           CommandContributionItem.STYLE_PUSH);
                   readMenu.add(item);
                   item = new CommandContributionItem(
                           "org.eclipse.jdt.ui.edit.text.java.search.read.access.in.hierarchy",
                           "org.eclipse.jdt.ui.edit.text.java.search.read.access.in.hierarchy",
                           null, null, null, null, null, "H", null,
                           CommandContributionItem.STYLE_PUSH);
                   readMenu.add(item);
                   item = new CommandContributionItem(
                           "org.eclipse.jdt.ui.edit.text.java.search.read.access.in.working.set",
                           "org.eclipse.jdt.ui.edit.text.java.search.read.access.in.working.set",
                           null, null, null, null, null, "S", null,
                           CommandContributionItem.STYLE_PUSH);
                   readMenu.add(item);
    
                   MenuManager writeMenu = new MenuManager("&Write Access",
                           "writeAccessSubMenu");
                   menuService.registerVisibleWhen(writeMenu,
                           activeSearchActionSet);
                   additions.add(writeMenu);
    
                   writeMenu.add(new GroupMarker("group1"));
    
                   item = new CommandContributionItem(
                           "org.eclipse.jdt.ui.edit.text.java.search.write.access.in.workspace",
                           "org.eclipse.jdt.ui.edit.text.java.search.write.access.in.workspace",
                           null, null, null, null, null, "W", null,
                           CommandContributionItem.STYLE_PUSH);
                   writeMenu.add(item);
                   item = new CommandContributionItem(
                           "org.eclipse.jdt.ui.edit.text.java.search.write.access.in.project",
                           "org.eclipse.jdt.ui.edit.text.java.search.write.access.in.project",
                           null, null, null, null, null, "P", null,
                           CommandContributionItem.STYLE_PUSH);
                   writeMenu.add(item);
                   item = new CommandContributionItem(
                           "org.eclipse.jdt.ui.edit.text.java.search.write.access.in.hierarchy",
                           "org.eclipse.jdt.ui.edit.text.java.search.write.access.in.hierarchy",
                           null, null, null, null, null, "H", null,
                           CommandContributionItem.STYLE_PUSH);
                   writeMenu.add(item);
                   item = new CommandContributionItem(
                           "org.eclipse.jdt.ui.edit.text.java.search.write.access.in.working.set",
                           "org.eclipse.jdt.ui.edit.text.java.search.write.access.in.working.set",
                           null, null, null, null, null, "S", null,
                           CommandContributionItem.STYLE_PUSH);
                   writeMenu.add(item);
               }
    
               public void releaseContributionItems(IMenuService menuService,
                       List items) {
               }
           };
           menuService.addContributionFactory(factory);
       }
    

 

  

When the main menu is populated, these contribution factories will be called.

