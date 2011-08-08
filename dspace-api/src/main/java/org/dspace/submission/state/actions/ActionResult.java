package org.dspace.submission.state.actions;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 11-aug-2010
 * Time: 13:55:38
 */
public class ActionResult {

    //TODO: make enum configurable
    public static enum TYPE{
        TYPE_OUTCOME,
        TYPE_PAGE,
        TYPE_ERROR,
        TYPE_CANCEL,
        TYPE_SUBMISSION_PAGE
    }

    public static final int OUTCOME_COMPLETE = 0;
    public static final int EDIT_COMPLETE = 25;
     public static final int INTEGRITY_ERROR = 1;

    // error in uploading file
    public static final int UPLOAD_ERROR = 2;

    // error - no files uploaded!
    public static final int NO_FILES_ERROR = 5;

    // format of uploaded file is unknown
    public static final int UNKNOWN_FORMAT = 10;

    // virus checker unavailable ?
    public static final int VIRUS_CHECKER_UNAVAILABLE = 14;

    // file failed virus check
    public static final int CONTAINS_VIRUS = 16;

    // edit file information
    public static final int EDIT_BITSTREAM = 20;

     public static final int VERIFY_PRUNE = 1;

    // pruning was cancelled by user
    public static final int CANCEL_PRUNE = 2;

    // user attempted to upload a thesis, when theses are not accepted
    public static final int THESIS_REJECTED = 3;

    private TYPE type;
    private int result;

    public ActionResult(TYPE type, int result) {
        this.type = type;
        this.result = result;
    }

    public ActionResult(TYPE type) {
        this.type = type;
        this.result = -1;
    }

    public int getResult() {
        return result;
    }

    public TYPE getType() {
        return type;
    }
}


