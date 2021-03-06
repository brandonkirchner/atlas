/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas.web.integration;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.model.typedef.AtlasBaseTypeDef;
import org.apache.atlas.v1.model.instance.Id;
import org.apache.atlas.v1.model.instance.Referenceable;
import org.apache.atlas.v1.model.instance.Struct;
import org.apache.atlas.v1.model.typedef.*;
import org.apache.atlas.v1.typesystem.types.utils.TypesUtil;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * Search Integration Tests.
 */
public class MetadataDiscoveryJerseyResourceIT extends BaseResourceIT {

    private String tagName;
    private String dbName;

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();
        dbName = "db"+randomString();
        createTypes();
        createInstance( createHiveDBInstanceV1(dbName) );
    }

    // Disabling DSL tests
    @Test (enabled = false)
    public void testSearchByDSL() throws Exception {
        String dslQuery = "from "+ DATABASE_TYPE + " name=\"" + dbName + "\"";
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("query", dslQuery);
        JSONObject response = atlasClientV1.callAPIWithQueryParams(AtlasClient.API_V1.SEARCH_DSL, queryParams);

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.get(AtlasClient.REQUEST_ID));

        assertEquals(response.getString("query"), dslQuery);
        assertEquals(response.getString("queryType"), "dsl");

        JSONArray results = response.getJSONArray(AtlasClient.RESULTS);
        assertNotNull(results);
        assertEquals(results.length(), 1);

        int numRows = response.getInt(AtlasClient.COUNT);
        assertEquals(numRows, 1);
    }

    // Disabling DSL tests, will be enabled when new implementation is ready
    @Test (enabled = false)
    public void testSearchDSLLimits() throws Exception {

        //search without new parameters of limit and offset should work
        String dslQuery = "from "+ DATABASE_TYPE + " name=\"" + dbName + "\"";
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("query", dslQuery);
        JSONObject response = atlasClientV1.callAPIWithQueryParams(AtlasClient.API_V1.SEARCH_DSL, queryParams);
        assertNotNull(response);

        //higher limit, all results returned
        JSONArray results = atlasClientV1.searchByDSL(dslQuery, 10, 0);
        assertEquals(results.length(), 1);

        //default limit and offset -1, all results returned
        results = atlasClientV1.searchByDSL(dslQuery, -1, -1);
        assertEquals(results.length(), 1);

        //uses the limit parameter passed
        results = atlasClientV1.searchByDSL(dslQuery, 1, 0);
        assertEquals(results.length(), 1);

        //uses the offset parameter passed
        results = atlasClientV1.searchByDSL(dslQuery, 10, 1);
        assertEquals(results.length(), 0);

        //limit > 0
        try {
            atlasClientV1.searchByDSL(dslQuery, 0, 10);
            fail("Expected BAD_REQUEST");
        } catch (AtlasServiceException e) {
            assertEquals(e.getStatus(), ClientResponse.Status.BAD_REQUEST, "Got " + e.getStatus());
        }

        //limit > maxlimit
        try {
            atlasClientV1.searchByDSL(dslQuery, Integer.MAX_VALUE, 10);
            fail("Expected BAD_REQUEST");
        } catch (AtlasServiceException e) {
            assertEquals(e.getStatus(), ClientResponse.Status.BAD_REQUEST, "Got " + e.getStatus());
        }

        //offset >= 0
        try {
            atlasClientV1.searchByDSL(dslQuery, 10, -2);
            fail("Expected BAD_REQUEST");
        } catch (AtlasServiceException e) {
            assertEquals(e.getStatus(), ClientResponse.Status.BAD_REQUEST, "Got " + e.getStatus());
        }
    }

    @Test(expectedExceptions = AtlasServiceException.class)
    public void testSearchByDSLForUnknownType() throws Exception {
        String dslQuery = "from blah";
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("query", dslQuery);
        atlasClientV1.callAPIWithQueryParams(AtlasClient.API_V1.SEARCH_DSL, queryParams);
    }

    @Test
    public void testSearchUsingGremlin() throws Exception {
        String query = "g.V.has('type', '" + BaseResourceIT.HIVE_TABLE_TYPE + "').toList()";
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("query", query);

        JSONObject response = atlasClientV1.callAPIWithQueryParams(AtlasClient.API_V1.GREMLIN_SEARCH, queryParams);

        assertNotNull(response);
        assertNotNull(response.get(AtlasClient.REQUEST_ID));

        assertEquals(response.getString("query"), query);
        assertEquals(response.getString("queryType"), "gremlin");
    }

    // Disabling DSL tests
    @Test (enabled = false)
    public void testSearchUsingDSL() throws Exception {
        //String query = "from dsl_test_type";
        String query = "from "+ DATABASE_TYPE + " name=\"" + dbName +"\"";
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("query", query);
        JSONObject response = atlasClientV1.callAPIWithQueryParams(AtlasClient.API_V1.SEARCH, queryParams);

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.get(AtlasClient.REQUEST_ID));

        assertEquals(response.getString("query"), query);
        assertEquals(response.getString("queryType"), "dsl");
    }

    // Disabling DSL tests
    @Test (enabled = false)
    public void testSearchFullTextOnDSLFailure() throws Exception {
        String query = "*";
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("query", query);
        JSONObject response = atlasClientV1.callAPIWithQueryParams(AtlasClient.API_V1.SEARCH, queryParams);

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.get(AtlasClient.REQUEST_ID));

        assertEquals(response.getString("query"), query);
        assertEquals(response.getString("queryType"), "full-text");
    }

    @Test(enabled = false, dependsOnMethods = "testSearchDSLLimits")
    public void testSearchUsingFullText() throws Exception {
        JSONObject response = atlasClientV1.searchByFullText(dbName, 10, 0);
        assertNotNull(response.get(AtlasClient.REQUEST_ID));

        assertEquals(response.getString("query"), dbName);
        assertEquals(response.getString("queryType"), "full-text");

        JSONArray results = response.getJSONArray(AtlasClient.RESULTS);
        assertEquals(results.length(), 1, "Results: " + results);

        JSONObject row = results.getJSONObject(0);
        assertNotNull(row.get("guid"));
        assertEquals(row.getString("typeName"), DATABASE_TYPE);
        assertNotNull(row.get("score"));

        int numRows = response.getInt(AtlasClient.COUNT);
        assertEquals(numRows, 1);

        //API works without limit and offset
        String query = dbName;
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("query", query);
        response = atlasClientV1.callAPIWithQueryParams(AtlasClient.API_V1.SEARCH_FULL_TEXT, queryParams);
        results = response.getJSONArray(AtlasClient.RESULTS);
        assertEquals(results.length(), 1);

        //verify passed in limits and offsets are used
        //higher limit and 0 offset returns all results
        results = atlasClientV1.searchByFullText(query, 10, 0).getJSONArray(AtlasClient.RESULTS);
        assertEquals(results.length(), 1);

        //offset is used
        results = atlasClientV1.searchByFullText(query, 10, 1).getJSONArray(AtlasClient.RESULTS);
        assertEquals(results.length(), 0);

        //limit is used
        results = atlasClientV1.searchByFullText(query, 1, 0).getJSONArray(AtlasClient.RESULTS);
        assertEquals(results.length(), 1);

        //higher offset returns 0 results
        results = atlasClientV1.searchByFullText(query, 1, 2).getJSONArray(AtlasClient.RESULTS);
        assertEquals(results.length(), 0);
    }

    private void createTypes() throws Exception {
        createTypeDefinitionsV1();

        ClassTypeDefinition dslTestTypeDefinition = TypesUtil
                .createClassTypeDef("dsl_test_type", null, Collections.<String>emptySet(),
                        TypesUtil.createUniqueRequiredAttrDef("name", AtlasBaseTypeDef.ATLAS_TYPE_STRING),
                        TypesUtil.createRequiredAttrDef("description", AtlasBaseTypeDef.ATLAS_TYPE_STRING));

        TraitTypeDefinition classificationTraitDefinition = TypesUtil
                .createTraitTypeDef("Classification", null, Collections.<String>emptySet(),
                        TypesUtil.createRequiredAttrDef("tag", AtlasBaseTypeDef.ATLAS_TYPE_STRING));
        TypesDef typesDef = new TypesDef(Collections.<EnumTypeDefinition>emptyList(), Collections.<StructTypeDefinition>emptyList(),
                Collections.singletonList(classificationTraitDefinition), Collections.singletonList(dslTestTypeDefinition));
        createType(typesDef);
    }

    private Id createInstance() throws Exception {
        Referenceable entityInstance = new Referenceable("dsl_test_type", "Classification");
        entityInstance.set("name", randomString());
        entityInstance.set("description", randomString());


        Struct traitInstance = (Struct) entityInstance.getTrait("Classification");
        tagName = randomString();
        traitInstance.set("tag", tagName);

        List<String> traits = entityInstance.getTraitNames();
        assertEquals(traits.size(), 1);

        return createInstance(entityInstance);
    }
}
