/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.http.client.cache;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.annotation.Immutable;
import org.apache.http.message.HeaderGroup;

/**
 * Structure used to store an {@link HttpResponse} in a cache. Some entries can optionally depend
 * on system resources that may require explicit deallocation. In such a case {@link #getResource()}
 * should return a non-null instance of {@link Resource} that must be deallocated by calling
 * {@link Resource#dispose()} method when no longer used.
 *
 * @since 4.1
 */
@Immutable
public class HttpCacheEntry implements Serializable {

    private static final long serialVersionUID = -6300496422359477413L;

    private final Date requestDate;
    private final Date responseDate;
    private final StatusLine statusLine;
    private final HeaderGroup responseHeaders;
    private final Resource resource;
    private final Set<String> variantURIs;
    private final Map<String,String> variantMap;

    private HttpCacheEntry(
            final Date requestDate,
            final Date responseDate,
            final StatusLine statusLine,
            final Header[] responseHeaders,
            final Resource resource,
            final Set<String> variants,
            final Map<String,String> variantMap) {
        super();
        if (requestDate == null) {
            throw new IllegalArgumentException("Request date may not be null");
        }
        if (responseDate == null) {
            throw new IllegalArgumentException("Response date may not be null");
        }
        if (statusLine == null) {
            throw new IllegalArgumentException("Status line may not be null");
        }
        if (responseHeaders == null) {
            throw new IllegalArgumentException("Response headers may not be null");
        }
        if (resource == null) {
            throw new IllegalArgumentException("Resource may not be null");
        }
        this.requestDate = requestDate;
        this.responseDate = responseDate;
        this.statusLine = statusLine;
        this.responseHeaders = new HeaderGroup();
        this.responseHeaders.setHeaders(responseHeaders);
        this.resource = resource;
        this.variantURIs = variants != null
            ? new HashSet<String>(variants) : null;
        this.variantMap = variantMap != null
            ? new HashMap<String,String>(variantMap)
            : null;
    }
    
    /**
     * Create a new {@link HttpCacheEntry} with a set of variant
     * URIs.
     *
     * @param requestDate
     *          Date/time when the request was made (Used for age
     *            calculations)
     * @param responseDate
     *          Date/time that the response came back (Used for age
     *            calculations)
     * @param statusLine
     *          HTTP status line from origin response
     * @param responseHeaders
     *          Header[] from original HTTP Response
     * @param resource representing origin response body
     * @param variants set of cache keys that are variants of this
     *   "parent" entry
     */
    @Deprecated
    public HttpCacheEntry(
            final Date requestDate,
            final Date responseDate,
            final StatusLine statusLine,
            final Header[] responseHeaders,
            final Resource resource,
            final Set<String> variants) {
        this(requestDate, responseDate, statusLine, responseHeaders,
                resource, variants, null);
    }

    /**
     * Create a new {@link HttpCacheEntry}.
     *
     * @param requestDate
     *          Date/time when the request was made (Used for age
     *            calculations)
     * @param responseDate
     *          Date/time that the response came back (Used for age
     *            calculations)
     * @param statusLine
     *          HTTP status line from origin response
     * @param responseHeaders
     *          Header[] from original HTTP Response
     * @param resource representing origin response body
     */
    public HttpCacheEntry(Date requestDate, Date responseDate, StatusLine statusLine,
            Header[] responseHeaders, Resource resource) {
        this(requestDate, responseDate, statusLine, responseHeaders, resource, null,
                new HashMap<String,String>());
    }

    /**
     * Create a new {@link HttpCacheEntry} with variants.
     * @param requestDate
     *          Date/time when the request was made (Used for age
     *            calculations)
     * @param responseDate
     *          Date/time that the response came back (Used for age
     *            calculations)
     * @param statusLine
     *          HTTP status line from origin response
     * @param responseHeaders
     *          Header[] from original HTTP Response
     * @param resource representing origin response body
     * @param variantMap describing cache entries that are variants
     *   of this parent entry; each cache key should map to itself
     */
    public HttpCacheEntry(Date requestDate, Date responseDate,
            StatusLine statusLine, Header[] responseHeaders,
            Resource resource, Map<String, String> variantMap) {
        this(requestDate, responseDate, statusLine, responseHeaders,
             resource, null, variantMap);
    }

    public StatusLine getStatusLine() {
        return this.statusLine;
    }

    public ProtocolVersion getProtocolVersion() {
        return this.statusLine.getProtocolVersion();
    }

    public String getReasonPhrase() {
        return this.statusLine.getReasonPhrase();
    }

    public int getStatusCode() {
        return this.statusLine.getStatusCode();
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public Date getResponseDate() {
        return responseDate;
    }

    public Header[] getAllHeaders() {
        return responseHeaders.getAllHeaders();
    }

    public Header getFirstHeader(String name) {
        return responseHeaders.getFirstHeader(name);
    }

    public Header[] getHeaders(String name) {
        return responseHeaders.getHeaders(name);
    }

    public Resource getResource() {
        return this.resource;
    }
    
    public boolean hasVariants() {
        return getFirstHeader(HeaderConstants.VARY) != null;
    }

    @Deprecated
    public Set<String> getVariantURIs() {
        if (variantMap != null) {
            return Collections.unmodifiableSet(new HashSet<String>(variantMap.values()));
        }
        return Collections.unmodifiableSet(variantURIs);
    }

    public Map<String, String> getVariantMap() {
        if (variantMap == null) {
            throw new UnsupportedOperationException("variant maps not" +
                    "supported if constructed with deprecated variant set");
        }
        return Collections.unmodifiableMap(variantMap);
    }

    @Override
    public String toString() {
        return "[request date=" + this.requestDate + "; response date=" + this.responseDate
                + "; statusLine=" + this.statusLine + "]";
    }
}
