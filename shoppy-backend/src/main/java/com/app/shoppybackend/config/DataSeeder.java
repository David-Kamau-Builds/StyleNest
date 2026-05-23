package com.app.shoppybackend.config;

import com.app.shoppybackend.entity.Product;
import com.app.shoppybackend.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner loadData(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                System.out.println("Seeding products...");
                Random random = new Random();
                int idCounter = 1;

                List<String> categories = Arrays.asList("Shirts", "Trousers", "Dresses", "Shoes", "Accessories");
                
                for (String mainCat : categories) {
                    List<String> subcategories;
                    switch (mainCat) {
                        case "Shirts": subcategories = Arrays.asList("Polos", "Tees", "Button-Downs", "Long-Sleeve"); break;
                        case "Trousers": subcategories = Arrays.asList("Jeans", "Chinos", "Sweatpants", "Cargo"); break;
                        case "Dresses": subcategories = Arrays.asList("Maxi", "Midi", "Summer", "Evening"); break;
                        case "Shoes": subcategories = Arrays.asList("Sneakers", "Boots", "Loafers", "Heels"); break;
                        case "Accessories": subcategories = Arrays.asList("Belts", "Socks", "Perfumes", "Watches"); break;
                        default: subcategories = Arrays.asList("Misc"); break;
                    }

                    for (String subCat : subcategories) {
                        for (int i = 1; i <= 5; i++) {
                            Product p = new Product();
                            p.setName("Premium " + subCat + " " + i);
                            p.setDescription("High quality " + subCat.toLowerCase() + " for everyday wear.");
                            p.setRichDescription("This " + p.getName() + " is crafted from the finest materials. Perfect for any occasion. It features breathable fabric, reinforced stitching, and a modern fit. Whether you're at the office or out for the weekend, this piece will keep you comfortable and stylish.");
                            p.setCategory(mainCat);
                            p.setSubCategory(subCat);
                            
                            double price = (10 + random.nextInt(141)) * 100.0; // 1000 to 15000
                            p.setPrice(price);

                            String img1 = "https://picsum.photos/seed/" + idCounter + "1/500/700";
                            String img2 = "https://picsum.photos/seed/" + idCounter + "2/500/700";
                            String img3 = "https://picsum.photos/seed/" + idCounter + "3/500/700";
                            
                            p.setImageUrl(img1);
                            p.setImages(img1 + "," + img2 + "," + img3);

                            String sizes;
                            if (mainCat.equals("Shoes")) {
                                sizes = "39, 40, 41, 42, 43, 44";
                            } else if (mainCat.equals("Accessories")) {
                                sizes = "One Size";
                            } else {
                                sizes = "XS, S, M, L, XL, XXL";
                            }
                            p.setSizes(sizes);
                            
                            productRepository.save(p);
                            idCounter++;
                        }
                    }
                }
                System.out.println("Seeding complete. Inserted " + (idCounter - 1) + " products.");
            }
        };
    }
}
