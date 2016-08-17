/*
 * Copyright 2013 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.feed.resource;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoBeanIterator;
import org.hippoecm.hst.content.beans.standard.HippoDocumentBean;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.jaxrs.services.content.AbstractContentResource;
import org.onehippo.forge.feed.api.FeedDescriptor;
import org.onehippo.forge.feed.api.modifier.Modifier;
import org.onehippo.forge.feed.util.ConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version "$Id: RssFeedResource.java 9 2013-04-08 08:29:25Z ksalic $"
 */
@Produces({MediaType.APPLICATION_XML})
@Path("/feed:rss20descriptor/")  //todo: include exclude path
public class AbstractSyndicationResource<T, E> extends AbstractContentResource {

    private static Logger log = LoggerFactory.getLogger(AbstractSyndicationResource.class);

    private Modifier<T, E, FeedDescriptor> modifier;

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/")
    public String getFeed(@Context HttpServletRequest request, @Context HttpServletResponse response, @Context UriInfo info) {
        String feed = null;
        try {
            long indexCount = 0;
            long limit = 10L;
            String sortByField;
            HstRequestContext requestContext = getRequestContext(request);
            HippoDocumentBean contentBean = getRequestContentBean(requestContext, HippoDocumentBean.class);

            if (contentBean == null || !(contentBean instanceof FeedDescriptor)) {
                log.error("Cannot find feed descriptor. Check relative content path on feed's sitemap item.");
                return null;
            }
            FeedDescriptor<T, E> document = (FeedDescriptor) contentBean;
            final T channel = ConversionUtil.covertToAppropriateSyndicationFeed(document, document, this, requestContext);

            // prevent NPE from auto-deboxing
            if (document.getItemCount() != null) {
                limit = document.getItemCount();
            }
            sortByField = document.getSortByField();

            // get scope for search query
            HippoBean scopeBean = getMountContentBaseBean(requestContext);
            String scope = document.getScope();
            if (scope != null && !"".equals(scope)) {
                HippoBean folderBean = scopeBean.getBean(scope);
                if (folderBean != null) {
                    scopeBean = folderBean;
                }
            }

            // FIXME get date field from BE template?
            HstQuery hstQuery = null;
            String [] documentTypes = StringUtils.split(document.getDocumentType(), ", \t\r\n");
            if (!ArrayUtils.isEmpty(documentTypes)) {
                hstQuery = getHstQueryManager(requestContext.getSession(), requestContext)
                        .createQuery(scopeBean.getNode(), true, documentTypes);
            }
            if (hstQuery == null) {
                // dunno if we should go further at this point?
                hstQuery = getHstQueryManager(requestContext.getSession(), requestContext).createQuery(scopeBean);
            }
            if (sortByField != null) {
                hstQuery.addOrderByDescending(sortByField);
            }

            hstQuery.setLimit((int) limit);
            if (modifier != null) {
                modifier.modifyHstQuery(requestContext, hstQuery, document);
            }
            HstQueryResult queryResult = hstQuery.execute();
            HippoBeanIterator beans = queryResult.getHippoBeans();

            List entries = new ArrayList();
            while (beans.hasNext() && indexCount < limit) {
                HippoBean bean = beans.nextHippoBean();
                if (bean != null) {
                    final E item = ConversionUtil.covertToAppropriateSyndicationEntry(document, bean, this, requestContext);
                    if (modifier != null) {
                        modifier.modifyEntry(requestContext, item, bean);
                    }

                    entries.add(item);
                }
                indexCount++;
            }
            document.set(channel, entries);
            if (modifier != null) {
                modifier.modifyFeed(requestContext, channel, document);
            }
            feed = document.process(channel);
        } catch (Exception e) {
            log.error("Cannot execute query for RSS feed. {e}", e);
        }
        return feed;
    }


    public Modifier<T, E, FeedDescriptor> getModifier() {
        return modifier;
    }

    public void setModifier(final Modifier<T, E, FeedDescriptor> modifier) {
        this.modifier = modifier;
    }
}
