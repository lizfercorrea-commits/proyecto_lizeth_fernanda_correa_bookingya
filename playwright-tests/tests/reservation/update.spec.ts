import { test, expect } from '@playwright/test';
import { createGuest, createRoom, createReservation } from '../helpers/api';

test('PUT /reservation/:id - debe actualizar guestsCount', async ({ request }) => {
  const guest = await createGuest(request);
  const room = await createRoom(request);
  const res = await createReservation(request, guest.id, room.id);
  const reservation = await res.json();

  const response = await request.put(`/api/reservation/${reservation.id}`, {
    data: {
      guestId: guest.id,
      roomId: room.id,
      checkIn: '2027-06-01T14:00:00',
      checkOut: '2027-06-05T12:00:00',
      guestsCount: 3,
    },
  });

  expect(response.status()).toBe(200);
  const body = await response.json();
  expect(body.guestsCount).toBe(3);
});
