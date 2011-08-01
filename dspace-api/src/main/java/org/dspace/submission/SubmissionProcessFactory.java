package org.dspace.submission;

import com.sun.org.apache.xpath.internal.XPathAPI;
import org.apache.log4j.Logger;
import org.dspace.content.Collection;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.submission.state.SubmissionProcess;
import org.dspace.submission.state.actions.SubmissionActionConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.TransformerException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: bram
 * Date: 3-aug-2010
 * Time: 13:51:18
 * To change this template use File | Settings | File Templates.
 */
public class SubmissionProcessFactory {

    private static Logger log = Logger.getLogger(SubmissionProcessFactory.class);

    private static HashMap<String, SubmissionProcess> submissionProcessCache;
    private static String path = ConfigurationManager.getProperty("dspace.dir")+"/config/SubmissionProcess.xml";
    private static String pathActions = ConfigurationManager.getProperty("dspace.dir")+"/config/submission-actions-xmlui.xml";

    //TODO: Depending on the role system, create one process object per collection
    public static SubmissionProcess getSubmissionProcess(Context context,Collection collection) throws Exception//, SubmissionProcessConfigurationException
     {
        //Initialize our cache if we have none
        if(submissionProcessCache == null)
            submissionProcessCache = new HashMap<String, SubmissionProcess>();

        // Attempt to retrieve our SubmissionProcess object
        if(submissionProcessCache.get(collection.getHandle())==null){
            try{
                    //We have a processID so retrieve it & resolve it to a SubmissionProcess, also store it in our cache
                    SubmissionProcess process = collection.getSubmissionProcess(context);
                    submissionProcessCache.put(collection.getHandle(), process);
                    return process;
            } catch (Exception e){
                log.error("Error while retrieving SubmissionProcess for collection: " + collection.getHandle(), e);
                throw new Exception("Error while retrieving SubmissionProcess for the following collection: " + collection.getHandle());
            }
        }else{
            return submissionProcessCache.get(collection.getHandle());
        }
    }





//    private static Map<Integer, String> getStepOutcomes(Node stepNode) throws TransformerException, SubmissionProcessConfigurationException {
//        try{
//            NodeList outcomesNodeList = XPathAPI.selectNodeList(stepNode, "outcomes/step");
//            Map<Integer, String> outcomes = new HashMap<Integer, String>();
//            //Add our outcome, should it be null it will be interpreted as the end of the line (last step)
//            for(int i = 0; i < outcomesNodeList.getLength(); i++){
//                Node outcomeNode = outcomesNodeList.item(i);
//                int index = Integer.parseInt(outcomeNode.getAttributes().getNamedItem("status").getTextContent());
//                if(index < 0){
//                    throw new SubmissionProcessConfigurationException("Outcome configuration error for step: "+stepNode.getAttributes().getNamedItem("id").getTextContent());
//                }
//                outcomes.put(index, outcomeNode.getTextContent());
//            }
//            return outcomes;
//        }catch(Exception e){
//            log.error("Outcome configuration error for step: " + stepNode.getAttributes().getNamedItem("id").getTextContent(), e);
//            throw new SubmissionProcessConfigurationException("Outcome configuration error for step: "+stepNode.getAttributes().getNamedItem("id").getTextContent());
//        }
//    }

    private static List<String> getStepActionConfigs(Node stepNode) throws TransformerException {
        NodeList actionConfigNodes = XPathAPI.selectNodeList(stepNode, "actions/action");
        List<String> actionConfigIDs = new ArrayList<String>();
        for(int i = 0; i < actionConfigNodes.getLength(); i++){
            actionConfigIDs.add(actionConfigNodes.item(i).getAttributes().getNamedItem("id").getTextContent());
        }
        return actionConfigIDs;
    }

//    public static SubmissionStep createStep(SubmissionProcess SubmissionProcess, String stepID) throws SubmissionProcessConfigurationException, IOException {
//        try{
//            File xmlFile = new File(path);
//            Document input = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
//            Node mainNode = input.getFirstChild();
//            Node stepNode = XPathAPI.selectSingleNode(mainNode, "//SubmissionProcess[@id='"+SubmissionProcess.getID()+"']/step[@id='"+stepID+"']");
//
//            if(stepNode == null){
//                throw new SubmissionProcessConfigurationException("SubmissionStep does not exist for SubmissionProcess: "+SubmissionProcess.getID());
//            }
//            Node roleNode = stepNode.getAttributes().getNamedItem("role");
//            Role role = null;
//            if(roleNode != null)
//                role = SubmissionProcess.getRoles().get(roleNode.getTextContent());
//            String userSelectionActionID = stepNode.getAttributes().getNamedItem("userSelectionMethod").getTextContent();
//            UserSelectionActionConfig userSelection = createUserAssignmentActionConfig(userSelectionActionID);
//            return new SubmissionStep(stepID, SubmissionProcess, role, userSelection, getStepActionConfigs(stepNode), getStepOutcomes(stepNode), getNbRequiredUser(stepNode));
//
//        }catch (Exception e){
//            log.error("Error while creating step with :" + stepID, e);
//            throw new SubmissionProcessConfigurationException("SubmissionStep: " + stepID + " does not exist for SubmissionProcess: "+SubmissionProcess.getID());
//        }
//    }
//     private static UserSelectionActionConfig createUserAssignmentActionConfig(String userSelectionActionID) {
//        ApplicationContext applicationContext = new FileSystemXmlApplicationContext("file:" + pathActions);
//        UserSelectionActionConfig config = (UserSelectionActionConfig) applicationContext.getBean(userSelectionActionID, UserSelectionActionConfig.class);
//        return config;
//    }
//
    public static SubmissionActionConfig createSubmissionActionConfig(String beanID)throws SQLException{
        ApplicationContext applicationContext = new FileSystemXmlApplicationContext("file:" + pathActions);
        SubmissionActionConfig config = (SubmissionActionConfig) applicationContext.getBean(beanID, SubmissionActionConfig.class);
        return config;
    }

//    private static HashMap<String, Role> getRoles(Node SubmissionProcessNode) throws SubmissionProcessConfigurationException {
//        NodeList roleNodes = null;
//        try{
//            roleNodes = XPathAPI.selectNodeList(SubmissionProcessNode, "roles/role");
//        }catch (Exception e){
//            log.error("Error while resolving nodes", e);
//            //throw new SubmissionProcessConfigurationException("Error while retrieving roles");
//        }
//        HashMap<String, Role> roles = new HashMap<String, Role>();
//        for(int i = 0; i < roleNodes.getLength(); i++){
//            String roleID = roleNodes.item(i).getAttributes().getNamedItem("id").getTextContent();
//            String roleName = roleNodes.item(i).getAttributes().getNamedItem("name").getTextContent();
//            Node descriptionNode = roleNodes.item(i).getAttributes().getNamedItem("description");
//            String roleDescription = null;
//            if(descriptionNode != null)
//                roleDescription = descriptionNode.getTextContent();
//
//            Node scopeNode = roleNodes.item(i).getAttributes().getNamedItem("scope");
//            String roleScope = null;
//            if(scopeNode != null)
//                roleScope = scopeNode.getTextContent();
//
//            Node internalNode = roleNodes.item(i).getAttributes().getNamedItem("internal");
//            String roleInternal;
//            boolean internal = false;
//            if(internalNode != null){
//                roleInternal = internalNode.getTextContent();
//                internal = Boolean.parseBoolean(roleInternal);
//
//            }
//
//            int scope = 0;
////            if(roleScope == null || roleScope.equalsIgnoreCase("collection"))
////                scope = Role.Scope.COLLECTION;
////            else
////            if(roleScope.equalsIgnoreCase("item"))
////                scope = Role.Scope.ITEM;
////            else
////            if(roleScope.equalsIgnoreCase("repository"))
////                scope = Role.Scope.REPOSITORY;
////            else
////               // throw new SubmissionProcessConfigurationException("An invalid role scope has been specified it must either be item or collection.");
//
//            Role role = new Role(roleName, roleDescription,internal, scope);
//            roles.put(roleID, role);
//        }
//        return roles;
//    }

}
