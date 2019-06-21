package org.oliot.epcis.service.query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringJUnit4ClassRunner.class)
public class RESTLikeQueryServiceTest {


    private MockMvc mockMvc;

    @InjectMocks
    RESTLikeQueryService restLikeQueryService;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(restLikeQueryService)
                .build();
    }

    String exampleEventJson = "[\n" +
            "    {\n" +
            "        \"eventTimeZoneOffset\": \"-06:00\",\n" +
            "        \"userPartyID\": \"user1\",\n" +
            "        \"eventType\": \"ObjectEvent\",\n" +
            "        \"any\": {\n" +
            "            \"example:temperature\": \"36\",\n" +
            "            \"example:emg\": \"22\",\n" +
            "            \"@example\": \"http://ns.example.com/epcis\",\n" +
            "            \"example:ecg\": \"11\"\n" +
            "        },\n" +
            "        \"bizStep\": \"urn:epcglobal:cbv:bizstep:receiving\",\n" +
            "        \"disposition\": \"urn:epcglobal:cbv:disp:in_progress\",\n" +
            "        \"recordTime\": {\n" +
            "            \"$date\": 1441967067206\n" +
            "        },\n" +
            "        \"readPoint\": {\n" +
            "            \"id\": \"urn:epc:id:sgln:0012345.11111.400\"\n" +
            "        },\n" +
            "        \"bizTransactionList\": [\n" +
            "            {\n" +
            "                \"urn:epcglobal:cbv:btt:po\": \"http://transaction.acme.com/po/12345678\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"urn:epcglobal:cbv:btt:desadv\": \"urn:epcglobal:cbv:bt:0614141073467:1152\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"eventTime\": {\n" +
            "            \"$date\": 1112668411116\n" +
            "        },\n" +
            "        \"action\": \"OBSERVE\",\n" +
            "        \"bizLocation\": {\n" +
            "            \"id\": \"urn:epc:id:sgln:0012345.11111.0\"\n" +
            "        },\n" +
            "        \"_id\": {\n" +
            "            \"$oid\": \"5d0b49b83ae13a05982d55b7\"\n" +
            "        },\n" +
            "        \"epcList\": [\n" +
            "            {\n" +
            "                \"epc\": \"urn:epc:id:sgtin:0614141.107346.2018\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "]";

    @Test
    public void getQueryNames() throws Exception{

        String queryExample = "[\n" +
                "    \"SimpleEventQuery\",\n" +
                "    \"SimpleMasterDataQuery\"\n" +
                "]";

        mockMvc.perform(MockMvcRequestBuilders.get("/Service/GetQueryNames")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
