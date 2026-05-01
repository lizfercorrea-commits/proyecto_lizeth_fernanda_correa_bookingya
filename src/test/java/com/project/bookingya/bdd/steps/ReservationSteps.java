package com.project.bookingya.bdd.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cucumber.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ReservationSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private ResponseEntity<String> lastResponse;
    private UUID lastReservationId;
    private UUID guestId;
    private UUID roomId;

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Given("existe un huésped con identificación {string}")
    public void existeUnHuesped(String identification) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("identification", identification);
        body.put("name", "Huésped BDD");
        body.put("email", identification.toLowerCase().replace("-", "") + "@bdd.com");

        String json = objectMapper.writeValueAsString(body);
        ResponseEntity<String> resp = restTemplate.exchange(
                "/guest", HttpMethod.POST,
                new HttpEntity<>(json, jsonHeaders()), String.class);
        JsonNode node = objectMapper.readTree(resp.getBody());
        guestId = UUID.fromString(node.get("id").asText());
    }

    @And("existe una habitación disponible con código {string} y capacidad máxima {int}")
    public void existeUnaHabitacion(String code, int maxGuests) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("name", "Habitación BDD");
        body.put("city", "Bogotá");
        body.put("maxGuests", maxGuests);
        body.put("nightlyPrice", 100.00);
        body.put("available", true);

        String json = objectMapper.writeValueAsString(body);
        ResponseEntity<String> resp = restTemplate.exchange(
                "/room", HttpMethod.POST,
                new HttpEntity<>(json, jsonHeaders()), String.class);
        JsonNode node = objectMapper.readTree(resp.getBody());
        roomId = UUID.fromString(node.get("id").asText());
    }

    private void crearReserva(int guests, String checkIn, String checkOut) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("guestId", guestId.toString());
        body.put("roomId", roomId.toString());
        body.put("checkIn", checkIn);
        body.put("checkOut", checkOut);
        body.put("guestsCount", guests);

        String json = objectMapper.writeValueAsString(body);
        lastResponse = restTemplate.exchange(
                "/reservation", HttpMethod.POST,
                new HttpEntity<>(json, jsonHeaders()), String.class);
        if (lastResponse.getStatusCode() == HttpStatus.OK) {
            JsonNode node = objectMapper.readTree(lastResponse.getBody());
            lastReservationId = UUID.fromString(node.get("id").asText());
        }
    }

    @When("creo una reserva con {int} huésped(es) desde {string} hasta {string}")
    public void creoUnaReserva(int guests, String checkIn, String checkOut) throws Exception {
        crearReserva(guests, checkIn, checkOut);
    }

    @When("consulto todas las reservas")
    public void consultoTodasLasReservas() {
        lastResponse = restTemplate.exchange(
                "/reservation", HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()), String.class);
    }

    @When("consulto la reserva por su ID")
    public void consultoLaReservaPorSuId() {
        lastResponse = restTemplate.exchange(
                "/reservation/" + lastReservationId, HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()), String.class);
    }

    @When("actualizo la reserva a {int} huéspedes")
    public void actualizoLaReserva(int newCount) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("guestId", guestId.toString());
        body.put("roomId", roomId.toString());
        body.put("checkIn", "2027-09-01T14:00:00");
        body.put("checkOut", "2027-09-03T12:00:00");
        body.put("guestsCount", newCount);

        String json = objectMapper.writeValueAsString(body);
        lastResponse = restTemplate.exchange(
                "/reservation/" + lastReservationId, HttpMethod.PUT,
                new HttpEntity<>(json, jsonHeaders()), String.class);
    }

    @When("cancelo la reserva")
    public void canceloLaReserva() {
        lastResponse = restTemplate.exchange(
                "/reservation/" + lastReservationId, HttpMethod.DELETE,
                new HttpEntity<>(jsonHeaders()), String.class);
    }

    @When("consulto las reservas del huésped")
    public void consultoLasReservasDelHuesped() {
        lastResponse = restTemplate.exchange(
                "/reservation/guest/" + guestId, HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()), String.class);
    }

    @When("intento crear una reserva con checkIn posterior al checkOut")
    public void intentoCrearReservaFechasInvalidas() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("guestId", guestId.toString());
        body.put("roomId", roomId.toString());
        body.put("checkIn", "2027-06-10T14:00:00");
        body.put("checkOut", "2027-06-01T12:00:00");
        body.put("guestsCount", 1);

        String json = objectMapper.writeValueAsString(body);
        lastResponse = restTemplate.exchange(
                "/reservation", HttpMethod.POST,
                new HttpEntity<>(json, jsonHeaders()), String.class);
    }

    @When("intento crear una reserva con {int} huéspedes desde {string} hasta {string}")
    public void intentoCrearReservaConHuespedes(int guests, String checkIn, String checkOut) throws Exception {
        crearReserva(guests, checkIn, checkOut);
    }

    @Then("la reserva se crea correctamente")
    public void laReservaSeCreacorrectamente() {
        assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(lastReservationId).isNotNull();
    }

    @Then("el estado de respuesta es {int}")
    public void elEstadoDeRespuestaEs(int expectedStatus) {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(expectedStatus);
    }

    @Then("la lista de reservas no está vacía")
    public void laListaDeReservasNoEstaVacia() throws Exception {
        assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode list = objectMapper.readTree(lastResponse.getBody());
        assertThat(list.isArray()).isTrue();
        assertThat(list.size()).isGreaterThan(0);
    }

    @Then("la reserva retornada coincide con la creada")
    public void laReservaRetornadaCoincideConLaCreada() throws Exception {
        assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode node = objectMapper.readTree(lastResponse.getBody());
        assertThat(node.get("id").asText()).isEqualTo(lastReservationId.toString());
    }

    @Then("la reserva actualizada tiene {int} huéspedes")
    public void laReservaActualizadaTieneHuespedes(int expectedCount) throws Exception {
        assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode node = objectMapper.readTree(lastResponse.getBody());
        assertThat(node.get("guestsCount").asInt()).isEqualTo(expectedCount);
    }

    @Then("la reserva ya no existe")
    public void laReservaYaNoExiste() {
        assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        ResponseEntity<String> getResp = restTemplate.exchange(
                "/reservation/" + lastReservationId, HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()), String.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Then("el resultado incluye la reserva creada")
    public void elResultadoIncluyeLaReservaCreada() throws Exception {
        assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode list = objectMapper.readTree(lastResponse.getBody());
        assertThat(list.isArray()).isTrue();
        boolean found = false;
        for (JsonNode item : list) {
            if (item.get("id").asText().equals(lastReservationId.toString())) {
                found = true;
                break;
            }
        }
        assertThat(found).as("La reserva %s debe estar en la lista", lastReservationId).isTrue();
    }

    @And("la respuesta contiene el error {string}")
    public void laRespuestaContieneElError(String expectedError) {
        assertThat(lastResponse.getBody()).contains(expectedError);
    }
}
