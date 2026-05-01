import { test, expect } from '@playwright/test';
import { createGuest, createRoom, createReservation } from '../helpers/api';

test('GET /reservation - debe retornar una lista no vacía', async ({ request }) => {
  const guest = await createGuest(request);
  const room = await createRoom(request);
  await createReservation(request, guest.id, room.id);

  const response = await request.get('/api/reservation');

  expect(response.status()).toBe(200);
  const body = await response.json();
  expect(Array.isArray(body)).toBeTruthy();
  expect(body.length).toBeGreaterThan(0);
});
