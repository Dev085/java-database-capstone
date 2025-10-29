package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.services.TokenService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // 1. Crear nuevo paciente/
   /* public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
*/
    public int createPatient(Patient patient) {
        try {
            System.out.println("Guardando paciente: " + patient.getEmail());
            Patient saved = patientRepository.save(patient);
            System.out.println("Paciente guardado con ID: " + saved.getId());
            return 1;
        } catch (Exception e) {
            System.out.println("Error al guardar paciente:");
            e.printStackTrace();
            return 0;
        }
    }

    // 2. Obtener citas del paciente
    @Transactional
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        //String email = tokenService.extractEmail(token);
        String email = tokenService.extractIdentifier(token); // ✅ correcto
        Patient patient = patientRepository.findByEmail(email);

        if (patient == null || !Objects.equals(patient.getId(), id)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
        }

        List<Appointment> appointments = appointmentRepository.findByPatientId(id);
        List<AppointmentDTO> dtos = appointments.stream()
                .map(AppointmentDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("appointments", dtos));
    }

    // 3. Filtrar citas por condición (pasadas o futuras)
    @Transactional
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        int status = condition.equalsIgnoreCase("past") ? 1 : condition.equalsIgnoreCase("future") ? 0 : -1;

        if (status == -1) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid condition"));
        }

        List<Appointment> appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, status);
        List<AppointmentDTO> dtos = appointments.stream()
                .map(AppointmentDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("appointments", dtos));
    }

    // 4. Filtrar citas por nombre del doctor
    @Transactional
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientId(name, patientId);
        List<AppointmentDTO> dtos = appointments.stream()
                .map(AppointmentDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("appointments", dtos));
    }

    // 5. Filtrar citas por doctor y condición
    @Transactional
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, long patientId) {
        int status = condition.equalsIgnoreCase("past") ? 1 : condition.equalsIgnoreCase("future") ? 0 : -1;

        if (status == -1) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid condition"));
        }

        List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(name, patientId, status);
        List<AppointmentDTO> dtos = appointments.stream()
                .map(AppointmentDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("appointments", dtos));
    }

    // 6. Obtener detalles del paciente desde el token
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        //String email = tokenService.extractEmail(token);
        String email = tokenService.extractIdentifier(token); // ✅ correcto
        Patient patient = patientRepository.findByEmail(email);

        if (patient == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Patient not found"));
        }

        return ResponseEntity.ok(Map.of("patient", patient));
    }
}

// 1. **Add @Service Annotation**:
//    - The `@Service` annotation is used to mark this class as a Spring service component. 
//    - It will be managed by Spring's container and used for business logic related to patients and appointments.
//    - Instruction: Ensure that the `@Service` annotation is applied above the class declaration.

// 2. **Constructor Injection for Dependencies**:
//    - The `PatientService` class has dependencies on `PatientRepository`, `AppointmentRepository`, and `TokenService`.
//    - These dependencies are injected via the constructor to maintain good practices of dependency injection and testing.
//    - Instruction: Ensure constructor injection is used for all the required dependencies.

// 3. **createPatient Method**:
//    - Creates a new patient in the database. It saves the patient object using the `PatientRepository`.
//    - If the patient is successfully saved, the method returns `1`; otherwise, it logs the error and returns `0`.
//    - Instruction: Ensure that error handling is done properly and exceptions are caught and logged appropriately.

// 4. **getPatientAppointment Method**:
//    - Retrieves a list of appointments for a specific patient, based on their ID.
//    - The appointments are then converted into `AppointmentDTO` objects for easier consumption by the API client.
//    - This method is marked as `@Transactional` to ensure database consistency during the transaction.
//    - Instruction: Ensure that appointment data is properly converted into DTOs and the method handles errors gracefully.

// 5. **filterByCondition Method**:
//    - Filters appointments for a patient based on the condition (e.g., "past" or "future").
//    - Retrieves appointments with a specific status (0 for future, 1 for past) for the patient.
//    - Converts the appointments into `AppointmentDTO` and returns them in the response.
//    - Instruction: Ensure the method correctly handles "past" and "future" conditions, and that invalid conditions are caught and returned as errors.

// 6. **filterByDoctor Method**:
//    - Filters appointments for a patient based on the doctor's name.
//    - It retrieves appointments where the doctor’s name matches the given value, and the patient ID matches the provided ID.
//    - Instruction: Ensure that the method correctly filters by doctor's name and patient ID and handles any errors or invalid cases.

// 7. **filterByDoctorAndCondition Method**:
//    - Filters appointments based on both the doctor's name and the condition (past or future) for a specific patient.
//    - This method combines filtering by doctor name and appointment status (past or future).
//    - Converts the appointments into `AppointmentDTO` objects and returns them in the response.
//    - Instruction: Ensure that the filter handles both doctor name and condition properly, and catches errors for invalid input.

// 8. **getPatientDetails Method**:
//    - Retrieves patient details using the `tokenService` to extract the patient's email from the provided token.
//    - Once the email is extracted, it fetches the corresponding patient from the `patientRepository`.
//    - It returns the patient's information in the response body.
    //    - Instruction: Make sure that the token extraction process works correctly and patient details are fetched properly based on the extracted email.

// 9. **Handling Exceptions and Errors**:
//    - The service methods handle exceptions using try-catch blocks and log any issues that occur. If an error occurs during database operations, the service responds with appropriate HTTP status codes (e.g., `500 Internal Server Error`).
//    - Instruction: Ensure that error handling is consistent across the service, with proper logging and meaningful error messages returned to the client.

// 10. **Use of DTOs (Data Transfer Objects)**:
//    - The service uses `AppointmentDTO` to transfer appointment-related data between layers. This ensures that sensitive or unnecessary data (e.g., password or private patient information) is not exposed in the response.
//    - Instruction: Ensure that DTOs are used appropriately to limit the exposure of internal data and only send the relevant fields to the client.




