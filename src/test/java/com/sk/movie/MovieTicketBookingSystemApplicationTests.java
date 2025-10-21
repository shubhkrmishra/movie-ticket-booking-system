package com.sk.movie;

import com.sk.movie.dto.BookingRequest;
import com.sk.movie.services.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootTest
class MovieTicketBookingSystemApplicationTests {

	@Autowired
	private BookingService bookingService;

	@Test
	public void testConcurrentBookingSameSeat() throws InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newFixedThreadPool(10);
		Long showId = 1L;
		List<Long> seatIds = Arrays.asList(1L, 2L); // Same seats

		List<Future<Boolean>> futures = new ArrayList<>();

		// 10 users trying to book the same seats simultaneously
		for (int i = 0; i < 10; i++) {
			final Long userId = (long) (i + 1);
			Future<Boolean> future = executor.submit(() -> {
				try {
					BookingRequest request = new BookingRequest();
					request.setShowId(showId);
					request.setSeatIds(seatIds);
					bookingService.createBooking(request, userId);
					return true;
				} catch (Exception e) {
					return false; // Booking failed due to concurrency
				}
			});
			futures.add(future);
		}

		int successCount = 0;
		for (Future<Boolean> future : futures) {
			if (future.get()) {
				successCount++;
			}
		}

		executor.shutdown();

		// Only ONE booking should succeed
		assert successCount == 1 : "Expected exactly 1 successful booking, got " + successCount;
	}

}
