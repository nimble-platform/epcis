package org.oliot.epcis.service.api;

import eu.nimble.service.epcis.EPCISRepositoryApplication;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EPCISRepositoryApplication.class)
public class EventDataTest {

    @Value("${test.base-url}")
    private String baseUrl;

    private String getBaseUrl() {
        return baseUrl + "/Service";
    }

    @Value("${test.accessToken}")
    private String accessToken;

    @Test
    public void getJsonEventQuery() {
        this.getRestAPITest(this.getBaseUrl() + "/Poll/SimpleEventQuery?format=JSON");
    }

    @Test
    public void getXmlEventQuery() {
        this.getRestAPITest(this.getBaseUrl() + "/Poll/SimpleEventQuery?format=XML");
    }

    private void getRestAPITest(String url) {
        try {
            HttpUriRequest request = new HttpGet(url);
            request.addHeader("Authorization", accessToken);
            HttpResponse httpResponse = HttpClientBuilder.create().build().execute( request );
            Assert.assertThat(
                    httpResponse.getStatusLine().getStatusCode(),
                    IsEqual.equalTo(HttpStatus.SC_OK));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

