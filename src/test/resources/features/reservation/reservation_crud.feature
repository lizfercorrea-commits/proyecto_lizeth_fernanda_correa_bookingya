Feature: Gestión de reservas
  Como usuario de la plataforma
  Quiero gestionar reservas de habitaciones
  Para que los huéspedes puedan reservar habitaciones disponibles

  Background:
    Given existe un huésped con identificación "BDD-GUEST-001"
    And existe una habitación disponible con código "BDD-ROOM-001" y capacidad máxima 4

  Scenario: Crear una reserva exitosamente
    When creo una reserva con 2 huéspedes desde "2027-06-01T14:00:00" hasta "2027-06-05T12:00:00"
    Then la reserva se crea correctamente
    And el estado de respuesta es 200

  Scenario: Listar todas las reservas
    Given creo una reserva con 1 huésped desde "2027-07-01T14:00:00" hasta "2027-07-03T12:00:00"
    When consulto todas las reservas
    Then la lista de reservas no está vacía

  Scenario: Obtener una reserva por ID
    Given creo una reserva con 2 huéspedes desde "2027-08-01T14:00:00" hasta "2027-08-04T12:00:00"
    When consulto la reserva por su ID
    Then la reserva retornada coincide con la creada

  Scenario: Actualizar una reserva existente
    Given creo una reserva con 1 huésped desde "2027-09-01T14:00:00" hasta "2027-09-03T12:00:00"
    When actualizo la reserva a 3 huéspedes
    Then la reserva actualizada tiene 3 huéspedes

  Scenario: Cancelar una reserva
    Given creo una reserva con 1 huésped desde "2027-10-01T14:00:00" hasta "2027-10-03T12:00:00"
    When cancelo la reserva
    Then la reserva ya no existe

  Scenario: Consultar reservas por huésped
    Given creo una reserva con 2 huéspedes desde "2027-11-01T14:00:00" hasta "2027-11-04T12:00:00"
    When consulto las reservas del huésped
    Then el resultado incluye la reserva creada

  Scenario: Rechazar reserva con rango de fechas inválido
    When intento crear una reserva con checkIn posterior al checkOut
    Then el estado de respuesta es 400
    And la respuesta contiene el error "checkIn must be before checkOut"

  Scenario: Rechazar reserva que excede la capacidad de la habitación
    When intento crear una reserva con 10 huéspedes desde "2027-12-01T14:00:00" hasta "2027-12-05T12:00:00"
    Then el estado de respuesta es 400
    And la respuesta contiene el error "guestsCount exceeds room capacity"
