document.addEventListener("DOMContentLoaded", function () {
    const container = document.getElementById("items-container");
    const addBtn = document.getElementById("add-item-btn");
    const totalInput = document.getElementById("totalAmount");
    let itemIndex = 0;

    const vehicles = window._vehicles || [];
    console.log("Loaded vehicles:", vehicles);

    if (!addBtn || !container || !totalInput) return;

    // ========== Add Item ==========
    addBtn.addEventListener("click", function () {
        const div = document.createElement("div");
        div.classList.add("item-row");

        let options = '<option value="">-- Select Vehicle --</option>';
        vehicles.forEach(v => {
            options += `<option value="${v.id}" data-price="${v.price || 0}">
                ${v.brand} ${v.modelName} (${v.modelCode})
            </option>`;
        });

        div.innerHTML = `
            <select name="items[${itemIndex}].vehicleId" required>
                ${options}
            </select>
            <input type="number" name="items[${itemIndex}].quantity" placeholder="Qty"
                   min="1" value="1" required>
            <input type="number" name="items[${itemIndex}].unitPrice" placeholder="Unit Price"
                   class="unit-price-input" step="0.01" required>
            <button type="button" class="remove-item-btn">🗑</button>
        `;

        container.appendChild(div);
        itemIndex++;

        const select = div.querySelector("select");
        const qtyInput = div.querySelector('[name$=".quantity"]');
        const priceInput = div.querySelector(".unit-price-input");

        // Auto-fill price khi chọn xe
        select.addEventListener("change", function () {
            const selectedOption = select.options[select.selectedIndex];
            const price = selectedOption.getAttribute("data-price");
            priceInput.value = price && parseFloat(price) > 0 ? price : "";
            updateTotal();
        });

        // Update total khi nhập số lượng / giá
        qtyInput.addEventListener("input", updateTotal);
        priceInput.addEventListener("input", updateTotal);

        // Nút xoá item
        div.querySelector(".remove-item-btn").addEventListener("click", () => {
            div.remove();
            updateTotal();
        });

        updateTotal();
    });

    // ========== Cập nhật tổng ==========
    function updateTotal() {
        const items = container.querySelectorAll(".item-row");
        let total = 0;
        items.forEach(row => {
            const qty = parseFloat(row.querySelector('[name$=".quantity"]').value) || 0;
            const price = parseFloat(row.querySelector('[name$=".unitPrice"]').value) || 0;
            total += qty * price;
        });
        totalInput.value = Number.isFinite(total) ? total.toFixed(2) : "0.00";
    }
});
