// ========================================
// QUOTE-CREATE.JS (FINAL FULL VERSION)
// ========================================

document.addEventListener("DOMContentLoaded", () => {
  console.log("✅ Quote Create JS loaded");

  const addItemBtn = document.getElementById("add-item-btn");
  const itemsContainer = document.getElementById("items-container");

  if (!addItemBtn || !itemsContainer) return;

  addItemBtn.addEventListener("click", addItemRow);

  // ================================
  // 🧩 Helper: Lấy danh sách xe
  // ================================
  async function getVehicles() {
    if (window._vehicles && Array.isArray(window._vehicles)) return window._vehicles;

        const urls = ["/dealer/quotes/api/trims"];

    for (const url of urls) {
      try {
        const res = await fetch(url, { headers: { "Accept": "application/json" } });
        if (res.ok) {
          const data = await res.json();
          if (Array.isArray(data) && data.length > 0) {
            window._vehicles = data;
            console.log(`✅ Vehicles loaded from ${url}`, data);
            return data;
          }
        }
      } catch (err) {
        console.warn("Fetch error at", url, err);
      }
    }

    console.error("🚨 Không tìm được endpoint trả danh sách vehicles!");
    return [];
  }

  // 💰 Helper: Lấy giá xe theo modelCode
  async function getVehiclePrice(trimId) {

    if (!trimId) return null;

    try {
        const res = await fetch(`/dealer/quotes/price/${trimId}`, {
        headers: { "Accept": "application/json" },
      });
      if (res.ok) {
        const price = await res.json();
        console.log(`💰 Giá xe Trim ${trimId}:`, price);
        return price;
      }
    } catch (err) {
      console.error("❌ Lỗi khi lấy giá xe:", err);
    }

    console.warn("⚠️ Không tìm thấy giá cho xe:", modelCode);
    return null;
  }

  // ================================
  // 🧮 Helper: Cập nhật tổng tiền
  // ================================
  function updateTotalAmount() {
    const rows = document.querySelectorAll(".item-row");
    let total = 0;

    rows.forEach(row => {
      const qty = parseFloat(row.querySelector("input[type='number']").value) || 0;
      let priceInputEl = row.querySelector("input[placeholder='Unit Price']");
      let price = parseFloat(priceInputEl.dataset.raw || "0");
      if (!isNaN(qty) && !isNaN(price)) {
        total += qty * price;
      }
    });

    // ✅ Cập nhật input hidden và hiển thị đẹp
    const totalInputHidden = document.getElementById("totalAmountInput");
    const totalInputForm = document.getElementById("totalAmount");
    const display = document.getElementById("totalAmountDisplay");

    if (totalInputHidden) totalInputHidden.value = total;
    if (totalInputForm) totalInputForm.value = total;
    if (display) display.textContent = new Intl.NumberFormat("vi-VN").format(total) + " ₫";

    console.log("💰 Tổng tiền:", total);
    updateFinalAmount();
  }


  // ================================
  // ➕ Tạo 1 dòng Item
  // ================================
  async function addItemRow() {
    const itemRow = document.createElement("div");
    itemRow.className = "item-row";

    const vehicleSelect = document.createElement("select");
    vehicleSelect.innerHTML = `<option value="">-- Select Vehicle --</option>`;

    const qtyInput = document.createElement("input");
    qtyInput.type = "number";
    qtyInput.min = "1";
    qtyInput.value = "1";

    const priceInput = document.createElement("input");
    priceInput.type = "text";
    priceInput.placeholder = "Unit Price";
    priceInput.readOnly = true;

    const removeBtn = document.createElement("button");
    removeBtn.type = "button";
    removeBtn.className = "remove-item-btn";
    removeBtn.textContent = "🗑️";

    // 👉 Khi bấm xóa dòng
    removeBtn.addEventListener("click", () => {
      itemRow.remove();
      updateTotalAmount(); // ✅ tính lại tổng
    });

    // 👉 Khi thay đổi số lượng
    qtyInput.addEventListener("input", updateTotalAmount);

    itemRow.append(vehicleSelect, qtyInput, priceInput, removeBtn);
    itemsContainer.appendChild(itemRow);

    // === Load danh sách xe từ API ===
    const vehicles = await getVehicles();
    vehicles.forEach((v) => {
      const opt = document.createElement("option");
      opt.value = v.trimId;
      opt.textContent = `${v.vehicleName} – ${v.trimName} (${new Intl.NumberFormat("vi-VN").format(v.price)} ₫)`;
      vehicleSelect.appendChild(opt);
    });

    // === Khi chọn xe => lấy giá từ API ===
       vehicleSelect.addEventListener("change", async () => {
         const trimId = vehicleSelect.value;
         if (!trimId) {
           priceInput.value = "";
           updateTotalAmount();
           return;
         }

         priceInput.value = "Loading...";
         const price = await getVehiclePrice(trimId);

         if (price && !isNaN(price)) {
           // ✅ Hiển thị format đẹp, nhưng lưu số thật vào thuộc tính riêng
             priceInput.value = new Intl.NumberFormat("vi-VN").format(price);
             priceInput.dataset.raw = price; // lưu giá trị gốc (không format)
         } else {
           priceInput.value = 0;
         }

         updateTotalAmount(); // ✅ tính lại tổng
       });

  }
});


// ==================== PROMOTION DROPDOWN HANDLER ====================
document.addEventListener("DOMContentLoaded", () => {
  console.log("✅ Quote Create JS loaded");


  // ====== PROMOTION ROWS ======
    const addPromoBtn = document.getElementById("addPromoBtn");
    const promoContainer = document.getElementById("promo-rows-container");
    const promoTemplate = document.getElementById("promo-row-template");

    if (addPromoBtn && promoContainer && promoTemplate) {
      addPromoBtn.addEventListener("click", () => {
        // clone dòng template
        const row = promoTemplate.cloneNode(true);
        row.style.display = "flex";
        row.removeAttribute("id"); // tránh trùng id

        const select = row.querySelector("select.promo-select");
        const removeBtn = row.querySelector(".remove-promo-btn");

        if (select) {
          select.addEventListener("change", updateFinalAmount);
        }
        if (removeBtn) {
          removeBtn.addEventListener("click", () => {
            row.remove();
            updateFinalAmount();
          });
        }

        promoContainer.appendChild(row);
      });
    }
});

// ========= TÍNH TIỀN CUỐI CÙNG =========
function updateFinalAmount() {
  // Lấy giá trị tổng gốc (từ input ẩn hoặc ô tính tổng)
  const totalInput = document.getElementById("totalAmountInput");
  const baseAmount = totalInput ? parseFloat(totalInput.value || 0) : 0;

  // Tổng phần trăm khuyến mãi
  let totalDiscountPercent = 0;

  // Duyệt qua tất cả các select khuyến mãi đang hiển thị
  document.querySelectorAll("select.promo-select").forEach(sel => {
    const opt = sel.selectedOptions[0];
    if (!opt) return;

    const percent = parseFloat(opt.dataset.percent || "0");
    if (!isNaN(percent)) {
      totalDiscountPercent += percent;
    }
  });

  // Tính lại giá cuối cùng
  let final = baseAmount * (1 - totalDiscountPercent / 100);
  if (final < 0) final = 0; // tránh âm giá

  // Format tiền kiểu Việt Nam
  const formatted = new Intl.NumberFormat("vi-VN").format(final);

  // Ghi ra giao diện
  const displayEl = document.getElementById("totalAmountDisplay");
  if (displayEl) {
    displayEl.textContent = formatted + " ₫";
  }

  // (Tùy chọn) cập nhật lại input ẩn nếu cần lưu khi submit
  const hiddenFinalInput = document.getElementById("finalAmountInput");
  if (hiddenFinalInput) {
    hiddenFinalInput.value = final;
  }
}



