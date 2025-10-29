// util.js
/*
  function setRole(role) {
    localStorage.setItem("userRole", role);
  }
  
  function getRole() {
    return localStorage.getItem("userRole");
  }
  
  function clearRole() {
    localStorage.removeItem("userRole");
  }
  
*/
// util.js
export function setRole(role) {
  localStorage.setItem("userRole", role);
}

export function getRole() {
  return localStorage.getItem("userRole");
}

export function clearRole() {
  localStorage.removeItem("userRole");
}
