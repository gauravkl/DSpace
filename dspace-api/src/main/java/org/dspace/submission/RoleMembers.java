package org.dspace.submission;

import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: bram
 * Date: 2-mrt-2011
 * Time: 13:38:09
 * To change this template use File | Settings | File Templates.
 */
public class RoleMembers {

    private ArrayList<Group> groups;
    private ArrayList<EPerson> epersons;

    public RoleMembers(){
        this.groups = new ArrayList<Group>();
        this.epersons = new ArrayList<EPerson>();
    }

    public ArrayList<Group> getGroups(){
        return groups;
    }
    public ArrayList<EPerson> getEPersons(){
        return epersons;
    }

    public void addGroup(Group group){
        groups.add(group);
    }
    public void addEPerson(EPerson eperson){
        epersons.add(eperson);
    }
    public void removeEperson(int toRemoveID){
        for(EPerson eperson: epersons){
            if(eperson.getID()==toRemoveID)
                epersons.remove(epersons);
        }
    }
    public ArrayList<EPerson> getAllUniqueMembers(Context context) throws SQLException {
        HashMap<Integer, EPerson> epersonsMap = new HashMap<Integer, EPerson>();
        for(EPerson eperson: epersons){
            epersonsMap.put(eperson.getID(), eperson);
        }
        for(Group group: groups){
            for(EPerson eperson: Group.allMembers(context, group)){
                epersonsMap.put(eperson.getID(), eperson);
            }
        }
        return new ArrayList<EPerson>(epersonsMap.values());
    }
}
