# Task 6 — Karzhaubay Bakdaulet, 240107105

1. I implemented variant 5–9: the active reservation limit, because my student ID ends in 5. Two or more active reservations return `REJECTED_LIMIT_REACHED`.
2. My first test protected the existing successful behavior: an eligible student receives `ACCEPTED`, the correct reservation is saved exactly once, and one confirmation is sent to that student. It passed before the new rule was added.
3. The dependency seams are the `ReservationRepository` and `MessagePort` interfaces injected through the `ReservationService` constructor. Tests replace them with in-memory fakes.
4. Rejection must produce no save or message because no reservation was accepted. Saving would create an invalid booking, and sending confirmation would mislead the student.
5. Equipment availability and inventory allocation could also be affected: a rejected request must not reserve an item.
