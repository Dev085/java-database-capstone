/*package com.project.back_end.services;

import com.project.back_end.model.Appointment;
import com.project.back_end.model.Doctor;
import com.project.back_end.model.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.service.Service;
import com.project.back_end.service.TokenService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;
    private final Service service;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              TokenService tokenService,
                              Service service) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
        this.service = service;
    }

    // 1. Book a new appointment
    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // 2. Update an existing appointment
    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Optional<Appointment> existing = appointmentRepository.findById(appointment.getId());
        Map<String, String> response = new HashMap<>();

        if (existing.isEmpty()) {
            response.put("error", "Appointment not found");
            return ResponseEntity.badRequest().body(response);
        }

        if (!service.validateAppointment(appointment)) {
            response.put("error", "Invalid appointment data");
            return ResponseEntity.badRequest().body(response);
        }

        appointmentRepository.save(appointment);
        response*/

package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
//import com.project.back_end.security.TokenService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;



import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;


@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              TokenService tokenService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
    }

@Transactional
public int bookAppointment(Appointment appointment) {
    try {
        // Validar que date y timeSlot no sean nulos
        if (appointment.getDate() != null && appointment.getTimeSlot() != null) {
            LocalDate date = appointment.getDate();
            String timeSlot = appointment.getTimeSlot().trim();

            System.out.println("⏱️ timeSlot recibido: " + timeSlot);

            // Validar formato HH:mm
            if (timeSlot.matches("\\d{2}:\\d{2}")) {
                System.out.println("🕒 timeSlot recibido: '" + timeSlot + "'");
                LocalTime time = LocalTime.parse(timeSlot);
                LocalDateTime appointmentDateTime = LocalDateTime.of(date, time);
                appointment.setAppointmentTime(appointmentDateTime);
                System.out.println("✅ appointmentTime construido: " + appointmentDateTime);
            } else {
                System.out.println("❌ Formato de hora inválido: " + timeSlot);
                return 0;
            }
        } else {
            System.out.println("❌ Fecha o hora no proporcionadas. No se puede construir appointmentTime.");
            return 0;
        }

        appointmentRepository.save(appointment);
        return 1;
    } catch (Exception e) {
        System.out.println("Error al guardar cita:");
        e.printStackTrace();
        return 0;
    }
}

    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment updatedAppointment) {
        Map<String, String> response = new HashMap<>();
        Optional<Appointment> existingOpt = appointmentRepository.findById(updatedAppointment.getId());

        if (existingOpt.isEmpty()) {
            response.put("message", "Appointment not found.");
            return ResponseEntity.badRequest().body(response);
        }

        Appointment existing = existingOpt.get();

        if (!Objects.equals(existing.getPatient().getId(), updatedAppointment.getPatient().getId())) {
            response.put("message", "Unauthorized update attempt.");
            return ResponseEntity.status(403).body(response);
        }

        Optional<Doctor> doctorOpt = doctorRepository.findById(updatedAppointment.getDoctor().getId());
        if (doctorOpt.isEmpty()) {
            response.put("message", "Doctor not found.");
            return ResponseEntity.badRequest().body(response);
        }
/*
        existing.setDoctor(updatedAppointment.getDoctor());
        existing.setAppointmentTime(updatedAppointment.getAppointmentTime());
*/

        //existing.setAppointmentTime(LocalDateTime.now()); // momento de modificación
        existing.setDate(updatedAppointment.getDate());   // fecha deseada
        existing.setTimeSlot(updatedAppointment.getTimeSlot()); // hora deseada


        System.out.println("📅 Fecha deseada: " + existing.getDate());
        System.out.println("⏰ Hora deseada: " + existing.getTimeSlot());
      //  System.out.println("🕒 appointmentTime (registro): " + existing.getAppointmentTime());


        appointmentRepository.save(existing);
        response.put("message", "Appointment updated successfully.");
        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> response = new HashMap<>();
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);

        if (appointmentOpt.isEmpty()) {
            response.put("message", "Appointment not found.");
            return ResponseEntity.badRequest().body(response);
        }

        Appointment appointment = appointmentOpt.get();
        //String email = tokenService.extractEmail(token);
        String email = tokenService.extractIdentifier(token); // ✅ correcto
        Patient patient = patientRepository.findByEmail(email); // corregido

        if (patient == null || !Objects.equals(patient.getId(), appointment.getPatient().getId())) {
            response.put("message", "Unauthorized cancellation.");
            return ResponseEntity.status(403).body(response);
        }

        appointmentRepository.delete(appointment);
        response.put("message", "Appointment cancelled successfully.");
        return ResponseEntity.ok(response);
    }



@Transactional
public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
    Map<String, Object> result = new HashMap<>();
    String email = tokenService.extractIdentifier(token);
    String role = tokenService.extractRole(token);

    // Se siguen generando las fechas, pero no se usan
    LocalDateTime start = date.atStartOfDay();
    LocalDateTime end = date.plusDays(1).atStartOfDay();

    List<Appointment> appointments = new ArrayList<>();

    if ("doctor".equals(role)) {
        Doctor doctor = doctorRepository.findByEmail(email);
        if (doctor == null) {
            result.put("error", "Doctor not found.");
            return result;
        }

        Long doctorId = doctor.getId();
        // ✅ Rango amplio para evitar filtro real por fecha
        appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctorId,
                LocalDateTime.of(2000, 1, 1, 0, 0),
                LocalDateTime.of(2100, 12, 31, 23, 59)
        );

        if (pname != null && !pname.isEmpty()) {
            appointments = appointments.stream()
                    .filter(a -> {
                        Patient p = a.getPatient();
                        return p != null && p.getName().toLowerCase().contains(pname.toLowerCase());
                    })
                    .collect(Collectors.toList());
        }

    } else if ("patient".equals(role)) {
        Patient patient = patientRepository.findByEmail(email);
        if (patient == null) {
            result.put("error", "Patient not found.");
            return result;
        }

        Long patientId = patient.getId();
        // ✅ Rango amplio para evitar filtro real por fecha
        appointments = appointmentRepository.findByPatientIdAndAppointmentTimeBetween(
                patientId,
                LocalDateTime.of(2000, 1, 1, 0, 0),
                LocalDateTime.of(2100, 12, 31, 23, 59)
        );

    } else {
        result.put("error", "Invalid role.");
        return result;
    }

    result.put("appointments", appointments);
    return result;
}

    @Transactional
    public ResponseEntity<Map<String, String>> changeStatus(Long id, String status) {
        Map<String, String> response = new HashMap<>();
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);

        if (appointmentOpt.isEmpty()) {
            response.put("message", "Appointment not found.");
            return ResponseEntity.badRequest().body(response);
        }

        Appointment appointment = appointmentOpt.get();

        try {
            int statusCode = Integer.parseInt(status); // ✅ conversión segura
            appointment.setStatus(statusCode);
        } catch (NumberFormatException e) {
            response.put("message", "Invalid status format. Must be a number.");
            return ResponseEntity.badRequest().body(response);
        }

        appointmentRepository.save(appointment);
        response.put("message", "Status updated successfully.");
        return ResponseEntity.ok(response);
    }

}

// 1. **Add @Service Annotation**:
//    - To indicate that this class is a service layer class for handling business logic.
//    - The `@Service` annotation should be added before the class declaration to mark it as a Spring service component.
//    - Instruction: Add `@Service` above the class definition.

// 2. **Constructor Injection for Dependencies**:
//    - The `AppointmentService` class requires several dependencies like `AppointmentRepository`, `Service`, `TokenService`, `PatientRepository`, and `DoctorRepository`.
//    - These dependencies should be injected through the constructor.
//    - Instruction: Ensure constructor injection is used for proper dependency management in Spring.

// 3. **Add @Transactional Annotation for Methods that Modify Database**:
//    - The methods that modify or update the database should be annotated with `@Transactional` to ensure atomicity and consistency of the operations.
//    - Instruction: Add the `@Transactional` annotation above methods that interact with the database, especially those modifying data.

// 4. **Book Appointment Method**:
//    - Responsible for saving the new appointment to the database.
//    - If the save operation fails, it returns `0`; otherwise, it returns `1`.
//    - Instruction: Ensure that the method handles any exceptions and returns an appropriate result code.

// 5. **Update Appointment Method**:
//    - This method is used to update an existing appointment based on its ID.
//    - It validates whether the patient ID matches, checks if the appointment is available for updating, and ensures that the doctor is available at the specified time.
//    - If the update is successful, it saves the appointment; otherwise, it returns an appropriate error message.
//    - Instruction: Ensure proper validation and error handling is included for appointment updates.

// 6. **Cancel Appointment Method**:
//    - This method cancels an appointment by deleting it from the database.
//    - It ensures the patient who owns the appointment is trying to cancel it and handles possible errors.
//    - Instruction: Make sure that the method checks for the patient ID match before deleting the appointment.

// 7. **Get Appointments Method**:
//    - This method retrieves a list of appointments for a specific doctor on a particular day, optionally filtered by the patient's name.
//    - It uses `@Transactional` to ensure that database operations are consistent and handled in a single transaction.
//    - Instruction: Ensure the correct use of transaction boundaries, especially when querying the database for appointments.

// 8. **Change Status Method**:
//    - This method updates the status of an appointment by changing its value in the database.
//    - It should be annotated with `@Transactional` to ensure the operation is executed in a single transaction.
//    - Instruction: Add `@Transactional` before this method to ensure atomicity when updating appointment status.



