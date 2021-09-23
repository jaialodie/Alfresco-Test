package org.alfresco.university.platformsample;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.io.FilenameUtils;



public class autoVersioningByNameBehaviour implements NodeServicePolicies.OnCreateNodePolicy {

    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private ContentService contentService;

    public void init() {
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {

        NodeRef nodeRef = childAssocRef.getChildRef();

        if (nodeService.exists(nodeRef) ) {

            NodeRef previouslyExistentDoc = existedPreviously(nodeRef);

            if (previouslyExistentDoc != null) {

                ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                ContentWriter writer = contentService.getWriter(previouslyExistentDoc, ContentModel.PROP_CONTENT, true);

                writer.putContent(reader);

                // Solving issue #2: nodes marked as hidden are not included in postActivity processor
                nodeService.deleteNode(nodeRef);

            }

        }

    }

    public NodeRef existedPreviously(NodeRef nodeRef) {

        String fileName = cleanNumberedSuffixes(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString());
        NodeRef folder = nodeService.getPrimaryParent(nodeRef).getParentRef();

        for(ChildAssociationRef child : nodeService.getChildAssocs(folder)){
            String currentName = nodeService.getProperty(child.getChildRef(), ContentModel.PROP_NAME).toString();
            boolean isContent = nodeService.getType(child.getChildRef()).isMatch(ContentModel.TYPE_CONTENT);
            if (isContent){
                if(currentName.equals(fileName) && !(child.getChildRef().getId().equals(nodeRef.getId()))) {
                    return child.getChildRef();
                }
            }
        }

        return null;

    }

    // Alfresco includes "-1" and so on for repeated filenames in the same folder, this method remove this addition from the file name
    public static String cleanNumberedSuffixes(String fileName) {

        String cleanedFileName = fileName;
        String baseName = FilenameUtils.getBaseName(fileName);
        if (baseName.indexOf("-") != -1) {
            if (isInteger(baseName.substring(baseName.lastIndexOf("-") + 1, baseName.length()))) {
                return baseName.substring(0, baseName.lastIndexOf("-")) + FilenameUtils.EXTENSION_SEPARATOR_STR + FilenameUtils.getExtension(fileName);
            }
        }
        return cleanedFileName;

    }

    public static boolean isInteger(String s) {
        boolean isValidInteger = false;
        try {
            Integer.parseInt(s);
            isValidInteger = true;
        } catch (NumberFormatException ex) {}
        return isValidInteger;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }


    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

}

