Menu Contributions/RCP removes the Project menu
===============================================

Product removes the Project menu
================================

**Note:** this probably will not make it into 3.4

An RCP product wishes to remove the Project menu. It should be possible to override the visibility of menu contributions.

 

       public void addOverride() {
           // the RCP app would already have its product key
           Object productKey = null;
    
           IMenuService menuServ = (IMenuService) PlatformUI.getWorkbench()
                   .getActiveWorkbenchWindow().getService(IMenuService.class);
           menuServ.addOverride(productKey, "menu:project", new OverrideAdapter() {
               public Boolean getVisible() {
                   return Boolean.FALSE;
               }
           });
       }
    

 

  

The idea is to provide this ability at the product level. For example, an RCP app should be able to hide any menu items that it doesn't want but picked up through the inclusion of a plugin.

That implies that it might not be part of the general IMenuService interface. Or (taking a page from the IExtensionRegistry) it might use a token that's available from the WorkbenchWindowAdvisor so that products can use the interface, or even expose the ability to their users.

If it returns `null` the next level of visibility is evaluated. The `null` case is to keep it consistent with other overrides.

The override service is ID based. For items which haven't specified their ID, the override will be applied to the commandId (which is required on every item).

