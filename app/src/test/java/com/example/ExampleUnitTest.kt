package com.example

import com.example.data.model.Product
import com.example.data.model.ProductWithDetails
import com.example.data.model.PurchasePrice
import com.example.data.model.SellingPrice
import com.example.data.model.UnitConversion
import com.example.util.BackupHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {

    @Test
    fun testModalAndProfitCalculation_directUnit() {
        val product = Product(id = 1, name = "Paku Beton", sku = "PK-01")
        val purchase = PurchasePrice(id = 1, productId = 1, price = 50000.0, unit = "kotak")
        val selling = SellingPrice(id = 1, productId = 1, price = 65000.0, unit = "kotak")

        val pwd = ProductWithDetails(
            product = product,
            purchasePrices = listOf(purchase),
            sellingPrices = listOf(selling)
        )

        val calcs = pwd.getCalculatedSellingPrices()
        assertEquals(1, calcs.size)
        assertEquals(50000.0, calcs[0].modalPrice)
        assertEquals(15000.0, calcs[0].profit)
        // Margin: (15000 / 65000) * 100 = 23.0769%
        assertEquals(23.08, calcs[0].profitPercentage!!, 0.02)
    }

    @Test
    fun testModalAndProfitCalculation_withUnitConversion() {
        // 1 Bal = 100 Meter. Beli 1 Bal = Rp 500.000. Modal per meter = Rp 5.000
        val product = Product(id = 1, name = "Karpet Plastik", sku = "KP-01")
        val purchase = PurchasePrice(id = 1, productId = 1, price = 500000.0, unit = "Bal")
        val conversion = UnitConversion(id = 1, productId = 1, fromUnit = "Bal", quantity = 100.0, toUnit = "Meter")
        val sellingEcer = SellingPrice(id = 1, productId = 1, price = 7500.0, unit = "Meter")
        val sellingGrosir = SellingPrice(id = 2, productId = 1, price = 650000.0, unit = "Bal")

        val pwd = ProductWithDetails(
            product = product,
            purchasePrices = listOf(purchase),
            conversions = listOf(conversion),
            sellingPrices = listOf(sellingEcer, sellingGrosir)
        )

        val calcs = pwd.getCalculatedSellingPrices()
        assertEquals(2, calcs.size)

        // Meter check: modal 5000, jual 7500, untung 2500, margin = 2500 / 7500 = 33.33%
        val meterCalc = calcs.find { it.sellingPrice.unit == "Meter" }
        assertNotNull(meterCalc)
        assertEquals(5000.0, meterCalc!!.modalPrice!!, 0.01)
        assertEquals(2500.0, meterCalc.profit!!, 0.01)
        assertEquals(33.33, meterCalc.profitPercentage!!, 0.02)

        // Bal check: modal 500000, jual 650000, untung 150000, margin = 150000 / 650000 = 23.08%
        val balCalc = calcs.find { it.sellingPrice.unit == "Bal" }
        assertNotNull(balCalc)
        assertEquals(500000.0, balCalc!!.modalPrice!!, 0.01)
        assertEquals(150000.0, balCalc.profit!!, 0.01)
        assertEquals(23.08, balCalc.profitPercentage!!, 0.02)
    }

    @Test
    fun test_TestA_Pack() {
        // TEST A — PACK: Rp10.000 / Pack, 1 Pack = 10 Pcs. Modal: Rp1.000 / Pcs. Jual: Rp1.500 / Pcs. Laba: Rp500 / Pcs
        val product = Product(id = 101, name = "Kuas Nylon Set")
        val purchase = PurchasePrice(id = 101, productId = 101, price = 10000.0, unit = "Pack")
        val conversion = UnitConversion(id = 101, productId = 101, fromUnit = "Pack", quantity = 10.0, toUnit = "Pcs")
        val selling = SellingPrice(id = 101, productId = 101, price = 1500.0, unit = "Pcs")

        val pwd = ProductWithDetails(
            product = product,
            purchasePrices = listOf(purchase),
            conversions = listOf(conversion),
            sellingPrices = listOf(selling)
        )

        assertEquals(1000.0, pwd.calculateModalPriceForUnit("Pcs")!!, 0.01)
        val calcs = pwd.getCalculatedSellingPrices()
        assertEquals(1, calcs.size)
        assertEquals(1000.0, calcs[0].modalPrice!!, 0.01)
        assertEquals(1500.0, calcs[0].sellingPrice.price, 0.01)
        assertEquals(500.0, calcs[0].profit!!, 0.01)
        assertEquals(33.33, calcs[0].profitPercentage!!, 0.01)
    }

    @Test
    fun test_TestB_RollMeter_WithPotential() {
        // TEST B — ROLL/METER: Rp300.000 / Roll, 1 Roll = 100 Meter.
        // Modal: Rp3.000 / Meter. Jual: Rp40.000 / Meter. Laba: Rp37.000 / Meter. Margin: 92,50%
        // Potensi Omzet: 100 × Rp40.000 = Rp4.000.000, Modal: Rp300.000, Potensi Laba: Rp3.700.000
        val product = Product(id = 102, name = "Karpet")
        val purchase = PurchasePrice(id = 102, productId = 102, price = 300000.0, unit = "Roll")
        val conversion = UnitConversion(id = 102, productId = 102, fromUnit = "Roll", quantity = 100.0, toUnit = "Meter")
        val selling = SellingPrice(id = 102, productId = 102, price = 40000.0, unit = "Meter")

        val pwd = ProductWithDetails(
            product = product,
            purchasePrices = listOf(purchase),
            conversions = listOf(conversion),
            sellingPrices = listOf(selling)
        )

        assertEquals(3000.0, pwd.calculateModalPriceForUnit("Meter")!!, 0.01)
        val calcs = pwd.getCalculatedSellingPrices()
        val calc = calcs.first()
        assertEquals(3000.0, calc.modalPrice!!, 0.01)
        assertEquals(40000.0, calc.sellingPrice.price, 0.01)
        assertEquals(37000.0, calc.profit!!, 0.01)
        assertEquals(92.5, calc.profitPercentage!!, 0.01)

        // Verifikasi Potensi Penjualan Seluruh Isi
        assertEquals(4000000.0, calc.potentialRevenue!!, 0.01)
        assertEquals(3700000.0, calc.potentialProfit!!, 0.01)
        assertEquals(100.0, calc.potentialQuantity!!, 0.01)
    }

    @Test
    fun test_TestC_Selang() {
        // TEST C — SELANG: Rp200.000 / Roll, 1 Roll = 50 Meter.
        // Modal: Rp4.000 / Meter. Jual: Rp10.000 / Meter. Laba: Rp6.000 / Meter. Margin: 60%
        // Potensi Omzet: 50 × Rp10.000 = Rp500.000, Potensi Laba: Rp300.000
        val product = Product(id = 103, name = "Selang Air Fleksibel")
        val purchase = PurchasePrice(id = 103, productId = 103, price = 200000.0, unit = "Roll")
        val conversion = UnitConversion(id = 103, productId = 103, fromUnit = "Roll", quantity = 50.0, toUnit = "Meter")
        val selling = SellingPrice(id = 103, productId = 103, price = 10000.0, unit = "Meter")

        val pwd = ProductWithDetails(
            product = product,
            purchasePrices = listOf(purchase),
            conversions = listOf(conversion),
            sellingPrices = listOf(selling)
        )

        assertEquals(4000.0, pwd.calculateModalPriceForUnit("Meter")!!, 0.01)
        val calc = pwd.getCalculatedSellingPrices().first()
        assertEquals(4000.0, calc.modalPrice!!, 0.01)
        assertEquals(10000.0, calc.sellingPrice.price, 0.01)
        assertEquals(6000.0, calc.profit!!, 0.01)
        assertEquals(60.0, calc.profitPercentage!!, 0.01)

        assertEquals(500000.0, calc.potentialRevenue!!, 0.01)
        assertEquals(300000.0, calc.potentialProfit!!, 0.01)
        assertEquals(50.0, calc.potentialQuantity!!, 0.01)
    }

    @Test
    fun test_TestD_KarungKg() {
        // TEST D — KARUNG/KG: Rp55.000 / Karung, 1 Karung = 50 Kg.
        // Modal: Rp1.100 / Kg. Jual: Rp2.000 / Kg. Laba: Rp900 / Kg. Margin: 45%
        val product = Product(id = 104, name = "Semen Gresik")
        val purchase = PurchasePrice(id = 104, productId = 104, price = 55000.0, unit = "Karung")
        val conversion = UnitConversion(id = 104, productId = 104, fromUnit = "Karung", quantity = 50.0, toUnit = "Kg")
        val selling = SellingPrice(id = 104, productId = 104, price = 2000.0, unit = "Kg")

        val pwd = ProductWithDetails(
            product = product,
            purchasePrices = listOf(purchase),
            conversions = listOf(conversion),
            sellingPrices = listOf(selling)
        )

        assertEquals(1100.0, pwd.calculateModalPriceForUnit("Kg")!!, 0.01)
        val calc = pwd.getCalculatedSellingPrices().first()
        assertEquals(1100.0, calc.modalPrice!!, 0.01)
        assertEquals(2000.0, calc.sellingPrice.price, 0.01)
        assertEquals(900.0, calc.profit!!, 0.01)
        // Margin: 900 ÷ 2.000 × 100 = 45%
        assertEquals(45.0, calc.profitPercentage!!, 0.01)
    }

    @Test
    fun test_TestE_MultiHarga_Karpet() {
        // TEST E — MULTI HARGA: Karpet: 1 Roll = 100 Meter. Modal: Rp300.000 / Roll.
        // Harga jual: Rp40.000 / Meter dan Rp350.000 / Roll.
        // Keduanya hidup bersamaan dan tidak saling menimpa!
        val product = Product(id = 105, name = "Karpet Roll")
        val purchase = PurchasePrice(id = 105, productId = 105, price = 300000.0, unit = "Roll")
        val conversion = UnitConversion(id = 105, productId = 105, fromUnit = "Roll", quantity = 100.0, toUnit = "Meter")
        val sellingMeter = SellingPrice(id = 201, productId = 105, price = 40000.0, unit = "Meter")
        val sellingRoll = SellingPrice(id = 202, productId = 105, price = 350000.0, unit = "Roll")

        val pwd = ProductWithDetails(
            product = product,
            purchasePrices = listOf(purchase),
            conversions = listOf(conversion),
            sellingPrices = listOf(sellingMeter, sellingRoll)
        )

        assertEquals(300000.0, pwd.calculateModalPriceForUnit("Roll")!!, 0.01)
        assertEquals(3000.0, pwd.calculateModalPriceForUnit("Meter")!!, 0.01)

        val calcs = pwd.getCalculatedSellingPrices()
        assertEquals(2, calcs.size)

        // Verifikasi harga Meter
        val meterCalc = calcs.find { it.sellingPrice.unit == "Meter" }!!
        assertEquals(3000.0, meterCalc.modalPrice!!, 0.01)
        assertEquals(40000.0, meterCalc.sellingPrice.price, 0.01)
        assertEquals(37000.0, meterCalc.profit!!, 0.01)
        assertEquals(92.5, meterCalc.profitPercentage!!, 0.01)

        // Verifikasi harga Roll
        val rollCalc = calcs.find { it.sellingPrice.unit == "Roll" }!!
        assertEquals(300000.0, rollCalc.modalPrice!!, 0.01)
        assertEquals(350000.0, rollCalc.sellingPrice.price, 0.01)
        assertEquals(50000.0, rollCalc.profit!!, 0.01)
        // Margin: 50.000 ÷ 350.000 × 100 = 14,29%
        assertEquals(14.29, rollCalc.profitPercentage!!, 0.01)
    }

    @Test
    fun testUserTestCase_KuasNylonSet_PackAndPcs() {
        // Kuas Nylon Set: 1 pack = 10 pcs
        // Harga beli 1 pack = Rp 10.000
        // Harga jual 1 pack = Rp 15.000
        // Harga jual 1 pcs = Rp 1.500
        val product = Product(id = 2, name = "Kuas Nylon Set")
        val purchase = PurchasePrice(id = 2, productId = 2, price = 10000.0, unit = "pack")
        val conversion = UnitConversion(id = 2, productId = 2, fromUnit = "pack", quantity = 10.0, toUnit = "pcs")
        val sellingPack = SellingPrice(id = 10, productId = 2, price = 15000.0, unit = "pack")
        val sellingPcs = SellingPrice(id = 11, productId = 2, price = 1500.0, unit = "pcs")

        val pwd = ProductWithDetails(
            product = product,
            purchasePrices = listOf(purchase),
            conversions = listOf(conversion),
            sellingPrices = listOf(sellingPack, sellingPcs)
        )

        // Verifikasi Modal per unit
        assertEquals(10000.0, pwd.calculateModalPriceForUnit("pack")!!, 0.01)
        assertEquals(1000.0, pwd.calculateModalPriceForUnit("pcs")!!, 0.01)

        val calcs = pwd.getCalculatedSellingPrices()
        val packCalc = calcs.find { it.sellingPrice.unit == "pack" }!!
        assertEquals(10000.0, packCalc.modalPrice!!, 0.01)
        assertEquals(5000.0, packCalc.profit!!, 0.01)
        // Margin: 5.000 ÷ 15.000 × 100 = 33,33%
        assertEquals(33.33, packCalc.profitPercentage!!, 0.01)

        val pcsCalc = calcs.find { it.sellingPrice.unit == "pcs" }!!
        assertEquals(1000.0, pcsCalc.modalPrice!!, 0.01)
        assertEquals(500.0, pcsCalc.profit!!, 0.01)
        // Margin: 500 ÷ 1.500 × 100 = 33,33%
        assertEquals(33.33, pcsCalc.profitPercentage!!, 0.01)
    }

    @Test
    fun testUserTestCase_Semen_KarungAndKg() {
        // Semen: 1 karung = 50 kg
        // Harga beli 1 karung = Rp 55.000
        val product = Product(id = 3, name = "Semen Gresik")
        val purchase = PurchasePrice(id = 3, productId = 3, price = 55000.0, unit = "karung")
        val conversion = UnitConversion(id = 3, productId = 3, fromUnit = "karung", quantity = 50.0, toUnit = "kg")

        val pwd = ProductWithDetails(
            product = product,
            purchasePrices = listOf(purchase),
            conversions = listOf(conversion),
            sellingPrices = emptyList()
        )

        assertEquals(55000.0, pwd.calculateModalPriceForUnit("karung")!!, 0.01)
        // Modal per kg = 55.000 / 50 = 1.100
        assertEquals(1100.0, pwd.calculateModalPriceForUnit("kg")!!, 0.01)
    }

    @Test
    fun testUserTestCase_Karpet_BalAndMeter() {
        // Karpet: 1 bal = 20 meter
        // Harga beli 1 bal = Rp 500.000
        val product = Product(id = 4, name = "Karpet Lantai")
        val purchase = PurchasePrice(id = 4, productId = 4, price = 500000.0, unit = "bal")
        val conversion = UnitConversion(id = 4, productId = 4, fromUnit = "bal", quantity = 20.0, toUnit = "meter")

        val pwd = ProductWithDetails(
            product = product,
            purchasePrices = listOf(purchase),
            conversions = listOf(conversion),
            sellingPrices = emptyList()
        )

        assertEquals(500000.0, pwd.calculateModalPriceForUnit("bal")!!, 0.01)
        // Modal per meter = 500.000 / 20 = 25.000
        assertEquals(25000.0, pwd.calculateModalPriceForUnit("meter")!!, 0.01)
    }

    @Test
    fun testBackupAndRestoreJson() {
        val product = Product(
            id = 10,
            name = "Semen Tiga Roda",
            sku = "SM-001",
            category = "Semen",
            brand = "Tiga Roda",
            variant = "50kg",
            notes = "Gudang A",
            stock = 45.0,
            stockUnit = "sak",
            minimumStock = 10.0
        )
        val purchase = PurchasePrice(id = 1, productId = 10, price = 62000.0, unit = "sak")
        val selling = SellingPrice(id = 1, productId = 10, price = 70000.0, unit = "sak")

        val pwd = ProductWithDetails(
            product = product,
            purchasePrices = listOf(purchase),
            sellingPrices = listOf(selling)
        )

        val json = BackupHelper.exportToJson(listOf(pwd), emptyList())
        assertTrue(json.contains("SM-001"))
        assertTrue(json.contains("Semen Tiga Roda"))

        val parsed = BackupHelper.parseFromJson(json)
        assertEquals(1, parsed.products.size)
        val p = parsed.products[0].product
        assertEquals("Semen Tiga Roda", p.name)
        assertEquals("SM-001", p.sku)
        assertEquals(45.0, p.stock, 0.01)
        assertEquals("sak", p.stockUnit)
        assertEquals(10.0, p.minimumStock, 0.01)
        assertEquals(1, parsed.products[0].purchasePrices.size)
        assertEquals(62000.0, parsed.products[0].purchasePrices[0].price, 0.01)
    }

    @Test
    fun testExportCsv() {
        val product = Product(
            id = 10,
            name = "Pipa PVC",
            sku = "PP-02",
            category = "Pipa",
            brand = "Rucika",
            variant = "3/4 inch",
            stock = 20.0,
            stockUnit = "batang",
            minimumStock = 5.0
        )
        val purchase = PurchasePrice(id = 1, productId = 10, price = 35000.0, unit = "batang")
        val selling = SellingPrice(id = 1, productId = 10, price = 45000.0, unit = "batang")

        val pwd = ProductWithDetails(
            product = product,
            purchasePrices = listOf(purchase),
            sellingPrices = listOf(selling)
        )

        val csv = BackupHelper.exportToCsv(listOf(pwd))
        assertTrue(csv.contains("Kode/SKU,Nama Barang,Kategori"))
        assertTrue(csv.contains("PP-02"))
        assertTrue(csv.contains("Pipa PVC"))
        assertTrue(csv.contains("Rucika"))
    }
}
