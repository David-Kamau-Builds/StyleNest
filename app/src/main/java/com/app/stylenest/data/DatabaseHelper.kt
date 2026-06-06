package com.app.stylenest.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "stylenest.db"
        // v7: Refreshed all product names, prices, and images with realistic data
        private const val DATABASE_VERSION = 7
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                email TEXT UNIQUE,
                password TEXT,
                avatar_url TEXT,
                two_factor_enabled INTEGER DEFAULT 0
            )
        """)
        db.execSQL("""
            CREATE TABLE products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                description TEXT,
                rich_description TEXT,
                category TEXT,
                sub_category TEXT,
                sizes TEXT,
                price REAL,
                image_url TEXT,
                images TEXT
            )
        """)
        db.execSQL("""
            CREATE TABLE cart (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_email TEXT,
                product_id INTEGER,
                selected_size TEXT,
                quantity INTEGER,
                price REAL,
                FOREIGN KEY(product_id) REFERENCES products(id)
            )
        """)
        db.execSQL("""
            CREATE TABLE wishlist (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_email TEXT,
                product_id INTEGER,
                FOREIGN KEY(product_id) REFERENCES products(id)
            )
        """)
        db.execSQL("""
            CREATE TABLE orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_email TEXT,
                order_number TEXT,
                customer_name TEXT,
                phone TEXT,
                delivery_address TEXT,
                payment_method TEXT,
                total_amount REAL,
                order_date TEXT,
                status TEXT
            )
        """)
        db.execSQL("""
            CREATE TABLE order_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER,
                product_id INTEGER,
                size TEXT,
                quantity INTEGER,
                price REAL,
                FOREIGN KEY(order_id) REFERENCES orders(id),
                FOREIGN KEY(product_id) REFERENCES products(id)
            )
        """)

        seed100Products(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS order_items")
        db.execSQL("DROP TABLE IF EXISTS orders")
        db.execSQL("DROP TABLE IF EXISTS wishlist")
        db.execSQL("DROP TABLE IF EXISTS cart")
        db.execSQL("DROP TABLE IF EXISTS products")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    // ---------------------------------------------------------------------------
    // Seeder — 100 realistic products with brand names, KSh prices, and keyword-
    // matched images via loremflickr.com (free, keyword-indexed, lock=deterministic)
    // ---------------------------------------------------------------------------
    private fun seed100Products(db: SQLiteDatabase) {

        // Each SubCat holds 5 products. Images are keyword-matched per sub-category.
        data class SubCat(
            val category: String,
            val subCategory: String,
            val sizes: String,
            val imageKeyword: String,   // used in loremflickr URL
            val shortDesc: String,
            val longDesc: String,
            val products: List<Pair<String, Double>>  // name to KSh price
        )

        val catalog = listOf(

            // ── SHIRTS ──────────────────────────────────────────────────────────

            SubCat(
                category = "Shirts", subCategory = "Polos",
                sizes = "S,M,L,XL,XXL",
                imageKeyword = "polo,shirt",
                shortDesc = "Classic polo crafted from premium piqué cotton.",
                longDesc = "A timeless polo shirt made from breathable piqué cotton with a ribbed collar and two-button placket. Perfect for smart-casual occasions — office, weekend brunch, or a round of golf. Machine washable and colour-fast.",
                products = listOf(
                    "Lacoste Classic Piqué Polo"       to 4_500.0,
                    "Ralph Lauren Slim Fit Polo"        to 6_200.0,
                    "Calvin Klein Stretch Polo"         to 3_800.0,
                    "Tommy Hilfiger Tipped Polo"        to 5_500.0,
                    "Hugo Boss Regular Fit Polo"        to 7_200.0
                )
            ),

            SubCat(
                category = "Shirts", subCategory = "Tees",
                sizes = "S,M,L,XL,XXL",
                imageKeyword = "tshirt,men",
                shortDesc = "Everyday comfort meets modern street style.",
                longDesc = "A versatile crew-neck tee in 100% combed cotton or performance blend. Lightweight, breathable, and preshrunk for a consistently great fit. Goes with anything — jeans, shorts, or under a jacket.",
                products = listOf(
                    "Levi's Batwing Graphic Tee"        to 1_800.0,
                    "H&M Premium Cotton Tee"            to 1_200.0,
                    "Zara Slim Graphic Tee"             to 2_100.0,
                    "Nike Dri-FIT Training Tee"         to 3_200.0,
                    "Adidas 3-Stripe Essential Tee"     to 2_800.0
                )
            ),

            SubCat(
                category = "Shirts", subCategory = "Button-Downs",
                sizes = "S,M,L,XL,XXL",
                imageKeyword = "dress,shirt",
                shortDesc = "Elegant button-down shirts for every occasion.",
                longDesc = "A premium button-down shirt cut from fine cotton poplin or oxford cloth. Features a structured collar, full-length button placket, and barrel cuffs. Equally sharp tucked in for the boardroom or worn open over a tee on weekends.",
                products = listOf(
                    "Brooks Brothers Oxford OCBD"       to 5_800.0,
                    "Van Heusen Regular Fit Shirt"      to 3_200.0,
                    "Charles Tyrwhitt Slim Shirt"       to 7_500.0,
                    "Arrow White Formal Shirt"          to 2_800.0,
                    "Ted Baker Floral Print Shirt"      to 9_200.0
                )
            ),

            SubCat(
                category = "Shirts", subCategory = "Long-Sleeve",
                sizes = "S,M,L,XL,XXL",
                imageKeyword = "longsleeve,shirt",
                shortDesc = "Warm, stylish long-sleeve essentials.",
                longDesc = "A comfortable long-sleeve shirt in soft cotton or performance blend. Perfect for cooler days or layering under a jacket. Ribbed cuffs and a clean silhouette keep things polished whether you're hiking or heading to a café.",
                products = listOf(
                    "Uniqlo Supima Cotton Crewneck"         to 2_500.0,
                    "Gap Classic Henley Shirt"              to 2_200.0,
                    "Banana Republic Heritage Stripe LS"    to 4_800.0,
                    "The North Face Long-Sleeve Tee"        to 4_200.0,
                    "Patagonia Organic Cotton LS"           to 5_500.0
                )
            ),

            // ── TROUSERS ─────────────────────────────────────────────────────────

            SubCat(
                category = "Trousers", subCategory = "Jeans",
                sizes = "S,M,L,XL,XXL",
                imageKeyword = "jeans,denim",
                shortDesc = "Iconic denim from the world's most trusted brands.",
                longDesc = "Premium denim jeans with classic five-pocket styling, reinforced rivets, and durable stitching. Available in straight, slim, and tapered cuts. Sanforized fabric prevents shrinkage — these jeans are built to be your daily favourites for years.",
                products = listOf(
                    "Levi's 501 Original Straight Jeans"   to 7_500.0,
                    "Wrangler Regular Fit Jeans"            to 4_800.0,
                    "Lee Cooper Slim Taper Jeans"           to 5_200.0,
                    "G-Star Raw 3301 Slim Jeans"            to 12_000.0,
                    "Diesel Thommer Slim-Skinny Jeans"      to 15_000.0
                )
            ),

            SubCat(
                category = "Trousers", subCategory = "Chinos",
                sizes = "S,M,L,XL,XXL",
                imageKeyword = "chinos,trousers",
                shortDesc = "Smart-casual chinos for the modern wardrobe.",
                longDesc = "Versatile chino trousers in lightweight cotton twill with a flat-front waistband and clean, tailored silhouette. Side slash pockets and a back welt pocket complete the look. Dressed up with a blazer or down with loafers — they do it all.",
                products = listOf(
                    "Dockers Slim Fit Alpha Chinos"         to 4_500.0,
                    "Banana Republic Athletic Chino"        to 6_800.0,
                    "Tommy Hilfiger Straight Chino"         to 5_200.0,
                    "H&M Regular Fit Chinos"                to 2_800.0,
                    "Ralph Lauren Stretch Slim Chino"       to 8_500.0
                )
            ),

            SubCat(
                category = "Trousers", subCategory = "Sweatpants",
                sizes = "S,M,L,XL,XXL",
                imageKeyword = "sweatpants,joggers",
                shortDesc = "Ultimate comfort for training and lounging.",
                longDesc = "High-performance sweatpants in soft fleece or moisture-wicking fabric. Elastic waistband with drawstring, ribbed cuffs, and side zip pockets. Engineered to keep you comfortable from morning runs to evening couch sessions.",
                products = listOf(
                    "Nike Tech Fleece Joggers"              to 8_500.0,
                    "Adidas Tiro 21 Track Pants"            to 6_200.0,
                    "Champion Reverse Weave Sweatpants"     to 5_800.0,
                    "Under Armour Rival Fleece Jogger"      to 6_500.0,
                    "Puma Essential Fleece Sweatpants"      to 3_200.0
                )
            ),

            SubCat(
                category = "Trousers", subCategory = "Cargo",
                sizes = "S,M,L,XL,XXL",
                imageKeyword = "cargo,pants",
                shortDesc = "Durable cargo pants built for adventure.",
                longDesc = "Heavy-duty cargo pants engineered for outdoor use with multiple reinforced pockets and durable ripstop or canvas fabric. A relaxed fit provides maximum mobility on the trail or on site. Rugged enough for work, cool enough for the streets.",
                products = listOf(
                    "Dickies Relaxed Fit Cargo Pants"       to 4_500.0,
                    "Columbia Silver Ridge Cargo"           to 7_800.0,
                    "The North Face Paramount Cargo"        to 9_200.0,
                    "Timberland Outdoor Cargo Pants"        to 6_800.0,
                    "Carhartt Rugged Flex Cargo"            to 8_500.0
                )
            ),

            // ── DRESSES ──────────────────────────────────────────────────────────

            SubCat(
                category = "Dresses", subCategory = "Maxi",
                sizes = "S,M,L,XL,XXL",
                imageKeyword = "maxi,dress",
                shortDesc = "Flowing maxi dresses for effortless elegance.",
                longDesc = "A gorgeous floor-length maxi dress in lightweight chiffon, jersey, or satin. Flattering silhouette with adjustable straps or a defined wrap waist. Perfect for beach holidays, garden parties, or a romantic sundowner. Wrinkle-resistant and easy to pack.",
                products = listOf(
                    "Zara Floral Wrap Maxi Dress"           to 5_500.0,
                    "H&M Jersey Maxi Dress"                 to 3_200.0,
                    "ASOS Smocked Maxi Dress"               to 4_800.0,
                    "Free People Endless Summer Maxi"       to 8_500.0,
                    "Mango Printed Halter Maxi Dress"       to 6_200.0
                )
            ),

            SubCat(
                category = "Dresses", subCategory = "Midi",
                sizes = "S,M,L,XL,XXL",
                imageKeyword = "midi,dress",
                shortDesc = "Sophisticated midi dresses for any occasion.",
                longDesc = "A versatile midi dress landing just below the knee in premium fabric with elegant drape. Thoughtful details like knife pleating, wrap ties, or button-front accents add character. Transitions effortlessly from 9-to-5 to cocktail hour.",
                products = listOf(
                    "& Other Stories Pleated Midi Dress"    to 7_800.0,
                    "COS Wrap Midi Dress"                   to 6_500.0,
                    "Reformation Rib Midi Dress"            to 12_000.0,
                    "Massimo Dutti Linen Midi Dress"        to 8_200.0,
                    "Whistles Button-Front Midi Dress"      to 9_500.0
                )
            ),

            SubCat(
                category = "Dresses", subCategory = "Summer",
                sizes = "S,M,L,XL,XXL",
                imageKeyword = "sundress,women",
                shortDesc = "Light and breezy dresses made for sunny days.",
                longDesc = "A gorgeous summer dress in lightweight cotton, linen, or satin with vibrant prints and airy cuts. Whether a strappy cami slip, floral wrap, or broderie anglaise style — these dresses bring effortless warm-weather charm to any outing.",
                products = listOf(
                    "Topshop Floral Wrap Dress"             to 3_800.0,
                    "M&S Ditsy Print Sundress"              to 4_200.0,
                    "New Look Cami Slip Dress"              to 2_500.0,
                    "River Island Broderie Dress"           to 3_500.0,
                    "Next Linen Mix Shirt Dress"            to 4_800.0
                )
            ),

            SubCat(
                category = "Dresses", subCategory = "Evening",
                sizes = "S,M,L,XL,XXL",
                imageKeyword = "evening,gown",
                shortDesc = "Show-stopping gowns for your most special nights.",
                longDesc = "A dazzling evening gown in luxurious fabric with intricate beading, sequins, or flowing chiffon layers. Designed to command the room at galas, weddings, and black-tie affairs. Precision-cut for a flattering silhouette with impeccable hand-finishing.",
                products = listOf(
                    "BCBGMAXAZRIA Sequin Gown"              to 18_500.0,
                    "Adrianna Papell Beaded Gown"           to 22_000.0,
                    "Badgley Mischka Floor-Length Gown"     to 35_000.0,
                    "JS Collections Chiffon Dress"          to 15_500.0,
                    "Mac Duggal Ruched Evening Dress"       to 28_000.0
                )
            ),

            // ── SHOES ────────────────────────────────────────────────────────────

            SubCat(
                category = "Shoes", subCategory = "Sneakers",
                sizes = "39,40,41,42,43,44",
                imageKeyword = "sneakers",
                shortDesc = "Iconic sneakers that define modern street style.",
                longDesc = "Premium sneakers engineered for all-day comfort with responsive foam cushioning, durable rubber outsoles, and instantly recognisable design. Whether hitting the gym, the streets, or a casual brunch — these sneakers deliver performance and personality.",
                products = listOf(
                    "Nike Air Max 270"                      to 12_500.0,
                    "Adidas Ultra Boost 22"                 to 15_800.0,
                    "New Balance 574 Classic"               to 9_800.0,
                    "Puma RS-X Tracks Sneakers"             to 8_500.0,
                    "Reebok Classic Leather"                to 7_200.0
                )
            ),

            SubCat(
                category = "Shoes", subCategory = "Boots",
                sizes = "39,40,41,42,43,44",
                imageKeyword = "leather,boots",
                shortDesc = "Rugged boots built for style and substance.",
                longDesc = "Premium boots in full-grain leather or waterproof nubuck with Goodyear-welted or cemented construction. Superior ankle support, slip-resistant outsoles, and insulated linings where needed. Built for city streets, mountain trails, and everything between.",
                products = listOf(
                    "Timberland 6-Inch Premium Waterproof Boot"  to 18_500.0,
                    "Dr. Martens 1460 8-Eye Boot"                to 16_800.0,
                    "UGG Classic Short Boot"                     to 22_000.0,
                    "Clarks Desert Boot"                         to 12_500.0,
                    "Steve Madden Hitch Chelsea Boot"            to 9_800.0
                )
            ),

            SubCat(
                category = "Shoes", subCategory = "Loafers",
                sizes = "39,40,41,42,43,44",
                imageKeyword = "loafers,leather",
                shortDesc = "Refined loafers for the discerning dresser.",
                longDesc = "Handcrafted slip-on loafers in supple full-grain leather with a cushioned leather insole and flexible outsole. Iconic horsebit hardware or classic penny keeper detailing. Pairs beautifully with tailored trousers, chinos, or dark selvedge denim.",
                products = listOf(
                    "Gucci Horsebit 1953 Loafer"            to 45_000.0,
                    "Sperry Gold Cup Loafer"                to 12_800.0,
                    "Tod's Gommino Driving Shoe"            to 38_000.0,
                    "Cole Haan Pinch Penny Loafer"          to 14_500.0,
                    "Bass Weejuns Larson Loafer"            to 10_200.0
                )
            ),

            SubCat(
                category = "Shoes", subCategory = "Heels",
                sizes = "36,37,38,39,40,41",
                imageKeyword = "heels,pumps",
                shortDesc = "Elegant heels that elevate every look.",
                longDesc = "Stunning heels in premium leather or suede with cushioned footbeds and graceful design. From sleek pointed-toe stilettos to versatile block heels — each pair is engineered for comfort without compromising on elegance. Walk tall, walk confidently.",
                products = listOf(
                    "Jimmy Choo Bing 100mm Slingback Pump"  to 55_000.0,
                    "Stuart Weitzman Nudist Sandal Heel"    to 28_000.0,
                    "Steve Madden Carrson Platform Heel"    to 8_500.0,
                    "Nine West Tatiana Platform Heel"       to 7_200.0,
                    "Aldo Cassedi Block Heel"               to 6_500.0
                )
            ),

            // ── ACCESSORIES ──────────────────────────────────────────────────────

            SubCat(
                category = "Accessories", subCategory = "Belts",
                sizes = "One Size",
                imageKeyword = "leather,belt",
                shortDesc = "The perfect finishing touch to every outfit.",
                longDesc = "A premium belt in full-grain leather or woven canvas with a polished solid-brass buckle. Adjustable fit with multiple pin holes. The right belt pulls any outfit together — from a formal suit to slim jeans and a white tee.",
                products = listOf(
                    "Gucci GG Marmont Leather Belt"         to 18_500.0,
                    "Levi's Reversible Leather Belt"        to 3_200.0,
                    "Tommy Hilfiger Leather Belt"           to 4_500.0,
                    "Fossil Glenn Leather Belt"             to 5_800.0,
                    "H&M Braided Cotton Belt"               to 1_200.0
                )
            ),

            SubCat(
                category = "Accessories", subCategory = "Socks",
                sizes = "One Size",
                imageKeyword = "colorful,socks",
                shortDesc = "Comfort and personality from the ground up.",
                longDesc = "Premium socks in combed cotton, merino wool, or moisture-wicking performance yarn. Reinforced heel and toe for durability, cushioned sole for all-day comfort, and a snug arch band to prevent slipping. Express yourself with bold patterns — or keep it clean.",
                products = listOf(
                    "Happy Socks Bold Solid Pack (3-pair)"  to 850.0,
                    "Bombas Quarter Ankle Socks (3-pair)"   to 1_800.0,
                    "Nike Everyday Cushion Crew (3-pair)"   to 650.0,
                    "Falke Run Ergonomic Sport Sock"        to 2_500.0,
                    "Stance Classic Crew Pack (3-pair)"     to 1_200.0
                )
            ),

            SubCat(
                category = "Accessories", subCategory = "Perfumes",
                sizes = "One Size",
                imageKeyword = "perfume,fragrance",
                shortDesc = "Signature fragrances from the world's finest houses.",
                longDesc = "A luxurious fragrance crafted by master perfumers using the rarest ingredients — Bulgarian rose, Haitian vetiver, Madagascan vanilla, and more. Balanced top, heart, and base notes that evolve beautifully on skin. Long-lasting sillage guaranteed to be remembered.",
                products = listOf(
                    "Chanel No. 5 Eau de Parfum 100ml"     to 18_500.0,
                    "Dior Sauvage Eau de Toilette 100ml"   to 16_200.0,
                    "Tom Ford Black Orchid EDP 50ml"       to 22_000.0,
                    "Versace Eros Pour Femme EDP 100ml"    to 12_800.0,
                    "Armani Acqua di Giò EDT 75ml"         to 14_500.0
                )
            ),

            SubCat(
                category = "Accessories", subCategory = "Watches",
                sizes = "One Size",
                imageKeyword = "wristwatch",
                shortDesc = "Precision timepieces for every wrist.",
                longDesc = "A meticulously crafted timepiece combining precision engineering with timeless design. Sapphire crystal glass resists scratches; genuine leather or stainless-steel strap provides lasting comfort. From Swiss automatics to solar smartwatches — time it right.",
                products = listOf(
                    "Casio G-Shock GA-2100 Carbon Core"    to 8_500.0,
                    "Fossil Gen 6 44mm Smartwatch"         to 28_000.0,
                    "Daniel Wellington Classic 40mm"       to 15_500.0,
                    "Seiko 5 Sports Automatic 38mm"        to 18_200.0,
                    "Orient Bambino Version 4 Dress Watch" to 22_000.0
                )
            )
        )

        // Insert every product — images are keyword-matched via loremflickr.com
        // ?lock=N is deterministic: same N always returns the same photo
        var id = 1
        for (sub in catalog) {
            for ((name, price) in sub.products) {
                val kw  = sub.imageKeyword
                val img1 = "https://loremflickr.com/500/700/$kw/all?lock=${id}0"
                val img2 = "https://loremflickr.com/500/700/$kw/all?lock=${id}1"
                val img3 = "https://loremflickr.com/500/700/$kw/all?lock=${id}2"
                val values = ContentValues().apply {
                    put("name", name)
                    put("description", sub.shortDesc)
                    put("rich_description", sub.longDesc)
                    put("category", sub.category)
                    put("sub_category", sub.subCategory)
                    put("sizes", sub.sizes)
                    put("price", price)
                    put("image_url", img1)
                    put("images", "$img1,$img2,$img3")
                }
                db.insert("products", null, values)
                id++
            }
        }
    }
}
