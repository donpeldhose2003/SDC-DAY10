document.getElementById("registerForm")?.addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(e.target).entries());
  await fetch("/register-student", {
    method: "POST",
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(data)
  });
  alert("Registered. Check your email for password.");
});

document.getElementById("loginForm")?.addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(e.target).entries());
  const res = await fetch("/login", {
    method: "POST",
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(data)
  });
  if (res.ok) {
    alert("Login successful");
    window.location.href = "dashboard.html";
  } else {
    alert("Login failed");
  }
});

document.getElementById("addCourseForm")?.addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(e.target).entries());
  await fetch("/course", {
    method: "POST",
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(data)
  });
  alert("Course added!");
});

document.getElementById("registerCourseForm")?.addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(e.target).entries());
  const res = await fetch("/register-course", {
    method: "POST",
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(data)
  });
  if (res.ok) {
    alert("Course registered!");
  } else {
    alert("Registration failed");
  }
});

window.addEventListener("load", async () => {
  if (document.getElementById("courseList")) {
    const res = await fetch("/courses");
    const courses = await res.json();
    document.getElementById("courseList").innerHTML = courses.map(c => `
      <li><b>${c.name}</b> - ID: ${c._id} - Seats: ${c.capacity}</li>
    `).join('');
  }
});
