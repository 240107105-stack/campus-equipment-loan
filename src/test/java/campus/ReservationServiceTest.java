package campus;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationServiceTest {
    private static final String STUDENT_ID = "240107105";
    private static final String ITEM_ID = "laptop-01";

    @Test
    void eligibleStudentIsAcceptedSavedOnceAndSentOneConfirmation() {
        FakeRepository repository = new FakeRepository(1);
        FakeMessagePort messages = new FakeMessagePort();
        ReservationService service = new ReservationService(repository, messages);

        ReservationResult result = service.reserve(STUDENT_ID, ITEM_ID);

        assertEquals(ReservationResult.ACCEPTED, result);
        assertEquals(List.of(new SavedReservation(STUDENT_ID, ITEM_ID)), repository.saved);
        assertEquals(List.of(new SentMessage(STUDENT_ID,
                "Reservation confirmed for item " + ITEM_ID)), messages.sent);
    }

    @Test
    void studentWithNoActiveReservationsIsAccepted() {
        FakeRepository repository = new FakeRepository(0);
        FakeMessagePort messages = new FakeMessagePort();
        ReservationService service = new ReservationService(repository, messages);

        ReservationResult result = service.reserve(STUDENT_ID, ITEM_ID);

        assertEquals(ReservationResult.ACCEPTED, result);
        assertEquals(List.of(new SavedReservation(STUDENT_ID, ITEM_ID)), repository.saved);
        assertEquals(List.of(new SentMessage(STUDENT_ID,
                "Reservation confirmed for item " + ITEM_ID)), messages.sent);
    }

    @Test
    void exactlyTwoActiveReservationsAreRejectedWithoutSavingOrMessaging() {
        assertRejectedWithoutSideEffects(2);
    }

    @Test
    void moreThanTwoActiveReservationsAreRejectedWithoutSavingOrMessaging() {
        assertRejectedWithoutSideEffects(3);
    }

    private void assertRejectedWithoutSideEffects(int activeReservations) {
        FakeRepository repository = new FakeRepository(activeReservations);
        FakeMessagePort messages = new FakeMessagePort();
        ReservationService service = new ReservationService(repository, messages);

        ReservationResult result = service.reserve(STUDENT_ID, ITEM_ID);

        assertEquals(ReservationResult.REJECTED_LIMIT_REACHED, result);
        assertEquals(0, repository.saved.size(), "Rejected requests must not be saved");
        assertEquals(0, messages.sent.size(), "Rejected requests must not send confirmations");
    }

    private record SavedReservation(String studentId, String itemId) { }
    private record SentMessage(String studentId, String message) { }

    private static final class FakeRepository implements ReservationRepository {
        private final int activeReservations;
        private final List<SavedReservation> saved = new ArrayList<>();

        FakeRepository(int activeReservations) {
            this.activeReservations = activeReservations;
        }

        @Override
        public boolean hasOverdueLoan(String studentId) {
            throw new AssertionError("The active-limit variant must not query overdue loans");
        }

        @Override
        public int countActiveReservations(String studentId) {
            assertEquals(STUDENT_ID, studentId);
            return activeReservations;
        }

        @Override
        public void save(String studentId, String itemId) {
            saved.add(new SavedReservation(studentId, itemId));
        }
    }

    private static final class FakeMessagePort implements MessagePort {
        private final List<SentMessage> sent = new ArrayList<>();

        @Override
        public void send(String studentId, String message) {
            sent.add(new SentMessage(studentId, message));
        }
    }
}
