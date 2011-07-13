/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.administrative.role;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.submission.Role;
import org.xml.sax.SAXException;

import java.sql.SQLException;

/**
 * Web interface to Bulk Metadata Import app.
 * ported from org.dspace.app.webui.servlet.MetadataImportServlet
 *
 * Initial select file / upload CSV form
 *
 * @author Kim Shepherd
 */

public class CreateRoleMain extends AbstractDSpaceTransformer {

	/** Language strings */
	private static final Message T_dspace_home = message("xmlui.general.dspace_home");

	private static final Message T_title = message("xmlui.administrative.role.CreateRoleMain.general.title");
	private static final Message T_head1 = message("xmlui.administrative.role.CreateRoleMain.head1");
        private static final Message T_submit_upload = message("xmlui.administrative.role.CreateRoleMain.submit_upload");
        private static final Message T_trail = message("xmlui.administrative.role.CreateRoleMain.general.trail");
    private static final Message T_head2 =
		message("xmlui.administrative.role.CreateRoleMain.head2");
	private static final Message T_namespace =
		message("xmlui.administrative.role.CreateRoleMain.namespace");
	private static final Message T_namespace_help =
		message("xmlui.administrative.role.CreateRoleMain.namespace_help");
	private static final Message T_namespace_error =
		message("xmlui.administrative.role.CreateRoleMain.namespace_error");
	private static final Message T_name =
		message("xmlui.administrative.role.CreateRoleMain.name");
    private static final Message T_description =
            message("xmlui.administrative.role.CreateRoleMain.description");
    private static final Message T_scope =
                message("xmlui.administrative.role.CreateRoleMain.scope");
    private static final Message T_column1 =
		message("xmlui.administrative.role.CreateRoleMain.column1");
	private static final Message T_column2 =
		message("xmlui.administrative.role.CreateRoleMain.column2");
	private static final Message T_column3 =
		message("xmlui.administrative.role.CreateRoleMain.column3");
	private static final Message T_name_help =
		message("xmlui.administrative.role.CreateRoleMain.name_help");
	private static final Message T_name_error =
		message("xmlui.administrative.role.CreateRoleMain.name_error");
	private static final Message T_submit_add =
		message("xmlui.administrative.role.CreateRoleMain.submit_add");
    private static final Message T_submit_delete =
		message("xmlui.administrative.role.CreateRoleMain.submit_delete");
    private static final Message T_submit_update =
		message("xmlui.administrative.role.CreateRoleMain.submit_update");
    private static final Message T_submit_cancel =
            message("xmlui.administrative.role.CreateRoleMain.submit_cancel");

	
	public void addPageMeta(PageMeta pageMeta) throws WingException  
	{
		pageMeta.addMetadata("title").addContent(T_title);
		
		pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
		pageMeta.addTrail().addContent(T_trail);
	}

	
	public void addBody(Body body) throws SAXException, WingException, SQLException
	{
		// Get all our parameters first
		Request request = ObjectModelHelper.getRequest(objectModel);
		String nameValue = request.getParameter("name");
        String descriptionValue = request.getParameter("description");
        String scopeValue = request.getParameter("scope");
        int roleID = parameters.getParameterAsInteger("roleID",-1);
		int updateID = parameters.getParameterAsInteger("updateID",-1);
		int highlightID = parameters.getParameterAsInteger("highlightID",-1);
        Role[] roles = Role.findAll(context);
        // DIVISION: submission-role
		Division main = body.addInteractiveDivision("role",contextPath+"/admin/roles",Division.METHOD_POST,"primary administrative metadata-registry ");
		main.setHead(T_head1);
        //main.addPara(T_para1);

		Table table = main.addTable("submissionprocess-main-table", roles.length+1, 4);

		Row header = table.addRow(Row.ROLE_HEADER);
		header.addCellContent(T_column1);
		header.addCellContent(T_column2);
		header.addCellContent(T_column3);

		for (Role role : roles)
		{
			int role_id     = role.getId();
			String name      = role.getName();
            String url = contextPath + "/admin/roles?administrative-continue="+knot.getId()+"&submit_edit&roleID="+role_id;

			Row row = table.addRow();
			if (role_id > 1)
			{
				// If the schema is not in the required DC schema allow the user to delete it.
				CheckBox select = row.addCell().addCheckBox("select_role");
				select.setLabel(String.valueOf(role_id));
				select.addOption(String.valueOf(role_id));
			}
			else
			{
				// The DC schema can not be removed.
				row.addCell();
			}

			row.addCell().addContent(role_id);
			row.addCell().addXref(url,name);
		}
		if (roles.length > 1)
		{
			// Only give the delete option if there are more schema's than the required dublin core.
			main.addPara().addButton("submit_delete").setValue(T_submit_delete);
		}

       if (updateID >= 0)
        {
            // Updating an existing field
            //addUpdateFieldForm(main, schemaName, updateID, errors);
        List form = main.addList("edit-schema-update-field-form",List.TYPE_FORM);

		form.addLabel(T_name);
		Highlight item =form.addItem().addHighlight("big");


		Item actions = form.addItem();
		actions.addButton("submit_update").setValue(T_submit_update);
		actions.addButton("submit_cancel").setValue(T_submit_cancel);
        }
		else
        {

       // DIVISION: add new role
		Division newProcess = main.addDivision("add-role");
		newProcess.setHead(T_head2);

		List form = newProcess.addList("new-role",List.TYPE_FORM);

		Text name = form.addItem().addText("name");
		name.setLabel(T_name);
		name.setRequired();
		name.setHelp(T_name_help);
		if (nameValue != null)
        {
            name.setValue(nameValue);
        }
        TextArea description = form.addItem().addTextArea("description");
		description.setLabel(T_description);
        description.setSize(2,35);
		//description.setHelp(T_description_help);
		if (descriptionValue != null)
        {
            description.setValue(descriptionValue);
        }

        Text scope = form.addItem().addText("scope");
		scope.setLabel(T_scope);
		scope.setRequired();
		//scope.setHelp(T_scope_help);
		if (scopeValue != null)
        {
            scope.setValue(scopeValue);
        }
//		if (errors.contains("name"))
//        {
//            name.addError(T_name_error);
//        }

		form.addItem().addButton("submit_add").setValue(T_submit_add);
		main.addHidden("administrative-continue").setValue(knot.getId());
        }
   }
}

