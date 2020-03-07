package com.aem.community.core.schedulers;

import java.text.SimpleDateFormat;

import java.util.Date;

import java.util.HashMap;

import java.util.Map;

import javax.jcr.ItemExistsException;

import javax.jcr.Node;

import javax.jcr.PathNotFoundException;

import javax.jcr.RepositoryException;

import javax.jcr.Session;

import javax.jcr.lock.LockException;

import javax.jcr.nodetype.ConstraintViolationException;

import javax.jcr.nodetype.NoSuchNodeTypeException;

import javax.jcr.version.VersionException;

import org.apache.sling.api.resource.LoginException;

import org.apache.sling.api.resource.ResourceResolver;

import org.apache.sling.api.resource.ResourceResolverFactory;

import org.osgi.service.component.annotations.Activate;

import org.osgi.service.component.annotations.Component;

import org.osgi.service.component.annotations.Reference;

import org.osgi.service.metatype.annotations.AttributeDefinition;

import org.osgi.service.metatype.annotations.Designate;

import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;

import com.day.cq.wcm.api.PageManager;

import com.day.cq.wcm.api.WCMException;

@Designate(ocd = CustomScheduler.Config.class)

@Component(service = Runnable.class)

public class CustomScheduler implements Runnable {

	@ObjectClassDefinition(name = "Custom Scheduler", description = "custom cron-job like task to create date folders")

	public static @interface Config {

		@AttributeDefinition(name = "Cron-job expression")

		String scheduler_expression() default "0 0/3 * 1/1 * ? *";

		@AttributeDefinition(name = "Concurrent task", description = "Whether or not to schedule this task concurrently")

		boolean scheduler_concurrent() default false;

		@AttributeDefinition(name = "A parameter", description = "Can be configured in /system/console/configMgr")

		String myParameter() default "";

	}

	@Reference

	private ResourceResolverFactory resolverFactory;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private String myParameter;

	@Override

	public void run() {

		Session session;

		Page newPage;

		ResourceResolver resolver = null;

		PageManager pageManager;

		Map<String, Object> param = new HashMap<String, Object>();

		param.put(ResourceResolverFactory.SUBSERVICE, "jcr-service");

		try {

			resolver = resolverFactory.getServiceResourceResolver(param);

			String path1 = "/content/globalScheduler/en/news";

			session = resolver.adaptTo(Session.class);

			if (session != null) {

				SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

				Date today = new Date();

				String pageName = DATE_FORMAT.format(today);

				logger.info(pageName);

				String templatePath = "/conf/globalScheduler/settings/wcm/templates/content-page";

				logger.info(templatePath);

				// create a page manager instance

				pageManager = resolver.adaptTo(PageManager.class);

				newPage = pageManager.create(path1, pageName, templatePath, pageName);

				Node pageNode = newPage.adaptTo(Node.class);

				logger.info(newPage.toString());

				Node jcrNode = null;

				if (newPage.hasContent()) {

					jcrNode = newPage.getContentResource().adaptTo(Node.class);

				} else {

					jcrNode = pageNode.addNode("jcr:content", "cq:PageContent");

				}

				jcrNode.setProperty("sling:resourceType", "globalScheduler/components/structure/page");

				Node parNode = jcrNode.addNode("par");

				parNode.setProperty("sling:resourceType", "foundation/components/parsys");

				Node textNode = parNode.addNode("text");

				textNode.setProperty("sling:resourceType", "foundation/components/text");

				textNode.setProperty("text", "Test page");

				session.save();

				session.refresh(true);

			}

		} catch (WCMException e) {

			e.printStackTrace();

		} catch (ItemExistsException e) {

			e.printStackTrace();

		} catch (PathNotFoundException e) {

			e.printStackTrace();

		} catch (NoSuchNodeTypeException e) {

			e.printStackTrace();

		} catch (LockException e) {

			e.printStackTrace();

		} catch (VersionException e) {

			e.printStackTrace();

		} catch (ConstraintViolationException e) {

			e.printStackTrace();

		} catch (RepositoryException e) {

			e.printStackTrace();

		} catch (LoginException e) {

			e.printStackTrace();

		}

	}

	@Activate

	protected void activate(final Config config) {

		myParameter = config.myParameter();

	}

}
