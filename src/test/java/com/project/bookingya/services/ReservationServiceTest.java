package com.project.bookingya.services;

import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.entities.GuestEntity;
import com.project.bookingya.entities.ReservationEntity;
import com.project.bookingya.entities.RoomEntity;
import com.project.bookingya.exceptions.BusinessRuleException;
import com.project.bookingya.exceptions.EntityNotExistsException;
import com.project.bookingya.models.Reservation;
import com.project.bookingya.repositories.IGuestRepository;
import com.project.bookingya.repositories.IReservationRepository;
import com.project.bookingya.repositories.IRoomRepository;
import com.project.bookingya.shared.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReservationService - Pruebas Unitarias TDD")
class ReservationServiceTest {

    @Mock
    private IReservationRepository reservationRepository;

    @Mock
    private IRoomRepository roomRepository;

    @Mock
    private IGuestRepository guestRepository;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private ReservationService reservationService;

    private UUID reservationId;
    private UUID guestId;
    private UUID roomId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private ReservationDto dto;
    private ReservationEntity entity;
    private Reservation model;
    private RoomEntity room;
    private GuestEntity guest;

    @BeforeEach
    void setUp() {
        reservationId = UUID.randomUUID();
        guestId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        checkIn = LocalDateTime.now().plusDays(1);
        checkOut = LocalDateTime.now().plusDays(5);

        dto = new ReservationDto();
        dto.setGuestId(guestId);
        dto.setRoomId(roomId);
        dto.setCheckIn(checkIn);
        dto.setCheckOut(checkOut);
        dto.setGuestsCount(2);

        entity = new ReservationEntity();
        entity.setId(reservationId);
        entity.setGuestId(guestId);
        entity.setRoomId(roomId);
        entity.setCheckIn(checkIn);
        entity.setCheckOut(checkOut);
        entity.setGuestsCount(2);

        model = new Reservation();
        model.setId(reservationId);
        model.setGuestId(guestId);
        model.setRoomId(roomId);
        model.setCheckIn(checkIn);
        model.setCheckOut(checkOut);
        model.setGuestsCount(2);

        room = new RoomEntity();
        room.setId(roomId);
        room.setCode("ROOM-001");
        room.setName("Suite Deluxe");
        room.setCity("Bogotá");
        room.setMaxGuests(4);
        room.setNightlyPrice(new BigDecimal("150.00"));
        room.setAvailable(true);

        guest = new GuestEntity();
        guest.setId(guestId);
        guest.setIdentification("CC-12345");
        guest.setName("Juan Pérez");
        guest.setEmail("juan@example.com");
    }

    // ── getAll ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAll: retorna lista de todas las reservas")
    void getAll_returnsAllReservations() {
        List<ReservationEntity> entities = List.of(entity);
        List<Reservation> expected = List.of(model);

        when(reservationRepository.findAll()).thenReturn(entities);
        when(mapper.map(any(), any(Type.class))).thenReturn(expected);

        List<Reservation> result = reservationService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(reservationId);
        verify(reservationRepository).findAll();
    }

    // ── getById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById: retorna reserva cuando existe")
    void getById_whenExists_returnsReservation() {
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(entity));
        when(mapper.map(entity, Reservation.class)).thenReturn(model);

        Reservation result = reservationService.getById(reservationId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(reservationId);
    }

    @Test
    @DisplayName("getById: lanza EntityNotExistsException cuando no existe")
    void getById_whenNotExists_throwsEntityNotExistsException() {
        when(reservationRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getById(UUID.randomUUID()))
                .isInstanceOf(EntityNotExistsException.class)
                .hasMessage(Constants.RESERVATION_NOT_FOUND);
    }

    // ── getByGuestId ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByGuestId: retorna lista de reservas del huésped")
    void getByGuestId_returnsMappedList() {
        List<ReservationEntity> entities = List.of(entity);
        List<Reservation> expected = List.of(model);

        when(reservationRepository.findByGuestId(guestId)).thenReturn(entities);
        when(mapper.map(any(), any(Type.class))).thenReturn(expected);

        List<Reservation> result = reservationService.getByGuestId(guestId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGuestId()).isEqualTo(guestId);
    }

    // ── create: casos de error ─────────────────────────────────────────────────

    @Test
    @DisplayName("create: lanza BusinessRuleException cuando checkIn es posterior a checkOut")
    void create_whenCheckInAfterCheckOut_throwsBusinessRuleException() {
        dto.setCheckIn(checkOut);
        dto.setCheckOut(checkIn);

        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(Constants.INVALID_RESERVATION_RANGE);
    }

    @Test
    @DisplayName("create: lanza BusinessRuleException cuando guestsCount es cero")
    void create_whenGuestsCountZero_throwsBusinessRuleException() {
        dto.setGuestsCount(0);

        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(Constants.INVALID_GUESTS_COUNT);
    }

    @Test
    @DisplayName("create: lanza EntityNotExistsException cuando la habitación no existe")
    void create_whenRoomNotFound_throwsEntityNotExistsException() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(EntityNotExistsException.class)
                .hasMessage(Constants.ROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("create: lanza EntityNotExistsException cuando el huésped no existe")
    void create_whenGuestNotFound_throwsEntityNotExistsException() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(guestRepository.findById(guestId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(EntityNotExistsException.class)
                .hasMessage(Constants.GUEST_NOT_FOUND);
    }

    @Test
    @DisplayName("create: lanza BusinessRuleException cuando la habitación no está disponible")
    void create_whenRoomNotAvailable_throwsBusinessRuleException() {
        room.setAvailable(false);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guest));

        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(Constants.ROOM_NOT_AVAILABLE);
    }

    @Test
    @DisplayName("create: lanza BusinessRuleException cuando guestsCount supera capacidad de la habitación")
    void create_whenGuestsCountExceedsCapacity_throwsBusinessRuleException() {
        dto.setGuestsCount(10);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guest));

        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(Constants.ROOM_CAPACITY_EXCEEDED);
    }

    @Test
    @DisplayName("create: lanza BusinessRuleException cuando hay solapamiento de reserva en la habitación")
    void create_whenRoomHasOverlappingReservation_throwsBusinessRuleException() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guest));
        when(reservationRepository.existsOverlappingReservationForRoom(
                eq(roomId), eq(checkIn), eq(checkOut), eq(null))).thenReturn(true);

        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(Constants.RESERVATION_OVERLAP_ROOM);
    }

    @Test
    @DisplayName("create: lanza BusinessRuleException cuando el huésped ya tiene reserva en ese rango")
    void create_whenGuestHasOverlappingReservation_throwsBusinessRuleException() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guest));
        when(reservationRepository.existsOverlappingReservationForRoom(
                eq(roomId), eq(checkIn), eq(checkOut), eq(null))).thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(
                eq(guestId), eq(checkIn), eq(checkOut), eq(null))).thenReturn(true);

        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(Constants.RESERVATION_OVERLAP_GUEST);
    }

    // ── create: happy path ────────────────────────────────────────────────────

    @Test
    @DisplayName("create: crea y retorna la reserva con datos válidos")
    void create_withValidData_returnsCreatedReservation() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guest));
        when(reservationRepository.existsOverlappingReservationForRoom(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(any(), any(), any(), any())).thenReturn(false);
        when(mapper.map(dto, ReservationEntity.class)).thenReturn(entity);
        when(reservationRepository.saveAndFlush(entity)).thenReturn(entity);
        when(mapper.map(entity, Reservation.class)).thenReturn(model);

        Reservation result = reservationService.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(reservationId);
        assertThat(result.getGuestId()).isEqualTo(guestId);
        assertThat(result.getRoomId()).isEqualTo(roomId);
        assertThat(result.getGuestsCount()).isEqualTo(2);
        verify(reservationRepository).saveAndFlush(entity);
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update: actualiza y retorna la reserva con datos válidos")
    void update_withValidData_updatesAndReturns() {
        dto.setGuestsCount(3);
        ReservationEntity updated = new ReservationEntity();
        updated.setId(reservationId);
        updated.setGuestsCount(3);
        Reservation updatedModel = new Reservation();
        updatedModel.setId(reservationId);
        updatedModel.setGuestsCount(3);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(entity));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guest));
        when(reservationRepository.existsOverlappingReservationForRoom(any(), any(), any(), eq(reservationId))).thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(any(), any(), any(), eq(reservationId))).thenReturn(false);
        when(reservationRepository.saveAndFlush(entity)).thenReturn(updated);
        when(mapper.map(updated, Reservation.class)).thenReturn(updatedModel);

        Reservation result = reservationService.update(dto, reservationId);

        assertThat(result.getGuestsCount()).isEqualTo(3);
        verify(reservationRepository).saveAndFlush(entity);
    }

    @Test
    @DisplayName("update: lanza EntityNotExistsException cuando la reserva no existe")
    void update_whenNotFound_throwsEntityNotExistsException() {
        when(reservationRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.update(dto, UUID.randomUUID()))
                .isInstanceOf(EntityNotExistsException.class)
                .hasMessage(Constants.RESERVATION_NOT_FOUND);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: elimina la reserva cuando existe")
    void delete_whenExists_deletesSuccessfully() {
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(entity));

        reservationService.delete(reservationId);

        verify(reservationRepository).delete(entity);
        verify(reservationRepository).flush();
    }

    @Test
    @DisplayName("delete: lanza EntityNotExistsException cuando la reserva no existe")
    void delete_whenNotFound_throwsEntityNotExistsException() {
        when(reservationRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.delete(UUID.randomUUID()))
                .isInstanceOf(EntityNotExistsException.class)
                .hasMessage(Constants.RESERVATION_NOT_FOUND);
    }
}
