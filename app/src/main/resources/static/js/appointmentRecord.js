// appointmentRecord.js
import { getAppointments } from "./components/appointmentRow.js";
import { getAppointmentRecord } from "./services/appointmentRecordService.js";

const tableBody = document.getElementById("patientTableBody");
const filterSelect = document.getElementById("appointmentFilter");

async function loadAppointments(filter = "upcoming") {
  const appointments = await getAppointmentRecord();

  if (!appointments || appointments.length === 0) {
    tableBody.innerHTML = `<tr><td class="noPatientRecord" colspan='5'>No appointments found.</td></tr>`;
    return;
  }

  //const today = new Date().setHours(0, 0, 0, 0);
 // const now = new Date();

  //let filteredAppointments = appointments;
/*
  if (filter === "upcoming") {
    filteredAppointments = appointments.filter(app => new Date(app.date) >= today);
  } else if (filter === "past") {
    filteredAppointments = appointments.filter(app => new Date(app.date) < today);
  }
*/
/*
if (filter === "upcoming") {
  filteredAppointments = appointments.filter(app => {
    const fullDateTime = new Date(`${app.date}T${app.time}:00`);
    return fullDateTime > now;
  });
} else if (filter === "past") {
  filteredAppointments = appointments.filter(app => {
    const fullDateTime = new Date(`${app.date}T${app.time}:00`);
    return fullDateTime <= now;
  });
}*/

    const now = new Date();
    let filteredAppointments = [];

    if (filter === "future" || filter === "past") {
      filteredAppointments = appointments.filter(app => {
        const rawDateTime = app.appointmentTime?.replace(" ", "T");
        const fullDateTime = new Date(rawDateTime);
        console.log(`🕒 Evaluando cita: ${rawDateTime} →`, fullDateTime.toISOString());

        if (isNaN(fullDateTime)) {
          console.warn("❌ Fecha inválida:", rawDateTime);
          return false;
        }

        return filter === "future" ? fullDateTime > now : fullDateTime <= now;
      });
    } else {
      filteredAppointments = appointments;
    }

    filteredAppointments.sort((a, b) => {
      const aTime = new Date(a.appointmentTime?.replace(" ", "T"));
      const bTime = new Date(b.appointmentTime?.replace(" ", "T"));
      return aTime - bTime;
    });




  if (filteredAppointments.length === 0) {
    tableBody.innerHTML = `<tr><td class="noPatientRecord" colspan='5'>No ${filter} appointments found.</td></tr>`;
    return;
  }

  tableBody.innerHTML = "";
  filteredAppointments.forEach(appointment => {
    const row = getAppointments(appointment);
    tableBody.appendChild(row);
  });
}

// Handle filter change
filterSelect.addEventListener("change", (e) => {
  const selectedFilter = e.target.value;
  loadAppointments(selectedFilter);
});

// Load upcoming appointments by default
loadAppointments("future");
