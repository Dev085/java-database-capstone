// render.js
// render.js
import { setRole, getRole } from "./util.js";

window.renderContent = renderContent;

export function selectRole(role) {
  setRole(role);
  const token = localStorage.getItem("token");

  switch (role) {
    case "admin":
      if (token) {
        window.location.href = `/adminDashboard/${token}`;
      } else {
        alert("Admin token missing. Please log in.");
      }
      break;

    case "doctor":
      if (token) {
        window.location.href = `/doctorDashboard/${token}`;
      } else {
        alert("Doctor token missing. Please log in.");
      }
      break;

    case "patient":
      window.location.href = "/pages/patientDashboard.html";
      break;

    case "loggedPatient":
      window.location.href = "/pages/loggedPatientDashboard.html";
      break;

    default:
      alert("Unknown role selected.");
  }
}

export function renderContent() {
  const role = getRole();
  if (!role) {
    window.location.href = "/";
  }
}

/*
import { setRole, getRole } from "./util.js"; //

export function selectRole(role) {
  setRole(role);
  const token = localStorage.getItem("token");

  switch (role) {
    case "admin":
      if (token) {
        window.location.href = `/adminDashboard/${token}`;
      } else {
        alert("Admin token missing. Please log in.");
      }
      break;

    case "doctor":
      if (token) {
        window.location.href = `/doctorDashboard/${token}`;
      } else {
        alert("Doctor token missing. Please log in.");
      }
      break;

    case "patient":
      window.location.href = "/pages/patientDashboard.html";
      break;

    case "loggedPatient":
      window.location.href = "/pages/loggedPatientDashboard.html";
      break;

    default:
      alert("Unknown role selected.");
  }
}

export function renderContent() {
  const role = getRole();
  if (!role) {
    window.location.href = "/";
  }
}
*/
