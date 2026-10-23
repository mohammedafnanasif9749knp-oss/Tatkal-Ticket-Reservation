package com.example.demo;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class concurrencytest {

    public static void main(String[] args) throws Exception {

        int numberOfRequests = 50;

        ExecutorService executor =
                Executors.newFixedThreadPool(numberOfRequests);

        HttpClient client = HttpClient.newHttpClient();

        String json = """
                {
                    "userId": 35,
                    "journeyId": 1,
                    "seatId": 66
                }
                """;

        List<Future<Integer>> results = new ArrayList<>();

        for (int i = 0; i < numberOfRequests; i++) {

            results.add(executor.submit(() -> {

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(
                                "http://localhost:8080/api/bookings"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response =
                        client.send(
                                request,
                                HttpResponse.BodyHandlers.ofString());

                return response.statusCode();
            }));
        }

        int success = 0;
        int failed = 0;

        for (Future<Integer> result : results) {

            int status = result.get();

            System.out.println("Status: " + status);

            if (status == 201) {
                success++;
            } else {
                failed++;
            }
        }

        executor.shutdown();

        System.out.println("\n========== RESULT ==========");
        System.out.println("Total requests : " + numberOfRequests);
        System.out.println("Successful     : " + success);
        System.out.println("Failed         : " + failed);
    }
}
