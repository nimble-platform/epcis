package org.oliot.epcis.service.query;

import eu.nimble.service.epcis.EPCISRepositoryApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oliot.epcis.service.query.mongodb.MongoQueryService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EPCISRepositoryApplication.class)
public class MongoQueryServiceTest {
    MongoQueryService mongoQueryService = new MongoQueryService();

    @Test
    public void getProductionProcessTemplateWithoutClass() {
        try {
            // this class "test" does not have entry on database
            assertTrue(mongoQueryService.pollProductionProcTemplateQuery("test").contains("[]"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getProductionProcessTemplateWithClass() {
        try {
            // this class "lindbacks_test" have entry of database
            assertFalse(mongoQueryService.pollProductionProcTemplateQuery("lindbacks_test").contains("[]"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
