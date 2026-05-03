import { test, expect } from '@playwright/test';
import { createGuest, createRoom } from '../helpers/api';

test.describe('POST /reservation - Crear reserva', () => {
  test('debe crear una reserva con datos válidos y retornar 200', async ({ request }) => {
    const guest = await createGuest(request);
    const room = await createRoom(request);

    const response = await request.post('/api/reservation', {
      data: {
        guestId: guest.id,  
        roomId: room.id,
        checkIn: '2027-06-01T14:00:00',
        checkOut: '2027-06-05T12:00:00',
        guestsCount: 2,
      },
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.id).toBeTruthy();
    expect(body.guestsCount).toBe(2);
  });

  test('debe retornar 400 si checkIn es posterior a checkOut', async ({ request }) => {
    const guest = await createGuest(request);
    const room = await createRoom(request);

    const response = await request.post('/api/reservation', {
      data: {
        guestId: guest.id,
        roomId: room.id,
        checkIn: '2027-06-10T14:00:00',
        checkOut: '2027-06-01T12:00:00',
        guestsCount: 1,
      },
    });

    expect(response.status()).toBe(400);
    const body = await response.json();
    expect(JSON.stringify(body)).toContain('checkIn must be before checkOut');
  });

  test('debe retornar 400 si guestsCount supera la capacidad', async ({ request }) => {
    const guest = await createGuest(request);
    const room = await createRoom(request);

    const response = await request.post('/api/reservation', {
      data: {
        guestId: guest.id,
        roomId: room.id,
        checkIn: '2027-07-01T14:00:00',
        checkOut: '2027-07-05T12:00:00',
        guestsCount: 10,
      },
    });

    expect(response.status()).toBe(400);
    const body = await response.json();
    expect(JSON.stringify(body)).toContain('guestsCount exceeds room capacity');
  });
});
