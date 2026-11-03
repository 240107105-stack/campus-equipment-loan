package campus;

public class ReservationService {
    private final ReservationRepository repository;
    private final MessagePort messagePort;

    public ReservationService(ReservationRepository repository, MessagePort messagePort) {
        this.repository = repository;
        this.messagePort = messagePort;
    }

    public ReservationResult reserve(String studentId, String itemId) {
        if (repository.countActiveReservations(studentId) >= 2) {
            return ReservationResult.REJECTED_LIMIT_REACHED;
        }

        repository.save(studentId, itemId);
        messagePort.send(studentId, "Reservation confirmed for item " + itemId);
        return ReservationResult.ACCEPTED;
    }
}
