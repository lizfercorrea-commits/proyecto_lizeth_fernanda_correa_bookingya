import { test, expect } from '@playwright/test';
import { createGuest, createRoom, createReservation } from '../helpers/api';

test.describe('GET /reservation/:id', () => {
  test('debe retornar la reserva cuando existe', async ({ request }) => {
    const guest = await createGuest(request);
    const room = await createRoom(request);
    const res = await createReservation(request, guest.id, room.id);
    const reservation = await res.json();

    const response = await request.get(`/api/reservation/${reservation.id}`);

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.id).toBe(reservation.id);
  });

  test('debe retornar 404 con un ID inexistente', async ({ request }) => {
    const fakeId = '00000000-0000-0000-0000-000000000000';
    const response = await request.get(`/api/reservation/${fakeId}`);
    expect(response.status()).toBe(404);
  });
});
