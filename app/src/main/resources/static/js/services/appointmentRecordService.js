// appointmentRecordService.js
import { API_BASE_URL } from "../config/config.js";
const APPOINTMENT_API = `${API_BASE_URL}/appointments`;


//This is for the doctor to get all the patient Appointments
/*
export async function getAllAppointments(date, patientName, token) {
  const response = await fetch(`${APPOINTMENT_API}/${date}/${patientName}/${token}`);
  if (!response.ok) {
    throw new Error("Failed to fetch appointments");
  }

  return await response.json();
}*/
export async function getAllAppointments(date, patientName, token) {
  // Validaciones defensivas
  if (!date || typeof date !== "string") {
    console.warn("❌ Fecha inválida:", date);
    throw new Error("La fecha es obligatoria y debe ser una cadena válida.");
  }

  if (!patientName || typeof patientName !== "string") {
    console.warn("❌ Nombre del paciente inválido:", patientName);
    throw new Error("El nombre del paciente es obligatorio y debe ser una cadena válida.");
  }

  if (!token || typeof token !== "string") {
    console.warn("❌ Token inválido o ausente:", token);
    throw new Error("El token JWT es obligatorio para autenticar la solicitud.");
  }

  const url = `${APPOINTMENT_API}/${date}/${encodeURIComponent(patientName)}`;
  console.log("📡 Solicitando citas desde:", url);

  try {
    const response = await fetch(url, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${token}`
      }
    });

    console.log("📥 Estado de respuesta:", response.status);

    if (!response.ok) {
      const errorText = await response.text();
      console.error("❌ Error al obtener citas:", errorText);
      throw new Error(`Error ${response.status}: ${errorText}`);
    }

    const data = await response.json();
    console.log("✅ Citas recibidas:", data);
    return data;
  } catch (error) {
    console.error("🚨 Error de red o backend:", error.message);
    throw new Error("No se pudieron obtener las citas. Verifica tu conexión o credenciales.");
  }
}


export async function bookAppointment(appointment, token) {
  try {
    const response = await fetch(`${APPOINTMENT_API}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}` // ← el token va aquí
      },
      body: JSON.stringify(appointment)
    });

    const data = await response.json();
    return {
      success: response.ok,
      message: data.message || "Something went wrong"
    };
  } catch (error) {
    console.error("Error while booking appointment:", error);
    return {
      success: false,
      message: "Network error. Please try again later."
    };
  }
}

/*
export async function bookAppointment(appointment, token) {
  try {
    const response = await fetch(`${APPOINTMENT_API}/${token}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(appointment)
    });

    const data = await response.json();
    return {
      success: response.ok,
      message: data.message || "Something went wrong"
    };
  } catch (error) {
    console.error("Error while booking appointment:", error);
    return {
      success: false,
      message: "Network error. Please try again later."
    };
  }
}
*/
/* ESTE ERA EL ANTERIOR 10/23/2025
export async function updateAppointment(appointment, token) {
  try {
    const response = await fetch(`${APPOINTMENT_API}/${token}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(appointment)
    });

    const data = await response.json();
    return {
      success: response.ok,
      message: data.message || "Something went wrong"
    };
  } catch (error) {
    console.error("Error while booking appointment:", error);
    return {
      success: false,
      message: "Network error. Please try again later."
    };
  }
}*/

export async function updateAppointment(appointment, token) {
  try {
    console.log("Datos enviados:", appointment);
    const response = await fetch(`${APPOINTMENT_API}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify(appointment)
    });

    const data = await response.json();
    return {
      success: response.ok,
      message: data.message || "Something went wrong"
    };
  } catch (error) {
    console.error("Error while updating appointment:", error);
    return {
      success: false,
      message: "Network error. Please try again later."
    };
  }
}

