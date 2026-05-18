const API = "/api";

function setStatus(msg, isError) {
  const el = document.getElementById("status");
  el.textContent = msg || "";
  el.className = isError ? "muted error" : "muted";
}

async function apiJson(path, options = {}) {
  const res = await fetch(API + path, {
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options,
  });
  const text = await res.text();
  let data;
  try {
    data = text ? JSON.parse(text) : {};
  } catch {
    data = { error: text };
  }
  if (!res.ok) {
    const err = data.error || res.statusText;
    throw new Error(err);
  }
  return data;
}

let gradeChart;

function renderChart(labels, values) {
  const ctx = document.getElementById("grade-chart");
  if (gradeChart) {
    gradeChart.destroy();
  }
  gradeChart = new Chart(ctx, {
    type: "line",
    data: {
      labels,
      datasets: [
        {
          label: "Not (kronolojik)",
          data: values,
          borderColor: "#3fa9f5",
          backgroundColor: "rgba(63, 169, 245, 0.15)",
          tension: 0.25,
          fill: true,
        },
      ],
    },
    options: {
      scales: {
        y: { min: 0, max: 100, grid: { color: "#2a3548" } },
        x: { grid: { color: "#2a3548" } },
      },
      plugins: { legend: { labels: { color: "#e7ecf3" } } },
    },
  });
}

function riskLabel(code) {
  if (code === "normal") return "Basarili (>=60)";
  if (code === "riskli") return "Destek gerekebilir (<60)";
  return "Henuz not yok";
}

function renderCourseTable(insights) {
  const wrap = document.getElementById("course-table-wrap");
  if (!insights || !insights.length) {
    wrap.innerHTML = "<p class=\"muted\">Ders bazli ozet icin en az bir not gerekir.</p>";
    return;
  }
  const rows = insights
    .map(
      (r) =>
        `<tr><td>${escapeHtml(r.courseCode)}</td><td>${escapeHtml(String(r.courseName))}</td><td>${r.credit}</td><td>${r.latestGrade}</td><td>${r.letterGrade}</td></tr>`
    )
    .join("");
  wrap.innerHTML = `
    <table class="data-table">
      <thead><tr><th>Kod</th><th>Ders</th><th>Kredi</th><th>Guncel not</th><th>Harf</th></tr></thead>
      <tbody>${rows}</tbody>
    </table>
    <p class="hint">Kredi agirlikli ortalama: her ders icin son not x kredi toplaminin, kredi toplamina bolumu.</p>`;
}

function escapeHtml(s) {
  return String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

async function refreshStudents() {
  const list = document.getElementById("student-list");
  list.innerHTML = "";
  try {
    const students = await apiJson("/students");
    students.forEach((s) => {
      const li = document.createElement("li");
      li.textContent = `${s.studentNumber} — ${s.firstName} ${s.lastName} (${s.department})`;
      list.appendChild(li);
    });
    setStatus(`${students.length} ogrenci listelendi.`);
  } catch (e) {
    setStatus(e.message, true);
  }
}

document.getElementById("form-student").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  const fd = new FormData(ev.target);
  const body = Object.fromEntries(fd.entries());
  try {
    await apiJson("/students", { method: "POST", body: JSON.stringify(body) });
    ev.target.reset();
    setStatus("Ogrenci kaydedildi.");
    refreshStudents();
  } catch (e) {
    setStatus(e.message, true);
  }
});

document.getElementById("form-course").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  const fd = new FormData(ev.target);
  const body = {
    courseCode: fd.get("courseCode"),
    name: fd.get("name"),
    credit: Number(fd.get("credit")),
  };
  try {
    await apiJson("/courses", { method: "POST", body: JSON.stringify(body) });
    ev.target.reset();
    setStatus("Ders tanimlandi.");
  } catch (e) {
    setStatus(e.message, true);
  }
});

document.getElementById("form-enroll").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  const fd = new FormData(ev.target);
  const body = Object.fromEntries(fd.entries());
  try {
    await apiJson("/enrollments", { method: "POST", body: JSON.stringify(body) });
    ev.target.reset();
    setStatus("Derse kayit tamamlandi.");
  } catch (e) {
    setStatus(e.message, true);
  }
});

document.getElementById("form-grade").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  const fd = new FormData(ev.target);
  const body = {
    studentNumber: fd.get("studentNumber"),
    courseCode: fd.get("courseCode"),
    grade: Number(fd.get("grade")),
  };
  try {
    await apiJson("/grades", { method: "POST", body: JSON.stringify(body) });
    ev.target.reset();
    setStatus("Not kaydedildi.");
  } catch (e) {
    setStatus(e.message, true);
  }
});

document.getElementById("btn-analysis").addEventListener("click", async () => {
  const num = document.getElementById("analysis-student").value.trim();
  if (!num) {
    setStatus("Ogrenci numarasi girin.", true);
    return;
  }
  try {
    const data = await apiJson(`/analysis/${encodeURIComponent(num)}`);
    const sum = document.getElementById("analysis-summary");
    const missing = (data.coursesWithoutGrade || []).join(", ") || "—";
    sum.innerHTML = `
      <strong>${escapeHtml(data.studentName)}</strong> (${escapeHtml(data.studentNumber)}) — ${escapeHtml(data.department)}<br/>
      Aritmetik ortalama: <strong>${data.averageGrade}</strong> &middot;
      Kredi agirlikli (ders basina son not): <strong>${data.creditWeightedAverage}</strong>
      (${data.totalCreditsInAverage || 0} kredi)<br/>
      Kayitli ders: ${data.enrollmentCount} &middot; Not satiri: ${data.gradeCount} &middot;
      Risk: <strong>${riskLabel(data.riskLevel)}</strong><br/>
      Notu girilmemis kayitli dersler: <span class="muted">${escapeHtml(missing)}</span><br/>
      Bildirim kaydi (SQL): ${(data.gradeNotifications || []).length}
    `;
    renderCourseTable(data.courseInsights || []);
    const chart = data.gradeChart || { labels: [], values: [] };
    if (typeof Chart !== "undefined") {
      renderChart(chart.labels || [], chart.values || []);
    }
    document.getElementById("analysis-raw").textContent = JSON.stringify(data, null, 2);
    setStatus("Rapor yuklendi.");
  } catch (e) {
    setStatus(e.message, true);
  }
});

document.getElementById("btn-refresh-students").addEventListener("click", refreshStudents);

refreshStudents();
