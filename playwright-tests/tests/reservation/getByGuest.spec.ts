import { test, expect } from '@playwright/test';
import { createGuest, createRoom, createReservation } from '../helpers/api';

test('GET /reservation/guest/:id - debe incluir la reserva del huésped', async ({ request }) => {
  const guest = await createGuest(request);
  const room = await createRoom(request);
  const res = await createReservation(request, guest.id, room.id);
  const reservation = await res.json();

  const response = await request.get(`/api/reservation/guest/${guest.id}`);

  expect(response.status()).toBe(200);   
  const body = await response.json();
  expect(Array.isArray(body)).toBeTruthy();
  const found = body.some((r: { id: string }) => r.id === reservation.id);
  expect(found).toBeTruthy();
});
