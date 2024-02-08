package org.example;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final long timeUnitMillis;
    private static List<Long> startTimes = new LinkedList<>();


    private synchronized void checkTimeLimits() throws InterruptedException {
        long diff;

        if (startTimes.size() == requestLimit) {
            diff = System.currentTimeMillis() - startTimes.remove(0) - timeUnitMillis;
            if(diff < 0) {
                Thread.sleep(-diff);
            }
        }
        startTimes.add(System.currentTimeMillis());
    }

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        if(requestLimit <= 0) {
            throw new RuntimeException("The request limit for the ismp.crpt.ru API must be greater than zero");
        }
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.timeUnitMillis = timeUnit.toMillis(1);
    }

    public String createDocument(Document document, String signature) throws InterruptedException, IOException {

        //Проверка по временным ограничениям
        checkTimeLimits();

        ObjectMapper objectMapper = new ObjectMapper();

        String requestBody = objectMapper.writeValueAsString(document);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                .header("Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        return response.body();

    }

    @Getter
    @Setter
    private static class Document {

        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private Boolean importRequest;
        private String owner_inn;
        private String producer_inn;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private Date productionDate;
        private String production_type;
        private Products products;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private Date regDate;
        private String regNumber;

    }

    @Getter
    @Setter
    private static class Products {

        private String certificate_document;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private Date certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private Date production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;

    }

    @Getter
    @Setter
    private static class Description {
        private String participantInn;
    }
}


