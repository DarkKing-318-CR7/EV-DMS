package com.uth.ev_dms.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static class CacheNames {

        // ========= Phan 1: Products & Inventory =========
        public static final String VEHICLES = "vehicles";
        public static final String TRIMS = "trims";
        public static final String PRICE_LISTS = "priceLists";

        // Inventory
        public static final String INVENTORY_LIST = "inventoryList";
        public static final String INVENTORY_ONE = "inventoryOne";
        public static final String INVENTORY_BY_BRANCH = "inventoryByBranch";
        public static final String INVENTORY_BY_DEALER = "inventoryByDealer";
        public static final String INVENTORY_ADJUSTMENTS = "inventoryAdjustments";

        // ========= Phan 2: Customers & Test Drive =========
        public static final String CUSTOMERS_BY_OWNER = "customersByOwner";
        public static final String CUSTOMERS_ALL = "customersAll";

        public static final String TESTDRIVES_BY_OWNER = "testdrivesByOwner";
        public static final String TESTDRIVES_MANAGER = "testdrivesManager";

        // ========= Phan 3: Quotes & Promotions =========
        public static final String PROMOTIONS_ACTIVE = "promotionsActive";
        public static final String QUOTES_MY = "quotesMy";
        public static final String QUOTES_PENDING = "quotesPending";

        // ========= Phan 4: Orders & Allocation & Payments =========
        public static final String ORDERS_MY = "ordersMy";
        public static final String ORDERS_MANAGER = "ordersManager";
        public static final String ORDERS_DEALER = "ordersDealer";
        public static final String ORDER_ITEMS = "orderItems";

        public static final String PAYMENTS_BY_ORDER = "paymentsByOrder";

        // ========= Phan 5: Reports & Settings =========
        public static final String SYSTEM_PARAMS = "systemParams";
        public static final String REPORTS_BRANCH = "reportsBranch";
        public static final String REPORTS_STAFF = "reportsStaff";
        public static final String REPORTS_REGION = "reportsRegion";
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(

                // ===== PRODUCTS =====
                CacheNames.VEHICLES,
                CacheNames.TRIMS,
                CacheNames.PRICE_LISTS,

                // ===== INVENTORY =====
                CacheNames.INVENTORY_LIST,
                CacheNames.INVENTORY_ONE,
                CacheNames.INVENTORY_BY_BRANCH,
                CacheNames.INVENTORY_BY_DEALER,
                CacheNames.INVENTORY_ADJUSTMENTS,

                // ===== CUSTOMERS =====
                CacheNames.CUSTOMERS_ALL,
                CacheNames.CUSTOMERS_BY_OWNER,

                // ===== TESTDRIVE =====
                CacheNames.TESTDRIVES_MANAGER,
                CacheNames.TESTDRIVES_BY_OWNER,

                // ===== PROMOTION =====
                CacheNames.PROMOTIONS_ACTIVE,

                // ===== QUOTES (du phong) =====
                CacheNames.QUOTES_MY,
                CacheNames.QUOTES_PENDING,

                // ===== ORDERS =====
                CacheNames.ORDERS_MANAGER,
                CacheNames.ORDERS_MY,
                CacheNames.ORDERS_DEALER,
                CacheNames.ORDER_ITEMS,

                // ===== PAYMENTS =====
                CacheNames.PAYMENTS_BY_ORDER,

                // ===== REPORTS / SETTINGS (du phong) =====
                CacheNames.SYSTEM_PARAMS,
                CacheNames.REPORTS_BRANCH,
                CacheNames.REPORTS_STAFF,
                CacheNames.REPORTS_REGION
        );
    }
}
