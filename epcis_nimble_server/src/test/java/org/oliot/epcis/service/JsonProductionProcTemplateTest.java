package org.oliot.epcis.service;

import eu.nimble.service.epcis.EPCISRepositoryApplication;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oliot.epcis.service.capture.mongodb.MongoCaptureUtil;
import org.oliot.epcis.service.query.mongodb.MongoQueryService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EPCISRepositoryApplication.class)
public class JsonProductionProcTemplateTest {

    MongoCaptureUtil mongoCaptureUtil = new MongoCaptureUtil();
    MongoQueryService mongoQueryService = new MongoQueryService();

    @Test
    public void captureJSONProductionProcTemplate() {
        String productProcessTemplateJson = "{\n" +
                "  \"productClass\": \"testProductionClass\",\n" +
                "  \"productionProcessTemplate\": [\n" +
                "    {\n" +
                "      \"id\": \"1\",\n" +
                "      \"hasPrev\": \"\",\n" +
                "      \"readPoint\": \"urn:epc:id:sgln:readPoint.lindbacks.1\",\n" +
                "      \"bizLocation\": \"urn:epc:id:sgln:bizLocation.lindbacks.2\",\n" +
                "      \"bizStep\": \"urn:epcglobal:cbv:bizstep:other\",\n" +
                "      \"hasNext\": \"2\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"4\",\n" +
                "      \"hasPrev\": \"3\",\n" +
                "      \"readPoint\": \"urn:epc:id:sgln:readPoint.lindbacks.4\",\n" +
                "      \"bizLocation\": \"urn:epc:id:sgln:bizLocation.lindbacks.5\",\n" +
                "      \"bizStep\": \"urn:epcglobal:cbv:bizstep:shipping\",\n" +
                "      \"hasNext\": \"\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        JSONObject jsonProductionProc = new JSONObject(productProcessTemplateJson);
        mongoCaptureUtil.captureJSONProductionProcTemplate(jsonProductionProc, "token1");
        try {
            assertFalse(mongoQueryService.pollProductionProcTemplateQuery("testProductionClass").contains("[]"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    // this class "test" does not have entry on database
    public void getProductionProcessTemplateWithoutClass() {
        try {
            assertTrue(mongoQueryService.pollProductionProcTemplateQuery("test").contains("[]"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    // this class "testProductionClass" have entry of database
    public void getProductionProcessTemplateWithClass() {
        try {
            assertFalse(mongoQueryService.pollProductionProcTemplateQuery("testProductionClass").contains("[]"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
