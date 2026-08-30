import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/products")
public class ProductController {

    private List<Product> productList = new ArrayList<>();

    // URL mapping for adding a product
    @PostMapping("/add")
    public String addProduct(@RequestBody Product product) {
        productList.add(product);
        return "Product added successfully!";
    }

    // URL mapping for searching products
    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String name) {
        List<Product> foundProducts = new ArrayList<>();
        for (Product product : productList) {
            if (product.getName().equalsIgnoreCase(name)) {
                foundProducts.add(product);
            }
        }
        return foundProducts;
    }
}