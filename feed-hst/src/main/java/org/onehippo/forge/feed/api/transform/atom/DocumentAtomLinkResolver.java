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

package org.onehippo.forge.feed.api.transform.atom;

import java.util.ArrayList;
import java.util.List;

import com.sun.syndication.feed.atom.Link;

import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.linking.HstLink;
import org.hippoecm.hst.core.linking.HstLinkCreator;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.onehippo.forge.feed.api.annot.ContextTransformable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version "$Id$"
 */
public class DocumentAtomLinkResolver {

    private static Logger log = LoggerFactory.getLogger(DocumentAtomLinkResolver.class);

    @ContextTransformable
    public List<Link> getHstLink(HstRequestContext context, HippoBean document) {
        final HstLinkCreator hstLinkCreator = context.getHstLinkCreator();
        final HstLink hstChannelLink = hstLinkCreator.create(document, context);
        final String link = hstChannelLink.toUrlForm(context, true);
        Link atomLink = new Link();
        atomLink.setRel("self");
        atomLink.setHref(link);
        final List<Link> linkList = new ArrayList<Link>() ;
        linkList.add(atomLink);
        return linkList;
    }

}
