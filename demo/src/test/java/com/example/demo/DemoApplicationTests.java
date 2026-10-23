package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.apache.kafka.clients.producer.RecordMetadata;
import com.example.demo.event.bookingEvent;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.demo.model.Booking;
import com.example.demo.model.Train;
import com.example.demo.model.coach;
import com.example.demo.model.journey;
import com.example.demo.model.seat;
import com.example.demo.config.DemoDataInitializer;
import com.example.demo.dto.bookingRequest;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.TrainRepository;
import com.example.demo.repository.coachRepository;
import com.example.demo.repository.journeyrepository;
import com.example.demo.repository.seatRepository;
import com.example.demo.repository.userRepository;
import com.example.demo.service.BookingService;
import com.example.demo.model.user;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {
				"spring.datasource.url=jdbc:h2:mem:flashy-test;DB_CLOSE_DELAY=-1",
				"spring.datasource.username=sa",
				"spring.datasource.password=test-only-password",
				"spring.jpa.hibernate.ddl-auto=create-drop",
				"spring.security.user.name=test-admin",
				"spring.security.user.password=test-only-admin-password",
				"app.kafka.listener-enabled=false",
				"spring.kafka.admin.auto-create=false",
				"spring.data.redis.repositories.enabled=false",
				"app.demo-data.enabled=true"
		})
class DemoApplicationTests {
	private static final String TEST_ADMIN = "test-admin";
	private static final String TEST_ADMIN_PASSWORD = "test-only-admin-password";

	@Value("${local.server.port}")
	private int port;

	@MockitoBean
	private LettuceConnectionFactory redisConnectionFactory;

	@MockitoBean
	private RedisTemplate<String, Object> redisTemplate;

	@MockitoBean
	private KafkaTemplate<String, bookingEvent> kafkaTemplate;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void isolateExternalServices() {
		ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(anyString())).thenReturn(null);
		doNothing().when(valueOperations).set(anyString(), any());

		SendResult<String, bookingEvent> sendResult = mock(SendResult.class);
		RecordMetadata metadata = mock(RecordMetadata.class);
		when(sendResult.getRecordMetadata()).thenReturn(metadata);
		when(metadata.partition()).thenReturn(0);
		when(metadata.offset()).thenReturn(0L);
		when(kafkaTemplate.send(anyString(), anyString(), any(bookingEvent.class)))
				.thenReturn(CompletableFuture.completedFuture(sendResult));
	}

	@Autowired
	private TrainRepository trainRepository;

	@Autowired
	private coachRepository coachRepository;

	@Autowired
	private journeyrepository journeyRepository;

	@Autowired
	private seatRepository seatRepository;

	@Autowired
	private userRepository userRepository;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private BookingService bookingService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private DemoDataInitializer demoDataInitializer;

	@Test
	void contextLoads() {
	}

	@Test
	void protectsBookingEndpointsWithBasicAuthentication() throws Exception {
		HttpRequest request = HttpRequest.newBuilder(
				URI.create("http://localhost:" + port + "/api/bookings"))
				.GET()
				.build();

		HttpResponse<String> response = HttpClient.newHttpClient()
				.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(401, response.statusCode());
	}

	@Test
	void registersUsersWithHashedPasswordsAndDefaultRole() throws Exception {
		String email = System.nanoTime() + "@example.test";
		String requestBody = """
				{"name":"Test User","email":"%s","password":"test-only-user-password","role":"ADMIN"}
				""".formatted(email);
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/users"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();

		HttpResponse<String> response = authenticatedHttpClient()
				.send(request, HttpResponse.BodyHandlers.ofString());
		user registered = userRepository.findAll().stream()
				.filter(existing -> email.equals(existing.getEmail()))
				.findFirst()
				.orElseThrow();

		assertEquals(201, response.statusCode(), response.body());
		assertTrue(!response.body().contains("test-only-user-password"));
		assertTrue(passwordEncoder.matches("test-only-user-password", registered.getPassword()));
		assertEquals("USER", registered.getRole());
	}

	@Test
	void startsWithPersistedDemoBooking() throws Exception {
		assertTrue(bookingRepository.findAll().stream()
				.anyMatch(booking -> "demo.booking@example.test".equals(booking.getUser().getEmail())));
		assertGetContains(authenticatedHttpClient(), "/api/bookings", "demo.booking@example.test");
	}

	@Test
	void providesRepeatableLoadTestDataWithAvailableSeats() {
		demoDataInitializer.run();

		long demoUserCount = userRepository.findAll().stream()
				.filter(demoUser -> "demo.booking@example.test".equals(demoUser.getEmail())
						|| demoUser.getEmail().startsWith("demo.loadtest.user"))
				.count();
		long demoSeatCount = seatRepository.findAll().stream()
				.filter(demoSeat -> demoSeat.getSeatNumber().matches("DEMO-\\d+"))
				.count();
		long demoBookingCount = bookingRepository.findAll().stream()
				.filter(booking -> booking.getUser().getEmail().equals("demo.booking@example.test")
						|| booking.getUser().getEmail().startsWith("demo.loadtest.user"))
				.count();
		journey demoJourney = journeyRepository.findAll().stream()
				.filter(existing -> "DEMO-1001".equals(existing.getTrain().getTrainNumber()))
				.findFirst()
				.orElseThrow();
		seat availableDemoSeat = seatRepository.findAll().stream()
				.filter(existing -> "DEMO-11".equals(existing.getSeatNumber()))
				.findFirst()
				.orElseThrow();

		assertEquals(10, demoUserCount);
		assertEquals(100, demoSeatCount);
		assertEquals(10, demoBookingCount);
		assertTrue(bookingService.isSeatAvialable(demoJourney.getId(), availableDemoSeat.getId()));
	}

	@Test
	void allowsOnlyOneOfTwoUsersToBookTheSameSeatConcurrently() throws Exception {
		demoDataInitializer.run();
		HttpClient client = authenticatedHttpClient();
		HttpRequest dataRequest = HttpRequest.newBuilder(
				URI.create("http://localhost:" + port + "/api/demo/booking-race-data"))
				.GET()
				.build();
		HttpResponse<String> dataResponse = client.send(dataRequest, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, dataResponse.statusCode(), dataResponse.body());
		List<Integer> userIds = userRepository.findAll().stream()
				.filter(demoUser -> "demo.booking@example.test".equals(demoUser.getEmail())
						|| demoUser.getEmail().startsWith("demo.loadtest.user"))
				.map(user::getId)
				.limit(2)
				.toList();
		assertEquals(2, userIds.size(), "Two separate demo users are required for the booking race");
		int journeyId = extractJsonInteger(dataResponse.body(), "journeyId");
		int seatId = extractJsonInteger(dataResponse.body(), "seatId");
		List<CompletableFuture<HttpResponse<String>>> requests = new ArrayList<>();

		for (Integer userId : userIds) {
			String bookingBody = """
					{"userId":%d,"journeyId":%d,"seatId":%d}
					""".formatted(userId, journeyId, seatId);
			HttpRequest bookingRequest = HttpRequest.newBuilder(
					URI.create("http://localhost:" + port + "/api/bookings"))
					.timeout(Duration.ofSeconds(10))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(bookingBody))
					.build();
			requests.add(client.sendAsync(bookingRequest, HttpResponse.BodyHandlers.ofString()));
		}

		CompletableFuture.allOf(requests.toArray(CompletableFuture[]::new)).join();
		long created = requests.stream().map(CompletableFuture::join)
				.filter(response -> response.statusCode() == 201)
				.count();
		long rejected = requests.stream().map(CompletableFuture::join)
				.filter(response -> response.statusCode() == 400)
				.count();

		assertEquals(1, created);
		assertEquals(1, rejected);
	}

	@Test
	void createsFreshBookingDataThatCanBeBookedWithCreatedResponse() throws Exception {
		demoDataInitializer.run();
		HttpClient client = authenticatedHttpClient();
		HttpRequest dataRequest = HttpRequest.newBuilder(
				URI.create("http://localhost:" + port + "/api/demo/booking-data"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.noBody())
				.build();
		HttpResponse<String> dataResponse = client.send(dataRequest, HttpResponse.BodyHandlers.ofString());

		assertEquals(201, dataResponse.statusCode(), dataResponse.body());
		String bookingBody = """
				{"userId":%d,"journeyId":%d,"seatId":%d}
				""".formatted(
						extractJsonInteger(dataResponse.body(), "userId"),
						extractJsonInteger(dataResponse.body(), "journeyId"),
						extractJsonInteger(dataResponse.body(), "seatId"));
		HttpRequest bookingRequest = HttpRequest.newBuilder(
				URI.create("http://localhost:" + port + "/api/bookings"))
				.timeout(Duration.ofSeconds(10))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(bookingBody))
				.build();
		HttpResponse<String> bookingResponse = client.send(bookingRequest, HttpResponse.BodyHandlers.ofString());

		assertEquals(201, bookingResponse.statusCode(), bookingResponse.body());
		assertTrue(bookingResponse.body().contains("\"status\":\"PENDING\""), bookingResponse.body());
	}

	@Test
	void suggestsAnotherAvailableSeatWhenRequestedSeatIsBooked() throws Exception {
		demoDataInitializer.run();
		user demoUser = userRepository.findAll().stream()
				.filter(existing -> "demo.booking@example.test".equals(existing.getEmail()))
				.findFirst()
				.orElseThrow();
		journey demoJourney = journeyRepository.findAll().stream()
				.filter(existing -> "DEMO-1001".equals(existing.getTrain().getTrainNumber()))
				.findFirst()
				.orElseThrow();
		seat bookedSeat = seatRepository.findAll().stream()
				.filter(existing -> "DEMO-1".equals(existing.getSeatNumber()))
				.findFirst()
				.orElseThrow();

		String requestBody = """
				{"userId":%d,"journeyId":%d,"seatId":%d}
				""".formatted(demoUser.getId(), demoJourney.getId(), bookedSeat.getId());
		HttpRequest request = HttpRequest.newBuilder(
				URI.create("http://localhost:" + port + "/api/bookings/available-seat"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();

		HttpResponse<String> response = authenticatedHttpClient()
				.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode(), response.body());
		assertTrue(response.body().contains("\"userId\":" + demoUser.getId()), response.body());
		assertTrue(response.body().contains("\"journeyId\":" + demoJourney.getId()), response.body());
		int suggestedSeatId = extractJsonInteger(response.body(), "seatId");
		assertTrue(suggestedSeatId != bookedSeat.getId(), response.body());
		assertTrue(bookingService.isSeatAvialable(demoJourney.getId(), suggestedSeatId));
	}

	@Test
	void returnsJsonErrorWhenBookingReferencesMissingUser() throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/bookings"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("""
						{"userId":2147483647,"journeyId":1,"seatId":1}
						"""))
				.build();

		HttpResponse<String> response = authenticatedHttpClient()
				.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(404, response.statusCode(), response.body());
		assertTrue(response.headers().firstValue("Content-Type").orElse("")
				.startsWith("application/json"), response.headers().toString());
		assertTrue(response.body().contains("\"status\":404"), response.body());
		assertTrue(response.body().contains("\"message\":\"User not found\""), response.body());
		assertTrue(response.body().contains("\"timestamp\":"), response.body());
	}

	@Test
	void savesTrainUsingExistingNumberColumn() {
		Train train = new Train();
		train.setTrainNumber("12301");
		train.setName("Rajdhani Express");

		Train savedTrain = trainRepository.saveAndFlush(train);

		assertEquals("12301", savedTrain.getTrainNumber());
		assertEquals("Rajdhani Express", savedTrain.getName());
	}

	@Test
	void createsBookingFromRelatedEntityIds() {
		String suffix = Long.toString(System.nanoTime());

		user bookingUser = new user();
		bookingUser.setName("Booking Test");
		bookingUser.setEmail(suffix + "@example.com");
		bookingUser.setPassword("test-password");
		bookingUser.setRole("USER");
		bookingUser = userRepository.saveAndFlush(bookingUser);

		Train train = new Train();
		train.setTrainNumber(suffix);
		train.setName("Booking Test Train");
		train = trainRepository.saveAndFlush(train);

		coach bookingCoach = new coach();
		bookingCoach.setTrain(train);
		bookingCoach.setCoachNumber("C1");
		bookingCoach.setCoachType("AC");
		bookingCoach = coachRepository.saveAndFlush(bookingCoach);

		journey bookingJourney = new journey();
		bookingJourney.setTrain(train);
		bookingJourney.setJourneyDate(java.time.LocalDate.now());
		bookingJourney = journeyRepository.saveAndFlush(bookingJourney);

		seat bookingSeat = new seat();
		bookingSeat.setCoach(bookingCoach);
		bookingSeat.setSeatNumber("1");
		bookingSeat.setSeatType("WINDOW");
		bookingSeat = seatRepository.saveAndFlush(bookingSeat);

		bookingRequest request = new bookingRequest();
		request.setUserId(bookingUser.getId());
		request.setJourneyId(bookingJourney.getId());
		request.setSeatId(bookingSeat.getId());
		Booking savedBooking = bookingService.createBooking(request);

		assertEquals(bookingUser.getId(), savedBooking.getUser().getId());
		assertEquals(bookingJourney.getId(), savedBooking.getJourney().getId());
		assertEquals(bookingSeat.getId(), savedBooking.getSeat().getId());
		assertTrue(bookingRepository.count() >= 1);
	}

	@Test
	void postAndGetReturnsSavedResourcesAndRelationships() throws Exception {
		HttpClient client = authenticatedHttpClient();
		String suffix = Long.toString(System.nanoTime());

		int trainId = postAndGetId(client, "/api/trains",
				"""
				{"trainNumber":"%s","name":"Persistence Train"}
				""".formatted(suffix));
		assertGetContains(client, "/api/trains", "Persistence Train");

		int userId = postAndGetId(client, "/api/users",
				"""
				{"name":"Persistence User","email":"%s@example.com","password":"test-password","role":"USER"}
				""".formatted(suffix));
		assertGetContains(client, "/api/users", "Persistence User");

		int coachId = postAndGetId(client, "/api/coaches",
				"""
				{"train":{"id":%d},"coachNumber":"C1","coachType":"AC"}
				""".formatted(trainId));
		assertGetContains(client, "/api/coaches", "C1");

		int journeyId = postAndGetId(client, "/api/journeys",
				"""
				{"train":{"id":%d},"journeyDate":"2026-10-03"}
				""".formatted(trainId));
		assertGetContains(client, "/api/journeys", "2026-10-03");

		int seatId = postAndGetId(client, "/api/seats",
				"""
				{"coach":{"id":%d},"seatNumber":"1","seatType":"WINDOW"}
				""".formatted(coachId));
		assertGetContains(client, "/api/seats", "WINDOW");

		long bookingRequestStartedAt = System.nanoTime();
		postAndGetId(client, "/api/bookings",
				"""
				{"userId":%d,"journeyId":%d,"seatId":%d}
				""".formatted(userId, journeyId, seatId));
		long bookingResponseTimeMs = Duration.ofNanos(System.nanoTime() - bookingRequestStartedAt).toMillis();
		assertTrue(bookingResponseTimeMs < 5_000,
				"Booking request took " + bookingResponseTimeMs + " ms");
		assertGetContains(client, "/api/bookings", "PENDING");

		int stationId = postAndGetId(client, "/api/stations",
				"""
				{"name":"Persistence Station %s","code":"S%s"}
				""".formatted(suffix, suffix));
		assertGetContains(client, "/api/stations", "Persistence Station " + suffix);

		int routeId = postAndGetId(client, "/api/routes",
				"""
				{"name":"Persistence Route %s"}
				""".formatted(suffix));
		assertGetContains(client, "/api/routes", "Persistence Route " + suffix);

		postAndGetId(client, "/api/route-stops",
				"""
				{"route":{"id":%d},"station":{"id":%d},"stopOrder":1}
				""".formatted(routeId, stationId));
		assertGetContains(client, "/api/route-stops", "\"stopOrder\":1");
	}

	private int postAndGetId(HttpClient client, String path, String body) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
				.timeout(Duration.ofSeconds(10))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		assertEquals(201, response.statusCode(), response.body());
		com.fasterxml.jackson.databind.JsonNode json = new com.fasterxml.jackson.databind.ObjectMapper()
				.readTree(response.body());
		assertTrue(json.path("id").isInt(), response.body());
		return json.path("id").asInt();
	}

	private HttpClient authenticatedHttpClient() {
		return HttpClient.newBuilder()
				.authenticator(new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(
								TEST_ADMIN,
								TEST_ADMIN_PASSWORD.toCharArray());
					}
				})
				.build();
	}

	private int extractJsonInteger(String json, String field) {
		java.util.regex.Matcher matcher = java.util.regex.Pattern
				.compile("\"" + field + "\"\\s*:\\s*(\\d+)")
				.matcher(json);
		assertTrue(matcher.find(), json);
		return Integer.parseInt(matcher.group(1));
	}

	private void assertGetContains(HttpClient client, String path, String expected) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
				.GET()
				.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		assertEquals(200, response.statusCode(), response.body());
		assertTrue(response.body().contains(expected), response.body());
	}

}
