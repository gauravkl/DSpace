/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.administrative.submissionprocess;

import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.submission.state.actions.SubmissionAction;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Prompt the user to determin if they really want to delete the displayed actions.
 * 
 * @author Scott phillips
 */
public class DeleteSubmissionActionConfirm extends AbstractDSpaceTransformer   
{
	/** Language Strings */
	private static final Message T_dspace_home =
		message("xmlui.general.dspace_home");
	private static final Message T_title =
		message("xmlui.administrative.submissionprocess.DeleteSubmissionActionConfirm.title");
	private static final Message T_submissionprocess_trail =
		message("xmlui.administrative.submissionprocess.general.submissionprocess_trail");
	private static final Message T_trail =
		message("xmlui.administrative.submissionprocess.DeleteSubmissionActionConfirm.trail");
	private static final Message T_head =
		message("xmlui.administrative.submissionprocess.DeleteSubmissionActionConfirm.head");
	private static final Message T_para1 =
		message("xmlui.administrative.submissionprocess.DeleteSubmissionActionConfirm.para1");
	private static final Message T_warning =
		message("xmlui.administrative.submissionprocess.DeleteSubmissionActionConfirm.warning");
	private static final Message T_para2 =
		message("xmlui.administrative.submissionprocess.DeleteSubmissionActionConfirm.para2");
	private static final Message T_column1 =
		message("xmlui.administrative.submissionprocess.DeleteSubmissionActionConfirm.column1");
	private static final Message T_column2 =
		message("xmlui.administrative.submissionprocess.DeleteSubmissionActionConfirm.column2");
	private static final Message T_submit_delete =
		message("xmlui.general.delete");
	private static final Message T_submit_cancel =
		message("xmlui.general.cancel");
	
	
	public void addPageMeta(PageMeta pageMeta) throws WingException
    {
        pageMeta.addMetadata("title").addContent(T_title);
        pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
        pageMeta.addTrailLink(contextPath + "/admin/submissionprocess",T_submissionprocess_trail);
        pageMeta.addTrail().addContent(T_trail);
    }
	
	
	public void addBody(Body body) throws WingException, SQLException, AuthorizeException
	{
		// Get all our parameters
		String idsString = parameters.getParameter("actionIDs", null);
		
		ArrayList<SubmissionAction> actions = new ArrayList<SubmissionAction>();
		for (String id : idsString.split(","))
		{
			SubmissionAction action = SubmissionAction.find(context,Integer.valueOf(id));
			actions.add(action);
		}
 
		// DIVISION: metadata-schema-confirm-delete
    	Division deleted = body.addInteractiveDivision("metadata-schema-confirm-delete",contextPath+"/admin/submissionprocess",Division.METHOD_POST,"primary administrative submissionprocess");
    	deleted.setHead(T_head);
    	deleted.addPara(T_para1);
    	Para warning = deleted.addPara();
    	warning.addHighlight("bold").addContent(T_warning);
    	warning.addContent(T_para2);
    	
    	Table table = deleted.addTable("step-confirm-delete",actions.size() + 1, 3);
        Row header = table.addRow(Row.ROLE_HEADER);
        header.addCell().addContent(T_column1);
        header.addCell().addContent(T_column2);
        
    	for (SubmissionAction action : actions)
    	{
    		Row row = table.addRow();
    		row.addCell().addContent(action.getId());
        	row.addCell().addContent(action.getBean_id());
	    }
    	Para buttons = deleted.addPara();
    	buttons.addButton("submit_confirm").setValue(T_submit_delete);
    	buttons.addButton("submit_cancel").setValue(T_submit_cancel);
    	
    	deleted.addHidden("administrative-continue").setValue(knot.getId());
    }
}
