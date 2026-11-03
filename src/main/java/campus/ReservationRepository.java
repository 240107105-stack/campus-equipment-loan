package campus;

public interface ReservationRepository {
    boolean hasOverdueLoan(String studentId);

    int countActiveReservations(String studentId);

    void save(String studentId, String itemId);
}
