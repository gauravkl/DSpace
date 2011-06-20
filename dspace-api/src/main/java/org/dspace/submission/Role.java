package org.dspace.submission;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;
import org.dspace.submission.storedcomponents.CollectionRole;
import org.dspace.submission.storedcomponents.WorkflowItemRole;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 16-aug-2010
 * Time: 16:25:52
 */
public class Role {

     /** log4j logger */
    private static Logger log = Logger.getLogger(Role.class);
    private int id;
    private String name;
    private String description;
    private boolean isInternal;
    private Scope scope;
    private TableRow row;

 // cache of process by ID (Integer)
    private static HashMap id2role = null;
    
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
    public Role(TableRow row){
		if (row != null)
		{
		  this.id = row.getIntColumn("process_id");
          this.name = row.getStringColumn("name");
          this.description = row.getStringColumn("description");
          this.scope = Scope.valueOf(row.getStringColumn("scope"));
		  this.row = row;
		}
	}

    public int getId() {
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
    public void create(Context context) throws SQLException,
            AuthorizeException
		{
			// Check authorisation: Only admins may create new process
			if (!AuthorizeManager.isAdmin(context))
			{
				throw new AuthorizeException(
						"Only administrators may modify the role registry");
			}

			// Create a table row and update it with the values
			row = DatabaseManager.create(context, "role");
            row.setColumn("description",description);
            row.setColumn("name",name);
            row.setColumn("scope",scope.toString());
			DatabaseManager.update(context, row);

			// Remember the new row number
			this.id = row.getIntColumn("role_id");

			log
					.info(LogManager.getHeader(context, "create_roles",
                            "role_id="
                                    + row.getIntColumn("role_id")));
		}

     public void update(Context context) throws SQLException,
            AuthorizeException
		{
			// Check authorisation: Only admins may create new process
			if (!AuthorizeManager.isAdmin(context))
			{
				throw new AuthorizeException(
						"Only administrators may modify the role registry");
			}

			// Create a table row and update it with the values
			row.setColumn("description",description);
            row.setColumn("name",name);
            row.setColumn("scope",scope.toString());
			DatabaseManager.update(context, row);

			log
					.info(LogManager.getHeader(context, "create_roles",
                            "role_id="
                                    + row.getIntColumn("role_id")));
		}
    
     public void delete(Context context) throws SQLException, AuthorizeException
           {
               // Check authorisation: Only admins may create DC types
               if (!AuthorizeManager.isAdmin(context))
               {
                   throw new AuthorizeException(
                           "Only administrators may modify the role registry");
               }

               log.info(LogManager.getHeader(context, "delete_role",
                       "process_id=" + getId()));

               DatabaseManager.delete(context, row);
           }
    
     public static Role find(Context context, int id)
                   throws SQLException
           {   decache();
               initCache(context);
               Integer iid = new Integer(id);

               // sanity check
               if (!id2role.containsKey(iid))
                   return null;

               return (Role) id2role.get(iid);
           }
    private static void decache()
              {
                  id2role = null;

              }

              //load caches if necessary
    private static void initCache(Context context) throws SQLException
              {
                  if (id2role != null )
                      return;

                  synchronized (Role.class)
                  {
                      if (id2role == null )
                      {
                          log.info("Loading form cache for fast finds");
                          HashMap new_id2role = new HashMap();

                          TableRowIterator tri = DatabaseManager.queryTable(context,"Role",
                                  "SELECT * from Role");

                          try
                          {
                              while (tri.hasNext())
                              {
                                  TableRow row = tri.next();

                                  Role role = new Role(row);
                                  new_id2role.put(new Integer(role.id), role);

                              }
                          }
                          finally
                          {
                              // close the TableRowIterator to free up resources
                              if (tri != null)
                                  tri.close();
                          }

                          id2role = new_id2role;

                      }
                  }
              }

}
