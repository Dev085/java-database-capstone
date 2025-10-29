// Import Required Modules
import { openModal } from "./components/modals.js";
//import { openModal } from "../components/modals.js";
import { getDoctors, filterDoctors, saveDoctor } from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";

// Attach a click listener to the "Add Doctor" button
document.getElementById("addDocBtn")?.addEventListener("click", () => {
  openModal("addDoctor");
});

// When the DOM is fully loaded
window.addEventListener("DOMContentLoaded", loadDoctorCards);

// Function: loadDoctorCards
async function loadDoctorCards() {
  try {
    const doctors = await getDoctors();
    renderDoctorCards(doctors);
  } catch (error) {
    console.error("Error loading doctors:", error);
  }
}


// Function: renderDoctorCards
function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  contentDiv.innerHTML = "";



  if (!doctors || doctors.length === 0) {
    contentDiv.innerHTML = `<p class="noPatientRecord">No doctors found.</p>`;
    return;
  }

  doctors.forEach((doctor) => {
    const card = createDoctorCard(doctor);
    contentDiv.appendChild(card);
  });
}

// Attach 'input' and 'change' event listeners to the search bar and filter dropdowns
document.getElementById("searchBar")?.addEventListener("input", filterDoctorsOnChange);
document.getElementById("filterTime")?.addEventListener("change", filterDoctorsOnChange);
document.getElementById("filterSpecialty")?.addEventListener("change", filterDoctorsOnChange);

// Function: filterDoctorsOnChange
async function filterDoctorsOnChange() {
  try {
    const name = document.getElementById("searchBar")?.value || null;
    const rawTime = document.getElementById("filterTime")?.value || null;
    const time = rawTime === "asc" ? "AM" : rawTime === "desc" ? "PM" : null;
    //const time = document.getElementById("filterTime")?.value || null;
    const specialty = document.getElementById("filterSpecialty")?.value || null;

    const doctors = await filterDoctors(name, time, specialty);


    if (doctors && doctors.length > 0) {
      renderDoctorCards(doctors);
    } else {
      const contentDiv = document.getElementById("content");
      contentDiv.innerHTML = `<p class="noPatientRecord">No doctors found with the given filters.</p>`;
    }
  } catch (error) {
    console.error("Error filtering doctors:", error);
    alert("Unable to filter doctors. Please try again.");
  }
}

// Function: adminAddDoctor
window.adminAddDoctor = async function () {
  try {
    const name = document.getElementById("docName")?.value;
    const email = document.getElementById("docEmail")?.value;
    const phone = document.getElementById("docPhone")?.value;
    const password = document.getElementById("docPassword")?.value;
    const specialty = document.getElementById("docSpecialty")?.value;

    const availabilityCheckboxes = document.querySelectorAll("input[name='availability']:checked");
    const availableTimes = Array.from(availabilityCheckboxes).map(cb => cb.value);

    const token = localStorage.getItem("token");
    if (!token) {
      alert("Session expired. Please log in again.");
      return;
    }

    const doctor = { name, email, phone, password, specialty, availableTimes };

    const result = await saveDoctor(doctor, token);

    if (result.success) {
      alert("Doctor added successfully.");
      document.getElementById("modal").classList.remove("active");
      loadDoctorCards();
    } else {
      alert(`Failed to add doctor: ${result.message}`);
    }
  } catch (error) {
    console.error("Error adding doctor:", error);
    alert("Something went wrong while adding the doctor.");
  }
};


/*
  This script handles the admin dashboard functionality for managing doctors:
  - Loads all doctor cards
  - Filters doctors by name, time, or specialty
  - Adds a new doctor via modal form


  Attach a click listener to the "Add Doctor" button
  When clicked, it opens a modal form using openModal('addDoctor')


  When the DOM is fully loaded:
    - Call loadDoctorCards() to fetch and display all doctors


  Function: loadDoctorCards
  Purpose: Fetch all doctors and display them as cards

    Call getDoctors() from the service layer
    Clear the current content area
    For each doctor returned:
    - Create a doctor card using createDoctorCard()
    - Append it to the content div

    Handle any fetch errors by logging them


  Attach 'input' and 'change' event listeners to the search bar and filter dropdowns
  On any input change, call filterDoctorsOnChange()


  Function: filterDoctorsOnChange
  Purpose: Filter doctors based on name, available time, and specialty

    Read values from the search bar and filters
    Normalize empty values to null
    Call filterDoctors(name, time, specialty) from the service

    If doctors are found:
    - Render them using createDoctorCard()
    If no doctors match the filter:
    - Show a message: "No doctors found with the given filters."

    Catch and display any errors with an alert


  Function: renderDoctorCards
  Purpose: A helper function to render a list of doctors passed to it

    Clear the content area
    Loop through the doctors and append each card to the content area


  Function: adminAddDoctor
  Purpose: Collect form data and add a new doctor to the system

    Collect input values from the modal form
    - Includes name, email, phone, password, specialty, and available times

    Retrieve the authentication token from localStorage
    - If no token is found, show an alert and stop execution

    Build a doctor object with the form values

    Call saveDoctor(doctor, token) from the service

    If save is successful:
    - Show a success message
    - Close the modal and reload the page

    If saving fails, show an error message
*/
