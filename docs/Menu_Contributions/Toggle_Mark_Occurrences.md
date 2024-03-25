Menu Contributions/Toggle Mark Occurrences
==========================================

Contents
--------

*   [1 Add Toggle Mark Occurrences to main toolbar](#Add-Toggle-Mark-Occurrences-to-main-toolbar)
    *   [1.1 Commands](#Commands)
    *   [1.2 Handlers](#Handlers)
    *   [1.3 Menus](#Menus)
    *   [1.4 Menus API](#Menus-API)

Add Toggle Mark Occurrences to main toolbar
===========================================

We can provide the Toggle Mark Occurrences toolbar button. It's normally contributed through an actionSet as a retargettable action, and the Java and Class File editors \*EditorActionBarContributors provide the implementation ToggleMarkOccurrencesAction through IActionBars#setGlobalActionHandler(\*).

Commands
--------

In 3.3 the enablement is tied to the command, and for the other behaviours we allow the active handler to update the UI presentation.

The command service keeps a list of registered UI elements, which can be updated by the active handler. The checked state can be updated through UIElement#setChecked(boolean);

    private boolean isChecked() {
    	return getStore().getBoolean(
    			PreferenceConstants.EDITOR_MARK_OCCURRENCES);
    }
    
    public void updateElement(UIElement element, Map parameters) {
    	element.setChecked(isChecked());
    }
    

 

When the toggle handler runs, it can request that any UI elements have their appearance updated from its execute(*) method:

 

    	ICommandService service = (ICommandService) serviceLocator
    			.getService(ICommandService.class);
    	service.refreshElements(
    			IJavaEditorActionDefinitionIds.TOGGLE_MARK_OCCURRENCES,
    			null);
    

 

  

Handlers
--------

This command doesn't have a default handler, as it only applies to specific editors that are provided. So we would provide the handler for the java editor.

    <extension
          point="org.eclipse.ui.handlers">
       <handler
             class="org.eclipse.jdt.internal.ui.javaeditor.ToggleMarkOccurrencesHandler"
             commandId="org.eclipse.jdt.ui.edit.text.java.toggleMarkOccurrences"
             helpContextId="org.eclipse.jdt.ui.toggle_mark_occurrences_action_context">
          <activeWhen>
             <with
                   variable="activeEditorId">
                <or>
                   <equals
                         value="org.eclipse.jdt.ui.CompilationUnitEditor">
                   </equals>
                   <equals
                         value="org.eclipse.jdt.ui.ClassFileEditor">
                   </equals>
                </or>
             </with>
          </activeWhen>
       </handler>
    </extension>
    

 

  
We're active for both the Java editor and the Class File editor. There is also the option to programmatically install the handler.

    AndExpression expr = new AndExperssion();
    expr.add(new ActivePartIdExpression("org.eclipse.jdt.ui.CompilationUnitEditor"));
    expr.add(new ActivePartIdExpression("org.eclipse.jdt.ui.ClassFileEditor"));
    IHandlerService handlerServ = (IHandlerService)getSite().getWorkbenchWindow().getService(IHandlerService.class);
    toggleOccurrencesHandler = new ToggleMarkOccurrencesHandler();
    handlerServ.activateHandler("org.eclipse.jdt.ui.edit.text.java.toggleMarkOccurrences", toggleOccurrencesHandler, expr);
    

 

Since the same handler is valid for both editors, we install it with a specific expression and **don't** tie the activation to the part site. But as written, the `toggleOccurrencesHandler` will exist as long as the workbench window exists.

Menus
-----

In **3.3M5** ActionSets generate and update active contexts.

  

 

      <extension
            point="org.eclipse.ui.menus">
         <menuContribution
               locationURI="toolbar:org.eclipse.ui.main.toolbar?after=additions">
            <toolbar
                  id="org.eclipse.ui.edit.text.actionSet.presentation">
               <command
                     commandId="org.eclipse.jdt.ui.edit.text.java.toggleMarkOccurrences"
                     disabledIcon="$nl$/icons/full/dtool16/mark_occurrences.gif"
                     helpContextId="toggle_mark_occurrences_action_context"
                     icon="$nl$/icons/full/etool16/mark_occurrences.gif"
                     id="org.eclipse.jdt.ui.edit.text.java.toggleMarkOccurrences"
                     label="%toggleMarkOccurrences.label"
                     style="toggle"
                     tooltip="%toggleMarkOccurrences.tooltip">
                  <visibleWhen
                        checkEnabled="false">
                     <with
                           variable="activeContexts">
                        <iterate
                              operator="or">
                           <equals
                                 value="org.eclipse.jdt.ui.text.java.actionSet.presentation">
                           </equals>
                        </iterate>
                     </with>
                  </visibleWhen>
               </command>
            </toolbar>
         </menuContribution>
      </extension>
    

 

  
This item is also tied to an actionSet.

Menus API
---------

The above XML can be done using the menus API:

    public static void createToggleMarkOccurrences() {
        final IMenuService menuService = (IMenuService) PlatformUI
                .getWorkbench().getService(IMenuService.class);
        final ImageDescriptor markOccurDesc = AbstractUIPlugin
                .imageDescriptorFromPlugin("org.eclise.ui.tests",
                        "icons/full/etool16/mark_occurrences.gif");
        final ImageDescriptor disabledMarkOccurDesc = AbstractUIPlugin
                .imageDescriptorFromPlugin("org.eclise.ui.tests",
                        "icons/full/dtool16/mark_occurrences.gif");
    
        AbstractContributionFactory contribution = new AbstractContributionFactory(
                "toolbar:org.eclipse.ui.edit.text.actionSet.presentation?after=Presentation") {
            public void createContributionItems(IMenuService menuService,
                    List additions) {
                IContributionItem item = new CommandContributionItem(
                        null,
                        "org.eclipse.jdt.ui.edit.text.java.toggleMarkOccurrences",
                        null, markOccurDesc, disabledMarkOccurDesc, null, null,
                        null, "Toggle Mark Occurrences", CommandContributionItem.STYLE_CHECK);
                menuService
                        .registerVisibleWhen(
                                item,
                                new ActiveActionSetExpression(
                                        "org.eclipse.jdt.ui.text.java.actionSet.presentation"));
                additions.add(item);
            }
    
            public void releaseContributionItems(IMenuService menuService,
                    List items) {
            }
        };
        menuService.addContributionFactory(contribution);
    }
    

 

  
This asks for a toolbar root in the org.eclipse.ui.edit.text.actionSet.presentation toolbar after the Presentation id.

It's contributed with a visibleWhen clause `ActiveActionSetExpression("org.eclipse.jdt.ui.text.java.actionSet.presentation")`, so it will be visible when the actionSet is active.

The registerVisibleWhen() method might be changing in **3.3M6**

