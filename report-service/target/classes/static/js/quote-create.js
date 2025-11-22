document.addEventListener("DOMContentLoaded", () => {
    console.log("Quote Create JS Loaded");

    const addItemBtn      = document.getElementById("add-item-btn");
    const itemsContainer  = document.getElementById("items-container");
    const addPromoBtn     = document.getElementById("addPromoBtn");
    const promoContainer  = document.getElementById("promo-rows-container");
    const totalSpan       = document.getElementById("totalAmountDisplay");

    // ================= LOAD VEHICLES (Trims) =================
    async function loadVehicles(selectElement) {
        try {
            const res  = await fetch("/dealer/quotes/api/trims");
            const data = await res.json();

            selectElement.innerHTML = `<option value="">-- Select Vehicle --</option>`;
            data.forEach(v => {
                const opt = document.createElement("option");
                opt.value = v.trimId;   // chọn trimId
                opt.dataset.price = v.price;
                opt.textContent = `${v.vehicleName} - ${v.trimName} (${Number(v.price).toLocaleString("vi-VN")} ₫)`;
                selectElement.appendChild(opt);
            });
        } catch (e) {
            console.error("Load vehicle error:", e);
        }
    }

    // ================= LOAD PROMOTIONS =================
    async function loadPromotions(selectElement) {
        selectElement.innerHTML = "<option>Loading...</option>";

        try {
            const res  = await fetch("/dealer/quotes/api/promotions");
            const data = await res.json();

            selectElement.innerHTML = `<option value="">-- Select Promotion --</option>`;
            data.forEach(p => {
                selectElement.innerHTML += `
                    <option value="${p.id}">${p.name} (${p.discount}%)</option>`;
            });

        } catch (err) {
            console.error("Load promo error: ", err);
            selectElement.innerHTML = `<option>Error loading</option>`;
        }
    }

    // ================== TÍNH TỔNG ITEM + PROMO ==================
    function recalcTotals() {
        let total = 0;

        document.querySelectorAll(".item-row").forEach(row => {
            const qtyInput   = row.querySelector(".qty");
            const priceInput = row.querySelector(".price");

            const qty   = qtyInput ? Number(qtyInput.value || 0)   : 0;
            const price = priceInput ? Number(priceInput.value || 0) : 0;

            total += qty * price;
        });

        // Discount
        let discountPercent = 0;
        document.querySelectorAll(".promo").forEach(select => {
            const opt = select.options[select.selectedIndex];
            if (!opt) return;
            const label = opt.textContent || "";
            const match = label.match(/(\d+(?:\.\d+)?)%/);
            if (match) discountPercent += Number(match[1]);
        });

        let final = total - (discountPercent > 0 ? total * discountPercent / 100 : 0);

        totalSpan.textContent = final.toLocaleString("vi-VN") + " ₫";
    }

    // ================== ADD ITEM ROW ==================
    function addItemRow() {
        const index = document.querySelectorAll(".item-row").length;

        const row = document.createElement("div");
        row.className = "item-row";

        row.innerHTML = `
            <select class="vehicle input-field" name="items[${index}].trimId">
                <option value="">-- Select Vehicle --</option>
            </select>
            <input class="qty input-field" type="number" value="1" min="1"
                   name="items[${index}].quantity" />
            <input class="price input-field" type="number" placeholder="Unit Price" readonly
                   name="items[${index}].unitPrice" />
            <button type="button" class="remove-item-btn">🗑</button>
        `;

        const vehicleSelect = row.querySelector(".vehicle");
        const qtyInput      = row.querySelector(".qty");
        const priceInput    = row.querySelector(".price");

        loadVehicles(vehicleSelect);

        vehicleSelect.addEventListener("change", () => {
            const selected = vehicleSelect.selectedOptions[0];
            const price = selected && selected.dataset.price ? Number(selected.dataset.price) : 0;
            priceInput.value = price;
            recalcTotals();
        });

        qtyInput.addEventListener("input", recalcTotals);

        row.querySelector(".remove-item-btn").addEventListener("click", () => {
            row.remove();

            // UPDATE INDEX NAME
            document.querySelectorAll(".item-row").forEach((r, i) => {
                r.querySelector(".vehicle").setAttribute("name", `items[${i}].trimId`);
                r.querySelector(".qty").setAttribute("name", `items[${i}].quantity`);
                r.querySelector(".price").setAttribute("name", `items[${i}].unitPrice`);
            });

            recalcTotals();
        });

        itemsContainer.appendChild(row);
    }

    // ================== ADD PROMO ROW ==================
    function addPromoRow() {
        const row = document.createElement("div");
        row.className = "promo-row";

        row.innerHTML = `
            <select class="promo input-field"></select>
            <button type="button" class="remove-promo-btn">🗑</button>
        `;

        const promoSelect = row.querySelector(".promo");
        loadPromotions(promoSelect);

        promoSelect.addEventListener("change", recalcTotals);

        row.querySelector(".remove-promo-btn").addEventListener("click", () => {
            row.remove();
            recalcTotals();
        });

        promoContainer.appendChild(row);
    }

    // ================== BIND EVENTS ==================
    addItemBtn.addEventListener("click", addItemRow);
    addPromoBtn.addEventListener("click", addPromoRow);
    document.addEventListener("input", recalcTotals);
    document.addEventListener("change", recalcTotals);
});
