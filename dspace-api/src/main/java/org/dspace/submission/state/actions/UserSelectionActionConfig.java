package org.dspace.submission.state.actions;

import org.dspace.submission.state.actions.userassignment.UserSelectionAction;

/**
 * Created by IntelliJ IDEA.
 * User: bram
 * Date: 6-aug-2010
 * Time: 14:57:17
 * To change this template use File | Settings | File Templates.
 */
public class UserSelectionActionConfig extends SubmissionActionConfig{

    public UserSelectionActionConfig(String id) {
        super(id);
    }

    public UserSelectionAction getProcessingAction(){
        return (UserSelectionAction) processingAction;
    }
}
