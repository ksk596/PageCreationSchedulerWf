package com.aem.community.core.workflows;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.Replicator;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
@Component(service=WorkflowProcess.class, property = {"process.label=PageApproverStatus Custom Step"})
public class PageApproverStatus implements WorkflowProcess {
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
	@Reference
	private ResourceResolverFactory resolverFactory;
	@Reference
	private Replicator replicator;
	private Session session;
	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
		final WorkflowData workflowData = workItem.getWorkflowData();
		session = workflowSession.getSession();
		final String path = workflowData.getPayload().toString();
		try {
            Node node = session.getNode(path);
            log.info(node.toString());
            final Node somenode = node.getNode("jcr:content");
            somenode.setProperty("ApproverStatus", "Approved");
            replicator.replicate(session, ReplicationActionType.ACTIVATE, path);
        } catch (Exception ex) {
            ex.printStackTrace();
        } 
	}

}
