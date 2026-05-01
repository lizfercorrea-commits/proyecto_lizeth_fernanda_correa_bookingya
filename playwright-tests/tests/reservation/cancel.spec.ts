import { test, expect } from '@playwright/test';
import { createGuest, createRoom, createReservation } from '../helpers/api';

test('DELETE /reservation/:id - debe cancelar y retornar 404 al consultar', async ({ request }) => {
  const guest = await createGuest(request);
  const room = await createRoom(request);
  const res = await createReservation(request, guest.id, room.id);
  const reservation = await res.json();

  const deleteResponse = await request.delete(`/api/reservation/${reservation.id}`);
  expect(deleteResponse.status()).toBe(200);

  const getResponse = await request.get(`/api/reservation/${reservation.id}`);
  expect(getResponse.status()).toBe(404);
});
