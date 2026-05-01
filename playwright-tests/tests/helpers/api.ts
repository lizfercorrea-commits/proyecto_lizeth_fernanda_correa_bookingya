import { APIRequestContext } from '@playwright/test';

function uid() {
  return `${Date.now()}-${Math.floor(Math.random() * 99999)}`;
}

export async function createGuest(request: APIRequestContext) {
  const id = uid();
  const response = await request.post('/api/guest', {
    data: {
      identification: `GUEST-${id}`,
      name: 'Test Guest',
      email: `guest${id.replace('-', '')}@test.com`,
    },
  });
  return response.json();
}

export async function createRoom(request: APIRequestContext) {
  const id = uid();
  const response = await request.post('/api/room', {
    data: {
      code: `ROOM-${id}`,
      name: 'Test Room',
      city: 'Bogotá',
      maxGuests: 4,
      nightlyPrice: 100.0,
      available: true,
    },
  });
  return response.json();
}

export async function createReservation(
  request: APIRequestContext,
  guestId: string,
  roomId: string,
  checkIn = '2027-06-01T14:00:00',
  checkOut = '2027-06-05T12:00:00',
  guestsCount = 2
) {
  const response = await request.post('/api/reservation', {
    data: { guestId, roomId, checkIn, checkOut, guestsCount },
  });
  return response;
}
