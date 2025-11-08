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

    const urls = ["/dealer/quotes/api/vehicles"];
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
  async function getVehiclePrice(modelCode) {
    if (!modelCode) return null;

    try {
      const res = await fetch(`/dealer/quotes/price/${modelCode}`, {
        headers: { "Accept": "application/json" },
      });
      if (res.ok) {
        const price = await res.json();
        console.log(`💰 Giá xe ${modelCode}:`, price);
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
      const price = parseFloat(row.querySelector("input[placeholder='Unit Price']").value) || 0;
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
      opt.value = v.modelCode; // ✅ dùng modelCode làm value
      opt.textContent = v.modelName || v.modelCode;
      vehicleSelect.appendChild(opt);
    });

    // === Khi chọn xe => lấy giá từ API ===
    vehicleSelect.addEventListener("change", async () => {
      const modelCode = vehicleSelect.value;
      if (!modelCode) {
        priceInput.value = "";
        updateTotalAmount(); // ✅ cập nhật tổng khi bỏ chọn
        return;
      }

      priceInput.value = "Loading...";
      const price = await getVehiclePrice(modelCode);

      if (price !== null && !isNaN(price)) {
        priceInput.value = parseFloat(price);
      } else {
        priceInput.value = 0;
      }



      updateTotalAmount(); // ✅ tính lại tổng sau khi load giá
    });
  }
});


// ==================== PROMOTION CHECKBOX HANDLER ====================
document.querySelectorAll("input[name='promotionIds']").forEach(cb => {
  cb.addEventListener("change", updateFinalAmount);
});

function updateFinalAmount() {
  const baseAmount = parseFloat(document.getElementById("totalAmountInput").value || 0);
  const checkboxes = document.querySelectorAll("input[name='promotionIds']:checked");
  let totalDiscountPercent = 0;

  checkboxes.forEach(cb => {
    const percent = parseFloat(cb.dataset.percent || 0);
    totalDiscountPercent += percent;
  });

  let final = baseAmount * (1 - totalDiscountPercent / 100);
  if (final < 0) final = 0;

  const formatted = new Intl.NumberFormat("vi-VN").format(final);
  document.getElementById("totalAmountDisplay").textContent = formatted + " ₫";
}
