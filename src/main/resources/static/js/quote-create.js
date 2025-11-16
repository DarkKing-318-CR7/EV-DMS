document.addEventListener("DOMContentLoaded", () => {
    console.log("Quote Create JS Loaded");
    // ================= LOAD VEHICLES =================
    async function loadVehicles(selectElement) {
        try {
            const res = await fetch("/dealer/quotes/api/trims");
            const data = await res.json();

            selectElement.innerHTML = `<option value="">-- Select Vehicle --</option>`;
            data.forEach(v => {
                const opt = document.createElement("option");
                opt.value = v.trimId;
                opt.dataset.price = v.price;
                opt.textContent = `${v.vehicleName} - ${v.trimName} (${v.price.toLocaleString("vi-VN")} ₫)`;
                selectElement.appendChild(opt);
            });

        } catch (e) {
            console.error("Load vehicle error:", e);
        }
    }

    // ================= LOAD PROMOTIONS =================
    async function loadPromotions(dropdown) {
        dropdown.innerHTML = "<option>Loading...</option>";

        try {
            const res = await fetch("/dealer/quotes/api/promotions");
            const data = await res.json();

            dropdown.innerHTML = `<option value="">-- Select Promotion --</option>`;
            data.forEach(p => {
                dropdown.innerHTML += `<option value="${p.id}">
                    ${p.name} (${p.discount})
                </option>`;
            });

        } catch (err) {
            console.error("Load promo error: ", err);
            dropdown.innerHTML = `<option>Error loading</option>`;
        }
    }





    const addItemBtn = document.getElementById("add-item-btn");
    const itemsContainer = document.getElementById("items-container");

    const addPromoBtn = document.getElementById("addPromoBtn");
    const promoContainer = document.getElementById("promo-rows-container");

    function updateTotal() {
        let total = 0;
        document.querySelectorAll(".item-row").forEach(row => {
            const qty = parseInt(row.querySelector(".qty").value) || 0;
            const price = parseFloat(row.querySelector(".price").value) || 0;
            total += qty * price;
        });

        document.getElementById("totalAmountInput").value = total;
        document.getElementById("totalAmountDisplay").textContent =
            total.toLocaleString("vi-VN") + " ₫";
    }

    function addItemRow() {
        const row = document.createElement("div");
        row.className = "item-row";

        row.innerHTML = `
            <select class="vehicle input-field">
                <option value="">-- Select Vehicle --</option>
            </select>
            <input class="qty input-field" type="number" value="1" min="1" />
            <input class="price input-field" type="number" placeholder="Unit Price" readonly />
            <button class="remove-item-btn">🗑</button>
        `;


        const vehicleSelect = row.querySelector(".vehicle");
        const qtyInput = row.querySelector(".qty");
        const priceInput = row.querySelector(".price");

        // Load vehicle list
        loadVehicles(vehicleSelect);

        // Khi chọn xe -> gán giá
        vehicleSelect.addEventListener("change", () => {
            const selected = vehicleSelect.selectedOptions[0];
            let price = selected.dataset.price || 0;
            priceInput.value = price;
            updateTotal();
        });

        // update realtime
        qtyInput.addEventListener("input", updateTotal);

        row.querySelector(".remove-item-btn").addEventListener("click", () => {
            row.remove();
            updateTotal();
        });

        itemsContainer.appendChild(row);
    }


    function addPromoRow() {
        const row = document.createElement("div");
        row.className = "promo-row";

        row.innerHTML = `
            <select class="promo input-field">
                <option value="">-- Select promotion --</option>
            </select>
            <button class="remove-promo-btn">🗑</button>
        `;

        const promoSelect = row.querySelector(".promo");

        loadPromotions(promoSelect);

        row.querySelector(".remove-promo-btn").addEventListener("click", () => row.remove());

        promoContainer.appendChild(row);
    }


    addItemBtn.addEventListener("click", addItemRow);
    addPromoBtn.addEventListener("click", addPromoRow);
});
