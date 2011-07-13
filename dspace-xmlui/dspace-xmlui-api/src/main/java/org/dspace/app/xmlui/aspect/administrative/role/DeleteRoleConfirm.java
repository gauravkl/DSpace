/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.administrative.role;

import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.submission.Role;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Prompt the user to determin if they really want to delete the displayed roles.
 * 
 * @author Scott phillips
 */
public class DeleteRoleConfirm extends AbstractDSpaceTransformer   
{
	/** Language Strings */
	private static final Message T_dspace_home =
		message("xmlui.general.dspace_home");
	private static final Message T_title =
		message("xmlui.administrative.Role.DeleteRoleConfirm.title");
	private static final Message T_Role =
		message("xmlui.administrative.Role.general.Role");
	private static final Message T_trail =
		message("xmlui.administrative.Role.DeleteRoleConfirm.trail");
	private static final Message T_head =
		message("xmlui.administrative.Role.DeleteRoleConfirm.head");
	private static final Message T_para1 =
		message("xmlui.administrative.Role.DeleteRoleConfirm.para1");
	private static final Message T_warning =
		message("xmlui.administrative.Role.DeleteRoleConfirm.warning");
	private static final Message T_para2 =
		message("xmlui.administrative.Role.DeleteRoleConfirm.para2");
	private static final Message T_column1 =
		message("xmlui.administrative.Role.DeleteRoleConfirm.column1");
	private static final Message T_column2 =
		message("xmlui.administrative.Role.DeleteRoleConfirm.column2");
	private static final Message T_submit_delete =
		message("xmlui.general.delete");
	private static final Message T_submit_cancel =
		message("xmlui.general.cancel");
	
	
	public void addPageMeta(PageMeta pageMeta) throws WingException
    {
        pageMeta.addMetadata("title").addContent(T_title);
        pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
        pageMeta.addTrailLink(contextPath + "/admin/metadata-registry",T_Role);
        pageMeta.addTrail().addContent(T_trail);
    }
	
	
	public void addBody(Body body) throws WingException, SQLException, AuthorizeException
	{
		// Get all our parameters
		String idsString = parameters.getParameter("roleIDs", null);
		
		ArrayList<Role> roles = new ArrayList<Role>();
		for (String id : idsString.split(","))
		{
			Role role = Role.find(context,Integer.valueOf(id));
			roles.add(role);
		}
 
		// DIVISION: metadata-schema-confirm-delete
    	Division deleted = body.addInteractiveDivision("metadata-schema-confirm-delete",contextPath+"/admin/metadata-registry",Division.METHOD_POST,"primary administrative metadata-registry");
    	deleted.setHead(T_head);
    	deleted.addPara(T_para1);
    	Para warning = deleted.addPara();
    	warning.addHighlight("bold").addContent(T_warning);
    	warning.addContent(T_para2);
    	
    	Table table = deleted.addTable("schema-confirm-delete",roles.size() + 1, 3);
        Row header = table.addRow(Row.ROLE_HEADER);
        header.addCell().addContent(T_column1);
        header.addCell().addContent(T_column2);
        
    	for (Role role : roles)
    	{
    		Row row = table.addRow();
    		row.addCell().addContent(role.getId());
        	row.addCell().addContent(role.getName());
	    }
    	Para buttons = deleted.addPara();
    	buttons.addButton("submit_confirm").setValue(T_submit_delete);
    	buttons.addButton("submit_cancel").setValue(T_submit_cancel);
    	
    	deleted.addHidden("administrative-continue").setValue(knot.getId());
    }
}
