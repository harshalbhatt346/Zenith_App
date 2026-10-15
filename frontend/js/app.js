// Zenith '26 - Student Event Portal Client Logic

document.addEventListener("DOMContentLoaded", () => {
  // State variables
  let currentCategory = "All";
  let searchDebounceTimeout = null;
  let activeEventData = null;

  // DOM Elements
  const eventsGrid = document.getElementById("events-grid");
  const stateContainer = document.getElementById("state-container");
  const searchInput = document.getElementById("search-input");
  const clearSearchBtn = document.getElementById("clear-search-btn");
  const resultsCount = document.getElementById("results-count");
  const categoryFilters = document.getElementById("category-filters");

  // Modal DOM Elements
  const modal = document.getElementById("event-modal");
  const modalCloseBtn = document.getElementById("modal-close-btn");
  const modalIcon = document.getElementById("modal-icon");
  const modalTitle = document.getElementById("modal-title");
  const modalCategory = document.getElementById("modal-category");
  const modalDatetime = document.getElementById("modal-datetime");
  const modalVenue = document.getElementById("modal-venue");
  const modalFee = document.getElementById("modal-fee");
  const modalSeatsBanner = document.getElementById("modal-seats-banner");
  const modalSeatsCount = document.getElementById("modal-seats-count");
  const modalDescription = document.getElementById("modal-description");
  const regForm = document.getElementById("registration-form");
  const regEventIdInput = document.getElementById("reg-event-id");
  const regNameInput = document.getElementById("reg-name");
  const regRollInput = document.getElementById("reg-roll");
  const regPhoneInput = document.getElementById("reg-phone");
  const regEmailInput = document.getElementById("reg-email");
  const formAlert = document.getElementById("form-alert");
  const submitRegBtn = document.getElementById("submit-reg-btn");
  const submitText = document.getElementById("submit-text");
  const submitSpinner = document.getElementById("submit-spinner");
  const errorName = document.getElementById("error-name");
  const errorRoll = document.getElementById("error-roll");
  const errorEmail = document.getElementById("error-email");

  // Category badge color helpers
  const categoryStyles = {
    Technical: "bg-blue-100 text-blue-800 border-blue-200",
    Cultural: "bg-purple-100 text-purple-800 border-purple-200",
    Sports: "bg-emerald-100 text-emerald-800 border-emerald-200",
    Gaming: "bg-rose-100 text-rose-800 border-rose-200",
    Workshop: "bg-amber-100 text-amber-800 border-amber-200",
  };

  // Helper to format date string
  function formatDate(isoDate) {
    if (!isoDate) return "";
    try {
      const parts = isoDate.split("-");
      if (parts.length === 3) {
        const monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
        const day = parseInt(parts[2], 10);
        const month = monthNames[parseInt(parts[1], 10) - 1];
        return `${day} ${month}`;
      }
      return isoDate;
    } catch {
      return isoDate;
    }
  }

  // 1. Fetch Events from backend with query parameters
  async function fetchEvents() {
    showLoading();

    const params = new URLSearchParams();
    if (currentCategory && currentCategory !== "All") {
      params.append("category", currentCategory);
    }
    const searchQuery = searchInput.value.trim();
    if (searchQuery) {
      params.append("search", searchQuery);
    }

    const url = `/api/events?${params.toString()}`;

    try {
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`Server returned HTTP ${response.status}: ${response.statusText}`);
      }
      const events = await response.json();
      renderEvents(events);
    } catch (err) {
      console.error("Failed to fetch events:", err);
      showError(err.message || "Failed to load events. Please verify the server is running.");
    }
  }

  // 2. Render Cards in Grid
  function renderEvents(events) {
    stateContainer.classList.add("hidden");
    eventsGrid.classList.remove("hidden");
    eventsGrid.innerHTML = "";

    resultsCount.textContent = `Showing ${events.length} event${events.length === 1 ? "" : "s"}`;

    if (events.length === 0) {
      showEmpty();
      return;
    }

    events.forEach((event) => {
      const isFull = event.available_seats <= 0;
      const catClass = categoryStyles[event.category] || "bg-slate-100 text-slate-800 border-slate-200";
      const feeText = event.fee > 0 ? `₹${event.fee}` : "Free";
      const formattedDate = formatDate(event.event_date);

      const card = document.createElement("div");
      card.className =
        "group bg-white rounded-3xl p-6 border border-slate-200/80 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 flex flex-col justify-between animate-fade-in relative overflow-hidden";

      // Status indicator line at top of card
      const topBarColor = isFull ? "bg-rose-500" : "bg-gradient-to-r from-purple-500 to-indigo-500";

      card.innerHTML = `
        <div class="absolute top-0 left-0 right-0 h-1.5 ${topBarColor}"></div>

        <div class="space-y-4">
          <!-- Card Header: Icon, Category & Full Badge -->
          <div class="flex items-start justify-between gap-3">
            <span class="text-3xl p-2.5 bg-slate-50 rounded-2xl border border-slate-100 shadow-inner group-hover:scale-110 transition duration-300">
              ${event.icon || "🎉"}
            </span>
            <div class="flex items-center space-x-2">
              <span class="px-3 py-1 rounded-full text-xs font-semibold border ${catClass}">
                ${event.category}
              </span>
              ${
                isFull
                  ? `<span class="px-2.5 py-1 rounded-full text-xs font-black tracking-wide bg-rose-600 text-white shadow-sm uppercase">FULL</span>`
                  : ""
              }
            </div>
          </div>

          <!-- Title and Description -->
          <div>
            <h3 class="text-lg font-bold text-slate-900 group-hover:text-purple-600 transition leading-snug">
              ${event.title}
            </h3>
            <p class="text-xs text-slate-500 line-clamp-2 mt-1 leading-relaxed">
              ${event.description || "No description provided."}
            </p>
          </div>

          <!-- Event Meta Details -->
          <div class="space-y-2 pt-2 border-t border-slate-100 text-xs text-slate-600">
            <div class="flex items-center space-x-2">
              <span class="text-slate-400">📅</span>
              <span class="font-medium">${formattedDate}, ${event.event_time}</span>
            </div>
            <div class="flex items-center space-x-2">
              <span class="text-slate-400">📍</span>
              <span class="truncate">${event.venue}</span>
            </div>
          </div>
        </div>

        <!-- Card Footer: Capacity, Fee & Action -->
        <div class="pt-5 mt-4 border-t border-slate-100 flex items-center justify-between">
          <div>
            <div class="text-[11px] uppercase tracking-wider text-slate-400 font-bold">Seats</div>
            <div class="text-xs font-semibold ${isFull ? "text-rose-600 font-bold" : "text-slate-800"}">
              ${
                isFull
                  ? `0 / ${event.capacity}`
                  : `<span class="text-purple-600 font-bold">${event.available_seats}</span> / ${event.capacity} left`
              }
            </div>
          </div>

          <div class="text-right">
            <div class="text-[11px] uppercase tracking-wider text-slate-400 font-bold">Fee</div>
            <div class="text-xs font-bold ${event.fee > 0 ? "text-slate-900" : "text-emerald-600"}">
              ${feeText}
            </div>
          </div>

          <button 
            data-event-id="${event.id}" 
            class="view-details-btn px-4 py-2 rounded-xl text-xs font-semibold transition shadow-sm ${
              isFull
                ? "bg-slate-100 text-slate-700 hover:bg-slate-200"
                : "bg-purple-600 hover:bg-purple-700 text-white shadow-purple-200"
            }"
          >
            View Details
          </button>
        </div>
      `;

      // Event listener for opening detail modal
      const btn = card.querySelector(".view-details-btn");
      btn.addEventListener("click", () => openEventModal(event.id));

      eventsGrid.appendChild(card);
    });
  }

  // 3. Loading, Empty, and Error States (Step 9)
  function showLoading() {
    eventsGrid.classList.add("hidden");
    stateContainer.classList.remove("hidden");
    stateContainer.innerHTML = `
      <div class="bg-white rounded-2xl p-12 text-center border border-slate-200 shadow-sm flex flex-col items-center justify-center space-y-4">
        <div class="w-10 h-10 border-4 border-purple-200 border-t-purple-600 rounded-full animate-spin"></div>
        <p class="text-sm font-medium text-slate-600">Loading Zenith '26 events...</p>
      </div>
    `;
    resultsCount.textContent = "Loading...";
  }

  function showEmpty() {
    eventsGrid.classList.add("hidden");
    stateContainer.classList.remove("hidden");
    stateContainer.innerHTML = `
      <div class="bg-white rounded-3xl p-12 text-center border border-slate-200 shadow-sm max-w-md mx-auto space-y-4">
        <div class="text-5xl">🔍</div>
        <h3 class="text-base font-bold text-slate-800">No events match your criteria</h3>
        <p class="text-xs text-slate-500 leading-relaxed">
          We couldn't find any events matching "${searchInput.value.trim()}" in ${currentCategory}. Try clearing your search or switching categories.
        </p>
        <button id="reset-filters-btn" class="px-5 py-2 rounded-xl text-xs font-bold text-white bg-purple-600 hover:bg-purple-700 transition">
          Show All Events
        </button>
      </div>
    `;
    const resetBtn = document.getElementById("reset-filters-btn");
    if (resetBtn) {
      resetBtn.addEventListener("click", () => {
        searchInput.value = "";
        clearSearchBtn.classList.add("hidden");
        setCategory("All");
      });
    }
  }

  function showError(message) {
    eventsGrid.classList.add("hidden");
    stateContainer.classList.remove("hidden");
    stateContainer.innerHTML = `
      <div class="bg-rose-50 border border-rose-200 rounded-3xl p-8 text-center max-w-lg mx-auto space-y-4">
        <div class="text-4xl">⚠️</div>
        <h3 class="text-base font-bold text-rose-800">Unable to Connect to Backend</h3>
        <p class="text-xs text-rose-600 leading-relaxed font-mono bg-white/60 p-3 rounded-xl border border-rose-100">
          ${message}
        </p>
        <p class="text-xs text-slate-500">Ensure FastAPI is running: <code class="bg-slate-200 px-1 py-0.5 rounded text-[11px]">uvicorn backend.main:app --reload</code></p>
        <button id="retry-btn" class="px-5 py-2 rounded-xl text-xs font-bold text-white bg-rose-600 hover:bg-rose-700 transition">
          Retry Fetching Events
        </button>
      </div>
    `;
    const retryBtn = document.getElementById("retry-btn");
    if (retryBtn) {
      retryBtn.addEventListener("click", fetchEvents);
    }
    resultsCount.textContent = "Error loading";
  }

  // 4. Modal Handling (Step 7)
  async function openEventModal(eventId) {
    resetFormAlert();
    clearFormErrors();
    regForm.reset();

    // Show modal in loading state
    modal.classList.remove("hidden");
    modalTitle.textContent = "Loading event details...";
    modalDescription.textContent = "";

    try {
      const response = await fetch(`/api/events/${eventId}`);
      if (!response.ok) {
        throw new Error(`Failed to load event details (${response.status})`);
      }
      activeEventData = await response.json();
      populateModal(activeEventData);
    } catch (err) {
      console.error(err);
      modalTitle.textContent = "Error loading event";
      modalDescription.textContent = err.message;
    }
  }

  function populateModal(event) {
    regEventIdInput.value = event.id;
    modalIcon.textContent = event.icon || "🎉";
    modalTitle.textContent = event.title;
    modalCategory.textContent = event.category;
    modalDatetime.textContent = `${formatDate(event.event_date)}, ${event.event_time}`;
    modalVenue.textContent = event.venue;
    modalFee.textContent = event.fee > 0 ? `₹${event.fee}` : "Free";
    modalDescription.textContent = event.description || "No full description provided.";

    const isFull = event.available_seats <= 0;
    if (isFull) {
      modalSeatsBanner.className =
        "flex items-center justify-between p-3 rounded-xl bg-rose-50 text-rose-800 text-xs sm:text-sm font-semibold border border-rose-200";
      modalSeatsCount.innerHTML = `<span class="bg-rose-600 text-white text-[11px] px-2 py-0.5 rounded uppercase font-bold mr-1">FULL</span> 0 of ${event.capacity} seats remaining`;
      submitRegBtn.disabled = true;
      submitText.textContent = "Registration Closed (Capacity Full)";
    } else {
      modalSeatsBanner.className =
        "flex items-center justify-between p-3 rounded-xl bg-purple-50 text-purple-800 text-xs sm:text-sm font-medium border border-purple-100";
      modalSeatsCount.innerHTML = `<strong>${event.available_seats}</strong> of ${event.capacity} seats remaining`;
      submitRegBtn.disabled = false;
      submitText.textContent = "Confirm Registration";
    }
  }

  function closeModal() {
    modal.classList.add("hidden");
    activeEventData = null;
    resetFormAlert();
  }

  modalCloseBtn.addEventListener("click", closeModal);
  modal.addEventListener("click", (e) => {
    if (e.target === modal) closeModal();
  });
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && !modal.classList.contains("hidden")) {
      closeModal();
    }
  });

  // 5. Client-Side Validation & Server Submission (Step 7)
  function clearFormErrors() {
    errorName.classList.add("hidden");
    errorRoll.classList.add("hidden");
    errorEmail.classList.add("hidden");
    regNameInput.classList.remove("border-rose-400");
    regRollInput.classList.remove("border-rose-400");
    regEmailInput.classList.remove("border-rose-400");
  }

  function resetFormAlert() {
    formAlert.classList.add("hidden");
    formAlert.textContent = "";
    formAlert.className = "hidden mb-3 p-3 rounded-xl text-xs sm:text-sm font-medium";
  }

  function showFormAlert(type, message) {
    formAlert.classList.remove("hidden");
    formAlert.textContent = message;
    if (type === "success") {
      formAlert.className = "mb-3 p-3 rounded-xl text-xs sm:text-sm font-semibold bg-emerald-50 text-emerald-800 border border-emerald-200 animate-fade-in";
    } else {
      formAlert.className = "mb-3 p-3 rounded-xl text-xs sm:text-sm font-semibold bg-rose-50 text-rose-800 border border-rose-200 animate-fade-in";
    }
  }

  regForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearFormErrors();
    resetFormAlert();

    const eventId = parseInt(regEventIdInput.value, 10);
    const studentName = regNameInput.value.trim();
    const rollNumber = regRollInput.value.trim();
    const phone = regPhoneInput.value.trim();
    const email = regEmailInput.value.trim();

    // Client-side validation
    let hasClientError = false;

    if (!studentName) {
      errorName.textContent = "Please enter your full name.";
      errorName.classList.remove("hidden");
      regNameInput.classList.add("border-rose-400");
      hasClientError = true;
    }

    if (!rollNumber) {
      errorRoll.textContent = "Please enter your college roll / student ID.";
      errorRoll.classList.remove("hidden");
      regRollInput.classList.add("border-rose-400");
      hasClientError = true;
    }

    if (!email) {
      errorEmail.textContent = "Please enter your email address.";
      errorEmail.classList.remove("hidden");
      regEmailInput.classList.add("border-rose-400");
      hasClientError = true;
    } else if (!email.includes("@") || !email.includes(".")) {
      errorEmail.textContent = "Please enter a valid email address with '@' and domain.";
      errorEmail.classList.remove("hidden");
      regEmailInput.classList.add("border-rose-400");
      hasClientError = true;
    }

    if (hasClientError) return;

    // Server-side submission
    submitRegBtn.disabled = true;
    submitSpinner.classList.remove("hidden");
    submitText.textContent = "Submitting registration...";

    try {
      const response = await fetch(`/api/events/${eventId}/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          student_name: studentName,
          roll_number: rollNumber,
          phone: phone || null,
          email: email,
        }),
      });

      const data = await response.json();

      if (response.status === 201) {
        // Step 7 success state
        showFormAlert(
          "success",
          `🎉 Registered successfully for ${activeEventData ? activeEventData.title : "the event"}! Reg ID: #${data.id}`
        );

        // Re-fetch event data to immediately update seat count
        const updatedRes = await fetch(`/api/events/${eventId}`);
        if (updatedRes.ok) {
          activeEventData = await updatedRes.json();
          populateModal(activeEventData);
        }

        // Also update background event list
        fetchEvents();

        // Close modal after delay
        setTimeout(() => {
          closeModal();
        }, 1800);
      } else {
        // Step 7 failure state (400 or 409): show exact server message
        const errMsg = data.detail || "Registration failed. Please check your inputs.";
        showFormAlert("error", `⚠️ ${errMsg}`);
        submitRegBtn.disabled = false;
        submitText.textContent = "Confirm Registration";
      }
    } catch (err) {
      console.error("Submission error:", err);
      showFormAlert("error", `⚠️ Network connection error: ${err.message}`);
      submitRegBtn.disabled = false;
      submitText.textContent = "Confirm Registration";
    } finally {
      submitSpinner.classList.add("hidden");
    }
  });

  // 6. Search Input & Category Filters (Step 6)
  searchInput.addEventListener("input", () => {
    if (searchInput.value.trim().length > 0) {
      clearSearchBtn.classList.remove("hidden");
    } else {
      clearSearchBtn.classList.add("hidden");
    }

    clearTimeout(searchDebounceTimeout);
    searchDebounceTimeout = setTimeout(() => {
      fetchEvents();
    }, 250);
  });

  clearSearchBtn.addEventListener("click", () => {
    searchInput.value = "";
    clearSearchBtn.classList.add("hidden");
    fetchEvents();
  });

  function setCategory(category) {
    currentCategory = category;

    // Update button styling
    const buttons = categoryFilters.querySelectorAll(".filter-btn");
    buttons.forEach((btn) => {
      if (btn.getAttribute("data-category") === category) {
        btn.className = "filter-btn px-4 py-1.5 rounded-full text-xs sm:text-sm font-semibold transition shadow-sm bg-purple-600 text-white";
      } else {
        btn.className = "filter-btn px-4 py-1.5 rounded-full text-xs sm:text-sm font-medium transition bg-slate-100 text-slate-700 hover:bg-slate-200";
      }
    });

    fetchEvents();
  }

  categoryFilters.addEventListener("click", (e) => {
    const btn = e.target.closest(".filter-btn");
    if (!btn) return;
    const cat = btn.getAttribute("data-category");
    if (cat) {
      setCategory(cat);
    }
  });

  // Initial load
  fetchEvents();
});
