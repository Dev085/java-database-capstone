package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Services1;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;


@RestController
@RequestMapping("/api/appointments") //@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Services1 service;

    public AppointmentController(AppointmentService appointmentService, Services1 service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    // 1. Obtener citas por fecha y nombre de paciente (solo para doctores)


    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Appointments API is active");
    }

/*
    @GetMapping("/{date}/{patientName}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");


        // 👇 Agrega aquí los logs
        System.out.println("Buscando citas para: " + patientName + " en fecha: " + date);
        System.out.println("Token recibido: " + token);
        //appointments.forEach(a -> System.out.println("→ Cita: " + a.getAppointmentTime()));
        // Cambia "doctor" por "patient"


        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "patient");
        if (!validation.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
        }


        LocalDate localDate = LocalDate.parse(date);
        Map<String, Object> result = appointmentService.getAppointment(patientName, localDate, token);
        return ResponseEntity.ok(result);
    }

*/
    @GetMapping("/{date}/{patientName}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");

        // ✅ Validar si el token corresponde a un doctor o paciente
        ResponseEntity<Map<String, String>> validationDoctor = service.validateToken(token, "doctor");
        ResponseEntity<Map<String, String>> validationPatient = service.validateToken(token, "patient");

        if (!validationDoctor.getStatusCode().is2xxSuccessful() && !validationPatient.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
        }

        // 👇 Logs informativos
        System.out.println("Buscando citas para: " + patientName + " en fecha: " + date);
        System.out.println("Token recibido: " + token);

        LocalDate localDate = LocalDate.parse(date);
        Map<String, Object> result = appointmentService.getAppointment(patientName, localDate, token);
        return ResponseEntity.ok(result);
    }




    // 2. Crear nueva cita (solo pacientes)

    @PostMapping
    public ResponseEntity<Map<String, String>> bookAppointment(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Appointment appointment) {

        // Extraer el token del header "Bearer eyJ..."
        String token = authHeader.replace("Bearer ", "");

        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "patient");
        if (!validation.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
        }
//
         if (appointment.getAppointmentTime() != null) {
            appointment.setDate(appointment.getAppointmentTime().toLocalDate());
            appointment.setTimeSlot(appointment.getAppointmentTime().toLocalTime().toString());
        }
        // No modificar date ni timeSlot aquí. El servicio se encarga de construir appointmentTime.


//
        int result = service.validateAppointment(appointment);
        if (result == -1) {
            return ResponseEntity.badRequest().body(Map.of("error", "Doctor not found"));
        } else if (result == 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Time slot unavailable"));
        }

        int booked = appointmentService.bookAppointment(appointment);
        if (booked == 1) {
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Appointment booked successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to book appointment"));
        }
    }

    // 3. Actualizar cita existente (solo pacientes) ESTE ERA EL ANTERIOR 10/23/2025

    @PutMapping
    public ResponseEntity<Map<String, String>> updateAppointment(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Appointment appointment) {

        // Extraer el token del header
        String token = authHeader.replace("Bearer ", "");

        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "patient");
        if (!validation.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
        }
        System.out.println("Appointment recibido: " + appointment);
        return appointmentService.updateAppointment(appointment);


    }


    // 4. Cancelar cita (solo pacientes)
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable long id,
                                                                 @PathVariable String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "patient");
        if (!validation.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
        }

        return appointmentService.cancelAppointment(id, token);
    }


}


// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to define it as a REST API controller.
//    - Use `@RequestMapping("/appointments")` to set a base path for all appointment-related endpoints.
//    - This centralizes all routes that deal with booking, updating, retrieving, and canceling appointments.


// 2. Autowire Dependencies:
//    - Inject `AppointmentService` for handling the business logic specific to appointments.
//    - Inject the general `Service` class, which provides shared functionality like token validation and appointment checks.


// 3. Define the `getAppointments` Method:
//    - Handles HTTP GET requests to fetch appointments based on date and patient name.
//    - Takes the appointment date, patient name, and token as path variables.
//    - First validates the token for role `"doctor"` using the `Service`.
//    - If the token is valid, returns appointments for the given patient on the specified date.
//    - If the token is invalid or expired, responds with the appropriate message and status code.


// 4. Define the `bookAppointment` Method:
//    - Handles HTTP POST requests to create a new appointment.
//    - Accepts a validated `Appointment` object in the request body and a token as a path variable.
//    - Validates the token for the `"patient"` role.
//    - Uses service logic to validate the appointment data (e.g., check for doctor availability and time conflicts).
//    - Returns success if booked, or appropriate error messages if the doctor ID is invalid or the slot is already taken.


// 5. Define the `updateAppointment` Method:
//    - Handles HTTP PUT requests to modify an existing appointment.
//    - Accepts a validated `Appointment` object and a token as input.
//    - Validates the token for `"patient"` role.
//    - Delegates the update logic to the `AppointmentService`.
//    - Returns an appropriate success or failure response based on the update result.


// 6. Define the `cancelAppointment` Method:
//    - Handles HTTP DELETE requests to cancel a specific appointment.
//    - Accepts the appointment ID and a token as path variables.
//    - Validates the token for `"patient"` role to ensure the user is authorized to cancel the appointment.
//    - Calls `AppointmentService` to handle the cancellation process and returns the result.



