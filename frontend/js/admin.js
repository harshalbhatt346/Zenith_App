// Zenith '26 - Fest Admin Dashboard Client Logic

document.addEventListener("DOMContentLoaded", () => {
  // Global dashboard state
  let cachedEvents = [];
  let cachedRegistrations = [];
  let pendingCancelRegId = null;

  // DOM Elements - Metrics
  const statTotalEvents = document.getElementById("stat-total-events");
  const statTotalRegistrations = document.getElementById("stat-total-registrations");
  const statCapacitySubtext = document.getElementById("stat-capacity-subtext");
  const statUtilizationPct = document.getElementById("stat-utilization-pct");
  const statUtilizationBar = document.getElementById("stat-utilization-bar");
  const statUpcomingEvents = document.getElementById("stat-upcoming-events");
  const categoryBreakdownContainer = document.getElementById("category-breakdown-container");
  const statsAlertContainer = document.getElementById("stats-alert-container");

  // DOM Elements - Tables & Tabs
  const tabEventsBtn = document.getElementById("tab-events-btn");
  const tabRegsBtn = document.getElementById("tab-regs-btn");
  const tabEventsContent = document.getElementById("tab-events-content");
  const tabRegsContent = document.getElementById("tab-regs-content");
  const eventsTableBody = document.getElementById("events-table-body");
  const eventsTableState = document.getElementById("events-table-state");
  const regsTableBody = document.getElementById("regs-table-body");
  const regsTableState = document.getElementById("regs-table-state");
  const regEventFilter = document.getElementById("reg-event-filter");
  const refreshAllBtn = document.getElementById("refresh-all-btn");
  const refreshIcon = document.getElementById("refresh-icon");

  // DOM Elements - Add Event Modal (Stretch Goal 1)
  const openAddEventBtn = document.getElementById("open-add-event-btn");
  const addEventModal = document.getElementById("add-event-modal");
  const closeAddEventBtn = document.getElementById("close-add-event-btn");
  const addEventForm = document.getElementById("add-event-form");
  const addEventAlert = document.getElementById("add-event-alert");

  // DOM Elements - Cancel Registration Modal (Stretch Goal 2)
  const cancelModal = document.getElementById("cancel-modal");
  const closeCancelModalBtn = document.getElementById("close-cancel-modal-btn");
  const confirmCancelRegBtn = document.getElementById("confirm-cancel-reg-btn");
  const cancelModalText = document.getElementById("cancel-modal-text");

  // Category styles
  const categoryStyles = {
    Technical: "bg-blue-100 text-blue-800 border-blue-200",
    Cultural: "bg-purple-100 text-purple-800 border-purple-200",
    Sports: "bg-emerald-100 text-emerald-800 border-emerald-200",
    Gaming: "bg-rose-100 text-rose-800 border-rose-200",
    Workshop: "bg-amber-100 text-amber-800 border-amber-200",
  };

  // Helper date formatter
  function formatDate(isoDate) {
    if (!isoDate) return "";
    try {
      const parts = isoDate.split("-");
      if (parts.length === 3) {
        const monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
        return `${parseInt(parts[2], 10)} ${monthNames[parseInt(parts[1], 10) - 1]}`;
      }
      return isoDate;
    } catch {
      return isoDate;
    }
  }

  // 1. Fetch & Render Stats (Step 8.1 & 8.2)
  async function loadStats() {
    try {
      statsAlertContainer.classList.add("hidden");
      const res = await fetch("/api/stats");
      if (!res.ok) throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      const stats = await res.json();

      statTotalEvents.textContent = stats.total_events;
      statTotalRegistrations.textContent = stats.total_registrations;
      statCapacitySubtext.textContent = `out of ${stats.total_capacity} total seats`;
      statUtilizationPct.textContent = `${stats.overall_utilization_pct}%`;
      statUtilizationBar.style.width = `${Math.min(100, stats.overall_utilization_pct)}%`;
      statUpcomingEvents.textContent = stats.upcoming_events_count;

      // Render Category Breakdown (Step 8.2)
      renderCategoryBreakdown(stats.events_by_category);
    } catch (err) {
      console.error("Failed to load stats:", err);
      statsAlertContainer.classList.remove("hidden");
      statsAlertContainer.innerHTML = `
        <div class="p-4 bg-rose-50 border border-rose-200 rounded-2xl text-xs text-rose-800 flex items-center justify-between">
          <span>⚠️ Failed to load dashboard statistics: ${err.message}</span>
          <button onclick="window.location.reload()" class="underline font-bold">Retry</button>
        </div>
      `;
    }
  }

  function renderCategoryBreakdown(categories) {
    categoryBreakdownContainer.innerHTML = "";
    if (!categories || Object.keys(categories).length === 0) {
      categoryBreakdownContainer.innerHTML = `<p class="text-xs text-slate-400">No categories found.</p>`;
      return;
    }

    const catIcons = {
      Technical: "💻",
      Cultural: "🎭",
      Sports: "⚽",
      Gaming: "🎮",
      Workshop: "🛠️",
    };

    for (const [catName, count] of Object.entries(categories)) {
      const card = document.createElement("div");
      const badgeStyle = categoryStyles[catName] || "bg-slate-100 text-slate-700 border-slate-200";
      card.className = `p-3.5 rounded-2xl border ${badgeStyle} flex items-center justify-between shadow-xs`;
      card.innerHTML = `
        <div class="flex items-center space-x-2">
          <span class="text-xl">${catIcons[catName] || "📌"}</span>
          <span class="font-bold text-xs">${catName}</span>
        </div>
        <span class="text-base font-black px-2 py-0.5 rounded-lg bg-white/70 shadow-inner">${count}</span>
      `;
      categoryBreakdownContainer.appendChild(card);
    }
  }

  // 2. Fetch & Render Events Table (Step 8.3 & 8.4)
  async function loadEventsTable() {
    eventsTableState.classList.remove("hidden");
    eventsTableState.innerHTML = `<div class="w-6 h-6 border-2 border-purple-600 border-t-transparent rounded-full animate-spin mx-auto"></div>`;
    eventsTableBody.innerHTML = "";

    try {
      const res = await fetch("/api/events");
      if (!res.ok) throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      const events = await res.json();
      cachedEvents = events;

      // Populate registration filter dropdown
      populateEventFilterDropdown(events);

      // Compute registered count & utilization percentage for each event
      // Sort by utilization percentage descending by default (Step 8.3)
      const enrichedEvents = events.map((ev) => {
        const registered = ev.capacity - ev.available_seats;
        const utilPct = ev.capacity > 0 ? (registered / ev.capacity) * 100 : 0;
        return {
          ...ev,
          registered,
          utilizationPct: utilPct,
        };
      });

      enrichedEvents.sort((a, b) => b.utilizationPct - a.utilizationPct);

      eventsTableState.classList.add("hidden");
      eventsTableBody.innerHTML = "";

      if (enrichedEvents.length === 0) {
        eventsTableState.classList.remove("hidden");
        eventsTableState.innerHTML = `<p class="text-xs text-slate-500">No events found in the database.</p>`;
        return;
      }

      enrichedEvents.forEach((ev) => {
        const isOver90 = ev.utilizationPct >= 90;
        const row = document.createElement("tr");

        // Highlight any event over 90% capacity in a warning color (Step 8.4)
        if (isOver90) {
          row.className = "bg-rose-50/70 hover:bg-rose-100/70 border-l-4 border-l-rose-500 transition";
        } else {
          row.className = "hover:bg-slate-50 transition";
        }

        const catBadge = categoryStyles[ev.category] || "bg-slate-100 text-slate-700 border-slate-200";

        row.innerHTML = `
          <td class="py-3.5 px-4 sm:px-6">
            <div class="flex items-center space-x-3">
              <span class="text-xl p-1.5 bg-white rounded-xl shadow-xs border border-slate-100">${ev.icon || "🎉"}</span>
              <div>
                <span class="font-bold text-slate-900 block">${ev.title}</span>
                <span class="text-[11px] text-slate-400">ID: #${ev.id}</span>
              </div>
            </div>
          </td>
          <td class="py-3.5 px-4">
            <span class="px-2.5 py-0.5 rounded-full text-[11px] font-semibold border ${catBadge}">
              ${ev.category}
            </span>
          </td>
          <td class="py-3.5 px-4 text-slate-600 font-medium">
            ${formatDate(ev.event_date)}, ${ev.event_time}
          </td>
          <td class="py-3.5 px-4 text-slate-600">
            ${ev.venue}
          </td>
          <td class="py-3.5 px-4">
            <span class="font-bold ${isOver90 ? "text-rose-700" : "text-slate-800"}">${ev.registered}</span>
            <span class="text-slate-400">/ ${ev.capacity}</span>
          </td>
          <td class="py-3.5 px-4">
            <div class="flex items-center space-x-2">
              <div class="w-16 bg-slate-200 rounded-full h-2 overflow-hidden">
                <div class="h-2 rounded-full ${isOver90 ? "bg-rose-600" : "bg-purple-600"}" style="width: ${Math.min(100, ev.utilizationPct)}%"></div>
              </div>
              <span class="font-bold text-xs ${isOver90 ? "text-rose-700 font-extrabold" : "text-slate-700"}">
                ${ev.utilizationPct.toFixed(1)}%
              </span>
              ${isOver90 ? `<span title="High Capacity Alert (>90%)">⚠️</span>` : ""}
            </div>
          </td>
          <td class="py-3.5 px-4 text-right font-medium ${ev.fee > 0 ? "text-slate-900" : "text-emerald-600"}">
            ${ev.fee > 0 ? `₹${ev.fee}` : "Free"}
          </td>
        `;

        eventsTableBody.appendChild(row);
      });
    } catch (err) {
      console.error(err);
      eventsTableState.classList.remove("hidden");
      eventsTableState.innerHTML = `<p class="text-xs text-rose-600">Failed to load events table: ${err.message}</p>`;
    }
  }

  function populateEventFilterDropdown(events) {
    const currentVal = regEventFilter.value;
    regEventFilter.innerHTML = `<option value="">All Events (${events.length})</option>`;
    events.forEach((ev) => {
      const opt = document.createElement("option");
      opt.value = ev.id;
      opt.textContent = `${ev.title} (#${ev.id})`;
      if (currentVal && parseInt(currentVal, 10) === ev.id) {
        opt.selected = true;
      }
      regEventFilter.appendChild(opt);
    });
  }

  // 3. Fetch & Render Registrations List (Step 8.5)
  async function loadRegistrations(eventIdFilter = "") {
    regsTableState.classList.remove("hidden");
    regsTableState.innerHTML = `<div class="w-6 h-6 border-2 border-purple-600 border-t-transparent rounded-full animate-spin mx-auto"></div>`;
    regsTableBody.innerHTML = "";

    try {
      let url = "/api/registrations";
      if (eventIdFilter) {
        url += `?event_id=${eventIdFilter}`;
      }

      const res = await fetch(url);
      if (!res.ok) throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      const regs = await res.json();
      cachedRegistrations = regs;

      regsTableState.classList.add("hidden");
      regsTableBody.innerHTML = "";

      if (regs.length === 0) {
        regsTableState.classList.remove("hidden");
        regsTableState.innerHTML = `<p class="text-xs text-slate-500 py-4">No student registrations found for this selection.</p>`;
        return;
      }

      regs.forEach((reg) => {
        const row = document.createElement("tr");
        row.className = "hover:bg-slate-50 transition";

        row.innerHTML = `
          <td class="py-3.5 px-4 sm:px-6 font-mono text-slate-400">#${reg.id}</td>
          <td class="py-3.5 px-4 font-bold text-slate-900">${reg.student_name}</td>
          <td class="py-3.5 px-4 font-mono text-slate-600">${reg.roll_number}</td>
          <td class="py-3.5 px-4 text-slate-600">${reg.email}</td>
          <td class="py-3.5 px-4 text-slate-500">${reg.phone || "—"}</td>
          <td class="py-3.5 px-4">
            <span class="inline-block px-2.5 py-0.5 rounded-lg bg-purple-50 text-purple-700 font-semibold text-xs border border-purple-100">
              ${reg.event_title}
            </span>
          </td>
          <td class="py-3.5 px-4 text-slate-400 text-xs">${reg.registered_at}</td>
          <td class="py-3.5 px-4 text-right">
            <button 
              data-reg-id="${reg.id}" 
              data-student-name="${reg.student_name}" 
              data-event-title="${reg.event_title}"
              class="cancel-reg-btn text-xs text-rose-600 hover:text-rose-800 hover:bg-rose-50 font-semibold px-2.5 py-1 rounded-lg border border-rose-200 transition"
              title="Cancel student registration"
            >
              Cancel
            </button>
          </td>
        `;

        const cancelBtn = row.querySelector(".cancel-reg-btn");
        cancelBtn.addEventListener("click", () => {
          pendingCancelRegId = reg.id;
          cancelModalText.textContent = `Are you sure you want to cancel the registration for "${reg.student_name}" in "${reg.event_title}"?`;
          cancelModal.classList.remove("hidden");
        });

        regsTableBody.appendChild(row);
      });
    } catch (err) {
      console.error(err);
      regsTableState.classList.remove("hidden");
      regsTableState.innerHTML = `<p class="text-xs text-rose-600">Failed to load registrations: ${err.message}</p>`;
    }
  }

  // 4. Tab Switching
  tabEventsBtn.addEventListener("click", () => {
    tabEventsBtn.className = "pb-3 text-sm font-bold border-b-2 border-purple-600 text-purple-700 transition";
    tabRegsBtn.className = "pb-3 text-sm font-semibold border-b-2 border-transparent text-slate-500 hover:text-slate-700 transition";
    tabEventsContent.classList.remove("hidden");
    tabRegsContent.classList.add("hidden");
  });

  tabRegsBtn.addEventListener("click", () => {
    tabRegsBtn.className = "pb-3 text-sm font-bold border-b-2 border-purple-600 text-purple-700 transition";
    tabEventsBtn.className = "pb-3 text-sm font-semibold border-b-2 border-transparent text-slate-500 hover:text-slate-700 transition";
    tabRegsContent.classList.remove("hidden");
    tabEventsContent.classList.add("hidden");
    loadRegistrations(regEventFilter.value);
  });

  regEventFilter.addEventListener("change", () => {
    loadRegistrations(regEventFilter.value);
  });

  // Refresh All Data button
  refreshAllBtn.addEventListener("click", async () => {
    refreshIcon.classList.add("animate-spin");
    await Promise.all([loadStats(), loadEventsTable(), loadRegistrations(regEventFilter.value)]);
    setTimeout(() => {
      refreshIcon.classList.remove("animate-spin");
    }, 500);
  });

  // 5. Stretch Goal 1: Add New Event Form Modal
  openAddEventBtn.addEventListener("click", () => {
    addEventAlert.classList.add("hidden");
    addEventForm.reset();
    addEventModal.classList.remove("hidden");
  });

  closeAddEventBtn.addEventListener("click", () => {
    addEventModal.classList.add("hidden");
  });

  addEventModal.addEventListener("click", (e) => {
    if (e.target === addEventModal) addEventModal.classList.add("hidden");
  });

  addEventForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    addEventAlert.classList.add("hidden");

    const payload = {
      title: document.getElementById("new-event-title").value.trim(),
      category: document.getElementById("new-event-category").value,
      icon: document.getElementById("new-event-icon").value.trim() || "⚡",
      event_date: document.getElementById("new-event-date").value,
      event_time: document.getElementById("new-event-time").value.trim(),
      venue: document.getElementById("new-event-venue").value.trim(),
      capacity: parseInt(document.getElementById("new-event-capacity").value, 10),
      fee: parseInt(document.getElementById("new-event-fee").value, 10) || 0,
      description: document.getElementById("new-event-desc").value.trim(),
    };

    try {
      const res = await fetch("/api/events", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      const data = await res.json();
      if (res.status === 201) {
        addEventAlert.className = "p-3 rounded-xl text-xs font-semibold bg-emerald-50 text-emerald-800 border border-emerald-200 block";
        addEventAlert.textContent = `🎉 Event "${data.title}" created successfully!`;
        await Promise.all([loadStats(), loadEventsTable()]);
        setTimeout(() => {
          addEventModal.classList.add("hidden");
        }, 1200);
      } else {
        addEventAlert.className = "p-3 rounded-xl text-xs font-semibold bg-rose-50 text-rose-800 border border-rose-200 block";
        addEventAlert.textContent = `⚠️ ${data.detail || "Failed to create event"}`;
      }
    } catch (err) {
      addEventAlert.className = "p-3 rounded-xl text-xs font-semibold bg-rose-50 text-rose-800 border border-rose-200 block";
      addEventAlert.textContent = `⚠️ Network error: ${err.message}`;
    }
  });

  // 6. Stretch Goal 2: Cancel Registration Flow
  closeCancelModalBtn.addEventListener("click", () => {
    cancelModal.classList.add("hidden");
    pendingCancelRegId = null;
  });

  cancelModal.addEventListener("click", (e) => {
    if (e.target === cancelModal) {
      cancelModal.classList.add("hidden");
      pendingCancelRegId = null;
    }
  });

  confirmCancelRegBtn.addEventListener("click", async () => {
    if (!pendingCancelRegId) return;

    try {
      const res = await fetch(`/api/registrations/${pendingCancelRegId}`, {
        method: "DELETE",
      });

      if (res.ok) {
        cancelModal.classList.add("hidden");
        pendingCancelRegId = null;
        // Refresh all tables and numbers immediately
        await Promise.all([loadStats(), loadEventsTable(), loadRegistrations(regEventFilter.value)]);
      } else {
        const data = await res.json();
        alert(`Failed to cancel: ${data.detail || "Server error"}`);
      }
    } catch (err) {
      alert(`Network error cancelling registration: ${err.message}`);
    }
  });

  // Initial load
  loadStats();
  loadEventsTable();
});
