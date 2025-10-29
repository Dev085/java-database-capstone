import { getPatientAppointments, getPatientData, filterAppointments } from "./services/patientServices.js";
import { setRole, getRole, clearRole } from './util.js';

const tableBody = document.getElementById("patientTableBody");
const token = localStorage.getItem("token");

let allAppointments = [];
let filteredAppointments = [];
let patientId = null;

document.addEventListener("DOMContentLoaded", initializePage);

async function initializePage() {
  try {
    if (!token) throw new Error("No token found");

    const patient = await getPatientData(token);
    if (!patient) throw new Error("Failed to fetch patient details");

    patientId = Number(patient.id);

    const today = new Date().toISOString().split("T")[0];
    const appointmentData = await getPatientAppointments(today, patient.name, token);
    allAppointments = appointmentData.filter(app => app.patient?.id === patientId);
    renderAppointments(allAppointments);
  } catch (error) {
    console.error("Error loading appointments:", error);
    alert("❌ Failed to load your appointments.");
  }
}

function renderAppointments(appointments) {
  tableBody.innerHTML = "";

  const actionTh = document.querySelector("#patientTable thead tr th:last-child");
  if (actionTh) {
    actionTh.style.display = "table-cell"; // Always show "Actions" column
  }

  if (!appointments.length) {
    tableBody.innerHTML = `<tr><td colspan="5" style="text-align:center;">No Appointments Found</td></tr>`;
    return;
  }
  appointments.forEach(appointment => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${appointment.patient?.name || "You"}</td>
        <td>${appointment.doctor?.name || "-"}</td>
        <td>${appointment.date}</td>
        <td>${appointment.timeSlot}</td>
        <td>${appointment.status === 0 ? `<img src="../assets/images/edit/edit.png" alt="Edit" class="prescription-btn" data-id="${appointment.patient?.id}">` : "-"}</td>
      `;

    if (appointment.status == 0) {
      const actionBtn = tr.querySelector(".prescription-btn");
      actionBtn?.addEventListener("click", () => redirectToUpdatePage(appointment));
    }

    //<td>${appointment.status === 0 ? `<img src="../assets/images/edit/edit.png" alt="Edit" class="prescription-btn" data-id="${appointment.patient?.id}">` : "-"}</td>
    tableBody.appendChild(tr);
  });
}

function redirectToUpdatePage(appointment) {
  // Prepare the query parameters
  /*
  const queryString = new URLSearchParams({
    appointmentId: appointment.id,
    patientId: appointment.patientId,
    patientName: appointment.patientName || "You",
    doctorName: appointment.doctorName,
    doctorId: appointment.doctorId,
    appointmentDate: appointment.appointmentDate,
    appointmentTime: appointment.appointmentTimeOnly,
  }).toString();
*/
    const queryString = new URLSearchParams({
      appointmentId: appointment.id,
      patientId: appointment.patient?.id,
      patientName: appointment.patient?.name || "You",
      doctorName: appointment.doctor?.name,
      doctorId: appointment.doctor?.id,
      appointmentDate: appointment.date,
      appointmentTime: appointment.timeSlot,
    }).toString();

  // Redirect to the update page with the query string
  setTimeout(() => {
    window.location.href = `/pages/updateAppointment.html?${queryString}`;
  }, 100);
}

// Search and Filter Listeners
document.getElementById("searchBar").addEventListener("input", handleFilterChange);
document.getElementById("appointmentFilter").addEventListener("change", handleFilterChange);

/* 1
async function handleFilterChange() {
  const searchBarValue = document.getElementById("searchBar").value.trim();
  const filterValue = document.getElementById("appointmentFilter").value;

  // const name = searchBarValue || null;
  //const condition = filterValue === "allAppointments" ? null : filterValue || null;
  const name = searchBarValue || "all";
  const condition = filterValue === "allAppointments" ? "all" : filterValue || "all";

  try {


    if (condition === "all" && name === "all") {
      // Mostrar todas las citas ya cargadas
      renderAppointments(allAppointments);
    } else {
      const response = await filterAppointments(condition, name, token);
      const appointments = response?.appointments || [];
      filteredAppointments = appointments.filter(app => app.patient?.id === patientId);
      renderAppointments(filteredAppointments);
    }
  } catch (error) {
    console.error("Failed to filter appointments:", error);
    alert("❌ An error occurred while filtering appointments.");
  }
}
*/
/* 2
async function handleFilterChange() {
  const searchBarValue = document.getElementById("searchBar").value.trim();
  const filterValue = document.getElementById("appointmentFilter").value;

  const name = searchBarValue || "all";
  const condition = filterValue === "allAppointments" ? "all" : filterValue || "all";

  try {
    let appointments;

    if (condition === "all" && name === "all") {
      appointments = allAppointments;
    } else {
      const response = await filterAppointments(condition, name, token);
      appointments = response?.appointments || [];
    }

    let filtered = appointments.filter(app => app.patient?.id === patientId);

   if (condition === "upcoming" || condition === "past") {
     const now = new Date();
     filtered = filtered.filter(app => {
       const rawDateTime = `${app.date}T${app.time}:00`; // ← CAMBIO CRÍTICO
       const fullDateTime = new Date(rawDateTime);
       console.log(`🕒 Evaluando cita: ${rawDateTime} →`, fullDateTime.toISOString());

       if (isNaN(fullDateTime)) return false;
       return condition === "upcoming" ? fullDateTime > now : fullDateTime <= now;
     });
   }


    filtered.sort((a, b) => {
      const aTime = new Date(`${a.date}T${a.time}:00`);
      const bTime = new Date(`${b.date}T${b.time}:00`);
      return aTime - bTime;
    });


    renderAppointments(filtered);
  } catch (error) {
    console.error("Failed to filter appointments:", error);
    alert("❌ An error occurred while filtering appointments.");
  }
}
*/

async function handleFilterChange() {
  const searchBarValue = document.getElementById("searchBar").value.trim();
  const filterValue = document.getElementById("appointmentFilter").value;

  const name = searchBarValue || "all";
  const condition = filterValue === "allAppointments" ? "all" : filterValue || "all";

  try {
    let appointments;

    if (condition === "all" && name === "all") {
      appointments = allAppointments;
    } else {
      const response = await filterAppointments(condition, name, token);
      appointments = response?.appointments || [];
    }

    let filtered = appointments.filter(app => app.patient?.id === patientId);

    if (condition === "upcoming" || condition === "past") {
      const now = new Date();
      const todayDate = now.toISOString().split("T")[0]; // "YYYY-MM-DD"
      const currentTime = now.toTimeString().split(":").slice(0, 2).join(":"); // "HH:mm"

      filtered = filtered.filter(app => {
        const appDate = app.date;
        const appTime = app.timeSlot;

        if (!appDate || !appTime) return false;

        return condition === "upcoming"
          ? appDate > todayDate || (appDate === todayDate && appTime > currentTime)
          : appDate < todayDate || (appDate === todayDate && appTime <= currentTime);
      });
    }

    filtered.sort((a, b) => {
      const aTime = new Date(`${a.date}T${a.timeSlot}:00`);
      const bTime = new Date(`${b.date}T${b.timeSlot}:00`);
      return aTime - bTime;
    });

    renderAppointments(filtered);
  } catch (error) {
    console.error("Failed to filter appointments:", error);
    alert("❌ An error occurred while filtering appointments.");
  }
}
