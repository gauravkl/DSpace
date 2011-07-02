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
import org.dspace.submission.state.SubmissionProcess;
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
public class EditSubmissionProcess extends AbstractDSpaceTransformer   
{	
	
	/** Language Strings */
	private static final Message T_dspace_home =
		message("xmlui.general.dspace_home");
	private static final Message T_title =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.title");
	private static final Message T_metadata_registry_trail =
		message("xmlui.administrative.submissionprocess.general.metadata_registry_trail");
	private static final Message T_trail =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.trail");	
	private static final Message T_head1 =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.head1");	
	private static final Message T_para1 =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.para1");	
	private static final Message T_head2 =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.head2");	
	private static final Message T_column1 =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.column1");	
	private static final Message T_column2 =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.column2");	
	private static final Message T_column3 =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.column3");	
	private static final Message T_column4 =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.column4");	
	private static final Message T_empty =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.empty");	
	private static final Message T_submit_return =
		message("xmlui.general.return");	
	private static final Message T_submit_delete =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.submit_delete");	
	private static final Message T_submit_move =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.submit_move");	
	private static final Message T_head3 =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.head3");	
	private static final Message T_name =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.name");	
	private static final Message T_note =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.note");	
	private static final Message T_note_help =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.note_help");
	private static final Message T_submit_add =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.submit_add");	
	private static final Message T_head4 =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.head4");
	private static final Message T_submit_update =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.submit_update");
	private static final Message T_submit_cancel =
		message("xmlui.general.cancel");
	private static final Message T_error =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.error");	
	private static final Message T_error_duplicate_field =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.error_duplicate_field");	
	private static final Message T_error_element_empty =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.error_element_empty");	
	private static final Message T_error_element_badchar =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.error_element_badchar");	
	private static final Message T_error_element_tolong =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.error_element_tolong");	
	private static final Message T_error_qualifier_tolong =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.error_qualifier_tolong");	
	private static final Message T_error_qualifier_badchar =
		message("xmlui.administrative.submissionprocess.EditSubmissionProcess.error_qualifier_badchar");	
	
	
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
		int processID = parameters.getParameterAsInteger("processID",-1);
		int updateID = parameters.getParameterAsInteger("updateID",-1);
		int highlightID = parameters.getParameterAsInteger("highlightID",-1);
		SubmissionProcess process = SubmissionProcess.find(context,processID);
		SubmissionStep[] steps = SubmissionProcess.getSteps(context, processID);
		String processName = process.getName();
		
		String errorString = parameters.getParameter("errors",null);
		ArrayList<String> errors = new ArrayList<String>();
		if (errorString != null)
		{
			for (String error : errorString.split(","))
            {
				errors.add(error);
            }
		}
		
	
        // DIVISION: edit-process
		Division main = body.addInteractiveDivision("process-edit",contextPath+"/admin/submissionprocess",Division.METHOD_POST,"primary administrative submissionprocess");
		main.setHead(T_head1.parameterize(processName));
	
		
		// DIVISION: add or updating a metadata field
		if (updateID >= 0)
        {
            // Updating an existing field
            addUpdateFieldForm(main, processName, updateID, errors);
        }
		else
        {
            // Add a new field
            addNewStepForm(main, processName, errors);
        }
		
		
		
		// DIVISION: existing fields
		Division existingFields = main.addDivision("process-edit-existing-fields");
		existingFields.setHead(T_head2);
		
		Table table = existingFields.addTable("process-edit-existing-fields", steps.length+1, 5);
		
		Row header = table.addRow(Row.ROLE_HEADER);
		header.addCellContent(T_column1);
		header.addCellContent(T_column2);
		header.addCellContent(T_column3);
		
		for (SubmissionStep step:steps)
		{
			String id = String.valueOf(step.getId());
			String stepName = step.getName();

				
			boolean highlight = false;
			if (step.getId() == highlightID)
            {
                highlight = true;
            }

			String url = contextPath + "/admin/submissionprocess?administrative-continue="+knot.getId()+"&submit_edit&stepID="+id;
			
			Row row;
			if (highlight)
            {
                row = table.addRow(null, null, "highlight");
            }
			else
            {
                row = table.addRow();
            }
			
			CheckBox select = row.addCell().addCheckBox("select_step");
			select.setLabel(id);
			select.addOption(id);
			
			row.addCell().addContent(id);
			row.addCell().addXref(url,stepName);
		}
		
		if (steps.length == 0)
		{
			// No fields, let the user know.
			table.addRow().addCell(1,4).addHighlight("italic").addContent(T_empty);
			main.addPara().addButton("submit_return").setValue(T_submit_return);
		}
		else
		{
			// Only show the actions if there are fields available to preform them on.
			Para actions = main.addPara();
			actions.addButton("submit_delete").setValue(T_submit_delete);
//			if (SubmissionProcess.findAll(context).length > 1)
//            {
//                actions.addButton("submit_move").setValue(T_submit_move);
//            }
			actions.addButton("submit_return").setValue(T_submit_return);
		}
		
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
