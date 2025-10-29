
package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret}")
    private String secret;

    public TokenService(AdminRepository adminRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    // 1. Obtener clave de firma
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 2. Generar token con identificador y rol
    public String generateToken(String identifier, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000); // 7 días

        return Jwts.builder()
                .setSubject(identifier)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    // 3. Extraer identificador (email o username)
    public String extractIdentifier(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 4. Extraer rol
    public String extractRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    // 5. Validar token según rol
    public boolean validateToken(String token, String role) {
        try {
            String identifier = extractIdentifier(token);
            String tokenRole = extractRole(token);

            // 🔍 Logs para depuración
            System.out.println("🔐 Validando token...");
            System.out.println("→ Identificador extraído: " + identifier);
            System.out.println("→ Rol extraído del token: " + tokenRole);
            System.out.println("→ Rol esperado: " + role);

            if (!tokenRole.equalsIgnoreCase(role)) {
                System.out.println("❌ Rol no coincide");
                return false;
            }

            boolean exists;
            switch (role.toLowerCase()) {
                case "admin":
                    exists = adminRepository.findByUsername(identifier) != null;
                    break;
                case "doctor":
                    exists = doctorRepository.findByEmail(identifier) != null;
                    break;
                case "patient":
                    exists = patientRepository.findByEmail(identifier) != null;
                    break;
                default:
                    System.out.println("❌ Rol desconocido");
                    return false;
            }

            System.out.println("→ ¿Usuario existe en base de datos?: " + exists);
            return exists;

        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("❌ Error al validar token: " + e.getMessage());
            return false;
        }
    }
}
    /*
    public boolean validateToken(String token, String role) {
        try {
            String identifier = extractIdentifier(token);
            String tokenRole = extractRole(token);

            if (!tokenRole.equalsIgnoreCase(role)) return false;

            switch (role.toLowerCase()) {
                case "admin":
                    return adminRepository.findByUsername(identifier) != null;
                case "doctor":
                    return doctorRepository.findByEmail(identifier) != null;
                case "patient":
                    return patientRepository.findByEmail(identifier) != null;
                default:
                    return false;
            }

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }*/


/*package com.project.back_end.services;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret}")
    private String secret;

    public TokenService(AdminRepository adminRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    // 1. Obtener clave de firma
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 2. Generar token JWT
    public String generateToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000); // 7 días

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    // 3. Extraer email del token
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 4. Validar token según tipo de usuario
    public boolean validateToken(String token, String role) {
        try {
            String email = extractEmail(token);

            switch (role.toLowerCase()) {
                case "admin":
                    return adminRepository.findByUsername(email) != null;
                case "doctor":
                    return doctorRepository.findByEmail(email) != null;
                case "patient":
                    return patientRepository.findByEmail(email) != null;
                default:
                    return false;
            }

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
*/
// 1. **@Component Annotation**
// The @Component annotation marks this class as a Spring component, meaning Spring will manage it as a bean within its application context.
// This allows the class to be injected into other Spring-managed components (like services or controllers) where it's needed.

// 2. **Constructor Injection for Dependencies**
// The constructor injects dependencies for `AdminRepository`, `DoctorRepository`, and `PatientRepository`,
// allowing the service to interact with the database and validate users based on their role (admin, doctor, or patient).
// Constructor injection ensures that the class is initialized with all required dependencies, promoting immutability and making the class testable.

// 3. **getSigningKey Method**
// This method retrieves the HMAC SHA key used to sign JWT tokens.
// It uses the `jwt.secret` value, which is provided from an external source (like application properties).
// The `Keys.hmacShaKeyFor()` method converts the secret key string into a valid `SecretKey` for signing and verification of JWTs.

// 4. **generateToken Method**
// This method generates a JWT token for a user based on their email.
// - The `subject` of the token is set to the user's email, which is used as an identifier.
// - The `issuedAt` is set to the current date and time.
// - The `expiration` is set to 7 days from the issue date, ensuring the token expires after one week.
// - The token is signed using the signing key generated by `getSigningKey()`, making it secure and tamper-proof.
// The method returns the JWT token as a string.

// 5. **extractEmail Method**
// This method extracts the user's email (subject) from the provided JWT token.
// - The token is first verified using the signing key to ensure it hasn’t been tampered with.
// - After verification, the token is parsed, and the subject (which represents the email) is extracted.
// This method allows the application to retrieve the user's identity (email) from the token for further use.

// 6. **validateToken Method**
// This method validates whether a provided JWT token is valid for a specific user role (admin, doctor, or patient).
// - It first extracts the email from the token using the `extractEmail()` method.
// - Depending on the role (`admin`, `doctor`, or `patient`), it checks the corresponding repository (AdminRepository, DoctorRepository, or PatientRepository)
//   to see if a user with the extracted email exists.
// - If a match is found for the specified user role, it returns true, indicating the token is valid.
// - If the role or user does not exist, it returns false, indicating the token is invalid.
// - The method gracefully handles any errors by returning false if the token is invalid or an exception occurs.
// This ensures secure access control based on the user's role and their existence in the system.



