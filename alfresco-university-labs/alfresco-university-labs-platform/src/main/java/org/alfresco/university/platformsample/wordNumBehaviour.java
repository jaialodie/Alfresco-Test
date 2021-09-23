package org.alfresco.university.platformsample;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class wordNumBehaviour implements NodeServicePolicies.OnCreateStorePolicy, NodeServicePolicies.OnUpdateNodePolicy {

    // Dependencies
    private final NodeService nodeService;
    private final PolicyComponent policyComponent;
    private final ContentService contentService;

    // Behaviours
    private Behaviour onCreateNode;
    private Behaviour onUpdateNode;

    public wordNumBehaviour(NodeService nodeService, PolicyComponent policyComponent, ContentService contentService) {
        this.nodeService = nodeService;
        this.policyComponent = policyComponent;
        this.contentService = contentService;
    }

    public void init() {
        // Create behaviours
        this.onCreateNode = new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.EVERY_EVENT);

        this.onUpdateNode = new JavaBehaviour(this, "onDeleteNode", Behaviour.NotificationFrequency.EVERY_EVENT);

        // Bind behaviours to node policies
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
                QName.createQName(SomeCoReviewModel.NAMESPACE_SOMECO_REVIEW_CONTENT_MODEL, SomeCoReviewModel.TYPE_SCR_REVIEW),
                this.onCreateNode
        );

        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateNode"),
                QName.createQName(SomeCoReviewModel.NAMESPACE_SOMECO_REVIEW_CONTENT_MODEL, SomeCoReviewModel.TYPE_SCR_REVIEW),
                this.onUpdateNode
        );
    }
    public void onCreateStore(NodeRef nodeRef) {
        setWordNum(nodeRef);
    }

    public void onUpdateNode(NodeRef nodeRef) {
        setWordNum(nodeRef);
    }

    public void setWordNum(NodeRef nodeRef) {

        // check the parent to make sure it has the right aspect
        if ( nodeService.hasAspect(nodeRef, QName.createQName(SomeCoReviewModel.NAMESPACE_SOMECO_REVIEW_CONTENT_MODEL, SomeCoReviewModel.ASPECT_SCR_REVIEW))) {

            ContentReader reader = contentService.getReader(nodeRef, QName.createQName(SomeCoReviewModel.PROP_WORD_NUM));

            long numberOfWords = reader.getSize();

            // Set the aspect property Num of words
            nodeService.setProperty(
                    nodeRef,
                    QName.createQName(
                            SomeCoReviewModel.NAMESPACE_SOMECO_REVIEW_CONTENT_MODEL,
                            SomeCoReviewModel.PROP_WORD_NUM),
                    numberOfWords);

        } else {
            return;
        }

    }
}

