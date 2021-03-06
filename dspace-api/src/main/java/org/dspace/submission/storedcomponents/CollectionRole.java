package org.dspace.submission.storedcomponents;

import org.dspace.core.Context;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRowIterator;
import org.dspace.authorize.AuthorizeException;
import org.dspace.eperson.Group;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 */
/*
 * Represents a workflow assignments database representation.
 * These assignments describe roles and the groups connected
 * to these roles for each collection
 */
public class CollectionRole {
    /** Our context */
    private Context myContext;

    /** The row in the table representing this object */
    private TableRow myRow;

    /**
     * Construct an ResourcePolicy
     *
     * @param context
     *            the context this object exists in
     * @param row
     *            the corresponding row in the table
     */
    CollectionRole(Context context, TableRow row)
    {
        myContext = context;
        myRow = row;
    }

    public static CollectionRole find(Context context, int id)
            throws SQLException
    {
        TableRow row = DatabaseManager.find(context, "xmlwf_collectionrole", id);

        if (row == null)
        {
            return null;
        }
        else
        {
            return new CollectionRole(context, row);
        }
    }

    public static CollectionRole find(Context context, int collection, int role) throws SQLException {
         TableRowIterator tri = DatabaseManager.queryTable(context,"xmlwf_collectionrole",
                "SELECT * FROM xmlwf_collectionrole WHERE collection_id="+collection+" AND role_id= ? ",
                role);

        TableRow row = null;
        if (tri.hasNext())
        {
            row = tri.next();
        }

        // close the TableRowIterator to free up resources
        tri.close();

        if (row == null)
        {
            return null;
        }
        else
        {
            return new CollectionRole(context, row);
        }
    }

    public static CollectionRole[] findByCollection(Context context, int collection) throws SQLException {
         TableRowIterator tri = DatabaseManager.queryTable(context,"xmlwf_collectionrole",
                "SELECT * FROM xmlwf_collectionrole WHERE collection_id=?", collection);

        List<CollectionRole> collectionRoles = new ArrayList<CollectionRole>();

        try
        {
            while (tri.hasNext())
            {
                TableRow row = tri.next();

                collectionRoles.add(new CollectionRole(context, row));
            }
        }
        finally
        {
            // close the TableRowIterator to free up resources
            if (tri != null)
                tri.close();
        }

        return collectionRoles.toArray(new CollectionRole[collectionRoles.size()]);
    }

    public static CollectionRole create(Context context) throws SQLException,
            AuthorizeException {

        TableRow row = DatabaseManager.create(context, "xmlwf_collectionrole");

        return new CollectionRole(context, row);
    }


    public void delete() throws SQLException
    {
        DatabaseManager.delete(myContext, myRow);
    }


    public void update() throws SQLException
    {
        DatabaseManager.update(myContext, myRow);
    }

    public void setRoleId(String id){
        myRow.setColumn("role_id",id);
    }

    public String getRoleId(){
        return myRow.getStringColumn("role_id");
    }

    public void setCollectionId(int id){
        myRow.setColumn("collection_id", id);
    }

    public int getCollectionId(){
        return myRow.getIntColumn("collection_id");
    }

    public void setGroupId(Group group){
        myRow.setColumn("group_id", group.getID());
    }

    public Group getGroup() throws SQLException {
        return Group.find(myContext, myRow.getIntColumn("group_id"));
    }

}
