/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.administrative.submissionprocess;


import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataSchema;
import org.dspace.submission.state.SubmissionStep;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Edit a metadata process by: listing all the existing fields in
 * the process, prompt the user to add a new field. If a current
 * field is selected then the field may be updated in the same
 * place where new field addition would be.
 * 
 * @author Scott Phillips
 */
public class EditSubmissionStep extends AbstractDSpaceTransformer   
{	
	
	/** Language Strings */
	private static final Message T_dspace_home =
		message("xmlui.general.dspace_home");
	private static final Message T_title =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.title");
	private static final Message T_metadata_registry_trail =
		message("xmlui.administrative.submissionprocess.general.metadata_registry_trail");
    private static final Message T_userselection_method =
            message("xmlui.administrative.submissionprocess.EditSubmissionStep.userselection_method");
    private static final Message T_role =
                message("xmlui.administrative.submissionprocess.EditSubmissionStep.role");
	private static final Message T_trail =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.trail");	
	private static final Message T_head1 =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.head1");	
	private static final Message T_para1 =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.para1");	
	private static final Message T_head2 =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.head2");	
	private static final Message T_column1 =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.column1");	
	private static final Message T_column2 =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.column2");	
	private static final Message T_column3 =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.column3");	
	private static final Message T_column4 =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.column4");	
	private static final Message T_empty =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.empty");	
	private static final Message T_submit_return =
		message("xmlui.general.return");	
	private static final Message T_submit_delete =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.submit_delete");
    private static final Message T_submit_edit =
            message("xmlui.administrative.submissionprocess.EditSubmissionStep.submit_edit");
	private static final Message T_submit_move =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.submit_move");	
	private static final Message T_head3 =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.head3");	
	private static final Message T_name =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.name");	
	private static final Message T_note =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.note");	
	private static final Message T_note_help =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.note_help");
	private static final Message T_submit_add =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.submit_add");	
	private static final Message T_head4 =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.head4");
	private static final Message T_submit_update =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.submit_update");
	private static final Message T_submit_cancel =
		message("xmlui.general.cancel");
	private static final Message T_error =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.error");	
	private static final Message T_error_duplicate_field =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.error_duplicate_field");	
	private static final Message T_error_element_empty =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.error_element_empty");	
	private static final Message T_error_element_badchar =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.error_element_badchar");	
	private static final Message T_error_element_tolong =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.error_element_tolong");	
	private static final Message T_error_qualifier_tolong =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.error_qualifier_tolong");	
	private static final Message T_error_qualifier_badchar =
		message("xmlui.administrative.submissionprocess.EditSubmissionStep.error_qualifier_badchar");	
	
	
	public void addPageMeta(PageMeta pageMeta) throws WingException
    {
        pageMeta.addMetadata("title").addContent(T_title);
        pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
        pageMeta.addTrailLink(contextPath + "/admin/submissionprocess",T_metadata_registry_trail);
        pageMeta.addTrail().addContent(T_trail);
    }
	
	
	public void addBody(Body body) throws WingException, SQLException 
	{
		// Get our parameters & state
		int stepID = parameters.getParameterAsInteger("stepID",-1);
		int updateID = parameters.getParameterAsInteger("updateID",-1);
		int highlightID = parameters.getParameterAsInteger("highlightID", -1);
		SubmissionStep step = SubmissionStep.find(context, stepID);
		String processName = step.getName();
		
		String errorString = parameters.getParameter("errors",null);
		ArrayList<String> errors = new ArrayList<String>();
		if (errorString != null)
		{
			for (String error : errorString.split(","))
            {
				errors.add(error);
            }
		}
		
	
        // DIVISION: edit-step
		Division main = body.addInteractiveDivision("process-edit",contextPath+"/admin/submissionprocess",Division.METHOD_POST,"primary administrative submissionprocess");
		main.setHead(T_head1.parameterize(processName));
	
		
		// DIVISION: add or updating a metadata field
//		if (updateID >= 0)
//        {
//            // Updating an existing field
//            addUpdateFieldForm(main, processName, updateID, errors);
//        }
//		else
//        {
//            // Add a new field
//            addNewStepForm(main, processName, errors);
//        }
		Para select = main.addPara();
        select.addContent(T_userselection_method);
		Select userMethod = select.addSelect("user_method");
        select.addContent(T_role);
        Select roleList = select.addSelect("role_list");

        MetadataSchema[] schemas = MetadataSchema.findAll(context);
        for (MetadataSchema schema : schemas)
    	{
    		roleList.addOption(schema.getSchemaID(), schema.getNamespace());
    	}
    	for (MetadataSchema schema : schemas)
    	{
    		userMethod.addOption(schema.getSchemaID(), schema.getNamespace());
    	}
		main.addPara().addButton("submit_edit").setValue(T_submit_edit);
		
		main.addHidden("administrative-continue").setValue(knot.getId());
        
   }
	
	
	/**
	 * Add a form prompting the user to add a new field to the this process.
	 *  
	 * @param div The division to add the form too.
	 * @param processName The processName currently being operated on.
	 * @param errors A list of errors from previous attempts at adding new fields.
	 */
	public void addNewStepForm(Division div, String processName, java.util.List<String> errors) throws WingException
	{
		Request request = ObjectModelHelper.getRequest(objectModel);
		String nameValue = request.getParameter("name");
		
        Division newStep = div.addDivision("edit-process-new-step");
		newStep.setHead(T_head3);
		
		List form = newStep.addList("edit-process-new-step-form",List.TYPE_FORM);
		addFieldErrors(form, errors);
		
		form.addLabel(T_name);
		Text name = form.addItem().addText("name");
				
		name.setSize(15);
		name.setValue(nameValue);
		
		form.addItem().addButton("submit_add").setValue(T_submit_add);
	}
	

	/**
	 * Update an existing field by promting the user for it's values.
	 *  
	 * @param div The division to add the form too.
	 * @param processName The processName currently being operated on.
	 * @param fieldID The id of the field being updated.
	 * @param errors A list of errors from previous attempts at updaating the field.
	 */
	public void addUpdateFieldForm(Division div, String processName, int fieldID, java.util.List<String> errors) throws WingException, SQLException
	{
		
		MetadataField field = MetadataField.find(context, fieldID);
		
		Request request = ObjectModelHelper.getRequest(objectModel);
		String elementValue = request.getParameter("updateElement");
		String qualifierValue = request.getParameter("updateQualifier");
		String noteValue = request.getParameter("updateNote");
		
		if (elementValue == null)
        {
            elementValue = field.getElement();
        }
		if (qualifierValue == null)
        {
            qualifierValue = field.getQualifier();
        }
		if (noteValue == null)
        {
            noteValue = field.getScopeNote();
        }
		
		
		Division newStep = div.addDivision("edit-process-update-field");
		newStep.setHead(T_head4.parameterize(field.getFieldID()));
		
		List form = newStep.addList("edit-process-update-field-form",List.TYPE_FORM);
		

		addFieldErrors(form, errors);
		
		form.addLabel(T_name);
		Highlight item =form.addItem().addHighlight("big");
		
		item.addContent(processName+" . ");
		Text element = item.addText("updateElement");
		item.addContent(" . ");
		Text qualifier = item.addText("updateQualifier");
		
		
		element.setSize(13);
		element.setValue(elementValue);
		
		qualifier.setSize(13);
		qualifier.setValue(qualifierValue);
		
		TextArea scopeNote =form.addItem().addTextArea("updateNote");
		scopeNote.setLabel(T_note);
		scopeNote.setHelp(T_note_help);
		scopeNote.setSize(2, 35);
		scopeNote.setValue(noteValue);
		
		Item actions = form.addItem();
		actions.addButton("submit_update").setValue(T_submit_update);
		actions.addButton("submit_cancel").setValue(T_submit_cancel);
		
	}
	
	/**
	 * Determine if there were any special errors and display approparte 
	 * text. Because of the inline nature of the element and qualifier 
	 * fields these errors can not be placed on the field. Instead they 
	 * have to be added as seperate items above the field.
	 * 
	 * @param form The form to add errors to.
	 * @param errors A list of errors.
	 */
	
	public void addFieldErrors(List form, java.util.List<String> errors) throws WingException 
	{
		if (errors.contains("duplicate_field"))
		{
			form.addLabel(T_error);
			form.addItem(T_error_duplicate_field);
		}
		if (errors.contains("element_empty"))
		{
			form.addLabel(T_error);
			form.addItem(T_error_element_empty);
		}
		if (errors.contains("element_badchar"))
		{
			form.addLabel(T_error);
			form.addItem(T_error_element_badchar);
		}
		if (errors.contains("element_tolong"))
		{
			form.addLabel(T_error);
			form.addItem(T_error_element_tolong);
		}
		if (errors.contains("qualifier_tolong"))
		{
			form.addLabel(T_error);
			form.addItem(T_error_qualifier_tolong);
		}
		if (errors.contains("qualifier_badchar"))
		{
			form.addLabel(T_error);
			form.addItem(T_error_qualifier_badchar);
		}
	}
}
