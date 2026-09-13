package com.mycar.app.data.model

data class CatalogItem(
    val id: String,
    val name: String,
    val category: String,
    val defaultKmInterval: Int,
    val defaultTimeIntervalMonths: Int,
    val description: String = ""
)

object CatalogData {
    val items: List<CatalogItem> = listOf(
        CatalogItem("cat-engine-oil", "روغن موتور", "مایعات و روانکارها", 6000, 6, "تعویض روغن موتور و فیلترها"),
        CatalogItem("cat-oil-filter", "فیلتر روغن", "فیلترها", 6000, 6, "تعویض همزمان با روغن موتور"),
        CatalogItem("cat-air-filter", "فیلتر هوا", "فیلترها", 6000, 6, "تصفیه هوای ورودی به منیفولد"),
        CatalogItem("cat-cabin-filter", "فیلتر اتاق (کابین)", "فیلترها", 12000, 12, "فیلتر تهویه مطبوع کابین"),
        CatalogItem("cat-fuel-filter", "فیلتر بنزین (صافی بنزین)", "فیلترها", 15000, 12, "جلوگیری از ورود ذرات به انژکتور"),
        CatalogItem("cat-spark-plugs", "شمع موتور", "سیستم جرقه‌زنی", 30000, 24, "سرویس یا تعویض شمع‌های جرقه‌زن"),
        CatalogItem("cat-timing-belt", "تسمه تایم", "موتور و تسمه‌ها", 60000, 48, "کیت تسمه تایم و بلبرینگ تایم"),
        CatalogItem("cat-alternator-belt", "تسمه دینام (تسمه شیاردار)", "موتور و تسمه‌ها", 40000, 36, "تسمه تجهیزات جانبی و دینام"),
        CatalogItem("cat-brake-pads-front", "لنت ترمز جلو", "سیستم ترمز", 25000, 18, "بررسی ضخامت گوشت لنت جلو"),
        CatalogItem("cat-brake-pads-rear", "لنت ترمز عقب", "سیستم ترمز", 40000, 30, "بررسی لنت یا کاسه چرخ عقب"),
        CatalogItem("cat-brake-fluid", "روغن ترمز", "مایعات و روانکارها", 40000, 24, "تخلیه و تعویض کامل روغن ترمز DOT4"),
        CatalogItem("cat-coolant", "ضدیخ و مایع خنک‌کننده", "سیستم خنک‌کاری", 30000, 24, "مخلوط آب مقطر و ضدیخ استاندارد"),
        CatalogItem("cat-gearbox-oil", "واسکازین (روغن گیربکس)", "مایعات و روانکارها", 40000, 36, "تعویض روغن جعبه‌دنده دستی یا اتوماتیک"),
        CatalogItem("cat-battery", "باتری خودرو", "برق و باتری", 50000, 24, "تست سلامت، دینام و تعویض باتری"),
        CatalogItem("cat-wipers", "تیغه برف‌پاک‌کن", "دید و بدنه", 20000, 12, "تعویض لاستیک یا کل تیغه‌های جلو"),
        CatalogItem("cat-tires", "لاستیک‌ها (تایر)", "چرخ و تعلیق", 60000, 48, "جابجایی ضربدری و بررسی عمق عاج"),
        CatalogItem("cat-wheel-alignment", "میزان فرمان و بالانس چرخ", "چرخ و تعلیق", 15000, 12, "تنظیم زوایای چرخ و فرمان"),
        CatalogItem("cat-engine-tuneup", "تنظیم موتور و شستشوی انژکتور", "موتور و سوخت‌رسانی", 25000, 18, "دیاگ، شستشوی سوزن‌ها و دریچه گاز"),
        CatalogItem("cat-hydraulic-oil", "روغن هیدرولیک فرمان", "مایعات و روانکارها", 50000, 36, "سرویس پمپ و تعویض روغن هیدرولیک"),
        CatalogItem("cat-technical-inspection", "معاینه فنی سالانه", "اداری و قانونی", 30000, 12, "اخذ برگه تاییدیه معاینه فنی"),
        CatalogItem("cat-third-party-insurance", "بیمه شخص ثالث", "اداری و قانونی", 30000, 12, "تمدید سالانه بیمه‌نامه شخص ثالث"),
        CatalogItem("cat-body-insurance", "بیمه بدنه", "اداری و قانونی", 30000, 12, "تمدید سالانه بیمه‌نامه بدنه"),
        CatalogItem("cat-clutch-kit", "کیت کلاچ (دیسک و صفحه)", "انتقال قدرت", 70000, 48, "تعویض دیسک، صفحه و بلبرینگ کلاچ"),
        CatalogItem("cat-suspension", "جلوبندی و سیستم تعلیق", "چرخ و تعلیق", 30000, 24, "بررسی سیبک، بوش و کمک‌فنرها")
    )
}
