package org.dspace.submission;

import org.dspace.content.WorkspaceItem;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.submission.storedcomponents.CollectionRole;
import org.dspace.submission.storedcomponents.WorkflowItemRole;


import java.sql.SQLException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 16-aug-2010
 * Time: 16:25:52
 */
public class Role {

    private String id;
    private String name;
    private String description;
    private boolean isInternal;
    private Scope scope;

    public static enum Scope{
        REPOSITORY,
        COLLECTION,
        ITEM
    }

    public Role(String name, String description, boolean isInternal, Scope scope){
        this.name = name;
        this.description = description;
        this.isInternal = isInternal;
        this.scope = scope;
    }

    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public String getDescription() {
        return description;
    }

    public boolean isInternal() {
        return isInternal;
    }

    public Scope getScope() {
        return scope;
    }

    public RoleMembers getMembers(Context context, WorkspaceItem wfi) throws SQLException {
        if(scope == Scope.REPOSITORY){
            Group group = Group.findByName(context, name);
            if(group == null)
                return new RoleMembers();
            else{
                RoleMembers assignees =  new RoleMembers();
                assignees.addGroup(group);
                return assignees;
            }
        } else
        if(scope == Scope.COLLECTION){
            CollectionRole collectionRole = CollectionRole.find(context,wfi.getCollection().getID(),id);
            if(collectionRole != null){
                RoleMembers assignees =  new RoleMembers();
                assignees.addGroup(collectionRole.getGroup());
                return assignees;
            }
            return new RoleMembers();
        }else{
            WorkflowItemRole[] roles = WorkflowItemRole.find(context, wfi.getID(), id);
            RoleMembers assignees = new RoleMembers();
            for (WorkflowItemRole itemRole : roles){
                EPerson user = itemRole.getEPerson();
                if(user != null)
                    assignees.addEPerson(user);

                Group group = itemRole.getGroup();
                if(group != null)
                    assignees.addGroup(group);
            }

            return assignees;
        }
    }

}
