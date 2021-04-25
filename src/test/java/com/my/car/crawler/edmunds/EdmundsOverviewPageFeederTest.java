package com.my.car.crawler.edmunds;

import org.jsoup.nodes.Document;
import org.junit.Test;
//import org.mockserver.client.MockServerClient;
//import org.mockserver.mock.action.ExpectationCallback;
//import org.mockserver.model.HttpRequest;
//import org.mockserver.model.HttpResponse;

import java.util.Iterator;

import static org.junit.Assert.*;
//import static org.mockserver.model.HttpClassCallback.callback;
//import static org.mockserver.model.HttpRequest.request;
//import static org.mockserver.model.HttpResponse.response;

public class EdmundsOverviewPageFeederTest {
//    private static final MockServerClient mockServer = new MockServerClient("www.edmunds.com", 80);
//
//    @BeforeClass
//    public static void init() {
//        mockServer.when(
//                request()
//                        .withMethod("GET")
//                        .withPath("/used-all")
//                        .withQueryStringParameter("radius")
//                        .withQueryStringParameter("pagenumber"))
//                .respond(callback().withCallbackClass(UsedAllCallback.class));
//    }
//
//    public static class UsedAllCallback implements ExpectationCallback<HttpResponse> {
//        private static final String template = "<html><head><title>Template Response</title></head>"
//                + "<body><p>pagenumber:%s</p><p>radius:%s</p></body></html>";
//
//        @Override
//        public HttpResponse handle(HttpRequest httpRequest) throws Exception {
//            HttpResponse httpResponse = response().withStatusCode(200);
//            String body = String.format(
//                    template,
//                    httpRequest.getFirstQueryStringParameter("pagenumber"),
//                    httpRequest.getFirstQueryStringParameter("radius"));
//            httpResponse.withBody(body);
//            return httpResponse;
//        }
//    }

    @Test
    public void testIterator_invalidPageRange_success() {
        EdmundsOverviewPageFeeder feeder = new EdmundsOverviewPageFeeder(10, 10);
        Iterator<Document> iterator = feeder.iterator();
        assertFalse(iterator.hasNext());
    }
}
