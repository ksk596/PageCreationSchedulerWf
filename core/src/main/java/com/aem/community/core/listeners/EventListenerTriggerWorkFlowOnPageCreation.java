package com.aem.community.core.listeners;

import java.util.HashMap;

import java.util.Map;

import javax.jcr.RepositoryException;

import javax.jcr.Session;

import javax.jcr.observation.Event;

import javax.jcr.observation.EventIterator;

import javax.jcr.observation.EventListener;

import org.apache.sling.api.resource.ResourceResolver;

import org.apache.sling.api.resource.ResourceResolverFactory;

import org.osgi.service.component.ComponentContext;

import org.osgi.service.component.annotations.Activate;

import org.osgi.service.component.annotations.Component;

import org.osgi.service.component.annotations.Deactivate;

import org.osgi.service.component.annotations.Reference;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import com.day.cq.workflow.WorkflowException;

import com.day.cq.workflow.WorkflowService;

import com.day.cq.workflow.WorkflowSession;

import com.day.cq.workflow.exec.WorkflowData;

import com.day.cq.workflow.model.WorkflowModel;

@Component(service = EventListener.class, immediate = true)

public class EventListenerTriggerWorkFlowOnPageCreation implements EventListener {

	@Reference

	private ResourceResolverFactory resolverFactory;

	@Reference

	private WorkflowService workflowService;

	private ResourceResolver resourceResolver;

	public static final String MY_WORKFLOW_PATH = "/var/workflow/models/contentapprovaldemo";

	Logger log = LoggerFactory.getLogger(this.getClass());

	private Session session;

	@Activate

	public void activate(ComponentContext context) throws Exception {

		log.info("activating ExampleObservation");

		try {

			Map<String, Object> param = new HashMap<String, Object>();

			param.put(ResourceResolverFactory.SUBSERVICE, "jcr-service");

			resourceResolver = resolverFactory.getServiceResourceResolver(param);

			session = resourceResolver.adaptTo(Session.class);

			session.getWorkspace().getObservationManager().addEventListener(this, Event.NODE_ADDED,

					"/content/globalScheduler/en/news", true, null, null, false);

		} catch (RepositoryException e) {

			log.error("unable to create session", e);

			throw new Exception(e);

		}

	}

	@Deactivate

	public void deactivate() {

		if (session != null) {

			session.logout();

		}

	}

	@Override

	public void onEvent(EventIterator eventIterator) {

		try {

		//	while (eventIterator.hasNext()) {

				Event currentEvent = eventIterator.nextEvent();

				log.info("Page has been added : {}", eventIterator.nextEvent().getPath());

				WorkflowSession wfSession = workflowService.getWorkflowSession(session);

				WorkflowModel wfModel = wfSession.getModel(MY_WORKFLOW_PATH);

				WorkflowData wfData = wfSession.newWorkflowData("JCR_PATH", currentEvent.getPath());

				// Start the Workflow

				wfSession.startWorkflow(wfModel, wfData);

			//}

		} catch (RepositoryException | WorkflowException e) {

			log.error("Error while treating events or workflow exception", e);

		}

	}

}